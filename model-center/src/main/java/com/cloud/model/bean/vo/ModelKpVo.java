package com.cloud.model.bean.vo;

import com.cloud.model.model.ModelKp;
import lombok.Data;

@Data
public class ModelKpVo extends ModelKp {
    private static final long serialVersionUID = 6370722745356706790L;

    private String servicePath;

    private String picPath;
}
