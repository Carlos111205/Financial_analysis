package com.accounting.revenuerecognition.repository;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import com.accounting.revenuerecognition.domain.model.ContractProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractProjectRepository extends JpaRepository<ContractProject, Long> {

    Optional<ContractProject> findByProjectCode(String projectCode);

    boolean existsByProjectCode(String projectCode);

    List<ContractProject> findByStatus(ProjectStatus status);

    List<ContractProject> findByPricingModel(PricingModel pricingModel);

    @Query("SELECT p FROM ContractProject p LEFT JOIN FETCH p.milestones WHERE p.id = :id")
    Optional<ContractProject> findByIdWithMilestones(Long id);
}
