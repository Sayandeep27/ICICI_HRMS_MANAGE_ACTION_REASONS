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
@Table(name = "module")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_seq_gen")
    @SequenceGenerator(name = "module_seq_gen", sequenceName = "MODULE_SEQ", allocationSize = 1)
    private Long id;

    private String moduleName;

}
