package com.cloud.exam.utils.word;

import com.alibaba.fastjson.JSONObject;
import com.cloud.exam.model.exam.QuestionManage;
import com.cloud.exam.utils.FileFastdfsUtils;
import com.cloud.utils.ObjectUtils;
import com.cloud.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.entity.ContentType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordImportUtil {
    private static final Logger logger = LoggerFactory.getLogger(WordImportUtil.class);

    /**
     * 解析word文件中的试题，如果包含不能导入的试题，则标记，并上传到fastdfs，供用户下载
     *
     * @param inputStream
     * @param originalFilename
     * @param knowledgePoints
     * @param questionContextList
     * @param successList
     * @return
     * @throws Exception
     */
    public static String importWordAndMark(InputStream inputStream, String originalFilename, Map<String, String> knowledgePoints
            , Map<String, List<QuestionManage>> questionContextList, List<QuestionManage> successList) throws Exception {

        XWPFDocument doc;
        //问答题是否过答案了
        boolean questionAuswerFlag = false;
        //验证试题是否有错
        boolean questionErrorFlag = false;

        //是否走知识点了
        boolean kpIFlag = false;
        try {
            doc = new XWPFDocument(inputStream);
        } catch (IOException e) {
            logger.error("load word file error", e);
            return null;
        }

        List<WordParagraph> wordParagraphList = new ArrayList<>();
        WordParagraph wordParagraph;
        List<QuestionManage> errorList = new ArrayList<>();
        QuestionManage question = null;
        //读取文件保存信息
        if (doc.getParagraphs() != null) {
            for (XWPFParagraph xwpfParagraph : doc.getParagraphs()) {

                wordParagraph = new WordParagraph();
                if (StringUtils.isNotEmpty(xwpfParagraph.getParagraphText())) {
                    wordParagraph.setParagraphText(xwpfParagraph.getParagraphText());
                }
                wordParagraph.setXwpfParagraph(xwpfParagraph);
                //文章的图片上传（word上传fdfs）
                String[] imgs = readImageInfo(xwpfParagraph);

                if (imgs != null) {
                    wordParagraph.setParagraphImages(imgs);
                }
                if (StringUtils.isNotEmpty(wordParagraph.getParagraphText()) || wordParagraph.getParagraphImages() != null) {
                    wordParagraphList.add(wordParagraph);
                }
            }

        }
        MyXWPFCommentsDocument commentsDocument = MyXWPFCommentsDocument.createCommentsDocument(doc);
        List<WordParagraph> wordParagraphHandledList = new ArrayList<>();

        JSONObject objectMap;
        if (CollectionUtils.isNotEmpty(wordParagraphList)) {
            //文字与图片的关系绑定
            for (int i = 0; i < wordParagraphList.size(); i++) {
                if ((i + 1) < wordParagraphList.size() && wordParagraphList.get(i + 1).getParagraphImages() != null &&
                        StringUtils.isEmpty(wordParagraphList.get(i + 1).getParagraphText())) {
                    wordParagraphList.get(i).setParagraphImages(wordParagraphList.get(i + 1).getParagraphImages());
                    wordParagraphHandledList.add(wordParagraphList.get(i));
                    i++;
                } else {
                    wordParagraphHandledList.add(wordParagraphList.get(i));
                }

            }
            //试题的题干
            String questionContext = "";
            String questionContextText = "";
            WordParagraph questionContextParagraph = null;
            for (int i = 0; i < wordParagraphHandledList.size(); i++) {
                WordParagraph paragraph = wordParagraphHandledList.get(i);
                String[] images = paragraph.getParagraphImages();

                if (ObjectUtils.isNull(images)) {
                    images = new String[0];
                }
                String textCon = "text";
                if (StringUtils.isNotEmpty(paragraph.getParagraphText())) {
                    textCon = paragraph.getParagraphText().trim();
                }
                if (i == 0) {
                    question = new QuestionManage();
                }
                objectMap = new JSONObject();
                if (textCon.contains("题】")) {
                    String bracket = textCon.substring(textCon.indexOf("【"), textCon.indexOf("】") + 1);
                    if (StringUtils.isNotEmpty(bracket)) {
                        int type = queryQuestionType(bracket);
                        //试题类型
                        question.setType(type);
                    }

                    //试题的题干
                    questionContextText = textCon.replaceAll(bracket, "");
                    //题干的括号空格处理
                    questionContextText = questionContextText.replaceAll("\\s+", " ");
                    questionContextParagraph = paragraph;
                    objectMap.put("text", questionContextText);
                    objectMap.put("url", images);
                    questionContext = objectMap.toJSONString();
                    //试题题目
                    question.setQuestion(questionContext);
                    if (StringUtils.isBlank(questionContextText)) {
                        questionErrorFlag = true;
                        question.setErrorText("试题题干为空");
                        question.setErrorType(5L);
                        commentsDocument.insertCommentToParagraph(paragraph.getXwpfParagraph(), "试题题干为空");
                    }
                }
                //选项  正则验证
                if (getStrings(textCon)) {
                    Map<String, Object> parmMap = new HashMap<>();
                    String text = getMatcherResult(textCon);
                    parmMap.put("text", text);
                    parmMap.put("url", images);
                    if (StringUtils.isNotEmpty(question.getOptions())) {
                        JSONObject optiondata = JSONObject.parseObject(question.getOptions());
                        optiondata.put(textCon.split("、")[0], parmMap);
                        question.setOptions(optiondata.toJSONString());
                    } else {
                        objectMap.put(textCon.split("、")[0], parmMap);
                        question.setOptions(objectMap.toJSONString());
                    }
                }
                //答案
                if (textCon.contains("【答案】")) {
                    //问答题答案处理；
                    if (question.getType() != null && question.getType() == 6) {
                        questionAuswerFlag = true;
                        //continue;
                    }

                    objectMap = new JSONObject(true);
                    JSONObject optionMap = new JSONObject(true);

                    String auswer = textCon.replaceAll("【答案】", "").trim();
                    if (StringUtils.isBlank(auswer)) {
                        JSONObject jsonObject = new JSONObject();
                        if (Integer.parseInt(question.getType() + "") == 4) {
                            //填空题
                            jsonObject.put("edit1", "");
                        } else {
                            jsonObject.put("text", "");
                            jsonObject.put("url", new String[0]);
                        }
                        question.setErrorText("试题答案为空");
                        question.setErrorType(4L);
                        question.setAnswer(jsonObject.toJSONString());
                        commentsDocument.insertCommentToParagraph(paragraph.getXwpfParagraph(), "试题答案为空");
                        questionErrorFlag = true;
                    } else {
                        int type = question.getType() == null ? -1 : question.getType();
                        //填空题
                        if (type == 4) {
                            String[] auswers = auswer.split("；");
                            if (StringUtils.isNotEmpty(auswers)) {
                                for (int j = 0; j < auswers.length; j++) {
                                    objectMap.put("edit" + (j + 1), auswers[j]);
                                    optionMap.put("edit" + (j + 1), "");
                                }
                                question.setAnswer(objectMap.toJSONString());
                                question.setOptions(optionMap.toJSONString());
                                continue;
                            }
                        }
                        //判断题
                        if (type == 3) {
                            JSONObject parmMap = new JSONObject();
                            if (auswer.equals("错")) {
                                auswer = "false";
                                parmMap.put("text", "false");
                                parmMap.put("url", new String[0]);
                            } else if (auswer.equals("对")) {
                                auswer = "true";
                                parmMap.put("text", "true");
                                parmMap.put("url", new String[0]);
                            }
                            question.setAnswer(parmMap.toJSONString());
                            question.setOptions("{}");
                        }
                        //多选
                        if (type == 2) {
                            if (auswer.contains("，") || auswer.contains("、")) {
                                auswer = auswer.replaceAll("，", ",").replaceAll("、", ",");
                            } else {
                                if (auswer.length() > 1) {
                                    StringBuilder ss = new StringBuilder();
                                    for (int y = 0; y < auswer.length(); y++) {
                                        if (y == (auswer.length() - 1)) {
                                            ss.append(auswer.charAt(y));
                                        } else {
                                            ss.append(auswer.charAt(y)).append(",");
                                        }
                                    }
                                    auswer = ss.toString();
                                }
                            }
                            objectMap.put("text", fullWidth2half(auswer));
                            objectMap.put("url", images);
                            question.setAnswer(objectMap.toJSONString());

                        }
                        //单选
                        if (type == 1) {
                            auswer = fullWidth2half(auswer);
                        }
                        if (type != 2 && type != 3 && type != 4 && type != 6 && type != -1) {
                            objectMap.put("text", auswer);
                            objectMap.put("url", images);
                            question.setAnswer(objectMap.toJSONString());
                        }
                    }

                }


                //知识点
                if (textCon.contains("【知识点】")) {
                    kpIFlag = true;
                    String kpstr = textCon.replaceAll("【知识点】", "").trim();
                    //试题可能关联多个知识点
                    String[] kpStrArray = kpstr.split("[,，、]");
                    List<String> kpList = Arrays.stream(kpStrArray).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(kpList) && kpList.stream().allMatch(knowledgePoints::containsKey)) {
                        List<String> kpIdList = kpList.stream().map(knowledgePoints::get).collect(Collectors.toList());
                        question.setKpIds(kpIdList);
                        //question.setKpId(kpid);
                        //根据试题题干 题型  知识点 判断是否同样的试题是否存在
                        if (ObjectUtils.isNotNull(questionContextList)) {
                            //和当前题干一样的数据库所有题
                            List<QuestionManage> compareQuestionlist = questionContextList.get(questionContextText);
                            Integer type = question.getType();
                            if (ObjectUtils.isNotNull(compareQuestionlist)) {
                                //题干重复 判断题型 和 知识点
                                //查询具有同样题型的题
                                List<QuestionManage> sameTypeQuestionList = compareQuestionlist.stream().filter(e -> type.equals(e.getType()))
                                        .collect(Collectors.toList());
                                List<QuestionManage> repeat = new ArrayList<>();
                                if (CollectionUtils.isNotEmpty(sameTypeQuestionList)) {
                                    //判断知识点是否重复
                                    for (QuestionManage sameTypeQuestion : sameTypeQuestionList) {
                                        List<String> sameTypeQuestionKpIds = sameTypeQuestion.getKpIds();
                                        if (CollectionUtils.isNotEmpty(sameTypeQuestionKpIds)
                                                && CollectionUtils.isNotEmpty(kpIdList)
                                                && CollectionUtils.isEqualCollection(kpIdList, sameTypeQuestionKpIds)) {
                                            repeat.add(sameTypeQuestion);
                                        }
                                    }
                                }

                                if (repeat.size() > 0) {
                                    JSONObject dataMap = new JSONObject();
                                    dataMap.put("kpId", kpIdList);
                                    dataMap.put("kpName", kpstr);
                                    List<Long> repeatIdList = repeat.stream().map(QuestionManage::getId).collect(Collectors.toList());
                                    //重复的id
                                    dataMap.put("idList", repeatIdList);
                                    question.setRepeatData(dataMap.toJSONString());
                                    question.setErrorText("试题导入重复");
                                    question.setErrorType(3L);
                                    commentsDocument.insertCommentToParagraph(questionContextParagraph.getXwpfParagraph(), "试题导入重复");
                                    questionErrorFlag = true;
                                }
                            }
                        }
                    } else {
                        if (StringUtils.isEmpty(kpstr)) {
                            question.setErrorText("知识点不能为空");
                            question.setErrorType(1L);
                            commentsDocument.insertCommentToParagraph(paragraph.getXwpfParagraph(), "知识点不能为空");
                            questionErrorFlag = true;
                        } else {
                            question.setErrorText("包含有新的知识点");
                            question.setErrorType(2L);
                            commentsDocument.insertCommentToParagraph(paragraph.getXwpfParagraph(), "包含有新的知识点");
                            questionErrorFlag = true;
                        }
                        question.setKpIds(Collections.emptyList());
                    }
                    question.setKpName(kpstr);


                }
                //试题难度
                if (textCon.contains("【难度】")) {
                    String difficultyStr = textCon.replaceAll("【难度】", "").trim();
                    double difficuty = convertDifficuty(difficultyStr);
                    question.setDifficulty(difficuty);
                }

                // 试题判断类型
                if (textCon.contains("【判读类型】")) {
                    String pdType = textCon.replaceAll("【判读类型】", "").trim();
                    if (StringUtils.isEmpty(pdType)) {
                        question.setErrorText("判读类型不能为空");
                        question.setErrorType(4L);
                        commentsDocument.insertCommentToParagraph(paragraph.getXwpfParagraph(), "判读类型不能为空");
                        questionErrorFlag = true;
                    } else {
                        question.setPdType(pdType);
                    }
                }

                if (textCon.contains("【解析】")) {
                    objectMap = new JSONObject();
                    objectMap.put("text", textCon.replaceAll("【解析】", "").trim());
                    objectMap.put("url", images);
                    question.setAnalysis(objectMap.toJSONString());
                }


                //问答题答案
                if (!textCon.contains("【知识点】") &&
                        !textCon.contains("【难度】") &&
                        !textCon.contains("【解析】") &&
                        !textCon.contains("【用途】") &&
                        StringUtils.isNotEmpty(question.getType() + "")
                        && Integer.parseInt(question.getType() + "") == 6 && questionAuswerFlag) {
                    String auswer = textCon.replaceAll("【答案】", "").trim();
                    if (StringUtils.isNotEmpty(auswer)) {
                        if (StringUtils.isNotEmpty(question.getAnswer())) {
                            JSONObject optiondata = JSONObject.parseObject(question.getAnswer());
                            String text = optiondata.getString("text");
                            if (StringUtils.isNotEmpty(text)) {
                                optiondata.put("text", text + "\n" + auswer);
                            } else {
                                if (StringUtils.isNotEmpty(auswer)) {
                                    optiondata.put("text", auswer + "\n");
                                }
                            }
                            question.setAnswer(optiondata.toJSONString());

                        } else {
                            objectMap.put("text", auswer);
                            objectMap.put("url", images);
                            question.setAnswer(objectMap.toJSONString());
                        }
                    }
                }

                int num = i + 1;
                if (i != 0 && num < wordParagraphHandledList.size() && wordParagraphHandledList.get(num).getParagraphText().contains("题】")) {
                    listData(questionErrorFlag, question, kpIFlag, successList, errorList);
                    questionAuswerFlag = false;
                    questionErrorFlag = false;
                    //试题的题干信息
                    questionContext = "";
                    questionContextText = "";
                    kpIFlag = false;
                    question = new QuestionManage();
                    continue;
                }
                //最后一行
                if (i + 1 == wordParagraphHandledList.size()) {
                    listData(questionErrorFlag, question, kpIFlag, successList, errorList);
                }
            }
        }
        //关闭文件
        close(inputStream);
        if (CollectionUtils.isNotEmpty(errorList)) {
            String realFileName = originalFilename + "-已标注错误.docx";
            String fileUrl = uploadWord(doc, realFileName);
            doc.close();
            return fileUrl;
        } else {
            doc.close();
            return null;
        }
    }

    public static double convertDifficuty(String difficultyStr) {
        switch (difficultyStr) {
            case "简单":
                return 0.2;
            case "一般":
                return 0.4;
            case "中等":
                return 0.6;
            case "复杂":
                return 0.8;
            case "困难":
                return 0.9;
        }
        return 0.3;
    }

    public static String fullWidth2half(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean isFullWidth = c >= '\uFF00' && c <= '\uFFEF';
            if (isFullWidth) {
                c = (char) (c - 65248);
            }
            sb.append(c);
        }
        return sb.toString();
    }
