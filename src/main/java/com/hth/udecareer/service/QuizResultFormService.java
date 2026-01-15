package com.hth.udecareer.service;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.hth.udecareer.model.dto.LanguagePointInfoDto;
import com.hth.udecareer.model.dto.LanguagePointInfoDto.PartPoint;
import com.hth.udecareer.model.dto.PointInfoDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizResultFormService {

    public String getLanguageResultForm(@NotNull final PointInfoDto pointInfoDto) {
        return "";
    }

    private String getLanguageResultForm(@NotNull final LanguagePointInfoDto pointInfoDto) {
        final String resultForm = """
                <>
                  <Text style={{
                    fontSize: 18,
                    fontWeight: 'bold',
                    marginBottom: 10
                  }}>
                    Results
                  </Text>
                  <View style={{
                    marginVertical: 20,
                    paddingVertical: 20,
                    justifyContent: 'center',
                    alignItems: "flex-start",
                    backgroundColor: Colors.accent250,
                    borderWidth: 2,
                    borderColor: Colors.accent200,
                    borderRadius: 8
                  }}>
                    $CONTENT
                  </View>
                </>""";
        final StringBuilder content = new StringBuilder();
        for (PartPoint partPoint : pointInfoDto.getPartPoints()) {
            content.append("""
                                   <Text key={item.partCode} style={{marginBottom: 5, paddingHorizontal: 10, fontSize: 15}}>
                                             <Text style={{
                                               fontSize: 18,
                                               fontWeight: 'bold',
                                               marginBottom: 10
                                             }}>""");
            content.append(partPoint.getPartName()).append(": </Text>");
            content.append(partPoint.getCorrects()).append('/').append(partPoint.getQuestions())
                   .append(" ~ ").append(partPoint.getPoints()).append('/').append(partPoint.getTotalPoints())
                   .append(" points </Text>");
        }
        return resultForm.replace("$CONTENT", content);
    }
}
