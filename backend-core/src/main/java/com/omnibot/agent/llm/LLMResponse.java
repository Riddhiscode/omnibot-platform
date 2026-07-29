package com.omnibot.agent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Structured response from the LLM.
 * May contain a text reply, tool calls, or both.
 */
public class LLMResponse {

    private String textReply;
    private List<ToolCall> toolCalls = new ArrayList<>();
    private String stopReason;
    private boolean error;

    public String getTextReply()              { return textReply; }
    public void setTextReply(String v)        { this.textReply = v; }
    public List<ToolCall> getToolCalls()      { return toolCalls; }
    public boolean hasToolCalls()             { return !toolCalls.isEmpty(); }
    public String getStopReason()             { return stopReason; }
    public void setStopReason(String v)       { this.stopReason = v; }
    public boolean isError()                  { return error; }
    public void setError(boolean v)           { this.error = v; }

    public void addToolCall(ToolCall call)    { this.toolCalls.add(call); }

    public static class ToolCall {
        private String id;
        private String name;
        private JsonNode input;

        public String getId()            { return id; }
        public void setId(String v)      { this.id = v; }
        public String getName()          { return name; }
        public void setName(String v)    { this.name = v; }
        public JsonNode getInput()       { return input; }
        public void setInput(JsonNode v) { this.input = v; }

        public String getInputText(String key) {
            if (input == null || !input.has(key)) return "";
            return input.path(key).asText("");
        }
    }
}
