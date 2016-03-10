package com.yuanhonglong.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 文件读取类
 * 
 * @author 天命剑主 <br>
 *         on 2015/9/18.
 */
public class CMMFileUtils {

	/**
	 * 读取文件
	 *
	 * @param file
	 *            文件
	 * @return 字符串
	 */
	public static String readToEnd(File file) {
		try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append("\n");
			}
			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 读取jar里的文件
	 */
	public static String readToEnd(String str) {
		StringBuilder builder = new StringBuilder();
		try (InputStream stream = CMMFileUtils.class.getClass().getResourceAsStream(str);
				BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line).append("\n");
			}
			return builder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 写入文件
	 *
	 * @param file
	 *            文件
	 * @param str
	 *            字符串
	 */
	public static void writeToFile(File file, String str) {
		str = str.replace("\r", "");
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(str.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
