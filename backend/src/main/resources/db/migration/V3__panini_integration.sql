-- ═══════════════════════════════════════════════════════════
-- Panini Digital Sticker Album - Integración
-- ═══════════════════════════════════════════════════════════

-- Perfiles Panini sincronizados
CREATE TABLE panini_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nickname VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150),
    avatar_url VARCHAR(500),
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    completion_percentage INTEGER NOT NULL DEFAULT 0,
    total_cards INTEGER NOT NULL DEFAULT 0,
    total_collection INTEGER NOT NULL DEFAULT 0,
    last_sync TIMESTAMP NOT NULL DEFAULT NOW(),
    sync_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Cartas del álbum Panini (sincronizadas)
CREATE TABLE panini_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES panini_profiles(id) ON DELETE CASCADE,
    card_code VARCHAR(50) NOT NULL,
    card_name VARCHAR(200),
    team VARCHAR(100),
    is_duplicate BOOLEAN NOT NULL DEFAULT FALSE,
    quantity INTEGER NOT NULL DEFAULT 1,
    is_missing BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(profile_id, card_code)
);

-- Log de sincronización
CREATE TABLE panini_sync_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nickname VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    cards_found INTEGER NOT NULL DEFAULT 0,
    duplicates_found INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    response_time_ms BIGINT NOT NULL DEFAULT 0,
    from_cache BOOLEAN NOT NULL DEFAULT FALSE,
    synced_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_panini_profiles_nickname ON panini_profiles(nickname);
CREATE INDEX idx_panini_profiles_active ON panini_profiles(active);
CREATE INDEX idx_panini_cards_profile_id ON panini_cards(profile_id);
CREATE INDEX idx_panini_cards_duplicate ON panini_cards(is_duplicate);
CREATE INDEX idx_panini_cards_missing ON panini_cards(is_missing);
CREATE INDEX idx_panini_sync_logs_nickname ON panini_sync_logs(nickname);
CREATE INDEX idx_panini_sync_logs_synced_at ON panini_sync_logs(synced_at);
