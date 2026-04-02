USE pet_memorial_db;

-- password: 123456
INSERT INTO users (username, email, password, display_name, avatar_url, bio, role, created_at, updated_at)
VALUES ('demo', 'demo@example.com', '$2a$10$7QJQfVh93B4A2f2eM5X7yeIeV4A9z7fYwQf9XQbY17LJg8E5fSLfG', '演示用户', NULL, '这是一个用于演示的社区账号。', 'ROLE_USER', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 管理员账号: username=0001, password=123456
INSERT INTO users (username, email, password, display_name, avatar_url, bio, role, created_at, updated_at)
VALUES ('0001', '0001@pet-memorial.local', '$2a$10$7QJQfVh93B4A2f2eM5X7yeIeV4A9z7fYwQf9XQbY17LJg8E5fSLfG', '系统管理员', NULL, '默认管理员账号', 'ROLE_ADMIN', NOW(), NOW())
ON DUPLICATE KEY UPDATE role = 'ROLE_ADMIN', updated_at = NOW();

INSERT INTO community_topics (name, description, created_by_username, created_at, updated_at)
VALUES ('纪念接力', '分享与你家宝贝的纪念故事', 'demo', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO pets (
  name, species, breed, gender, birth_date, memorial_date, avatar_url, description,
  owner_username, is_public, share_token, created_at, updated_at
)
VALUES (
  '可乐', '猫', '英短', '公', '2018-05-20', NULL, NULL, '一只性格温柔的小伙伴',
  'demo', b'1', 'demopublictoken1234567890', NOW(), NOW()
)
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO memory_entries (pet_id, title, content, event_date, location, image_url, created_at, updated_at)
SELECT p.id, '第一次见面', '那天抱回家的时候，它一直在打呼噜。', '2018-06-01', '广州', NULL, NOW(), NOW()
FROM pets p
WHERE p.share_token = 'demopublictoken1234567890'
LIMIT 1;

INSERT INTO community_posts (
  pet_id, topic_id, author_username, title, content, image_url, mood_tag, narrative_mode,
  pet_voice, relay_enabled, like_count, comment_count, created_at, updated_at
)
SELECT p.id, t.id, 'demo', '纪念接力：写给可乐的话', '欢迎在评论区留下你和宠物的温暖片段。', NULL, 'RAINBOW', 'MEMORIAL',
  b'0', b'1', 0, 0, NOW(), NOW()
FROM pets p
LEFT JOIN community_topics t ON t.name = '纪念接力'
WHERE p.share_token = 'demopublictoken1234567890'
LIMIT 1;

INSERT INTO community_comments (post_id, author_username, content, relay_reply, created_at, updated_at)
SELECT cp.id, 'demo', '我先接力：愿每一段陪伴都被好好记住。', b'1', NOW(), NOW()
FROM community_posts cp
ORDER BY cp.id DESC
LIMIT 1;

INSERT INTO user_account_appeals (
  username, appeal_type, details, status, handled_by_username, handled_at, handle_note, created_at, updated_at
)
VALUES (
  'demo', 'ACCOUNT_SECURITY', '账号疑似被盗，请管理员协助核查。', 'PENDING', NULL, NULL, NULL, NOW(), NOW()
);
