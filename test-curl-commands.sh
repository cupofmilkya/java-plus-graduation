#!/bin/bash

# Цвета для вывода
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Тестирование EWM Plus через Gateway  ${NC}"
echo -e "${BLUE}========================================${NC}"

# Базовый URL Gateway
GATEWAY_URL="http://localhost:8080"

# Функция для вывода результата
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

# Функция для выполнения запроса
execute_request() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4

    echo -e "\n${YELLOW}📌 $description${NC}"
    echo -e "${BLUE}➜ $method $url${NC}"

    if [ -n "$data" ]; then
        echo -e "${BLUE}➜ Body: $data${NC}"
        response=$(curl -s -X $method "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -X $method "$url" \
            -H "Content-Type: application/json")
    fi

    echo -e "${GREEN}Response:${NC}"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
}

echo -e "\n${YELLOW}=== 1. Проверка доступности сервисов ===${NC}"

# Проверка Eureka
echo -e "\n${BLUE}➜ Проверка Eureka:${NC}"
curl -s "http://localhost:8761/eureka/v2/apps" | jq '.applications.application[].name' 2>/dev/null || echo "Eureka недоступна"

# Проверка Gateway
echo -e "\n${BLUE}➜ Проверка Gateway:${NC}"
curl -s "http://localhost:8080/actuator/health" | jq '.'

echo -e "\n${YELLOW}=== 2. Работа с категориями ===${NC}"

# Создание категории
echo -e "\n${BLUE}➜ Создание категории:${NC}"
CATEGORY_NAME="Test Category $(date +%s)"
CATEGORY_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/admin/categories" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"$CATEGORY_NAME\"}")
echo "$CATEGORY_RESPONSE" | jq '.'

CATEGORY_ID=$(echo "$CATEGORY_RESPONSE" | jq -r '.id // empty')
if [ -n "$CATEGORY_ID" ]; then
    echo -e "${GREEN}✅ Категория создана с ID: $CATEGORY_ID${NC}"
else
    echo -e "${RED}❌ Не удалось создать категорию${NC}"
    exit 1
fi

# Получение категории
echo -e "\n${BLUE}➜ Получение категории по ID:${NC}"
curl -s "$GATEWAY_URL/categories/$CATEGORY_ID" | jq '.'

# Получение всех категорий
echo -e "\n${BLUE}➜ Получение всех категорий:${NC}"
curl -s "$GATEWAY_URL/categories?from=0&size=10" | jq '.'

echo -e "\n${YELLOW}=== 3. Работа с пользователями ===${NC}"

# Создание пользователя
echo -e "\n${BLUE}➜ Создание пользователя:${NC}"
USER_EMAIL="test$(date +%s)@example.com"
USER_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/admin/users" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"Test User\", \"email\": \"$USER_EMAIL\"}")
echo "$USER_RESPONSE" | jq '.'

USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id // empty')
if [ -n "$USER_ID" ]; then
    echo -e "${GREEN}✅ Пользователь создан с ID: $USER_ID${NC}"
else
    echo -e "${RED}❌ Не удалось создать пользователя${NC}"
    exit 1
fi

# Получение всех пользователей
echo -e "\n${BLUE}➜ Получение всех пользователей:${NC}"
curl -s "$GATEWAY_URL/admin/users?from=0&size=10" | jq '.'

echo -e "\n${YELLOW}=== 4. Работа с событиями ===${NC}"

# Создание события
echo -e "\n${BLUE}➜ Создание события:${NC}"
EVENT_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/users/$USER_ID/events" \
    -H "Content-Type: application/json" \
    -d "{
        \"annotation\": \"Test annotation for event with enough length\",
        \"category\": $CATEGORY_ID,
        \"description\": \"Test description for event with enough length and more text to satisfy validation\",
        \"eventDate\": \"2026-07-01 12:00:00\",
        \"location\": {\"lat\": 55.7558, \"lon\": 37.6173},
        \"paid\": false,
        \"participantLimit\": 0,
        \"requestModeration\": true,
        \"title\": \"Test Event\"
    }")
echo "$EVENT_RESPONSE" | jq '.'

EVENT_ID=$(echo "$EVENT_RESPONSE" | jq -r '.id // empty')
if [ -n "$EVENT_ID" ]; then
    echo -e "${GREEN}✅ Событие создано с ID: $EVENT_ID${NC}"
else
    echo -e "${RED}❌ Не удалось создать событие${NC}"
    exit 1
