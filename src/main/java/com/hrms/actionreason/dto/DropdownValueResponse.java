package com.hrms.actionreason.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DropdownValueResponse {

    private Long id;

    private String code;

    private String name;

}
