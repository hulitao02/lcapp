package com.cloud.file.controller;

import com.alibaba.nacos.common.util.Md5Utils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.file.config.FileServiceFactory;
import com.cloud.file.dao.FileDao;
import com.cloud.file.model.FileInfo;
import com.cloud.file.model.FileSource;
import com.cloud.file.model.TifFastdfsRelation;
import com.cloud.file.service.FileService;
import com.cloud.file.service.TifFastdfsRelationService;
import com.cloud.file.service.impl.LocalFileServiceImpl;
import com.cloud.file.utils.ImageUtil;
import com.cloud.file.utils.ThumbnailGenerator;
import com.cloud.file.utils.UpLoadConstant;
import com.cloud.model.common.Page;
import com.cloud.model.log.LogAnnotation;
import com.cloud.model.log.constants.LogModule;
import com.cloud.utils.PageUtil;
import com.cloud.utils.StringUtils;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@RestController
@RequestMapping("/files")
@Slf4j
public class FileController {

    @Autowired
    private FileServiceFactory fileServiceFactory;
    @Autowired
    private AppendFileStorageClient appendFileStorageClient;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired
    private LocalFileServiceImpl localFileService;
    @Autowired
    private FileDao fileDao;
    @Autowired
    private FastFileStorageClient storageClient;

    // TODO 新增 TIF Service 类
    @Autowired
    private TifFastdfsRelationService tifFastdfsRelationService;

    @Value(value = "${tiffastPath}")
    private String tiffastPath;

    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;

    // 切片服务图片存放路径
    @Value(value = "${cell_server_path}")
    private String cellServerPath;

    @Value("${file.fdfs.server_prefix}")
    private String serverPrefix;

    /**
     * 上传文件存储在本地的根路径
     */
    @Value("${file.local.path}")
    private String localFilePath;

    @Value("${file.local.urlPrefix}")
    private String localUrlPrefix;


    // 文件在线预览访问地址
    @Value("${kkFileViewUrl}")
    private String kkFileViewUrl;
    // 文档原地址
    @Value("${sourcePath}")
    private String sourcepath;
    // 文档在线预览地址
    @Value("${destPath}")
    private String destpath;
    // 文档在线预览地址
    @Value("${autodestPath}")
    private String autodestPath;

    // 影像访问前缀地址
    @Value("${tifServerUrl}")
    private String tifServerUrl;
    // 产品访问前缀地址
    @Value("${proServerUrl}")
    private String proServerUrl;


    /**
     * 文件上传<br>
     * <p>
     * <p>
     * 根据fileSource选择上传方式，目前仅实现了上传到本地<br>
     * 如有需要可上传到第三方，如阿里云、七牛等
     *
     * @param file
     * @param fileSource FileSource
     * @return
     * @throws Exception
     */
    @LogAnnotation(module = LogModule.FILE_UPLOAD, recordParam = false)
    @PostMapping
    public FileInfo upload(@RequestParam("file") MultipartFile file, String fileSource) throws Exception {
        FileService fileService = fileServiceFactory.getFileService(fileSource);
        return fileService.upload(file, true);
    }

    /**
     * layui富文本文件自定义上传
     *
     * @param file
     * @param fileSource
     * @return
     * @throws Exception
     */
    @LogAnnotation(module = LogModule.FILE_UPLOAD, recordParam = false)
    @PostMapping("/layui")
    public Map<String, Object> uploadLayui(@RequestParam("file") MultipartFile file, String fileSource)
            throws Exception {
        FileInfo fileInfo = upload(file, fileSource);

        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        Map<String, Object> data = new HashMap<>();
        data.put("src", fileInfo.getUrl());
        map.put("data", data);

        return map;
    }

    /**
     * 文件删除
     *
     * @param id
     */
    @LogAnnotation(module = LogModule.FILE_DELETE)
    @PreAuthorize("hasAuthority('file:del')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        FileInfo fileInfo = fileDao.getById(id);
        if (fileInfo != null) {
            FileService fileService = fileServiceFactory.getFileService(fileInfo.getSource());
            StorePath storePath = StorePath.praseFromUrl(fileInfo.getUrl());
            //appendFileStorageClient.deleteFile(UpLoadConstant.DEFAULT_GROUP,fileInfo.getUrl());
            fastFileStorageClient.deleteFile(UpLoadConstant.DEFAULT_GROUP, storePath.getPath());
            fileService.delete(fileInfo);
        }
    }

