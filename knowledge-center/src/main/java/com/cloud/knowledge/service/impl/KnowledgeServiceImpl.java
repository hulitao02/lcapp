package com.cloud.knowledge.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.knowledge.dao.KnowledgeDao;
import com.cloud.knowledge.dao.PicImportDao;
import com.cloud.knowledge.model.PicImport;
import com.cloud.knowledge.service.KnowledgeService;
import com.cloud.utils.ObjectUtils;
import com.cloud.utils.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;
import util.WordParagraph;

import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class KnowledgeServiceImpl extends ServiceImpl<PicImportDao, PicImport>
        implements KnowledgeService {

    @Value("${tupu_pic}")
    private String tupuPic;
    @Autowired
    PicImportDao picImportDao;
    String picId = "";
    List<PicImport> errorList = null;


    @Override
    @Transactional
    public List<PicImport> importWord(MultipartFile multipartFile) throws Exception {
        List<PicImport> imageList = null;
        errorList = new ArrayList<>();
        List<List<PicImport>> importList = new ArrayList<>();
        InputStream inputStream = multipartFile.getInputStream();
        XWPFDocument doc = new XWPFDocument(inputStream);
        WordParagraph wordParagraph;
        PicImport picImport = null;
        //读取文件保存信息
        if (doc.getParagraphs() != null) {
            for (int k = 0; k < doc.getParagraphs().size(); k++) {
                XWPFParagraph xwpfParagraph = doc.getParagraphs().get(k);
                wordParagraph = new WordParagraph();
                if (StringUtils.isNotEmpty(xwpfParagraph.getParagraphText())) {
                    String textCon = xwpfParagraph.getParagraphText();
//                    System.out.println("-----文本内容：" + textCon);
                    if (textCon.indexOf("【") > -1) {
                        String bracket = textCon.substring(textCon.indexOf("【"), textCon.indexOf("】") + 1);
//                        System.out.println("------内容值：" + bracket);
                        String rowContent = textCon.substring(textCon.indexOf("】") + 1).trim();
                        if (bracket.contains("【知识名称】")) {
                            if (imageList != null && imageList.size() > 0) {
                                importList.add(imageList);
                            }
                            picImport = new PicImport();
                            imageList = new ArrayList<>();
                            picImport.setParentKnowledge(rowContent);
                        } else if (bracket.contains("【知识分类】")) {
                            picImport.setKpName(rowContent);
                        } else if (bracket.contains("【图片名称】")) {
                            picImport.setPicKnowledge(rowContent);
                        } else if (bracket.contains("【拍摄位置】")) {
                            picImport.setPswz(rowContent);
                        } else if (bracket.contains("【拍摄角度】")) {
                            picImport.setPsjd(rowContent);
                        }
                    }
                    wordParagraph.setParagraphText(xwpfParagraph.getParagraphText());
                }
                wordParagraph.setXwpfParagraph(xwpfParagraph);
                //文章的图片上传（word上传fdfs）
                if (picImport != null) {
                    PicImport detail = new PicImport();
                    detail.setParentKnowledge(picImport.getParentKnowledge());
                    detail.setKpName(picImport.getKpName());
                    detail.setPicKnowledge(picImport.getPicKnowledge());
                    detail.setPswz(picImport.getPswz());
                    detail.setPsjd(picImport.getPsjd());
                    PicImport picImport1 = readImageInfo(xwpfParagraph, detail);
                    if (picImport1 != null) {
                        imageList.add(picImport1);
                    }
                }
            }
            if (imageList != null && imageList.size() > 0) {
                importList.add(imageList);
            }
        }

        // 图谱处理逻辑
        for (int i = 0; i < importList.size(); i++) {
            List<PicImport> picImports = importList.get(i);
            for (int k = 0; k < picImports.size(); k++) {
                PicImport bean = picImports.get(k);
                int index = k;
                importTupuPic(bean, index);
            }
        }
        return errorList;
    }

    public PicImport readImageInfo(XWPFParagraph paragraph, PicImport picImport) throws Exception {
        String path = tupuPic;
        //段落中所有XWPFRun
        List<XWPFRun> runList = paragraph.getRuns();
        for (XWPFRun run : runList) {
            List<XWPFPicture> pictures = run.getEmbeddedPictures();
            if (pictures != null) {
                for (int i = 0; i < pictures.size(); i++) {
                    byte[] imgByte = pictures.get(i).getPictureData().getData();
//                    String base64Image = Base64.getEncoder().encodeToString(imgByte);
                    String fileName = UUID.randomUUID().toString().replaceAll("-", "") + pictures.get(i).getPictureData().getFileName();
                    if (ObjectUtils.isNotNull(imgByte)) {
                        MultipartFile multipartFile = new MockMultipartFile("0", "0", ContentType.APPLICATION_OCTET_STREAM.toString(), imgByte);
                        // 查看数据库是否有该图片（根据知识名称，图片内容查询）
                        QueryWrapper<PicImport> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("pic_content", imgByte);
                        queryWrapper.eq("parent_knowledge", picImport.getParentKnowledge());
                        List<PicImport> picImports = picImportDao.selectList(queryWrapper);
                        if (picImports != null && picImports.size() > 0) {
                            picImport.setErrorMessage("该图片已存在，请勿重复导入");
                            return picImport;
                        } else {
                            File targetFile = new File(path + fileName);
                            if (!targetFile.getParentFile().exists()) {
                                targetFile.getParentFile().mkdirs();
                            }
                            multipartFile.transferTo(targetFile);
                            picImport.setCreateTime(new Timestamp(System.currentTimeMillis()));
                            picImport.setPicContent(imgByte);
                            picImport.setPicPath(path + fileName);
                            picImport.setPicPathtp("IMAGE" + picImport.getPicPath().split("IMAGE")[1]);
                            return picImport;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Value(value = "${class_id}")
    private String classId;
    @Autowired
    KnowledgeDao knowledgeDao;

    public void importTupuPic(PicImport picImport, int index) {
        try {
            if (picImport.getErrorMessage() != null) {
                errorList.add(picImport);
            } else {
                String knowledgeName = picImport.getParentKnowledge();
                String kpName = picImport.getKpName();
                String picName = picImport.getPicKnowledge() + "_" + new Timestamp(System.currentTimeMillis());
                String pswz = picImport.getPswz();
                String psjd = picImport.getPsjd();
                String picPath = picImport.getPicPathtp();
                Map parMap = null;
                String picKnowledgeId = "";
                if (index != 0) {
                    picKnowledgeId = picId;
                    // 只添加图片实体中图片地址属性值（如：高波级通用驱逐舰01的图片地址）
                    parMap = new HashMap();
                    parMap.put("id", "ontologyindividualdppic" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("picKnowledgeId", picKnowledgeId);
                    parMap.put("picPath", picPath);
                    parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                    knowledgeDao.save_ontology_individual_dp_pic_path(parMap);
                    // 向pic_import表添加数据
                    picImport.setKnowledgeId(picKnowledgeId);
                    picImportDao.insert(picImport);
                } else {
                    // 添加图片知识实体
                    parMap = new HashMap();
                    parMap.put("id", "ontologyindividual" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("picName", picName);
                    parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                    Map result = knowledgeDao.save_ontology_individual(parMap);
                    picKnowledgeId = parMap.get("id").toString();
                    picId = picKnowledgeId;
                    // 向pic_import表添加数据
                    picImport.setKnowledgeId(picKnowledgeId);
                    picImportDao.insert(picImport);
                    // 添加图片与关系之间的关系（如：高波级通用驱逐舰01与图片关系）ontology_class_individual
                    parMap = new HashMap();
                    parMap.put("id", "ontologyclassindividual" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("classId", classId);
                    parMap.put("picKnowledgeId", picKnowledgeId);
                    knowledgeDao.save_ontology_class_individual(parMap);
                    // 添加图片实体中图片名称属性值（如：高波级通用驱逐舰01的图片名称）
                    parMap = new HashMap();
                    parMap.put("id", "ontologyindividualdpname" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("picKnowledgeId", picKnowledgeId);
                    parMap.put("picName", picName);
                    parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                    knowledgeDao.save_ontology_individual_dp_name(parMap);
                    // 添加图片实体中图片地址属性值（如：高波级通用驱逐舰01的图片地址）
                    parMap = new HashMap();
                    parMap.put("id", "ontologyindividualdppic" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("picKnowledgeId", picKnowledgeId);
                    parMap.put("picPath", picPath);
                    parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                    knowledgeDao.save_ontology_individual_dp_pic_path(parMap);
                    // 添加图片实体中图片拍摄位置属性值（如：高波级通用驱逐舰01的图片拍摄位置）
                    parMap = new HashMap();
                    parMap.put("typeId", "pswz");
                    parMap.put("name", pswz);
                    Map sys_dictionary = knowledgeDao.get_sys_dictionary(parMap);
                    if (sys_dictionary == null) {
                        picImport.setPicContent(null);
                        picImport.setPicPathtp(null);
                        picImport.setPicPath(null);
                        errorList.add(picImport);
                        picImport.setErrorMessage("拍摄位置数据不正确，请检查数据正确性。");
                        errorList.add(picImport);
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                    String pswzValue = sys_dictionary.get("code").toString();
                    parMap = new HashMap();
                    parMap.put("id", "ontologyindividualdppswz" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("picKnowledgeId", picKnowledgeId);
                    parMap.put("pswz", pswzValue);
                    parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                    knowledgeDao.save_ontology_individual_dp_pswz(parMap);
                    // 添加图片实体中图片拍摄角度属性值（如：高波级通用驱逐舰01的图片拍摄角度）
                    parMap = new HashMap();
                    parMap.put("typeId", "psjd");
                    parMap.put("name", psjd);
                    Map sys_dictionary1 = knowledgeDao.get_sys_dictionary(parMap);
                    if (sys_dictionary1 == null) {
                        picImport.setPicContent(null);
                        picImport.setPicPathtp(null);
                        picImport.setPicPath(null);
                        errorList.add(picImport);
                        picImport.setErrorMessage("拍摄角度数据不正确，请检查数据正确性。");
                        errorList.add(picImport);
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                    String psjdValue = sys_dictionary1.get("code").toString();
                    parMap = new HashMap();
                    parMap.put("id", "ontologyindividualdppsjd" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("picKnowledgeId", picKnowledgeId);
                    parMap.put("psjd", psjdValue);
                    parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                    knowledgeDao.save_ontology_individual_dp_psjd(parMap);
                    // 根据知识点名称，知识名称查询知识id
                    parMap = new HashMap();
                    parMap.put("kpName", kpName);
                    parMap.put("knowledgeName", knowledgeName);
                    Map ontology_class = knowledgeDao.get_ontology_class(parMap);
                    if (ontology_class == null) {
                        picImport.setPicContent(null);
                        picImport.setPicPathtp(null);
                        picImport.setPicPath(null);
                        errorList.add(picImport);
                        picImport.setErrorMessage("知识名称或知识分类错误，请检查数据正确性。");
                        errorList.add(picImport);
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                    String parentKnowledgeId = ontology_class.get("id").toString();
                    // 添加知识与图片关系（高波级通用驱逐舰-常规图片集合-高波级通用驱逐舰01）
                    parMap = new HashMap();
                    parMap.put("id", "ontologyindividualop" + UUID.randomUUID().toString().replaceAll("-", ""));
                    parMap.put("parentKnowledgeId", parentKnowledgeId);
                    parMap.put("picKnowledgeId", picKnowledgeId);
                    parMap.put("createTime", new Timestamp(System.currentTimeMillis()));
                    knowledgeDao.save_ontology_individual_op(parMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            picImport.setPicContent(null);
            picImport.setPicPathtp(null);
            picImport.setPicPath(null);
            errorList.add(picImport);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }
}