fi

# Получение событий пользователя
echo -e "\n${BLUE}➜ Получение событий пользователя:${NC}"
curl -s "$GATEWAY_URL/users/$USER_ID/events?from=0&size=10" | jq '.'

# Получение события по ID (до публикации - должно быть 404)
echo -e "\n${BLUE}➜ Получение события до публикации (ожидается 404):${NC}"
curl -s -w "\nHTTP Status: %{http_code}\n" "$GATEWAY_URL/events/$EVENT_ID" | head -20

echo -e "\n${YELLOW}=== 5. Публикация события (админ) ===${NC}"

# Публикация события
echo -e "\n${BLUE}➜ Публикация события:${NC}"
PUBLISH_RESPONSE=$(curl -s -X PATCH "$GATEWAY_URL/admin/events/$EVENT_ID" \
    -H "Content-Type: application/json" \
    -d '{"stateAction": "PUBLISH_EVENT"}')
echo "$PUBLISH_RESPONSE" | jq '.'

# Проверка статуса события
EVENT_STATUS=$(echo "$PUBLISH_RESPONSE" | jq -r '.state // empty')
if [ "$EVENT_STATUS" = "PUBLISHED" ]; then
    echo -e "${GREEN}✅ Событие опубликовано${NC}"
else
    echo -e "${RED}❌ Не удалось опубликовать событие${NC}"
fi

echo -e "\n${YELLOW}=== 6. Публичные запросы к событиям ===${NC}"

# Получение опубликованного события
echo -e "\n${BLUE}➜ Получение опубликованного события:${NC}"
curl -s "$GATEWAY_URL/events/$EVENT_ID" | jq '.'

# Получение списка событий
echo -e "\n${BLUE}➜ Получение списка событий:${NC}"
curl -s "$GATEWAY_URL/events?from=0&size=10" | jq '.'

# Поиск событий с фильтрами
echo -e "\n${BLUE}➜ Поиск событий с фильтрами (текст):${NC}"
curl -s "$GATEWAY_URL/events?text=Test&from=0&size=10" | jq '.'

echo -e "\n${YELLOW}=== 7. Работа со статистикой ===${NC}"

# Отправка hit
echo -e "\n${BLUE}➜ Отправка hit в статистику:${NC}"
HIT_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/stats-server/hit" \
    -H "Content-Type: application/json" \
    -d "{
        \"app\": \"ewm-main-service\",
        \"uri\": \"/events/$EVENT_ID\",
        \"ip\": \"127.0.0.1\",
        \"timestamp\": \"$(date '+%Y-%m-%d %H:%M:%S')\"
    }")
echo "$HIT_RESPONSE" | jq '.' 2>/dev/null || echo "✅ Hit отправлен"

# Получение статистики с unique=true
echo -e "\n${BLUE}➜ Получение статистики (unique=true):${NC}"
curl -s "$GATEWAY_URL/stats-server/stats?start=2026-06-01%2000:00:00&end=2026-12-31%2023:59:59&uris=/events/$EVENT_ID&unique=true" | jq '.'

# Получение статистики с unique=false
echo -e "\n${BLUE}➜ Получение статистики (unique=false):${NC}"
curl -s "$GATEWAY_URL/stats-server/stats?start=2026-06-01%2000:00:00&end=2026-12-31%2023:59:59&uris=/events/$EVENT_ID&unique=false" | jq '.'

# Множественные запросы для теста уникальности
echo -e "\n${BLUE}➜ Отправка 3 запросов к событию для теста уникальности:${NC}"
for i in {1..3}; do
    curl -s -X POST "$GATEWAY_URL/stats-server/hit" \
        -H "Content-Type: application/json" \
        -d "{
            \"app\": \"ewm-main-service\",
            \"uri\": \"/events/$EVENT_ID\",
            \"ip\": \"127.0.0.1\",
            \"timestamp\": \"$(date '+%Y-%m-%d %H:%M:%S')\"
        }" > /dev/null
    echo -e "${GREEN}  ➜ Запрос $i отправлен${NC}"
    sleep 1
done

# Проверка статистики после множественных запросов
echo -e "\n${BLUE}➜ Статистика после 3 запросов (unique=true - должно быть 1):${NC}"
curl -s "$GATEWAY_URL/stats-server/stats?start=2026-06-01%2000:00:00&end=2026-12-31%2023:59:59&uris=/events/$EVENT_ID&unique=true" | jq '.'

