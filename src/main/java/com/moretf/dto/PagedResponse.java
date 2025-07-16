package com.moretf.dto;

import java.util.List;

public class PagedResponse<T> {
    public long total;
    public List<T> data;

    public PagedResponse(List<T> data, long total) {
        this.data = data;
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public long getTotal() {
        return total;
    }
}
