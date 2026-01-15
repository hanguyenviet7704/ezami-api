package com.hth.udecareer.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HtmlUtil {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(?<!(>))(\\r\\n)(?!<)");
    private static final Pattern AUDIO_PATTERN = Pattern.compile("\\[audio .*=\"(.*)\"]\\[/audio]");
    private static final Pattern EMBED_PATTERN = Pattern.compile("\\[embed](.*)\\[/embed]");

    @Nullable
    public static String processHtml(@Nullable final String html) {
        if (html == null) {
            return null;
        }
        String processedHtml = NEW_LINE_PATTERN.matcher(html.replace("<!--StartFragment -->", "")
                                                            .replace("<!--EndFragment -->", "")
                                                            .replace("<p class=\"pf0\">", "<p>")
                                                            .replace("<span class=\"cf0\">", "<span>")
                                                            .replace("<span></span>", "")
                                                            .replace("<p></p>", ""))
                                               .replaceAll("<br>")
                                               .trim();

        final Matcher audioMatcher = AUDIO_PATTERN.matcher(processedHtml);
        if (audioMatcher.find()) {
            final String audioContent = audioMatcher.group(0);
            final String audioSource = audioMatcher.group(1);

            final String audioTag = "<audio controls src=\"" + audioSource + "\"></audio>";
            processedHtml = processedHtml.replace(audioContent, audioTag);
        }

        final Matcher embedMatcher = EMBED_PATTERN.matcher(processedHtml);
        if (embedMatcher.find()) {
            final String videoContent = embedMatcher.group(0);
            final String videoSource = embedMatcher.group(1);

            final String videoTag = "<embed controls src=\"" + videoSource + "\"></embed>";
            processedHtml = processedHtml.replace(videoContent, videoTag);
        }

        return processedHtml;
    }
}
