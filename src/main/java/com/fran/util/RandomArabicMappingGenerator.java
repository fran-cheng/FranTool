package com.fran.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author 程良明
 * @date 2024/4/12
 * * * 说明:生成随机的混淆字典
 **/
public class RandomArabicMappingGenerator {
	public static void main(String[] args) {
		int length = 10;
		int lengthRow = 1000;
		String randomArabicString = generateRandomArabicMapping(length,lengthRow);
		saveToFile(randomArabicString, "random_arabic_mapping.txt");
	}

	public static String generateRandomArabicMapping(int length,int row) {
		String arabicLetters = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي";
		char[] arabicChars = arabicLetters.toCharArray();
		char[] shuffledArabicChars = shuffleArray(arabicChars);
		Map<Character, Character> mapping = new HashMap<>();
		for (int i = 0; i < arabicChars.length; i++) {
			mapping.put(arabicChars[i], shuffledArabicChars[i]);
		}

		StringBuilder result = new StringBuilder();
		Random random = new Random();
		for (int rowLine = 0; rowLine < row; rowLine++ ){
			for (int i = 0; i < length; i++) {
				char letter = arabicChars[random.nextInt(arabicChars.length)];
				result.append(mapping.get(letter));
			}
			result.append("\r\n");
		}


		return result.toString();
	}

	public static void saveToFile(String content, String filePath) {
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write(content);
			writer.close();
			System.out.println("随机阿拉伯语混淆字典已保存到 '" + filePath + "'");
		} catch (IOException e) {
			System.out.println("保存文件时出现错误：" + e.getMessage());
		}
	}

	public static char[] shuffleArray(char[] array) {
		Random rand = new Random();
		for (int i = array.length - 1; i > 0; i--) {
			int index = rand.nextInt(i + 1);
			char temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}
		return array;
	}


}