//    public static Map<String, List<Question>> importWord(InputStream inputStream, Map<String, Long> KnowledgePoints, Map<String, List<Question>> questionContextList) throws IOException {
//        Gson gson = new Gson();
//
//        XWPFDocument doc = null;
//        //问答题是否过答案了
//        boolean questionAuswerFlag = false;
//        //验证试题是否有错
//        boolean questionErrorFlag = false;
//
//        //是否走知识点了
//        boolean kpIFlag = false;
//        try {
//            doc = new XWPFDocument(inputStream);
//        } catch (IOException e) {
//            logger.error("load word file error", e);
//            return null;
//        }
//        //关闭文件
//        close(inputStream);
//        List<WordParagraph> wordParagraphList = new ArrayList<>();
//        WordParagraph wordParagraph = null;
//        List<QuestionManage> successList = new ArrayList<>();
//        List<QuestionManage> errorList = new ArrayList<>();
//        Map<String, List<Question>> resultMap = new HashMap<>();
//        Question question = null;
//        //读取文件保存信息
//        if (doc.getParagraphs() != null) {
//            for (XWPFParagraph xwpfParagraph : doc.getParagraphs()) {
//                wordParagraph = new WordParagraph();
//
//                if (StringUtils.isNotEmpty(xwpfParagraph.getParagraphText())) {
//                    wordParagraph.setParagraphText(xwpfParagraph.getParagraphText());
//                }
//                //文章的图片上传（word上传fdfs）
//                String[] imgs = readImageInfo(xwpfParagraph);
//
//                if (imgs != null) {
//                    wordParagraph.setParagraphImages(imgs);
//                }
//                if (wordParagraph != null && (StringUtils.isNotEmpty(wordParagraph.getParagraphText()) || wordParagraph.getParagraphImages() != null)) {
//                    wordParagraphList.add(wordParagraph);
//                }
//            }
//        }
//
//        List<WordParagraph> wordParagraphList1 = new ArrayList<>();
//
//        Map<String, Object> objectMap = null;
//        if (wordParagraphList != null) {
//            //文字与图片的关系绑定
//            for (int i = 0; i < wordParagraphList.size(); i++) {
//                if ((i + 1) < wordParagraphList.size() && wordParagraphList.get(i + 1).getParagraphImages() != null &&
//                        StringUtils.isEmpty(wordParagraphList.get(i + 1).getParagraphText())) {
//                    wordParagraphList.get(i).setParagraphImages(wordParagraphList.get(i + 1).getParagraphImages());
//                    wordParagraphList1.add(wordParagraphList.get(i));
//                    i++;
//                } else {
//                    wordParagraphList1.add(wordParagraphList.get(i));
//                }
//
//            }
//            //试题的题干
//            String questionContext = "";
//            for (int i = 0; i < wordParagraphList1.size(); i++) {
//                String[] images = wordParagraphList1.get(i).getParagraphImages();
//
//                if (ObjectUtils.isNull(images)) {
//                    images = new String[0];
//                }
//                String textCon = "text";
//                if (StringUtils.isNotEmpty(wordParagraphList1.get(i).getParagraphText())) {
//                    textCon = wordParagraphList1.get(i).getParagraphText().trim();
//                }
//                if (i == 0) {
//                    question = new Question();
//                }
//                objectMap = new HashMap<>();
//                if (textCon.contains("题】")) {
//                    String bracket = textCon.substring(textCon.indexOf("【"), textCon.indexOf("】") + 1);
//                    if (StringUtils.isNotEmpty(bracket)) {
//                        int type = queryQuestionType(bracket);
//                        //试题类型
//                        question.setType(type);
//                    }
//
//                    //试题的题干
//                    String text = textCon.replaceAll(bracket, "");
//                    //题干的括号空格处理
//                    text = text.replaceAll("\\s+", " ");
//
//                    objectMap.put("text", text);
//                    objectMap.put("url", images);
//                    questionContext = gson.toJson(objectMap);
//                    //试题题目
//                    question.setQuestion(questionContext);
//                    if (StringUtils.isBlank(text)) {
//                        questionErrorFlag = true;
//                        question.setErrorText("试题题干为空");
//                        question.setErrorType(5L);
//                    }
//                }
//                //选项  正则验证
//                if (getStrings(textCon)) {
//                    Map<String, Object> parmMap = new HashMap<>();
//                    String text = getMatcherResult(textCon);
//                    parmMap.put("text", text);
//                    parmMap.put("url", images);
//                    if (StringUtils.isNotEmpty(question.getOptions())) {
//                        Map<String, Object> optiondata = new Gson().fromJson(question.getOptions(), Map.class);
//                        optiondata.put(textCon.split("、")[0], parmMap);
//                        question.setOptions(gson.toJson(optiondata));
//                    } else {
//                        objectMap.put(textCon.split("、")[0], parmMap);
//                        question.setOptions(gson.toJson(objectMap));
//                    }
//                }
//                //答案
//                if (textCon.contains("【答案】")) {
//                    //问答题答案处理；
//                    if (StringUtils.isNotEmpty(question.getType() + "") && Integer.parseInt(question.getType() + "") == 6) {
//                        questionAuswerFlag = true;
//                        //continue;
//                    }
//
//                    objectMap = new LinkedHashMap<>();
//                    Map<String, String> optionMap = new LinkedHashMap<>();
//
//                    String auswer = textCon.replaceAll("【答案】", "").trim();
//                    if (StringUtils.isBlank(auswer)) {
//                        JSONObject jsonObject = new JSONObject();
//                        if (Integer.parseInt(question.getType() + "") == 4) {
//                            //填空题
//                            jsonObject.put("edit1", "");
//                        } else {
//                            jsonObject.put("text", "");
//                            jsonObject.put("url", new String[0]);
//                        }
//                        question.setErrorText("试题答案为空");
//                        question.setErrorType(4L);
//                        question.setAnswer(jsonObject.toJSONString());
//                        questionErrorFlag = true;
//                    } else {
//                        //填空题
//                        if (StringUtils.isNotEmpty(question.getType() + "") && Integer.parseInt(question.getType() + "") == 4) {
//                            String[] auswers = auswer.split("；");
//                            if (StringUtils.isNotEmpty(auswers)) {
//                                for (int j = 0; j < auswers.length; j++) {
//                                    objectMap.put("edit" + (j + 1), auswers[j]);
//                                    optionMap.put("edit" + (j + 1), "");
//                                }
//                                question.setAnswer(gson.toJson(objectMap));
//                                question.setOptions(gson.toJson(optionMap));
//                                continue;
//                            }
//                        }
//                        //判断题
//                        if (StringUtils.isNotEmpty(question.getType() + "") && Integer.parseInt(question.getType() + "") == 3) {
//                            Map<String, Object> parmMap = new HashMap<>();
//                            if (auswer.equals("错")) {
//                                auswer = "false";
//                                parmMap.put("text", "false");
//                                parmMap.put("url", new String[0]);
//                            } else if (auswer.equals("对")) {
//                                auswer = "true";
//                                parmMap.put("text", "true");
//                                parmMap.put("url", new String[0]);
//                            }
//                            question.setAnswer(gson.toJson(parmMap));
//                            question.setOptions("{}");
//                        }
//
//                        if (StringUtils.isNotEmpty(question.getType() + "") && Integer.parseInt(question.getType() + "") == 2) {
//                            if (auswer.contains("，") || auswer.contains("、")) {
//                                auswer.replaceAll("，", ",").replaceAll("、", ",");
//                            } else {
//                                if (auswer.length() > 1) {
//                                    String ss = "";
//                                    for (int y = 0; y < auswer.length(); y++) {
//                                        if (y == (auswer.length() - 1)) {
//                                            ss += auswer.charAt(y);
//                                        } else {
//                                            ss += auswer.charAt(y) + ",";
//                                        }
//                                    }
//                                    auswer = ss;
//                                }
//                            }
//                            objectMap.put("text", auswer);
//                            objectMap.put("url", images);
//                            question.setAnswer(gson.toJson(objectMap));
//
//                        }
//                        if (StringUtils.isNotEmpty(question.getType() + "")) {
//                            int type = Integer.parseInt(question.getType() + "");
//                            if (type != 2 && type != 3 && type != 4 && type != 6) {
//                                objectMap.put("text", auswer);
//                                objectMap.put("url", images);
//                                question.setAnswer(gson.toJson(objectMap));
//                            }
//                        }
//                    }
//
//                }
//
//
//                //知识点
//                if (textCon.contains("【知识点】")) {
//                    kpIFlag = true;
//                    String kpstr = textCon.replaceAll("【知识点】", "").trim();
//                    if (StringUtils.isNotEmpty(kpstr) && KnowledgePoints.get(kpstr) != null && StringUtils.isNotEmpty(KnowledgePoints.get(kpstr) + "")) {
//                        //修改多知识点的关联
//                        List<Long> set = new ArrayList<>();
//                        String[] split = kpstr.split(",");
//                        for (int j = 0; j < split.length; j++) {
//                            Long kpid = Long.parseLong(KnowledgePoints.get(split[j]) + "");
//                            set.add(kpid);
//                        }
//                        question.setKpIds(set);
//                        //question.setKpId(kpid);
//                        //根据试题题干 题型  知识点 判断是否同样的试题是否存在
//                        if (ObjectUtils.isNotNull(questionContextList) && ObjectUtils.isNotNull(questionContextList.get(questionContext))) {
//                            int type = question.getType();
//                            List<Long> kpIds1 = question.getKpIds();
//                            Collections.sort(kpIds1);
//                            if (ObjectUtils.isNotNull(questionContextList.get(questionContext))) {
//                                //题干重复 判断题型 和 知识点
//
//                                //和当前题干一样的数据库所有题
//                                List<QuestionManage> questionList = questionContextList.get(questionContext);
//                                //查询具有同样题型的题
//                                List<QuestionManage> collect = questionList.stream().filter(e -> {
//                                    Integer type_e = e.getType();
//                                    if (Objects.nonNull(type_e)) {
//                                        return type_e == type;
//                                    }
//                                    return false;
//                                }).collect(Collectors.toList());
//                                List<QuestionManage> ll = new ArrayList<>();
//                                if (CollectionUtils.isNotEmpty(collect)) {
//                                    //判断知识点是否重复
//                                    for (Question q : collect) {
//                                        List<Long> kpIds = q.getKpIds();
//                                        Collections.sort(kpIds);
//                                        if (kpIds.toString().equals(kpIds1.toString())) {
//                                            ll.add(q);
//                                        }
//                                        ;
//                                    }
//                                }
//
//                                if (ll.size() > 0) {
//                                    Map<String, Object> dataMap = new HashMap<>();
//                                    List<Long> idList = new ArrayList();
//                                    dataMap.put("kpId", questionList.get(0).getKpId());
//                                    dataMap.put("kpName", questionList.get(0).getKpName());
//                                    ll.forEach(q -> {
//                                        idList.add(q.getId());
//                                    });
//                                    //重复的id
//                                    dataMap.put("idList", idList);
//                                    question.setRepeatData(gson.toJson(dataMap));
//                                    question.setErrorText("试题导入重复");
//                                    question.setErrorType(3L);
//                                    questionErrorFlag = true;
//                                }
//                            }
//                        }
//                    } else {
//                        if (StringUtils.isEmpty(kpstr)) {
//                            question.setErrorText("知识点不能为空");
//                            question.setErrorType(1L);
//                            questionErrorFlag = true;
//                        } else {
//                            question.setErrorText("新的知识点");
//                            question.setErrorType(2L);
//                            questionErrorFlag = true;
//                        }
//                        question.setKpIds(Collections.EMPTY_LIST);
//                    }
//                    question.setKpName(kpstr);
//
//
//                }
//                //试题难度
//                if (textCon.contains("【难度】")) {
//                    String difficultyStr = textCon.replaceAll("【难度】", "").trim();
//                    if (difficultyStr.equals("简单")) {
//                        question.setDifficulty(0.2);
//                    } else if (difficultyStr.equals("一般")) {
//                        question.setDifficulty(0.4);
//                    } else if (difficultyStr.equals("中等")) {
//                        question.setDifficulty(0.6);
//                    } else if (difficultyStr.equals("复杂")) {
//                        question.setDifficulty(0.8);
//                    } else if (difficultyStr.equals("困难")) {
//                        question.setDifficulty(0.9);
//                    }
//                }
//
//                if (textCon.contains("【解析】")) {
//                    objectMap = new HashMap<>();
//                    objectMap.put("text", textCon.replaceAll("【解析】", "").trim());
//                    objectMap.put("url", images);
//                    question.setAnalysis(gson.toJson(objectMap));
//                }
//
//
//                //问答题答案
//                if (!textCon.contains("【知识点】") &&
//                        !textCon.contains("【难度】") &&
//                        !textCon.contains("【解析】") &&
//                        !textCon.contains("【用途】") &&
//                        StringUtils.isNotEmpty(question.getType() + "")
//                        && Integer.parseInt(question.getType() + "") == 6 && questionAuswerFlag) {
//                    String auswer = textCon.replaceAll("【答案】", "").trim();
//                    if (StringUtils.isNotEmpty(auswer)) {
//                        if (StringUtils.isNotEmpty(question.getAnswer())) {
//                            Map<String, Object> optiondata = new Gson().fromJson(question.getAnswer(), Map.class);
//                            if (StringUtils.isNotEmpty(optiondata.get("text") + "")) {
//                                optiondata.put("text", optiondata.get("text") + "\n" + auswer);
//                            } else {
//                                if (StringUtils.isNotEmpty(auswer)) {
//                                    optiondata.put("text", auswer + "\n");
//                                }
//                            }
//                            question.setAnswer(gson.toJson(optiondata));
//
//                        } else {
//                            objectMap.put("text", auswer);
//                            objectMap.put("url", images);
//                            question.setAnswer(gson.toJson(objectMap));
//                        }
//                    }
//                }
//
//                int num = i + 1;
//                if (i != 0 && num < wordParagraphList1.size() && wordParagraphList1.get(num).getParagraphText().contains("题】")) {
//                    listData(questionErrorFlag, question, kpIFlag, successList, errorList);
//                    questionAuswerFlag = false;
//                    questionErrorFlag = false;
//                    //试题的题干信息
//                    questionContext = "";
//                    kpIFlag = false;
//                    question = new Question();
//                    continue;
//                }
//                //最后一行
//                if (i + 1 == wordParagraphList1.size()) {
//                    listData(questionErrorFlag, question, kpIFlag, successList, errorList);
//                }
//            }
//        }
//
//        resultMap.put("success", successList);
//        resultMap.put("error", errorList);
//        return resultMap;
//    }

    public static void listData(boolean questionErrorFlag, QuestionManage question, boolean kpIFlag, List<QuestionManage> successList, List<QuestionManage> errorList) {
        if (questionErrorFlag) {
            errorList.add(question);
            return;
        }
        if (!kpIFlag) {
            //没有通过知识点标识  没有【知识点】的标签
            question.setErrorText("知识点不能为空");
            question.setErrorType(1L);
            errorList.add(question);
            return;
        }
        //没有通过答案标识  没有答案
        if (StringUtils.isEmpty(question.getAnswer())) {
            question.setErrorText("答案不能为空");
            question.setErrorType(4L);
            errorList.add(question);
            return;
        }
        //成功
        successList.add(question);
    }

    /**
     * 关闭输入流
     *
     * @param is 输入流
     */
    private static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                logger.error("流关闭异常", e);
            }
        }
    }


    //匹配答案
    public static boolean getStrings(String str) {
        boolean flag = false;
        if (StringUtils.isEmpty(str)) {
            return flag;
        }
        Pattern p = Pattern.compile("^[A-Z](、)(.*?)$");
        Matcher m = p.matcher(str);
        if (m.find()) {
            flag = true;
        }
        return flag;
    }

    //获取选项后面的字符串
    public static String getMatcherResult(String context) {
        Pattern pattern = Pattern.compile("^[A-Z](、)(.*?)$");
        Matcher matcher = pattern.matcher(context);
        StringBuilder bf = new StringBuilder(64);
        while (matcher.find()) {
            bf.append(matcher.group(2));
        }
        return bf.toString();
    }

    //获取试题类型
    public static int queryQuestionType(String text) {
        switch (text) {
            case "【单选题】":
                return 1;
            case "【多选题】":
                return 2;
            case "【判断题】":
                return 3;
            case "【填空题】":
                return 4;
            case "【简答题】":
                return 5;
            case "【问答题】":
                return 6;
            case "【情析题】":
                return 7;
            case "【实操题】":
                return 8;
        }
        return 66;
    }

    //获取某一个段落中的一个图片
    public static String[] readImageInfo(XWPFParagraph paragraph) {
        String[] data = null;
        //段落中所有XWPFRun
        List<XWPFRun> runList = paragraph.getRuns();
        for (XWPFRun run : runList) {
            List<XWPFPicture> pictures = run.getEmbeddedPictures();
            if (pictures != null) {
                data = new String[pictures.size()];
                for (int i = 0; i < pictures.size(); i++) {
                    byte[] imgByte = pictures.get(i).getPictureData().getData();
                    String fileName = pictures.get(i).getPictureData().getFileName();
                    //int pictureType = pictures.get(i).getPictureData().getPictureType();
                    if (ObjectUtils.isNotNull(imgByte)) {
                        //InputStream inputStream = new ByteArrayInputStream(imgByte);
                        MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, ContentType.APPLICATION_OCTET_STREAM.toString(), imgByte);
                        //MultipartFile multipartFile = new MockMultipartFile(ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
                        data[i] = FileFastdfsUtils.uploadFile(multipartFile);
                    }
                }
            }
        }
        return data;
    }

    /**
     * 将XWPFDocument对象上传到fastdfs
     *
     * @param doc
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String uploadWord(XWPFDocument doc, String fileName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.write(outputStream);
        byte[] bytes = outputStream.toByteArray();
        String newFileName = UUID.randomUUID().toString();
        MockMultipartFile mockMultipartFile = new MockMultipartFile(newFileName, fileName, "application/vnd.ms-word", bytes);
        return FileFastdfsUtils.uploadFile(mockMultipartFile);
    }
}
