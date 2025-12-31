package com.cloud.exam.utils;

import ch.ethz.ssh2.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * 在线office编辑程序所在linux系统文件传输工具(ssh调用方式)
 */
@Component
@Slf4j
public class WebOfficeUtil implements ApplicationContextAware {

    private static String hostname = null;
    private static String user = null;
    private static String password = null;
    private static String webOfficeDir = null;
    private static String localFileDir = null;

    /**
     * 清除WebOffice所在文件系统文件
     */
//    public static void clearByStayDays(int stayDays) throws Exception {
//        Connection conn = null;
//
//        Calendar c = Calendar.getInstance();
//        c.add(Calendar.DATE, -stayDays);
//
//        try {
//            conn = getConnection();
//            SFTPv3Client sftPv3Client = new SFTPv3Client(conn);
//            sftPv3Client.setCharset("UTF-8");
//            List<SFTPv3DirectoryEntry> files = sftPv3Client.ls(webOfficeDir);
//            if (CollectionUtils.isNotEmpty(files)) {
//                for (SFTPv3DirectoryEntry file : files) {
//                    if (file.attributes.isDirectory() && new Date(file.attributes.atime).before(c.getTime())) {
//                        rm(sftPv3Client, webOfficeDir + file.filename);
//                    }
//                }
//            }
//        } finally {
//            if (null != conn) {
//                conn.close();
//            }
//        }
//    }

    /**
     * 删除WebOffice所在文件系统文件
     */
//    public static void rm(String filePath) throws Exception {
//        Connection conn = null;
//        try {
//            conn = getConnection();
//            SFTPv3Client sftPv3Client = new SFTPv3Client(conn);
//            rm(sftPv3Client, webOfficeDir + filePath);
//        } finally {
//            if (null != conn) {
//                conn.close();
//            }
//        }
//    }

    /**
     * 删除WebOffice所在文件系统文件
     */
//    public static void rm(SFTPv3Client sftPv3Client, String fileAbsolutePath) throws Exception {
//        SFTPv3FileAttributes stat = sftPv3Client.stat(fileAbsolutePath);
//        if (stat.isDirectory()) {
//            List<SFTPv3DirectoryEntry> ls = sftPv3Client.ls(fileAbsolutePath);
//            for (SFTPv3DirectoryEntry file : ls) {
//                if (".".equals(file.filename) || "..".equals(file.filename)) {
//                    continue;
//                }
//                rm(sftPv3Client, fileAbsolutePath + "/" + file.filename);
//            }
//            log.info("rmdir:" + fileAbsolutePath);
//            sftPv3Client.rmdir(fileAbsolutePath);
//        } else {
//            log.info("rm:" + fileAbsolutePath);
//            sftPv3Client.rm(fileAbsolutePath);
//        }
//    }

    /**
     * 浏览WebOffice所在文件系统文件列表
     */
//    public static List<SFTPv3DirectoryEntry> ls() throws Exception {
//        Connection conn = null;
//        try {
//            conn = getConnection();
//            SFTPv3Client sftPv3Client = new SFTPv3Client(conn);
//            sftPv3Client.setCharset("UTF-8");
//            return sftPv3Client.ls(webOfficeDir);
//        } finally {
//            if (null != conn) {
//                conn.close();
//            }
//        }
//    }

//    public static void get(String remoteFilePath, String localFilePath) throws Exception {
//        Connection conn = null;
//        String dpath = localFileDir;
//        File outFileDir = new File(dpath);
//        if (!outFileDir.exists()) {
//            boolean isMakDir = outFileDir.mkdirs();
//            if (isMakDir) {
//                System.out.println("创建下载目录成功");
//            }
//        }
//        try (FileOutputStream fos = new FileOutputStream(localFileDir + "/" + localFilePath)) {
//            conn = getConnection();
//
//            SCPClient client = conn.createSCPClient();
//
//            log.info("get:" + webOfficeDir + remoteFilePath + " -> " + localFileDir + localFilePath);
//            SCPInputStream in = client.get(webOfficeDir + remoteFilePath);
//            IOUtils.copy(in, fos);
//        } finally {
//            if (null != conn) {
//                conn.close();
//            }
//        }
//    }

    /**
     * 上传文件至WebOffice所在文件系统
     *
     * @param filePath 文件路径
     */
//    public static void put(String filePath) throws Exception {
//        int lastIndexOfSplit = filePath.lastIndexOf("/");
//        String filePathDir = filePath.substring(0, lastIndexOfSplit > -1 ? lastIndexOfSplit : filePath.length());
//        File file = new File(localFileDir + "/" + filePath);
//        put(file, filePathDir);
//    }

