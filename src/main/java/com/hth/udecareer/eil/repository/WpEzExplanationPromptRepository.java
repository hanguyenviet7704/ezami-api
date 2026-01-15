package com.hth.udecareer.eil.repository;

import com.hth.udecareer.eil.entities.WpEzExplanationPromptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WpEzExplanationPromptRepository extends JpaRepository<WpEzExplanationPromptEntity, Long> {

    // Find by version
    Optional<WpEzExplanationPromptEntity> findByVersion(String version);

    // Find active prompt
    @Query("SELECT p FROM WpEzExplanationPromptEntity p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<WpEzExplanationPromptEntity> findActivePrompts();

    default Optional<WpEzExplanationPromptEntity> findActivePrompt() {
        List<WpEzExplanationPromptEntity> results = findActivePrompts();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Find all ordered by creation date
    List<WpEzExplanationPromptEntity> findAllByOrderByCreatedAtDesc();

    // Find by model
    List<WpEzExplanationPromptEntity> findByModelOrderByCreatedAtDesc(String model);

    // Find by creator
    List<WpEzExplanationPromptEntity> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    // Check if version exists
    boolean existsByVersion(String version);
}
