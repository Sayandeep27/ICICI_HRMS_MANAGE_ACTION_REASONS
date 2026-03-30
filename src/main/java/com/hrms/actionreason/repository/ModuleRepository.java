package com.hrms.actionreason.repository;

import com.hrms.actionreason.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    Optional<Module> findByModuleNameIgnoreCase(String moduleName);

}
