package com.moretf.dto;

import lombok.Data;
import java.util.List;

@Data
public class CommendBulkRequest {
    private List<String> commendedIds;
}