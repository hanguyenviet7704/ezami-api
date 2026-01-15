package com.hth.udecareer.service;

import com.hth.udecareer.entities.Certificate;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.enums.PostStatus;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for certificate template operations with caching
 */
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;

    /**
     * Get all published certificates with caching
     */
    @Cacheable(value = "certificates", key = "'all'")
    public List<Certificate> getAllPublished() {
        return certificateRepository.findAllPublished();
    }

    /**
     * Get certificate by ID with caching
     */
    @Cacheable(value = "certificates", key = "#id")
    public Certificate getById(Long id) {
        return certificateRepository.findByIdAndStatus(id, PostStatus.PUBLISH)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    /**
     * Search certificates by title (no caching for search)
     */
    public List<Certificate> searchByTitle(String keyword) {
        return certificateRepository.searchByTitle(keyword);
    }
}
