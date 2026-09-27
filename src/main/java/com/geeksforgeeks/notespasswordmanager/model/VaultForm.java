package com.geeksforgeeks.notespasswordmanager.model;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class VaultForm {
    private Long id;
    @jakarta.validation.constraints.NotBlank @Size(max = 120)
    private String label;
    @Size(max = 190)
    private String username;
    @Size(max = 500) @Pattern(regexp = "(?i)^(https?://\\S*)?$")
    private String website;
    @Size(max = 10000)
    private String secret;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
