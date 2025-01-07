package com.fran.tool;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author 程良明
 * @date 2025/1/7
 * * * 说明: AES加解密工具类
 **/
public class AESTool {
	private String algorithm = "AES/CBC/PKCS5Padding";
	private byte[] key = "DodXhJoyGamesSdk".getBytes(StandardCharsets.UTF_8);
	private byte[] iv = "Fran_ChengXhGame".getBytes(StandardCharsets.UTF_8);

	public static void main(String[] args) throws Exception {
		String str = "clm";
		AESTool aesTool = new AESTool();
		String encryptStr = aesTool.encrypt(str.getBytes(StandardCharsets.UTF_8));
		String decryptStr = Arrays.toString(aesTool.decrypt(encryptStr));
		System.out.println("encryptStr："+encryptStr);
		System.out.println("decryptStr："+decryptStr);
	}

	public String encrypt(byte[] plaintext) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		byte[] encryptedBytes = cipher.doFinal(plaintext);
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public byte[] decrypt(String ciphertext) throws Exception {
		Cipher cipher = Cipher.getInstance(algorithm);
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
		return cipher.doFinal(decodedBytes);
	}
}
