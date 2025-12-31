package com.cloud.file.service.impl;

import com.cloud.file.dao.FileDao;
import com.cloud.file.model.FileInfo;
import com.cloud.file.model.FileSource;
import com.cloud.file.utils.FileUtil;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FdfsFileServiceImpl extends AbstractFileService {

    @Autowired
    private FastFileStorageClient storageClient;

    public FileInfo upload(MultipartFile file) throws Exception {
        return null;
    }

    @Override
    protected FileDao getFileDao() {
        return null;
    }

    @Override
    protected FileSource fileSource() {
        return null;
    }

    @Override
    protected void uploadFile(MultipartFile file, FileInfo fileInfo) throws Exception {

    }

    @Override
    protected void uploadFileByName(MultipartFile file, FileInfo fileInfo) throws Exception {

    }

    @Override
    protected boolean deleteFile(FileInfo fileInfo) {
        return false;
    }

    @Override
    public String uploadFdfsFile(MultipartFile file) throws Exception {
        FileInfo fileInfo = FileUtil.getFileInfo(file);
        if (!fileInfo.getName().contains(".")) {
            throw new IllegalArgumentException("缺少后缀名");
        }
        StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
        return storePath.getFullPath();
    }

    @Override
    public String uploadFdfsFile_normal(File fileInfo) throws Exception {
        if (!fileInfo.getName().contains(".")) {
            throw new IllegalArgumentException("缺少后缀名");
        }
        StorePath storePath = storageClient.uploadFile(new FileInputStream(fileInfo), fileInfo.length(), FilenameUtils.getExtension(fileInfo.getName()), null);
        return storePath.getFullPath();
    }


    /**
     * @author:胡立涛
     * @description: TODO 多文件上传
     * @date: 2022/11/17
     * @param: [files]
     * @return: void
     */
    @Override
    @Transactional
    public List<String> uploadMore(MultipartFile[] files) throws Exception {
        List<String> pathList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file != null) {
                FileInfo fileInfo = FileUtil.getFileInfo(file);
                if (!fileInfo.getName().contains(".")) {
                    throw new IllegalArgumentException("缺少后缀名");
                }
                StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                pathList.add(storePath.getFullPath());
            }
        }
        return pathList;
    }
}
