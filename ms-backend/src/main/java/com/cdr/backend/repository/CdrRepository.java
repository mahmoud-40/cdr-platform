package com.cdr.backend.repository;

import com.cdr.backend.model.Cdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CdrRepository extends JpaRepository<Cdr, Long>, JpaSpecificationExecutor<Cdr> {
    List<Cdr> findBySource(String source);
    List<Cdr> findByDestination(String destination);
    List<Cdr> findByService(String service);
    List<Cdr> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
} 