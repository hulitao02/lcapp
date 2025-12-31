package com.cloud.exam.utils.word;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Map;

/**
 * word导入
 */
public class MDoc {
    private Configuration configuration=null;

    public MDoc(){
        configuration=new Configuration();
        configuration.setDefaultEncoding("utf-8");
    }

    public void createDoc(Map<String,Object> dataMap,String fileUrl){
        configuration.setClassForTemplateLoading(this.getClass(),"/static/ftl");
        Template t=null;
        try {
            t=configuration.getTemplate("paper.ftl");
        }catch (IOException e){
            e.printStackTrace();
        }
        File file=new File(fileUrl);
        Writer out=null;
        FileOutputStream fos=null;
        try {
            fos=new FileOutputStream(file);
            OutputStreamWriter oWriter=new OutputStreamWriter(fos,"UTF-8");
            out=new BufferedWriter(oWriter);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try{
            t.process(dataMap,out);
            out.close();
            fos.close();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
