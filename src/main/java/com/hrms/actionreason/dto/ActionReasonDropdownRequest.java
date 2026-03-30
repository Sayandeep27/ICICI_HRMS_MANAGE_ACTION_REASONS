package com.hrms.actionreason.dto;

import lombok.Data;

@Data
public class ActionReasonDropdownRequest {

    private Long tenantId;

    private String module;

    private String moduleMaster;

    private String searchKey;

}
