package com.cloud.file.controller;

import com.cloud.file.model.FileInfo;
import com.cloud.file.service.impl.LocalFileServiceImpl;
import com.cloud.file.utils.ApiResult;
import com.cloud.file.utils.FileUtil;
import com.cloud.file.utils.UpLoadConstant;
import com.cloud.utils.StringUtils;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @author md
 * @date 2021/3/31 18:21
 */
@RestController
@RequestMapping("/split")
@RefreshScope
public class SplitFileController {
    @Autowired
    private AppendFileStorageClient appendFileStorageClient;
    @Autowired
    public StringRedisTemplate stringRedisTemplate;
    @Autowired
    private LocalFileServiceImpl localFileService;
    private long persize = 100l;


    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;

    /**
     * 检查文件是否上传完
     *
     * @param param
     * @return
     */
    @GetMapping("/checkFileMd5")
    public Map<String, Object> checkFileMd5(@RequestParam Map<String, String> param) {
        String md5 = param.get("md5");
        String url = "";
        long chunk = Long.parseLong(param.get("chunk"));
        List<Integer> reList = new ArrayList<>();
        Map<String, Object> reMap = new HashMap<>();
        String redisChunkCurr = stringRedisTemplate.opsForValue().get(UpLoadConstant.chunkCurr + md5);
        //情况一:是否全部上传完：数据已经存在，则直接返回url
        FileInfo fileInfo = localFileService.getById(md5);
        if (Objects.nonNull(fileInfo)) {
            if (fileInfo.getUrl().contains("null")) {
                reMap.put("url", "");
                reMap.put("chunked", reList);
                return reMap;
            }
            url = fileInfo.getUrl();
            //全部已经上传完，跳过所有断点，并返回路径
            for (int i = 0; i < chunk; i++) {
                reList.add(i);
            }
            reMap.put("url", fileServer + url);
            reMap.put("chunked", reList);
            int useTimes = fileInfo.getUseTimes();
            fileInfo.setUseTimes(useTimes + 1);
            localFileService.updateFile(fileInfo);
            return reMap;
        }
        //情况二:文件已经上传部分，redis中存在断点，需要向前端返回断点进行续传
        //情况三：文件不存在断点，直接走重新上传。
        if (StringUtils.isNotBlank(redisChunkCurr)) {
            Long chunkCurr = Long.parseLong(redisChunkCurr);
            //存在断点，返回已经上传过的分片列表
            if (chunkCurr > 0) {
                for (int i = 0; i < chunkCurr; i++) {
                    reList.add(i);
                }
            }
        }

        reMap.put("url", url);
        reMap.put("chunked", reList);
        return reMap;
    }


