package util;

import lombok.Data;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.Serializable;

@Data
public class WordParagraph implements Serializable {
    private static final long serialVersionUID = 6020411532824674818L;

    //图片存储
    private String[] paragraphImages;

    //文字
    private String paragraphText;

    private XWPFParagraph xwpfParagraph;
}
