CREATE DATABASE IF NOT EXISTS pet_memorial_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pet_memorial_db;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(120) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL,
  display_name VARCHAR(100),
  avatar_url VARCHAR(500),
  bio VARCHAR(1000),
  role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
  account_frozen BIT NOT NULL DEFAULT 0,
  posting_restricted BIT NOT NULL DEFAULT 0,
  warning_count INT NOT NULL DEFAULT 0,
  admin_note VARCHAR(1000),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS pets (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  species VARCHAR(100),
  breed VARCHAR(100),
  gender VARCHAR(20),
  birth_date DATE,
  memorial_date DATE,
  age INT,
  weight VARCHAR(20),
  marital_status VARCHAR(20),
  skills VARCHAR(500),
  dietary_habits VARCHAR(1000),
  physical_condition VARCHAR(1000),
  avatar_url VARCHAR(500),
  description VARCHAR(2000),
  owner_username VARCHAR(100) NOT NULL,
  is_public BIT NOT NULL DEFAULT 0,
  share_token VARCHAR(64) NOT NULL UNIQUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_pets_owner (owner_username),
  CONSTRAINT fk_pets_owner_username FOREIGN KEY (owner_username) REFERENCES users(username)
);

CREATE TABLE IF NOT EXISTS memory_entries (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content VARCHAR(5000) NOT NULL,
  event_date DATE,
  location VARCHAR(200),
  image_url VARCHAR(500),
  video_url VARCHAR(500),
  video_cover_url VARCHAR(500),
  video_duration_seconds INT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_memory_pet_event (pet_id, event_date DESC, id DESC),
  CONSTRAINT fk_memory_pet FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pet_archive_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL,
  archive_type VARCHAR(30) NOT NULL,
  title VARCHAR(120) NOT NULL,
  metric_value VARCHAR(100),
  unit VARCHAR(20),
  event_date DATE,
  note VARCHAR(1000),
  reminder_enabled BIT NOT NULL DEFAULT 0,
  reminder_at DATETIME,
  reminder_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  reminder_completed_at DATETIME,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_pet_archive_pet_event (pet_id, event_date DESC, id DESC),
  INDEX idx_pet_archive_reminder (reminder_enabled, reminder_status, reminder_at),
  CONSTRAINT fk_pet_archive_pet FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS community_topics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(500),
  created_by_username VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS community_posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id BIGINT NOT NULL,
  topic_id BIGINT,
  author_username VARCHAR(100) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content VARCHAR(5000) NOT NULL,
  image_url VARCHAR(500),
  video_url VARCHAR(500),
  video_cover_url VARCHAR(500),
  video_duration_seconds INT,
  mood_tag VARCHAR(20) NOT NULL DEFAULT 'SUNNY',
  narrative_mode VARCHAR(20) NOT NULL DEFAULT 'DAILY',
  pet_voice BIT NOT NULL DEFAULT 0,
  relay_enabled BIT NOT NULL DEFAULT 0,
  like_count INT NOT NULL DEFAULT 0,
  comment_count INT NOT NULL DEFAULT 0,
  hidden_by_admin BIT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_community_posts_feed (created_at DESC, id DESC),
  INDEX idx_community_posts_author (author_username, created_at DESC),
  INDEX idx_community_posts_topic (topic_id, created_at DESC),
  CONSTRAINT fk_community_post_pet FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  CONSTRAINT fk_community_post_topic FOREIGN KEY (topic_id) REFERENCES community_topics(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS community_comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  author_username VARCHAR(100) NOT NULL,
  content VARCHAR(1000) NOT NULL,
  relay_reply BIT NOT NULL DEFAULT 0,
  hidden_by_admin BIT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_community_comments_post (post_id, created_at ASC, id ASC),
  CONSTRAINT fk_community_comment_post FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS community_post_likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  username VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uk_post_like_user UNIQUE (post_id, username),
  INDEX idx_community_likes_post (post_id),
  CONSTRAINT fk_community_like_post FOREIGN KEY (post_id) REFERENCES community_posts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_follows (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  follower_username VARCHAR(100) NOT NULL,
  following_username VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT uk_user_follows_pair UNIQUE (follower_username, following_username),
  INDEX idx_user_follows_follower (follower_username),
  INDEX idx_user_follows_following (following_username),
  CONSTRAINT fk_user_follows_follower FOREIGN KEY (follower_username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_user_follows_following FOREIGN KEY (following_username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sender_username VARCHAR(100) NOT NULL,
  receiver_username VARCHAR(100) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  read_by_receiver BIT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_user_messages_sender_receiver (sender_username, receiver_username, created_at DESC),
  INDEX idx_user_messages_receiver_read (receiver_username, read_by_receiver),
  CONSTRAINT fk_user_messages_sender FOREIGN KEY (sender_username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_user_messages_receiver FOREIGN KEY (receiver_username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS content_reports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporter_username VARCHAR(100) NOT NULL,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  reason VARCHAR(120) NOT NULL,
  details VARCHAR(2000),
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  handled_by_username VARCHAR(100),
  handled_at DATETIME,
  handle_note VARCHAR(1000),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_content_reports_status (status),
  INDEX idx_content_reports_reporter (reporter_username),
  CONSTRAINT fk_content_reports_reporter FOREIGN KEY (reporter_username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_content_reports_handler FOREIGN KEY (handled_by_username) REFERENCES users(username) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS user_account_appeals (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  appeal_type VARCHAR(40) NOT NULL,
  details VARCHAR(2000) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  handled_by_username VARCHAR(100),
  handled_at DATETIME,
  handle_note VARCHAR(1000),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_user_account_appeals_status (status),
  INDEX idx_user_account_appeals_username (username),
  CONSTRAINT fk_user_account_appeals_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
  CONSTRAINT fk_user_account_appeals_handler FOREIGN KEY (handled_by_username) REFERENCES users(username) ON DELETE SET NULL
);

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
  name, species, breed, gender, birth_date, memorial_date, age, weight, marital_status, skills, dietary_habits, physical_condition, avatar_url, description,
  owner_username, is_public, share_token, created_at, updated_at
)
VALUES (
  '可乐', '猫', '英短', '公', '2018-05-20', NULL, 8, '4.8kg', '未婚', '接飞盘、握手', '偏爱鸡胸肉和湿粮', '状态稳定，食欲良好', NULL, '一只性格温柔的小伙伴',
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
SELECT p.id, t.id, 'demo', '今天是晴朗的一天', '可乐在窗边晒太阳，打呼噜一整下午。', NULL, 'SUNNY', 'DAILY',
  b'0', b'1', 0, 0, NOW(), NOW()
FROM pets p
LEFT JOIN community_topics t ON t.name = '纪念接力'
WHERE p.share_token = 'demopublictoken1234567890'
LIMIT 1;

INSERT INTO user_account_appeals (
  username, appeal_type, details, status, handled_by_username, handled_at, handle_note, created_at, updated_at
)
VALUES (
  'demo', 'PASSWORD_RESET', '我忘记了密码，想申请重置。', 'PENDING', NULL, NULL, NULL, NOW(), NOW()
)
ON DUPLICATE KEY UPDATE updated_at = NOW();
