package com.cloud.exam.utils.word;


import com.cloud.utils.ObjectUtils;
import com.cloud.utils.StringUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.*;
import java.io.*;
import java.util.Random;

/**
 * Word 转 Pdf 帮助类
 *
 * @author admin
 */
public class WordPdfUtil {
    private static boolean license = false;

    public static void main(String[] args) throws IOException {
//        doc2pdf("C:\\Users\\admin\\Desktop\\doc\\111.docx","C:\\Users\\admin\\Desktop\\doc\\aaa.pdf");
//        doc2pdf("C:\\Users\\admin\\Desktop\\doc\\222.docx","C:\\Users\\admin\\Desktop\\doc\\bbb.pdf");
//        doc2pdf("C:\\Users\\admin\\Desktop\\doc\\333.docx","C:\\Users\\admin\\Desktop\\doc\\ccc.pdf");
        doc2pdf("D:\\workspase\\exam-system\\examFile\\mmm.docx", "D:\\workspase\\exam-system\\examFile\\ccc.pdf");

//        String str="";
//        BASE64Decoder base64Decoder=new BASE64Decoder();
//        byte[] bs=base64Decoder.decodeBuffer(str);


    }

    static {
        try {
            System.setProperty("java.awt.headless","true");
            // license.xml放在src/main/resources文件夹下
            InputStream is = WordPdfUtil.class.getClassLoader().getResourceAsStream("license.xml");
            License aposeLic = new License();
            aposeLic.setLicense(is);
            license = true;
        } catch (Exception e) {
            license = false;
            System.out.println("License验证失败...");
            e.printStackTrace();
        }
    }

    /**
     * doc转pdf
     *
     * @param wordPath
     * @param pdfPath
     */
    public static void doc2pdf(String wordPath, String pdfPath) {
        // 验证License 若不验证则转化出的pdf文档会有水印产生
        if (!license) {
            System.out.println("License验证不通过...");
            return;
        }

        try {
            long old = System.currentTimeMillis();
            //新建一个pdf文档
            File file = new File(pdfPath);
            FileOutputStream os = new FileOutputStream(file);
            //Address是将要被转化的word文档
            Document doc = new Document(wordPath);
            //全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
            doc.save(os, SaveFormat.PDF);
            long now = System.currentTimeMillis();
            os.close();
            //转化用时
            System.out.println("Word 转 Pdf 共耗时：" + ((now - old) / 1000.0) + "秒");
        } catch (Exception e) {
            System.out.println("Word 转 Pdf 失败...");
            e.printStackTrace();
        }
    }


    /**
     * @param doc
     * @param watermarkText
     * @throws Exception
     * @throws
     * @Title: insertWatermarkText
     * @Description: PDF生成水印
     * @author mzl
     */
    private static void insertWatermarkText(Document doc, String watermarkText) throws Exception {
        Shape watermark = new Shape(doc, ShapeType.TEXT_PLAIN_TEXT);
        //水印内容
        watermark.getTextPath().setText(watermarkText);

        //水印字体
        watermark.getTextPath().setFontFamily("宋体");
        //水印宽度
        watermark.setWidth(500);
        //水印高度
        watermark.setHeight(100);
        //旋转水印
        watermark.setRotation(-40);
        //水印颜色
        watermark.getFill().setColor(Color.lightGray);
        watermark.setStrokeColor(Color.lightGray);
        watermark.setRelativeHorizontalPosition(RelativeHorizontalPosition.PAGE);
        watermark.setRelativeVerticalPosition(RelativeVerticalPosition.PAGE);
        watermark.setWrapType(WrapType.NONE);
        watermark.setVerticalAlignment(VerticalAlignment.CENTER);
        watermark.setHorizontalAlignment(HorizontalAlignment.CENTER);
        Paragraph watermarkPara = new Paragraph(doc);
        watermarkPara.appendChild(watermark);
        for (Section sect : doc.getSections()) {
            insertWatermarkIntoHeader(watermarkPara, sect, HeaderFooterType.HEADER_PRIMARY);
            insertWatermarkIntoHeader(watermarkPara, sect, HeaderFooterType.HEADER_FIRST);
            insertWatermarkIntoHeader(watermarkPara, sect, HeaderFooterType.HEADER_EVEN);
        }
        System.out.println("Watermark Set");
    }

    private static void insertWatermarkIntoHeader(Paragraph watermarkPara, Section sect, int headerType) throws Exception {
        HeaderFooter header = sect.getHeadersFooters().getByHeaderFooterType(headerType);
        if (header == null) {
            header = new HeaderFooter(sect.getDocument(), headerType);
            sect.getHeadersFooters().add(header);
        }
        header.appendChild(watermarkPara.deepClone(true));
    }

    /**
     *
     * @param filePath 无水印的pdf
     * @param endFilePath 有水印的pdf
     * @param file  水印图片
     * @throws Exception
     */
    public static void insertWatermarkImage(InputStream inputStream, String endFilePath, InputStream ins,String fileName) throws Exception {

        File toFile=null;
        if (ObjectUtils.isNotNull(ins)&& StringUtils.isNotEmpty(fileName)){
            toFile=new File(fileName);
            OutputStream os=new FileOutputStream(toFile);
            int byteRead=0;
            byte [] buffer=new byte[1024];
            while ((byteRead=ins.read(buffer,0,1024))!=-1){
                os.write(buffer,0,byteRead);
            }
            os.close();
            ins.close();
        }
//
//        InputStream inputStream=new FileInputStream(new File(filePath));
        PDFParser pdfParser=new PDFParser(new RandomAccessBuffer(inputStream));
        pdfParser.parse();
        PDDocument pdDocument=pdfParser.getPDDocument();
        inputStream.close();
        for (int top=0;top<700;top+=300){
            int beginLeft =new Random().ints(-200,150).limit(1).findFirst().getAsInt();
            for (int left=beginLeft;left<700;left+=150){
                for (int i=0;i<pdDocument.getNumberOfPages();i++){
                    PDPage pdPage=pdDocument.getPage(i);
                    PDPageContentStream contentStream=new PDPageContentStream(pdDocument,pdPage,PDPageContentStream.AppendMode.APPEND,true,true);
                    PDImageXObject pdImageXObject=PDImageXObject.createFromFileByExtension(toFile,pdDocument);
                    PDExtendedGraphicsState pdExtendedGraphicsState=new PDExtendedGraphicsState();
                    //设置透明度
                    pdExtendedGraphicsState.setNonStrokingAlphaConstant(0.25f);
                    pdExtendedGraphicsState.setAlphaSourceFlag(true);
                    pdExtendedGraphicsState.getCOSObject().setItem(COSName.BM,COSName.MULTIPLY);
                    contentStream.setGraphicsStateParameters(pdExtendedGraphicsState);
                    float width=100;
                    float height=100;
                    contentStream.drawImage(pdImageXObject,left,top,width,height);
                    contentStream.close();
                    pdDocument.save(endFilePath);
                }
            }
        }
    }
}