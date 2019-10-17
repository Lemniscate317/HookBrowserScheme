package com.l.hookbrowserscheme.utils;

import android.net.Uri;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {
    public static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)((?:http|https|file):\\/\\/|(?:inline|data|about|javascript):|(?:.*:.*@))(.*)");
    private static final Pattern STRIP_URL_PATTERN = Pattern.compile("^http://(.*?)/?$");

    public static String stripUrl(String url) {
        if (url == null) {
            return null;
        }
        Matcher m = STRIP_URL_PATTERN.matcher(url);
        if (m.matches()) {
            return m.group(1);
        }
        return url;
    }

    protected static String smartUrlFilter(Uri inUri) {
        if (inUri != null) {
            return smartUrlFilter(inUri.toString());
        }
        return null;
    }

    public static String smartUrlFilter(String url) {
        return smartUrlFilter(url, true);
    }

    public static String smartUrlFilter(String url, boolean canBeSearch) {
        String inUrl = url.trim();
        boolean hasSpace = inUrl.indexOf(32) != -1;
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(inUrl);
        if (matcher.matches()) {
            String scheme = matcher.group(1);
            String lcScheme = scheme.toLowerCase();
            if (!lcScheme.equals(scheme)) {
                inUrl = lcScheme + matcher.group(2);
            }
            if (hasSpace && Patterns.WEB_URL.matcher(inUrl).matches()) {
                inUrl = inUrl.replace(" ", "%20");
            }
            return inUrl;
        } else if (!hasSpace && Patterns.WEB_URL.matcher(inUrl).matches()) {
            return URLUtil.guessUrl(inUrl);
        } else {
            if (canBeSearch) {
                return URLUtil.composeSearchUrl(inUrl, "http://www.google.com/m?q=%s", "%s");
            }
            return null;
        }
    }

    static String fixUrl(String inUrl) {
        int colon = inUrl.indexOf(58);
        boolean allLower = true;
        for (int index = 0; index < colon; index++) {
            char ch = inUrl.charAt(index);
            if (!Character.isLetter(ch)) {
                break;
            }
            allLower &= Character.isLowerCase(ch);
            if (index == colon - 1 && !allLower) {
                inUrl = inUrl.substring(0, colon).toLowerCase() + inUrl.substring(colon);
            }
        }
        if (inUrl.startsWith("http://") || inUrl.startsWith("https://")) {
            return inUrl;
        }
        if (!inUrl.startsWith("http:") && !inUrl.startsWith("https:")) {
            return inUrl;
        }
        if (inUrl.startsWith("http:/") || inUrl.startsWith("https:/")) {
            return inUrl.replaceFirst("/", "//");
        }
        return inUrl.replaceFirst(":", "://");
    }

    static String filteredUrl(String inUrl) {
        if (inUrl == null) {
            return "";
        }
        if (inUrl.startsWith("content:") || inUrl.startsWith("browser:")) {
            return "";
        }
        return inUrl;
    }
}