    /**
     * 秒传以及断点续传，断点需要存储在redis中
     *
     * @param request
     * @param param
     * @return 返回路径
     * @throws IOException
     */
    @PostMapping("/chunkUpload")
    public boolean chunkUpload(HttpServletRequest request, @RequestParam Map<String, String> param) {
        StorePath path = null;
        String noGroupPath = "";
        long alreadyUpload = 0l;
        //获取分片文件流
        MultipartFile uploadFile = ((MultipartHttpServletRequest) request).getFile("upload");
        //文件MD5
        String fileMd5 = param.get("fileMd5");
        if (null != stringRedisTemplate.opsForValue().get(UpLoadConstant.historyUpload + fileMd5)) {
            alreadyUpload = Long.parseLong(stringRedisTemplate.opsForValue().get(UpLoadConstant.historyUpload + fileMd5));
        }
        //当前分片的大小
        long currChunkSize = Long.parseLong(param.get("chunkSize"));
        //当前传的是第几块
        long chunkCurr = Long.parseLong(param.get("chunk"));
        String type = param.get("type");
        noGroupPath = stringRedisTemplate.opsForValue().get(UpLoadConstant.chunkCurrPath + fileMd5);
        String fileExt = uploadFile.getOriginalFilename().substring(uploadFile.getOriginalFilename().lastIndexOf(".") + 1);
        try {
            if (uploadFile != null)
                //如果上传的是第一块则创建一个新的文件用此方法
                if (chunkCurr == 0) {
//                    System.out.println("当前是第一次上传"+currChunkSize);
                    path = appendFileStorageClient.uploadAppenderFile(UpLoadConstant.DEFAULT_GROUP, uploadFile.getInputStream(), currChunkSize, fileExt);
                    if (path != null) {
                        noGroupPath = path.getPath();
                        stringRedisTemplate.opsForValue().set(UpLoadConstant.chunkCurrPath + fileMd5, noGroupPath);
                        Thread.sleep(1000);
                    }
                } else {
//                    noGroupPath = param.get("noGroupPath");
//                    System.out.println("不是第一次上传的拼接"+alreadyUpload+"......"+"分片"+currChunkSize);
//                    appendFileStorageClient.modifyFile(UpLoadConstant.DEFAULT_GROUP, noGroupPath, uploadFile.getInputStream(), currChunkSize, alreadyUpload);
                    appendFileStorageClient.appendFile(UpLoadConstant.DEFAULT_GROUP, noGroupPath, uploadFile.getInputStream(), currChunkSize);
                }
//            }
            //在redis中存储已经文件md5以及已经上传到第几块
            stringRedisTemplate.opsForValue().set(UpLoadConstant.chunkCurr + fileMd5, String.valueOf(chunkCurr));
//            System.out.println("fastdfs路径"+noGroupPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("文件上传失败！");
        }
        try {
            stringRedisTemplate.opsForValue().set(UpLoadConstant.historyUpload + fileMd5, String.valueOf(currChunkSize + alreadyUpload));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
//        System.out.println("当前已经上传的大小"+currChunkSize+alreadyUpload);
        return true;
    }


    /**
     * 前端所有分片上传成功后进行调用
     * 保存文件到数据库，并删除在redis中存储的上传记录
     *
     * @param map
     * @return
     */
    @GetMapping("/saveFile")
    public String saveFile(@RequestParam Map<String, Object> map) {
        String noGroupPath;
        String md5 = map.get("md5").toString();
        String fileName = map.get("fileName").toString();
//            String fileName = map.get("fileName").toString();
        //从redis中查询文件名为md5的路径信息
        //查看redis中是否存在当前分片
        noGroupPath = stringRedisTemplate.opsForValue().get(UpLoadConstant.chunkCurrPath + md5);
        String url = UpLoadConstant.noGroupPath + "/" + noGroupPath;
        try {
            //上传成功后将文件保存在数据库中,并清空redis
//            FileInfo file = new FileInfo();
//            file.setId(md5);
//            file.setName(fileName);
//            file.setUrl(url);
//            file.setCreateTime(new Date());
//            file.setUseTimes(1);
//            // 删除数据
//            localFileService.deleteById(md5);
//            localFileService.save(file);

            //保持缩略图

            stringRedisTemplate.delete(UpLoadConstant.chunkCurr + md5);
            stringRedisTemplate.delete(UpLoadConstant.chunkCurrPath + md5);
            stringRedisTemplate.delete(UpLoadConstant.historyUpload + md5);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        return fileServer + url;
    }


    //暂时不对文件进行去重
    @PostMapping("/checkFile")
    public ApiResult checkFile(@RequestParam Map<String, Object> paramMap, HttpServletRequest request) throws IOException {
        return ApiResult.success(0);
    }


    //todo：文件下载
    @GetMapping("/download")
    public HttpServletResponse download(String path, HttpServletResponse response) {
        try {
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();
            // 取得文件的后缀名。
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();
            // 以流的形式下载文件。
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return response;
    }


    @DeleteMapping("/deleteFile/{id}")
    public ApiResult deleteFile(@PathVariable String id) {
        FileInfo fileInfo = localFileService.getById(id);
        if (null != fileInfo && fileInfo.getUseTimes() > 1) {
            //此处做一个假删除,跳过删除操作
            return new ApiResult("200", "刪除成功", 1);
        }
        int ifDelete = localFileService.deleteById(id);

        if (fileInfo.getUrl().contains(UpLoadConstant.DEFAULT_GROUP)) {
            //删除fastdfs中文件
            appendFileStorageClient.deleteFile(UpLoadConstant.DEFAULT_GROUP, fileInfo.getUrl());
        } else {
            //删除weboffice路径下的文件
            FileUtil.deleteFile(fileInfo.getUrl());
        }

        return new ApiResult("200", "刪除成功", ifDelete);
    }


    /**
     * 断点下载
     *
     * @param fileMap
     * @return
     */
    @RequestMapping(value = "/downloadFiles", method = RequestMethod.GET)
    public byte[] downloadFiles(@RequestParam Map<String, Object> fileMap) {
        String fdfsFile = fileMap.get("fileUrl").toString();
        //当前需要下载第几片，从第0片开始
//        long chunk = Long.parseLong(fileMap.get("chunk").toString());
        long alreadyDown = Long.parseLong(fileMap.get("alreadyDown").toString());
        com.github.tobato.fastdfs.domain.FileInfo fileInfo = appendFileStorageClient.queryFileInfo(UpLoadConstant.DEFAULT_GROUP, fdfsFile);
        long fileSize = fileInfo.getFileSize();
        //判断是否是最后一片
        long needUpload = fileSize - alreadyDown;
        if (needUpload < persize) {
            persize = needUpload;
        }
        byte[] bytes = appendFileStorageClient.downloadFile(UpLoadConstant.DEFAULT_GROUP, fdfsFile, alreadyDown, persize, new DownloadByteArray());
        return bytes;
    }

    /**
     * 断点下载,直接生成文件
     *
     * @param fileMap
     * @return /group1/M00/00/00/wKgK0WJOj-qAN3DzAALsVviQxqw47.jpeg
     */
    @RequestMapping(value = "/downloadWriteFiles", method = RequestMethod.POST)
    public void downloadWriteFiles(@RequestBody Map<String, Object> fileMap) {
        String local_prefix = "D://";
        String fastDfsPathPrefix = "group1/M00/";
        String fdfsFile = fileMap.get("fdfsFile_path").toString();
        String fdfsPathParams = "";
        if (StringUtils.isBlank(fdfsFile)) {
            return;
        }
        fdfsPathParams = fdfsFile.substring(fastDfsPathPrefix.length());
        //当前需要下载第几片，从第0片开始
        long alreadyDown = Long.parseLong(fileMap.get("alreadyDown").toString());
        com.github.tobato.fastdfs.domain.FileInfo fileInfo = appendFileStorageClient.queryFileInfo(UpLoadConstant.DEFAULT_GROUP, fdfsPathParams);
        long fileSize = fileInfo.getFileSize();
//        //判断是否是最后一片
//        long needUpload = fileSize - alreadyDown;
//        if (needUpload < persize) {
//            persize = needUpload;
//        }
//      得到一个文件全部的字节流 。
        byte[] content = appendFileStorageClient.downloadFile(UpLoadConstant.DEFAULT_GROUP, fdfsPathParams, alreadyDown, fileSize, new DownloadByteArray());
        /**
         * 生成文件
         */
        BufferedInputStream buffin = null;
        FileOutputStream fileOutputStream = null;// 获取文件输出IO流
        BufferedOutputStream bufferOut = null; // 输出流
        // 本地文件位置
        byte[] buffer = new byte[1024];
        String path = local_prefix + fdfsFile;
        File targetFile = new File(path);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        InputStream inputStream = new ByteArrayInputStream(content);
        try {
            fileOutputStream = new FileOutputStream(targetFile, true);// 获取文件输出IO流
            bufferOut = new BufferedOutputStream(fileOutputStream);
            buffin = new BufferedInputStream(inputStream);
            int i;
            while ((i = buffin.read(buffer)) != -1) {
                bufferOut.write(buffer, 0, i);
            }
            bufferOut.flush();
            bufferOut.close();
            buffin.close();
            System.out.println("文件下载成功： " + targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (Objects.nonNull(bufferOut)) {
                    bufferOut.flush();
                }
                if (Objects.nonNull(bufferOut)) {
                    bufferOut.close();
                }
                if (Objects.nonNull(buffin)) {
                    buffin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
