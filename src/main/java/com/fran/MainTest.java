package com.fran;


import com.fran.util.Utils;

import org.dom4j.DocumentException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) throws DocumentException {
		String filePath = "E:\\temp\\IpMac";
		File fileDir = new File(filePath);
		List<String> arpStaticList = new ArrayList<>();
		List<String> dhcpStaticList = new ArrayList<>();
		StringBuilder dhcpStrBuild = new StringBuilder();
		StringBuilder arpStrBuild = new StringBuilder();
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
//			static-bind ip-address 172.16.3.253 mask 255.255.252.0 hardware-address 9009-d034-af59
			StringBuilder macTemp = new StringBuilder();
			int i = 0;
			for (String s : mac.toLowerCase().split("-")) {
				if (i == 2 || i == 4) {
					macTemp.append("-");
				}
				macTemp.append(s);
				i++;
			}
//			static-bind ip-address 172.16.3.253 mask 255.255.252.0 hardware-address 9009-d034-af59
			String strDhcp = String.format("static-bind ip-address %s mask 255.255.252.0 hardware-address %s", ipv4, macTemp);
//			arp static 172.16.0.10 d85e-d35f-842c 1721 GigabitEthernet1/0/17
			String strArp = String.format("arp static %s %s 1721 GigabitEthernet1/0/17", ipv4, macTemp);
			nameMac.put(name, strDhcp + "::" + strArp);

		}

		for (String strDhcpArp : nameMac.values()) {
			String[] strs = strDhcpArp.split("::");
			String dhcpStr = strs[0];
			String arpStr = strs[1];
			dhcpStaticList.add(dhcpStr);
			arpStaticList.add(arpStr);
			dhcpStrBuild.append(dhcpStr).append("\n\r");
			arpStrBuild.append(arpStr).append("\n\r");
		}
		File dhcpFile = new File("C:\\Users\\Fran\\Desktop\\temp\\ip配置\\dhcpFile.txt");
		File arpFile = new File("C:\\Users\\Fran\\Desktop\\temp\\ip配置\\arpFile.txt");
		Utils.writeFile(dhcpFile, dhcpStrBuild.toString(), "utf-8");
		Utils.writeFile(arpFile, arpStrBuild.toString(), "utf-8");
	}


}