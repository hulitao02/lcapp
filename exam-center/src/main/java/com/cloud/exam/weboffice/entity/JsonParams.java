package com.cloud.exam.weboffice.entity;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

import java.net.URLEncoder;

/**
 * WebOffice参数对象
 * @author 张争洋
 * @date 2020-11-04
 */
@Data
@Builder
public class JsonParams {

    private int method;
    private Params params;

    public JsonParams(int method, Params params) {
        this.params = params;
        this.method = method;
    }

//    private StudentAnswer studentAnswer;

    @SneakyThrows
    @Override
    public String toString() {
        return URLEncoder.encode(JSON.toJSONString(this), "UTF-8");
    }

    @Data
    @Builder
    public static class Params {
        public Params(String userId, String fileId, Integer userRight, String filePath, Boolean saveFlag, String userMenuPermission) {
            this.userId = userId;
            this.userRight = userRight;
            this.filePath = filePath;
            this.saveFlag = saveFlag;
            this.fileId = fileId;
            this.userMenuPermission = userMenuPermission;
        }

        /**
         * userId如果为空则默认值为本机ip地址
         */
        private String userId;
        /**
         * 建议同filePath
         */
        private String fileId;
        /**
         * 0：以编辑模式打开文档 1：以只读模式打开文档 2：以临时只读方式打开文档（可以通过编辑按钮跳转到编辑模式）
         */
        private Integer userRight;
        /**
         * 此路径是相对父路径（配置文件filePath）下的子路径
         */
        private String filePath;
        /**
         * 是否保存文档
         */
        private Boolean saveFlag;

        private String userMenuPermission;

    }
}
