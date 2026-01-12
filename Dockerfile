FROM postgres:18

ENV DEBIAN_FRONTEND=noninteractive

# Устанавливаем pg_cron (пакет в репозиториях Debian/Ubuntu)
RUN apt-get update \
 && apt-get install -y --no-install-recommends postgresql-18-cron \
 && rm -rf /var/lib/apt/lists/*

# Включим pg_cron в конфигурации до первого запуска сервера
# (записываем в sample — при инициализации контейнера postgresql использует этот файл)
RUN echo "shared_preload_libraries = 'pg_cron'" >> /usr/share/postgresql/postgresql.conf.sample \
 && echo "cron.database_name = 'task_app'" >> /usr/share/postgresql/postgresql.conf.sample
