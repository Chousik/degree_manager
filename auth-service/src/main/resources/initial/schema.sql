-- USERS
CREATE TABLE IF NOT EXISTS users (
                                     username VARCHAR(50) NOT NULL PRIMARY KEY,
                                     password VARCHAR(100) NOT NULL,
                                     enabled BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS authorities (
                                           username VARCHAR(50) NOT NULL,
                                           authority VARCHAR(50) NOT NULL,
                                           CONSTRAINT fk_username FOREIGN KEY(username) REFERENCES users(username)
);
CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);
