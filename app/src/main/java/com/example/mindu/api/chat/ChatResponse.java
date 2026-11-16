package com.example.mindu.api.chat;

import java.util.List;
import java.util.Map;

public class ChatResponse {

    private String response;
    private String intent;
    private List<String> sources;
    private boolean missing_info;
    private Map<String, Object> debug_info;
    private String transcription;

    public String getResponse() {
        return response;
    }

    public String getIntent() {
        return intent;
    }

    public List<String> getSources() {
        return sources;
    }

    public boolean isMissing_info() {
        return missing_info;
    }

    public Map<String, Object> getDebug_info() {
        return debug_info;
    }

    public String getTranscription() {
        return transcription;
    }
}
