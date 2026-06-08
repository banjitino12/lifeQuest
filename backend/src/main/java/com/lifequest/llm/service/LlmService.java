package com.lifequest.llm.service;

import com.lifequest.llm.model.LlmParseResult;

public interface LlmService {

    LlmParseResult parseDailyLog(String rawText);
}
