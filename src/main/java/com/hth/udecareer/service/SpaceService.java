package com.hth.udecareer.service;

import com.hth.udecareer.entities.SpaceEntity;
import com.hth.udecareer.entities.SpaceUserEntity;
import com.hth.udecareer.entities.TranslationEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.response.SpaceMembershipResponse;
import com.hth.udecareer.model.response.SpaceResponse;
import com.hth.udecareer.repository.SpaceRepository;
import com.hth.udecareer.repository.SpaceUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceUserRepository spaceUserRepository;
    private final TranslationService translationService;

    public List<SpaceResponse> getSpaces() {

        List<SpaceEntity> spaces = spaceRepository.findAll();


        List<SpaceEntity> validEntities = spaces.stream()
                .filter(s -> s.getPrivacy() != null && "public".equalsIgnoreCase(s.getPrivacy()))
                .filter(s -> s.getStatus() != null && ("published".equalsIgnoreCase(s.getStatus()) || "active".equalsIgnoreCase(s.getStatus())))
                .collect(Collectors.toList());


        Map<Long, Long> idToParentMap = validEntities.stream()
                .collect(Collectors.toMap(
                        SpaceEntity::getId,
                        e -> e.getParentId() == null ? 0L : e.getParentId(),
                        (existing, replacement) -> existing
                ));


        List<SpaceResponse> allDtos = validEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        Map<Long, List<SpaceResponse>> childrenMap = allDtos.stream()
                .filter(dto -> {
                    Long pid = idToParentMap.get(dto.getId());
                    return pid != null && pid != 0L;
                })
                .collect(Collectors.groupingBy(dto -> idToParentMap.get(dto.getId())));

        return allDtos.stream()
                .filter(dto -> {
                    Long pid = idToParentMap.get(dto.getId());
                    return pid == null || pid == 0L;
                })
                .peek(root -> {
                    List<SpaceResponse> children = childrenMap.get(root.getId());
                    if (children != null) {
                        root.setSpaces(children);
                    }
                })
                .collect(Collectors.toList());
    }

    public SpaceResponse getSpaceBySlug(String slug) {
        SpaceEntity entity = spaceRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));

        return convertToDto(entity);
    }

    public SpaceResponse getSpaceById(Long id) {
        SpaceEntity entity = spaceRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SPACE_NOT_FOUND));

        return convertToDto(entity);
    }

    private SpaceResponse convertToDto(SpaceEntity entity) {
        // Get translations for current language
        Map<String, String> translations = translationService.getTranslations(
                TranslationEntity.TYPE_SPACE, entity.getId());

        // Use translated values if available, otherwise use original
        String translatedTitle = translations.getOrDefault(
                TranslationEntity.FIELD_TITLE, entity.getTitle());

        SpaceResponse dto = new SpaceResponse();
        dto.setId(entity.getId());
        dto.setTitle(translatedTitle);
        dto.setSlug(entity.getSlug());
        dto.setType(entity.getType());
        dto.setPrivacy(entity.getPrivacy());
        dto.setSpaces(new ArrayList<>());
        return dto;
    }

    @Transactional
    public SpaceMembershipResponse joinSpace(Long spaceId, Long userId) {
        // Check if space exists
        if (!spaceRepository.existsById(spaceId)) {
            throw new AppException(ErrorCode.SPACE_NOT_FOUND);
        }

        // Check if already a member
        Optional<SpaceUserEntity> existingMembership = spaceUserRepository
                .findBySpaceIdAndUserId(spaceId, userId);

        if (existingMembership.isPresent()) {
            SpaceUserEntity membership = existingMembership.get();
            // If status is "left" or inactive, reactivate it
            if ("left".equals(membership.getStatus()) || "inactive".equals(membership.getStatus())) {
                membership.setStatus("active");
                membership.setRole("member");
                spaceUserRepository.save(membership);

                return SpaceMembershipResponse.builder()
                        .spaceId(spaceId)
                        .userId(userId)
                        .isMember(true)
                        .role("member")
                        .message("Successfully rejoined space")
                        .build();
            }

            // Already an active member
            return SpaceMembershipResponse.builder()
                    .spaceId(spaceId)
                    .userId(userId)
                    .isMember(true)
                    .role(membership.getRole())
                    .message("Already a member of this space")
                    .build();
        }

        // Create new membership
        SpaceUserEntity newMembership = SpaceUserEntity.builder()
                .spaceId(spaceId)
                .userId(userId)
                .status("active")
                .role("member")
                .build();
        spaceUserRepository.save(newMembership);

        return SpaceMembershipResponse.builder()
                .spaceId(spaceId)
                .userId(userId)
                .isMember(true)
                .role("member")
                .message("Successfully joined space")
                .build();
    }

    @Transactional
    public SpaceMembershipResponse leaveSpace(Long spaceId, Long userId) {
        // Check if space exists
        if (!spaceRepository.existsById(spaceId)) {
            throw new AppException(ErrorCode.SPACE_NOT_FOUND);
        }

        // Check if user is a member
        Optional<SpaceUserEntity> existingMembership = spaceUserRepository
                .findBySpaceIdAndUserId(spaceId, userId);

        if (existingMembership.isEmpty()) {
            return SpaceMembershipResponse.builder()
                    .spaceId(spaceId)
                    .userId(userId)
                    .isMember(false)
                    .role(null)
                    .message("Not a member of this space")
                    .build();
        }

        SpaceUserEntity membership = existingMembership.get();

        // Check if user is admin - admins cannot leave (must transfer ownership first)
        if ("admin".equals(membership.getRole())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Admins cannot leave space. Please transfer ownership first.");
        }

        // Mark as left (soft delete)
        membership.setStatus("left");
        spaceUserRepository.save(membership);

        return SpaceMembershipResponse.builder()
                .spaceId(spaceId)
                .userId(userId)
                .isMember(false)
                .role(null)
                .message("Successfully left space")
                .build();
    }

}