echo -e "\n${BLUE}➜ Статистика после 3 запросов (unique=false - должно быть 3):${NC}"
curl -s "$GATEWAY_URL/stats-server/stats?start=2026-06-01%2000:00:00&end=2026-12-31%2023:59:59&uris=/events/$EVENT_ID&unique=false" | jq '.'

echo -e "\n${YELLOW}=== 8. Работа с запросами на участие ===${NC}"

# Создание второго пользователя для запроса
echo -e "\n${BLUE}➜ Создание второго пользователя:${NC}"
USER2_EMAIL="test2-$(date +%s)@example.com"
USER2_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/admin/users" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"Test User 2\", \"email\": \"$USER2_EMAIL\"}")
echo "$USER2_RESPONSE" | jq '.'

USER2_ID=$(echo "$USER2_RESPONSE" | jq -r '.id // empty')
if [ -n "$USER2_ID" ]; then
    echo -e "${GREEN}✅ Второй пользователь создан с ID: $USER2_ID${NC}"
else
    echo -e "${RED}❌ Не удалось создать второго пользователя${NC}"
fi

# Создание запроса на участие
echo -e "\n${BLUE}➜ Создание запроса на участие:${NC}"
REQUEST_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/users/$USER2_ID/requests?eventId=$EVENT_ID" \
    -H "Content-Type: application/json")
echo "$REQUEST_RESPONSE" | jq '.'

REQUEST_ID=$(echo "$REQUEST_RESPONSE" | jq -r '.id // empty')
if [ -n "$REQUEST_ID" ]; then
    echo -e "${GREEN}✅ Запрос создан с ID: $REQUEST_ID${NC}"
else
    echo -e "${RED}❌ Не удалось создать запрос${NC}"
fi

# Получение запросов пользователя
echo -e "\n${BLUE}➜ Получение запросов пользователя:${NC}"
curl -s "$GATEWAY_URL/users/$USER2_ID/requests" | jq '.'

# Получение запросов на событие
echo -e "\n${BLUE}➜ Получение запросов на событие (владелец):${NC}"
curl -s "$GATEWAY_URL/users/$USER_ID/events/$EVENT_ID/requests" | jq '.'

echo -e "\n${YELLOW}=== 9. Обновление статуса запроса ===${NC}"

# Подтверждение запроса (если есть PENDING запросы)
echo -e "\n${BLUE}➜ Подтверждение запроса:${NC}"
if [ -n "$REQUEST_ID" ]; then
    UPDATE_RESPONSE=$(curl -s -X PATCH "$GATEWAY_URL/users/$USER_ID/events/$EVENT_ID/requests" \
        -H "Content-Type: application/json" \
        -d "{
            \"requestIds\": [$REQUEST_ID],
            \"status\": \"CONFIRMED\"
        }")
    echo "$UPDATE_RESPONSE" | jq '.'
fi

echo -e "\n${YELLOW}=== 10. Проверка Health Check всех сервисов ===${NC}"

# Проверка всех сервисов
echo -e "\n${BLUE}➜ Eureka:${NC}"
curl -s "http://localhost:8761/actuator/health" | jq '.'

echo -e "\n${BLUE}➜ Gateway:${NC}"
curl -s "http://localhost:8080/actuator/health" | jq '.'

echo -e "\n${BLUE}➜ Stats Server (через Gateway):${NC}"
curl -s "http://localhost:8080/stats-server/actuator/health" | jq '.'

echo -e "\n${BLUE}➜ Main Service (через Gateway):${NC}"
curl -s "http://localhost:8080/main-service/actuator/health" | jq '.'

echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✅ Тестирование завершено!${NC}"
echo -e "${BLUE}========================================${NC}"

# Вывод резюме
echo -e "\n${YELLOW}📊 Резюме:${NC}"
echo -e "${GREEN}✅ Категория: ID=$CATEGORY_ID${NC}"
echo -e "${GREEN}✅ Пользователь: ID=$USER_ID${NC}"
echo -e "${GREEN}✅ Событие: ID=$EVENT_ID${NC}"
if [ -n "$USER2_ID" ]; then
    echo -e "${GREEN}✅ Пользователь 2: ID=$USER2_ID${NC}"
fi
if [ -n "$REQUEST_ID" ]; then
    echo -e "${GREEN}✅ Запрос: ID=$REQUEST_ID${NC}"
fi