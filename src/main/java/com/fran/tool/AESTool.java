package com.fran.tool;

import com.fran.util.Utils;
import com.google.gson.JsonObject;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;

import java.io.File;
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

	public void changeManifestXml(String workPath) throws DocumentException {
		File workDir = new File(workPath);
		String workManifestPath = Utils.linkPath(workPath, "AndroidManifest.xml");
		SAXReader saxReader = new SAXReader();
		Document workDocument = null;
		workDocument = saxReader.read(workManifestPath);
		String workPackName = workDocument.getRootElement().attributeValue("package");
		Namespace androidNamespace = Namespace.get("android", "http://schemas.android.com/apk/res/android");
		Element workManifestElement = workDocument.getRootElement();
		String applicationName = workManifestElement.element("application").attributeValue(new QName("name", androidNamespace));

		if (applicationName != null && !applicationName.isEmpty()) {
			// TODO: 2025/1/7 清单文件或者smali里面写入原来的application的位置
//			File[] workSmaliFiles = workDir.listFiles((file, s) -> {
//				String fileName = s.toLowerCase();
//				return fileName.startsWith("smali");
//			});
//
//			String applicationPath = applicationName.replaceAll("\\.", "/") + ".smali";
//			assert workSmaliFiles != null;
//			for (File file : workSmaliFiles) {
//				File applicationFile = new File(file, applicationPath);
//				if (applicationFile.exists()) {
//					// TODO: 2025/1/7 修改smali的继承关系(或者通过直接在壳的application里面直接调用？)
//					System.out.println("applicationFile.exists():"+applicationFile.getPath());
//				}
//			}
			// TODO: 2025/1/8 先写清单文件，或者写文件 base64加密？
			File file = new File(workDir, Utils.linkPath("assets", "xh", "xhData.xh"));
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("application", applicationName);
			Utils.writeFile(file, jsonObject.toString(), "utf-8");

		}
		System.out.println("workPackName:" + workPackName);
		System.out.println("applicationName:" + applicationName);
	}
}
