package com.cloud.exam.utils.word;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;


/**
 * <p>
 * POI不支持插入批注，只能自己写一个扩展
 * </p>
 *
 * @author tongkesong
 * @since 2023-08-24
 */
@Slf4j
public class MyXWPFCommentsDocument extends POIXMLDocumentPart {
    private CTComments comments;
    private BigInteger offset;

    public MyXWPFCommentsDocument(PackagePart part) {
        super(part);
        try {
            comments = CommentsDocument.Factory.parse(part.getInputStream(), DEFAULT_XML_OPTIONS).getComments();
        } catch (Exception ex) {

        }
        if (comments == null) comments = CommentsDocument.Factory.newInstance().addNewComments();
        offset = BigInteger.ZERO;
    }

    public CTComments getComments() {
        return comments;
    }

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTComments.type.getName().getNamespaceURI(), "comments"));
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        comments.save(out, xmlOptions);
        out.close();
    }

    public static MyXWPFCommentsDocument createCommentsDocument(XWPFDocument document) throws Exception {
        MyXWPFCommentsDocument myXWPFCommentsDocument = null;

        //trying to get the CommentsDocument
        for (POIXMLDocumentPart.RelationPart rpart : document.getRelationParts()) {
            String relation = rpart.getRelationship().getRelationshipType();
            if (relation.equals(XWPFRelation.COMMENT.getRelation())) {
                POIXMLDocumentPart part = rpart.getDocumentPart();
                myXWPFCommentsDocument = new MyXWPFCommentsDocument(part.getPackagePart());
                String rId = document.getRelationId(part);
                document.addRelation(rId, XWPFRelation.COMMENT, myXWPFCommentsDocument);
            }
        }

        //create a new CommentsDocument if there is not one already
        if (myXWPFCommentsDocument == null) {
            OPCPackage oPCPackage = document.getPackage();
            PackagePartName partName = PackagingURIHelper.createPartName("/word/comments.xml");
            PackagePart part = oPCPackage.createPart(partName,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.comments+xml");
            myXWPFCommentsDocument = new MyXWPFCommentsDocument(part);
            document.addRelation(null, XWPFRelation.COMMENT, myXWPFCommentsDocument);
        }

        return myXWPFCommentsDocument;
    }

    //a method to get the next comment Id from CTComments
    public BigInteger getCommentId(CTComments comments) {
        BigInteger max = comments.getCommentList().stream().map(CTComment::getId).max(BigInteger::compareTo).orElse(BigInteger.ONE);
        offset = offset.add(BigInteger.ONE);
        return max.add(offset);
    }

    //method to set CommentRangeStart as first element in paragraph
    public CTMarkupRange insertCommentRangeStartAsFirstElement(XWPFParagraph paragraph) {
        String uri = CTMarkupRange.type.getName().getNamespaceURI();
        String localPart = "commentRangeStart";
        XmlCursor cursor = paragraph.getCTP().newCursor();
        cursor.toFirstChild();
        cursor.beginElement(localPart, uri);
        cursor.toParent();
        CTMarkupRange commentRangeStart = (CTMarkupRange) cursor.getObject();
        cursor.dispose();
        return commentRangeStart;
    }

    public void insertCommentToParagraph(XWPFParagraph paragraph, String text) {
        CTComments comments = getComments();
        BigInteger cId = getCommentId(comments);
        CTComment ctComment = comments.addNewComment();
        ctComment.setAuthor("图像判读学训考评系统");
        ctComment.setInitials("AR");
        CTText ctText = ctComment.addNewP().addNewR().addNewT();
        ctText.setStringValue(text);
        ctComment.setId(cId);
        insertCommentRangeStartAsFirstElement(paragraph).setId(cId);
        paragraph.getCTP().addNewCommentRangeEnd().setId(cId);
        paragraph.getCTP().addNewR().addNewCommentReference().setId(cId);
    }
}
