-- V2__seed_data.sql
-- Seed providers and routing rules so the app is usable on first run.

INSERT INTO providers (id, npi, name, region, specialty, active, created_at, updated_at) VALUES
    ('11111111-1111-1111-1111-111111111111', '1000000001', 'Sunset Medical Group',     'WEST',    'GENERAL_PRACTICE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', '1000000002', 'Lakeside Dental',          'MIDWEST', 'DENTAL',           TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', '1000000003', 'Northeast Vision Center',  'EAST',    'OPTOMETRY',        TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', '1000000004', 'Gulf Coast Behavioral',    'SOUTH',   'BEHAVIORAL',       TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', '1000000005', 'Pacific Pharmacy Network', 'WEST',    'PHARMACY',         TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO routing_rules (id, name, claim_type, min_amount, max_amount, region, destination, priority, active, created_at, updated_at) VALUES
    ('aaaaaaaa-0000-0000-0000-000000000001', 'High-value medical (manual review)', 'MEDICAL',           50000.00, NULL,    NULL,      'queue.medical.manual-review', 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaaa-0000-0000-0000-000000000002', 'Behavioral health',                   'BEHAVIORAL_HEALTH', NULL,     NULL,    NULL,      'queue.behavioral.standard',   20, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaaa-0000-0000-0000-000000000003', 'Dental claims',                       'DENTAL',            NULL,     NULL,    NULL,      'queue.dental.standard',       30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaaa-0000-0000-0000-000000000004', 'Vision claims',                       'VISION',            NULL,     NULL,    NULL,      'queue.vision.standard',       40, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaaa-0000-0000-0000-000000000005', 'Pharmacy west region',                'PHARMACY',          NULL,     NULL,    'WEST',    'queue.pharmacy.west',         50, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('aaaaaaaa-0000-0000-0000-000000000006', 'Default medical',                     'MEDICAL',           NULL,     NULL,    NULL,      'queue.medical.standard',     100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
