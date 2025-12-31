package com.cloud.model.enums;

public enum ModelKpStatus {
    UNUSABLE(0),    //不可用
    USABLE(1),      //可用
    USED(2),        //已挂载数据
    ;

    private Integer value;

    ModelKpStatus(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
