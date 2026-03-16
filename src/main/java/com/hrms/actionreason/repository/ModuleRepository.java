package com.hrms.actionreason.repository;

import com.hrms.actionreason.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
}