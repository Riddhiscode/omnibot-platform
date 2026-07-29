package com.omnibot.agent.dto;

public class CancellationResult {
    private String orderId;
    private boolean success;
    private String message;
    private String refundStatus;

    public CancellationResult() {}

    public CancellationResult(String orderId, boolean success, String message, String refundStatus) {
        this.orderId = orderId;
        this.success = success;
        this.message = message;
        this.refundStatus = refundStatus;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
}
