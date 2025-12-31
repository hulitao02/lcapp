import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.exam.ExamCenterApplication;
import com.cloud.exam.model.exam.Question;
import com.cloud.exam.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;




public class JsonTest {







    public static void main(String[] args) {

        String questionContent = "{\"text\":\"测试地图制图整饰\"," +
                "\"url\":[\"group1/M00/00/2D/wKgKy2GprlGERoowAAAAAAN00OE256.png\",\"group1/M00/00/3C/wKgKy2K8FFyEL0-XAAAAAHaB4z8570.png\"]}";
        JSONObject jsonObject = JSONObject.parseObject(questionContent);

        Object o = jsonObject.get("url");
        if(Objects.isNull(o)){


        }
    }



}
