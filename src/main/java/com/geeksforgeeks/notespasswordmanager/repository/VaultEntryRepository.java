package com.geeksforgeeks.notespasswordmanager.repository;

import com.geeksforgeeks.notespasswordmanager.model.AppUser;
import com.geeksforgeeks.notespasswordmanager.model.VaultEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VaultEntryRepository extends JpaRepository<VaultEntry, Long> {
    List<VaultEntry> findByOwnerOrderByUpdatedAtDesc(AppUser owner);
    Optional<VaultEntry> findByIdAndOwner(Long id, AppUser owner);
}
