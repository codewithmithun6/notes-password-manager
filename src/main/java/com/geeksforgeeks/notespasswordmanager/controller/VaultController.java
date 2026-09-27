package com.geeksforgeeks.notespasswordmanager.controller;

import com.geeksforgeeks.notespasswordmanager.model.AppUser;
import com.geeksforgeeks.notespasswordmanager.model.VaultEntry;
import com.geeksforgeeks.notespasswordmanager.model.VaultForm;
import com.geeksforgeeks.notespasswordmanager.repository.VaultEntryRepository;
import com.geeksforgeeks.notespasswordmanager.service.VaultEncryptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/vault")
public class VaultController {
    private final VaultEntryRepository entries;
    private final VaultEncryptionService encryption;

    public VaultController(VaultEntryRepository entries, VaultEncryptionService encryption) {
        this.entries = entries;
        this.encryption = encryption;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal AppUser user, Model model) {
        model.addAttribute("entries", entries.findByOwnerOrderByUpdatedAtDesc(user));
        model.addAttribute("vaultForm", new VaultForm());
        model.addAttribute("editing", false);
        return "vault";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute VaultForm vaultForm, BindingResult bindingResult,
                       @AuthenticationPrincipal AppUser user, Model model) {
        if (vaultForm.getId() == null && (vaultForm.getSecret() == null || vaultForm.getSecret().isBlank())) {
            bindingResult.rejectValue("secret", "NotBlank", "Secret is required for a new entry.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("entries", entries.findByOwnerOrderByUpdatedAtDesc(user));
            model.addAttribute("editing", vaultForm.getId() != null);
            return "vault";
        }
        VaultEntry entry = vaultForm.getId() == null
                ? new VaultEntry(vaultForm.getLabel().trim(), vaultForm.getUsername(), vaultForm.getWebsite(), "", user)
                : findOwnedEntry(vaultForm.getId(), user);
        entry.setLabel(vaultForm.getLabel().trim());
        entry.setUsername(vaultForm.getUsername());
        entry.setWebsite(vaultForm.getWebsite());
        if (vaultForm.getSecret() != null && !vaultForm.getSecret().isBlank()) {
            entry.setEncryptedSecret(encryption.encrypt(vaultForm.getSecret()));
        }
        entries.save(entry);
        return "redirect:/vault";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, @AuthenticationPrincipal AppUser user, Model model) {
        VaultEntry entry = findOwnedEntry(id, user);
        VaultForm form = new VaultForm();
        form.setId(entry.getId());
        form.setLabel(entry.getLabel());
        form.setUsername(entry.getUsername());
        form.setWebsite(entry.getWebsite());
        model.addAttribute("vaultForm", form);
        model.addAttribute("editing", true);
        model.addAttribute("entries", entries.findByOwnerOrderByUpdatedAtDesc(user));
        return "vault";
    }

    @PostMapping("/{id}/reveal")
    public String reveal(@PathVariable Long id, @AuthenticationPrincipal AppUser user, Model model) {
        VaultEntry entry = findOwnedEntry(id, user);
        model.addAttribute("entries", entries.findByOwnerOrderByUpdatedAtDesc(user));
        model.addAttribute("vaultForm", new VaultForm());
        model.addAttribute("editing", false);
        model.addAttribute("revealedId", id);
        model.addAttribute("revealedSecret", encryption.decrypt(entry.getEncryptedSecret()));
        return "vault";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        entries.delete(findOwnedEntry(id, user));
        return "redirect:/vault";
    }

    private VaultEntry findOwnedEntry(Long id, AppUser user) {
        return entries.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
