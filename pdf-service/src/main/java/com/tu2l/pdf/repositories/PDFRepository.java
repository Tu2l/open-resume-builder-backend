package com.tu2l.pdf.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tu2l.pdf.entities.GeneratedPDFEntity;

@Repository
public interface PDFRepository extends JpaRepository<GeneratedPDFEntity, Long> {
}
