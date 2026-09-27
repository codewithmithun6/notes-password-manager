package com.geeksforgeeks.notespasswordmanager.repository;

import com.geeksforgeeks.notespasswordmanager.model.AppUser;
import com.geeksforgeeks.notespasswordmanager.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByOwnerAndTitleContainingIgnoreCaseOrOwnerAndContentContainingIgnoreCaseOrderByUpdatedAtDesc(
            AppUser titleOwner, String title, AppUser contentOwner, String content);
    List<Note> findByOwnerOrderByUpdatedAtDesc(AppUser owner);
    Optional<Note> findByIdAndOwner(Long id, AppUser owner);
}
