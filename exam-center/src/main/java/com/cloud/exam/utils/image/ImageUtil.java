package com.cloud.exam.utils.image;

import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtil {
    public static String getName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    //获取文件后缀
    public static String getSuffix(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    //获取文件后缀
    public static String getFullName(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        return fileName.substring(fileName.lastIndexOf("."));
    }

    //远程读取图片的转换为Base64字符串
    public static String getImageBase64(String imageUrl) {
        URL url = null;
        InputStream is = null;
        ByteArrayOutputStream outputStream = null;
        HttpURLConnection httpUrl = null;
        try {
            url = new URL(imageUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            is = httpUrl.getInputStream();
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int let = 0;
            while ((let = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, let);
            }
            return new BASE64Encoder().encode(outputStream.toByteArray());
        } catch (MalformedURLException e) {
            return "";
        } catch (IOException e) {
            return "";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    return "";
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    return "";
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
    }

}
