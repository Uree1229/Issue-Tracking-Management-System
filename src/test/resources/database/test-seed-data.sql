INSERT INTO accounts (
    account_id,
    login_id,
    password,
    name,
    email,
    role,
    created_at,
    is_active
) VALUES
    (1, 'admin', 'admin', 'Default Admin', 'admin@its.test', 'ADMIN', '2026-01-01T00:00:00', 1),
    (2, 'dev1', 'password', 'Dev One', 'dev1@its.test', 'DEV', '2026-01-01T00:00:00', 1),
    (3, 'tester1', 'password', 'Tester One', 'tester1@its.test', 'TESTER', '2026-01-01T00:00:00', 1),
    (4, 'pl1', 'password', 'Project Leader One', 'pl1@its.test', 'PL', '2026-01-01T00:00:00', 1),

INSERT INTO projects (
    project_id,
    name,
    description,
    created_at,
    created_by_account_id
) VALUES (
    1,
    'Test Project',
    'Project for automated tests',
    '2026-01-01T00:00:00',
    1
);

INSERT INTO tags (
    tag_id,
    name,
    description,
    project_id
) VALUES
    (1, 'backend', 'Backend issues', 1),
    (2, 'ui', 'UI issues', 1);

INSERT INTO issues (
    issue_id,
    title,
    description,
    status,
    priority,
    reporter_account_id,
    assignee_account_id,
    fixer_account_id,
    reported_at,
    last_modified_at,
    project_id
) VALUES
    (1, 'Login fails', 'Login fails with valid credentials', 'NEW', 'MAJOR', 3, NULL, NULL, '2026-01-02T10:00:00', '2026-01-02T10:00:00', 1),
    (2, 'Button typo', 'Submit button has a typo', 'ASSIGNED', 'MINOR', 3, 2, NULL, '2026-01-03T10:00:00', '2026-01-03T10:00:00', 1);

INSERT INTO issue_tags (issue_id, tag_id) VALUES
    (1, 1),
    (2, 2);
