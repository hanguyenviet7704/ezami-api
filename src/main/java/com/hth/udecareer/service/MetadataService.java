package com.hth.udecareer.service;
import com.hth.udecareer.model.response.CategoryDropdownResponse;

import java.util.List;

public interface MetadataService {
    List<CategoryDropdownResponse> getQuizCategoriesDropdown();
    List<CategoryDropdownResponse> getPostCategoriesDropdown();
}

