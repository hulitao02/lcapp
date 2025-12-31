package com.cloud.file.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtilsZip {




    @RequestMapping("/api/downlownd")
    public String getOssFile(HttpServletRequest request, HttpServletResponse response){
        // endpoint以杭州为例，其它region请按实际情况填写，1改为自己的
        String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建
        String accessKeyId = "2改为自己的";
        String accessKeySecret = "3改为自己的";
        String bucketName = "4改为自己的";
         //要下载的文件名（Object Name）字符串，中间用‘,’间隔。文件名从bucket目录开始.5改为自己的
         //todo 这个key 是上传时候的key，如下有上传案例的讲解
        String key = "focus/item/P8c5z8BKWY1533108107411.png,focus/item/fJ4WmmbM5w1533108018272.png";
        try {
            // 初始化
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);;
            //6改为自己的名称
            String fileName = "test.zip";
            // 创建临时文件
            File zipFile = File.createTempFile("test", ".zip");
            FileOutputStream f = new FileOutputStream(zipFile);
        /**
        * 作用是为任何OutputStream产生校验和
        * 第一个参数是制定产生校验和的输出流，第二个参数是指定Checksum的类型 （Adler32（较快）和CRC32两种）
        */
            CheckedOutputStream csum = new CheckedOutputStream(f, new Adler32());
            // 用于将数据压缩成Zip文件格式
            ZipOutputStream zos = new ZipOutputStream(csum);

            String[] keylist = key.split(",");
            for (String ossfile : keylist) {
                // 获取Object，返回结果为OSSObject对象
                OSSObject ossObject = ossClient.getObject(bucketName, ossfile);
                // 读去Object内容 返回
                InputStream inputStream = ossObject.getObjectContent();
                // 对于每一个要被存放到压缩包的文件，都必须调用ZipOutputStream对象的putNextEntry()方法，确保压缩包里面文件不同名

                zos.putNextEntry(new ZipEntry(ossfile.split("/")[2]));
                int bytesRead = 0;
                // 向压缩文件中输出数据
                while((bytesRead=inputStream.read())!=-1){
                    zos.write(bytesRead);
                }
                inputStream.close();
                zos.closeEntry(); // 当前文件写完，定位为写入下一条项目
            }
            zos.close();
            String header = request.getHeader("User-Agent").toUpperCase();
            if (header.contains("MSIE") || header.contains("TRIDENT") || header.contains("EDGE")) {
                fileName = URLEncoder.encode(fileName, "utf-8");
                fileName = fileName.replace("+", "%20"); //IE下载文件名空格变+号问题
            } else {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            }
            response.reset();
            response.setContentType("text/plain");
            response.setContentType("application/octet-stream; charset=utf-8");
            response.setHeader("Location", fileName);
            response.setHeader("Cache-Control", "max-age=0");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            FileInputStream fis = new FileInputStream(zipFile);
            BufferedInputStream buff = new BufferedInputStream(fis);
            BufferedOutputStream out=new BufferedOutputStream(response.getOutputStream());
            byte[] car=new byte[1024];
            int l=0;
            while (l < zipFile.length()) {
                int j = buff.read(car, 0, 1024);
                l += j;
                out.write(car, 0, j);
            }
            // 关闭流
            fis.close();
            buff.close();
            out.close();

            ossClient.shutdown();
            // 删除临时文件
            zipFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

