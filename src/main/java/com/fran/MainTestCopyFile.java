package com.fran;

import java.io.File;

/**
 * @author 程良明
 * @date 2024/6/6
 * * * 说明: copy 文件处理
 **/
public class MainTestCopyFile {
	public static void main(String[] args) {
		String[] strs = {
						"abc_list_pressed_holo_dark.9.png",
						"abc_list_pressed_holo_dark.9.png",
						"abc_list_pressed_holo_dark.9.png",
						"abc_list_pressed_holo_dark.9.png",
						"abc_list_selector_disabled_holo_dark.9.png",
						"abc_list_selector_disabled_holo_dark.9.png",
						"abc_list_selector_disabled_holo_dark.9.png",
						"abc_list_selector_disabled_holo_dark.9.png",
						"notification_bg_low_normal.9.png",
						"notification_bg_low_normal.9.png",
						"notification_bg_low_normal.9.png",
						"notification_bg_low_pressed.9.png",
						"notification_bg_low_pressed.9.png",
						"notification_bg_low_pressed.9.png",
						"notification_bg_normal.9.png",
						"notification_bg_normal.9.png",
						"notification_bg_normal.9.png",
						"notification_bg_normal_pressed.9.png",
						"notification_bg_normal_pressed.9.png",
						"notification_bg_normal_pressed.9.png",
		};
		File originResFile = new File("E:\\Downloaded\\pocketpiggy-release\\res");
		File targetResFile = new File("E:\\Downloaded\\gf-17-240606214543675381384\\res");

		for (File tempFileDir : targetResFile.listFiles()) {
//			System.out.println("clm tempFile name:   " +tempFileDir.getName() );
			File[] temp = tempFileDir.listFiles((file, s) -> {
//				System.out.println("clm file name:   " +s );

				for (String str : strs) {
					if (str.equals(s)) {
						return true;
					}
				}
				return false;
			});
//			System.out.println("clm:   " + temp);
			if (temp != null) {
//				System.out.println("clm:   " + temp.length);
				for (File originFile : temp) {
					System.out.println("clm originFile:   " + originFile.getName());
					String originFilePath = originFile.getAbsoluteFile().getAbsolutePath();
//					System.out.println("clm originFilePath:   " + originFilePath);
//
					String targetFIlePath = originFilePath.replace(originResFile.getAbsolutePath(), targetResFile.getAbsolutePath());
//					System.out.println("clm targetFIlePath:   " + targetFIlePath);
//					Utils.copyFiles(new File(originFilePath),new File(targetFIlePath));
//					originFile.delete();
				}
//			for (File tempFile : tempFileDir.listFiles()) {
//
//				}

			}
		}
	}
}
