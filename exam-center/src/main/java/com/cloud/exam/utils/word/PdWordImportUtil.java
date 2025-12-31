package com.cloud.exam.utils.word;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.exam.dao.PicImportDao;
import com.cloud.exam.model.exam.PicImport;
import com.cloud.utils.ObjectUtils;
import com.cloud.utils.StringUtils;

import org.apache.http.entity.ContentType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;


public class PdWordImportUtil {
    private static final Logger logger = LoggerFactory.getLogger(PdWordImportUtil.class);

    public static List<Map> importWordAndMark(InputStream inputStream, PicImportDao picImportDao) throws Exception {
        List<Map> rList = new ArrayList<>();
        List<String> picList = new ArrayList<>();
        XWPFDocument doc = new XWPFDocument(inputStream);
        WordParagraph wordParagraph;
        PicImport picImport = null;
        //读取文件保存信息
        if (doc.getParagraphs() != null) {
            for (XWPFParagraph xwpfParagraph : doc.getParagraphs()) {
                wordParagraph = new WordParagraph();
                if (StringUtils.isNotEmpty(xwpfParagraph.getParagraphText())) {
                    String textCon = xwpfParagraph.getParagraphText();
                    System.out.println("-----文本内容：" + textCon);
                    if (textCon.indexOf("【") > -1) {
                        String bracket = textCon.substring(textCon.indexOf("【"), textCon.indexOf("】") + 1);
                        System.out.println("------内容值：" + bracket);
                        String rowContent = textCon.substring(textCon.indexOf("】") + 1).trim();
                        if (bracket.contains("【知识名称】")) {
                            picImport = new PicImport();
                            picImport.setParentKnowledge(rowContent);
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
                Map imageMap = readImageInfo(xwpfParagraph, picImportDao, picImport);
                if (imageMap != null) {
                    rList.add(imageMap);
                }
            }
        }
        //关闭文件
        close(inputStream);
        return rList;
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


    //获取某一个段落中的一个图片
    public static Map readImageInfo(XWPFParagraph paragraph, PicImportDao picImportDao, PicImport picImport) throws Exception {
        String path = "D:\\pic\\";
        String[] data = null;
        Map map = null;
        //段落中所有XWPFRun
        List<XWPFRun> runList = paragraph.getRuns();
        for (XWPFRun run : runList) {
            List<XWPFPicture> pictures = run.getEmbeddedPictures();
            if (pictures != null) {
                data = new String[pictures.size()];
                for (int i = 0; i < pictures.size(); i++) {
                    byte[] imgByte = pictures.get(i).getPictureData().getData();
                    String base64Image = Base64.getEncoder().encodeToString(imgByte);
                    String fileName = UUID.randomUUID().toString().replaceAll("-", "") + pictures.get(i).getPictureData().getFileName();
                    if (ObjectUtils.isNotNull(imgByte)) {
                        MultipartFile multipartFile = new MockMultipartFile("0", "0", ContentType.APPLICATION_OCTET_STREAM.toString(), imgByte);
                        // 查看数据库是否有该图片（根据知识名称，图片内容查询）
                        QueryWrapper<PicImport> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("pic_content", base64Image);
                        queryWrapper.eq("parent_knowledge", picImport.getParentKnowledge());
                        PicImport picImport1 = picImportDao.selectOne(queryWrapper);
                        if (picImport1 != null) {
                            System.out.println("------" + picImport.getParentKnowledge() + "的知识已经有该图片了");
                        } else {
                            File targetFile = new File(path + fileName);
                            if (!targetFile.getParentFile().exists()) {
                                targetFile.getParentFile().mkdirs();
                            }
                            multipartFile.transferTo(targetFile);
                            data[i] = path + fileName;
                            picImport.setCreateTime(new Timestamp(System.currentTimeMillis()));
                            picImport.setPicContent(base64Image);
                            picImport.setPicPath(path + fileName);
                            picImportDao.insert(picImport);
                            map = new HashMap();
                            map.put("knowledgeName", picImport.getParentKnowledge());
                            map.put("picName", picImport.getPicKnowledge());
                            map.put("psjd", picImport.getPsjd());
                            map.put("pswz", picImport.getPswz());
                            map.put("picPath", picImport.getPicPath());
                        }
                    }
                }
            }
        }
        return map;
    }
}
