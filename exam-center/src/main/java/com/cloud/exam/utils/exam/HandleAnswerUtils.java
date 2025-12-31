package com.cloud.exam.utils.exam;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.model.exam.StudentAnswerDetails;
import com.cloud.exam.service.QuestionService;
import com.cloud.exam.service.StudentAnswerService;
import com.cloud.exam.vo.StudentAnswerVO;
import com.cloud.utils.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2022/01/17.
 */
@Component
public class HandleAnswerUtils {

    @Autowired
    private QuestionService qs;
    private static QuestionService questionService;
    @Autowired
    private StudentAnswerService ss;
    private static StudentAnswerService studentAnswerService;
    @PostConstruct
    public void init(){
        questionService = this.qs;
        studentAnswerService = this.ss;
    }

    //返回每道题的实际得分
    public  static double  judgeStudentAnswer(StudentAnswerVO sa,Map<Object,Object> map){

        double score = 0L;
        Question byId = questionService.getById(sa.getQuestionId());
        sa.setQuestion(byId.getQuestion());
        sa.setOptions(byId.getOptions());
        JSONObject js = new JSONObject();
        js.put("text","");
        js.put("url","");
        sa.setKeywords(byId.getKeywords()==null?js.toJSONString():byId.getKeywords());
        sa.setAnalysis(byId.getAnalysis()==null?js.toJSONString():byId.getAnalysis());
        sa.setAnswer(byId.getAnswer()==null?js.toJSONString():byId.getAnswer());
        if(sa.getType()==1 ||   sa.getType()==3){
            JSONObject jsonObject = JSONObject.parseObject(sa.getStuAnswer());
            JSONObject jsonObject1 = JSONObject.parseObject(byId.getAnswer());
            List<StudentAnswerDetails> list = (List<StudentAnswerDetails>)map.get("list");
            StudentAnswerDetails studentAnswerDetails = new StudentAnswerDetails();
            if(sa.getType()==1 ){
                studentAnswerDetails = list.get(0);
                studentAnswerDetails.setQuestionType(1);
            }else{
                studentAnswerDetails = list.get(2);
                studentAnswerDetails.setQuestionType(3);
            }
            Integer totalNumber = studentAnswerDetails.getTotalNumber();
            Integer correctNumber = studentAnswerDetails.getCorrectNumber();
            Integer wrongNumber = studentAnswerDetails.getWrongNumber();
            double totalScore = studentAnswerDetails.getTotalScore();

            totalNumber++;
            if(jsonObject.getString("text").equals(jsonObject1.getString("text"))){
                sa.setFlag(true);
                score = sa.getQuestionScore();
                totalScore+=score;
                correctNumber++;
                studentAnswerDetails.setCorrectNumber(correctNumber);
                studentAnswerDetails.setTotalScore(totalScore);
            }else{
                wrongNumber++;
                studentAnswerDetails.setWrongNumber(wrongNumber);
            }
            studentAnswerDetails.setQuestionScore(sa.getQuestionScore());
            studentAnswerDetails.setTotalNumber(totalNumber);
        }else if(sa.getType()==2){
            List<StudentAnswerDetails> list = (List<StudentAnswerDetails>)map.get("list");
            StudentAnswerDetails studentAnswerDetails = list.get(1);
            studentAnswerDetails.setQuestionType(2);
            Integer totalNumber = studentAnswerDetails.getTotalNumber();
            totalNumber++;
            Integer correctNumber = studentAnswerDetails.getCorrectNumber();
            Integer wrongNumber = studentAnswerDetails.getWrongNumber();
            double totalScore = studentAnswerDetails.getTotalScore();
            JSONObject jsonObject1 = JSONObject.parseObject(sa.getStuAnswer());
            JSONArray o = JSONArray.parseArray(jsonObject1.getString("text"));
            if(!Validator.isEmpty(o)&&o.size()>0){
                String value  = "";
                for(int i=0;i<o.size();i++){
                    if(i>0){
                        value+=",";
                    }
                    value+=o.get(i);
                }
                JSONObject jos = JSONObject.parseObject(byId.getAnswer());
                String text = (String)jos.get("text");
                String[] split = text.split(",");
                List ll = new ArrayList();
                for(int i = 0;i<split.length;i++){
                    ll.add(split[i]);
                }
                Collections.sort(ll);
                String v  = "";
                for(int i=0;i<ll.size();i++){
                    if(i>0){
                        v+=",";
                    }
                    v+=ll.get(i);
                }
                if(value.equals(v)){
                    sa.setFlag(true);
                    score = sa.getQuestionScore();
                    totalScore+=score;
                    correctNumber++;
                    studentAnswerDetails.setCorrectNumber(correctNumber);
                    studentAnswerDetails.setTotalScore(totalScore);
                }else{
                    wrongNumber++;
                    studentAnswerDetails.setWrongNumber(wrongNumber);
                }
                studentAnswerDetails.setQuestionScore(sa.getQuestionScore());
                studentAnswerDetails.setTotalNumber(totalNumber);
            }
        }else if(sa.getType()==4){
            String answer = byId.getAnswer();
            List s = new LinkedList();
            LinkedHashMap<String, String> jsonmap = JSON.parseObject(answer, new TypeReference<LinkedHashMap<String, String>>() {
            });
            for (Map.Entry<String,String> entry:jsonmap.entrySet()) {
                s.add(entry.getValue());
            }
            JSONObject  jso = new JSONObject(true);
            jso.put("text",s.toArray());
            sa.setAnswer(jso.toJSONString());
            String stuAnswer = sa.getStuAnswer();
            JSONObject jsonObject = JSONObject.parseObject(stuAnswer);
            JSONArray text = (JSONArray)jsonObject.get("text");
            sa.setFlag(true);
            score = sa.getQuestionScore();
            if(s.size()!=text.size()){
                sa.setFlag(false);
                score = 0L;
            }else{
                for(int i=0;i<s.size();i++) {
                    if(!s.get(i).equals(text.get(i))){
                        sa.setFlag(false);
                        score = 0L;
                    }
                }
            }
        }
        return score;
    }

