package com.al.exchange.util;

import java.util.List;

public class PagingTrans {
    private int pageNum;
    private int pageSize;
    private int from;
    private int to;
    private List ls;

    public PagingTrans(int pageNum, int pageSize, List ls) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.ls = ls;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public PagingTrans invoke() {
        from = pageNum * pageSize;
        to = from + pageSize;
        if (from > ls.size()) {
            from = ls.size();
        }
        if (to > ls.size()) {
            to = ls.size();
        }
        return this;
    }
}