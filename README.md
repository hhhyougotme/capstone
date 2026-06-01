# FlashMart (Capstone)

High-concurrency flash-sale and coupon platform built with **Spring Boot 3**, **MySQL**, **Redis**, and a **Vue 3** frontend.

## Source Code (GitHub)

**Repository:** [https://github.com/hhhyougotme/capstone](https://github.com/hhhyougotme/capstone)

```bash
git clone https://github.com/hhhyougotme/capstone.git
cd capstone
```

## Prerequisites

| Software | Version |
|----------|---------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.x |
| Redis | 5.0+ recommended (Streams); Redis 3.x works with streams disabled |
| Node.js | 18+ (only if you run the frontend in dev mode or rebuild static assets) |

Ensure **MySQL** and **Redis** are running on `localhost` before starting the backend.

## How to Run the Project

### Step 1 — Create the database

From the repository root:

```bash
mysql -u root -p < flashmart-backend/src/main/resources/schema.sql
```

This creates the `flashmart` database, tables, and demo seed data (merchants, coupons, products, one flash-sale event, admin user).

If you already have an older `flashmart` database, apply scripts under `flashmart-backend/src/main/resources/sql/` as needed (see `flashmart-backend/README.md`).

### Step 2 — Configure the backend

**Windows (PowerShell / CMD):**

```bash
copy flashmart-backend\src\main\resources\application-example.yml flashmart-backend\src\main\resources\application.yml
```

**macOS / Linux:**

```bash
cp flashmart-backend/src/main/resources/application-example.yml flashmart-backend/src/main/resources/application.yml
```

Edit `flashmart-backend/src/main/resources/application.yml`:

- Set `spring.datasource.password` to your MySQL `root` password.
- Adjust `spring.data.redis.host` / `port` if Redis is not on `localhost:6379`.
- For Redis **3.x** (no Streams), set `flashmart.streams.enabled: false`.

> `application.yml` is listed in `.gitignore` and is **not** pushed to GitHub. Use `application-example.yml` as the template.

### Step 3 — Start the application

**Option A — Recommended (single server, UI + API on port 8080)**

The repository may already include a built frontend under `flashmart-backend/src/main/resources/static/`. To rebuild it:

```bash
cd flashmart-frontend
npm install
npm run build
cd ../flashmart-backend
mvn spring-boot:run
```

Open in a browser:

- **Web UI:** http://localhost:8080/
- **Health check:** http://localhost:8080/api/health

**Option B — Frontend dev server (hot reload)**

Terminal 1 — backend:

```bash
cd flashmart-backend
mvn spring-boot:run
```

Terminal 2 — frontend:

```bash
cd flashmart-frontend
npm install
npm run dev
```

Open the URL shown by Vite (default http://localhost:5173). API requests are proxied to http://localhost:8080.

### Step 4 — Demo accounts

| Role | Login (`account`) | Password |
|------|-------------------|----------|
| Admin | `13800000000` (phone) | `admin123` |
| New user | Register via **Register** page (phone or email + verification code) | (your choice) |

Registration flow: click **获取验证码** on the register page; when `flashmart.auth.expose-verification-code-in-response` is `true` in `application.yml`, the API returns a demo code in the response for testing without SMS.

## Main Features (for testing)

- User registration (phone / email + verification code) and login (phone or email)
- Merchant list (pagination, category filter), detail, service description
- Coupon claim and history
- Flash-sale events and orders
- **Product purchase** from the product detail page (**立即购买**)
- Admin: create flash-sale events (**管理闪购**, admin login required)
- Orders: **我的** → **我的订单**

## Project Layout

```
capstone/
├── flashmart-backend/     # Spring Boot API, MyBatis-Plus, Redis, Redisson
├── flashmart-frontend/    # Vue 3 + Vite source
└── README.md              # This file
```

API details: `flashmart-backend/README.md`.

## Troubleshooting

| Issue | Action |
|-------|--------|
| `Port 8080 was already in use` | Stop the other Java process or change `server.port` in `application.yml`. |
| Empty flash-sale list | Ensure event `begin_time` / `end_time` include the current time, or create a new event as admin. |
| Redis Streams errors on old Redis | Set `flashmart.streams.enabled: false`; orders still persist via MySQL. |

## Author

Capstone project — FlashMart high-concurrency e-commerce prototype.
