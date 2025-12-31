package com.cloud.exam.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.CommonConstans;
import com.cloud.exam.controller.AutoQuestionController;
import com.cloud.exam.dao.AutoQuestionDao;
import com.cloud.exam.dao.AutoQuestionKpDao;
import com.cloud.exam.dao.QuestionDao;
import com.cloud.exam.dao.QuestionKpRelDao;
import com.cloud.exam.model.exam.AutoQuestion;
import com.cloud.exam.model.exam.AutoQuestionKp;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.model.exam.QuestionKpRel;
import com.cloud.exam.service.AutoQuestionService;
import com.cloud.feign.file.FileClientFeign;
import com.cloud.feign.managebackend.ManageBackendFeign;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.common.KnowledgePoints;
import com.cloud.model.user.LoginAppUser;
import com.cloud.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.reflect.generics.tree.VoidDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;


@Service
@Transactional
public class AutoQuestionServiceImpl extends ServiceImpl<QuestionDao, Question> implements AutoQuestionService {

    private static final Logger logger = LoggerFactory.getLogger(AutoQuestionController.class);
    @Autowired
    QuestionDao questionDao;
    @Autowired
    QuestionKpRelDao questionKpRelDao;
    @Autowired
    ManageBackendFeign manageBackendFeign;
    // 知识图谱访问地址
    @Value(value = "${tupu_server}")
    private String tupuServer;
    @Autowired
    AutoQuestionDao autoQuestionDao;
    @Autowired
    SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    AutoQuestionKpDao autoQuestionKpDao;
    @Autowired
    FileClientFeign fileClientFeign;


