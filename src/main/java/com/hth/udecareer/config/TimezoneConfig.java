package com.hth.udecareer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

@Slf4j
@Configuration
public class TimezoneConfig {

    public static final ZoneId VIETNAM_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final TimeZone VIETNAM_TIMEZONE = TimeZone.getTimeZone(VIETNAM_ZONE_ID);

    @PostConstruct
    public void init() {
        // Set default timezone for the entire application
        TimeZone.setDefault(VIETNAM_TIMEZONE);

        // Log timezone info for debugging
        log.info("🕰️ Application timezone set to: {}", VIETNAM_TIMEZONE.getDisplayName());
        log.info("🕰️ Current Vietnam time: {}", getCurrentVietnamTime());
        log.info("🕰️ System default timezone: {}", TimeZone.getDefault().getDisplayName());
    }

    /**
     * Get current time in Vietnam timezone as LocalDateTime
     */
    public static LocalDateTime getCurrentVietnamTime() {
        return ZonedDateTime.now(VIETNAM_ZONE_ID).toLocalDateTime();
    }

    /**
     * Get current time in Vietnam timezone as ZonedDateTime
     */
    public static ZonedDateTime getCurrentVietnamZonedTime() {
        return ZonedDateTime.now(VIETNAM_ZONE_ID);
    }
}
