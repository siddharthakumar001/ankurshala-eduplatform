/**
 * Indian Language Support for Frontend
 * Supports major Indian languages for AI Tutor
 */

export interface LanguageInfo {
  code: string
  englishName: string
  nativeName: string
  localeCode: string
  femaleVoice: string
  maleVoice: string
}

export const INDIAN_LANGUAGES: Record<string, LanguageInfo> = {
  hi: {
    code: 'hi',
    englishName: 'Hindi',
    nativeName: 'हिंदी',
    localeCode: 'hi-IN',
    femaleVoice: 'hi-IN-AarohiNeural',
    maleVoice: 'hi-IN-MadhurNeural'
  },
  en: {
    code: 'en',
    englishName: 'English',
    nativeName: 'English',
    localeCode: 'en-IN',
    femaleVoice: 'en-IN-NeerjaNeural',
    maleVoice: 'en-IN-PrabhatNeural'
  },
  ta: {
    code: 'ta',
    englishName: 'Tamil',
    nativeName: 'தமிழ்',
    localeCode: 'ta-IN',
    femaleVoice: 'ta-IN-PallaviNeural',
    maleVoice: 'ta-IN-ValluvarNeural'
  },
  te: {
    code: 'te',
    englishName: 'Telugu',
    nativeName: 'తెలుగు',
    localeCode: 'te-IN',
    femaleVoice: 'te-IN-ShrutiNeural',
    maleVoice: 'te-IN-MohanNeural'
  },
  bn: {
    code: 'bn',
    englishName: 'Bengali',
    nativeName: 'বাংলা',
    localeCode: 'bn-IN',
    femaleVoice: 'bn-IN-TanishaaNeural',
    maleVoice: 'bn-IN-BashkarNeural'
  },
  mr: {
    code: 'mr',
    englishName: 'Marathi',
    nativeName: 'मराठी',
    localeCode: 'mr-IN',
    femaleVoice: 'mr-IN-AarohiNeural',
    maleVoice: 'mr-IN-ManoharNeural'
  },
  gu: {
    code: 'gu',
    englishName: 'Gujarati',
    nativeName: 'ગુજરાતી',
    localeCode: 'gu-IN',
    femaleVoice: 'gu-IN-DhwaniNeural',
    maleVoice: 'gu-IN-NiranjanNeural'
  },
  kn: {
    code: 'kn',
    englishName: 'Kannada',
    nativeName: 'ಕನ್ನಡ',
    localeCode: 'kn-IN',
    femaleVoice: 'kn-IN-SapnaNeural',
    maleVoice: 'kn-IN-GaganNeural'
  },
  ml: {
    code: 'ml',
    englishName: 'Malayalam',
    nativeName: 'മലയാളം',
    localeCode: 'ml-IN',
    femaleVoice: 'ml-IN-SobhanaNeural',
    maleVoice: 'ml-IN-MidhunNeural'
  },
  pa: {
    code: 'pa',
    englishName: 'Punjabi',
    nativeName: 'ਪੰਜਾਬੀ',
    localeCode: 'pa-IN',
    femaleVoice: 'pa-IN-GulNeural',
    maleVoice: 'pa-IN-GurpreetNeural'
  }
}

export const getLanguageInfo = (code: string): LanguageInfo => {
  return INDIAN_LANGUAGES[code] || INDIAN_LANGUAGES.en
}

export const getSupportedLanguages = (): LanguageInfo[] => {
  return Object.values(INDIAN_LANGUAGES)
}

export const getLanguageLocale = (code: string): string => {
  return getLanguageInfo(code).localeCode
}

