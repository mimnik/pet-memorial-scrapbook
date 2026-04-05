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
