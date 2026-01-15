package com.hth.udecareer.service;

import com.hth.udecareer.entities.CommentEntity;
import com.hth.udecareer.entities.Post;
import com.hth.udecareer.entities.User;
import com.hth.udecareer.entities.UserMetaEntity;
import com.hth.udecareer.enums.ErrorCode;
import com.hth.udecareer.exception.AppException;
import com.hth.udecareer.model.dto.CommentDto;
import com.hth.udecareer.model.request.CommentRequest;
import com.hth.udecareer.model.response.PageResponse;
import com.hth.udecareer.repository.CommentRepository;
import com.hth.udecareer.repository.PostRepository;
import com.hth.udecareer.repository.UserMetaRepository;
import com.hth.udecareer.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private static final String KEY_AVATAR_URL        = "url_image";

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserMetaRepository userMetaRepository;

    public PageResponse<CommentDto> getRootComments(Long postId, int page, int size) {
        if (postId == null || postRepository.findById(postId).isEmpty()) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size,  Sort.by("commentDate").descending());

        List<CommentEntity> rootComments = commentRepository.findCommentByCommentPostIdRoot(postId, pageable);

        long totalComments = commentRepository.countAllCommentsByPostId(postId);



        List<CommentDto> dtos = rootComments.stream().map(this::mapToDtoWithCount).collect(Collectors.toList());

        return PageResponse.of(dtos, pageable, totalComments);
    }

    public PageResponse<CommentDto> getReplies(Long parentId, int page, int size) {
        if(parentId == null || parentId <= 0 || !commentRepository.existsById(parentId)) {
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("commentDate").descending());

        List<CommentEntity> replies = commentRepository.findCommentByCommentParent(parentId, pageable);

        Long totalReplies = commentRepository.getTotalReplies(parentId);

        List<CommentDto> dtos = replies.stream().map(this::mapToDtoWithCount).collect(Collectors.toList());

        return PageResponse.of(dtos, pageable, totalReplies);
    }

    @Transactional
    public CommentDto createComment(CommentRequest commentRequest, String currentUserEmail) {
//        if(postRepository.findById(commentRequest.getPostId()).isEmpty()) {
//            throw new AppException(ErrorCode.POST_NOT_FOUND);
//        }

        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        Long authorId = userRepository.findByEmail(currentUserEmail).map(u -> u.getId()).orElse(0L);

        String author = userRepository.findByEmail(currentUserEmail).map(u -> u.getDisplayName()).orElse(currentUserEmail);

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentPostId(commentRequest.getPostId());
        commentEntity.setUserId(authorId);
        commentEntity.setCommentAuthor(author);
        commentEntity.setCommentAuthorEmail(currentUserEmail);
        commentEntity.setCommentContent(commentRequest.getContent());
        commentEntity.setCommentParent(commentRequest.getParentId() != null ? commentRequest.getParentId() : 0);
        commentEntity.setCommentApproved("1");
        commentEntity.setCommentDate(LocalDateTime.now());

        commentRepository.save(commentEntity);

        post.setCommentCount(post.getCommentCount()+1);
        postRepository.save(post);

        Long parentId = commentEntity.getCommentParent();

        while(parentId != null && parentId > 0) {
            Long totalReplies = Optional.ofNullable(commentRepository.getTotalReplies(parentId)).orElse(0L);
            saveOrUpdateTotalReplies(parentId, totalReplies + 1);

            parentId = commentRepository.findCommentByCommentId(parentId).map(CommentEntity::getCommentParent).orElse(0L);
        }


        return mapToDtoWithCount(commentEntity);
    }

    @Transactional
    public void saveOrUpdateTotalReplies(Long parentId, Long count){
        commentRepository.updateTotalReplies(parentId, count);
        commentRepository.insertTotalRepliesIfNotExist(parentId, count);
    }

    @Transactional
    public void deleteComment(Long commentId, String currentUserEmail) {
        CommentEntity commentEntity = commentRepository.findCommentByCommentId(commentId).orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));


        if(!commentEntity.getCommentAuthorEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        List<Long> allIds = new ArrayList<>();
        collectCommentIds(commentEntity, allIds);
        commentRepository.deleteCommentsByIds(allIds);
        commentRepository.deleteMetaByCommentIds(allIds);

        Long parentId = commentEntity.getCommentParent();
        while(parentId != null && parentId > 0) {
            Long totalReplies = Optional.ofNullable(commentRepository.getTotalReplies(parentId)).orElse(0L);
            saveOrUpdateTotalReplies(parentId, Math.max(totalReplies - allIds.size(), 0));

            parentId = commentRepository.findCommentByCommentId(parentId).map(CommentEntity::getCommentParent).orElse(0L);
        }

        Post post = postRepository.findById(commentEntity.getCommentPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        post.setCommentCount(Math.max(post.getCommentCount() - allIds.size(), 0));

        postRepository.save(post);
    }

    private void collectCommentIds(CommentEntity comment, List<Long> ids){
        ids.add(comment.getCommentId());
        List<CommentEntity> children = commentRepository.findChildren(comment.getCommentId());
        for(CommentEntity child : children){
            collectCommentIds(child, ids);
        }
    }

    private CommentDto mapToDtoWithCount(CommentEntity commentEntity) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentId(commentEntity.getCommentId());
        commentDto.setAuthor(commentEntity.getCommentAuthor());
        commentDto.setContent(commentEntity.getCommentContent());
        commentDto.setDate(commentEntity.getCommentDate());
        commentDto.setAuthorId(commentEntity.getUserId());

        userMetaRepository.findByUserIdAndMetaKey(commentEntity.getUserId(), KEY_AVATAR_URL).ifPresent(userMeta -> commentDto.setAvatarUrl(userMeta.getMetaValue()));


        Long repliesCount = commentRepository.getTotalReplies(commentEntity.getCommentId());
        commentDto.setRepliesCount(repliesCount != null ? repliesCount.intValue() : 0);
        return commentDto;
    }

}
