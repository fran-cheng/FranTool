package com.fran.tool;

import com.fran.util.Utils;
import com.google.gson.JsonObject;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final byte[] KEY = "DodXhJoyGamesSdk".getBytes(StandardCharsets.UTF_8);
	private static final byte[] IV = "Fran_ChengXhGame".getBytes(StandardCharsets.UTF_8);

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
		Element applicationElement = workManifestElement.element("application");

		String applicationName = applicationElement.attributeValue(new QName("name", androidNamespace));

		if (applicationName != null && !applicationName.isEmpty()) {
			File file = new File(workDir, Utils.linkPath("assets", "xh", "xhData.xh"));
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("application", applicationName);
			Utils.writeFile(file, jsonObject.toString(), "utf-8");

			applicationElement.setAttributeValue(new QName("name", androidNamespace), "xh.sdk.test.XhApplication");
		}
		writeXmlFile(workManifestPath, workDocument);
		System.out.println("workPackName:" + workPackName);
		System.out.println("applicationName:" + applicationName);
	}

	private void writeXmlFile(String outPutPath, Document document) {
		Utils.log("写入: " + outPutPath);
		XMLWriter writer = null;
		try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(outPutPath))) {
			writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
			writer.write(document);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
