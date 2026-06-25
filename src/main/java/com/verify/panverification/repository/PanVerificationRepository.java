package com.verify.panverification.repository;

import com.verify.panverification.entity.PanVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PanVerificationRepository extends JpaRepository<PanVerification,Long> {

    List<PanVerification> findByUserId(Long userId);

    Optional<PanVerification>
    findByPanNumber(String panNumber);

    List<PanVerification> findAllByOrderByIdDesc();

    List<PanVerification>
    findByPanNumberContaining(String pan);
}
