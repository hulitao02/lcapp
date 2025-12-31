package com.cloud.exam.utils;

import com.cloud.utils.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

/**
 * @Auther: 张争洋
 * @Date: 2019/3/20 21:12
 * 文件操作类
 */

@Slf4j
public class FileUtils extends org.apache.commons.io.FileUtils {


    private static String parasmArg_multipartFile = "multipartFile";

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    @SneakyThrows
    public  static  void downLoadFile(String filePath, HttpServletResponse response,boolean isOnLine) {
        File f = new File(filePath);
        if(!f.exists()){
            response.sendError(404,"File not found!");
            return;
        }
        BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
        byte[] buf = new byte[1024];
        int len = 0;
        response.reset();
        if(isOnLine){
            URL u = new URL("file:///" +filePath);
            response.setContentType(u.openConnection().getContentType());
            response.setHeader("Content-Disposition","inline; filename="+f.getName());
        }else{
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition","attachment; filename=" +f.getName());
        }
        OutputStream out = response.getOutputStream();
        while((len = br.read(buf)) > 0){
            out.write(buf,0,len);
        }
        br.close();
        out.close();
    }



    @SneakyThrows
    public static String saveFileByType(File file,String localFileDir,String newFileName) {
        String localAbsoluteFilePath = localFileDir + (localFileDir.endsWith("/") ? "" : "/") + newFileName;
        try ( FileInputStream fileInputStream = new FileInputStream(file);) {
            FileUtils.copyInputStreamToFile(fileInputStream, new File(localAbsoluteFilePath));
        }
        return localAbsoluteFilePath;
    }


    @SneakyThrows
    public static String saveFileByType(MultipartFile file,String localFileDir, String fileName) {
//        String fileName = file.getOriginalFilename();
        String localAbsoluteFilePath = localFileDir + (localFileDir.endsWith("/") ? "" : "/") + fileName;
        try (InputStream is = file.getInputStream()) {
            FileUtils.copyInputStreamToFile(is, new File(localAbsoluteFilePath));
        }
        return localAbsoluteFilePath;
    }

    @SneakyThrows
    public static boolean ifExists(String filePath){
        File file = new File(filePath);
        boolean msgFlag = false;
        if (!file.exists()){
            return msgFlag;
        }else {
            return !msgFlag;
        }
    }

    /**
     * @param fieldName The name of the form field
     * @param file
     * @return
     */
    public static MultipartFile fileDoMultipartFile(String fieldName,File file){
        FileItem fileItem= null;
        try {

            if(StringUtils.isBlank(fieldName)){
                fieldName = parasmArg_multipartFile;
            }
            fileItem = new DiskFileItem(fieldName, Files.probeContentType(file.toPath()),false,file.getName(),(int)file.length(),file.getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (InputStream inputStream=new FileInputStream(file);
            OutputStream os=fileItem.getOutputStream();){
            IOUtils.copy(inputStream,os);
            return new CommonsMultipartFile(fileItem);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void moveFile(String sourcePath,String targetpath){
        //sourcePath /123/1.txt
        //targetpath /456
        boolean flag = false;
        File sourceFile = new File(sourcePath);
        File targetPath = new File(targetpath);
        if(!targetPath.exists()){
            targetPath.mkdirs();
        }
        File targetFile = new File(targetPath+File.separator+sourceFile.getName());
        try{
            if(sourceFile.renameTo(targetFile)){
                logger.error("移动文件成功。。。");
            }
        }catch (Exception e){
            logger.error("移动文件失败。。。"+e.getMessage());
        }
    }

    public static String file2str(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            System.out.println("找不到文件。。。");
        }
        String str = "";
        String s = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            while ((str=bufferedReader.readLine())!=null){
                s+= str;
            }

        }catch (Exception e){
            System.out.println("找不到文件。。。");
        }
        return s ;
    }
}
