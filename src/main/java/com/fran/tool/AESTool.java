package com.fran.tool;

import java.nio.charset.StandardCharsets;
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
	private final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private final byte[] KEY = "DodXhJoyGamesSdk".getBytes(StandardCharsets.UTF_8);
	private final byte[] IV = "Fran_ChengXhGame".getBytes(StandardCharsets.UTF_8);

	public static void main(String[] args) throws Exception {
		String str = "clm";
		AESTool aesTool = new AESTool();
		String encryptStr = aesTool.encrypt(str.getBytes(StandardCharsets.UTF_8));
		String decryptStr = new String(aesTool.decrypt(encryptStr));
		System.out.println("encryptStr：" + encryptStr);
		System.out.println("decryptStr：" + decryptStr);
	}

	public String encrypt(byte[] plaintext) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		byte[] encryptedBytes = cipher.doFinal(plaintext);
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public byte[] decrypt(String ciphertext) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
		return cipher.doFinal(decodedBytes);
	}
}
