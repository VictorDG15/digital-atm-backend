package com.portfolio.digitalatm.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<DebitCard, Long> {
    boolean existsByCardNumber(String cardNumber);
    Optional<DebitCard> findByIdAndAccountOwnerUsername(Long id, String username);
    List<DebitCard> findByAccountOwnerUsernameOrderByCreatedAtDesc(String username);
}
