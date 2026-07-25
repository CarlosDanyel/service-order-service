package com.fiap.tech_challenge_fase2.infrastructure.persistence.repository;

import com.fiap.tech_challenge_fase2.domain.enums.ServiceOrderStatus;
import com.fiap.tech_challenge_fase2.infrastructure.persistence.entity.ServiceOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderJpaEntity, String> {

    Optional<ServiceOrderJpaEntity> findByApprovalToken(String approvalToken);

    @Query("""
        SELECT os FROM ServiceOrderJpaEntity os
        WHERE os.deleted = false
          AND os.status NOT IN :excludedStatuses
        ORDER BY
            CASE os.status
                WHEN 'EXECUTION'         THEN 1
                WHEN 'AWAITING_APPROVAL' THEN 2
                WHEN 'DIAGNOSIS'         THEN 3
                WHEN 'RECEIVED'          THEN 4
                ELSE 5
            END ASC,
            os.createdAt ASC
        """)
    List<ServiceOrderJpaEntity> findActiveOrdered(List<ServiceOrderStatus> excludedStatuses);
}
