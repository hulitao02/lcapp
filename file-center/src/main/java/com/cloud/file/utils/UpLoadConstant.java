package com.cloud.file.utils;

public class UpLoadConstant {

    private UpLoadConstant() {
    }

    private  final static  String uploading="Uploading:";
    private final  static  String lock=uploading+"lock:";
    private  final  static String file=uploading+"file:";
    public  final static  String  currLocks=lock+"currLocks:";

    public final static String fileMd5 = uploading+"fileMd5:";

    //当前锁的拥有者
    public  final static  String  lockOwner=lock+"lockOwner:";

    //当前文件传输到第几块(作为断点的标志)
    public final  static  String chunkCurr=file+"chunkCurr:";

    //总的分片数
    public final  static  String chunks=file+"chunks:";

    //当前文件在fastdfs中的文件名
    public final static  String noGroupPath = "group1";


    //分片大小
    public final  static  String chunkSize=file+"chunkSize:";

    //当前分片大小
    public final  static  String currChunkSize=file+"currChunkSize:";

    //当前文件存放的路径
    public final  static  String chunkCurrPath = file+"chunkCurrPath";

    //当前文件上传到fastdfs路径
    public final static String fastDfsPath=file+"fastDfsPath:";

    //当前文件的指针位置
    public final static String splitIndex=file+"splitIndex:";

    //默认分组
    public final static  String DEFAULT_GROUP = "group1";

    //全部上传成功已完成
    public final static String completedList=uploading+"completedList";

    //文件块锁(解决同一个用户正在上传时并发解决,比如后端正在上传一个大文件块,前端页面点击删除按钮,
    // 继续添加删除的文件,这时候要加锁来阻止其上传,否则会出现丢块问题,
    // 因为fastdfs上传不像迅雷下载一样,下载时会创建一个完整的文件,如果上传第一块时,服务器能快速创建一个大文件0填充,那么这样可以支持并发乱序来下载文件块,上传速度会成倍提升,要实现乱序下载文件块,估计就得研究fastdfs源码了)
    public  final static String chunkLock=lock+"chunkLock:";

    public final static  String historyUpload=file+"historyUpload:";


}