    /**
     * 文件查询
     *
     * @param params
     * @return
     */
    @PreAuthorize("hasAuthority('file:query')")
    @GetMapping
    public Page<FileInfo> findFiles(@RequestParam Map<String, Object> params) {
        int total = fileDao.count(params);
        List<FileInfo> list = Collections.emptyList();
        if (total > 0) {
            PageUtil.pageParamConver(params, true);
            list = fileDao.findData(params);
        }
        return new Page<>(total, list);
    }

    @GetMapping("/saveFile")
    public void saveFile(@RequestParam Map<String, Object> fileParam) {
        FileInfo file = new FileInfo();
        file.setId(fileParam.get("id").toString());
        file.setName(fileParam.get("name").toString());
        file.setUrl(fileParam.get("url").toString());
        file.setCreateTime(new Date());
        file.setUseTimes(1);
        localFileService.save(file);

    }

    /**
     * 缩略图处理
     *
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/upload/thumbnail/image")
    public ApiResult uploadThumbnailImage(@RequestParam("file") MultipartFile file, String fileName) throws Exception {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                ImageUtil.thumbnailImage(file.getInputStream(), fileName, localFilePath);
            }
        }).start();
        return ApiResultHandler.buildApiResult(200, "缩略图生成成功", null);
    }


    /**
     * 文件上传<br>
     *
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/upload")
    public String fdfaUpload(@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        System.out.println("file===========" + file);
        FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
        return fileService.uploadFdfsFile(file);
    }


    /**
     * @author:胡立涛
     * @description: TODO 文件上传 图片、文件、视频
     * @date: 2021/12/6
     * @param: [file]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/uploadFile")
    public ApiResult fastdsUpload(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            String s = fileService.uploadFdfsFile(file);
            return ApiResultHandler.buildApiResult(200, "上传成功", fileServer + s);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "上传失败", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 上传 ，返回路径不带ip地址
     * @date: 2022/3/18
     * @param: [file]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/uploadFilePath")
    public ApiResult uploadFilePath(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            String s = fileService.uploadFdfsFile(file);
            Map<String, Object> rMap = new HashMap<>();
            return ApiResultHandler.buildApiResult(200, "上传成功", s);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "上传失败", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 视频上传并生成缩略图
     * @date: 2021/12/13
     * @param: [file]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("uploadVedio")
    public ApiResult uploadHdfsVedioFFmpegImage(@RequestParam("file") MultipartFile file) {

        if (Objects.isNull(file)) {
            return ApiResultHandler.buildApiResult(400, "请选择文件", null);
        }
        // 文件名称 (有可能是中文的，不影响)
        String originalFileName = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFileName)) {
            return ApiResultHandler.buildApiResult(400, "请选择文件", null);
        }
        // 只得到文件的名称，不包含后缀名
        String fileName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
        FFmpegFrameGrabber ff = null;
        String imageHdfsPath = null;
        // 图片格式
        final String imageMat = "jpg";
        try {
            // HDFS 上传到服务器 视频绝对路径
            String vedioHdfsFilePath = serverPrefix + "/" + fileService.uploadFdfsFile(file);
            log.info("HDFS视频位置: {} ", vedioHdfsFilePath);
            //  本地临时文件地址
            String imageLocalPath = System.getProperty("user.dir") + "/tempPic/" + fileName + "/" + fileName + "." + imageMat;
            ;
            //上传视频成功后，开始抽帧
            ff = FFmpegFrameGrabber.createDefault(vedioHdfsFilePath);
            ff.start();
            log.info("开始读取视频,视频长度:{}", ff.getLengthInFrames());
            Frame frame = ff.grabImage();
            //转换成图片
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bufferedImage = converter.getBufferedImage(frame);
            /**
             * 本地先生成缩略图,然后上传到 服务器地址\
             *  创建父级 目录
             */
            File outputFile = new File(imageLocalPath);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
                outputFile.createNewFile();
            }
            ImageIO.write(bufferedImage, imageMat, outputFile);
            ff.stop();
