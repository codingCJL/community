package com.cjl.community.community.entity;



/**
 * @author cjl
 * @date 2020/4/10 10:47
 * 封装分页信息
 */
public class Page {
    //当前页码
    private int current=1;
    //显示上限，每页显示多少条
    private int limit=10;
    //数据的总行数（用于计算总页数）
    private int rows;
    //查询路径（用户分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<=100){
            this.limit = limit;
        }

    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 当前页的起始行
     * @return
     */
    public int getOffset(){
        //current*limit-limit
        return current*limit-limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){
        //rows/limit+1
        if(rows%limit==0){
            return rows/limit;
        }else {
            return rows/limit+1;
        }
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        int from=current-2;
        return from<1?1:from;
    }
    /**
     * 获取结束页码
     * @return
     */
    public int getTo(){
        int to=current+2;
        return to>getTotal()?getTotal():to;
    }
}
