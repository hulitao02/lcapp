package com.cloud.exam.controller.fastdfs;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Objects;



@Slf4j
@RestController
@RefreshScope
public class FileSplitController {


//    Redis
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AppendFileStorageClient appendFileStorageClient;

    int bufferSize = 1024 * 10 ;

    @RequestMapping(value = "uploadFileNew", method = RequestMethod.GET)
    @ResponseBody
    public void uploadLargeFile(@RequestParam("sourcePath") String sourcePath){
        splitFile(sourcePath,bufferSize);
    }


    public void splitFile(String sourceFilePath, long perFileSize) {
        File sourceFile = new File(sourceFilePath);
        if (!sourceFile.exists()) {
            log.warn("文件路径不存在:{}", sourceFile);
            return;
        }
        RandomAccessFile raf = null;
        try {
            //获取目标文件 预分配文件所占的空间 在磁盘中创建一个指定大小的文件   r 是只读
            raf = new RandomAccessFile(new File(sourceFilePath), "r");
            // 默认的文件大小
            long fileTotalLength = raf.length();
            System.out.println("原始文件大小: ["+fileTotalLength+"]");
//          文件块数
            int blocksCount = (int) Math.ceil(fileTotalLength / (double) perFileSize);
//          偏移初始量
            long offSet = 0L;
            for (int i = 0; i < blocksCount - 1; i++) { //最后一片单独处理
                long begin = offSet;
                long end = (i + 1) * perFileSize;
                offSet = getWrite(sourceFilePath, i, begin, end);
            }
            if (fileTotalLength - offSet > 0) {
                getWrite(sourceFilePath, blocksCount - 1, offSet, fileTotalLength);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 指定文件每一份的边界，写入不同文件中
     * @param file 源文件
     * @param index 源文件的顺序标识
     * @param begin 开始指针的位置
     * @param end 结束指针的位置
     * @return long
     */
    public long getWrite(String file,int index,long begin,long end){
        long endPointer = 0L;
        RandomAccessFile in = null ;
        ByteArrayOutputStream byteOutStream = null ;
        try {
            File sourceFile = new File(file);
//            得到文件的MD5值
            String fileMd5 = FileMD5Util.getFileMD5(sourceFile);
            //申明文件切割后的文件磁盘
            in = new RandomAccessFile(sourceFile, "r");
//          定义一个可读，可写的文件并且后缀名为.tmp的二进制文件
            byteOutStream = new ByteArrayOutputStream();
            //申明具体每一文件的字节数组
            byte[] byteArray = new byte[bufferSize];
            int n = 0;
            //从指定位置读取文件字节流
            in.seek(begin);
            //判断文件流读取的边界
            long len = 0 ;
            while((n = in.read(byteArray)) != -1 && in.getFilePointer() <= end){
                // 写入到输出流中
                byteOutStream.write(byteArray, 0, n);
                len +=n;
                System.out.println("文件块序号: "+(index+1)+"  当前文件读取位置: "+begin+" 当前读取的字节长度: "+len);
            }
            //定义当前读取文件的指针
            in.seek(begin + len);
            endPointer  = in.getFilePointer();
//          最后把流上传到 文件服务器
            InputStream inputStream = new ByteArrayInputStream(byteOutStream.toByteArray());
            String fileExt = sourceFile.getName().substring(sourceFile.getName().lastIndexOf(".") + 1);
            /**
             *   后台切片 上传到fastdfs服务器
              */
            StorePath storePath = null ;
            if(index == 0){
                storePath = this.appendFileStorageClient.uploadAppenderFile(FastdfsConfig.FASTDFS_GROUP, inputStream, len, fileExt);
//              把路径放入REDIS中
                stringRedisTemplate.opsForHash().putIfAbsent(fileMd5,fileMd5+"_"+"path",storePath.getPath());
                System.out.println("------ 上传后的路径："+storePath.getFullPath());
            }else{
                Object fastPathObject = stringRedisTemplate.opsForHash().get(fileMd5, fileMd5 + "_" + "path");
                this.appendFileStorageClient.modifyFile(FastdfsConfig.FASTDFS_GROUP, fastPathObject.toString(), inputStream, len, begin);
            }
            if(sourceFile.length() == endPointer){
                System.out.println("文件:["+sourceFile.getName()+"] 上传到服务器成功 ！！ 问价大小: ["+endPointer+"]");
                stringRedisTemplate.opsForHash().delete(fileMd5,fileMd5 + "_" + "path");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(Objects.nonNull(byteOutStream)){
                    byteOutStream.close();
                }
                if(Objects.nonNull(in)){
                    in.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return endPointer;
    }





}
