-- =====================================================
-- CLEAN SQL SCHEMA - DOMINICAN REPUBLIC BUSINESS
-- Security Business Suite - ASEPRE
-- No triggers, no complex constraints - Pure table definitions
-- =====================================================

-- Enums for Dominican Republic specific types
CREATE TYPE client_type_enum AS ENUM ('PERSONA_FISICA', 'SRL', 'CONSUMIDOR_FINAL');
CREATE TYPE business_sector_enum AS ENUM ('DEALER_AUTOS', 'RESIDENCIAL', 'COOPERATIVA', 'GOBIERNO', 'INDIVIDUAL');
CREATE TYPE security_service_enum AS ENUM ('PUESTO_FIJO', 'PATRULLAJE', 'ESCOLTA');
CREATE TYPE payment_method_enum AS ENUM ('EFECTIVO', 'CHEQUE', 'TRANSFERENCIA_BANCARIA');
CREATE TYPE client_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE invoice_type_enum AS ENUM ('CREDITO', 'CONTADO');
CREATE TYPE invoice_status_enum AS ENUM ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED');
CREATE TYPE receipt_status_enum AS ENUM ('ACTIVE', 'CANCELLED', 'VOIDED');
CREATE TYPE ncf_type_enum AS ENUM ('B01', 'B02');

-- =====================================================
-- CLIENTS TABLE
-- =====================================================
CREATE TABLE clients (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Client Identification
                         client_code TEXT UNIQUE NOT NULL,
                         rnc TEXT,
                         client_type client_type_enum NOT NULL,

    -- Business Information
                         business_name TEXT NOT NULL,
                         contact_person TEXT,
                         business_sector business_sector_enum,

    -- Contact Information
                         phone TEXT,
                         email TEXT,

    -- Dominican Address Format
                         street_name TEXT,
                         street_number TEXT,
                         sector TEXT,
                         provincia TEXT,

    -- Services and Contract Info
                         services security_service_enum[],
                         has_contract BOOLEAN DEFAULT FALSE,
                         contract_start_date DATE,
                         contract_end_date DATE,
                         auto_renewal BOOLEAN DEFAULT TRUE,

    -- Payment Information
                         payment_method payment_method_enum,
                         hourly_rate NUMERIC(10,2),

    -- Tax Information
                         requires_ncf BOOLEAN NOT NULL DEFAULT TRUE,
                         requires_rnc BOOLEAN NOT NULL DEFAULT TRUE,
                         applies_itbis BOOLEAN NOT NULL DEFAULT TRUE,

    -- Status and Audit
                         status client_status_enum NOT NULL DEFAULT 'ACTIVE',
                         notes TEXT,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at TIMESTAMPTZ,
                         created_by UUID NOT NULL REFERENCES users(id),
                         updated_by UUID REFERENCES users(id)
);