    /**
     * @author:胡立涛
     * @description: TODO 单选题 模板一
     * @date: 2022/11/10
     * @param: [map]
     * @return: void
     */
    @Override
    public int dxTypeOne(Map<String, Object> map) {
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        saveAutoQuestionKp(map);
        // 实际出题数
        int realNum = 0;
        // 知识点
        String kpId = map.get("kpId").toString();
        Integer questionNum = Integer.parseInt(map.get("questionNum").toString());
        double difficulty = Double.valueOf(map.get("difficulty").toString());
        for (int i = 0; i < questionNum; i++) {
            Question question = new Question();
            JSONObject object = new JSONObject();
            // 选项 A-D
            Map<String, String> optionMap = new HashMap<>();
            optionMap.put("0", "A");
            optionMap.put("1", "B");
            optionMap.put("2", "C");
            optionMap.put("3", "D");
            // 产生的随机数
            int answerIndex = (int) (Math.random() * 4);
            String answerText = optionMap.get(String.valueOf(answerIndex));
            String knowledgePointId = kpId;
            // 查询知识点下的所有知识
            KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(knowledgePointId);
            String kpCode = knowledgePointsById.getCode();
            List<Map> knowledges = getKnowledges(kpCode);

            // 知识点下的知识数量不够
            if (knowledges == null || (knowledges != null && knowledges.size() < 4)) {
                return -1;
            }
            // 打乱知识的顺序
            Collections.shuffle(knowledges);
            // 选项map
            Map<String, String> optMap = new HashMap<>();
            optMap.put("A", null);
            optMap.put("B", null);
            optMap.put("C", null);
            optMap.put("D", null);
            String answerPic = null;
            for (Map m : knowledges) {
                String knowledgeCode = m.get("id").toString();
                String knowledgeName = m.get("name").toString();
                if (answerPic == null) {
                    // 查
                    // 询该知识下的所有图片
                    // 基础图片code值,图片code,图片缩略图code
//                    String relationCode = "1318c27d-fb22-43a9-9c4a-e525a0613317";
//                    String targetCode = "b340ff82-1de2-4648-92c4-8de374a6d278";
//                    String proCode = "b79cae6f-a30e-470f-8481-97295fe9dc03";
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("kp_id", kpId);
                    AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
                    String relationCode = autoQuestionKp.getRelationCode();
                    String targetCode = autoQuestionKp.getTargetCode();
                    String proCode = autoQuestionKp.getProCode();
                    List<String> pics = getPics(knowledgeCode, relationCode, targetCode, proCode);

                    Collections.shuffle(pics);
                    if (pics != null && pics.size() > 0) {
                        for (String str : pics) {
                            if (str != null && str.split(";")[0] != null) {
                                // 查看该图片是否已经使用
                                queryWrapper = new QueryWrapper();
                                queryWrapper.eq("pic", str.split(";")[0]);
                                queryWrapper.eq("question_type", 1);
                                Integer integer = autoQuestionDao.selectCount(queryWrapper);
                                if (integer == 0) {
                                    answerPic = str.split(";")[0];
                                    optMap.put(answerText, knowledgeName);
                                    break;
                                }
                            }
                        }
                        if (answerPic != null) {
                            continue;
                        }
                    }
                }
                int flg = 0;
                for (String optKey : optMap.keySet()) {
                    if (optMap.get(optKey) == null) {
                        optMap.put(optKey, knowledgeName);
                        break;
                    }
                    flg = optMap.get(optKey) == null ? flg : flg + 1;
                }
                if (flg == 4 && answerPic != null) {
                    break;
                }
            }
            // 图片均已被使用
            if (answerPic == null) {
                return -2;
            }
            //题目解析
            object = new JSONObject();
            object.put("text", "");
            object.put("url", new ArrayList<>());
            question.setAnalysis(JSON.toJSONString(object));
            // 答案
            object = new JSONObject();
            object.put("text", answerText);
            object.put("url", new ArrayList<>());
            question.setAnswer(JSON.toJSONString(object));
            // 难度
            question.setDifficulty(difficulty);
            // 知识点
            List<String> kpIds = new ArrayList<>();
            kpIds.add(kpId);
            question.setKpIds(kpIds);
            // 选项
            Map<String, Object> optionsMap = new HashMap<>();
            for (String optKey : optMap.keySet()) {
                object = new JSONObject();
                object.put("text", optMap.get(optKey));
                object.put("url", new ArrayList<>());
                optionsMap.put(optKey, object);
            }
            question.setOptions(JSON.toJSONString(optionsMap));
            // 题干 question: "{"text":"自动出题_单选题_下图中驱逐舰是什么型号（)","url":["group1/M00/00/02/wKgKy2L0zmGEFD_nAAAAAAq5vbk299.png"]}"
            object = new JSONObject();
            String text = "下图中" + knowledgePointsById.getPointName() + "是什么型号（）";
            object.put("text", text);
            List<String> fjList = new ArrayList<>();
            fjList.add(fileClientFeign.copyPic(answerPic));
            object.put("url", fjList);
            question.setQuestion(JSON.toJSONString(object));
            // 试题类型
            question.setType(1);
            question.setCreateTime(new Timestamp(System.currentTimeMillis()));
            // 创建人
            question.setCreator(loginAppUser.getId());
            question.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            question.setUse(0);
            question.setStatus(0);
            question.setVersion(1);
            questionDao.insert(question);
            // 保存试题与知识点关系
            QuestionKpRel questionKpRel = new QuestionKpRel();
            questionKpRel.setKpId(kpId);
            questionKpRel.setQuestionId(question.getId());
            questionKpRelDao.insert(questionKpRel);
            // 添加试题中正确答案的图片
            AutoQuestion autoQuestion = new AutoQuestion();
            autoQuestion.setPic(answerPic);
            autoQuestion.setQuestionType(1);
            autoQuestionDao.insert(autoQuestion);
            realNum += 1;
        }
        return realNum;
    }

    /**
     * @author:胡立涛
     * @description: TODO 根据知识点查询知识点下的所有知识
     * @date: 2022/11/9
     * @param: [kpCode]
     * @return: java.util.List<java.util.Map>
     */
    public List<Map> getKnowledges(String kpCode) {
        List<Map> result = null;
        String kpUrl = tupuServer + CommonConstans.getAllKnowledgeListPoint + "?kpCode=" + kpCode;
        logger.info("调用图谱接口，查询知识点下的所有知识：" + kpUrl);
        String esKnowledgeString = getOther(kpUrl);
        if (StringUtils.isNotEmpty(esKnowledgeString)) {
            JSONObject esObject = JSON.parseObject(esKnowledgeString);
            String code = esObject.getString("code");
            if (code.equals("200")) {
                result = (List) esObject.get("result");
            }
        }
        return result;
    }

    public String getOther(String url) {
        tool.PooledHttpClientAdaptor adaptor = new tool.PooledHttpClientAdaptor();
        Map<String, String> headMap = new HashMap<>();
        //知识点下的所有的知识
        return adaptor.doGet(url, headMap, Collections.emptyMap());
    }

