package com.fran.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 程良明
 * @date 2024/3/11
 * * * 说明:计算smali文件的最大方法数
 **/
public class SmaliFileMethodHelper {

	public static final SmaliFileMethodHelper getInstance() {
		return InnerHolder.sInstance;
	}

	public static void main(String[] args) {
		String classPath = "E:\\www\\hw\\10007-240307120819429111809\\smali_classes2\\com\\bytedance\\sdk\\openadsdk\\activity";
		Set<String> methodCount = SmaliFileMethodHelper.getInstance().getMethodCount(classPath);
		System.out.println("clm count : " + methodCount);
		System.out.println("clm count : " + methodCount.size());

		String str = " Lcom/bytedance/sdk/openadsdk/core/eB;->nz(Landroid/content/Context;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; onCreate(Landroid/os/Bundle;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; onNewIntent(Landroid/content/Intent;)V, Lcom/bytedance/sdk/openadsdk/activity/TTBaseActivity;->onResume()V, Lcom/bytedance/sdk/openadsdk/tool/nz;->nz(Ljava/util/List;)Ljava/lang/String;, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; dispatchTouchEvent(Landroid/view/MotionEvent;)Z, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V, Lcom/bytedance/sdk/openadsdk/dislike/oUa;->oUa()Z, Ljava/util/Map;->size()I, Lcom/bytedance/sdk/openadsdk/activity/TTBaseActivity;-><init>()V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; <init>()V, Lcom/bytedance/sdk/openadsdk/core/model/tdC;->Ft()Lorg/json/JSONObject;, Lcom/bytedance/sdk/openadsdk/dislike/oUa;-><init>(Landroid/content/Context;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/core/Dla;->sn()Z, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->oUa()V, Lcom/safedk/android/analytics/brandsafety/BrandSafetyUtils;->detectAdClick(Landroid/content/Intent;Ljava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/core/model/tdC;->KMF()Ljava/lang/String;, Landroid/app/Activity;->setIntent(Landroid/content/Intent;)V, Ljava/lang/Object;-><init>()V, Lcom/bytedance/sdk/openadsdk/core/AXx;->nz()V, Landroid/view/Window;->getAttributes()Landroid/view/WindowManager$LayoutParams;, Lcom/bytedance/sdk/openadsdk/core/hGN;->oUa()Lcom/bytedance/sdk/openadsdk/core/hGN;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Ljava/lang/String;)V, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; oUa()V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->qs()V, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;, Landroid/app/Dialog;->isShowing()Z, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z, Lcom/safedk/android/analytics/brandsafety/DetectTouchUtils;->activityOnTouch(Ljava/lang/String;Landroid/view/MotionEvent;)V, Ljava/util/HashMap;-><init>()V, Ljava/util/Collections;->synchronizedMap(Ljava/util/Map;)Ljava/util/Map;, Landroid/app/Activity;->onDestroy()V, Lcom/bytedance/sdk/openadsdk/core/eB;->nz()Landroid/content/Context;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Lcom/bytedance/sdk/openadsdk/core/model/tdC;Ljava/lang/String;Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;)V, Lcom/bytedance/sdk/openadsdk/core/model/tdC;->ol()Ljava/util/List;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; onResume()V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;Ljava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; safedk_Context_startActivity_97cb3195734cf5c9cc3418feeafa6dd6(Landroid/content/Context;Landroid/content/Intent;)V, Ljava/util/Map;->remove(Ljava/lang/Object;)Ljava/lang/Object;, Landroid/app/Activity;->getIntent()Landroid/content/Intent;, Lcom/bytedance/sdk/openadsdk/core/hGN;->Yu(Ljava/lang/String;)V, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;)Lcom/bytedance/sdk/openadsdk/core/AXx;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz()Ljava/util/Map;, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; nz(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/tool/nz;->nz(Ljava/lang/String;)Ljava/util/List;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; nz(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;)Lcom/bytedance/sdk/openadsdk/core/AXx;, Lcom/bytedance/sdk/openadsdk/activity/TTBaseActivity;->dispatchTouchEvent(Landroid/view/MotionEvent;)Z, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; onDestroy()V, Lcom/bytedance/sdk/openadsdk/dislike/oUa;->nz(Ljava/lang/String;)V, Landroid/view/Window;->setAttributes(Landroid/view/WindowManager$LayoutParams;)V, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;, Landroid/app/Activity;->onNewIntent(Landroid/content/Intent;)V, Lcom/bytedance/sdk/openadsdk/core/AXx;->nz(Lcom/bytedance/sdk/openadsdk/core/AXx$nz;)V, Lcom/bytedance/sdk/openadsdk/multipro/oUa;->qs()Z, Lorg/json/JSONObject;->toString()Ljava/lang/String;, Lcom/bytedance/sdk/openadsdk/core/hGN;->qs(Ljava/lang/String;)Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;, Lcom/bytedance/sdk/openadsdk/dislike/oUa;->nz(Z)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; nz()Ljava/util/Map;, Landroid/app/Activity;->finish()V, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity$1; nz()V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; qs()V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity$1; nz(ILjava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; nz(Lcom/bytedance/sdk/openadsdk/core/model/tdC;Ljava/lang/String;Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;)V, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V, Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;->nz()V, Landroid/app/Activity;->getWindow()Landroid/view/Window;, Lcom/bytedance/sdk/component/utils/Dla;->Yu()Z, Lcom/bytedance/sdk/openadsdk/core/hGN;->nz(Ljava/lang/String;Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;)V, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity$1;-><init>(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;Ljava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity$1; <init>(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;Ljava/lang/String;)V, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->safedk_Context_startActivity_97cb3195734cf5c9cc3418feeafa6dd6(Landroid/content/Context;Landroid/content/Intent;)V, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; nz(Lcom/bytedance/sdk/openadsdk/core/model/tdC;Ljava/lang/String;)V, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; nz(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;Ljava/lang/String;)V, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; nz(Ljava/lang/String;)V, Lcom/safedk/android/utils/Logger;->d(Ljava/lang/String;)I, Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity; <clinit>()V\n";
		String str2 = "Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity$1;-><init>(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;Ljava/lang/String;)V,Ljava/lang/Object;-><init>()V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity$1;->nz()V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;)Lcom/bytedance/sdk/openadsdk/core/AXx;,Lcom/bytedance/sdk/openadsdk/dislike/oUa;->oUa()Z,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;Ljava/lang/String;)V,Landroid/app/Activity;->finish()V,Lcom/bytedance/sdk/openadsdk/dislike/oUa;->nz(Z)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity$1;->nz(ILjava/lang/String;)V,Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V,Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;,Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz()Ljava/util/Map;,Ljava/util/Map;->size()I,Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z,Lcom/bytedance/sdk/openadsdk/multipro/oUa;->qs()Z,Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;,Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;->nz()V,Lcom/bytedance/sdk/openadsdk/core/hGN;->oUa()Lcom/bytedance/sdk/openadsdk/core/hGN;,Lcom/bytedance/sdk/openadsdk/core/hGN;->qs(Ljava/lang/String;)Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;,Lcom/bytedance/sdk/openadsdk/core/hGN;->Yu(Ljava/lang/String;)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;-><clinit>()V,Ljava/util/HashMap;-><init>()V,Ljava/util/Collections;->synchronizedMap(Ljava/util/Map;)Ljava/util/Map;,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;-><init>()V,Lcom/bytedance/sdk/openadsdk/activity/TTBaseActivity;-><init>()V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Ljava/lang/String;)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Lcom/bytedance/sdk/openadsdk/core/model/tdC;Ljava/lang/String;)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Lcom/bytedance/sdk/openadsdk/core/model/tdC;Ljava/lang/String;Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;)V,Lcom/bytedance/sdk/openadsdk/core/eB;->nz()Landroid/content/Context;,Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V,Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;,Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;,Lcom/bytedance/sdk/openadsdk/core/model/tdC;->KMF()Ljava/lang/String;,Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;,Lcom/bytedance/sdk/openadsdk/core/model/tdC;->ol()Ljava/util/List;,Lcom/bytedance/sdk/openadsdk/tool/nz;->nz(Ljava/util/List;)Ljava/lang/String;,Lcom/bytedance/sdk/openadsdk/core/model/tdC;->Ft()Lorg/json/JSONObject;,Lorg/json/JSONObject;->toString()Ljava/lang/String;,Lcom/bytedance/sdk/openadsdk/core/hGN;->nz(Ljava/lang/String;Lcom/bytedance/sdk/openadsdk/core/bannerexpress/nz$nz;)V,Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->safedk_Context_startActivity_97cb3195734cf5c9cc3418feeafa6dd6(Landroid/content/Context;Landroid/content/Intent;)V,Ljava/util/Map;->remove(Ljava/lang/Object;)Ljava/lang/Object;,Lcom/bytedance/sdk/component/utils/Dla;->Yu()Z,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->nz(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V,Lcom/bytedance/sdk/openadsdk/tool/nz;->nz(Ljava/lang/String;)Ljava/util/List;,Lcom/bytedance/sdk/openadsdk/dislike/oUa;-><init>(Landroid/content/Context;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;)V,Lcom/bytedance/sdk/openadsdk/dislike/oUa;->nz(Ljava/lang/String;)V,Lcom/bytedance/sdk/openadsdk/core/AXx;->nz(Lcom/bytedance/sdk/openadsdk/core/AXx$nz;)V,Lcom/bytedance/sdk/openadsdk/core/AXx;->nz()V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->oUa()V,Landroid/app/Activity;->getWindow()Landroid/view/Window;,Landroid/view/Window;->getAttributes()Landroid/view/WindowManager$LayoutParams;,Landroid/view/Window;->setAttributes(Landroid/view/WindowManager$LayoutParams;)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->qs()V,Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I,Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;,Lcom/safedk/android/utils/Logger;->d(Ljava/lang/String;)I,Lcom/safedk/android/analytics/brandsafety/BrandSafetyUtils;->detectAdClick(Landroid/content/Intent;Ljava/lang/String;)V,Landroid/content/Context;->startActivity(Landroid/content/Intent;)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->dispatchTouchEvent(Landroid/view/MotionEvent;)Z,Lcom/safedk/android/analytics/brandsafety/DetectTouchUtils;->activityOnTouch(Ljava/lang/String;Landroid/view/MotionEvent;)V,Lcom/bytedance/sdk/openadsdk/activity/TTBaseActivity;->dispatchTouchEvent(Landroid/view/MotionEvent;)Z,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->onCreate(Landroid/os/Bundle;)V,Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V,Lcom/bytedance/sdk/openadsdk/core/Dla;->sn()Z,Landroid/app/Activity;->getIntent()Landroid/content/Intent;,Lcom/bytedance/sdk/openadsdk/core/eB;->nz(Landroid/content/Context;)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->onDestroy()V,Landroid/app/Activity;->onDestroy()V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->onNewIntent(Landroid/content/Intent;)V,Landroid/app/Activity;->onNewIntent(Landroid/content/Intent;)V,Landroid/app/Activity;->setIntent(Landroid/content/Intent;)V,Lcom/bytedance/sdk/openadsdk/activity/TTDelegateActivity;->onResume()V,Lcom/bytedance/sdk/openadsdk/activity/TTBaseActivity;->onResume()V,Landroid/app/Dialog;->isShowing()";

		TreeSet treeSet = new TreeSet<String>();
		TreeSet treeSet2 = new TreeSet<String>();
		for (String tempStr : str.split(",")) {
			treeSet.add(tempStr.trim());
		}
		for (String tempStr : str2.split(",")) {
			treeSet2.add(tempStr.trim());
		}
		System.out.println("treeSet:");
		System.out.println(treeSet);
		System.out.println("treeSet2:");
		System.out.println(treeSet2);


		treeSet.removeAll(treeSet2);
		System.out.println("removeAll:");
		System.out.println(treeSet);

	}

