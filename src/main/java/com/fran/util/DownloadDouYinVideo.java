package com.fran.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 程良明
 * @date 2024/12/25
 * * * 说明:
 **/
public class DownloadDouYinVideo {
	public static void main(String[] args) {

		String url = "https://v.douyin.com/CeiJa73Ps/";
		if (url == null || url.isEmpty()) {
			System.out.println("[404, '链接为空', '']");
			return;
		}
		try {
			String str = httpRequest(url, true);
			Pattern pattern = Pattern.compile("video_id=(.*?)&", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(str);
			String vid = "";
			if (matcher.find()) {
				vid = matcher.group(1);
			}
			if (vid == null || vid.isEmpty()) {
				System.out.println("[404, '解析失败', '']");
				return;
			}
			String playUrl = "https://aweme.snssdk.com/aweme/v1/play/?video_id=" + vid + "&line=0";
			str = httpRequest(playUrl, false);
			pattern = Pattern.compile("<a href=\"(.*?)\">");
			matcher = pattern.matcher(str);
			String arr = "";
			if (matcher.find()) {
				arr = matcher.group(1);
			}
			if (arr == null || arr.isEmpty()) {
				System.out.println("[403, '解析失败', '']");
				return;
			}
			String videoUrl = arr.replace("http://", "https://");
			// 设置允许跨域等响应头（这里在命令行测试场景下更多是示意，实际Web应用场景中设置会更复杂）
			System.out.println("{\"code\":0,\"msg\":\"解析成功\",\"data\":\"" + videoUrl + "\"}");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 模拟发送HTTP请求，类似于PHP中的CurlRequest函数
	public static String httpRequest(String url, boolean followRedirects) throws IOException {
		OkHttpClient client = new OkHttpClient.Builder()
						.followRedirects(followRedirects)
						.build();
		Request request = new Request.Builder()
						.url(url)
						.header("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25")
						.build();
		try (Response response = client.newCall(request).execute()) {
			return response.body().string();
		}
	}
}
