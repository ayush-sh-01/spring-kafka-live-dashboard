# Engineering Decisions Log: High-Performance Reporting Engine

This document explains every important decision taken during the design, coding, debugging, and styling of this project. It is written in simple, plain English so you can understand the exact reasoning behind each choice and explain them clearly in an interview or code review.

---

## 1. Architectural & Technology Decisions

### Decision 1: Using Hibernate Second-Level Cache (Ehcache 3) on Geolocation and UserAgent
* **The Question:** Why not just query PostgreSQL every time a user request comes in?
* **The Reason:** 
  Tables like `Geolocation` (Countries, Cities, IPs) and `UserAgent` (Browsers, OS, Device Types) are essentially **static lookup tables**. Their data almost never changes.
  If 100,000 visitors come to your site, making 100,000 queries to the database just to ask *"What is the name of Country ID 5?"* is a huge waste of database memory and connection pools.
* **Why Ehcache 3?**
  Ehcache 3 supports standard JSR-107 (JCache) and integrates natively with Hibernate 6. It stores entities directly in application RAM (heap).
* **The Outcome:** The database only gets asked once. The next 99,999 lookups take **0 milliseconds** directly from RAM.

---

### Decision 2: Hibernate Query Cache with a Strict 5-Minute TTL
* **The Question:** Why do we need a Query Cache in addition to an Entity (L2) Cache?
* **The Reason:**
  - Entity Cache only stores **single rows by ID** (e.g., `Geolocation where id = 1`).
  - Analytics dashboards need **aggregations** (e.g., `COUNT(*) grouped by country`).
  Calculating aggregations across millions of rows burns database CPU.
* **Why a 5-minute TTL?**
  In high-traffic systems, real-time dashboards don't need sub-second freshness on heavy multi-month totals. A 5-minute Time-To-Live (TTL) allows the dashboard to serve fast cached summaries while preventing data from getting too old.

---

### Decision 3: Solving the Stale Cache Invalidation Challenge
* **The Problem (Classic Interview Trap):**
  If you cache heavy query results in RAM for 5 minutes, what happens when new users keep clicking and inserting new logs into the database? Won't the query cache show old, wrong data?
* **Our Decision:**
  We routed all activity writes through Hibernate's JPA persistence context (`ActivityPersistenceService`) instead of writing raw SQL or bypassing Hibernate.
* **Why this works:**
  Hibernate has a built-in region called `default-update-timestamps-region`. Every time a new `ActivityLog` is saved through JPA, Hibernate records the exact timestamp of that table change. When a cached query is requested, Hibernate compares timestamps: if the table was modified after the cache was created, **Hibernate automatically drops the stale cache and calculates fresh numbers**.

---

### Decision 4: Using JPA Criteria API Instead of Plain SQL Strings
* **The Question:** Why use `CriteriaBuilder` and `Predicate` instead of writing `"SELECT * FROM activity_logs WHERE ..."`?
* **The Reason:**
  Our dashboard has multiple optional filters:
  - User can filter by **Country only**, or
  - **Country + Action**, or
  - **Browser + Latency**, or
  - **No filters at all**.
  If you use string concatenation (`"WHERE 1=1 " + (country != null ? "AND country = ..." : "")`), your code becomes messy, hard to test, and vulnerable to SQL injection.
  With the **JPA Criteria API**, we build the query dynamically and type-safely in Java. Only fields that the user actually selected are added as conditions.

---

### Decision 5: Eliminating the N+1 Query Problem with `@NamedEntityGraph`
* **The Problem:**
  An `ActivityLog` has relationships with `User`, `Geolocation`, and `UserAgent`. If you fetch 20 logs lazily, Hibernate runs:
  1 query for the logs + 20 for users + 20 for locations + 20 for user agents = **61 database queries!** This is called the **N+1 problem**.
* **Our Decision:**
  We added `@NamedEntityGraph(name = "ActivityLog.detailedReport")` to the `ActivityLog` entity.
* **The Result:**
  Hibernate joins all 4 tables in **a single SQL query** (`JOIN users`, `JOIN geolocations`, `JOIN user_agents`). 61 queries become **exactly 1 query**.

---

### Decision 6: Decoupling Ingestion with Kafka and an In-Memory Dev Fallback
* **The Question:** Why put Kafka between the REST API and the Database?
* **The Reason:**
  During flash sales or traffic spikes, 10,000 clicks can hit the server per second. If the server writes each click directly to PostgreSQL synchronously, database connections get exhausted and user requests time out.
  **Kafka acts as a shock absorber.** The REST endpoint accepts the click, immediately puts it into the Kafka topic `user-activity-events`, and responds in `< 5ms`. The background consumer reads from Kafka at a steady pace and writes to the database.
* **Why the graceful fallback in dev mode?**
  If a developer or interviewer runs the project without Docker or an active Kafka cluster running, we made `ActivityProducerService` gracefully fall back to direct asynchronous persistence so the application **always boots and works out of the box** without throwing connection crashes.

---