	public Set<String> getMethodCount(String path) {
		return getMethodCount(new File(path));
	}

	public Set<String> getMethodCount(File file) {
		getMethodCountByDir(file);
		return mMethodCount;
	}

	public void getMethodCountByDir(File file) {
		if (file.isDirectory()) {
			File[] listFile = file.listFiles();
			if (listFile != null) {
				for (File tempFile : listFile) {
					getMethodCountByDir(tempFile);
				}
			}
		} else {
			getMethodCountByFile(file);
		}
	}

	private void getMethodCountByFile(File file) {
		if (!file.isFile()) {
			return;
		}
		String content = readFileFromLine(file);
		processMethodCount(content);
	}

	private Set<String> mMethodCount = new HashSet<>();

	private void processMethodCount(String content) {
		String className = getClassByFile(content);
		String regex = "\\.method\\s.+|invoke-.*->.*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String line = matcher.group();
			String targetStr;
			if (line.startsWith(".method")) {
				targetStr = makeMethodInvoke(line, className);
			} else {
				targetStr = parseMethodInvoke(line);
			}
			mMethodCount.add(targetStr.trim());
		}
	}

	private String parseMethodInvoke(String line) {
		return line.substring(line.lastIndexOf(" "));
	}

	private String makeMethodInvoke(String line, String className) {
		return className + "->" + line.substring(line.lastIndexOf(" ")).trim();
	}

	private String readFileFromLine(File file) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line).append("\r");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	private String getClassByFile(String content) {
		String className = "";
		String regex = "\\.class.+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			String line = matcher.group();
			className = line.substring(line.indexOf(" L"));
		}
		return className;
	}

	private SmaliFileMethodHelper() {

	}

	private static class InnerHolder {
		private static SmaliFileMethodHelper sInstance = new SmaliFileMethodHelper();
	}
}
