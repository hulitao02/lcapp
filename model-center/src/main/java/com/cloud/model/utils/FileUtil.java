package com.cloud.model.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

	/**
	 * 文件的md5
	 *
	 * @param inputStream
	 * @return
	 */
	public static String fileMd5(InputStream inputStream) {
		try {
			return DigestUtils.md5Hex(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将文件保存到本地
	 *
	 * @param file
	 * @param path
	 * @return
	 */
	public static String saveFile(MultipartFile file, String path) {
		try {
			File targetFile = new File(path);
			if (targetFile.exists()) {
				return path;
			}

			if (!targetFile.getParentFile().exists()) {
				targetFile.getParentFile().mkdirs();
			}
			file.transferTo(targetFile);
			return path;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 删除本地文件
	 *
	 * @param pathname
	 * @return
	 */
	public static boolean deleteFiles(String pathname) {
		File file = new File(pathname);
		if (file.exists()) {
			boolean flag = file.delete();
			if (flag) {
				File[] files = file.getParentFile().listFiles();
				if (files == null || files.length == 0) {
					file.getParentFile().delete();
				}
			}

			return flag;
		}

		return false;
	}


	/**
	 * 根据传入路径删除当前文件
	 * @param pathname
	 * @return
	 */
	public static boolean deleteFile(String pathname){
		File file = new File(pathname);
		boolean flag = false;
		if (file.exists()){
			flag = file.delete();
		}
		return flag;
	}





}
