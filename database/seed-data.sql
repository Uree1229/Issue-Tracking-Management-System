INSERT OR IGNORE INTO accounts (
    login_id,
    password,
    name,
    email,
    role,
    created_at,
    is_active
) VALUES (
    'admin',
    'admin',
    'Default Admin',
    'admin@its.local',
    'ADMIN',
    CURRENT_TIMESTAMP,
    1
);

