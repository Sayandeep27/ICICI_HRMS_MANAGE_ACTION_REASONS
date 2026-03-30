package com.hrms.actionreason.dto;

import lombok.Data;

@Data
public class ViewRequest {

    private String userId;
    private Integer page;
    private Integer size;
    private String searchKey;

}
