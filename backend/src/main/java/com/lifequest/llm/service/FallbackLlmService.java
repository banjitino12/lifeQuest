package com.lifequest.llm.service;

import com.lifequest.llm.model.LlmParseResult;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FallbackLlmService implements LlmService {

    @Override
    public LlmParseResult parseDailyLog(String rawText) {
        return new LlmParseResult("FALLBACK", List.of(), List.of(), null, null, null, List.of());
    }
}
