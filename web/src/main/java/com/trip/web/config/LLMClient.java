package com.trip.web.config;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class LLMClient {

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);

    private final String apiKey;
    private static final String MODEL = "qwen3-max";

    public LLMClient(@Value("${llm.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String chat(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            log.warn("用户消息为空，跳过请求");
            return null;
        }

        try {
            Message userMsg = Message.builder()
                    .role("user")
                    .content(userMessage)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .model(MODEL)
                    .apiKey(apiKey)
                    .messages(Collections.singletonList(userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            if (result != null && result.getOutput() != null && !result.getOutput().getChoices().isEmpty()) {
                return result.getOutput().getChoices().get(0).getMessage().getContent();
            } else {
                log.warn("模型返回结果为空");
                return null;
            }
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error("参数错误: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("调用大模型失败: {}", e.getMessage(), e);
        }
        return null;
    }
}