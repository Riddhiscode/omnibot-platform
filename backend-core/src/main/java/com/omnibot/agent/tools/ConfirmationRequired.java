package com.omnibot.agent.tools;

public record ConfirmationRequired(String token, String summary, int expiresInSeconds) {}
