package com.sjsu.appointments.model;

public class Provider {
    private Long id;
    private Long userId;
    private String specialty;
    private String bio;
    private String displayName;

    public Provider() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