-- =====================================================
-- NCF SEQUENCE MANAGEMENT
-- =====================================================
CREATE TABLE ncf_sequences (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               ncf_type ncf_type_enum UNIQUE NOT NULL,
                               prefix TEXT NOT NULL,
                               current_number BIGINT NOT NULL DEFAULT 0,
                               max_number BIGINT NOT NULL DEFAULT 99999999,
                               year INTEGER NOT NULL DEFAULT EXTRACT(YEAR FROM CURRENT_DATE),
                               is_active BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================
-- INVOICES TABLE
-- =====================================================
CREATE TABLE invoices (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Invoice Identification
                          invoice_number TEXT UNIQUE NOT NULL,
                          ncf TEXT UNIQUE,
                          ncf_type ncf_type_enum,

    -- Client Reference
                          client_id UUID NOT NULL REFERENCES clients(id),

    -- Invoice Dates
                          issue_date DATE NOT NULL,
                          due_date DATE NOT NULL,

    -- Invoice Type
                          invoice_type invoice_type_enum NOT NULL DEFAULT 'CREDITO',

    -- Amounts (calculated by backend services)
                          subtotal NUMERIC(12,2) NOT NULL,
                          itbis_amount NUMERIC(12,2) NOT NULL,
                          discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
                          total_amount NUMERIC(12,2) NOT NULL,
                          net_amount NUMERIC(12,2) NOT NULL,

    -- Payment tracking (calculated by backend services)
                          paid_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,
                          balance_due NUMERIC(12,2) NOT NULL DEFAULT 0.00,

    -- Status and Notes
                          status invoice_status_enum NOT NULL DEFAULT 'PENDING',
                          notes TEXT,

    -- Future e-CF compliance fields
                          dgii_track_id TEXT,
                          dgii_status TEXT,
                          ecf_payload JSONB,
                          qr_hash TEXT,

    -- Audit
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at TIMESTAMPTZ,
                          created_by UUID NOT NULL REFERENCES users(id),
                          updated_by UUID REFERENCES users(id)
);

-- =====================================================
-- INVOICE ITEMS TABLE
-- =====================================================
CREATE TABLE invoice_items (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,

    -- Service Information
                               service_code TEXT NOT NULL DEFAULT 'SF-0001',
                               description TEXT NOT NULL DEFAULT 'SERVICIO SEGURIDAD PRIVADA',
                               service_type security_service_enum,

    -- Pricing
                               quantity NUMERIC(8,2) NOT NULL DEFAULT 1.0,
                               unit_price NUMERIC(12,2) NOT NULL,

    -- Tax Information
                               itbis_rate NUMERIC(5,4) NOT NULL DEFAULT 0.1800,
                               itbis_amount NUMERIC(12,2) NOT NULL,

    -- Discounts
                               discount_percentage NUMERIC(5,2) NOT NULL DEFAULT 0.00,
                               discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0.00,

    -- Totals (calculated by backend services)
                               line_subtotal NUMERIC(12,2) NOT NULL,
                               line_total NUMERIC(12,2) NOT NULL,

    -- Audit
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               created_by UUID NOT NULL REFERENCES users(id)
);

-- =====================================================
-- RECEIPTS TABLE (Payment Records)
-- =====================================================
CREATE TABLE receipts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Receipt Identification
                          receipt_number TEXT UNIQUE NOT NULL,

    -- Client and Date
                          client_id UUID NOT NULL REFERENCES clients(id),
                          issue_date DATE NOT NULL,

    -- Payment Information
                          total_amount NUMERIC(12,2) NOT NULL,
                          payment_method payment_method_enum NOT NULL,
                          currency CHAR(3) NOT NULL DEFAULT 'DOP',

    -- Payment Method Details
                          check_number TEXT,
                          bank_name TEXT,
                          reference_number TEXT,

    -- Status and Notes
                          status receipt_status_enum NOT NULL DEFAULT 'ACTIVE',
                          notes TEXT,

    -- Audit
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at TIMESTAMPTZ,
                          created_by UUID NOT NULL REFERENCES users(id),
                          updated_by UUID REFERENCES users(id)
);

-- =====================================================
-- RECEIPT ALLOCATIONS (Payment Applications)
-- =====================================================
CREATE TABLE receipt_allocations (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     receipt_id UUID NOT NULL REFERENCES receipts(id) ON DELETE CASCADE,
                                     invoice_id UUID NOT NULL REFERENCES invoices(id),
                                     allocated_amount NUMERIC(12,2) NOT NULL,

    -- Audit
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     created_by UUID NOT NULL REFERENCES users(id)
);

-- =====================================================
-- SEQUENCE TABLES FOR AUTO-NUMBERING
-- =====================================================
CREATE TABLE client_sequences (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  current_number INTEGER NOT NULL DEFAULT 0,
                                  prefix TEXT NOT NULL DEFAULT 'I',
                                  year INTEGER NOT NULL DEFAULT EXTRACT(YEAR FROM CURRENT_DATE)
);

CREATE TABLE invoice_sequences (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   current_number INTEGER NOT NULL DEFAULT 0,
                                   year INTEGER NOT NULL DEFAULT EXTRACT(YEAR FROM CURRENT_DATE)
);

