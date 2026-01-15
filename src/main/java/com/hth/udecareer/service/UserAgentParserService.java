package com.hth.udecareer.service;

import com.hth.udecareer.enums.DeviceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class UserAgentParserService {

    // Patterns for device detection
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "(?i)(android|webos|iphone|ipod|blackberry|iemobile|opera mini|mobile|palm|windows phone)"
    );
    private static final Pattern TABLET_PATTERN = Pattern.compile(
            "(?i)(ipad|tablet|playbook|silk|(?!.*mobile)android)"
    );
    private static final Pattern DESKTOP_PATTERN = Pattern.compile(
            "(?i)(windows|macintosh|linux|x11|unix)"
    );

    // Patterns for browser detection
    private static final Pattern CHROME_PATTERN = Pattern.compile("(?i)(chrome|chromium|crios)/([\\d.]+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("(?i)(firefox|fxios)/([\\d.]+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("(?i)version/([\\d.]+).*safari");
    private static final Pattern EDGE_PATTERN = Pattern.compile("(?i)(edge|edg|edga|edgios)/([\\d.]+)");
    private static final Pattern OPERA_PATTERN = Pattern.compile("(?i)(opera|opr)/([\\d.]+)");
    private static final Pattern IE_PATTERN = Pattern.compile("(?i)(msie|trident)/([\\d.]+)");
    private static final Pattern BRAVE_PATTERN = Pattern.compile("(?i)brave/([\\d.]+)");

    // Patterns for OS detection
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("(?i)(windows nt|win64|win32|wow64)");
    private static final Pattern MACOS_PATTERN = Pattern.compile("(?i)(macintosh|mac os x|mac_powerpc)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("(?i)(linux|x11)");
    private static final Pattern IOS_PATTERN = Pattern.compile("(?i)(iphone|ipad|ipod).*os ([\\d_]+)");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("(?i)android ([\\d.]+)");

    /**
     * Detect device type from user agent string
     */
    public DeviceType detectDeviceType(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty() || "Unknown".equalsIgnoreCase(userAgent)) {
            return DeviceType.UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        // Check for tablet first (tablets often contain "mobile" in user agent)
        if (TABLET_PATTERN.matcher(ua).find()) {
            return DeviceType.TABLET;
        }

        // Check for mobile
        if (MOBILE_PATTERN.matcher(ua).find()) {
            return DeviceType.MOBILE;
        }

        // Check for desktop
        if (DESKTOP_PATTERN.matcher(ua).find()) {
            return DeviceType.DESKTOP;
        }

        return DeviceType.UNKNOWN;
    }

    /**
     * Extract browser name and version from user agent
     */
    public String detectBrowser(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty() || "Unknown".equalsIgnoreCase(userAgent)) {
            return null;
        }

        // Check Edge first (Edge user agent contains Chrome)
        if (EDGE_PATTERN.matcher(userAgent).find()) {
            return "Edge";
        }

        // Check Chrome
        if (CHROME_PATTERN.matcher(userAgent).find()) {
            return "Chrome";
        }

        // Check Firefox
        if (FIREFOX_PATTERN.matcher(userAgent).find()) {
            return "Firefox";
        }

        // Check Safari (but not Chrome-based)
        if (SAFARI_PATTERN.matcher(userAgent).find() && !CHROME_PATTERN.matcher(userAgent).find()) {
            return "Safari";
        }

        // Check Opera
        if (OPERA_PATTERN.matcher(userAgent).find()) {
            return "Opera";
        }

        // Check Internet Explorer
        if (IE_PATTERN.matcher(userAgent).find()) {
            return "Internet Explorer";
        }

        // Check Brave
        if (BRAVE_PATTERN.matcher(userAgent).find()) {
            return "Brave";
        }

        return "Unknown";
    }

    /**
     * Extract OS name from user agent
     */
    public String detectOS(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty() || "Unknown".equalsIgnoreCase(userAgent)) {
            return null;
        }

        // Check iOS
        if (IOS_PATTERN.matcher(userAgent).find()) {
            return "iOS";
        }

        // Check Android
        if (ANDROID_PATTERN.matcher(userAgent).find()) {
            return "Android";
        }

        // Check Windows
        if (WINDOWS_PATTERN.matcher(userAgent).find()) {
            return "Windows";
        }

        // Check macOS
        if (MACOS_PATTERN.matcher(userAgent).find()) {
            return "macOS";
        }

        // Check Linux
        if (LINUX_PATTERN.matcher(userAgent).find()) {
            return "Linux";
        }

        return "Unknown";
    }
}

