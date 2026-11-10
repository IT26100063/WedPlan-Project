package com.wedplan.wedplan.model.bookingMgt;

public abstract class BaseEntity {
    private String id;
    private String createdAt;

    protected BaseEntity(String id, String createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}