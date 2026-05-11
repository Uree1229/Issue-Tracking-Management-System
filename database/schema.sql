PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS accounts (
    account_id INTEGER PRIMARY KEY AUTOINCREMENT,
    login_id TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    role TEXT NOT NULL CHECK (role IN ('ADMIN', 'PL', 'DEV', 'TESTER')),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

CREATE TABLE IF NOT EXISTS projects (
    project_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_account_id INTEGER NOT NULL,
    CONSTRAINT fk_projects_created_by
        FOREIGN KEY (created_by_account_id) REFERENCES accounts(account_id)
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS project_members (
    project_member_id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    account_id INTEGER NOT NULL,
    assigned_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_project_members_project_account UNIQUE (project_id, account_id),
    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id) REFERENCES projects(project_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_members_account
        FOREIGN KEY (account_id) REFERENCES accounts(account_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tags (
    tag_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    project_id INTEGER NOT NULL,
    CONSTRAINT uq_tags_project_name UNIQUE (project_id, name),
    CONSTRAINT fk_tags_project
        FOREIGN KEY (project_id) REFERENCES projects(project_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS issues (
    issue_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('NEW', 'ASSIGNED', 'FIXED', 'RESOLVED', 'CLOSED', 'REOPENED')),
    priority TEXT NOT NULL DEFAULT 'MAJOR' CHECK (priority IN ('BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR', 'TRIVIAL')),
    reporter_account_id INTEGER NOT NULL,
    assignee_account_id INTEGER,
    fixer_account_id INTEGER,
    reported_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    project_id INTEGER NOT NULL,
    CONSTRAINT fk_issues_reporter
        FOREIGN KEY (reporter_account_id) REFERENCES accounts(account_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_issues_assignee
        FOREIGN KEY (assignee_account_id) REFERENCES accounts(account_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_issues_fixer
        FOREIGN KEY (fixer_account_id) REFERENCES accounts(account_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_issues_project
        FOREIGN KEY (project_id) REFERENCES projects(project_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    comment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    content TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    author_account_id INTEGER NOT NULL,
    issue_id INTEGER NOT NULL,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_account_id) REFERENCES accounts(account_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_comments_issue
        FOREIGN KEY (issue_id) REFERENCES issues(issue_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS issue_histories (
    history_id INTEGER PRIMARY KEY AUTOINCREMENT,
    issue_id INTEGER NOT NULL,
    changed_by_account_id INTEGER NOT NULL,
    changed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_histories_issue
        FOREIGN KEY (issue_id) REFERENCES issues(issue_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_histories_changed_by
        FOREIGN KEY (changed_by_account_id) REFERENCES accounts(account_id)
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS issue_deltas (
    delta_id INTEGER PRIMARY KEY AUTOINCREMENT,
    history_id INTEGER NOT NULL UNIQUE,
    old_title TEXT,
    new_title TEXT,
    old_content TEXT,
    new_content TEXT,
    old_priority TEXT CHECK (old_priority IS NULL OR old_priority IN ('BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR', 'TRIVIAL')),
    new_priority TEXT CHECK (new_priority IS NULL OR new_priority IN ('BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR', 'TRIVIAL')),
    old_status TEXT CHECK (old_status IS NULL OR old_status IN ('NEW', 'ASSIGNED', 'FIXED', 'RESOLVED', 'CLOSED', 'REOPENED')),
    new_status TEXT CHECK (new_status IS NULL OR new_status IN ('NEW', 'ASSIGNED', 'FIXED', 'RESOLVED', 'CLOSED', 'REOPENED')),
    CONSTRAINT fk_deltas_history
        FOREIGN KEY (history_id) REFERENCES issue_histories(history_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS issue_tags (
    issue_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (issue_id, tag_id),
    CONSTRAINT fk_issue_tags_issue
        FOREIGN KEY (issue_id) REFERENCES issues(issue_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_issue_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags(tag_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_issues_project_status_priority
    ON issues (project_id, status, priority);

CREATE INDEX IF NOT EXISTS idx_issues_reported_at
    ON issues (reported_at);

CREATE INDEX IF NOT EXISTS idx_comments_issue_created_at
    ON comments (issue_id, created_at);

CREATE INDEX IF NOT EXISTS idx_issue_histories_issue_changed_at
    ON issue_histories (issue_id, changed_at);

CREATE INDEX IF NOT EXISTS idx_issue_histories_changed_by
    ON issue_histories (changed_by_account_id, changed_at);

CREATE INDEX IF NOT EXISTS idx_project_members_account
    ON project_members (account_id);

CREATE INDEX IF NOT EXISTS idx_project_members_project
    ON project_members (project_id);
