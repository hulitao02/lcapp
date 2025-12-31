package com.cloud.file.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

;

/**
 * @ProjectName: personal
 * @Package: com.personal.file.utils
 * @ClassName: ImageUtil
 * @Author: xuweijie
 * @Description:
 * @Date: 2021/7/12 13:42
 * @Version: 1.0
 */
@Slf4j
public class ImageUtil {
    /**
     * <p>Title: thumbnailImage</p>
     * <p>Description: 根据图片路径生成缩略图 </p>
     */
    public static void thumbnailImage(InputStream inputStream, String fileName, String localFilePath) {
        try {
            //需要处理的文件名称
            String name = fileName.substring(0, fileName.lastIndexOf("."));

            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

            if (fileName.indexOf(".") > -1 &&
                    suffix.equals("tiff") || suffix.equals("tif") || suffix.equals("img")) {
                suffix = "jpg";
            }
            Image image = ImageIO.read(inputStream);
            String prevfix = "_small";
            int w = 190;
            int h = 130;
            //生成小的缩略图
            sendImage(image, w, h, localFilePath, name, prevfix, suffix);
            prevfix = "_big";
            w = 600;
            h = 400;
            //生成大的缩略图
            sendImage(image, w, h, localFilePath, name, prevfix, suffix);
        } catch (IOException e) {
            log.error("generate thumbnail image failed.", e);
        } catch (Exception e) {
            log.error("generate thumbnail image failed.", e);
        }
    }

    public static void sendImage(Image image, int w, int h, String localFilePath, String name, String prevfix, String suffix) throws IOException {
        log.info("缩略图保存地址："+localFilePath);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();
        ImageIO.write(bi, suffix, new File(localFilePath + "/" + name + prevfix + "." + suffix));
    }
}








