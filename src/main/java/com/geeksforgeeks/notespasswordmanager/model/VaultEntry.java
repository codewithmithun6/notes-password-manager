package com.geeksforgeeks.notespasswordmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "vault_entries")
public class VaultEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(length = 190)
    private String username;

    @Column(length = 500)
    private String website;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedSecret;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    protected VaultEntry() {
    }

    public VaultEntry(String label, String username, String website, String encryptedSecret, AppUser owner) {
        this.label = label;
        this.username = username;
        this.website = website;
        this.encryptedSecret = encryptedSecret;
        this.owner = owner;
    }

    @PrePersist
    @PreUpdate
    void touch() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getEncryptedSecret() { return encryptedSecret; }
    public void setEncryptedSecret(String encryptedSecret) { this.encryptedSecret = encryptedSecret; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public AppUser getOwner() { return owner; }
}
