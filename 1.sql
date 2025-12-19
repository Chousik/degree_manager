BEGIN;

CREATE TABLE IF NOT EXISTS users (
                                     id              BIGSERIAL PRIMARY KEY,
                                     email           TEXT NOT NULL UNIQUE,
                                     hashed_password TEXT NOT NULL,
                                     name            TEXT,
                                     created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS quizzes (
                                       id          BIGSERIAL PRIMARY KEY,
                                       owner_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       title       TEXT NOT NULL,
                                       description TEXT,
                                       is_public   BOOLEAN NOT NULL DEFAULT FALSE,
                                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_quizzes_owner_id ON quizzes(owner_id);

CREATE TABLE IF NOT EXISTS questions (
                                         id            BIGSERIAL PRIMARY KEY,
                                         quiz_id       BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
                                         type          TEXT NOT NULL,
                                         prompt        TEXT NOT NULL,
                                         time_limit_ms INTEGER NOT NULL DEFAULT 20000,
                                         points        INTEGER NOT NULL DEFAULT 1000,
                                         position      INTEGER NOT NULL,
                                         created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         CONSTRAINT uq_questions_quiz_position UNIQUE (quiz_id, position)
);

CREATE INDEX IF NOT EXISTS idx_questions_quiz_id ON questions(quiz_id);

CREATE TABLE IF NOT EXISTS choices (
                                       id          BIGSERIAL PRIMARY KEY,
                                       question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                                       text        TEXT NOT NULL,
                                       is_correct  BOOLEAN NOT NULL DEFAULT FALSE,
                                       position    INTEGER NOT NULL,
                                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                                       CONSTRAINT uq_choices_question_position UNIQUE (question_id, position)
);

CREATE INDEX IF NOT EXISTS idx_choices_question_id ON choices(question_id);

CREATE TABLE IF NOT EXISTS game_sessions (
                                             id         BIGSERIAL PRIMARY KEY,
                                             quiz_id    BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE RESTRICT,
                                             host_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                                             pin        TEXT NOT NULL UNIQUE,
                                             status     TEXT NOT NULL DEFAULT 'lobby',
                                             started_at TIMESTAMPTZ,
                                             ended_at   TIMESTAMPTZ,
                                             created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                             updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_game_sessions_quiz_id ON game_sessions(quiz_id);
CREATE INDEX IF NOT EXISTS idx_game_sessions_host_id ON game_sessions(host_id);

-- OPTIONAL (for storing answers/history). 
-- CREATE TABLE IF NOT EXISTS session_answers (
--   id              BIGSERIAL PRIMARY KEY,
--   session_id      BIGINT NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
--   question_id     BIGINT NOT NULL REFERENCES questions(id) ON DELETE RESTRICT,
--   player_nickname TEXT NOT NULL,
--   choice_id       BIGINT REFERENCES choices(id) ON DELETE SET NULL,
--   is_correct      BOOLEAN NOT NULL,
--   latency_ms      INTEGER,
--   created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
-- );
-- CREATE INDEX IF NOT EXISTS idx_session_answers_session_id ON session_answers(session_id);
-- CREATE INDEX IF NOT EXISTS idx_session_answers_question_id ON session_answers(question_id);

COMMIT;
