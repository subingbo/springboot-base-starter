package com.campus.common.cache;

public final class CacheKeys {

    public static final String PREFIX = "campus:";

    private CacheKeys() {
    }

    public static String refreshToken(String tokenId) {
        return PREFIX + "refresh_token:" + tokenId;
    }

    public static String refreshFamily(String familyId) {
        return PREFIX + "refresh_family:" + familyId;
    }

    public static String userFamilies(Long userId) {
        return PREFIX + "user_families:" + userId;
    }

    public static String refreshResult(String tokenId) {
        return PREFIX + "refresh_result:" + tokenId;
    }

    public static String loginUser(Long userId) {
        return PREFIX + "login_user:" + userId;
    }

    public static String blacklist(String tokenId) {
        return PREFIX + "blacklist:" + tokenId;
    }

    public static String blacklistFamily(String familyId) {
        return PREFIX + "blacklist_family:" + familyId;
    }

    public static String online(String tokenId) {
        return PREFIX + "online:" + tokenId;
    }

    public static String onlineIndex() {
        return PREFIX + "online:index";
    }

    public static String captcha(String captchaId) {
        return PREFIX + "captcha:" + captchaId;
    }

    public static String loginFail(String username) {
        return PREFIX + "login_fail:" + username;
    }

    public static String loginLock(String username) {
        return PREFIX + "login_lock:" + username;
    }

    public static String ipBlacklist() {
        return PREFIX + "ip_blacklist";
    }

    public static String repeat(String key) {
        return PREFIX + "repeat:" + key;
    }

    public static String rate(String bizKey) {
        return PREFIX + "rate:" + bizKey;
    }

    public static String dict(String dictType) {
        return PREFIX + "dict:" + dictType;
    }

    public static String config(String configKey) {
        return PREFIX + "config:" + configKey;
    }

    public static String dataScopeVersion() {
        return PREFIX + "data_scope_version";
    }

    public static String dataScope(long version, Long userId) {
        return PREFIX + "data_scope:" + version + ":" + userId;
    }

    public static String routes(Long userId) {
        return PREFIX + "routes:" + userId;
    }
}
