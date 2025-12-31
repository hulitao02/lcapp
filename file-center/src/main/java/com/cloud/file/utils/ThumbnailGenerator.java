package com.cloud.file.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class ThumbnailGenerator {

    public static void main(String[] args) {
        String imageUrl = "http://192.168.10.235:8888/group1/M00/00/03/wKgK62X5YxaELGozAAAAAIk7se0984.tif"; // 远程TIFF图像的URL
        String outputFile = "F:\\thumbnail_image.jpg"; // 输出缩略图文件名

        // 缩略图的宽度和高度
        int thumbWidth = 190;
        int thumbHeight = 130;

        try {
            // 从URL读取图像数据并生成缩略图
            generateThumbnail(new URL(imageUrl), outputFile, thumbWidth, thumbHeight);

            System.out.println("Thumbnail created successfully.");

        } catch (IOException e) {
            System.out.println("Error generating thumbnail: " + e.getMessage());
        }
    }


    public static void thumbnailImage(String imageUrl, String fileName, String localFilePath) {
        try {
            //需要处理的文件名称
            String name = fileName.substring(0, fileName.lastIndexOf("."));

            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

            if (fileName.indexOf(".") > -1 &&
                    suffix.equals("tiff") || suffix.equals("tif") || suffix.equals("img")) {
                suffix = "jpg";
            }
            String prevfix = "_big";
            int thumbWidth = 1920;
            int thumbHeight = 1080;
            String outputFile = localFilePath + "/" + name + prevfix + "." + suffix;
            generateThumbnail(new URL(imageUrl), outputFile, thumbWidth, thumbHeight);
        } catch (IOException e) {
            log.error("generate thumbnail image failed.", e);
        } catch (Exception e) {
            log.error("generate thumbnail image failed.", e);
        }
    }

    private static void generateThumbnail(URL imageUrl, String outputFile, int thumbWidth, int thumbHeight) throws IOException {
        // 从URL获取输入流
        try (InputStream inputStream = imageUrl.openStream()) {
            // 使用ImageIO.read()方法读取输入流，这是一个流式处理的过程
            BufferedImage image = ImageIO.read(inputStream);

            // 创建缩略图
            BufferedImage thumbnail = createThumbnail(image, thumbWidth, thumbHeight);

            // 将缩略图保存为JPG文件
            saveAsJpg(thumbnail, outputFile);
        }
    }

    private static BufferedImage createThumbnail(BufferedImage image, int thumbWidth, int thumbHeight) {
        // 计算缩略图的比例
        double scaleX = (double) thumbWidth / image.getWidth();
        double scaleY = (double) thumbHeight / image.getHeight();
        double scale = Math.min(scaleX, scaleY);

        // 计算缩略图的尺寸
        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);

        // 创建缩略图
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();

        // 缩放图像并绘制缩略图
        g2d.drawImage(image.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH), 0, 0, null);

        // 释放资源
        g2d.dispose();

        return thumbnail;
    }

    private static void saveAsJpg(BufferedImage image, String outputFile) throws IOException {
        // 获取输出流并保存为JPG格式
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(outputFile))) {
            ImageIO.write(image, "jpg", outputStream);
        }
    }

}
