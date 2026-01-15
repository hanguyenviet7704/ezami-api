package com.hth.udecareer.model.response;

import static com.hth.udecareer.utils.HtmlUtil.processHtml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.hth.udecareer.entities.QuestionEntity;
import com.hth.udecareer.utils.PostMetaUtil;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionResponse {
    private static final Pattern ANSWER_DATA_PATTERN = Pattern.compile(
            "i:(?<answerIndex>\\d+);O:27:\"WpProQuiz_Model_AnswerTypes\":\\d+:\\{(?<answerData>[^}]*)}");

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QuestionResponse.class);

    private Long id;

    private Long quizId;

    private Integer sort;

    private String title;

    private Integer points;

    private String question;

    private String correctMsg;

    private String incorrectMsg;

    private Integer correctAnswerCount;

    private Integer correctSameText;

    private Integer tipEnabled;

    private String tipMsg;

    private String answerType;

    private Integer showPointsInBox;

    private Integer answerPointsActivated;

    private Long categoryId;

    private Integer answerPointsDiffModusActivated;

    private Integer disableCorrect;

    private Integer matrixSortAnswerCriteriaWidth;

    private List<AnswerData> answerData;

    private QuestionType questionType;

    private QuestionTypeInfo questionTypeInfo;

    @Data
    @Builder
    public static class AnswerData {
        private String answer;

        private boolean correct;

        private Integer point;

        private Integer index;

        public static AnswerData from(@NotNull String answerDataStr, Integer index) {
            final Map<String, Object> mapAnswerData = PostMetaUtil.getPostMetaValuesNew(answerDataStr);
            final AnswerData answerData = builder().index(index).build();

            for (Entry<String, Object> entry : mapAnswerData.entrySet()) {
                if (entry.getKey().endsWith("_answer") && entry.getValue() instanceof String) {
                    String answer = processHtml((String) entry.getValue());
                    if (StringUtils.isNotBlank(answer)) {
                        while (answer.endsWith("<br>")) {
                            answer = answer.substring(0, answer.lastIndexOf("<br>"));
                        }
                        // Escape HTML code examples
                        answer = escapeCodeTags(answer);
                    } else {
                        // DEFENSIVE: Set placeholder for empty answers to prevent null/empty in response
                        log.warn("Empty answer text detected in answer parsing. Original value: {}", entry.getValue());
                        answer = "(No answer text)";
                    }

                    answerData.setAnswer(answer);
                } else if (entry.getKey().endsWith("_points") && entry.getValue() instanceof Integer) {
                    answerData.setPoint((Integer) entry.getValue());
                } else if (entry.getKey().endsWith("_correct")) {
                    answerData.setCorrect((boolean) entry.getValue());
                }
            }
            return answerData;
        }

        /**
         * Escape HTML tags in code examples to prevent rendering as actual HTML.
         * Converts <tag> to &lt;tag&gt; for display.
         */
        private static String escapeCodeTags(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            // Escape < and > for HTML safety
            return text.replace("<", "&lt;").replace(">", "&gt;");
        }
    }

    public static QuestionResponse from(@NotNull QuestionEntity entity) {
        return from(entity, 0);
    }

    public static QuestionResponse from(@NotNull QuestionEntity entity,
                                        Integer randomAnswer) {
        final List<AnswerData> answerDataList = new ArrayList<>();

        final Matcher answerDataMatcher = ANSWER_DATA_PATTERN.matcher(entity.getAnswerData());
        while (answerDataMatcher.find()) {
            final String answerDataStr = answerDataMatcher.group("answerData");
            final String index = answerDataMatcher.group("answerIndex");
            answerDataList.add(AnswerData.from(answerDataStr, Integer.valueOf(index)));
        }
        // Shuffle answers - Currently disabled
        // if (BooleanUtil.isTrue(randomAnswer)) {
        //     Collections.shuffle(answerDataList);
        // }
        final int correctAnswerCount = (int) answerDataList.stream()
                .filter(AnswerData::isCorrect)
                .count();
        final QuestionTypeInfo questionTypeInfo = entity.getQuestionTypeInfo();

        return builder()
                .id(entity.getId())
                .quizId(entity.getQuizId())
                .sort(entity.getSort())
                .title(entity.getTitle())
                .points(entity.getPoints())
                .question(processHtml(entity.getQuestion()))
                .correctMsg(processHtml(entity.getCorrectMsg()))
                .incorrectMsg(processHtml(entity.getIncorrectMsg()))
                .correctAnswerCount(correctAnswerCount)
                .correctSameText(entity.getCorrectSameText())
                .tipEnabled(entity.getTipEnabled())
                .tipMsg(entity.getTipMsg())
                .answerType(entity.getAnswerType())
                .showPointsInBox(entity.getShowPointsInBox())
                .answerPointsActivated(entity.getAnswerPointsActivated())
                .answerData(answerDataList)
                .categoryId(entity.getCategoryId())
                .answerPointsDiffModusActivated(entity.getAnswerPointsDiffModusActivated())
                .disableCorrect(entity.getDisableCorrect())
                .matrixSortAnswerCriteriaWidth(entity.getMatrixSortAnswerCriteriaWidth())
                .questionType(questionTypeInfo.getType())
                .questionTypeInfo(questionTypeInfo)
                .build();
    }
}
