package com.geeksforgeeks.notespasswordmanager;

import com.geeksforgeeks.notespasswordmanager.model.AppUser;
import com.geeksforgeeks.notespasswordmanager.model.Note;
import com.geeksforgeeks.notespasswordmanager.model.VaultEntry;
import com.geeksforgeeks.notespasswordmanager.repository.NoteRepository;
import com.geeksforgeeks.notespasswordmanager.repository.UserRepository;
import com.geeksforgeeks.notespasswordmanager.repository.VaultEntryRepository;
import com.geeksforgeeks.notespasswordmanager.service.VaultEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pocketkeeper;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.vault.key=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
})
class WebPageIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private NoteRepository notes;
    @Autowired private VaultEntryRepository entries;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private VaultEncryptionService encryption;

    private AppUser account;

    @BeforeEach
    void setUp() {
        entries.deleteAll();
        notes.deleteAll();
        users.deleteAll();
        account = users.saveAndFlush(new AppUser("reader@example.com", passwordEncoder.encode("correct-horse-battery")));
        notes.saveAndFlush(new Note("Weekend list", "Pick up herbs and bread", account));
        entries.saveAndFlush(new VaultEntry("Mail", "reader@example.com", "https://example.com",
                encryption.encrypt("top-secret-value"), account));
    }

    @Test
    void publicAuthenticationPagesRender() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")));
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create account")));
    }

    @Test
    void ownerPagesRenderAndVaultSecretStaysMasked() throws Exception {
        mockMvc.perform(get("/notes").with(user(account)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Weekend list")));
        mockMvc.perform(get("/notes/new").with(user(account)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Start writing")));
        mockMvc.perform(get("/notes").param("q", "herbs").with(user(account)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Weekend list")));
        mockMvc.perform(get("/vault").with(user(account)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mail")))
                .andExpect(content().string(not(containsString("top-secret-value"))));
        Long entryId = entries.findByOwnerOrderByUpdatedAtDesc(account).get(0).getId();
        mockMvc.perform(post("/vault/{id}/reveal", entryId).with(user(account)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("top-secret-value")));
    }
}
