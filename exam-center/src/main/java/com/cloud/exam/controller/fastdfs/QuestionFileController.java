package com.cloud.exam.controller.fastdfs;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.service.QuestionService;
import com.cloud.exam.utils.FileUtils;
import com.cloud.executors.WorkorderExecutors;
import com.cloud.feign.file.FileClientFeign;
import com.cloud.redislock.RedisLock;
import com.cloud.utils.StringUtils;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.exception.FdfsException;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件上传
 */
@Slf4j
@RestController
@RefreshScope
public class QuestionFileController {

    private Logger logger = LoggerFactory.getLogger(QuestionFileController.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private StorageService storageService;

    @Value("${file.upload.dir}")
    private String fileUploadPath;

    @Resource
    private QuestionService questionService;

    @Value("${questionFile.maxNum:2000}")
    private Integer maxNum;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;
    /**
     * 分别是锁
     */
    @Autowired
    private RedisLock redisLock;


    @Autowired
    private FileClientFeign fileClientFeign;

    /**
     * 秒传判断，断点判断
     *
     * @return
     */
    @RequestMapping(value = "checkFileMd5", method = RequestMethod.POST)
    @ResponseBody
    public Object checkFileMd5(String md5) throws IOException {
        Object processingObj = stringRedisTemplate.opsForHash().get(Constants.FILE_UPLOAD_STATUS, md5);
        if (Objects.isNull(processingObj)) {
            return new ResultVo(ResultStatus.NO_HAVE);
        }
        String processingStr = processingObj.toString();
        boolean processing = Boolean.parseBoolean(processingStr);
        String value = stringRedisTemplate.opsForValue().get(Constants.FILE_MD5_KEY + md5);
        if (processing) {
            log.info("正在上传的文件:{} 已存在。 继续解析ZIP包，上传试题 ",value);
            return new ResultVo(ResultStatus.NO_HAVE, value);
        } else {
//          如果 已上传到了服务器，但是文件分片没有完整的上传到服务器。
            File confFile = new File(value);
            byte[] completeList = FileUtils.readFileToByteArray(confFile);
            List<String> missChunkList = new LinkedList<>();
            for (int i = 0; i < completeList.length; i++) {
                if (completeList[i] != Byte.MAX_VALUE) {
                    missChunkList.add(i + "");
                }
            }
            return new ResultVo<>(ResultStatus.ING_HAVE, missChunkList);
        }
    }


    /**
     * 上传文件
     *
     * @param param
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity fileUpload(MultipartFileParam param, HttpServletRequest request) {

        String zipName = param.getName();
        String zipPath = "";
        String uploadResult = "上传成功 !! ";
        int status = 200;
        /**
         *  先验证服务器是否 已有上传的文件
         */
        String uploadFilePath = stringRedisTemplate.opsForValue().get(Constants.FILE_MD5_KEY + param.getMd5());
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if(StringUtils.isNotBlank(uploadFilePath)){
            File fileExits = new File(uploadFilePath);
            if(!fileExits.exists()){
                if (isMultipart) {
                    logger.info("{} 上传文件start 当前第 {} 片 总片数:{} ", zipName, param.getChunk(), param.getChunks());
                    try {
//                      上传文件 在redis中记录 上传的状态  。
                        storageService.uploadFileByMappedByteBuffer(param);
                        logger.info("{} 上传当前文件成功!! ", zipName);
                    } catch (IOException e) {
                        status = 500;
                        e.printStackTrace();
                        logger.error("文件上传失败。{}", param.toString());
                        return ResponseEntity.status(status).body("上传 [" + zipName + "] 失败");
                    }
                } else {
                    status = 400;
                    uploadResult = "上传请求头不正确。";
                    return ResponseEntity.status(status).body(uploadResult);
                }
            }
        }else{
            if (isMultipart) {
                logger.info("{} 上传文件start 当前第 {} 片 总片数:{} ", zipName, param.getChunk(), param.getChunks());
                try {
//                  上传文件 在redis中记录 上传的状态  。
                    storageService.uploadFileByMappedByteBuffer(param);
                    logger.info("{} 上传当前文件成功!! ", zipName);
                } catch (IOException e) {
                    status = 500;
                    e.printStackTrace();
                    logger.error("文件上传失败。{}", param.toString());
                    return ResponseEntity.status(status).body("上传 [" + zipName + "] 失败");
                }
            } else {
                status = 400;
                uploadResult = "上传请求头不正确。";
                return ResponseEntity.status(status).body(uploadResult);
            }
        }
        /**
         *  异步 执行 解析和上传
         */
        Object uploadStatus = stringRedisTemplate.opsForHash().get(Constants.FILE_UPLOAD_STATUS, param.getMd5());
        if (Objects.isNull(uploadStatus) || !Boolean.valueOf(uploadStatus.toString())) {
            status = 200;
            uploadResult = "分片上传中";
        } else {
            /**
             *  3秒内尝试枷锁，如果不能枷锁，退出
             */
            if (redisLock.tryLock(stringRedisTemplate.opsForValue().get(Constants.FILE_MD5_KEY + param.getMd5()), 3l, TimeUnit.SECONDS)) {
                try {
                    zipPath = stringRedisTemplate.opsForValue().get(Constants.FILE_MD5_KEY + param.getMd5());
                    log.info("{} 上传后服务器地址: {} ", zipName, zipPath);
                    if (StringUtils.isBlank(zipPath)) {
                        return ResponseEntity.status(500).body("Redis中，没有缓存上传的路径");
                    }
                    String unZipPath = uncompressZip(zipPath, fileUploadPath);
                    log.info("{} 解压完毕，解压后的文件路径: {}", zipName, unZipPath);
                    if (StringUtils.isNotBlank(unZipPath)) {
//                      解析JSON 试题文件 。
                        analysisUnZipFolder(unZipPath);
                    } else {
                        uploadResult = "解压后路径地址为空";
                        status = 504;
                    }
                } catch (Exception e) {
                    status = 500;
                    uploadResult = String.format("解析异常[%s]", e.getMessage());
                    e.printStackTrace();
                } finally {
                    redisLock.unLock(stringRedisTemplate.opsForValue().get(Constants.FILE_MD5_KEY + param.getMd5()));
                }
            } else {
                status = 200;
                uploadResult = String.format(" [%s]解析中", zipName);
                log.warn(uploadResult);
            }
            ;
        }
        return ResponseEntity.status(status).body(uploadResult);
    }


