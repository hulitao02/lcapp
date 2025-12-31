package com.cloud.file.service;

import com.cloud.file.model.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface FileService {

	/**
	 * 上传文件
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	FileInfo upload(MultipartFile file,Boolean flag) throws Exception;

	/**
	 * 删除文件
	 *
	 * @param fileInfo
	 */
	void delete(FileInfo fileInfo);

	FileInfo getById(String id);

	int save(FileInfo fileInfo);

	/**
	 * dfds的文件上传
	 * @param file
	 * @return
	 * @throws Exception
	 */
	String uploadFdfsFile(MultipartFile file) throws Exception;


	public String uploadFdfsFile_normal(File fileInfo) throws Exception;

	/**
	 *
	 * @author:胡立涛
	 * @description: TODO 影像切片，发布处理逻辑
	 * @date: 2022/4/21
	 * @param: []
	 * @return: int
	 */
	int tifUpload(String path,String fileName) throws Exception;

	/**
	 *
	 * @author:胡立涛
	 * @description: TODO 多文件上传
	 * @date: 2022/11/17
	 * @param: [files]
	 * @return: void
	 */
	List<String> uploadMore(MultipartFile[] files) throws Exception;
}
