-- Seed active apartment towers for internal delivery at Vinhomes Grand Park
-- (formerly District 9), starting with the completed The Rainbow subdivision.
-- Re-running this seed refreshes tower metadata without overriding admin status.

INSERT INTO residential_building (
    code,
    name,
    description,
    display_order,
    is_active,
    created_at,
    updated_at
) VALUES
    ('S1.01', 'Tòa S1.01 - The Rainbow', 'Cụm S1, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 101, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S1.02', 'Tòa S1.02 - The Rainbow', 'Cụm S1, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 102, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S1.03', 'Tòa S1.03 - The Rainbow', 'Cụm S1, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 103, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S1.05', 'Tòa S1.05 - The Rainbow', 'Cụm S1, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 105, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S1.06', 'Tòa S1.06 - The Rainbow', 'Cụm S1, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 106, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S1.07', 'Tòa S1.07 - The Rainbow', 'Cụm S1, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 107, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S2.01', 'Tòa S2.01 - The Rainbow', 'Cụm S2, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 201, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S2.02', 'Tòa S2.02 - The Rainbow', 'Cụm S2, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 202, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S2.03', 'Tòa S2.03 - The Rainbow', 'Cụm S2, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 203, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S2.05', 'Tòa S2.05 - The Rainbow', 'Cụm S2, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 205, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S3.01', 'Tòa S3.01 - The Rainbow', 'Cụm S3, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 301, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S3.02', 'Tòa S3.02 - The Rainbow', 'Cụm S3, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 302, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S3.03', 'Tòa S3.03 - The Rainbow', 'Cụm S3, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 303, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S3.05', 'Tòa S3.05 - The Rainbow', 'Cụm S3, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 305, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S5.01', 'Tòa S5.01 - The Rainbow', 'Cụm S5, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 501, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S5.02', 'Tòa S5.02 - The Rainbow', 'Cụm S5, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 502, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('S5.03', 'Tòa S5.03 - The Rainbow', 'Cụm S5, phân khu The Rainbow, Vinhomes Grand Park (Quận 9 cũ)', 503, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    updated_at = CURRENT_TIMESTAMP;
