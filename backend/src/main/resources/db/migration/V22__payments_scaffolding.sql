-- V22__payments_scaffolding.sql
-- Payments scaffolding for Phase 4C

-- PAYMENT METHODS
CREATE TABLE payment_methods (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    method_type VARCHAR(50) NOT NULL CHECK (method_type IN ('CARD', 'UPI', 'NET_BANKING', 'WALLET', 'CASH')),
    provider VARCHAR(100) NOT NULL,
    provider_id VARCHAR(200),
    masked_details VARCHAR(200),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PAYMENT INTENTS
CREATE TABLE payment_intents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id BIGINT REFERENCES bookings(id) ON DELETE SET NULL,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    payment_method_id BIGINT REFERENCES payment_methods(id) ON DELETE SET NULL,
    provider_payment_id VARCHAR(200),
    provider_order_id VARCHAR(200),
    provider_response JSONB,
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PAYMENT TRANSACTIONS
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_intent_id BIGINT NOT NULL REFERENCES payment_intents(id) ON DELETE CASCADE,
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('PAYMENT', 'REFUND', 'PARTIAL_REFUND', 'CHARGEBACK')),
    amount_cents BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED')),
    provider_transaction_id VARCHAR(200),
    provider_response JSONB,
    failure_reason TEXT,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PAYMENT REFUNDS
CREATE TABLE payment_refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_intent_id BIGINT NOT NULL REFERENCES payment_intents(id) ON DELETE CASCADE,
    amount_cents BIGINT NOT NULL,
    reason VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED')),
    provider_refund_id VARCHAR(200),
    provider_response JSONB,
    failure_reason TEXT,
    processed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PAYMENT WEBHOOKS
CREATE TABLE payment_webhooks (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_id VARCHAR(200) NOT NULL,
    payload JSONB NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    processing_error TEXT,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- PAYMENT AUDIT LOG
CREATE TABLE payment_audit_log (
    id BIGSERIAL PRIMARY KEY,
    payment_intent_id BIGINT NOT NULL REFERENCES payment_intents(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    performed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    performed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ip_address INET,
    user_agent TEXT
);

-- Indexes
CREATE INDEX idx_payment_methods_user ON payment_methods(user_id);
CREATE INDEX idx_payment_methods_active ON payment_methods(is_active);
CREATE INDEX idx_payment_intents_user ON payment_intents(user_id);
CREATE INDEX idx_payment_intents_booking ON payment_intents(booking_id);
CREATE INDEX idx_payment_intents_status ON payment_intents(status);
CREATE INDEX idx_payment_intents_provider ON payment_intents(provider_payment_id);
CREATE INDEX idx_payment_transactions_intent ON payment_transactions(payment_intent_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(status);
CREATE INDEX idx_payment_transactions_provider ON payment_transactions(provider_transaction_id);
CREATE INDEX idx_payment_refunds_intent ON payment_refunds(payment_intent_id);
CREATE INDEX idx_payment_refunds_status ON payment_refunds(status);
CREATE INDEX idx_payment_webhooks_provider ON payment_webhooks(provider);
CREATE INDEX idx_payment_webhooks_event ON payment_webhooks(event_type);
CREATE INDEX idx_payment_webhooks_processed ON payment_webhooks(processed);
CREATE INDEX idx_payment_audit_log_intent ON payment_audit_log(payment_intent_id);
CREATE INDEX idx_payment_audit_log_performed ON payment_audit_log(performed_at);
