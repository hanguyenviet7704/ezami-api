package com.hth.udecareer.model.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hth.udecareer.enums.QuizType;

public interface QuizDto {

    Long getId();

    String getSlug();

    Integer getTimeLimit();

    String getName();

    Long getPostId();

    String getPostContent();

    String getPostTitle();

    default boolean isCategory(String category) {
        final Pattern pattern = Pattern.compile("^(" + category.toLowerCase() + ")\\s*[-–]", Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(getName().toLowerCase());
        return matcher.find();
    }

    default boolean isMiniTest() {
        return getName().toLowerCase().contains("- mini -");
    }

    default QuizType getQuizType() {
        if (getName().toLowerCase().contains("- mini test -")) {
            return QuizType.MINI;
        }
        if (getName().toLowerCase().contains("- half test -")) {
            return QuizType.HALF;
        }
        if (getName().toLowerCase().contains("- part review -")) {
            return QuizType.PART;
        }
        return QuizType.FULL;
    }
}
