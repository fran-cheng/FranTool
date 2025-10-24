#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
check_page_alignment_windows.py
A Windows-friendly script (pure Python) that replicates the behavior of the Bash script:
- Works on .so files directly
- Works on directories (recursively finds *.so)
- Works on .apk files by extracting and scanning embedded *.so files
It does NOT require objdump or unzip; it parses ELF headers and APKs with zipfile.
"""

import os
import sys
import math
import struct
import tempfile
import zipfile
import argparse
from pathlib import Path
from typing import List, Tuple, Optional

# ============================= Colors / UI ====================================

# ANSI colors (Windows 10+ supports these in most terminals; otherwise they'll show raw codes)
RED   = "\033[0;31m"
GREEN = "\033[0;32m"
BLUE  = "\033[0;34m"
YELLOW= "\033[1;33m"
NC    = "\033[0m"

def hrule(width: int = 120) -> str:
    return "=" * width

def print_table_header():
    print(f"{BLUE}{'Status':<8} {'File Path':<50} {'Architecture':<12} {'Alignment':<12} {'Notes'}{NC}")
    print(hrule())

def print_table_row(symbol: str, filepath: str, arch: str, alignment: str, notes: str):
    color = GREEN if symbol == "✅" else RED if symbol == "❌" else NC
    print(f"{color}{symbol:<8}{NC} {filepath:<50} {arch:<12} {alignment:<12} {notes}")

# ============================= ELF parsing ====================================

ELF_MAGIC = b"\x7fELF"
ELFCLASS32 = 1
ELFCLASS64 = 2
ELFDATA2LSB = 1
ELFDATA2MSB = 2

PT_LOAD = 1

# e_machine values
EM_386     = 3
EM_ARM     = 40
EM_X86_64  = 62
EM_AARCH64 = 183

def arch_name(e_machine: int) -> str:
    if e_machine == EM_AARCH64:
        return "ARM64"
    if e_machine == EM_X86_64:
        return "x86_64"
    if e_machine == EM_386:
        return "x86"
    if e_machine == EM_ARM:
        return "ARM v7"
    return "unknown"

def read_elf_info(path: str) -> Tuple[Optional[int], Optional[int], Optional[int], Optional[int], Optional[int]]:
    """
    Returns (elf_class, data_encoding, e_machine, e_phoff, e_phnum) or (None,... ) if not ELF.
    """
    with open(path, "rb") as f:
        head = f.read(64)  # enough for both 32/64 headers
        if len(head) < 16 or head[:4] != ELF_MAGIC:
            return (None, None, None, None, None)

        elf_class = head[4]
        data_enc  = head[5]  # 1 little, 2 big
        # e_machine is at offset 18 (0x12), 2 bytes
        if data_enc == ELFDATA2LSB:
            e_machine = struct.unpack_from("<H", head, 18)[0]
        elif data_enc == ELFDATA2MSB:
            e_machine = struct.unpack_from(">H", head, 18)[0]
        else:
            return (None, None, None, None, None)

        # e_phoff, e_phentsize, e_phnum differ by class
        if elf_class == ELFCLASS32:
            # Offsets: e_phoff @ 0x1C (4), e_phentsize @ 0x2A (2), e_phnum @ 0x2C (2)
            f.seek(0)
            ehdr = f.read(52)  # 32-bit ELF header size
            if data_enc == ELFDATA2LSB:
                e_phoff     = struct.unpack_from("<I", ehdr, 0x1C)[0]
                e_phentsize = struct.unpack_from("<H", ehdr, 0x2A)[0]
                e_phnum     = struct.unpack_from("<H", ehdr, 0x2C)[0]
            else:
                e_phoff     = struct.unpack_from(">I", ehdr, 0x1C)[0]
                e_phentsize = struct.unpack_from(">H", ehdr, 0x2A)[0]
                e_phnum     = struct.unpack_from(">H", ehdr, 0x2C)[0]
        elif elf_class == ELFCLASS64:
            # Offsets: e_phoff @ 0x20 (8), e_phentsize @ 0x36 (2), e_phnum @ 0x38 (2)
            f.seek(0)
            ehdr = f.read(64)  # 64-bit ELF header size
            if data_enc == ELFDATA2LSB:
                e_phoff     = struct.unpack_from("<Q", ehdr, 0x20)[0]
                e_phentsize = struct.unpack_from("<H", ehdr, 0x36)[0]
                e_phnum     = struct.unpack_from("<H", ehdr, 0x38)[0]
            else:
                e_phoff     = struct.unpack_from(">Q", ehdr, 0x20)[0]
                e_phentsize = struct.unpack_from(">H", ehdr, 0x36)[0]
                e_phnum     = struct.unpack_from(">H", ehdr, 0x38)[0]
        else:
            return (None, None, None, None, None)

        return (elf_class, data_enc, e_machine, e_phoff, e_phnum)

def max_load_align_exponent(path: str) -> Optional[int]:
    """
    Iterate program headers and return max log2(p_align) for PT_LOAD segments.
    If p_align is 0, treat as 0 (i.e., 2**0).
    """
    info = read_elf_info(path)
    if info[0] is None:
        return None
    elf_class, data_enc, e_machine, e_phoff, e_phnum = info
    if e_phoff is None or e_phnum is None:
        return None

    # Program header entry sizes:
    if elf_class == ELFCLASS32:
        # p_type(4), p_offset(4), p_vaddr(4), p_paddr(4), p_filesz(4), p_memsz(4), p_flags(4), p_align(4)
        ph_size = 32
        if data_enc == ELFDATA2LSB:
            fmt = "<IIIIIIII"
        else:
            fmt = ">IIIIIIII"
        p_type_idx = 0
        p_align_idx = 7
    else:
        # 64-bit: p_type(4), p_flags(4), p_offset(8), p_vaddr(8), p_paddr(8), p_filesz(8), p_memsz(8), p_align(8)
        ph_size = 56
        if data_enc == ELFDATA2LSB:
            fmt = "<IIQQQQQQ"
        else:
            fmt = ">IIQQQQQQ"
        p_type_idx = 0
        p_align_idx = 7

    max_exp = None
    with open(path, "rb") as f:
        f.seek(e_phoff)
        for _ in range(e_phnum):
            data = f.read(ph_size)
            if len(data) != ph_size:
                break
            fields = struct.unpack(fmt, data)
            p_type = fields[p_type_idx]
            if p_type == PT_LOAD:
                p_align = fields[p_align_idx]
                if p_align == 0:
                    exp = 0
                else:
                    # p_align can be any number; compute floor(log2(p_align))
                    exp = int(math.log2(p_align)) if p_align > 0 else 0
                max_exp = exp if max_exp is None else max(max_exp, exp)

    return max_exp

def get_arch(path: str) -> str:
    info = read_elf_info(path)
    if info[0] is None:
        return "Unknown"
    e_machine = info[2]
    return arch_name(e_machine)

def is_arm_v7(path: str) -> bool:
    info = read_elf_info(path)
    return info[0] is not None and info[2] == EM_ARM

# ============================= Core logic =====================================

Result = Tuple[str, str, str, str, str]  # (status_type, notes, symbol, alignment, filepath, arch) - aligns w/ Bash script idea

def check_so_alignment(so_file: str, relative_path: str) -> Result:
    if not os.path.isfile(so_file):
        return ("ERROR", "File not found", "❌", "0B", relative_path, "Unknown")

    # Detect ELF
    with open(so_file, "rb") as f:
        magic = f.read(4)
    if magic != ELF_MAGIC:
        return ("ERROR", "Not an ELF file", "❌", "Unknown", relative_path, "Unknown")

    arch = get_arch(so_file)

    if is_arm_v7(so_file):
        return ("PASS", "ARM v7 - 16KB not required", "✅", "N/A", relative_path, arch)

    max_exp = max_load_align_exponent(so_file)
    if max_exp is None:
        return ("ERROR", "Unknown alignment", "❌", "Unknown", relative_path, arch)

    # Human display for alignment
    if max_exp >= 10:
        kb = 1 << (max_exp - 10)
        alignment_disp = f"{kb}KB"
    else:
        b = 1 << max_exp
        alignment_disp = f"{b}B"

    if max_exp >= 14:
        return ("PASS", ">=16KB aligned", "✅", alignment_disp, relative_path, arch)
    else:
        return ("FAIL", "Need 16KB alignment", "❌", alignment_disp, relative_path, arch)

def process_results(results: List[Result]):
    total_so = 0
    aligned_so = 0
    failed_so = 0
    passes: List[Tuple[str,str,str,str,str]] = []
    fails:  List[Tuple[str,str,str,str,str]] = []

    for r in results:
        if not r:
            continue
        total_so += 1
        status_type, notes, symbol, alignment, filepath, arch = r
        if status_type == "PASS":
            aligned_so += 1
            passes.append((symbol, filepath, arch, alignment, notes))
        else:
            failed_so += 1
            fails.append((symbol, filepath, arch, alignment, notes))

    if passes:
        print(f"\n{GREEN}✅ Compatible Libraries{NC}")
        print_table_header()
        for symbol, filepath, arch, alignment, notes in passes:
            print_table_row(symbol, filepath, arch, alignment, notes)

    if fails:
        print(f"\n{RED}❌ Not Compatible Libraries{NC}")
        print_table_header()
        for symbol, filepath, arch, alignment, notes in fails:
            print_table_row(symbol, filepath, arch, alignment, notes)

    print("")
    print(hrule())
    print(f"{BLUE}📊 SUMMARY{NC}")
    print(f"Total .so files: {total_so}")
    print(f"16KB Aligned (>=): {GREEN}{aligned_so}{NC}")
    print(f"Not aligned: {RED}{failed_so}{NC}")

    if failed_so == 0:
        print(f"\n{GREEN}🎉 All .so files are properly aligned!{NC}")
    else:
        print(f"\n{RED}⚠️  Some .so files need alignment fixes{NC}")

def process_apk(apk_file: str):
    print(f"{BLUE}📱 Processing APK: {apk_file}{NC}")
    print("=================================================")
    results: List[Result] = []
    with tempfile.TemporaryDirectory() as temp_dir:
        try:
            with zipfile.ZipFile(apk_file, 'r') as z:
                z.extractall(temp_dir)
        except Exception as e:
            print(f"{RED}❌ Failed to extract APK file: {e}{NC}")
            return
        for root, _, files in os.walk(temp_dir):
            for name in files:
                if name.lower().endswith(".so"):
                    so_path = os.path.join(root, name)
                    rel_path = os.path.relpath(so_path, temp_dir)
                    results.append(check_so_alignment(so_path, rel_path))

    if not results:
        print(f"{YELLOW}⚠️  No .so files found in APK{NC}")
        return
    process_results(results)

def process_single_so(so_file: str):
    print(f"{BLUE}🔍 Processing single .so file: {so_file}{NC}")
    print("=================================================")
    result = check_so_alignment(so_file, os.path.basename(so_file))
    process_results([result])

def process_directory(directory: str):
    print(f"{BLUE}📁 Processing directory: {directory}{NC}")
    print("=================================================")
    results: List[Result] = []
    for root, _, files in os.walk(directory):
        for name in files:
            if name.lower().endswith(".so"):
                so_path = os.path.join(root, name)
                rel_path = os.path.relpath(so_path, directory)
                results.append(check_so_alignment(so_path, rel_path))
    if not results:
        print(f"{YELLOW}⚠️  No .so files found in directory{NC}")
        return
    process_results(results)

def main():
    parser = argparse.ArgumentParser(description="Check .so PT_LOAD alignment (>=16KB) inside ELF files, dirs, or APKs.")
    parser.add_argument("input", help="Path to .apk, .so, or a directory")
    args = parser.parse_args()

    inp = args.input
    if not os.path.exists(inp):
        print(f"{RED}❌ Invalid input: not found{NC}")
        sys.exit(1)

    if os.path.isfile(inp):
        lower = inp.lower()
        if lower.endswith(".apk"):
            process_apk(inp)
        elif lower.endswith(".so"):
            process_single_so(inp)
        else:
            print(f"{RED}❌ Unsupported file type (expected .apk or .so){NC}")
            sys.exit(1)
    elif os.path.isdir(inp):
        process_directory(inp)
    else:
        print(f"{RED}❌ Invalid input{NC}")
        sys.exit(1)

if __name__ == "__main__":
    main()