CREATE TABLE receipt_sequences (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   current_number INTEGER NOT NULL DEFAULT 0,
                                   prefix TEXT NOT NULL DEFAULT 'REC',
                                   year INTEGER NOT NULL DEFAULT EXTRACT(YEAR FROM CURRENT_DATE)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Client indexes
CREATE INDEX idx_clients_rnc ON clients(rnc) WHERE rnc IS NOT NULL;
CREATE INDEX idx_clients_client_code ON clients(client_code);
CREATE INDEX idx_clients_business_name ON clients(LOWER(business_name));
CREATE INDEX idx_clients_status ON clients(status);
CREATE INDEX idx_clients_client_type ON clients(client_type);

-- Invoice indexes
CREATE INDEX idx_invoices_client_id ON invoices(client_id);
CREATE INDEX idx_invoices_ncf ON invoices(ncf) WHERE ncf IS NOT NULL;
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);
CREATE INDEX idx_invoices_issue_date ON invoices(issue_date);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_balance_due ON invoices(balance_due) WHERE balance_due > 0;

-- Receipt indexes
CREATE INDEX idx_receipts_client_id ON receipts(client_id);
CREATE INDEX idx_receipts_receipt_number ON receipts(receipt_number);
CREATE INDEX idx_receipts_issue_date ON receipts(issue_date);
CREATE INDEX idx_receipts_status ON receipts(status);

-- Allocation indexes
CREATE INDEX idx_receipt_allocations_receipt_id ON receipt_allocations(receipt_id);
CREATE INDEX idx_receipt_allocations_invoice_id ON receipt_allocations(invoice_id);

-- =====================================================
-- BASIC CONSTRAINTS (Data Integrity Only)
-- =====================================================

-- Basic data integrity constraints only
ALTER TABLE invoices ADD CONSTRAINT chk_positive_amounts
    CHECK (subtotal >= 0 AND itbis_amount >= 0 AND total_amount >= 0);

ALTER TABLE invoice_items ADD CONSTRAINT chk_positive_line_amounts
    CHECK (quantity > 0 AND unit_price >= 0 AND line_total >= 0);

ALTER TABLE receipts ADD CONSTRAINT chk_positive_receipt_amount
    CHECK (total_amount > 0);

ALTER TABLE receipt_allocations ADD CONSTRAINT chk_positive_allocation
    CHECK (allocated_amount > 0);

-- =====================================================
-- INITIAL DATA SETUP
-- =====================================================

-- Initialize NCF sequences
INSERT INTO ncf_sequences (ncf_type, prefix, current_number, year) VALUES
                                                                       ('B01', 'B01', 0, EXTRACT(YEAR FROM CURRENT_DATE)),
                                                                       ('B02', 'B02', 0, EXTRACT(YEAR FROM CURRENT_DATE));

-- Initialize other sequences
INSERT INTO client_sequences (current_number, prefix, year) VALUES
    (0, 'I', EXTRACT(YEAR FROM CURRENT_DATE));

INSERT INTO invoice_sequences (current_number, year) VALUES
    (0, EXTRACT(YEAR FROM CURRENT_DATE));

INSERT INTO receipt_sequences (current_number, prefix, year) VALUES
    (0, 'REC', EXTRACT(YEAR FROM CURRENT_DATE));

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

COMMENT ON TABLE clients IS 'Client master data with Dominican Republic specific fields';
COMMENT ON COLUMN clients.rnc IS 'Registro Nacional del Contribuyente - required for business clients';
COMMENT ON COLUMN clients.requires_ncf IS 'Whether this client requires NCF on invoices';

COMMENT ON TABLE invoices IS 'Invoice header with NCF compliance for Dominican Republic';
COMMENT ON COLUMN invoices.ncf IS 'Número de Comprobante Fiscal - required for business clients';
COMMENT ON COLUMN invoices.balance_due IS 'Calculated field: total_amount - paid_amount';

COMMENT ON TABLE receipts IS 'Payment receipts - legal documents in Dominican Republic';
COMMENT ON TABLE receipt_allocations IS 'Applications of payments to specific invoices';

COMMENT ON TABLE ncf_sequences IS 'Manages NCF number sequences for DGII compliance';