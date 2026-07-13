# OmniBot Platform — AI Handoff Document
**Generated**: 2026-07-13 | **Repo**: https://github.com/Riddhiscode/omnibot-platform.git | **Branch**: main (4 commits)

---

## What This Is
A multi-vendor commerce aggregator platform ("Swiggy of everything"). Users chat naturally ("order me a biryani", "book a cab to airport", "buy me a laptop") and the system searches across multiple real vendors, compares prices, and places orders.

**Stack**: Java 21 / Spring Boot 3.2.5 / MySQL 9.7 / React Vite + Tailwind

---

## Project Layout
```
C:\Users\riddh\omnibot-platform\
├── Start OmniBot.bat          ← Double-click to start everything
├── Stop OmniBot.bat           ← Double-click to stop
├── start.ps1                  ← PowerShell alternative
├── database/
│   ├── schema.sql             ← 6 base tables
│   └── schema_updates.sql     ← 4 additional tables (10 total)
├── backend-core/              ← Spring Boot app
│   ├── pom.xml
│   └── src/main/java/com/omnibot/
│       ├── adapter/           ← VENDOR ADAPTER LAYER (the main feature)
│       ├── agent/             ← Intent detection, NLP, mock data
│       ├── config/            ← Security, CORS, data seeding, vendor config
│       ├── controller/        ← REST endpoints
│       ├── model/             ← JPA entities + DTOs
│       ├── repository/        ← Spring Data JPA repos
│       ├── service/           ← Business logic
│       └── util/              ← JWT helper
└── frontend/                  ← React app (not built — no Node.js installed)
    └── src/components/        ← 5 JSX components
```

---

## How To Run
### Prerequisites
- MySQL 9.7 running at `C:\Users\riddh\mysql-9.7.0-winx64` on port 3306
- Maven at `C:\Users\riddh\maven\apache-maven-3.9.16\bin\mvn.cmd`
- **No Node.js installed** — frontend cannot be built locally
- **JDK 26.0.1** — Lombok is INCOMPATIBLE. Do NOT add Lombok. All DTOs use manual getters/setters.

### Quick Start
1. Double-click `Start OmniBot.bat` — starts MySQL if down, then starts Spring Boot
2. Backend runs at `http://localhost:8080`
3. Context path is `/api`, so all endpoints are under `http://localhost:8080/api/v1/...`

### Manual Start
```powershell
# Start MySQL (if not running)
Start-Process "C:\Users\riddh\mysql-9.7.0-winx64\bin\mysqld.exe" -ArgumentList "--defaults-file=C:\Users\riddh\mysql-9.7.0-winx64\my-omnibot.ini"

# Start app
cd C:\Users\riddh\omnibot-platform\backend-core
& "C:\Users\riddh\maven\apache-maven-3.9.16\bin\mvn.cmd" spring-boot:run -DskipTests
```

### Run Tests
```powershell
cd C:\Users\riddh\omnibot-platform\backend-core
& "C:\Users\riddh\maven\apache-maven-3.9.16\bin\mvn.cmd" test
# 32 tests, all passing
```

### Login Credentials
- **Admin**: `admin@omnibot.in` / `Admin@12345`
- **Demo user**: `demo@omnibot.in` / `Demo@1234`
- Login: `POST http://localhost:8080/api/auth/login` with `{"email":"...","password":"..."}`
- Returns JWT `accessToken` — pass as `Authorization: Bearer <token>` header

---

## Architecture — Adapter Pattern (THE MAIN FEATURE)

### Core Interface: `VendorAdapter` (`adapter/VendorAdapter.java`)
```java
public interface VendorAdapter {
    VendorCategory getCategory();                    // FOOD, TRANSPORT, SHOPPING
    String getVendorName();                          // "Zomato", "Uber", etc.
    boolean isAvailable();                           // health check
    List<VendorSearchResult> search(VendorSearchRequest request);  // mock or live
    VendorOrderResult placeOrder(VendorOrderRequest request);
    VendorTrackingResult trackOrder(String orderId);
}
```

### Central Registry: `VendorAdapterRegistry` (`adapter/VendorAdapterRegistry.java`)
- All 15 adapters self-register at startup via `VendorAdapterConfig`
- Key lookup: `getAdapter("zomato")` or `getAdapter("zomato", FOOD)`
- Aggregate search: `searchAll(request, category)` — queries all adapters in category, marks cheapest/fastest
- Cross-vendor: `placeOrder()`, `trackOrder()`, `getVendorStatus()`

### 15 Vendor Adapters (all mock mode, structure ready for live APIs)
| Category | Vendors |
|----------|---------|
| **Food** (4) | Zomato, Swiggy, UberEats, DoorDash |
| **Transport** (6) | Uber, Ola, Lyft, Bolt, Rapido, Yulu |
| **Shopping** (5) | Amazon, Flipkart, Meesho, Myntra, eBay |

Each adapter:
- Checks `VendorProperties.mode` ("mock" or "live") in constructor
- In mock mode: returns realistic simulated data (varies by query)
- In live mode: TODO — needs real API keys configured in `application.properties`
- Uses Java setters (NO Lombok — JDK 26 incompatible)

### Config Switch: `application.properties`
```properties
omnibot.vendor.mode=mock          # Change to "live" when API keys are ready
omnibot.vendor.timeoutMs=5000
omnibot.vendor.maxRetries=2
# Each vendor has: omnibot.vendor.{name}.api-key, .endpoint, .enabled
```

---

## Chat Flow (End-to-End)

