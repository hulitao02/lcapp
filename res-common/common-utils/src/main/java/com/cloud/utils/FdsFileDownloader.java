package com.cloud.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 从指定文本找到所有fds文件并按照原目录结构下载到本地
 * </p>
 *
 * @author tongkesong
 * @since 2024-04-22
 */
public class FdsFileDownloader {
    public static final String domain = "http://192.168.10.232:8888/";
    public static final String txtPath = "D:\\项目\\航天局\\2024年7月4日部署\\试题.txt";
    public static final String charsetName = "GB2312";
    public static final String baseDir = txtPath + ".se/" +
            domain.substring(domain.lastIndexOf("://") + 1, domain.lastIndexOf(":"));

    public static void readAndDownload() {
        List<String> errList = new ArrayList<>();
        try (Scanner sc = new Scanner(new FileInputStream(txtPath), charsetName)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains("group1/M00/")) {
                    Pattern pattern = Pattern.compile("group1/M00/([0-9A-F]{2})/([0-9A-F]{2})/([\\w-]{1,36}\\.\\w+)");
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        String url = matcher.group();
                        System.out.println(url);
                        if (!download(url)) {
                            errList.add(url);
                        }
                    }

                }
            }
            FileUtils.writeLines(new File(baseDir + "-err.txt"), errList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean download(String url) {
        int lastSepIndex = url.lastIndexOf("/");
        String fileName = url.substring(lastSepIndex + 1);
        String dir = baseDir + "/data/" + url.substring(11, lastSepIndex);
        try {
            FileUtils.copyURLToFile(new URL(domain + url), new File(dir, fileName));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        readAndDownload();
    }

}
