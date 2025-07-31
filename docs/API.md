# API Documentation

## Аутентификация

### Регистрация

```http
POST /api/auth/register
```

Request:
```json
{
  "username": "user1",
  "password": "password123",
  "email": "user1@example.com"
}
```

Response:
```json
{
  "message": "User registered successfully"
}
```

### Вход

```http
POST /api/auth/login
```

Request:
```json
{
  "username": "user1",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "user1",
  "role": "USER"
}
```

## Карты

### Получение списка карт

```http
GET /api/cards/my
```

Response:
```json
[
  {
    "id": 1,
    "maskedCardNumber": "4000 **** **** 1234",
    "cardHolder": "USER NAME",
    "expiryDate": "2025-12-31",
    "status": "ACTIVE",
    "balance": 1000.00
  }
]
```

### Создание карты

```http
POST /api/cards
```

Request:
```json
{
  "cardHolder": "USER NAME"
}
```

Response:
```json
{
  "id": 1,
  "maskedCardNumber": "4000 **** **** 1234",
  "cardHolder": "USER NAME",
  "expiryDate": "2025-12-31",
  "status": "ACTIVE",
  "balance": 0.00
}
```

### Блокировка карты

```http
PUT /api/cards/{cardId}/block
```

Response:
```json
{
  "message": "Card blocked successfully"
}
```

### Перевод между картами

```http
POST /api/cards/transfer
```

Request:
```json
{
  "fromCardId": 1,
  "toCardId": 2,
  "amount": 100.00
}
```

Response:
```json
{
  "message": "Transfer completed successfully"
}
```

## Админ API

### Получение списка всех карт

```http
GET /api/admin/cards
```

Response:
```json
{
  "content": [
    {
      "id": 1,
      "maskedCardNumber": "4000 **** **** 1234",
      "cardHolder": "USER NAME",
      "expiryDate": "2025-12-31",
      "status": "ACTIVE",
      "balance": 1000.00
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

## Коды ошибок

- 400 Bad Request - Неверные входные данные
- 401 Unauthorized - Требуется аутентификация
- 403 Forbidden - Недостаточно прав
- 404 Not Found - Ресурс не найден
- 409 Conflict - Конфликт (например, превышен лимит карт)
- 500 Internal Server Error - Внутренняя ошибка сервера

## Заголовки

Для авторизованных запросов необходимо передавать JWT токен в заголовке:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```