package com.hrms.actionreason.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "module_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModuleMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_master_seq_gen")
    @SequenceGenerator(name = "module_master_seq_gen", sequenceName = "MODULE_MASTER_SEQ", allocationSize = 1)
    private Long id;

    private String moduleMasterName;

}
