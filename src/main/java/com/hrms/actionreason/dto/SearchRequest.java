package com.hrms.actionreason.dto;

import lombok.Data;

@Data
public class SearchRequest {

    private String field;

    private String value;

    private String operator;

}