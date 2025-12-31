package com.cloud.exam.utils.thread;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.exam.controller.fastdfs.FastdfsConfig;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.feign.file.FileClientFeign;
import com.cloud.model.file.TifFastdfsRelation;
import com.cloud.utils.StringUtils;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每个试题中的文件，在一个线程中 执行
 */
@Slf4j
public class QuestionDownThread implements Runnable {

    private String targetFolder;
    private QuestionManage question;
    /**
     * 线程同步器
     */
    private CountDownLatch downLatch;

    /**
     * TODO 目前只把图片信息的 路径过滤出来
     */
    private Pattern pattern = Pattern.compile(".*?(group1.*?\\.[a-zA-Z0-9]{1,})");

    private FileClientFeign fileClientFeign;
    private FastFileStorageClient fastFileStorageClient;

    public QuestionDownThread() {
    }

    public QuestionDownThread(String targetFolder, final QuestionManage question, CountDownLatch downLatch,
                              FastFileStorageClient fastFileStorageClient, FileClientFeign fileClientFeign) {

        this.targetFolder = targetFolder;
        this.question = question;
        this.fastFileStorageClient = fastFileStorageClient;
        this.downLatch = downLatch;
        this.fileClientFeign = fileClientFeign;
    }

    @Override
    public void run() {
        try {
//           得到 当前试题中包含的所有的 URL
            List<String> fileList = wrapQuestionFiles(question);
            if (CollectionUtils.isNotEmpty(fileList)) {
                fileList.stream().forEach(path -> {
                    downWriteFile(targetFolder, path);
                });
            }
        } finally {
            if (Objects.nonNull(downLatch)) {
                downLatch.countDown();
            }
        }
    }


    public List<String> wrapQuestionFiles(QuestionManage question) {
        if (Objects.isNull(question)) {
            return Collections.emptyList();
        }
        List<String> allFileList = new ArrayList<>();
        // 题干
        String questionContent = question.getQuestion();
        List<String> questionList = new ArrayList<>();
        try {
            JSONObject questionObject = JSONObject.parseObject(questionContent);
            allFileList.addAll(convertToMapCollectUrl(questionList, questionObject));
            question.setQuestion(questionObject.toJSONString());
        } catch (Exception e) {
        }
        String analysis = question.getAnalysis();
        List<String> analysisList = new ArrayList<>();
        try {
            JSONObject anaJsonObject = JSONObject.parseObject(analysis);
            allFileList.addAll(convertToMapCollectUrl(analysisList, anaJsonObject));
            question.setAnalysis(anaJsonObject.toJSONString());
        } catch (Exception e) {

        }
        String answer = question.getAnswer();
        List<String> answerList = new ArrayList<>();
        try {
            JSONObject answerObject = JSONObject.parseObject(answer);
            allFileList.addAll(convertToMapCollectUrl(answerList, answerObject));
        } catch (Exception e) {

        }
        String options = question.getOptions();
        List<String> optionsList = new ArrayList<>();
        try {
            JSONObject optionsObject = JSONObject.parseObject(options);
            allFileList.addAll(convertToMapCollectUrl(optionsList, optionsObject));
        } catch (Exception e) {
        }
        return allFileList;
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

    

//    public static void main(String[] args) {
//        Map<String, Object> question = new HashMap<>();
//        List<String> url = new ArrayList<>();
//        url.add("http://xxxxxxxxxx");
//        question.put("url1", url);
//
//        String mapString = JSON.toJSONString(question);
//        JSONObject jsonObject = JSONObject.parseObject(mapString);
//
//        List object = (List) jsonObject.get("url");
//        System.out.println(JSON.toJSONString(object));
//
//    }

    /**
     * 递归 获取URL
     *
     * @param resultList
     * @param jsonObject_root
     * @return
     */
    public List<String> convertToMapCollectUrl(final List<String> resultList, JSONObject jsonObject_root) {

        if (Objects.isNull(jsonObject_root)) {
            return resultList;
        }
        Object currentURL_Object = jsonObject_root.get("url");
        if (Objects.nonNull(currentURL_Object)) {
//          每一个层级结构 URL 集合
            List<String> currentList = new ArrayList<>();
            JSONArray array = JSONArray.parseArray(currentURL_Object.toString());
            if (CollectionUtils.isNotEmpty(array)) {
                List<String> collect = array.toJavaList(String.class);
//                 验证 FASTDFS中路径，有后缀的 无后缀的
                collect.stream().forEach(path -> {
                    String s = regexGroupFilePath(path);
                    if (StringUtils.isNotBlank(s)) {
                        resultList.add(s);
                        currentList.add(s);
                    } else {
//                      目前只有影像文件会有 ，影像ID和上传到服务器后的位置有映射关系
                        TifFastdfsRelation tifFastdfs = null;
                        try {
                            tifFastdfs = fileClientFeign.getTifFastdfsRelationByTifId(path);
                            if (Objects.nonNull(tifFastdfs)) {
                                resultList.add(tifFastdfs.getFastdfsPath());
                                currentList.add(tifFastdfs.getFastdfsPath());
                                log.info("试题ID:{} ,切片ID: {}，影像路径:{} ", question.getId(), path, tifFastdfs.getFastdfsPath());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error(" 影像试题: {}，远程查询，映射关系异常 :{} ", question.getId(), e.getMessage());
                        }
                    }
                });
                jsonObject_root.put("url", currentList);
            }
        } else {
//          如果当前JSON结构中, 没有URL 键值对，那么就是解析 所有的VALUE是否含有，
            Map<String, Object> innerMap = jsonObject_root.getInnerMap();
            innerMap.forEach((k, v) -> {
                try{
                    JSONObject vObject = JSONObject.parseObject(v.toString());
                    convertToMapCollectUrl(resultList, vObject);
                }catch (Exception e){
                }
            });
        }
        return resultList;

    }


    public String downWriteFile(String targetFolder, String fdfsFile) {

        String target_prefix = targetFolder + "/" + question.getId() + "/";
//      fastdfs的前缀
        String fastDfsPathPrefix = FastdfsConfig.FASTDFS_GROUP + "/" + FastdfsConfig.FASTDFS_M00 + "/";
        String fdfsPathParams = fdfsFile.substring(fastDfsPathPrefix.length());
        /**
         *  最后 拼出输出文件的 具体位置
         */
        String outPath = target_prefix + fdfsFile;
        BufferedInputStream buffin = null;
        FileOutputStream fileOutputStream = null;// 获取文件输出IO流
        BufferedOutputStream bufferOut = null; // 输出流
        try {
            //得到一个文件全部的字节流 。
            byte[] content = fastFileStorageClient.downloadFile(FastdfsConfig.FASTDFS_GROUP, fdfsPathParams, new DownloadByteArray());
            // 本地文件位置
            byte[] buffer = new byte[1024];
            File targetFile = new File(outPath);
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            InputStream inputStream = new ByteArrayInputStream(content);
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
            log.info("******文件下载成功 :{} ", outPath);
        } catch (Exception e) {
            log.info("[ERROR]文件下载异常: {} {}  异常信息:{} ", FastdfsConfig.FASTDFS_GROUP, fdfsPathParams, e.getMessage());
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
        return outPath;
    }


}