//          本地生成图片后 开始上传到 服务器地址
            StorePath storePath = storageClient.uploadFile(new FileInputStream(outputFile), outputFile.length(), FilenameUtils.getExtension(outputFile.getName()), null);
//          最后删除缩略图 文件
            File[] files = outputFile.getParentFile().listFiles();
            if (Objects.nonNull(files) && files.length > 0) {
                Arrays.stream(files).filter(e -> {
                    if (!e.isDirectory()) {
                        return true;
                    }
                    return false;
                }).forEach(e -> e.delete());
            }
            imageHdfsPath = serverPrefix + storePath.getFullPath();
            log.info("HDFS缩略图位置: {} ", imageHdfsPath);
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("videoPath", vedioHdfsFilePath);
            rMap.put("videoImg", imageHdfsPath);
            return ApiResultHandler.buildApiResult(200, "视频上传成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传视频 截图缩略图异常，异常信息:{}", e.getMessage());
            return ApiResultHandler.buildApiResult(500, "缩略图失败", e.getMessage());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 上传影像并生成缩略图
     * @date: 2021/12/10
     * @param: [file]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "tifUpload")
    public ApiResult tifUpload(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            String s = fileService.uploadFdfsFile(file);
            int i = s.lastIndexOf("/");
            String fileName = s.substring(i + 1);
            String tifId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis() + "_" + fileName;
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("filePath", fileServer + s);
            rMap.put("tifId", tifId.substring(0, tifId.indexOf(".")));
            rMap.put("cellPath", cellServerPath + fileName);
            // 生成影像缩略图
            ImageUtil.thumbnailImage(file.getInputStream(), tifId, localFilePath);
            rMap.put("smallPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + "_small.jpg");
            rMap.put("bigPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + "_big.jpg");
            return ApiResultHandler.buildApiResult(200, "上传成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "上传失败", null);
        }
    }


    /**
     * 通过MD5 查询 fastdfs 路径值
     *
     * @param tifId
     * @return
     */
    @GetMapping(value = "/getTifFastdfsRelationByTifId")
    public TifFastdfsRelation getTifFastdfsRelationByTifId(@RequestParam(value = "tifId") String tifId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("tif_server_id", tifId);
        List<TifFastdfsRelation> list = this.tifFastdfsRelationService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    /**
     * @author:胡立涛
     * @description: TODO 影像处理：上传、生成缩略图、切片、发布
     * @date: 2022/4/22
     * @param: [file]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "tifProcess")
    public ApiResult tifProcess(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            String s = fileService.uploadFdfsFile(file);
            int i = s.lastIndexOf("/");
            String fileName = s.substring(i + 1);
            String tifId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis() + "_" + fileName;
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("filePath", fileServer + s);
            // 生成影像缩略图
            ImageUtil.thumbnailImage(file.getInputStream(), tifId, localFilePath);
            rMap.put("smallPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + "_small.jpg");
            rMap.put("bigPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + "_big.jpg");
            // 影像服务 切片、发布
            // tiffastPath
//            String path = s.replace("group1/M00", "/root/docker/fastdfs/storage/data");
            String path = s.replace("group1/M00", tiffastPath);
            int id = fileService.tifUpload(path, fileName);
            if (id == -1) {
                return ApiResultHandler.buildApiResult(500, "影响服务接口调用异常", null);
            }
            rMap.put("id", id);
            // 不带ip地址的缩略图和影像地址
            rMap.put("noipSmallPic", tifId.substring(0, tifId.indexOf(".")) + "_small.jpg");
            rMap.put("noipbigPic", tifId.substring(0, tifId.indexOf(".")) + "_big.jpg");
            rMap.put("noipFilePath", s);

            try {
                /**
                 *  影像ID和文件服务器 文件路径映射关系表
                 */
                TifFastdfsRelation tifFastdfsRelation = new TifFastdfsRelation();
                tifFastdfsRelation.setCreate_time(new Date());
                tifFastdfsRelation.setFastdfsPath(s);
                tifFastdfsRelation.setTifServerId(String.valueOf(id));
//          名称转成 MD5
                String fileMd5 = Md5Utils.getMD5(fileName, "utf-8");
                tifFastdfsRelation.setFileMd5(fileMd5);
                this.tifFastdfsRelationService.saveOrUpdate(tifFastdfsRelation);
                log.info("上传影像文件成功: FASTDFS:{}  TIF-ID:{}", s, id);
            } catch (Exception e) {
                e.printStackTrace();
                log.info(" 保存镜像文件合 文件服务地址映射 异常 : FASTDFS:{}  TIF-ID:{}", s, id);
            }
            return ApiResultHandler.buildApiResult(200, "上传成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 图片上传并生成缩略图
     * @date: 2022/7/1
     * @param: [file]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/imageUpload")
    public ApiResult imageUpload(@RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            Map<String, Object> rMap = new HashMap<>();
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            String s = fileService.uploadFdfsFile(file);
            int i = s.lastIndexOf("/");
            String fileName = s.substring(i + 1);
            String name = fileName.substring(0, fileName.lastIndexOf("."));
            // 缩略图生成
            Image image = ImageIO.read(file.getInputStream());
            String prevfix = "_small";
            int w = 190;
            int h = 130;
            //生成小的缩略图
            ImageUtil.sendImage(image, w, h, localFilePath, name, prevfix, "jpg");
            rMap.put("filePath", fileServer + s);
            rMap.put("smallPic", localUrlPrefix + "/" + name + "_small.jpg");
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "图片上传失败", e.toString());
        }
    }

    /**
     * @author:lyd
     * @description: TODO 影像处理：上传、生成缩略图、切片、发布
     * @date: 2022/4/22
     * @param: [file]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(path = "/tifProcessMultiFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult tifProcessMultiFile(@RequestPart("multipartFile") MultipartFile multipartFile) {
        try {
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            String s = fileService.uploadFdfsFile(multipartFile);
            System.out.println("上传后路径: " + s);
            int i = s.lastIndexOf("/");
            String fileName = s.substring(i + 1);
            String tifId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis() + "_" + fileName;
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("filePath", fileServer + s);
            // 生成影像缩略图
            ImageUtil.thumbnailImage(multipartFile.getInputStream(), tifId, localFilePath);
            rMap.put("smallPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + "_small.jpg");
            rMap.put("bigPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + "_big.jpg");
            // 影像服务 切片、发布
//          String path = s.replace("group1/M00", "/root/docker/fastdfs/storage/data");
            String path = s.replace("group1/M00", tiffastPath);

            int id = fileService.tifUpload(path, fileName);
//            int id = 1234567;
            if (id == -1) {
                return ApiResultHandler.buildApiResult(500, "影响服务接口调用异常", null);
            }
            rMap.put("id", id);
            /**
             * TODO 增加TIF fastdfs路径位置和 返回的ID值 关系
             */
            try{
                TifFastdfsRelation tifFastdfsRelation = new TifFastdfsRelation();
                tifFastdfsRelation.setCreate_time(new Date());
                tifFastdfsRelation.setFastdfsPath(s);
                tifFastdfsRelation.setTifServerId(String.valueOf(id));
                String fileMd5 = Md5Utils.getMD5(fileName, "utf-8");
                tifFastdfsRelation.setFileMd5(fileMd5);
                this.tifFastdfsRelationService.saveOrUpdate(tifFastdfsRelation);
            }catch (Exception e){
                e.printStackTrace();
            }
            log.info("上传影像文件成功: FASTDFS:{}  TIF-ID:{}", s, id);
            return ApiResultHandler.buildApiResult(200, "上传成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败", null);
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 在线预览，文件复制
     * @date: 2022/6/14
     * @param: [sourcePath]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "copyFile")
    public ApiResult copyFile(@RequestBody Map<String, Object> map) {
        try {
            String sourcePath = map.get("sourcePath").toString();
            sourcePath = sourcepath + sourcePath;
            log.info("图谱文件位置: {} ", sourcePath);
            String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
            String destPath = destpath + fileName;
            File dest = new File(destPath);
            // 判断文件夹是否存在，若不存在，创建文件夹
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            if (!dest.exists()) {
                File source = new File(sourcePath);
                Files.copy(source.toPath(), dest.toPath());
            }
            log.info("拷贝后路径位置: {} ", dest.toPath());
            String viewUrl = kkFileViewUrl + fileName;
            return ApiResultHandler.buildApiResult(200, "操作成功", viewUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败", e.toString());
        }

    }


    /**
     * @author:胡立涛
     * @description: TODO 自动出题 将图谱中的图片上传至fastdfs服务
     * @date: 2022/11/15
     * @param: [sourcePath]
     * @return: java.lang.String
     */
    @PostMapping(value = "copyPic")
    public String copyPic(@RequestBody String sourcePath) {
        try {
            sourcePath = sourcePath.split("base64Path=")[1];
            sourcePath = sourcepath + sourcePath;
            log.info("图谱文件位置: {} ", sourcePath);
            String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
            String destPath = autodestPath + fileName;
            File dest = new File(destPath);
            // 判断文件夹是否存在，若不存在，创建文件夹
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            if (!dest.exists()) {
                File source = new File(sourcePath);
                Files.copy(source.toPath(), dest.toPath());
            }
            log.info("拷贝后路径位置: {} ", dest.toPath());
            return "group1/M00" + destPath.split("data")[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * @author:胡立涛
     * @description: TODO 将网络文件下载下来，并写入到指定位置
     * @date: 2022/8/17
     * @param: [map]参数格式： http://192.168.10.203:8888/group1/M00/00/02/wKgKy2L1tnyEAMmgAAAAAL8fSzA87.docx
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "copyFileHttp")
    public ApiResult copyFileHttp(@RequestBody Map<String, Object> map) throws Exception {
        OutputStream os = null;
        InputStream inputStream = null;
        try {
            String sourcePath = map.get("sourcePath").toString();
            URL url = new URL(sourcePath);
            log.info("fastds文件原访问地址：" + sourcePath);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            int httpResult = httpURLConnection.getResponseCode();
            if (httpResult != 200) {
                log.info("fastds文件服务连接失败");
                return ApiResultHandler.buildApiResult(500, "文件服务连接失败", sourcePath);
            }
            String path = destpath;
            String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
            String destPath = path + fileName;
            File dest = new File(destPath);
            // 判断文件夹是否存在，若不存在，创建文件夹
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            if (!dest.exists()) {
                byte[] bs = new byte[1024];
                int len;
                File tempFile = new File(path);
                os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
                inputStream = urlConnection.getInputStream();
                while ((len = inputStream.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
            }
            log.info("fastds拷贝后路径位置: {} ", dest.toPath());
            String viewUrl = kkFileViewUrl + fileName;
            return ApiResultHandler.buildApiResult(200, "操作成功", viewUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败", e.toString());
        } finally {
            if (os != null) {
                os.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 返回影像服务地址
     * @date: 2022/8/19
     * @param: []
     * @return: com.cloud.core.ApiResult
     */
    @GetMapping(value = "/getPath")
    public ApiResult getPath(@RequestParam(required = false) String type) {
        try {
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("localUrlPrefix",localUrlPrefix);
            rMap.put("fileAddr",fileServer);
            rMap.put("localFilePath",localFilePath);
            if (type != null && type.equals("1")) {
                // 产品服务前缀地址
                rMap.put("proServerUrl", proServerUrl);
            } else {
                // 影像服务前缀地址
                rMap.put("tifServerUrl", tifServerUrl);
            }
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 多文件上传
     * @date: 2022/11/17
     * @param: [files]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping(value = "/uploadMore")
    public ApiResult uploadMore(@RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            List<String> pathList = fileService.uploadMore(files);
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("fileServer", fileServer);
            rMap.put("pathList", pathList);
            return ApiResultHandler.buildApiResult(200, "上传成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "上传失败", null);
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 大文件 分片上传 前端进行文件分片，进度条计算展示
     * @date: 2022/11/17
     * @param: [file：分片文件流, name：文件名, md5：文件MD5值, size：文件大小, chunks：分片总数, chunk：当前分片数]
     * @return: com.cloud.core.ApiResult
     */

    // 文件MD5的缓存容器
    private static final ConcurrentMap<String, File> MD5_CACHE = new ConcurrentHashMap<String, File>();
    String filePath = null;

    @PostMapping(value = "chunkUpload")
    public ApiResult chunkUpload(@RequestParam(value = "file", required = true) MultipartFile file,
                                 @RequestParam(value = "name", required = true) String name,
                                 @RequestParam(value = "md5", required = true) String md5,
                                 @RequestParam(value = "size", required = true) Long size,
                                 @RequestParam(value = "chunks", required = true) Integer chunks,
                                 @RequestParam(value = "chunk", required = true) Integer chunk) {
        try {
            // 是否生成了文件
            File targetFile = MD5_CACHE.get(md5);
            // 没有生成
            if (targetFile == null) {
                String fileName = UUID.randomUUID().toString() + "_" + name;
                filePath = autodestPath + fileName;
                targetFile = new File(autodestPath, fileName);
                targetFile.getParentFile().mkdirs();
                MD5_CACHE.putIfAbsent(md5, targetFile);
            }
            // 可以对文件的任意位置进行读写
            RandomAccessFile accessFile = new RandomAccessFile(targetFile, "rw");
            // 是否是最后一片
            boolean finished = chunk == chunks;
            if (finished) {
                // 移动指针到指定位置
                accessFile.seek(size - file.getSize());
            } else {
                accessFile.seek((chunk - 1) * file.getSize());
            }
            // 写入分片的数据
            accessFile.write(file.getBytes());
            accessFile.close();
            if (finished) {
                // 上传成功，删除缓存信息
                MD5_CACHE.remove(md5);
            }
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("filePath", "group1/M00" + filePath.split("data")[1]);
            rMap.put("fileServer", fileServer);
            return ApiResultHandler.buildApiResult(200, "上传成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "分片上传失败", null);
        }
    }

    @PostMapping(value = "tifProcessPath")
    public ApiResult tifProcessPath(@RequestParam(value = "fileUrl", required = false) String fileUrl) throws Exception {
        InputStream is = null;
        try {
            String s = fileUrl.replace(fileServer, "");
            FileService fileService = fileServiceFactory.getFileService(FileSource.FDFS.name());
            int i = s.lastIndexOf("/");
            String fileName = s.substring(i + 1);
            String tifId = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis() + "_" + fileName;
            Map<String, Object> rMap = new HashMap<>();
            rMap.put("filePath", fileUrl);
            // 生成影像缩略图
            String imageName = tifId.substring(0, tifId.indexOf(".")) + ".jpg";
            ThumbnailGenerator.thumbnailImage(fileUrl, imageName, localFilePath);
            rMap.put("smallPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + "_big.jpg");
            rMap.put("bigPic", localUrlPrefix + "/" + tifId.substring(0, tifId.indexOf(".")) + ".jpg");
            // 不带ip地址的缩略图和影像地址
            rMap.put("noipSmallPic", tifId.substring(0, tifId.indexOf(".")) + "_big.jpg");
            rMap.put("noipbigPic", tifId.substring(0, tifId.indexOf(".")) + "_big.jpg");
            // 影像服务 切片、发布
            String path = s.replace("group1/M00", "");
            path = tiffastPath + path;
            log.info(String.format("File tifUpload,fastPath:%s,path:%s", s, path));
            int id = fileService.tifUpload(path, fileName);
            if (id == -1) {
                return ApiResultHandler.buildApiResult(500, "影像服务接口调用异常", null);
            }
            rMap.put("id", id);
            rMap.put("noipFilePath", s);
            try {
                /**
                 *  影像ID和文件服务器 文件路径映射关系表
                 */
                TifFastdfsRelation tifFastdfsRelation = new TifFastdfsRelation();
                tifFastdfsRelation.setCreate_time(new Date());
                tifFastdfsRelation.setFastdfsPath(s);
                tifFastdfsRelation.setTifServerId(String.valueOf(id));
//          名称转成 MD5
                String fileMd5 = Md5Utils.getMD5(fileName, "utf-8");
                tifFastdfsRelation.setFileMd5(fileMd5);
                this.tifFastdfsRelationService.saveOrUpdate(tifFastdfsRelation);
                log.info("上传影像文件成功: FASTDFS:{}  TIF-ID:{}", s, id);
            } catch (Exception e) {
                e.printStackTrace();
                log.info(" 保存镜像文件合 文件服务地址映射 异常 : FASTDFS:{}  TIF-ID:{}", s, id);
            }
            return ApiResultHandler.buildApiResult(200, "上传成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "操作失败", e.toString());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
