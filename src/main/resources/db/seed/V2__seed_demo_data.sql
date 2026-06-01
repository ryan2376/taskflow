-- ============================================================================
--  V2__seed_demo_data.sql  —  DEV-ONLY demo data.
--
--  "Behind a flag": this file lives in src/main/resources/db/seed, NOT in
--  db/migration. Flyway only sees it because application-dev.yml adds
--  `classpath:db/seed` to spring.flyway.locations. The base/prod config lists
--  ONLY classpath:db/migration, so production never runs this seed.
--
--  Why fixed UUIDs + ON CONFLICT DO NOTHING? So the seed is deterministic
--  (tasks can reference known category/user IDs) and safe to re-run against a
--  fresh dev database without errors or duplicates.
--
--  Demo login (works after Phase 5):  demo@taskflow.com  /  Password123!
--  The password_hash below is a real BCrypt hash of that password.
-- ============================================================================

-- ---- demo user ----
INSERT INTO users (id, email, password_hash, display_name)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'demo@taskflow.com',
    '$2y$10$0XUo4juYPirX6Fba1QJxy.EGk/XEcDtK49WbhVLG3mGotpwUxeHrK', -- BCrypt("Password123!")
    'Demo User'
)
ON CONFLICT (id) DO NOTHING;

-- ---- categories owned by the demo user ----
INSERT INTO categories (id, name, color, owner_id) VALUES
    ('22222222-2222-2222-2222-222222222201', 'Work',     '#3B82F6', '11111111-1111-1111-1111-111111111111'),
    ('22222222-2222-2222-2222-222222222202', 'Personal', '#10B981', '11111111-1111-1111-1111-111111111111')
ON CONFLICT (id) DO NOTHING;

-- ---- tasks: a deliberate mix so analytics (Phase 10) has interesting numbers ----
-- Due dates are relative to seed time: some upcoming, one overdue, one done, one undated.
INSERT INTO tasks (id, title, description, status, priority, due_date, completed_at, owner_id, category_id) VALUES
    -- upcoming, high priority, Work
    ('33333333-3333-3333-3333-333333333301', 'Write project README', 'Document setup and endpoints',
        'TODO', 'HIGH', now() + interval '3 days', NULL,
        '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222201'),
    -- upcoming, low priority, Personal
    ('33333333-3333-3333-3333-333333333302', 'Buy groceries', 'Milk, eggs, coffee',
        'TODO', 'LOW', now() + interval '1 day', NULL,
        '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222202'),
    -- OVERDUE (due in the past, not done), in progress, high, Personal
    ('33333333-3333-3333-3333-333333333303', 'Submit tax documents', 'Before the deadline!',
        'IN_PROGRESS', 'HIGH', now() - interval '2 days', NULL,
        '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222202'),
    -- completed, medium, Work
    ('33333333-3333-3333-3333-333333333304', 'Read Spring docs', 'Finished the security chapter',
        'DONE', 'MEDIUM', now() - interval '3 days', now() - interval '1 day',
        '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222201'),
    -- no due date, no category, medium
    ('33333333-3333-3333-3333-333333333305', 'Plan weekend trip', NULL,
        'TODO', 'MEDIUM', NULL, NULL,
        '11111111-1111-1111-1111-111111111111', NULL)
ON CONFLICT (id) DO NOTHING;
