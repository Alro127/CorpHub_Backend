package com.example.ticket_helpdesk_backend.consts;

public enum BucketName  {
    EMPLOYEE_AVATAR("employee-avatars"),
    EMPLOYEE_DOCUMENT("employee-documents"),
    TICKET_ATTACHMENT("ticket-attachments");

    private final String bucketName;

    BucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