### Decision 7: Real-Time Updates via WebSocket (STOMP) Instead of Polling
* **The Question:** Why not let the browser use `setInterval` to fetch new data every 2 seconds?
* **The Reason:**
  HTTP polling causes unnecessary network traffic, repeated HTTP header handshakes, and wasted CPU cycles.
  With **WebSocket + STOMP**, we keep one lightweight open connection. A `@Scheduled(fixedRate = 3000)` worker aggregates latest metrics and pushes them down to all connected clients simultaneously over `/topic/analytics`.

---

## 2. Bug Fixes & Code Evolution Decisions

### Decision 8: Fixing Logger Reference in `ActivityPersistenceService`
* **The Issue:** A typo `LoggerFactory.getLogger(ActivityPersistenceService)` caused a compile failure.
* **The Fix:** Changed to `LoggerFactory.getLogger(ActivityPersistenceService.class)`. In Java, classes are referenced as object tokens using `.class`.

---

### Decision 9: Fixing JCache URI Specification in `application.yml`
* **The Issue:** `Couldn't load URI from classpath:ehcache.xml`.
* **The Reason:** JCache's `JCacheRegionFactory` uses Java `ClassLoader.getResource()` to find the file. It expects a relative path like `ehcache.xml` rather than Spring's `classpath:` protocol string.
* **The Fix:** Changed `uri: classpath:ehcache.xml` to `uri: ehcache.xml`.

---

### Decision 10: Fixing Second-Level Cache Hit Verification in Tests
* **The Issue:**
  In the integration test `testSecondLevelCacheLookup`, saving an entity and querying it inside an uncommitted `@Transactional` method produced 0 cache hits.
* **The Reason:**
  Hibernate's `READ_WRITE` cache strategy protects against dirty reads. It does **not** commit entities into the L2 cache until the database transaction has successfully committed! In a test method where the transaction never commits, `afterInsert()` is never triggered.
* **The Fix:**
  We injected `TransactionTemplate` to explicitly commit the insert in Transaction 1, and then queried it in Transaction 2. This immediately registered a clean **L2 cache hit with 0 JDBC statements executed**.

---

### Decision 11: Fixing `data.l2CacheHits` Frontend Property Name
* **The Issue:** The Chart.js doughnut and bar charts were not populating on initial page load.
* **The Reason:**
  The Java backend DTO returned `"l2CacheHits": 0` (capital `C`), but the JavaScript code looked for `data.l2cacheHits` (lowercase `c`), throwing an `undefined` error which stopped the remaining chart rendering lines.
* **The Fix:**
  Updated the JavaScript function with a safe fallback:
  ```javascript
  const l2Hits = data.l2CacheHits !== undefined ? data.l2CacheHits : (data.l2cacheHits !== undefined ? data.l2cacheHits : 0);
  ```
  Both charts now render immediately upon receiving the WebSocket summary.

---

## 3. UI / UX Design Decisions

### Decision 12: Luxury "Black, White, and Gold" Theme
* **The Request:** Change the UI/UX from the standard blue/cyan style into a high-end, premium Black, White, and Gold aesthetic.
* **The Design System Implemented:**
  - **Background:** Deep Obsidian Black (`#08080A`, `#0E0E12`) with frosted glass cards (`rgba(18, 18, 23, 0.78)`).
  - **Metallic Gold Typography:** Created a multi-stop gold gradient for headers:
    `linear-gradient(135deg, #FFF2B2 0%, #D4AF37 50%, #996515 100%)`.
  - **Metric Badges:** Micro-badges with subtle gold borders (`KAFKA / DB`, `LATENCY`, `EHCACHE 3`, `5M TTL`) to clearly communicate the technical architecture of each KPI card.
  - **Charts:** Redesigned Chart.js datasets using gold and champagne tones (`#D4AF37`, `#FBEEA4`, `#996515`, `#FFFFFF`, `#A1A1AA`) and vertical metallic gradient bars for countries.
  - **Interactive Criteria Table:** Highlighted rows with gold IDs (`#38`), clean white usernames, and custom gold action badges (`PURCHASE`, `CHECKOUT`, `EXPORT`, `LOGIN`).

---

## Summary Checklist for Interviews

| Feature / Concept | Why Did We Use It? | Interview Talking Point |
| :--- | :--- | :--- |
| **Ehcache 3 L2 Cache** | Avoids repeated SQL lookups for static tables (`Geolocation`, `UserAgent`). | Reduces database load to zero for dimension lookups; data is read from JVM RAM in nanoseconds. |
| **Query Cache (5m TTL)** | Caches expensive multi-row aggregate counts. | Prevents CPU-heavy recalculations on every dashboard refresh. |
| **Persistence Sync** | Automatically invalidates the Query Cache when new data arrives. | Solves the "stale cache" problem by updating Hibernate's `update-timestamps-region`. |
| **`@NamedEntityGraph`** | Solves the N+1 query problem. | Forces Hibernate to do a single SQL `JOIN` instead of 60+ individual queries. |
| **JPA Criteria API** | Dynamic multi-filter searching. | Type-safe, programmatic query construction without messy SQL string concatenation. |
| **Kafka Buffer** | Decouples event ingestion from database writing. | Ingests thousands of clicks per second with sub-5ms response times. |
| **WebSocket (STOMP)** | Pushes live metrics to the dashboard. | Replaces wasteful HTTP polling with a single persistent bidirectional connection. |
