-- ═══════════════════════════════════════════════════════════
-- World Cup Cards - Schema Inicial
-- ═══════════════════════════════════════════════════════════

-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(500),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    total_exchanges INTEGER NOT NULL DEFAULT 0,
    completed_exchanges INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Cards (master catalog)
CREATE TABLE cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    card_number INTEGER NOT NULL,
    team VARCHAR(100) NOT NULL,
    position VARCHAR(50),
    image_url VARCHAR(500),
    rarity VARCHAR(20) NOT NULL DEFAULT 'COMMON',
    description TEXT,
    year INTEGER NOT NULL DEFAULT 2026,
    edition VARCHAR(100) NOT NULL DEFAULT 'Mundial 2026',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- User cards (album + repeats)
CREATE TABLE user_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_id UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1,
    is_in_album BOOLEAN NOT NULL DEFAULT FALSE,
    is_repeated BOOLEAN NOT NULL DEFAULT FALSE,
    tradeable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, card_id)
);

-- Exchanges
CREATE TABLE exchanges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Exchange items
CREATE TABLE exchange_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exchange_id UUID NOT NULL REFERENCES exchanges(id) ON DELETE CASCADE,
    card_id UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    offered_by VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1
);

-- Indexes
CREATE INDEX idx_user_cards_user_id ON user_cards(user_id);
CREATE INDEX idx_user_cards_card_id ON user_cards(card_id);
CREATE INDEX idx_user_cards_is_in_album ON user_cards(is_in_album);
CREATE INDEX idx_user_cards_is_repeated ON user_cards(is_repeated);
CREATE INDEX idx_exchanges_requester_id ON exchanges(requester_id);
CREATE INDEX idx_exchanges_receiver_id ON exchanges(receiver_id);
CREATE INDEX idx_exchanges_status ON exchanges(status);
CREATE INDEX idx_exchange_items_exchange_id ON exchange_items(exchange_id);
CREATE INDEX idx_cards_team ON cards(team);
CREATE INDEX idx_cards_rarity ON cards(rarity);
CREATE INDEX idx_cards_year ON cards(year);
CREATE INDEX idx_cards_active ON cards(active);
