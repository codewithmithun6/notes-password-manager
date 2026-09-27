package com.geeksforgeeks.notespasswordmanager.controller;

import com.geeksforgeeks.notespasswordmanager.model.AppUser;
import com.geeksforgeeks.notespasswordmanager.model.Note;
import com.geeksforgeeks.notespasswordmanager.model.NoteForm;
import com.geeksforgeeks.notespasswordmanager.repository.NoteRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/notes")
public class NoteController {
    private final NoteRepository notes;

    public NoteController(NoteRepository notes) {
        this.notes = notes;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal AppUser user,
                       @RequestParam(defaultValue = "") String q, Model model) {
        String search = q.trim();
        model.addAttribute("notes", search.isEmpty()
                ? notes.findByOwnerOrderByUpdatedAtDesc(user)
                : notes.findByOwnerAndTitleContainingIgnoreCaseOrOwnerAndContentContainingIgnoreCaseOrderByUpdatedAtDesc(
                        user, search, user, search));
        model.addAttribute("q", q);
        return "notes";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("noteForm", new NoteForm());
        model.addAttribute("pageTitle", "New note");
        return "note-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal AppUser user, Model model) {
        Note note = findOwnedNote(id, user);
        NoteForm form = new NoteForm();
        form.setId(note.getId());
        form.setTitle(note.getTitle());
        form.setContent(note.getContent());
        model.addAttribute("noteForm", form);
        model.addAttribute("pageTitle", "Edit note");
        return "note-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute NoteForm noteForm, BindingResult bindingResult,
                       @AuthenticationPrincipal AppUser user, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", noteForm.getId() == null ? "New note" : "Edit note");
            return "note-form";
        }
        Note note = noteForm.getId() == null ? new Note(noteForm.getTitle(), noteForm.getContent(), user)
                : findOwnedNote(noteForm.getId(), user);
        note.setTitle(noteForm.getTitle().trim());
        note.setContent(noteForm.getContent());
        notes.save(note);
        return "redirect:/notes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        notes.delete(findOwnedNote(id, user));
        return "redirect:/notes";
    }

    private Note findOwnedNote(Long id, AppUser user) {
        return notes.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
