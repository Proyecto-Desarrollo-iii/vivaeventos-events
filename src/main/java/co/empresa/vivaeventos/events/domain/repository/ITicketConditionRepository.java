package co.empresa.vivaeventos.events.domain.repository;

import co.empresa.vivaeventos.events.domain.model.TicketCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ITicketConditionRepository extends JpaRepository<TicketCondition, UUID> {

    List<TicketCondition> findByTicketId(UUID ticketId);

    List<TicketCondition> findByTicketIdAndIsActiveTrue(UUID ticketId);

    void deleteByTicketId(UUID ticketId);
}