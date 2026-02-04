# Документация серверного приложения на Java Spring

## Обзор

Серверное приложение представляет собой REST API для управления накладными и товарными позициями. Приложение построено на Java Spring Boot и предоставляет полный CRUD-функционал для работы с накладными, включая интеграцию с системой 1С.

## Технологический стек

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL** (запускается через Docker)
- **Lombok**
- **Swagger/OpenAPI 3**
- **Docker & Docker Compose**

## Запуск приложения

### 1. Запуск базы данных

Перед запуском приложения необходимо запустить PostgreSQL через Docker Compose:

```bash
docker-compose up -d
```

### 2. Запуск приложения

Соберите и запустите Spring Boot приложение:

```bash
# Сборка проекта (если используется Maven)
mvn clean package

# Запуск приложения
java -jar target/your-application.jar
```

Или запустите через IDE напрямую класс `DataCollectionTerminalApplication` (или аналогичный).

## Конфигурация

Основные настройки приложения в `application.yml`/`application.properties`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/terminal_db
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## API Endpoints

### Накладные

#### Создание накладной
```
POST /api/v1/invoices
```

**Описание:** Создает новую накладную с автоматически сгенерированным номером.

**Ответ:**
- `201 Created` - возвращает созданную накладную

#### Получение накладной по номеру
```
GET /api/v1/invoices/{invoiceNumber}
```

**Параметры:**
- `invoiceNumber` (path) - номер накладной

**Ответ:**
- `200 OK` - возвращает данные накладной
- `404 Not Found` - если накладная не найдена

#### Получение всех накладных
```
GET /api/v1/invoices
```

**Ответ:**
- `200 OK` - список всех накладных

#### Отправка накладной в 1С
```
POST /api/v1/invoices/{invoiceNumber}/send
```

**Параметры:**
- `invoiceNumber` (path) - номер накладной

**Описание:** Отправляет накладную в систему 1С и обновляет её статус.

**Ответ:**
- `200 OK` - обновленная накладная

#### Удаление накладной
```
DELETE /api/v1/invoices/{invoiceNumber}
```

**Параметры:**
- `invoiceNumber` (path) - номер накладной

**Ответ:**
- `204 No Content` - при успешном удалении
- `404 Not Found` - если накладная не найдена

### Товарные позиции

#### Добавление позиции в накладную
```
POST /api/v1/invoices/{invoiceNumber}/items
```

**Параметры:**
- `invoiceNumber` (path) - номер накладной
- **Body:** `AddNewProductItemRequest`

**Пример запроса:**
```json
{
  "barcode": "1234567890123",
  "quantity": 10
}
```

**Ответ:**
- `201 Created` - созданная позиция

#### Получение всех позиций накладной
```
GET /api/v1/invoices/{invoiceNumber}/items
```

**Параметры:**
- `invoiceNumber` (path) - номер накладной

**Ответ:**
- `200 OK` - список всех позиций накладной

#### Изменение позиции в накладной
```
PUT /api/v1/invoices/{invoiceNumber}/items
```

**Параметры:**
- `invoiceNumber` (path) - номер накладной
- **Body:** `UpdateProductItemRequestOnInvoice`

**Пример запроса:**
```json
{
  "productItemId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 15
}
```

**Ответ:**
- `200 OK` - обновленная позиция

#### Удаление позиции из накладной
```
DELETE /api/v1/invoices/{invoiceNumber}/items/{productItemId}
```

**Параметры:**
- `invoiceNumber` (path) - номер накладной
- `productItemId` (path) - UUID позиции

**Ответ:**
- `204 No Content` - при успешном удалении

## Модели данных

### InvoiceResponse
```java
public record InvoiceResponse(
    UUID id,
    Long invoiceNumber,          // Номер накладной
    InvoiceStatus status,        // Статус (например, DRAFT, SENT)
    Integer totalAmount,         // Общая сумма
    Integer itemsCount,          // Количество позиций
    LocalDateTime sentAt,        // Дата отправки в 1С
    LocalDateTime createdAt,     // Дата создания
    List<ProductItemResponse> items  // Список позиций
) {}
```

### ProductItemResponse
```java
public record ProductItemResponse(
    UUID id,
    UUID invoiceId,              // ID накладной
    Long invoiceNumber,          // Номер накладной
    String barcode,              // Штрихкод товара
    String wareTitle,            // Наименование товара
    String wareCode1c,           // Код товара в 1С
    Integer quantity,            // Количество
    Integer unitPrice,           // Цена за единицу
    Integer totalPrice,          // Общая стоимость
    Boolean isWeighty            // Весовой товар
) {}
```

## Интеграция с 1С

Приложение поддерживает отправку накладных в систему 1С. При отправке формируется запрос в формате:

### Invoice1CRequest
```json
{
  "invoice_number": 123456,
  "total_amount": 15000,
  "items_count": 3,
  "items": [
    {
      "ware_1c_code": "001234",
      "barcode": "1234567890123",
      "ware_title": "Товар 1",
      "quantity": 10,
      "unit_price": 1000,
      "total_price": 10000,
      "is_weighty": false
    }
  ]
}
```

## Валидация

- **Штрихкод:** обязательное поле (не пустое)
- **Количество:** должно быть больше 0
- **ID позиции:** обязателен при обновлении

## Логирование

Приложение использует SLF4J для логирования. Все REST-запросы логируются с уровнем INFO.

## Документация API

После запуска приложения доступна Swagger-документация:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI spec:** http://localhost:8080/v3/api-docs

## Обработка ошибок

Приложение возвращает стандартные HTTP-статусы:
- `200 OK` - успешный запрос
- `201 Created` - ресурс создан
- `204 No Content` - успешное удаление
- `400 Bad Request` - ошибка валидации
- `404 Not Found` - ресурс не найден
- `500 Internal Server Error` - внутренняя ошибка сервера

## Примеры использования

### 1. Создание накладной и добавление позиций

```bash
# 1. Создать накладную
curl -X POST http://localhost:8080/api/v1/invoices

# 2. Добавить позицию (предположим, номер накладной = 1001)
curl -X POST http://localhost:8080/api/v1/invoices/1001/items \
  -H "Content-Type: application/json" \
  -d '{"barcode": "1234567890123", "quantity": 5}'

# 3. Отправить накладную в 1С
curl -X POST http://localhost:8080/api/v1/invoices/1001/send
```

### 2. Получение информации

```bash
# Получить все накладные
curl http://localhost:8080/api/v1/invoices

# Получить конкретную накладную
curl http://localhost:8080/api/v1/invoices/1001

# Получить позиции накладной
curl http://localhost:8080/api/v1/invoices/1001/items
```

## Docker Compose файл (пример)

`docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: terminal-postgres
    environment:
      POSTGRES_DB: terminal_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

## Зависимости проекта (Maven)

Основные зависимости:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `springdoc-openapi-starter-webmvc-ui`
- `postgresql`
- `lombok`

## Разработка

### Структура пакетов
```
com.hamming.data.collection.terminal
├── core
│   ├── web          # Контроллеры
│   ├── service      # Бизнес-логика
│   ├── dto          # Data Transfer Objects
│   │   ├── request
│   │   └── response
│   └── model        # Сущности БД
└── config          # Конфигурации
```

### Тестирование
```bash
# Запуск всех тестов
mvn test

# Запуск с покрытием
mvn jacoco:report
```

## Контакты и поддержка
TG - @hamm1ng
