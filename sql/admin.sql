CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS app_page_menus (
    id BIGINT PRIMARY KEY,
    page_code VARCHAR(64) NOT NULL,
    menu_code VARCHAR(64) NOT NULL,
    menu_name VARCHAR(128) NOT NULL,
    menu_type INTEGER NOT NULL DEFAULT 1,
    icon_url VARCHAR(512),
    image_url VARCHAR(512),
    jump_type INTEGER NOT NULL DEFAULT 0,
    jump_url VARCHAR(512),
    visible_status INTEGER NOT NULL DEFAULT 1,
    sort_no INTEGER NOT NULL DEFAULT 0,
    remark VARCHAR(512),
    create_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_app_page_menus_page_menu UNIQUE (page_code, menu_code)
);

CREATE INDEX IF NOT EXISTS idx_app_page_menus_page_visible_sort
    ON app_page_menus(page_code, visible_status, sort_no ASC, id DESC);
CREATE INDEX IF NOT EXISTS idx_app_page_menus_type_visible
    ON app_page_menus(menu_type, visible_status);
CREATE INDEX IF NOT EXISTS idx_app_page_menus_name_trgm
    ON app_page_menus USING gin (menu_name gin_trgm_ops);

INSERT INTO app_page_menus (
    id, page_code, menu_code, menu_name, menu_type, jump_type, jump_url,
    visible_status, sort_no, remark
) VALUES
    (9100000000001, 'profile', 'profile_info', '个人栏', 1, 1, '/profile/info', 1, 10, '个人信息入口'),
    (9100000000002, 'profile', 'word_history', '历史词汇', 1, 1, '/word/history', 1, 20, '用户背词历史'),
    (9100000000003, 'profile', 'word_entry', '背词入口', 1, 1, '/word/study', 1, 30, '进入背词流程'),
    (9100000000004, 'profile', 'consult', '咨询', 1, 1, '/consult', 1, 40, '咨询入口'),
    (9100000000005, 'profile', 'profile_banner', '个人页 Banner', 2, 0, NULL, 1, 1, '个人页顶部运营位')
ON CONFLICT (page_code, menu_code) DO NOTHING;
