# OmniBot Platform

**One login. Every service.**
An AI-powered chatbot that unifies food delivery, e-commerce, and urban transport into a single conversational interface.

---

## Status

| Phase | Feature | Status |
|---|---|---|
| 1 | User Auth (Register, Login, JWT, MySQL) | ✅ Done |
| 2 | Chatbot core — intent detection, mock APIs | ✅ Done |
| 2.5 | Guided multi-step booking flows (transport + food) | ✅ Done |
| 3 | Real API integrations (Zomato/Swiggy/Uber/Ola partnerships) | 📅 Planned |
| 4 | React + Material-UI frontend | 📅 Planned |
| 5 | AWS/Azure deployment | 📅 Planned |

---

## Tech Stack

- **Backend**: Java 17+, Spring Boot 3.2.5, Spring Security, JWT
- **Database**: MySQL 8 (via XAMPP locally)
- **Frontend**: Static HTML/CSS/JS served by Spring Boot (`src/main/resources/static`)
- **Build**: Maven

> Note: the frontend is currently plain HTML/JS for speed of iteration. A React + Material-UI rewrite is planned for Phase 4.

---

## Integrated Services (mocked for now)

| Category | Services |
|---|---|
| 🍕 Food Delivery | Zomato, Swiggy, Blinkit, Zepto |
| 🛍️ Shopping | Amazon, Flipkart, Meesho, Myntra |
| 🚗 Transport | Uber, Ola, Rapido, Yulu |

All responses currently come from `MockServiceAdapter` — realistic simulated prices, ETAs, and ratings. Real partner APIs get wired in during Phase 3.

---

## Project Structure

```
omnibot-platform/
├── src/main/java/com/omnibot/
│   ├── OmniBotApplication.java        Entry point
│   ├── agent/                         Intent detection, reply generation, mock service adapters
│   │   ├── IntentDetector.java
│   │   ├── BotReplyEngine.java
│   │   └── MockServiceAdapter.java
│   ├── config/SecurityConfig.java     JWT + Spring Security setup
│   ├── controller/                    REST endpoints
│   │   ├── AuthController.java
│   │   └── ChatController.java
│   ├── model/                         Entities + DTOs
│   │   ├── User.java
│   │   ├── ChatMessage.java
│   │   ├── ConversationState.java     Tracks in-progress booking flows
│   │   ├── SavedRoute.java
│   │   ├── AuthDto.java
│   │   └── ChatDto.java
│   ├── repository/                    Spring Data JPA repositories
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── ChatService.java           Orchestrates intent → reply → cards
│   │   └── ConversationFlowService.java  Multi-step "slot filling" for bookings
│   └── util/JwtUtil.java
├── src/main/resources/
│   ├── application.properties
│   └── static/                        login.html, dashboard.html (served directly)
├── database/schema.sql                Full MySQL schema (run once)
├── scripts/                           Windows helper scripts (start-mysql, run-app)
└── pom.xml
```

---

## Quick Start

### Prerequisites
- Java 17+ (`java -version`)
- Maven (`mvn -version`)
- MySQL 8 running locally (XAMPP, standalone install, or any MySQL server)

### 1. Clone
```bash
git clone https://github.com/Riddhiscode/omnibot-platform.git
cd omnibot-platform
```

### 2. Set up the database
Run the schema once against your MySQL server:
```bash
mysql -u root -p < database/schema.sql
```
(Or paste its contents into phpMyAdmin's SQL tab.)

### 3. Configure
Edit `src/main/resources/application.properties` if your MySQL setup differs from the defaults:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/omnibot_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```

### 4. Run
```bash
mvn spring-boot:run
```

**Windows shortcut:** instead of step 4 above, you can use the helper scripts in
[`scripts/`](scripts/README.md) — `scripts\start-all.bat` starts MySQL and the app together.

The app serves everything — API and frontend — under `http://localhost:8080/api`.

Open the login page at:
```
http://localhost:8080/api/login.html
```

---

## API Reference

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login, receive JWT access + refresh tokens |
| POST | `/api/auth/forgot-password` | Trigger reset flow |
| GET | `/api/auth/health` | Health check |

### Chat (requires `Authorization: Bearer <token>`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/chat` | Send a message, get AI reply + service cards |
| GET | `/api/chat/history?sessionId=...` | Get full session history |

---

## How the chatbot works

1. **Intent detection** (`IntentDetector`) — keyword-based classifier maps a message to `FOOD_ORDER`, `TRANSPORT_BOOK`, `SHOPPING_ORDER`, `TRACK_ORDER`, `COMPARE`, `GREETING`, `HELP`, or `UNKNOWN`.
2. **Simple intents** (shopping/help/etc.) get an immediate reply + mock service cards from `MockServiceAdapter`.
3. **Booking intents** (transport, food) trigger a guided multi-turn flow via `ConversationFlowService`:

   **Transport (`TRANSPORT_BOOK`)**
   - "Where are you right now?" (source)
   - "Where would you like to go?" (destination)
   - "When do you need the ride?" (time)
   - "Want me to save this route?" (yes/no → `saved_routes`)
   - "Do you have a preferred app?" (Uber/Ola/Rapido/Yulu)
   - Booking summary + service cards, preferred app sorted first.

   **Food (`FOOD_ORDER`)**
   - "What are you in the mood for?" (cuisine)
   - "Any specific items?" (or "surprise me")
   - "Where should this be delivered?" (address)
   - "When do you want it delivered?" (time)
   - "Save this address for next time?" (yes/no → `saved_addresses`)
   - "Do you have a preferred app?" (Zomato/Swiggy/Blinkit/Zepto)
   - Order summary + service cards, preferred app sorted first.

4. All turns are persisted to `chat_messages`; in-progress flows live in `conversation_states` until completed.

---

## Roadmap

- **Phase 3**: Wire in real partner APIs (requires business agreements with Zomato/Swiggy/Uber/Ola etc.); add BigBasket, D-Mart, JioMart.
- **Phase 4**: Rebuild frontend in React + Material-UI.
- **Phase 5**: Deploy to AWS or Azure; CI/CD via GitHub Actions.

---

## License
MIT
