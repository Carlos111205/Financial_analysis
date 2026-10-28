package com.accounting.revenuerecognition.repository;

import com.accounting.revenuerecognition.domain.enums.ActivityType;
import com.accounting.revenuerecognition.domain.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByProjectId(Long projectId);

    List<ActivityLog> findByProjectIdAndInvoicedFalse(Long projectId);

    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId AND a.invoiced = false AND a.logDate <= :cutoffDate")
    List<ActivityLog> findUninvoicedByProjectIdAndDateBefore(@Param("projectId") Long projectId,
                                                            @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT COALESCE(SUM(a.incurredCost), 0) FROM ActivityLog a WHERE a.project.id = :projectId")
    BigDecimal sumTotalIncurredCostByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(a.hoursWorked), 0) FROM ActivityLog a WHERE a.project.id = :projectId AND a.activityType = :type")
    BigDecimal sumHoursWorkedByProjectIdAndType(@Param("projectId") Long projectId,
                                                @Param("type") ActivityType type);
}
