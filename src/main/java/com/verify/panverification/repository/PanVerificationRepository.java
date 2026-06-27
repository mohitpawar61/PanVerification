package com.verify.panverification.repository;

import com.verify.panverification.entity.PanVerification;
import com.verify.panverification.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PanVerificationRepository extends JpaRepository<PanVerification,Long> {

    List<PanVerification> findByUser(User user);

    Optional<PanVerification>
    findByPanNumber(String panNumber);

    List<PanVerification> findByUserOrderByIdDesc(User user);

    List<PanVerification> findAllByOrderByIdDesc();

    List<PanVerification>
    findByPanNumberContaining(String pan,User user);
}
