package com.fran;


import com.fran.util.Utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) throws DocumentException {
		// TODO: 2023/10/24 解析string.xml  ，替换内容，从数组
		String stringPath = "C:\\Users\\Administrator\\Desktop\\work\\xh\\string\\values-zh\\strings.xml";
		SAXReader saxReader = new SAXReader();
		Document workDocument = saxReader.read(stringPath);

		List<Element> elements = workDocument.getRootElement().elements();

		String[] values = {"海外测试",
						"账号销毁",
						"邮箱",
						"密码",
						"验证码",
						"发送",
						"提交",
						"绑定邮箱",
						"用户协议",
						"隐私协议",
						"确定退出？",
						"取消",
						"确认",
						"提示",
						"同意",
						"继续",
						"绑定",
						"忘记密码?",
						"游客模式",
						"登录",
						"登陆中",
						"切换账号",
						"旧密码",
						"新密码",
						"修改密码",
						"找回密码",
						"请输入账号邮箱用于验证，检查邮件文件夹包括垃圾箱",
						"确认发送",
						"确认",
						"前往绑定",
						"Facebook绑定",
						"Google绑定",
						"解绑后google不能再登录此账号，确定解绑吗？",
						"解绑后facebook不能再登录此账号，确定解绑吗？",
						"绑定成功",
						"解绑成功",
						"已绑定",
						"是否确定注销账号，账号数据将彻底删除",
						"不再提示",
						"注册",
						"注册",
						"邮箱",
						"请同意",
						"和",
						"网络错误，请重试",
						"登录成功",
						"邮箱绑定成功",
						"请输入邮箱和密码",
						"注册成功",
						"请输入邮箱",
						"请输入密码",
						"请输入验证码",
						"请同意用户协议",
						"游客登录",
						"Facebook登录",
						"Google登录",
						"请输入旧密码",
						"请输入新密码",
						"请输入验证码",
						"找回成功，请重新登录",
						"支付成功",
						"支付取消",
						"支付失败",
						"修改密码成功，重新登录",
						"请输入:我确认删除账号",
						"我确认删除账号",
						"内容不正确，请重新输入"};

		int i = 0;
		for (Element element : elements) {
			String name = element.attributeValue("name");
			String value = element.getStringValue();
			String targetValue = values[i];
			element.setText(targetValue);
			i++;
		}
		writeXmlFile(stringPath, workDocument);

	}

	private static void writeXmlFile(String outPutPath, Document document) {
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