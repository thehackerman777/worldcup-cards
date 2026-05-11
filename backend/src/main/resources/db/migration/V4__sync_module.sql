-- ═══════════════════════════════════════════════════════════
-- Módulo de Sincronización (Overlay Scanner)
-- ═══════════════════════════════════════════════════════════

CREATE TABLE synced_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_code VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    is_duplicate BOOLEAN NOT NULL DEFAULT FALSE,
    scan_timestamp BIGINT,
    synced_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_synced_cards_user_id ON synced_cards(user_id);
CREATE INDEX idx_synced_cards_card_code ON synced_cards(card_code);
CREATE INDEX idx_synced_cards_duplicate ON synced_cards(is_duplicate);
CREATE INDEX idx_synced_cards_user_card ON synced_cards(user_id, card_code);
