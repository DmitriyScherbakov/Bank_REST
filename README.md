# Bank REST API

REST API для управления банковскими картами. Позволяет создавать карты, управлять ими и совершать переводы между картами.

## Технологии

- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- PostgreSQL
- Liquibase
- Docker & Docker Compose
- Swagger/OpenAPI

## Быстрый старт

### Предварительные требования

- Java 17+
- Docker и Docker Compose
- Git

### Установка и запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/your-username/Bank_REST.git
cd Bank_REST
```

2. Настройте конфигурацию:
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

3. Отредактируйте `src/main/resources/application.yml` и укажите ваши настройки:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database_name
    username: your_username
    password: your_password
```

4. Запустите приложение:
```bash
./mvnw spring-boot:run
```

Приложение будет доступно по адресу: http://localhost:8080

### Docker Compose

Для запуска базы данных используется Docker Compose:
```bash
docker-compose up -d
```

## Тестовые учетные записи

После запуска приложения доступны следующие тестовые пользователи:

1. Администратор:
   - Логин: `admin`
   - Пароль: `admin`
   - Права: полный доступ к API

2. Обычные пользователи:
   - Логин: `user1` / Пароль: `user123`
   - Логин: `user2` / Пароль: `user123`
   - Права: управление своими картами

## API Endpoints

### Аутентификация

- POST `/api/auth/register` - регистрация нового пользователя
- POST `/api/auth/login` - вход в систему, получение JWT токена

### Карты

- GET `/api/cards/my` - получение списка своих карт
- POST `/api/cards` - создание новой карты
- PUT `/api/cards/{id}/block` - блокировка карты
- POST `/api/cards/transfer` - перевод между картами

### Администрирование

- GET `/api/admin/cards` - получение списка всех карт (только для админа)
- GET `/api/admin/users` - получение списка пользователей (только для админа)

Полная документация API доступна через Swagger UI: http://localhost:8080/swagger-ui.html

## Основные функции

1. Управление пользователями:
   - Регистрация
   - Аутентификация (JWT)
   - Разграничение прав (USER/ADMIN)

2. Управление картами:
   - Создание карт (до 5 карт на пользователя)
   - Просмотр своих карт
   - Блокировка карт
   - Переводы между картами

3. Безопасность:
   - JWT аутентификация
   - Шифрование номеров карт
   - Валидация входных данных

## Разработка

### База данных

Миграции базы данных управляются через Liquibase. Файлы миграций находятся в:
```
src/main/resources/db/migration/
```

### Тестирование

Запуск тестов:
```bash
./mvnw test
```

## Безопасность

- Пароли хешируются с использованием BCrypt
- Номера карт шифруются перед сохранением в базу
- Все API-endpoints защищены Spring Security
- Используется JWT для аутентификации

## Мониторинг

Приложение включает базовый мониторинг через Spring Boot Actuator:
- Health check: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics