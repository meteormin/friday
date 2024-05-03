CREATE SEQUENCE IF NOT EXISTS auth_user_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS file_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS host_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS login_history_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS search_file_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS search_seq START WITH 1 INCREMENT BY 50;

CREATE TYPE SocialProvider AS ENUM ('GOOGLE', 'NAVER', 'KAKAO', 'NONE');

CREATE TYPE UserRole AS ENUM ('ADMIN', 'USER', 'MANAGER');

CREATE TABLE auth_user
(
    id         BIGINT       NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    sns_id     VARCHAR(255),
    provider   SocialProvider DEFAULT 'NONE' NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    role       UserRole DEFAULT 'USER' NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_auth_user PRIMARY KEY (id)
);

CREATE TABLE file
(
    id          BIGINT       NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    mime_type   VARCHAR(50)  NOT NULL,
    size        BIGINT       NOT NULL,
    path        VARCHAR(255) NOT NULL,
    origin_name VARCHAR(255) NOT NULL,
    conv_name   VARCHAR(255) NOT NULL,
    extension   VARCHAR(20)  NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    user_id     BIGINT,
    CONSTRAINT pk_file PRIMARY KEY (id)
);

CREATE TABLE host
(
    id          BIGINT  NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    host        VARCHAR(255),
    summary     VARCHAR(255),
    description VARCHAR(255),
    path        VARCHAR(255),
    publish     BOOLEAN NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    user_id     BIGINT,
    CONSTRAINT pk_host PRIMARY KEY (id)
);

CREATE TABLE login_history
(
    id          BIGINT  NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    ip          VARCHAR(20),
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    user_id     BIGINT,
    CONSTRAINT pk_login_history PRIMARY KEY (id)
);

CREATE TABLE search
(
    id          BIGINT       NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    query_key   VARCHAR(255),
    query       VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    publish     BOOLEAN      NOT NULL,
    views       INTEGER      NOT NULL,
    deleted_at  TIMESTAMP WITHOUT TIME ZONE,
    short_url   VARCHAR(255),
    host_id     BIGINT,
    CONSTRAINT pk_search PRIMARY KEY (id)
);

CREATE TABLE search_file
(
    id         BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    file_id    BIGINT,
    search_id  BIGINT,
    CONSTRAINT pk_search_file PRIMARY KEY (id)
);

ALTER TABLE auth_user
    ADD CONSTRAINT uc_auth_user_email UNIQUE (email);

ALTER TABLE search_file
    ADD CONSTRAINT uc_search_file_file UNIQUE (file_id);

ALTER TABLE search_file
    ADD CONSTRAINT uc_search_file_search UNIQUE (search_id);

ALTER TABLE file
    ADD CONSTRAINT FK_FILE_ON_USER FOREIGN KEY (user_id) REFERENCES auth_user (id);

ALTER TABLE host
    ADD CONSTRAINT FK_HOST_ON_USER FOREIGN KEY (user_id) REFERENCES auth_user (id);

ALTER TABLE login_history
    ADD CONSTRAINT FK_LOGIN_HISTORY_ON_USER FOREIGN KEY (user_id) REFERENCES auth_user (id);

ALTER TABLE search_file
    ADD CONSTRAINT FK_SEARCH_FILE_ON_FILE FOREIGN KEY (file_id) REFERENCES file (id);

ALTER TABLE search_file
    ADD CONSTRAINT FK_SEARCH_FILE_ON_SEARCH FOREIGN KEY (search_id) REFERENCES search (id);

ALTER TABLE search
    ADD CONSTRAINT FK_SEARCH_ON_HOST FOREIGN KEY (host_id) REFERENCES host (id);
