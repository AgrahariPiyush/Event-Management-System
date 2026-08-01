package com.project.tickets.repositories;

import com.project.tickets.domain.entities.Ticket;
import com.project.tickets.domain.entities.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    //put pessimistic lock while buying ticket for stopping concurrent changes

    @Query("SELECT tt FROM TicketType tt WHERE tt.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketType> findByIdWithLock(@Param("id") UUID id);
}