    /**
     * @author:胡立涛
     * @description: TODO 查询知识下的所有图片
     * @date: 2022/11/9
     * @param: [knowledgeId, relationCode, targetCode, proCode]
     * @return: java.util.List<java.lang.String>
     */
    public List<String> getPics(String knowledgeId, String relationCode, String targetCode, String proCode) {
        List<String> picList = new ArrayList<>();
        // 根据知识code,关系code和知识code查询关系中的知识实体数据
        String kpUrl = tupuServer + CommonConstans.getRelationList + "?kpCode=" + knowledgeId +
                "&relationCode=" + relationCode + "&targetCode=" + targetCode;
        logger.info("调用图谱接口查询知识下的图片实体信息：" + kpUrl);
        String esKnowledgeString = getOther(kpUrl);
        if (StringUtils.isNotEmpty(esKnowledgeString)) {
            JSONObject esObject = JSON.parseObject(esKnowledgeString);
            String code = esObject.getString("code");
            if (code.equals("200")) {
                JSONObject rObject = JSON.parseObject(esObject.get("result").toString());
                List<Map> result = (List) rObject.get("list");
                for (Map m : result) {
                    String knowledgeCode = m.get("id").toString();
                    String url = tupuServer + CommonConstans.getProInfo + "?proCode=" + proCode + "&sensId=" + knowledgeCode;
                    logger.info("--------根据知识code和属性code查询属性值url" + url);
                    Map<String, Object> otherProValue = getOtherProValue(url);
                    if (otherProValue.get("proProvalue") != null) {
                        picList.add(otherProValue.get("proProvalue").toString());
                    }
                }
            }
        }
        return picList;
    }


