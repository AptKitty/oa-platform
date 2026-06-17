package com.oa.common;

import java.util.List;

/**
 * 分页结果
 */
public class PageResult<T> {

    private long total;
    private int page;
    private int pageSize;
    private List<T> rows;

    public PageResult() {}

    public PageResult(long total, int page, int pageSize, List<T> rows) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.rows = rows;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public List<T> getRows() { return rows; }
    public void setRows(List<T> rows) { this.rows = rows; }
}
