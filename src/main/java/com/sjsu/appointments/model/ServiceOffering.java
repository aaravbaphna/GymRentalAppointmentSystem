package com.sjsu.appointments.model;

public class ServiceOffering {
    private Long id;
    private Long providerId;
    private String name;
    private int durationMin;
    private int priceCents;

    public ServiceOffering() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getDurationMin() { return durationMin; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
}
