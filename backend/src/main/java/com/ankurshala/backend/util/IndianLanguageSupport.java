package com.ankurshala.backend.util;

import java.util.*;

/**
 * Indian Language Support Constants and Utilities
 * Supports major Indian languages for AI Tutor and Voice features
 */
public class IndianLanguageSupport {

    /**
     * Supported Indian languages with their locale codes
     */
    public static final Map<String, LanguageInfo> SUPPORTED_LANGUAGES = new LinkedHashMap<>();

    static {
        // Major Indian languages
        SUPPORTED_LANGUAGES.put("hi", new LanguageInfo("Hindi", "हिंदी", "hi-IN", "hi-IN-AarohiNeural", "hi-IN-MadhurNeural"));
        SUPPORTED_LANGUAGES.put("en", new LanguageInfo("English", "English", "en-IN", "en-IN-NeerjaNeural", "en-IN-PrabhatNeural"));
        SUPPORTED_LANGUAGES.put("ta", new LanguageInfo("Tamil", "தமிழ்", "ta-IN", "ta-IN-PallaviNeural", "ta-IN-ValluvarNeural"));
        SUPPORTED_LANGUAGES.put("te", new LanguageInfo("Telugu", "తెలుగు", "te-IN", "te-IN-ShrutiNeural", "te-IN-MohanNeural"));
        SUPPORTED_LANGUAGES.put("bn", new LanguageInfo("Bengali", "বাংলা", "bn-IN", "bn-IN-TanishaaNeural", "bn-IN-BashkarNeural"));
        SUPPORTED_LANGUAGES.put("mr", new LanguageInfo("Marathi", "मराठी", "mr-IN", "mr-IN-AarohiNeural", "mr-IN-ManoharNeural"));
        SUPPORTED_LANGUAGES.put("gu", new LanguageInfo("Gujarati", "ગુજરાતી", "gu-IN", "gu-IN-DhwaniNeural", "gu-IN-NiranjanNeural"));
        SUPPORTED_LANGUAGES.put("kn", new LanguageInfo("Kannada", "ಕನ್ನಡ", "kn-IN", "kn-IN-SapnaNeural", "kn-IN-GaganNeural"));
        SUPPORTED_LANGUAGES.put("ml", new LanguageInfo("Malayalam", "മലയാളം", "ml-IN", "ml-IN-SobhanaNeural", "ml-IN-MidhunNeural"));
        SUPPORTED_LANGUAGES.put("pa", new LanguageInfo("Punjabi", "ਪੰਜਾਬੀ", "pa-IN", "pa-IN-GulNeural", "pa-IN-GurpreetNeural"));
    }

    /**
     * Get language info by code
     */
    public static LanguageInfo getLanguageInfo(String languageCode) {
        return SUPPORTED_LANGUAGES.getOrDefault(languageCode, SUPPORTED_LANGUAGES.get("en"));
    }

    /**
     * Get all supported language codes
     */
    public static List<String> getSupportedLanguageCodes() {
        return new ArrayList<>(SUPPORTED_LANGUAGES.keySet());
    }

    /**
     * Get default voice for language and gender
     */
    public static String getDefaultVoice(String languageCode, boolean isFemale) {
        LanguageInfo info = getLanguageInfo(languageCode);
        return isFemale ? info.getFemaleVoice() : info.getMaleVoice();
    }

    /**
     * Check if language is supported
     */
    public static boolean isSupported(String languageCode) {
        return SUPPORTED_LANGUAGES.containsKey(languageCode);
    }

    /**
     * Language information holder
     */
    public static class LanguageInfo {
        private final String englishName;
        private final String nativeName;
        private final String localeCode;
        private final String femaleVoice;
        private final String maleVoice;

        public LanguageInfo(String englishName, String nativeName, String localeCode, 
                           String femaleVoice, String maleVoice) {
            this.englishName = englishName;
            this.nativeName = nativeName;
            this.localeCode = localeCode;
            this.femaleVoice = femaleVoice;
            this.maleVoice = maleVoice;
        }

        public String getEnglishName() { return englishName; }
        public String getNativeName() { return nativeName; }
        public String getLocaleCode() { return localeCode; }
        public String getFemaleVoice() { return femaleVoice; }
        public String getMaleVoice() { return maleVoice; }
    }
}

