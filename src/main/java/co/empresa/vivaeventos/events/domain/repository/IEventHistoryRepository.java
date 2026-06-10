package co.empresa.vivaeventos.events.domain.repository;

import co.empresa.vivaeventos.events.domain.model.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IEventHistoryRepository extends JpaRepository<EventHistory, UUID> {
    List<EventHistory> findByEventIdOrderByCreatedAtDesc(UUID eventId);
}
