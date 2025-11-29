package com.tu2l.pdf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tu2l.pdf.entity.GeneratedPDFEntity;

@Repository
public interface PDFRepository extends JpaRepository<GeneratedPDFEntity, Long> {
}
