package com.verify.panverification.repository;

import com.verify.panverification.entity.ProteanResponseHeader;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProteanResponseHeaderRepository
        extends JpaRepository<ProteanResponseHeader, Long> {

}