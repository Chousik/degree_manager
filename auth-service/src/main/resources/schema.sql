CREATE TABLE IF NOT EXISTS users (
  id SERIAL,
  username VARCHAR(45) NULL,
  password VARCHAR(45) NULL,
  enabled INT NOT NULL,
  PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS authorities (
  id SERIAL,
  username VARCHAR(45) NULL,
  authority VARCHAR(45) NULL,
  PRIMARY KEY (id));