    public Map<String, Object> getOtherProValue(String url) {
        String str = getOther(url);
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(str)) {
            JSONObject jsonObject = JSON.parseObject(str);
            String code = jsonObject.getString("code");
            if (code.equals("200")) {
                map = (Map) jsonObject.get("result");
            }
        }
        return map;
    }


    /**
     * @author:胡立涛
     * @description: TODO 单选 模板二
     * @date: 2022/11/11
     * @param: [map]
     * @return: int -1:知识数量不够 -2：图片均已经被使用过 -3：图片数量不够
     */
    @Override
    public int dxTypeTwo(Map<String, Object> map) {
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        saveAutoQuestionKp(map);
        // 实际出题数
        int realNum = 0;
        // 知识点
        String kpId = map.get("kpId").toString();
        Integer questionNum = Integer.parseInt(map.get("questionNum").toString());
        double difficulty = Double.valueOf(map.get("difficulty").toString());
        for (int i = 0; i < questionNum; i++) {
            Question question = new Question();
            JSONObject object = null;
            // 选项 A-D
            Map<String, String> optionMap = new HashMap<>();
            optionMap.put("0", "A");
            optionMap.put("1", "B");
            optionMap.put("2", "C");
            optionMap.put("3", "D");
            // 产生的随机数
            int answerIndex = (int) (Math.random() * 4);
            String answerText = optionMap.get(String.valueOf(answerIndex));
            String knowledgePointId = kpId;
            // 查询知识点下的所有知识
            KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(knowledgePointId);
            String kpCode = knowledgePointsById.getCode();
            List<Map> knowledges = getKnowledges(kpCode);

            // 知识点下的知识数量不够
            if (knowledges == null || (knowledges != null && knowledges.size() < 4)) {
                return -1;
            }
            // 打乱知识的顺序
            Collections.shuffle(knowledges);
            // 选项map
            Map<String, String> optMap = new HashMap<>();
            optMap.put("A", null);
            optMap.put("B", null);
            optMap.put("C", null);
            optMap.put("D", null);
            String answerPic = null;
            String answerKnowledgeCode = null;
            String answerKnowledgeName = null;
            // 循环知识 获取正确答案图片
            for (Map m : knowledges) {
                String knowledgeCode = m.get("id").toString();
                String knowledgeName = m.get("name").toString();
                if (answerPic == null) {
                    // 查询该知识下的所有图片
                    // 基础图片code值,图片code,图片缩略图code
//                    String relationCode = "1318c27d-fb22-43a9-9c4a-e525a0613317";
//                    String targetCode = "b340ff82-1de2-4648-92c4-8de374a6d278";
//                    String proCode = "b79cae6f-a30e-470f-8481-97295fe9dc03";
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("kp_id", kpId);
                    AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
                    String relationCode = autoQuestionKp.getRelationCode();
                    String targetCode = autoQuestionKp.getTargetCode();
                    String proCode = autoQuestionKp.getProCode();
                    List<String> pics = getPics(knowledgeCode, relationCode, targetCode, proCode);
                    Collections.shuffle(pics);
                    if (pics != null && pics.size() > 0) {
                        for (String str : pics) {
                            if (str != null && str.split(";")[0] != null) {
                                // 查看该图片是否已经使用
                                queryWrapper = new QueryWrapper();
                                queryWrapper.eq("pic", str.split(";")[0]);
                                queryWrapper.eq("question_type", 1);
                                Integer integer = autoQuestionDao.selectCount(queryWrapper);
                                if (integer == 0) {
                                    answerPic = str.split(";")[0];
                                    optMap.put(answerText, answerPic);
                                    answerKnowledgeCode = knowledgeCode;
                                    answerKnowledgeName = knowledgeName;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // 图片均已被使用
            if (answerPic == null) {
                return -2;
            }
            // 循环知识获取所有知识图片，除正确答案知识外的图片
            List<String> picList = new ArrayList<>();
            for (Map m : knowledges) {
                String knowledgeCode = m.get("id").toString();
                String knowledgeName = m.get("name").toString();
                if (knowledgeCode == answerKnowledgeCode) {
                    continue;
                }
                // 查询该知识下的所有图片
                // 基础图片code值,图片code,图片缩略图code
//                String relationCode = "1318c27d-fb22-43a9-9c4a-e525a0613317";
//                String targetCode = "b340ff82-1de2-4648-92c4-8de374a6d278";
//                String proCode = "b79cae6f-a30e-470f-8481-97295fe9dc03";
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("kp_id", kpId);
                AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
                String relationCode = autoQuestionKp.getRelationCode();
                String targetCode = autoQuestionKp.getTargetCode();
                String proCode = autoQuestionKp.getProCode();
                List<String> pics = getPics(knowledgeCode, relationCode, targetCode, proCode);
                if (pics != null && pics.size() > 0) {
                    for (String str : pics) {
                        picList.add(str);
                    }
                }
            }
            // 图片数量不够
            if (picList.size() < 3) {
                return -3;
            }
            // 打乱所有图片顺序
            Collections.shuffle(picList);
            for (String str : picList) {
                int flg = 0;
                for (String optKey : optMap.keySet()) {
                    if (optMap.get(optKey) == null) {
                        optMap.put(optKey, str);
                        break;
                    }
                    flg = optMap.get(optKey) == null ? flg : flg + 1;
                }
                if (flg == 4) {
                    break;
                }
            }
            //题目解析
            object = new JSONObject();
            object.put("text", "");
            object.put("url", new ArrayList<>());
            question.setAnalysis(JSON.toJSONString(object));
            // 答案
            object = new JSONObject();
            object.put("text", answerText);
            object.put("url", new ArrayList<>());
            question.setAnswer(JSON.toJSONString(object));
            // 难度
            question.setDifficulty(difficulty);
            // 知识点
            List<String> kpIds = new ArrayList<>();
            kpIds.add(kpId);
            question.setKpIds(kpIds);
            // 选项
            Map<String, Object> optionsMap = new HashMap<>();
            for (String optKey : optMap.keySet()) {
                object = new JSONObject();
                object.put("text", "");
                List<String> fjList = new ArrayList<>();
                fjList.add(fileClientFeign.copyPic(optMap.get(optKey)));
                object.put("url", fjList);
                optionsMap.put(optKey, object);
            }
            question.setOptions(JSON.toJSONString(optionsMap));
            // 题干
            object = new JSONObject();
            String text = "下面哪张图是" + answerKnowledgeName + "（）";
            object.put("text", text);
            object.put("url", new ArrayList<>());
            question.setQuestion(JSON.toJSONString(object));
            // 试题类型
            question.setType(1);
            question.setCreateTime(new Timestamp(System.currentTimeMillis()));
            // 创建人
            question.setCreator(loginAppUser.getId());
            question.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            question.setUse(0);
            question.setStatus(0);
            question.setVersion(1);
            questionDao.insert(question);
            // 保存试题与知识点关系
            QuestionKpRel questionKpRel = new QuestionKpRel();
            questionKpRel.setKpId(kpId);
            questionKpRel.setQuestionId(question.getId());
            questionKpRelDao.insert(questionKpRel);
            // 添加试题中正确答案的图片
            AutoQuestion autoQuestion = new AutoQuestion();
            autoQuestion.setPic(answerPic);
            autoQuestion.setQuestionType(1);
            autoQuestionDao.insert(autoQuestion);
            realNum += 1;
        }
        return realNum;
    }


    /**
     * @author:胡立涛
     * @description: TODO 自动出题 判断题
     * @date: 2022/11/14
     * @param: [map]
     * @return: int
     */
    @Override
    public int pd(Map<String, Object> map) {
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        saveAutoQuestionKp(map);
        // 实际出题数
        int realNum = 0;
        // 知识点
        String kpId = map.get("kpId").toString();
        Integer questionNum = Integer.parseInt(map.get("questionNum").toString());
        double difficulty = Double.valueOf(map.get("difficulty").toString());

        for (int i = 0; i < questionNum; i++) {
            Question question = new Question();
            JSONObject object = null;
            String knowledgePointId = kpId;
            // 查询知识点下的所有知识
            KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(knowledgePointId);
            String kpCode = knowledgePointsById.getCode();
            List<Map> knowledges = getKnowledges(kpCode);

            // 知识点下的知识数量不够
            if (knowledges == null || knowledges.size() == 0) {
                return -1;
            }
            // 随即抽取一个知识
            // 产生的随机数
            int index = (int) (Math.random() * knowledges.size());
            Map tgMap = knowledges.get(index);

            String answerPic = null;
            String answerKnowledgeCode = null;
            String answerKnowledgeName = null;
            // 获取所有知识的图片
            List<String> picList = new ArrayList<>();
            for (Map m : knowledges) {
                String knowledgeCode = m.get("id").toString();
                String knowledgeName = m.get("name").toString();
                // 查询知识下的图片
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("kp_id", kpId);
                AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
                String relationCode = autoQuestionKp.getRelationCode();
                String targetCode = autoQuestionKp.getTargetCode();
                String proCode = autoQuestionKp.getProCode();
                List<String> pics = getPics(knowledgeCode, relationCode, targetCode, proCode);
                if (pics != null && pics.size() > 0) {
                    for (String str : pics) {
                        if (str != null && str.split(";")[0] != null) {
                            // 查看该图片是否已经使用
                            queryWrapper = new QueryWrapper();
                            queryWrapper.eq("pic", str.split(";")[0]);
                            queryWrapper.eq("question_type", 3);
                            Integer integer = autoQuestionDao.selectCount(queryWrapper);
                            if (integer == 0) {
                                answerPic = str.split(";")[0];
                                answerKnowledgeCode = knowledgeCode;
                                answerKnowledgeName = knowledgeName;
                                break;
                            }
                        }
                    }
                }
            }
            if (answerPic == null) {
                break;
            }
            //题目解析
            object = new JSONObject();
            object.put("text", "");
            object.put("url", new ArrayList<>());
            question.setAnalysis(JSON.toJSONString(object));
            // 答案
            object = new JSONObject();
            object.put("text", "false");
            if (answerKnowledgeCode == tgMap.get("id").toString()) {
                object.put("text", "true");
            }
            object.put("url", new ArrayList<>());
            question.setAnswer(JSON.toJSONString(object));
            // 难度
            question.setDifficulty(difficulty);
            // 知识点
            List<String> kpIds = new ArrayList<>();
            kpIds.add(kpId);
            question.setKpIds(kpIds);
            // 选项
            question.setOptions(JSON.toJSONString(new Object()));
            // 题干
            object = new JSONObject();
            String text = "下图中目标是" + tgMap.get("name").toString() + "（）";
            object.put("text", text);
            List<String> fjList = new ArrayList<>();
            fjList.add(fileClientFeign.copyPic(answerPic));
            object.put("url", fjList);
            question.setQuestion(JSON.toJSONString(object));
            // 试题类型
            question.setType(3);
            question.setCreateTime(new Timestamp(System.currentTimeMillis()));
            // 创建人
            question.setCreator(loginAppUser.getId());
            question.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            question.setUse(0);
            question.setStatus(0);
            question.setVersion(1);
            questionDao.insert(question);
            // 保存试题与知识点关系
            QuestionKpRel questionKpRel = new QuestionKpRel();
            questionKpRel.setKpId(kpId);
            questionKpRel.setQuestionId(question.getId());
            questionKpRelDao.insert(questionKpRel);
            // 添加试题中正确答案的图片
            AutoQuestion autoQuestion = new AutoQuestion();
            autoQuestion.setPic(answerPic);
            autoQuestion.setQuestionType(3);
            autoQuestionDao.insert(autoQuestion);
            realNum += 1;
        }
        return realNum;
    }

    /**
     * @author:胡立涛
     * @description: TODO 自动出题 填空题
     * @date: 2022/11/14
     * @param: [map]
     * @return: int
     */
    @Override
    public int tk(Map<String, Object> map) {
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        saveAutoQuestionKp(map);
        // 实际出题数
        int realNum = 0;
        // 知识点
        String kpId = map.get("kpId").toString();
        Integer questionNum = Integer.parseInt(map.get("questionNum").toString());
        double difficulty = Double.valueOf(map.get("difficulty").toString());
        Question question = new Question();
        JSONObject object = null;
        String knowledgePointId = kpId;
        // 查询知识点下的所有知识
        KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(knowledgePointId);
        String kpCode = knowledgePointsById.getCode();
        List<Map> knowledges = getKnowledges(kpCode);

        // 知识点下的知识数量不够
        if (knowledges == null || knowledges.size() == 0) {
            return -1;
        }
        for (int i = 0; i < questionNum; i++) {
            String answerPic = null;
            String answerKnowledgeName = null;
            // 打乱知识顺序
            Collections.shuffle(knowledges);
            for (Map m : knowledges) {
                String knowledgeCode = m.get("id").toString();
                String knowledgeName = m.get("name").toString();
                if (answerPic == null) {
                    // 查询该知识下的所有图片
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("kp_id", kpId);
                    AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
                    String relationCode = autoQuestionKp.getRelationCode();
                    String targetCode = autoQuestionKp.getTargetCode();
                    String proCode = autoQuestionKp.getProCode();
                    List<String> pics = getPics(knowledgeCode, relationCode, targetCode, proCode);
                    if (pics != null && pics.size() > 0) {
                        for (String str : pics) {
                            if (str != null && str.split(";")[0] != null) {
                                // 查看该图片是否已经使用
                                queryWrapper = new QueryWrapper();
                                queryWrapper.eq("pic", str.split(";")[0]);
                                queryWrapper.eq("question_type", 4);
                                Integer integer = autoQuestionDao.selectCount(queryWrapper);
                                if (integer == 0) {
                                    answerPic = str.split(";")[0];
                                    answerKnowledgeName = knowledgeName;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (answerPic == null) {
                break;
            }
            // 答案
            object = new JSONObject();
            object.put("edit1", answerKnowledgeName);
            question.setAnswer(JSON.toJSONString(object));
            //题目解析
            object = new JSONObject();
            object.put("text", "");
            object.put("url", new ArrayList<>());
            question.setAnalysis(JSON.toJSONString(object));
            // 难度
            question.setDifficulty(difficulty);
            // 知识点
            List<String> kpIds = new ArrayList<>();
            kpIds.add(kpId);
            question.setKpIds(kpIds);
            // 选项
            question.setOptions(JSON.toJSONString(new Object()));
            // 题干
            object = new JSONObject();
            String text = "下图中" + knowledgePointsById.getPointName() + "是什么型号（）";
            object.put("text", text);
            List<String> fjList = new ArrayList<>();
            fjList.add(fileClientFeign.copyPic(answerPic));
            object.put("url", fjList);
            question.setQuestion(JSON.toJSONString(object));
            // 试题类型
            question.setType(4);
            question.setCreateTime(new Timestamp(System.currentTimeMillis()));
            // 创建人
            question.setCreator(loginAppUser.getId());
            question.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            question.setUse(0);
            question.setStatus(0);
            question.setVersion(1);
            questionDao.insert(question);
            // 保存试题与知识点关系
            QuestionKpRel questionKpRel = new QuestionKpRel();
            questionKpRel.setKpId(kpId);
            questionKpRel.setQuestionId(question.getId());
            questionKpRelDao.insert(questionKpRel);
            // 添加试题中正确答案的图片
            AutoQuestion autoQuestion = new AutoQuestion();
            autoQuestion.setPic(answerPic);
            autoQuestion.setQuestionType(4);
            autoQuestion.setQuestionId(question.getId());
            autoQuestionDao.insert(autoQuestion);
            realNum += 1;
        }
        return realNum;
    }


    /**
     * @author:胡立涛
     * @description: TODO 自动出题 多选
     * @date: 2022/11/15
     * @param: [map]
     * @return: int
     */
    @Override
    public int duoxuan(Map<String, Object> map) {
        // 创建人
        LoginAppUser loginAppUser = sysDepartmentFeign.getLoginAppUser();
        saveAutoQuestionKp(map);
        // 实际出题数
        int realNum = 0;
        // 知识点
        String kpId = map.get("kpId").toString();
        Integer questionNum = Integer.parseInt(map.get("questionNum").toString());
        double difficulty = Double.valueOf(map.get("difficulty").toString());

        // 查询知识点下的所有知识
        KnowledgePoints knowledgePointsById = manageBackendFeign.getKnowledgePointsById(kpId);
        String kpCode = knowledgePointsById.getCode();
        List<Map> knowledges = getKnowledges(kpCode);
        // 知识点下的知识数量不够
        if (knowledges == null || (knowledges != null && knowledges.size() < 4)) {
            return -1;
        }
        // 每个知识下的图片
        Map<String, List> knowledgePicMap = new HashMap<>();

        // 查询知识下的图片
        for (Map m : knowledges) {
            String knowledgeCode = m.get("id").toString();
            String knowledgeName = m.get("name").toString();
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("kp_id", kpId);
            AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
            String relationCode = autoQuestionKp.getRelationCode();
            String targetCode = autoQuestionKp.getTargetCode();
            String proCode = autoQuestionKp.getProCode();
            List<String> pics = getPics(knowledgeCode, relationCode, targetCode, proCode);
            knowledgePicMap.put(knowledgeCode, pics);
        }
        JSONObject object = null;

        // 选项 A-D
        Map<String, String> optionMap = new HashMap<>();
        List<String> daList = new ArrayList<>();
        daList.add("A");
        daList.add("B");
        daList.add("C");
        daList.add("D");

        Map<String, Integer> answerMap = new HashMap<>();
        answerMap.put("A", 1);
        answerMap.put("B", 2);
        answerMap.put("C", 3);
        answerMap.put("D", 4);

        for (int i = 0; i < questionNum; i++) {
            Question question = new Question();
            // 打乱顺序
            Collections.shuffle(daList);
            String answerOne = daList.get(0);
            String answerTwo = daList.get(1);

            // 选项map
            Map<String, String> optMap = new HashMap<>();
            optMap.put("A", null);
            optMap.put("B", null);
            optMap.put("C", null);
            optMap.put("D", null);
            String answerPicOne = null;
            String answerPicTwo = null;
            String answerKnowledgeName = null;
            String answerKnowledgeCode = null;
            List<String> picAllList = new ArrayList<>();
            // 打乱知识顺序
            Collections.shuffle(knowledges);
            for (Map m : knowledges) {
                answerPicOne = null;
                answerPicTwo = null;
                String knowledgeCode = m.get("id").toString();
                String knowledgeName = m.get("name").toString();
                if (answerKnowledgeCode == null) {
                    while (true) {
                        List<String> piclist = knowledgePicMap.get(knowledgeCode);
                        if (piclist != null && piclist.size() >= 2) {
                            Collections.shuffle(piclist);
                            for (String pic : piclist) {
                                if (answerPicOne == null) {
                                    answerPicOne = pic.split(";")[0];
                                } else {
                                    answerPicTwo = answerPicTwo == null ? pic.split(";")[0] : answerPicTwo;
                                }
                                if (answerPicOne != null && answerPicTwo != null) {
                                    answerKnowledgeName = knowledgeName;
                                    answerKnowledgeCode = knowledgeCode;
                                    optMap.put(answerOne, answerPicOne);
                                    optMap.put(answerTwo, answerPicTwo);
                                    break;
                                }
                            }
                            if (answerKnowledgeCode != null) {
                                break;
                            }
                        }
                    }
                }
                // 非答案的知识
                if (answerKnowledgeCode != knowledgeCode) {
                    List<String> piclist = knowledgePicMap.get(knowledgeCode);
                    if (piclist != null && piclist.size() > 0) {
                        for (String pic : piclist) {
                            picAllList.add(pic);
                        }
                    }
                }
            }
            // 图片数量不够
            if (picAllList.size() < 2) {
                continue;
            }
            Collections.shuffle(picAllList);

            for (String str : picAllList) {
                int num = 0;
                for (String key : optMap.keySet()) {
                    num += 1;
                    if (optMap.get(key) == null) {
                        optMap.put(key, str.split(";")[0]);
                        break;
                    }
                }
                if (num == 4) {
                    break;
                }
            }

            //题目解析
            object = new JSONObject();
            object.put("text", "");
            object.put("url", new ArrayList<>());
            question.setAnalysis(JSON.toJSONString(object));
            // 答案
            object = new JSONObject();
            if (answerMap.get(answerOne) < answerMap.get(answerTwo)) {
                object.put("text", answerOne + "," + answerTwo);
            } else {
                object.put("text", answerTwo + "," + answerOne);
            }
            object.put("url", new ArrayList<>());
            question.setAnswer(JSON.toJSONString(object));
            // 难度
            question.setDifficulty(difficulty);
            // 知识点
            List<String> kpIds = new ArrayList<>();
            kpIds.add(kpId);
            question.setKpIds(kpIds);
            // 选项
            Map<String, Object> optionsMap = new HashMap<>();
            for (String optKey : optMap.keySet()) {
                object = new JSONObject();
                object.put("text", "");
                List<String> fjList = new ArrayList<>();
                fjList.add(fileClientFeign.copyPic(optMap.get(optKey)));
                object.put("url", fjList);
                optionsMap.put(optKey, object);
            }
            question.setOptions(JSON.toJSONString(optionsMap));
            // 题干
            object = new JSONObject();
            String text = "下面哪张图是" + answerKnowledgeName + "（）";
            object.put("text", text);
            object.put("url", new ArrayList<>());
            question.setQuestion(JSON.toJSONString(object));
            // 试题类型
            question.setType(2);
            question.setCreateTime(new Timestamp(System.currentTimeMillis()));
            question.setCreator(loginAppUser.getId());
            question.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            question.setUse(0);
            question.setStatus(0);
            question.setVersion(1);
            questionDao.insert(question);
            // 保存试题与知识点关系
            QuestionKpRel questionKpRel = new QuestionKpRel();
            questionKpRel.setKpId(kpId);
            questionKpRel.setQuestionId(question.getId());
            questionKpRelDao.insert(questionKpRel);
            realNum += 1;
        }
        return realNum;
    }


    /**
     * @author:胡立涛
     * @description: TODO 保存关系图片信息
     * @date: 2022/11/15
     * @param: [map]
     * @return: void
     */
    public void saveAutoQuestionKp(Map map) {
        // 关系code
        String relationCode = map.get("relationCode") == null ? null : map.get("relationCode").toString();
        // 关联概念code
        String targetCode = map.get("targetCode") == null ? null : map.get("targetCode").toString();
        // 属性code
        String proCode = map.get("proCode") == null ? null : map.get("proCode").toString();
        Long kpId = Long.valueOf(map.get("kpId").toString());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("kp_id", kpId);
        AutoQuestionKp autoQuestionKp = autoQuestionKpDao.selectOne(queryWrapper);
        if (autoQuestionKp == null) {
            autoQuestionKp = new AutoQuestionKp();
            autoQuestionKp.setKpId(kpId);
            autoQuestionKp.setRelationCode(relationCode);
            autoQuestionKp.setTargetCode(targetCode);
            autoQuestionKp.setProCode(proCode);
            autoQuestionKpDao.insert(autoQuestionKp);
        } else {
            autoQuestionKp.setRelationCode(relationCode);
            autoQuestionKp.setTargetCode(targetCode);
            autoQuestionKp.setProCode(proCode);
            autoQuestionKpDao.updateById(autoQuestionKp);
        }
    }
}
