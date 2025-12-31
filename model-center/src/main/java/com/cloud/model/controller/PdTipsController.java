package com.cloud.model.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.core.ApiResult;
import com.cloud.core.ApiResultHandler;
import com.cloud.model.bean.dto.PdTipsDto;
import com.cloud.model.dao.PdTipsDao;
import com.cloud.model.model.PdTips;
import io.swagger.annotations.ApiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


@RestController
@ApiModel(value = "专家判读建议")
@RefreshScope
@Slf4j
public class PdTipsController {

    // 文件服务地址
    @Value(value = "${file_server}")
    private String fileServer;
    @Autowired
    PdTipsDao pdTipsDao;
    @Value(value = "${dj_localUrlPrefix}")
    private String djLocalUrlPrefix;


    /**
     * @author:胡立涛
     * @description: TODO 添加判读建议
     * @date: 2025/1/13
     * @param: [pdTips]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdtips/addInfo")
    public ApiResult addInfo(@RequestBody PdTips pdTips) {
        try {
            pdTips.setCreateTime(new Timestamp(System.currentTimeMillis()));
            pdTips.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            pdTipsDao.insert(pdTips);
            return ApiResultHandler.buildApiResult(200, "操作成功", pdTips);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    //window下测试使用
//    static String localPath = "E:/dataManage/checkout/movetoFile/";
    static String localPath = "/dataManage/checkout/movetoFile/";

    /**
     * @author:胡立涛
     * @description: TODO 单机版：导入专家判读意见
     * @date: 2025/2/17
     * @param: [multipartFile]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdtips/importInfos")
    public ApiResult importInfos(@RequestParam(name = "file") MultipartFile multipartFile) {
        try {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String destDirectory = localPath + uuid;
            // 将zip文件写入指定目录
            File targetFile = new File(destDirectory + "/" + multipartFile.getOriginalFilename());
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            multipartFile.transferTo(targetFile);
            // 步骤一：解压zip文件
            // 解压后路径
            String oldPicBasePath = destDirectory + "/";
            unzip(destDirectory + "/" + multipartFile.getOriginalFilename(), destDirectory);
            // 步骤二： 解析json数据
            // json文件路径
            String filePath = localPath + uuid + "/tips.json";
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            List<PdTipsDto> pdTipsDtos = JSON.parseArray(content, PdTipsDto.class);
            for (PdTipsDto bean : pdTipsDtos) {
                String rPicPath = "";
                String filePaths = bean.getFilePath() == null ? "" : bean.getFilePath();
                if (filePaths != "") {
                    String[] picPathArr = filePaths.split(",");
                    for (int i = 0; i < picPathArr.length; i++) {
                        try {
                            String picP = picPathArr[i];
                            int index = picP.lastIndexOf("/");
                            String picName = picP.substring(index + 1);
                            // 将图片保存至指定位置
                            String oldPic = oldPicBasePath + picName;
                            String s = savePic(picName, oldPic);
                            rPicPath += s + ",";
                        } catch (Exception e) {
                            System.out.println("-----图片不存在" + e.toString());
                            continue;
                        }
                    }
                }
                // 将专家判读建议保存至数据库
                // 根据单机版唯一标识id查询pdTips表中有无该数据
                QueryWrapper<PdTips> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("dj_id", bean.getDjId());
                queryWrapper.eq("user_id", bean.getUserId());
                PdTips pdTips1 = pdTipsDao.selectOne(queryWrapper);
                if (pdTips1 == null) {
                    // 新增数据
                    pdTips1 = new PdTips();
                    pdTips1.setDjId(bean.getDjId());
                    pdTips1.setPdType(bean.getPdType());
                    pdTips1.setUserId(bean.getUserId());
                    pdTips1.setFlg(1);
                    pdTips1.setScore(bean.getScore());
                    pdTips1.setFilePath(rPicPath == "" ? "" : rPicPath.substring(0, rPicPath.length() - 1));
                    pdTips1.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    pdTips1.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    // 审核状态 0：待审核
                    pdTips1.setCheckState(0);
                    pdTips1.setPdContent(bean.getPdContent());
                    pdTips1.setPdYs(bean.getPdYs());
                    pdTipsDao.insert(pdTips1);
                }
            }
            Files.walk(Paths.get(destDirectory))
                    .sorted(java.util.Comparator.reverseOrder()) // 先删除子文件夹，后删除父文件夹
                    .forEach(path1 -> {
                        try {
                            Files.delete(path1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Deleted: " + path1);
                    });
            return ApiResultHandler.buildApiResult(200, "操作成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 单机版：专家意见图片保存至fastdfs中
     * @date: 2025/2/17
     * @param: [picName, oldPath]
     * @return: void
     */
    public static String savePic(String picName, String oldPath) throws Exception {
        String uuid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        String newPath = localPath + "dj_" + uuid + picName;
        File file = new File(oldPath);
        FileInputStream inputStream = new FileInputStream(file);
        // 新图片地址
        File file1 = new File(newPath);
        System.out.println("------新图片地址：" + newPath);
        FileOutputStream outputStream = new FileOutputStream(file1);
        byte[] b = new byte[1024];
        while (inputStream.read(b) != -1) {
            outputStream.write(b);
        }
        outputStream.close();
        inputStream.close();
        return "dj_" + uuid + picName;
    }


