package com.cloud.exam.utils;

import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * Created by dyl on 2022/5/16.
 */
public class PageBean<T> {
    private List<T> records;            //展现一页数据的联系人列表
    private int firstPage;               //首页
    private int prePage;                 //上一页
    private int nextPage;                //下一页
    private int pages;               //总页数，也叫末页
    private int current;                 //当前页
    private int total;                   //总条数
    private int size;                //每页大小，每页显示多少条

    //增加起始页 与 结束页成员属性
    private int beginPage;
    private int endPage;

    public int getBeginPage() {
        return beginPage;
    }

    public void setBeginPage(int beginPage) {
        this.beginPage = beginPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }



    //设计创建PageBean对象的工具类方法
    public static  PageBean getPageBean(int current,int size,int count,List dataList){

        //实例对象
        PageBean pageBean = new PageBean();

        //封装8个属性
        //第一个属性dataList【通过调用数据库获取，需要作为方法参数传入进来】
        pageBean.setRecords(dataList);

        //第二个属性firstPage=1
        pageBean.setFirstPage(1);

        //第三个属性curPage【前端传递过来，需要作为方法参数传入进来】
        pageBean.setCurrent(current);

        //第四个属性prePage = 当前页-1
        pageBean.setPrePage(current-1);

        //第五个属性nextPage = 当前页+1
        pageBean.setNextPage(current+1);

        //第六个总条数count【通过调用数据库获取，需要作为方法参数传入进来】
        pageBean.setTotal(count);

        //第七个每页大小pageSize【前端传递过来，需要作为方法参数传入进来】
        pageBean.setSize(size);

        //第八个总页数totalPage
        //总条数10，每页5条，总页数为2页
        //总条数10，每页3条，总页数为4页
        int totalPage = count%size==0?count/size:count/size+1;
        pageBean.setPages(totalPage);

        //封装起始页与结束页
        //实现百度分页栏数字效果：展现10个分页数字
        //分析：计算出起始值与结束值，中间使用循环展现
        //起始值 beginPage
        //结束值 endPage

        //请况1：总页数<=10
        if(totalPage <= 10) {
            //起始值 beginPage=1
            //结束值 endPage=总页数
            pageBean.setBeginPage(1);
            pageBean.setEndPage(totalPage);
        }else {
            //情况2：总页数>10
            //起始值 beginPage=curPage -5
            //结束值 endPage=curPage + 4
            pageBean.setBeginPage(current - 5);
            pageBean.setEndPage(current + 4);

            //起始值溢出 beginPage<1
            if(pageBean.getBeginPage()<1) {
                //起始值 beginPage=1
                //结束值 endPage=10
                pageBean.setBeginPage(1);
                pageBean.setEndPage(10);
            }

            //结束值溢出	 endPage>总页数
            if(pageBean.getEndPage()>totalPage) {
                //起始值 beginPage=总页数-9
                //结束值 endPage=总页数
                pageBean.setBeginPage(totalPage-9);
                pageBean.setEndPage(totalPage);
            }
        }


        return pageBean;
    }

    @Override
    public String toString() {
        return "PageBean{" +
                "dataList=" + records +
                ", firstPage=" + firstPage +
                ", prePage=" + prePage +
                ", nextPage=" + nextPage +
                ", totalPage=" + pages +
                ", curPage=" + current +
                ", count=" + size +
                ", pages=" + pages +
                '}';
    }

    public PageBean() {
    }

    public PageBean(List<T> records, int firstPage, int prePage, int nextPage, int size, int current, int count, int pages) {
        this.records = records;
        this.firstPage = firstPage;
        this.prePage = prePage;
        this.nextPage = nextPage;
        this.size = size;
        this.current = current;
        this.total = count;
        this.pages = pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(int firstPage) {
        this.firstPage = firstPage;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
