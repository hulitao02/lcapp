package com.cloud.knowledge.controller;

import net.sf.json.JSONString;
import org.codehaus.jackson.map.ObjectMapper;
import util.ZipUtils;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class test {

    public static void main(String[] args) throws Exception {

        // 将结果写入到指定的json文件中
//        String filePath = "C:/Users/Administrator/Desktop/单机版数据/tips.json";
//        File jsonFile = new File(filePath);
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map map = new HashMap();
//        map.put("djId", "单机版数据唯一标识");
//        map.put("userId", 1);
//        map.put("pdType", 1);
//        map.put("pdYs", "");
//        map.put("filePath", "dm1.png,test.png");
//        map.put("pdContent", "专家判读建议");
//        map.put("delFlg", 0);
//        //map.put("score",20);
//        List<Map> rList=new ArrayList<>();
//        rList.add(map);
//        objectMapper.writeValue(jsonFile, rList);
        String zipFilePath = "D:/test/专家判断建议数据" + ".zip";
        FileOutputStream fos1 = new FileOutputStream(new File(zipFilePath));
        ZipUtils.toZip("D:/test", fos1, true);
        System.out.println("-------执行完毕");


        try {
//            String forderPath = "D:\\test\\one";
//            Path path = Paths.get(forderPath+"\\pic");
//            if (!Files.exists(path)) {
//                Files.createDirectories(path);
//            }
//            String json = "ddd";
//            String filePath = "D:\\test\\one\\a.json";
//            Files.write(Paths.get(filePath), json.getBytes());
//
//            // 将图片复制到指定位置
//            File file = new File("D:\\test\\taotao\\图片\\dm1.png");
//            FileInputStream inputStream = new FileInputStream(file);
//            File file1 = new File("D:\\test\\one\\pic\\dm1.png");
//            FileOutputStream outputStream = new FileOutputStream(file1);
//            byte[] b = new byte[1024];
//            while (inputStream.read(b) != -1) {
//                outputStream.write(b);
//            }
//            outputStream.close();
//            inputStream.close();

//            zipFolder("D:\\test\\one\\", "D:\\test\\one1.zip");
            // 生成zip文件
            // 输出位置
//            String zipFilePath = "D:\\test\\one.zip";
//            FileOutputStream fos = new FileOutputStream(zipFilePath);
//            ZipOutputStream zos = new ZipOutputStream(fos);
//            File filezip = new File(forderPath);
//            zipFile(filezip, filezip.getName(), zos);

            // 删除非zip文件
            // 删除文件夹及其所有子文件夹和文件
//            Files.walk(path)
//                    .sorted(java.util.Comparator.reverseOrder()) // 先删除子文件夹，后删除父文件夹
//                    .forEach(path1 -> {
//                        try {
//                            Files.delete(path1);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println("Deleted: " + path);
//                    });


            /** 测试压缩方法1  */
//            FileOutputStream fos1 = new FileOutputStream(new File("d:/test/one1.zip"));
//            ZipUtils.toZip("d:/test/one", fos1, true);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void zipFolder(String sourceFolder, String outputZipFile) throws IOException {
        Path outputPath = Paths.get(outputZipFile);
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputPath))) {
            Files.walk(Paths.get(sourceFolder))
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        File fileToZip = path.toFile();
                        try {
                            zipFile(fileToZip, fileToZip.getName(), zos);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    private static void zipFile(File fileToZip, String fileNameInZip, ZipOutputStream zos) throws IOException {
//        zos.putNextEntry(new ZipEntry(fileNameInZip));
//        try (FileInputStream fis = new FileInputStream(fileToZip)) {
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = fis.read(buffer)) > 0) {
//                zos.write(buffer, 0, length);
//            }
//        } finally {
//            zos.closeEntry();
//        }
//    }


    public static void zipFile(File file, String fileName, ZipOutputStream zos) {
        if (file.isDirectory()) {
            /*
            假如是个空文件夹，下面这个：
             zos.putNextEntry(new ZipEntry(fileName + "/"));
            就是为了保留空文件夹
             */
            try {
                zos.putNextEntry(new ZipEntry(fileName + "/"));
                zos.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            }

            File[] children = file.listFiles();
            //确保目录不是空的
            if (children != null) {
                for (File child : children) {
                    //加 fileName + "/" 是为了保留原始的目录结构
                    zipFile(child, fileName + "/" + child.getName(), zos);
                }
            }
            return;
        }

        try (
                FileInputStream fileInputStream = new FileInputStream(file)
        ) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
