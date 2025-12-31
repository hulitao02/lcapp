package com.cloud.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import com.cloud.exception.BussinessException;
import com.cloud.exception.ResultMesCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2021/04/08.
 */
public class ExcelFileUtils {
    private static List<List<Object>> lineList = new ArrayList<>();

    /**
     * excel 导出较大文件工具类
     *
     * @param response
     * @param fileName    文件名
     * @param projects    对象集合
     * @param columnNames 导出的excel中的列名
     * @param keys        对应的是对象中的字段名字
     * @throws IOException
     */
    public static void export(HttpServletResponse response, String fileName, List<?> projects, String[] columnNames, String[] keys) throws Exception {

        ExcelWriter bigWriter = ExcelUtil.getBigWriter();

        for (int i = 0; i < columnNames.length; i++) {
            bigWriter.addHeaderAlias(columnNames[i], keys[i]);
            bigWriter.setColumnWidth(i, 20);
        }
        // 一次性写出内容，使用默认样式，强制输出标题
        bigWriter.write(projects, true);
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xlsx").getBytes(), "iso-8859-1"));
        ServletOutputStream out = response.getOutputStream();
        bigWriter.flush(out, true);
        // 关闭writer，释放内存
        bigWriter.close();
        //此处记得关闭输出Servlet流
        IoUtil.close(out);
    }


    /**
     * excel导入工具类
     *
     * @param file       文件
     * @param columNames 列对应的字段名
     * @return 返回数据集合
     * @throws BussinessException
     * @throws IOException
     */
    public static List<Map<String, Object>> leading(MultipartFile file, String[] columNames) throws BussinessException, IOException {
        String fileName = file.getOriginalFilename();
        // 上传文件为空
        if (StringUtils.isEmpty(fileName)) {
            throw new BussinessException(ResultMesCode.Bad_Request.getResultCode(), "没有导入文件");
        }
        //上传文件大小为1000条数据
        if (file.getSize() > 1024 * 1024 * 10) {
            throw new BussinessException(ResultMesCode.Bad_Request.getResultCode(), "上传失败: 文件大小不能超过10M!");
        }
        // 上传文件名格式不正确
        if (fileName.lastIndexOf(".") != -1 && !".xlsx".equals(fileName.substring(fileName.lastIndexOf(".")))&& !".xls".equals(fileName.substring(fileName.lastIndexOf(".")))) {
            throw new BussinessException(ResultMesCode.Bad_Request.getResultCode(), "文件名格式不正确, 请使用后缀名为.xlsx或者.xls的文件");
        }

        //读取数据
        ExcelUtil.read07BySax(file.getInputStream(), 0, createRowHandler());
        //去除excel中的第一行数据
        lineList.remove(0);

        //将数据封装到list<Map>中
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < lineList.size(); i++) {
            if (null != lineList.get(i)) {
                Map<String, Object> hashMap = new HashMap<>();
                for (int j = 0; j < columNames.length; j++) {
                    Object property = lineList.get(i).get(j);
                    hashMap.put(columNames[j], property);
                }
                dataList.add(hashMap);
            } else {
                break;
            }
        }
        return dataList;
    }

    /**
     * 通过实现handle方法编写我们要对每行数据的操作方式
     */
    private static RowHandler createRowHandler() {
        //清空一下集合中的数据
        lineList.removeAll(lineList);
        return new RowHandler() {
            @Override
            public void handle(int i, int i1, List<Object> list) {
                //将读取到的每一行数据放入到list集合中
                JSONArray jsonObject = new JSONArray(list);
                lineList.add(jsonObject.toList(Object.class));
            }
        };
    }

    public static void exportFile(List<Map<String,Object>> list, Map<String, String> map,String name ,HttpServletResponse response) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter();
        writer.write(list, true);
        Sheet sheet = writer.getSheet();
        setSizeColumn(sheet,map.size()-1);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        ServletOutputStream out = null;
        response.setHeader("Content-Disposition", "attachment;filename=" + new String((name + ".xls").getBytes(), "iso-8859-1"));
        out = response.getOutputStream();
        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }

    /**
     * 自适应宽度(中文支持)
     * @param sheet
     * @param size 因为for循环从0开始，size值为 列数-1
     */
    public static void setSizeColumn(Sheet sheet, int size) {
        for (int columnNum = 0; columnNum <= size; columnNum++) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }

                if (currentRow.getCell(columnNum) != null) {
                    Cell currentCell = currentRow.getCell(columnNum);
                    if (currentCell.getCellType() == CellType.STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            sheet.setColumnWidth(columnNum, columnWidth * 256);
        }
    }

    /**
     * 导出模版文件
     */
    public static void exportModelExcel(HttpServletResponse response,String name ,List<Map<String, String>> map,List<String> ll) throws Exception {
        ExcelWriter excelWriter = ExcelUtil.getWriter();
        int i  = 0;
        CellRangeAddressList addressList = new CellRangeAddressList(1,Integer.MAX_VALUE,0,0);

        String[] str = new String[ll.size()];
        for (int k = 0;k<ll.size();k++) {
            str[k] = ll.get(k);
        }
        Sheet sheet = excelWriter.getSheet();
        DataValidationHelper dataValidationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint explicitListConstraint = dataValidationHelper.createExplicitListConstraint(str);
        DataValidation validation = dataValidationHelper.createValidation(explicitListConstraint, addressList);
        excelWriter.addValidationData(validation);

        for (Map.Entry<String,String> entry:map.get(0).entrySet()) {
            //excelWriter.addHeaderAlias(entry.getKey(),entry.getValue());
            excelWriter.setColumnWidth(i,25);
            i++;
        }
        excelWriter.setDefaultRowHeight(20);
        excelWriter.write(map,true);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        ServletOutputStream out = null;
        response.setHeader("Content-Disposition", "attachment;filename=" + new String((name + ".xls").getBytes(), "iso-8859-1"));
        out = response.getOutputStream();
        excelWriter.flush(out, true);
        excelWriter.close();
    }
}
