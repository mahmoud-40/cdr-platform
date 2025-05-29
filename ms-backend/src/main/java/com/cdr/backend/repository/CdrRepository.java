package com.cdr.backend.repository;

import com.cdr.backend.model.Cdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CdrRepository extends JpaRepository<Cdr, Long>, JpaSpecificationExecutor<Cdr> {
    List<Cdr> findByCallingNumber(String number);
    List<Cdr> findByCalledNumber(String number);
    List<Cdr> findByCallType(Cdr.CallType type);
    List<Cdr> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
} 