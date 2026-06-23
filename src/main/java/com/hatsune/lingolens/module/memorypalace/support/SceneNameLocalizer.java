package com.hatsune.lingolens.module.memorypalace.support;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * Localizes user-facing palace scene titles without changing the stable scene key.
 * Unknown custom titles intentionally fall back to the original database value.
 */
@Component
public class SceneNameLocalizer {

    private static final Map<String, LocalizedSceneName> EXACT_NAMES = Map.ofEntries(
            entry("浅草咖啡馆", "浅草咖啡馆", "Asakusa Cafe", "浅草カフェ"),
            entry("浅草静谧咖啡馆", "浅草静谧咖啡馆", "Quiet Asakusa Cafe", "浅草の静かなカフェ"),
            entry("书桌工作区", "书桌工作区", "Desk Workspace", "デスク作業スペース"),
            entry("涩谷深空工作桌", "涩谷深空工作桌", "Shibuya Space Gray Desk", "渋谷の深空ワークデスク"),
            entry("駅前商店街", "站前商店街", "Station Shopping Street", "駅前商店街"),
            entry("东京站换乘线", "东京站换乘线", "Tokyo Station Transfer Route", "東京駅の乗換ルート"),
            entry("雨天公寓玄关", "雨天公寓玄关", "Rainy Apartment Entryway", "雨の日のアパート玄関"),
            entry("夜间学习角", "夜间学习角", "Night Study Corner", "夜の学習コーナー"),
            entry("现场扫描", "现场扫描", "Live Scan", "ライブスキャン"),
            entry("扫描场景", "扫描场景", "Scan Scene", "スキャンシーン"),
            entry("今日卡片池", "今日卡片池", "Today's Cards", "今日のカード"),
            entry("Default Scene", "默认场景", "Default Scene", "デフォルトシーン")
    );

    public String localize(String sceneName, String language) {
        if (!StringUtils.hasText(sceneName)) {
            return "";
        }
        String normalizedName = sceneName.trim();
        String languageKey = languageKey(language);
        LocalizedSceneName exact = EXACT_NAMES.get(normalizedName);
        if (exact != null) {
            return exact.textFor(languageKey);
        }
        LocalizedSceneName inferred = infer(normalizedName);
        return inferred == null ? normalizedName : inferred.textFor(languageKey);
    }

    public boolean matches(String sceneName, String keyword) {
        if (!StringUtils.hasText(sceneName) || !StringUtils.hasText(keyword)) {
            return false;
        }
        String normalizedName = sceneName.trim();
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        if (normalizedName.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
            return true;
        }
        LocalizedSceneName exact = EXACT_NAMES.get(normalizedName);
        if (exact != null && exact.contains(normalizedKeyword)) {
            return true;
        }
        LocalizedSceneName inferred = infer(normalizedName);
        return inferred != null && inferred.contains(normalizedKeyword);
    }

    private LocalizedSceneName infer(String sceneName) {
        if (containsAny(sceneName, "浅草") && containsAny(sceneName, "咖啡", "カフェ", "cafe")) {
            return new LocalizedSceneName("浅草咖啡馆", "Asakusa Cafe", "浅草カフェ");
        }
        if (containsAny(sceneName, "咖啡", "カフェ", "cafe")) {
            return new LocalizedSceneName("咖啡馆", "Cafe", "カフェ");
        }
        if (containsAny(sceneName, "书桌", "工作桌", "工作区", "desk")) {
            return new LocalizedSceneName("书桌工作区", "Desk Workspace", "デスク作業スペース");
        }
        if (containsAny(sceneName, "车站", "站", "駅", "station")) {
            return new LocalizedSceneName("车站场景", "Station Scene", "駅のシーン");
        }
        if (containsAny(sceneName, "商店街", "shop", "street")) {
            return new LocalizedSceneName("商店街", "Shopping Street", "商店街");
        }
        if (containsAny(sceneName, "厨房", "kitchen")) {
            return new LocalizedSceneName("厨房", "Kitchen", "キッチン");
        }
        if (containsAny(sceneName, "玄关", "entry", "entrance")) {
            return new LocalizedSceneName("玄关", "Entryway", "玄関");
        }
        if (containsAny(sceneName, "学习", "study")) {
            return new LocalizedSceneName("学习角", "Study Corner", "学習コーナー");
        }
        return null;
    }

    private static Map.Entry<String, LocalizedSceneName> entry(String key, String zh, String en, String ja) {
        return Map.entry(key, new LocalizedSceneName(zh, en, ja));
    }

    private boolean containsAny(String text, String... needles) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (String needle : needles) {
            if (lowerText.contains(needle.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String languageKey(String language) {
        if (!StringUtils.hasText(language)) {
            return "zh";
        }
        String lower = language.toLowerCase(Locale.ROOT);
        if (lower.startsWith("ja")) {
            return "ja";
        }
        if (lower.startsWith("en")) {
            return "en";
        }
        return "zh";
    }

    private record LocalizedSceneName(String zh, String en, String ja) {

        private String textFor(String languageKey) {
            return switch (languageKey) {
                case "ja" -> ja;
                case "en" -> en;
                default -> zh;
            };
        }

        private boolean contains(String keyword) {
            return zh.toLowerCase(Locale.ROOT).contains(keyword)
                    || en.toLowerCase(Locale.ROOT).contains(keyword)
                    || ja.toLowerCase(Locale.ROOT).contains(keyword);
        }
    }
}
