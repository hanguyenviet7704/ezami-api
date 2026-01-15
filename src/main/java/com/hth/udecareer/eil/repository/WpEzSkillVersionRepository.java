package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.WpEzSkillVersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WpEzSkillVersionRepository extends JpaRepository<WpEzSkillVersionEntity, Long> {

    // Find versions by skill
    List<WpEzSkillVersionEntity> findBySkillIdOrderByVersionDesc(Long skillId);

    Page<WpEzSkillVersionEntity> findBySkillIdOrderByVersionDesc(Long skillId, Pageable pageable);

    // Find specific version
    Optional<WpEzSkillVersionEntity> findBySkillIdAndVersion(Long skillId, Integer version);

    // Find latest version for a skill
    @Query("SELECT sv FROM WpEzSkillVersionEntity sv WHERE sv.skillId = :skillId ORDER BY sv.version DESC")
    List<WpEzSkillVersionEntity> findLatestBySkillId(@Param("skillId") Long skillId, Pageable pageable);

    default Optional<WpEzSkillVersionEntity> findLatestBySkillId(Long skillId) {
        List<WpEzSkillVersionEntity> results = findLatestBySkillId(skillId, Pageable.ofSize(1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Count versions by skill
    long countBySkillId(Long skillId);

    // Find versions by changed_by user
    Page<WpEzSkillVersionEntity> findByChangedByOrderByCreatedAtDesc(Long changedBy, Pageable pageable);
}
