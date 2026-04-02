package com.hrms.actionreason.dto;

import lombok.Data;

@Data
public class DropdownRequest {

    private Long tenantId;

    private String actionCode;

}
