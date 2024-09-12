package com.fran;


import com.fran.util.Utils;

import org.dom4j.DocumentException;

import java.io.File;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) throws DocumentException {
		String filePath = "E:\\temp\\IpMac";
		File fileDir = new File(filePath);

		File[] files = fileDir.listFiles();
		for (File file : files) {

			String name = file.getName().split("-")[1];
			System.out.println(name);
			String content = Utils.read(file);
			int macSIndex = content.indexOf("Address.........:");
			if (macSIndex <= 0) {
				continue;
			}
			macSIndex = macSIndex + "Address.........:".length();
			int macEIndex = content.indexOf("DHCPEnabled...........:Yes");
			if (macEIndex <= 0) {
				continue;
			}
			String mac = content.substring(macSIndex, macEIndex);
			System.out.println(mac);
			int ipv4SIndex = content.indexOf("IPv4Address...........:");
			if (ipv4SIndex <= 0) {
				continue;
			}
			ipv4SIndex = ipv4SIndex + "IPv4Address...........:".length();
			int ipv4EIndex = content.indexOf("(Preferred)SubnetMask");
			if (ipv4EIndex <= 0) {
				continue;
			}

			String ipv4 = content.substring(ipv4SIndex, ipv4EIndex);
			System.out.println(ipv4);
			System.out.println();
		}
	}


}