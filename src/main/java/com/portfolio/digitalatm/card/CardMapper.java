package com.portfolio.digitalatm.card;

public final class CardMapper {
    private CardMapper() {
    }

    public static CardResponse toResponse(DebitCard card) {
        return new CardResponse(
                card.getId(),
                card.getAccount().getId(),
                card.getAccount().getAccountNumber(),
                mask(card.getCardNumber()),
                card.getStatus(),
                card.getCreatedAt()
        );
    }

    public static String mask(String cardNumber) {
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
