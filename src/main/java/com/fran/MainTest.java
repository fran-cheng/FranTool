package com.fran;


import com.fran.util.Utils;

import org.dom4j.DocumentException;

import java.io.File;
import java.util.HashMap;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) throws DocumentException {
		String filePath = "E:\\temp\\IpMac";
		File fileDir = new File(filePath);

		HashMap<String, String> nameMac = new HashMap<>();

		File[] files = fileDir.listFiles();
		for (File file : files) {

			String name = file.getName().split("-")[1];
//			System.out.println(name);
			String content = Utils.read(file);
			String[] contentList = content.split("172.16.");

			String contentMac = contentList[0];
			String contentIp = contentList[1];
			int macSIndex = contentMac.lastIndexOf("Address.........:");
			if (macSIndex <= 0) {
				continue;
			}
			macSIndex = macSIndex + "Address.........:".length();
			int macEIndex = contentMac.lastIndexOf("DHCPEnabled...........:Yes");
			if (macEIndex <= 0) {
				continue;
			}
			String mac = contentMac.substring(macSIndex, macEIndex);
//			System.out.println(mac);
//			int ipv4SIndex = contentIp.indexOf("IPv4Address...........:");
//			if (ipv4SIndex <= 0) {
//				continue;
//			}
//			ipv4SIndex = ipv4SIndex + "IPv4Address...........:".length();
//			int ipv4EIndex = contentIp.indexOf("(Preferred)SubnetMask");
//			if (ipv4EIndex <= 0) {
//				continue;
//			}
			int ipv4EIndex = contentIp.indexOf("(Preferred)SubnetMask");

			String ipv4 = "172.16." + contentIp.substring(0, ipv4EIndex);
//			System.out.println(ipv4);
//			System.out.println();

			if (nameMac.containsKey(name)) {
				String macIp = nameMac.get(name);
				System.out.println(name + ":重复上报: ip=" + ipv4);
				if (ipv4.equals(macIp.split(":")[1])) {
					System.out.println("ip相同: " + ipv4);
				} else {
					System.out.println("ip不相同: " + ipv4 + "  !=  " + macIp.split(":")[1]);
				}
			}
			nameMac.put(name, mac + ":" + ipv4);
		}


	}


}