    /**
     * @author:胡立涛
     * @description: TODO 单机版：专家意见 解压zip文件
     * @date: 2025/2/17
     * @param: [multipartFile, destDirectory]
     * @return: void
     */
    public static void unzip(String oldPath, String destDirectory) throws Exception {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(oldPath));
        ZipEntry entry;
        String currentEntry;

        while ((entry = zipIn.getNextEntry()) != null) {
            currentEntry = destDirectory + File.separator + entry.getName();
            File destFile = new File(currentEntry);

            if (entry.isDirectory()) {
                destFile.mkdirs();
            } else {
                new File(destFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(destFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = zipIn.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipIn.closeEntry();
        }
        zipIn.close();
    }


    /**
     * @author:胡立涛
     * @description: TODO 根据id查询判读详细信息
     * @date: 2025/1/13
     * @param: [pdTips]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdtips/getDetail")
    public ApiResult getDetail(@RequestBody PdTips pdTips) {
        try {
            PdTips pdTips1 = pdTipsDao.selectById(pdTips.getId());
            Map rMap = new HashMap();
            rMap.put("data", pdTips1);
            rMap.put("fileServer", fileServer);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 我提的建议
     * @date: 2025/1/13
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdtips/getList")
    public ApiResult getList(@RequestBody Map<String, Object> map) {
        try {
            String userId = map.get("userId").toString();
            int page = Integer.parseInt(map.get("page").toString());
            int size = Integer.parseInt(map.get("size").toString());
            Page<PdTips> pg = new Page(page, size);
            QueryWrapper<PdTips> queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", Integer.parseInt(userId));
            queryWrapper.orderByDesc("update_time");
            IPage pageResult = pdTipsDao.selectPage(pg, queryWrapper);
            Map rMap = new HashMap();
            rMap.put("fileServer", fileServer);
            rMap.put("list", pageResult);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO 修改判读建议
     * @date: 2025/1/13
     * @param: [pdTips]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdtips/updateInfo")
    public ApiResult updateInfo(@RequestBody PdTips pdTips) {
        try {
            pdTips.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            pdTips.setCheckTime(new Timestamp(System.currentTimeMillis()));
            pdTipsDao.updateById(pdTips);
            return ApiResultHandler.buildApiResult(200, "操作成功", pdTips);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 审核
     * @date: 2025/1/13
     * @param: [pdTips]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdtips/check")
    public ApiResult check(@RequestBody PdTips pdTips) {
        try {
            PdTips bean = pdTipsDao.selectById(pdTips.getId());
            bean.setCheckTime(new Timestamp(System.currentTimeMillis()));
            bean.setCheckUserId(pdTips.getCheckUserId());
            bean.setCheckUserName(pdTips.getCheckUserName());
            bean.setCheckDes(pdTips.getCheckDes());
            bean.setCheckState(pdTips.getCheckState());
            pdTipsDao.updateById(bean);
            return ApiResultHandler.buildApiResult(200, "操作成功", pdTips);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }

    /**
     * @author:胡立涛
     * @description: TODO 审核列表
     * @date: 2025/1/13
     * @param: [map]
     * @return: com.cloud.core.ApiResult
     */
    @PostMapping("/pdtips/checkList")
    public ApiResult checkList(@RequestBody Map<String, Object> map) {
        try {
            int page = Integer.parseInt(map.get("page").toString());
            int size = Integer.parseInt(map.get("size").toString());
            String keyWord = map.get("keyWord") == null ? "" : map.get("keyWord").toString();
            Page<PdTips> pg = new Page(page, size);
            QueryWrapper<PdTips> queryWrapper = new QueryWrapper();
            if (keyWord != "") {
                queryWrapper.like("pd_content", keyWord);
            }
            if (map.get("pdType").toString() != "") {
                queryWrapper.eq("pd_type", Integer.parseInt(map.get("pdType").toString()));
            }
            if (map.get("checkState").toString() != "") {
                queryWrapper.eq("check_state", Integer.parseInt(map.get("checkState").toString()));
            }
            if (map.get("pdys").toString() != "") {
                queryWrapper.eq("pd_ys", map.get("pdys").toString());
            }
            queryWrapper.orderByDesc("update_time");
            IPage pageResult = pdTipsDao.selectPage(pg, queryWrapper);
            Map rMap = new HashMap();
            rMap.put("fileServer", fileServer);
            rMap.put("list", pageResult);
            rMap.put("djLocalUrlPrefix", djLocalUrlPrefix);
            return ApiResultHandler.buildApiResult(200, "操作成功", rMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
            return ApiResultHandler.buildApiResult(500, "操作异常", e.toString());
        }
    }
}