    /**
     * 上传文件至WebOffice所在文件系统并换一个filePath
     *
     * @param filePath 文件路径
     */
//    @SneakyThrows
//    public static String putNewFilePath(String filePath, String userId) {
//        String filePathDir = filePath.lastIndexOf("/") > -1 ? filePath.substring(0, filePath.lastIndexOf("/")) : "";
//        //String newFilePath = getNewFilePath(filePathDir, userId);
//        String newFilePath = "";
//        put(new File(localFileDir + "/" + filePath), newFilePath);
//        //return getNewFilePath(filePath, userId);
//        return filePath;
//    }

    /**
     * 上传文件至WebOffice所在文件系统并换一个filePath
     *
     * @param filePath 文件路径
     */
//    @SneakyThrows
//    public static String putNewFilePathHasUser(String filePath, String userId) {
//        String filePathDir = filePath.lastIndexOf("/") > -1 ? filePath.substring(0, filePath.lastIndexOf("/")) : "";
//        String newFilePath = getNewFilePath(filePathDir, userId);
//        put(new File(localFileDir + "/" + filePath), newFilePath);
//        return getNewFilePath(filePath, userId);
//    }

    /**
     * 获取文件在WebOffice服务器的filePath
     *
     * @param filePath 文件路径
     */
  /*  public static String getNewFilePath(String filePath, String userId) {
        return userId + "/" + filePath;
    }*/

    /**
     * 获取文件在WebOffice服务器的filePath
     *
     * @param filePath 文件路径
     */
    public static String getNewFilePath(String filePath, String userId, String urlPath) {
        return urlPath + userId + "/" + filePath;
    }

    /**
     * 获取文件在WebOffice服务器的filePath
     *
     * @param filePath 文件路径
     */
    public static String getNewFilePath(String filePath, String identityCard) {
        return filePath + "/" + identityCard;
    }

    public static String newOffice(String fileType) throws Exception {
        String path = "office/" + DateFormatUtils.format(new Date(), "yyyyMMdd");
        String fileName = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "") + "." + fileType;
        String filePath = path + "/" + fileName;
        String fileAbsolutePath = localFileDir + filePath;
        File file = new File(fileAbsolutePath);
        file.getParentFile().mkdirs();
        while (file.exists()) {
            fileName = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "") + "." + fileType;
            filePath = path + "/" + fileName;
            fileAbsolutePath = localFileDir + filePath;
            file = new File(fileAbsolutePath);
        }
        switch (fileType) {
            case "txt":
                file.createNewFile();
                break;
            case "docx":
                createWordFile(file);
                break;
            case "xlsx":
                createExcelFile(file);
                break;
            case "pptx":
                createPptFile(file);
                break;
        }

        return filePath;
    }

    public static File getFile(String filePath) {
        return new File(localFileDir + filePath);
    }

    private static void createPptFile(File file) throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ppt.write(fos);
        }
    }

    private static void createExcelFile(File file) throws Exception {
        Workbook wb = new XSSFWorkbook();
        wb.createSheet();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            wb.write(fos);
        }
    }

    private static void createWordFile(File file) throws Exception {
        XWPFDocument doc = new XWPFDocument();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            doc.write(fos);
        }
    }


    /**
     * 获取linux连接
     *
     * @return
     * @throws IOException
     */
    private static Connection getConnection() throws Exception {


        //1, 创建一个连接connection对象
        Connection conn = new Connection(hostname);
        //2, 进行连接操作
        conn.connect();

        //3, 进行连接访问授权验证
        boolean isAuth = conn.authenticateWithPassword(user, password);
        if (!isAuth) {
            throw new RuntimeException("Authentication failed");
        }
        return conn;
    }

    public static void delete(String filePath) {
        FileUtils.deleteQuietly(new File(localFileDir + filePath));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment env = applicationContext.getBean(Environment.class);
        WebOfficeUtil.localFileDir = env.getProperty("dbs.localFile.dir");
        WebOfficeUtil.webOfficeDir = env.getProperty("weboffice.ssh.dir");
        WebOfficeUtil.hostname = env.getProperty("weboffice.ssh.hostname");
        WebOfficeUtil.user = env.getProperty("weboffice.ssh.user");
        WebOfficeUtil.password = env.getProperty("weboffice.ssh.password");
    }
}
