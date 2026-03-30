package com.hrms.actionreason.repository;

import com.hrms.actionreason.entity.ModuleMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleMasterRepository extends JpaRepository<ModuleMaster, Long> {

    Optional<ModuleMaster> findByModuleMasterNameIgnoreCase(String moduleMasterName);

}