### Simple Flow (Shopping)
```
User: "buy me a laptop"
  → IntentDetector: SHOPPING_ORDER
  → ChatService.getVendorCards(): searches Amazon, Flipkart, Meesho, Myntra, eBay
  → Returns 26 product results with prices in INR
```

### Multi-Step Flow (Food / Transport)
Uses `ConversationFlowService` with slot-filling via `conversation_states` table:

**Food (6 steps)**:
1. "order biryani" → "What are you in the mood for?"
2. "biryani" → "Any specific items?"
3. "chicken biryani" → "Where should this be delivered?"
4. "MG Road Bangalore" → "When do you want it?"
5. "30 mins" → "Save this address? (yes/no)"
6. "no" → "Preferred app? (Zomato/Swiggy/UberEats/DoorDash)"
7. "no preference" → Shows 4 vendor results with prices

**Transport (5 steps)**:
1. "book cab to gurgaon" → "Where are you?"
2. "Connaught Place" → "Where to?"
3. "Cyber Hub" → "When?"
4. "in 10 mins" → "Save route? (yes/no)"
5. "no" → "Preferred app?"
6. "no preference" → Shows 6 vendor results

### Known Issue
`conversation_states.current_step` was MySQL ENUM (only transport steps). Fixed to VARCHAR(30) in live DB, and entity has `columnDefinition = "VARCHAR(30)"` to prevent Hibernate revert. If you recreate the DB, ensure the column stays VARCHAR.

---

## REST API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | No | Login, returns JWT |
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/v1/chat` | Yes | Main chat endpoint |
| GET | `/api/v1/chat/history` | Yes | Chat history for session |
| GET | `/api/v1/orders/vendors` | Yes | All vendor statuses |
| GET | `/api/v1/orders/search` | Yes | Search across vendors |
| GET | `/api/v1/orders/track/{vendor}/{orderId}` | Yes | Track order |
| POST | `/api/v1/orders/place` | Yes | Place order |
| GET | `/api/v1/export/excel` | Yes | Export Excel report |
| GET | `/api/v1/export/pdf` | Yes | Export PDF report |
| GET | `/api/health` | No | Health check |

**Security**: JWT filter runs on all requests. `/auth/**` is public. All other endpoints require `Authorization: Bearer <token>`.

---

## Database (MySQL 9.7)

```properties
URL:      jdbc:mysql://localhost:3306/omnibot_db
User:     root
Password: (empty)
DDL:      spring.jpa.hibernate.ddl-auto=update
```

**10 tables**: users, chat_messages, conversation_states, saved_routes, saved_addresses, connected_services, vendor_mappings, orders, payment_transactions, intents_extracted

---

## Key Gotchas

1. **No Lombok** — JDK 26.0.1 is incompatible. Use manual getters/setters everywhere.
2. **No Node.js** — Frontend cannot be built. Code exists in `frontend/src/` but no `npm install` or `npm run build`.
3. **MySQL dies** — Standalone MySQL 9.7 can die unexpectedly. Use `Start OmniBot.bat` which auto-restarts it.
4. **XAMPP MySQL error** — XAMPP's MySQL (port 3307) fails because standalone MySQL occupies port 3306. This is harmless — ignore it.
5. **Context path is `/api`** — So `server.servlet.context-path=/api` means `GET /api/health` maps to servlet path `/health`.
6. **Security matcher mismatch** — SecurityConfig permits `/auth/**`, `/api/**`, `/chat/**` but servlet paths after context-path don't include `/api`. The `/auth/**` matcher works because auth endpoints are at `/auth/login` (servlet path). Other endpoints rely on JWT authentication.
7. **MongoDB warning** — `spring-boot-starter-data-mongodb` is in pom.xml but no MongoDB is configured. The warning is harmless.
8. **Two `@Enumerated` columns** — `ConversationState.flow_type` and `current_step` both use `@Enumerated(EnumType.STRING)`. Hibernate's `ddl-auto=update` may create MySQL ENUM columns instead of VARCHAR. The entity now specifies `columnDefinition = "VARCHAR(30)"` to prevent this.

---

## Tests (32 total, all passing)

| File | Tests | What It Covers |
|------|:---:|----------------|
| `VendorAdapterRegistryTest.java` | 12 | Register, lookup, category filter, searchAll, placeOrder delegation, case-insensitive keys, status, edge cases |
| `VendorAdaptersTest.java` | 20 | All 15 adapters: search/placeOrder/trackOrder + contract test validating interface compliance |

Run with: `mvn test` from `backend-core/`

---

## What's NOT Done Yet

1. **Real API integration** — All 15 adapters use mock data. Need API keys from Zomato, Swiggy, Uber, Amazon, etc.
2. **Frontend build** — No Node.js installed. `ChatEngine.jsx` is wired to vendor adapter responses but can't be verified.
3. **Order persistence** — Orders are returned from adapters but not saved to the `orders` table.
4. **Payment flow** — No actual payment processing.
5. **Shopping flow** — Returns results directly (no multi-step slot-filling like Food/Transport).
6. **`connected_services` table** — Schema exists but no JPA entity or repo.
7. **Rate limiting / circuit breaker** — No fault tolerance for live API calls.
8. **Deployment** — Not deployed anywhere.

---

## Git History
```
516dd79 chore: add one-click startup scripts for MySQL + backend
f7b0571 feat: bridge ChatService to VendorAdapterRegistry, fix DB schema, add unit tests
2cbd1b6 feat: add vendor adapter architecture for 15 real API integrations
7ee5cc4 chore: clean repo structure - remove target/, dedupe scripts, fix README to match actual Java/Spring Boot stack
```
