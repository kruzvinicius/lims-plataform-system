CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL
);

CREATE TABLE users_aud
(
    id       BIGINT NOT NULL,
    rev      BIGINT NOT NULL,
    revtype  SMALLINT,
    username VARCHAR(255),
    password VARCHAR(255),
    role     VARCHAR(50),
    PRIMARY KEY (id, rev),
    CONSTRAINT fk_users_aud_revisions FOREIGN KEY (rev) REFERENCES revisions (id)
);