package com.hth.udecareer.service.Impl;

import com.hth.udecareer.entities.QuizCategoryEntity;
import com.hth.udecareer.entities.TermEntity;
import com.hth.udecareer.model.response.CategoryDropdownResponse;
import com.hth.udecareer.repository.PostRepository;
import com.hth.udecareer.repository.QuizCategoryRepository;
import com.hth.udecareer.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {

    private final QuizCategoryRepository quizCategoryRepository;
    private final PostRepository postCategoryRepository;

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "quizCategories", key = "'all'")
    public List<CategoryDropdownResponse> getQuizCategoriesDropdown() {
        return quizCategoryRepository.findAllActiveWithQuizCount()
                .stream()
                .map(obj -> {
                    QuizCategoryEntity category = (QuizCategoryEntity) obj[0];
                    Long count = (Long) obj[1];
                    CategoryDropdownResponse response = CategoryDropdownResponse.fromQuiz(category);
                    response.setCount(count);
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "postCategories", key = "'all'")
    public List<CategoryDropdownResponse> getPostCategoriesDropdown() {
        List<Object[]> categories = postCategoryRepository.findAllCategoriesWithCount();

        Map<Long, CategoryDropdownResponse> map = new HashMap<>();

        Map<Long, Long> parentMap = new HashMap<>();

        for (Object[] obj : categories) {
            TermEntity termEntity = (TermEntity) obj[0];
            Long parentId = (Long) obj[1];
            Long count = obj.length > 2 && obj[2] != null ? (Long) obj[2] : 0L;
            String description = obj.length > 3 ? (String) obj[3] : null;

            CategoryDropdownResponse dto = CategoryDropdownResponse.fromPost(termEntity);
            dto.setCount(count);
            dto.setDescription(description);

            map.put(termEntity.getId(), dto);
            parentMap.put(termEntity.getId(), parentId);
        }

        List<CategoryDropdownResponse> roots = new ArrayList<>();

        for (Map.Entry<Long, CategoryDropdownResponse> entry : map.entrySet()) {
            Long currentId = entry.getKey();
            CategoryDropdownResponse currentDto = entry.getValue();

            Long  parentId = parentMap.get(currentId);

            if (parentId == 0L) {
                roots.add(currentDto);
            }else{
                CategoryDropdownResponse parentDto = map.get(parentId);

                if(parentDto != null){
                    parentDto.getChildren().add(currentDto);
                }else {
                    roots.add(currentDto);
                }
            }
        }

        return roots;
    }
}