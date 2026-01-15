package com.hth.udecareer.service;

import com.hth.udecareer.entities.FcomTermEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.request.TopicRequest;
import com.hth.udecareer.model.response.TopicResponse;
import com.hth.udecareer.repository.FcomTermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService {

    private static final String TAXONOMY_POST_TOPIC = "post_topic";

    private final FcomTermRepository fcomTermRepository;
    private final EntityManager entityManager;

    /**
     * Get all topics (public)
     */
    @Transactional(readOnly = true)
    public List<TopicResponse> getAllTopics() {
        List<FcomTermEntity> topics = fcomTermRepository.findByTaxonomyName(TAXONOMY_POST_TOPIC);
        return topics.stream()
                .map(this::mapToTopicResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get topic by ID
     */
    @Transactional(readOnly = true)
    public TopicResponse getTopicById(Long id) {
        FcomTermEntity topic = fcomTermRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found"));

        if (!TAXONOMY_POST_TOPIC.equals(topic.getTaxonomyName())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found");
        }

        return mapToTopicResponse(topic);
    }

    /**
     * Get topic by slug
     */
    @Transactional(readOnly = true)
    public TopicResponse getTopicBySlug(String slug) {
        FcomTermEntity topic = fcomTermRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found"));

        if (!TAXONOMY_POST_TOPIC.equals(topic.getTaxonomyName())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found");
        }

        return mapToTopicResponse(topic);
    }

    /**
     * Create or update topic (admin)
     */
    @Transactional
    public TopicResponse saveTopic(TopicRequest request) {
        FcomTermEntity topic;

        if (request.getId() != null) {
            // Update existing
            topic = fcomTermRepository.findById(request.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found"));

            if (!TAXONOMY_POST_TOPIC.equals(topic.getTaxonomyName())) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found");
            }
        } else {
            // Create new
            topic = new FcomTermEntity();
            topic.setTaxonomyName(TAXONOMY_POST_TOPIC);
        }

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());

        // Generate slug if not provided
        if (StringUtils.isBlank(request.getSlug())) {
            topic.setSlug(generateSlug(request.getTitle()));
        } else {
            topic.setSlug(request.getSlug());
        }

        topic = fcomTermRepository.save(topic);
        log.info("Topic saved: id={}, title={}", topic.getId(), topic.getTitle());

        return mapToTopicResponse(topic);
    }

    /**
     * Delete topic (admin)
     */
    @Transactional
    public void deleteTopic(Long id) {
        FcomTermEntity topic = fcomTermRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found"));

        if (!TAXONOMY_POST_TOPIC.equals(topic.getTaxonomyName())) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Topic not found");
        }

        fcomTermRepository.delete(topic);
        log.info("Topic deleted: id={}", id);
    }

    // ============= HELPER METHODS =============

    private TopicResponse mapToTopicResponse(FcomTermEntity entity) {
        return TopicResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .feedsCount(countFeedsByTopic(entity.getId()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Long countFeedsByTopic(Long topicId) {
        String sql = "SELECT COUNT(DISTINCT tf.post_id) FROM wp_fcom_term_feed tf " +
                "JOIN wp_fcom_posts p ON p.id = tf.post_id " +
                "WHERE tf.term_id = :topicId AND p.status = 'published'";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("topicId", topicId);

        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    private String generateSlug(String title) {
        if (StringUtils.isBlank(title)) {
            return "";
        }

        // Normalize and remove diacritics
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(normalized).replaceAll("");

        // Convert to lowercase and replace spaces/special chars with hyphens
        slug = slug.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return slug;
    }
}
