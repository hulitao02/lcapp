package com.cloud.exam.utils;


import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.exception.FdfsUnsupportStorePathException;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;

/**
 * Created by dyl on 2021/04/12.
 */
@Component
public class FileFastdfsUtils {


    private Logger logger = LoggerFactory.getLogger(FileFastdfsUtils.class);

    @Autowired
    private FastFileStorageClient fastClient;
    private static FastFileStorageClient fastFileStorageClient;
    @Autowired
    private AppendFileStorageClient fileStorageClient;
    private static AppendFileStorageClient appendFileStorageClient;
    @PostConstruct
    public void init(){
        fastFileStorageClient = this.fastClient;
        appendFileStorageClient = this.fileStorageClient;
    }



    //提交情报分析考试报告word文档
    @RequestMapping("/uploadAnalyseFile")
    public String uploadAnalyseFile(@RequestBody MultipartFile file) {
        String path = "";
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if ("doc".equalsIgnoreCase(suffix) || "docx".equalsIgnoreCase(suffix) || "rar".equalsIgnoreCase(suffix) || "zip".equalsIgnoreCase(suffix)) {
            try {
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                return path = storePath.getFullPath();
            } catch (IOException e) {
                logger.error("上传文件错误。。。", e.getMessage());
                e.printStackTrace();
            }
        } else {
            return path;
        }
        return path;
    }

    //提交情报分析考试报告word文档
    public static String uploadFile(MultipartFile file) {
        try {
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
            return storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String upload(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            StorePath storePath = appendFileStorageClient.uploadAppenderFile("group1",inputStream,file.getSize(),file.getOriginalFilename());
            return storePath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问地址
     * @return
     */
    public ApiResult deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return ApiResultHandler.buildApiResult(200, "文件为空", null);
        }
        try {
            StorePath storePath = StorePath.praseFromUrl(fileUrl);
            fastFileStorageClient.deleteFile(storePath.getGroup(), storePath.getPath());
            return ApiResultHandler.buildApiResult(200, "文件删除成功", null);
        } catch (FdfsUnsupportStorePathException e) {
            e.printStackTrace();
        }
        return ApiResultHandler.buildApiResult(500, "文件删除失败", null);
    }

    /**
     * 下载文件
     *
     * @param fileUrl 文件URL
     * @return 文件字节
     * @throws IOException
     */
    public static byte[] downloadFile(String fileUrl) throws IOException {
        String group = fileUrl.substring(0, fileUrl.indexOf("/"));
        String path = fileUrl.substring(fileUrl.indexOf("/") + 1);
        DownloadByteArray downloadByteArray = new DownloadByteArray();
        byte[] bytes = fastFileStorageClient.downloadFile(group, path, downloadByteArray);
        return bytes;
    }

    public static String fileDoMu(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(file.getName(), file.getName(), Files.probeContentType(file.toPath()), inputStream);
        String s = uploadFile(mockMultipartFile);
        return s;
    }
}
