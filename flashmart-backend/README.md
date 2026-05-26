# FlashMart Backend

Spring Boot **3.2** + **MyBatis-Plus** + **Redis** (session token), classic **Controller → Service → Mapper** layering.  
Implements features aligned with the thesis: registration/login, merchant browse, coupons, flash-sale orders, optional product catalog, plus **thesis-oriented concurrency building blocks** below.

### Thesis / requirements alignment (implemented in code)

| Topic | Status |
|--------|--------|
| Redis session tokens | Yes (`SessionService`) |
| Redis **cache-aside** + **random TTL jitter** (merchant lists/detail, coupon lists, active flash-sale DTOs) | Yes (`MerchantService`, `CouponService`, `FlashSaleService`) |
| **Lua** atomic decrement for flash-sale / coupon stock mirrors | Yes (`AtomicStockRedisService` + warmup from MySQL) |
| **Redisson** distributed locks (coupon claim + flash order) | Yes (`CouponClaimService`, `OrderService`) |
| **Redis Streams** for post-order events + scheduled consumer (at-least-once style) | Yes when **Redis ≥ 5**; **auto-disabled** on Redis 3.x (`unknown command XADD`) or set `flashmart.streams.enabled=false` |
| MySQL remains system of record | Yes (orders/claims still persisted in DB) |
| Delayed double-delete cache invalidation, full RabbitMQ, multi-node Nginx lab | **Not** implemented (thesis narrative may describe broader deployment; this repo is a focused prototype). |

## Prerequisites

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8** (listening on `localhost:3306`)
- **Redis-compatible server** on **`localhost:6379`** (no password by default).  
  **Streams** (`XADD` / `XGROUP`) require **Redis 5.0+**. The older `winget install Redis.Redis` (3.0.x) does **not** support Streams — the app will **auto-disable** stream features and still start; or set `flashmart.streams.enabled: false` in `application.yml`. For full thesis features on Windows, use **Memurai** or another **Redis 5+** build.

### Redis on Windows

If `redis-cli` / `redis-server` are not on your PATH, you likely do not have Redis installed yet.

- **Recommended:** install **Memurai Developer** (Redis 7–compatible API, good match for Spring Boot 3.x):

  `winget install Memurai.MemuraiDeveloper --accept-package-agreements --accept-source-agreements`

  Use an **elevated** (Run as administrator) terminal if the installer waits on UAC.

- **Fallback:** older Windows port — `winget install Redis.Redis` (Redis 3.0.x; enough for this project’s basic token use).

Do **not** run two `winget install` sessions at the same time (they block each other).

More detail: [docs/REDIS_WINDOWS.md](docs/REDIS_WINDOWS.md).

**After `Redis.Redis` (winget) install:** a Windows service named **Redis** should be **Running** on port **6379**. Verify:

```powershell
& "C:\Program Files\Redis\redis-cli.exe" ping
```

Expected: `PONG`.

If anything is missing, install it first; the app will not start without MySQL and Redis.

## Database

Create schema and seed data:

```bash
mysql -u root -p < src/main/resources/schema.sql
```

If you already had an older database **without** the `product` table / `product_id` columns, or you see **`Unknown column 'product_id'`** on startup, run the incremental script once:

```bash
mysql -u root -p flashmart < src/main/resources/sql/migration_add_product.sql
```

Edit `src/main/resources/application.yml` if your MySQL user/password is not `root` / `root`.

## Run

```bash
cd flashmart-backend
mvn spring-boot:run
```

Health check: `GET http://localhost:8080/api/health`

## 成品前端（Vue）

源码目录：`../flashmart-frontend`（Vue 3 + Vite）。开发时 Vite 把 `/api` 代理到 `http://localhost:8080`；生产构建产物输出到本项目的 `src/main/resources/static`，与后端 **同域** 部署。

**本地联调：** 终端 1 启动后端 `mvn spring-boot:run`；终端 2：

```bash
cd ../flashmart-frontend
npm install
npm run dev
```

浏览器打开 Vite 提示的地址（默认 http://localhost:5173）。

**仅后端 jar / 一体化访问：** 先在 `flashmart-frontend` 执行 `npm run build`，再启动 Spring Boot，访问 **http://localhost:8080/** 即可。登录后 token 存 `localStorage`，请求自动带 `Authorization: Bearer …`。

## API overview

| Method | Path | Auth |
|--------|------|------|
| POST | `/api/auth/register` | No |
| POST | `/api/auth/login` | No → returns `token` |
| GET | `/api/merchant-types` | No |
| GET | `/api/merchants?page=1&pageSize=10&merchantTypeId=` | No（分页列表，Redis 缓存 + 重建互斥锁） |
| GET | `/api/merchants/{id}` | No（含 `serviceDescription`，详情缓存 + 重建互斥锁） |
| GET | `/api/products?merchantId=1` | No (在售商品) |
| GET | `/api/products/{id}` | No |
| POST | `/api/products` | Bearer（演示：登录用户可上架） |
| GET | `/api/coupons?merchantId=1` | No |
| GET | `/api/coupons?merchantId=1&activeOnly=true` | No |
| GET | `/api/flash-sales` | No（活动列表，含可选关联商品名称/标价） |
| POST | `/api/coupons/{couponId}/claim` | `Authorization: Bearer <token>` |
| GET | `/api/me/coupon-claims` | Bearer |
| POST | `/api/flash-sales/{eventId}/orders` | Bearer (JSON body optional `{"amount":1.00}`) |
| GET | `/api/me/orders` | Bearer |

### Quick curl flow (after seed)

```bash
curl -s -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"phone\":\"13800000001\",\"password\":\"secret12\",\"nickname\":\"u1\"}"

curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"phone\":\"13800000001\",\"password\":\"secret12\"}"
# copy token from response

curl -s http://localhost:8080/api/flash-sales

curl -s -X POST http://localhost:8080/api/flash-sales/1/orders -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{}"
```

Seed data creates **flash_sale_event** `id = 1` tied to coupon `id = 2` (50 stock). Each user may place **one** order per event (`uk_user_event`).

## Project layout

```
src/main/java/com/flashmart/
  controller/   REST
  service/      business logic
  mapper/       MyBatis-Plus mappers
  entity/       tables
  dto/          request/response POJOs
  config/       security crypto, CORS, exceptions
```

SQL source of truth: `src/main/resources/schema.sql`.