    /**
     * 解析 解压后的文件
     *
     * @param sourceUnzipFolder
     */
    public void analysisUnZipFolder(String sourceUnzipFolder) {
        File file = new File(sourceUnzipFolder);
        if (!file.exists()) {
            throw new RuntimeException(sourceUnzipFolder + " 解压后，文件不存在。请核查！");
        }
        File[] jsonFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(UploadQuestionConfig.QUESTTION_FILE_SUFFIX.toLowerCase());
            }
        });
        /**
         *  解析全部试题，全部上传
         */
        if (Objects.nonNull(jsonFiles) && jsonFiles.length > 0) {
            File jsonFile = jsonFiles[0];
            importQuestionByjsonfile(sourceUnzipFolder, jsonFile);
        }else{
            log.warn("[WARN] 解压后JSON文件， byte 为 0 ");
            throw new RuntimeException(sourceUnzipFolder + " 解压后试题文件 内容为 0 byte ");
        }
    }


    /**
     * @param sourceUnzipFolder 需要解析的文件夹目录
     * @param jsonFile
     * @return
     */
    public ApiResult importQuestionByjsonfile(String sourceUnzipFolder, File jsonFile) {

        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        List<Question> list = new ArrayList<>();
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(jsonFile));
            bufferedReader = new BufferedReader(inputStreamReader);
            JSONReader jsonReader = new JSONReader(bufferedReader);
            jsonReader.startArray();
            while (jsonReader.hasNext()) {
                JSONObject jsonObject = (JSONObject) jsonReader.readObject();
                Question q = JSON.toJavaObject(jsonObject, Question.class);
                List<String> kpIds = q.getKpIds();
                Question qq = new Question();
                BeanUtils.copyProperties(q, qq);
                Pattern pattern = Pattern.compile("\\s*|\t|\n");
                Matcher m1 = pattern.matcher(qq.getQuestion());
                String s1 = m1.replaceAll("");
                qq.setQuestion(s1);
                Matcher m2 = pattern.matcher(qq.getAnswer());
                m2.replaceAll("");
                String s2 = m2.replaceAll("");
                qq.setAnswer(s2);
                Matcher m3 = pattern.matcher(qq.getAnalysis());
                m3.replaceAll("");
                String s3 = m3.replaceAll("");
                qq.setAnalysis(s3);
                Matcher m4 = pattern.matcher(qq.getOptions());
                m4.replaceAll("");
                String s4 = m4.replaceAll("");
                qq.setOptions(s4);
                qq.setUpdateTime(new Date());
                qq.setCreateTime(new Date());
                qq.setKpIds(kpIds);
//              写死如果是倒入的试题 0L
                qq.setCreator(0L);
                list.add(qq);
            }
            jsonReader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("解析迁入的json文件失败 失败JSON：{}", e.getMessage());
            throw new RuntimeException(jsonFile.getName() + " 解析文件异常，请检查格式。");
        } finally {
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        }
        int j = (list.size() + maxNum - 1) / maxNum;
        List<List<Question>> mgList = new ArrayList<>();
        Stream.iterate(0, n -> n + 1).limit(j).forEach(i -> {
            mgList.add(list.stream().skip(i * maxNum).limit(maxNum).collect(Collectors.toList()));
        });
        try {
            final CountDownLatch downLatch = new CountDownLatch(mgList.size());
            mgList.parallelStream().forEach(questionList -> {
                QuestionFileController.ImportQuestionTask task = new QuestionFileController.ImportQuestionTask(downLatch,
                        appendFileStorageClient, sourceUnzipFolder, questionList, fileClientFeign);
                WorkorderExecutors.getInstance().customerService.execute(task);
            });
            downLatch.await();
            log.info("[END 试题迁入 SUCCESS ] {} 执行结束 ++++++++++++++++++++++++++++++++++++++++++++ ", sourceUnzipFolder);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("线程池执行，出现异常，异常信息:{} ", e.getMessage());
        }
        return ApiResultHandler.buildApiResult(ApiResultHandler.success().getCode(), "试题迁入完成。", null);
    }


    public static void main(String[] args) {

        String questionContent = "{\"text\":\"测试地图制图整饰\"," +
                "\"url\":[\"group1/M00/00/2D/wKgKy2GprlGERoowAAAAAAN00OE256.png\",\"group1/M00/00/3C/wKgKy2K8FFyEL0-XAAAAAHaB4z8570.png\"]}";
        JSONObject jsonObject = JSONObject.parseObject(questionContent);

        Object o = jsonObject.get("url");
        if (Objects.isNull(o)) {


        }
    }


    //    内部类
    class ImportQuestionTask implements Runnable {

        /**
         * TODO 目前只把图片信息的 路径过滤出来
         * 如果还有TIF的路径，不需要验证 是否有后缀名称
         */
        private Pattern pattern = Pattern.compile(".*?(group1.*?\\.[a-zA-Z0-9]{1,})");

        private AppendFileStorageClient appendFileStorageClient;

        public String sourceUnzipFolderPath;

        private List<Question> questionList;

        private CountDownLatch downLatch;

        private FileClientFeign fileClientFeign;

        public ImportQuestionTask(CountDownLatch downLatch, AppendFileStorageClient appendFileStorageClient,
                                  String sourceUnzipFolderPath, List<Question> list, FileClientFeign fileClientFeign) {
            this.downLatch = downLatch;
            this.appendFileStorageClient = appendFileStorageClient;
            this.sourceUnzipFolderPath = sourceUnzipFolderPath;
            this.questionList = list;
            this.fileClientFeign = fileClientFeign;
        }

        @Override
        public void run() {
            try {
                questionList.stream().forEach(e -> {
                    try {
//                      迁入试题时，文件上传到fastdfs后 ,得到最新的地址替换试题中原有的问价地址。
                        updateQuestionFilePath(sourceUnzipFolderPath, e);
                        questionService.saveJsonAndFiles(e, e.getKpIds());
                    } catch (Exception innerE) {
                        innerE.printStackTrace();
                        log.error("迁入试题，试题: {} 异常：{} ", JSON.toJSONString(e), innerE.getMessage());
                    }
                });
            } finally {
                downLatch.countDown();
            }

        }

        /**
         * @param sourceUnzipFolderPath,解压后的路径
         * @param question
         */
        public void updateQuestionFilePath(String sourceUnzipFolderPath, Question question) {
//            System.out.println("上传前json: " + JSON.toJSONString(question));
            /**
             *  问题ID，查找对应试题的文件夹时，使用
             */
            Long folderId = question.getId();
//            答案JSON
            String answer = question.getAnswer();
            if (StringUtils.isNotBlank(answer)) {
                JSONObject answerJsonObj = JSONObject.parseObject(answer);
                if (Objects.nonNull(answerJsonObj)) {
                    uploadFileAndUpdateQuestion(String.valueOf(folderId), answerJsonObj);
                    question.setAnswer(answerJsonObj.toJSONString());
                }
            }
//            题干
            String content = question.getQuestion();
            if (StringUtils.isNotBlank(content)) {
                JSONObject contentObject = JSONObject.parseObject(content);
                if (Objects.nonNull(contentObject)) {
                    uploadFileAndUpdateQuestion(String.valueOf(folderId), contentObject);
                    question.setQuestion(contentObject.toJSONString());
                }
            }
//            解析
            String analysis = question.getAnalysis();
            if (StringUtils.isNotBlank(analysis)) {
                JSONObject analysisObject = JSONObject.parseObject(analysis);
                uploadFileAndUpdateQuestion(String.valueOf(folderId), analysisObject);
                question.setAnalysis(analysisObject.toJSONString());
            }
//            选项
            String options = question.getOptions();
            if (StringUtils.isNotBlank(options)) {
                JSONObject optionsObject = JSONObject.parseObject(options);
                uploadFileAndUpdateQuestion(String.valueOf(folderId), optionsObject);
                question.setOptions(optionsObject.toJSONString());
            }
//          最后ID 设置为NULL，导入到新的数据库产生新的数据库ID
            question.setId(null);
//            System.out.println("上传后json: " + JSON.toJSONString(question));
        }

        /**
         * filePath
         *
         * @param
         * @return
         */
        public String regexGroupFilePath(String filePath) {

            if (StringUtils.isBlank(filePath)) {
                return "";
            }
            Matcher matcher = pattern.matcher(filePath);
            while (matcher.find()) {
                return matcher.group(0);
            }
            return "";
        }


        /**
         * 递归解析JSON，给URL赋值
         *
         * @param questionFileId
         * @param jsonObject_root
         * @return
         */
        public JSONObject uploadFileAndUpdateQuestion(String questionFileId, JSONObject jsonObject_root) {

            Object currentURL_Object = jsonObject_root.get("url");
            if (Objects.isNull(currentURL_Object)) {
                /**
                 *  当前结构层级下, 没有URL的 KEY
                 *   所以，解析 当前结构下，value中是否有 URL kEY键
                 */
                Map<String, Object> innerMap = jsonObject_root.getInnerMap();
                innerMap.forEach((k, v) -> {
                    try {
                        JSONObject valueObject = JSONObject.parseObject(v.toString());
                        uploadFileAndUpdateQuestion(questionFileId, valueObject);
                    } catch (Exception e) {
//                        log.warn("K: {}  value:{}  已是最后的层级 ", k, v);
                    }
                });
            } else {
                /**
                 *  如果 当前层级下 包含 URL，那么继续解析
                 */
                JSONArray array = JSONArray.parseArray(currentURL_Object.toString());
                if (CollectionUtils.isEmpty(array)) {
                    return jsonObject_root;
                }
                List<String> urlCollectList = array.toJavaList(String.class);
                List<String> updateList = new ArrayList<>();
//               解析URL中，包含的所有的 文件 地址
                for (String path : urlCollectList) {
                    String s = regexGroupFilePath(path);
                    if (StringUtils.isBlank(s)) {
                        log.warn(" 试题：{}, 文件[不包含后缀名称]  试题中路径: {} ", questionFileId, path);
                        continue;
                    }
                    String localFilePath = sourceUnzipFolderPath + "/" + questionFileId + "/" + path;
//                  得到 当前上传到服务器的路径
                    File localFile = new File(localFilePath);
                    if (!localFile.exists()) {
                        log.warn(" 试题ID :[{}]， 文件不存在。[{}]", questionFileId, path);
                        continue;
                    } else {
//                      如果是影像TIF 文件，调用
                        if (path.toLowerCase().indexOf(".tif") > -1) {
                            try {
                                // TODO 这个文件可能会是一个大的文件。
                                MultipartFile multipartFile = FileUtils.fileDoMultipartFile("multipartFile", localFile);
                                ApiResult apiResult = this.fileClientFeign.tifProcessMultiFile(multipartFile);
                                if (Objects.isNull(apiResult)) {
                                    continue;
                                }
                                if (apiResult.getCode() == 200) {
                                    Map map = (Map) apiResult.getData();
                                    Object id = map.get("id");
                                    if (Objects.nonNull(id)) {
                                        updateList.add(id.toString());
                                    }
                                } else {
                                    log.warn(" 试题:[{}]，文件:[{}]  接口返回code: {}", questionFileId, path, apiResult.getCode());
                                    continue;
                                }
                            } catch (Exception e) {
                                log.warn(" 试题:[{}]，文件:[{}]  上传过程发生异常：{}", questionFileId, path, e.getMessage());
                                continue;
                            }
                        } else {
//                          非影像文件上传，直接上传到 文件服务器
                            String fileExt = localFile.getName().substring(localFile.getName().lastIndexOf(".") + 1);
                            try {
                                StorePath storePath = this.appendFileStorageClient
                                        .uploadAppenderFile(FastdfsConfig.FASTDFS_GROUP, new FileInputStream(localFile), localFile.length(), fileExt);
                                if (Objects.nonNull(storePath)) {
                                    updateList.add(storePath.getFullPath());
                                }
                            } catch (FdfsException fe) {
                                fe.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
//              最后更新当前结构下，URL集合中的最新的 文件服务器的位置。
                if (CollectionUtils.isNotEmpty(updateList)) {
                    jsonObject_root.put("url", updateList);
                }
            }
            return jsonObject_root;
        }


    }


    /**
     * 解压ZIP文件
     *
     * @param sourcePath
     */
    public String uncompressZip(String sourcePath, String destDirPath) throws Exception {
        //获取当前压缩文件
        File srcFile = new File(sourcePath);
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "源文件路径不存在");
        }
        InputStream inputStream = null;
        BufferedInputStream bufferIn = null;
        FileOutputStream fos = null;
        BufferedOutputStream outBuffer = null;
//      解压后的 路径
        String unzipdDirPath = destDirPath + "/" + srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
        //创建压缩文件对象
        ZipFile zipFile = new ZipFile(srcFile);
        //开始解压
        Enumeration<?> entries = zipFile.getEntries();
        try {
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    srcFile.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    inputStream = zipFile.getInputStream(entry);
                    bufferIn = new BufferedInputStream(inputStream);
                    outBuffer = new BufferedOutputStream(new FileOutputStream(targetFile));
                    byte[] buff = new byte[1024 * 1024 * 50];
                    int n;
                    while ((n = bufferIn.read(buff)) != -1) {
                        outBuffer.write(buff, 0, n);
                    }
                    int len;
                    while ((len = bufferIn.read(buff)) != -1) {
                        fos.write(buff, 0, len);
                    }
                }
            }
            log.info("[SUCCESS]>>>>>>>>>>>>源文件路径 {}", sourcePath);
            log.info("[SUCCESS]>>>>>>>>>>>>解压后路径 {}", unzipdDirPath);
        } catch (Exception e) {
            unzipdDirPath = null;
            e.printStackTrace();
            throw new RuntimeException(srcFile.getName() + "解压ZIP异常");
        } finally {
            try {
                if (Objects.nonNull(outBuffer)) {
                    outBuffer.flush();
                    outBuffer.close();
                }
                if (Objects.nonNull(bufferIn)) {
                    bufferIn.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return unzipdDirPath;
    }


}
