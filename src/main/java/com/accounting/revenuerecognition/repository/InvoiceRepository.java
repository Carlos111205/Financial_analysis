package com.accounting.revenuerecognition.repository;

import com.accounting.revenuerecognition.domain.enums.InvoiceStatus;
import com.accounting.revenuerecognition.domain.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByProjectId(Long projectId);

    List<Invoice> findByProjectIdAndStatus(Long projectId, InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.project.id = :projectId AND i.status <> :excludedStatus")
    BigDecimal sumTotalInvoicedByProjectId(@Param("projectId") Long projectId,
                                           @Param("excludedStatus") InvoiceStatus excludedStatus);

    @Query("SELECT COALESCE(SUM(i.recognizedRevenueAmount), 0) FROM Invoice i WHERE i.project.id = :projectId AND i.status <> :excludedStatus")
    BigDecimal sumRecognizedRevenueByProjectId(@Param("projectId") Long projectId,
                                               @Param("excludedStatus") InvoiceStatus excludedStatus);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status <> :excludedStatus")
    BigDecimal sumTotalInvoicedAcrossAllProjects(@Param("excludedStatus") InvoiceStatus excludedStatus);

    @Query("SELECT COALESCE(SUM(i.recognizedRevenueAmount), 0) FROM Invoice i WHERE i.status <> :excludedStatus")
    BigDecimal sumRecognizedRevenueAcrossAllProjects(@Param("excludedStatus") InvoiceStatus excludedStatus);
}
