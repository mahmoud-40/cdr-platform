package com.cdr.msloader.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cdr.msloader.entity.CDR;

public interface CdrRepository extends JpaRepository<CDR, Long> { }
