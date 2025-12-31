package com.cloud.model.controller;

import com.alibaba.fastjson.JSON;
import com.cloud.model.bean.dto.PdTipsDto;
import com.cloud.model.user.AppUser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class test {
    public static void main(String[] args) throws Exception {
        try {
            // 步骤一： 解压zip文件
            String zipFilePath = "D:/test/pd.zip";
            String destDirectory = "D:/test";
            unzip(zipFilePath, destDirectory);
            // 步骤二： 解析json数据
//            String filePath = "D:/test/one/a.json";
//            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
//            List<AppUser> appUsers = JSON.parseArray(content, AppUser.class);
//            System.out.println("----------------读取到的json内容为：" + appUsers);
            // 步骤三： 将图片保存至指定位置
//            String picName = "dm1.png";
//            String oldPath = "D:/test/one/pic/" + picName;
//            String newPath = "E:/dataManage/checkout/movetoFile/" + "dj_" + picName;
//            File file = new File(oldPath);
//            FileInputStream inputStream = new FileInputStream(file);
//            // 新图片地址
//            File file1 = new File(newPath);
//            FileOutputStream outputStream = new FileOutputStream(file1);
//            byte[] b = new byte[1024];
//            while (inputStream.read(b) != -1) {
//                outputStream.write(b);
//            }
//            outputStream.close();
//            inputStream.close();
            System.out.println("-------图片生成成功------------");
            System.out.println("-------------执行完毕-------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String zipFilePath, String destDirectory) throws Exception {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry;
        String currentEntry;

        while ((entry = zipIn.getNextEntry()) != null) {
            currentEntry = destDirectory + File.separator + entry.getName();
            File destFile = new File(currentEntry);

            if (entry.isDirectory()) {
                destFile.mkdirs();
            } else {
                new File(destFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(destFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zipIn.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipIn.closeEntry();
        }
        zipIn.close();
    }
}
