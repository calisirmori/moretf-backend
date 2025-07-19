package com.moretf.dto;

import lombok.Data;

@Data
public class CommendBroadcast {
    private String logId;
    private String commendedId;
    private String commenderId;
    private int totalCount;
}
