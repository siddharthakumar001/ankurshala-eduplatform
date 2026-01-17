# Indian Language Support Implementation

## Overview
Complete implementation of multi-language support for Indian market with 10 major Indian languages.

## Supported Languages

1. **Hindi** (हिंदी) - `hi-IN`
2. **English** - `en-IN`
3. **Tamil** (தமிழ்) - `ta-IN`
4. **Telugu** (తెలుగు) - `te-IN`
5. **Bengali** (বাংলা) - `bn-IN`
6. **Marathi** (मराठी) - `mr-IN`
7. **Gujarati** (ગુજરાતી) - `gu-IN`
8. **Kannada** (ಕನ್ನಡ) - `kn-IN`
9. **Malayalam** (മലയാളം) - `ml-IN`
10. **Punjabi** (ਪੰਜਾਬੀ) - `pa-IN`

## Implementation Details

### Backend

#### Language Support Utility
**File**: `backend/src/main/java/com/ankurshala/backend/util/IndianLanguageSupport.java`

- Centralized language information
- Locale code mapping
- Azure Speech voice mapping (male/female voices)
- Helper methods for language operations

#### AI Tutor Service
**File**: `backend/src/main/java/com/ankurshala/backend/service/AITutorService.java`

- Updated system prompt to mention multi-language support
- Uses student profile language preference
- RAG retrieval respects language preference

#### Voice Service
**File**: `backend/src/main/java/com/ankurshala/backend/service/VoiceService.java`

- Complete Azure Speech SDK integration
- Speech-to-text with language detection
- Text-to-speech with language-specific voices
- Supports all 10 Indian languages

#### Voice Controller
**File**: `backend/src/main/java/com/ankurshala/backend/controller/VoiceController.java`

- `/student/ai/voice/transcribe` - Accepts language parameter
- `/student/ai/voice/synthesize` - Accepts language and voice parameters

### Frontend

#### Language Constants
**File**: `frontend/src/lib/indianLanguages.ts`

- TypeScript definitions for all languages
- Voice mapping for each language
- Helper functions for language operations

#### AI Tutor Page
**File**: `frontend/src/app/student/ai-tutor/page.tsx`

- Language selector dropdown
- Real-time language switching
- Speech recognition uses selected language
- Text-to-speech uses language-specific voice
- Chat API calls include language parameter

## Azure Speech SDK Integration

### Maven Dependency
```xml
<dependency>
    <groupId>com.microsoft.cognitiveservices.speech</groupId>
    <artifactId>client-sdk</artifactId>
    <version>1.40.0</version>
</dependency>
```

### Features
- **Speech-to-Text**: Real-time transcription in selected language
- **Text-to-Speech**: Natural-sounding voices for each language
- **Voice Selection**: Male and female voices available for each language

### Voice Names
Each language has neural voices:
- Female voices: `{lang}-IN-{FemaleVoice}Neural`
- Male voices: `{lang}-IN-{MaleVoice}Neural`

Example:
- Hindi Female: `hi-IN-AarohiNeural`
- Hindi Male: `hi-IN-MadhurNeural`

## Configuration

### Environment Variables

```bash
# Azure Speech Service
AZURE_SPEECH_KEY=your-speech-key
AZURE_SPEECH_REGION=eastus
AZURE_SPEECH_LANGUAGE=en-IN  # Default language

# AI Configuration
AI_ENABLED=true
AI_PROVIDER=azure-openai  # or openai
```

### Docker Configuration

The `docker-compose.yml` includes:
- Azure OpenAI environment variables
- Azure Speech environment variables
- Language configuration

## Usage

### For Students

1. **Select Language**: Use the language dropdown in AI Tutor page
2. **Voice Mode**: Enable voice mode to use speech recognition
3. **Chat**: AI responds in selected language
4. **Voice Output**: Assistant responses are spoken in selected language

### For Developers

#### Backend API

```java
// Transcribe audio
String transcript = voiceService.transcribeAudio(audioData, "hi-IN");

// Synthesize speech
byte[] audio = voiceService.synthesizeSpeech(text, "hi-IN", "hi-IN-AarohiNeural");
```

#### Frontend API

```typescript
// Get language info
const langInfo = getLanguageInfo('hi');
// Returns: { code: 'hi', englishName: 'Hindi', nativeName: 'हिंदी', ... }

// Get all supported languages
const languages = getSupportedLanguages();
```

## Testing

### Manual Testing Steps

1. **Language Selection**:
   - Open AI Tutor page
   - Select different languages from dropdown
   - Verify UI updates

2. **Speech Recognition**:
   - Enable voice mode
   - Speak in selected language
   - Verify transcription appears correctly

3. **Text-to-Speech**:
   - Send a message
   - Verify response is spoken in selected language
   - Test different languages

4. **Chat Responses**:
   - Send messages in different languages
   - Verify AI responds in same language
   - Check language-specific formatting

### Test Cases

- [ ] Language selector displays all 10 languages
- [ ] Language selection persists during session
- [ ] Speech recognition works for all languages
- [ ] Text-to-speech uses correct voice for language
- [ ] AI responses are in selected language
- [ ] Student profile language preference is respected
- [ ] Fallback to English if language not supported

## Deployment

### Local Deployment

1. **Run Azure Setup** (if not done):
   ```powershell
   .\setup-azure-ai.ps1
   ```

2. **Deploy Locally**:
   ```powershell
   .\deploy-local-ai.ps1
   ```

3. **Verify Services**:
   - Backend: http://localhost:8080/api/actuator/health
   - Frontend: http://localhost:3000

### Production Deployment

1. Set environment variables in production environment
2. Ensure Azure Speech Service is accessible
3. Configure CORS for frontend domain
4. Test all languages in production environment

## Known Limitations

1. **Browser Speech API**: Limited language support in some browsers
   - Chrome/Edge: Full support
   - Firefox: Limited support
   - Safari: Limited support
   - **Solution**: Azure Speech Service fallback

2. **Azure Speech SDK**: Requires internet connection
   - Local development: Use Azure Speech Service
   - Production: Ensure network access to Azure

3. **Language Detection**: Currently manual selection
   - Future: Auto-detect from audio input
   - Future: Auto-detect from text input

## Future Enhancements

1. **Auto Language Detection**: Detect language from user input
2. **Mixed Language Support**: Support code-switching (e.g., Hindi-English)
3. **Language Learning**: Track which languages students use
4. **Regional Variants**: Support regional language variants
5. **Translation**: Translate between languages

## Troubleshooting

### Speech Recognition Not Working

1. Check browser compatibility
2. Verify microphone permissions
3. Check Azure Speech Service configuration
4. Review browser console for errors

### Text-to-Speech Not Working

1. Verify Azure Speech Service is configured
2. Check voice name is correct for language
3. Review backend logs for errors
4. Test with different language

### Language Not Displaying Correctly

1. Verify font supports the script (Devanagari, Tamil, etc.)
2. Check browser encoding settings
3. Ensure UTF-8 encoding in HTML

## Support

For issues:
1. Check logs: `backend/logs/ankurshala-backend.log`
2. Review Azure Speech Service status
3. Verify environment variables
4. Test with different browsers

