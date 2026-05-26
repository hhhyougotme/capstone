# FlashMart (Capstone)

High-concurrency flash-sale and coupon backend (Spring Boot + Redis) with Vue 3 frontend.

## Prerequisites

- JDK 17+, Maven 3.8+
- MySQL 8, Redis (5+ recommended for Streams)
- Node.js 18+ (frontend)

## Quick start

### 1. Database

```bash
mysql -u root -p < flashmart-backend/src/main/resources/schema.sql
```

If upgrading an existing DB, run migrations under `flashmart-backend/src/main/resources/sql/` as needed.

### 2. Backend config

```bash
copy flashmart-backend\src\main\resources\application-example.yml flashmart-backend\src\main\resources\application.yml
```

Edit `application.yml` with your MySQL password and Redis host.

### 3. Run backend

```bash
cd flashmart-backend
mvn spring-boot:run
```

API: `http://localhost:8080/api/health`

### 4. Run frontend (dev)

```bash
cd flashmart-frontend
npm install
npm run dev
```

Open the Vite URL (default `http://localhost:5173`). API calls are proxied to port 8080.

### 5. Optional: bundled UI on backend

```bash
cd flashmart-frontend
npm run build
cd ../flashmart-backend
mvn spring-boot:run
```

Then open `http://localhost:8080/`.

## Project layout

- `flashmart-backend/` — Spring Boot 3, MyBatis-Plus, Redis, Redisson, Lua stock, Streams
- `flashmart-frontend/` — Vue 3 + Vite

See `flashmart-backend/README.md` for API overview.
