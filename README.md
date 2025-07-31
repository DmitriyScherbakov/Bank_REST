# Bank REST API

REST API для управления банковскими картами с использованием Spring Boot.

## Требования

- Java 17+
- Docker и Docker Compose
- PostgreSQL (или Docker)
- Maven

## Быстрый старт

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd Bank_REST
```

2. Настройте конфигурацию базы данных:
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

3. Отредактируйте `src/main/resources/application.yml` и укажите ваши параметры подключения к базе данных:
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

## Тестовые учетные записи

После запуска приложения будут доступны следующие тестовые пользователи:

1. Администратор:
   - Логин: `admin`
   - Пароль: `admin`

2. Обычные пользователи:
   - Логин: `user1` / Пароль: `user123`
   - Логин: `user2` / Пароль: `user123`

## API Документация

После запуска приложения документация Swagger будет доступна по адресу:
- http://localhost:8080/swagger-ui.html