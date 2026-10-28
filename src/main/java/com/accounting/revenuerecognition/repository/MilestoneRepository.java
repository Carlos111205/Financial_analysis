package com.accounting.revenuerecognition.repository;

import com.accounting.revenuerecognition.domain.enums.MilestoneStatus;
import com.accounting.revenuerecognition.domain.model.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByProjectId(Long projectId);

    List<Milestone> findByProjectIdAndStatus(Long projectId, MilestoneStatus status);

    @Query("SELECT COALESCE(SUM(m.deliverableAmount), 0) FROM Milestone m WHERE m.project.id = :projectId AND m.status IN :statuses")
    BigDecimal sumAmountByProjectIdAndStatuses(@Param("projectId") Long projectId,
                                               @Param("statuses") List<MilestoneStatus> statuses);
}
