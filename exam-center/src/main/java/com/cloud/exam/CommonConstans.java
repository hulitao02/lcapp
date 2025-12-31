package com.cloud.exam;

public class CommonConstans {

    // 知识推荐列表和根据知识名称模糊查询
    public static String getListKnowledgeListPage="/onto-kg-gateway/daoDaJ2LearnTrainService/getListKnowledgeListPage";
    // 查询知识点下的所有知识
    public static String getAllKnowledgeListPoint="/onto-kg-gateway/daoDaJ2LearnTrainService/getAllKnowledgeListPoint";
    // 根据属性code查询属性值
    public static  String getProInfo="/onto-kg-gateway/daoDaJ2LearnTrainService/getProInfo";
    // 根据知识id，关系id，关联概念id查询属性列表
    public static  String getProList="/onto-kg-gateway/daoDaJ2LearnTrainService/getRelationInfo";
    // 获取所有知识数量
    public static String getKnowledgeTotal="/onto-kg-gateway/daoDaJ2LearnTrainService/getKnowledgeTotal";
    // 查询关系里面的知识
    public static String getRelationList="/onto-kg-gateway/daoDaJ2LearnTrainService/getRelationList";
    // 根据知识id查询知识点code
    public static String getKpCode="/onto-kg-gateway/daoDaJ2LearnTrainService/getClassByIndividualId";
    // 根据知识code，属性code修改属性值
    public static String saveProInfo="/onto-kg-gateway/daoDaJ2LearnTrainService/saveProInfo";
    // 智能推荐：个人能力评估阀值参数名称
    public static String eval_score="eval_score";
    // 只能推荐：knowledge_view数据查询天数
    public static String day_num="day_num";
}