    /**
     * "questionId": 3505,
     "  studentAnswer": "{\"text\":[\"B\",\"D\",\"A\",\"C\",\"E\"]}"
     * @param map
     * @return
     * 判断单选多选判断填空考生答案时候正确
     */

    public static boolean judgePerQuestion(Map<String,Object> map){
        boolean flag = true ;
        Object questionId = map.get("questionId");
        Object studentAnswer = map.get("studentAnswer");
        Question question = questionService.getById(Long.valueOf(String.valueOf(questionId)));
        if(ObjectUtil.isEmpty(studentAnswer)){
            flag = false ;
        }
        if(question.getType()==1 ||   question.getType()==3){
            JSONObject jsonObject = JSONObject.parseObject(studentAnswer.toString());
            JSONObject jsonObject1 = JSONObject.parseObject(question.getAnswer());
            if(jsonObject.getString("text").equals(jsonObject1.getString("text"))){
                flag = true ;
            }else{
                flag = false ;
            }
        }else if(question.getType()==2){
            JSONObject jsonObject = JSONObject.parseObject(studentAnswer.toString());
            JSONObject jsonObject1 = JSONObject.parseObject(question.getAnswer());
            JSONArray o = JSONArray.parseArray(jsonObject.getString("text"));
            if(!Validator.isEmpty(o)&&o.size()>0){
                String value  = jsonObject1.getString("text");
                List ll = new ArrayList();
                for(int i = 0;i<o.size();i++){
                    ll.add(o.get(i));
                }
                Collections.sort(ll);
                String v  = "";
                for(int i=0;i<ll.size();i++){
                    if(i>0){
                        v+=",";
                    }
                    v+=ll.get(i);
                }
                if(value.equals(v)){
                    flag = true ;
                }else{
                    flag = false ;
                }

            }
        }else if(question.getType()==4){
            List s = new LinkedList();
            LinkedHashMap<String, String> jsonmap = JSON.parseObject(question.getAnswer(), new TypeReference<LinkedHashMap<String, String>>() {
            });
            for (Map.Entry<String,String> entry:jsonmap.entrySet()) {
                s.add(entry.getValue());
            }
            JSONObject  jso = new JSONObject(true);
            jso.put("text",s.toArray());
            String stuAnswer = studentAnswer.toString();
            JSONObject jsonObject = JSONObject.parseObject(stuAnswer);
            JSONArray text = (JSONArray)jsonObject.get("text");
            if(s.size()!=text.size()){
                flag = false ;
            }else{
                for(int i=0;i<s.size();i++) {
                    if(!s.get(i).equals(text.get(i))){
                        flag = false ;
                    }
                }
            }
        }
        return flag ;
    }

    public static boolean judgeRelationsAnswer(JSONArray sanser , JSONArray qanser){
        try{
            if(sanser.size()*qanser.size()==0 || sanser.size()!=qanser.size()){
                return false ;
            }else {
                Map<String,List<String>> m1 = new HashMap<>();
                Map<String,List<String>> m2 = new HashMap<>();
                for (int i = 0; i < sanser.size(); i++) {
                    List<String> strings = (List<String>)sanser.get(i);
                    if(m1.containsKey(strings.get(0))){
                        m1.get(strings.get(0)).add(strings.get(1));
                    }else {
                        List<String> list = new ArrayList<>();
                        list.add(strings.get(1));
                        m1.put(strings.get(0),list);
                    }
                }

                for (int i = 0; i < qanser.size(); i++) {
                    List<String> strings = (List<String>)qanser.get(i);
                    if(m2.containsKey(strings.get(0))){
                        m2.get(strings.get(0)).add(strings.get(1));
                    }else {
                        List<String> list = new ArrayList<>();
                        list.add(strings.get(1));
                        m2.put(strings.get(0),list);
                    }
                }
                for (Map.Entry<String,List<String>> map:m1.entrySet()) {
                    String key = map.getKey();
                    List<String> value = map.getValue();
                    if(!m2.containsKey(key)){
                        return false ;
                    }else {
                        List<String> list = m2.get(key);
                        if(!value.stream().sorted().collect(Collectors.joining()).equals(list.stream().sorted().collect(Collectors.joining()))){
                            return false ;
                        }
                    }
                }
            }
        }catch (Exception e ){
            e.printStackTrace();
            return false ;
        }
        return true ;
    }
}
