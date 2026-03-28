# 🏢 DEALER & VEHICLE INVENTORY MODULE

## Task Breakdown & Architecture Guide

**Framework**: Spring Boot 4.x | **Language**: Java 25 | **Type**: Modular Monolith | **Pattern**: Clean Architecture

---

## 📋 **Overview**

Build a **multi-tenant inventory module** that manages:
- **Dealers** and their subscription tiers
- **Vehicles** associated with each dealer

Key principles:
- ✅ Clean Architecture with **clear separation of concerns**
- ✅ **Tenant isolation** enforced at every layer
- ✅ **Return 400** if X-Tenant-Id is missing
- ✅ **Return 403** for cross-tenant access attempts
- ✅ **SOLID & DDD** principles

---

## 🏗️ **Clean Architecture Layers**

### Layer Breakdown

| Layer | Responsibility | Key Examples |
|-------|-----------------|--------------|
| **Controller** | HTTP request handling, X-Tenant-Id validation, routing | `DealerController`, `VehicleController` |
| **Service** | Business logic, tenant enforcement, orchestration | `DealerService`, `VehicleService` |
| **Domain/Entity** | JPA entities, domain logic, enums | `Dealer`, `Vehicle`, `SubscriptionType`, `VehicleStatus` |
| **Repository** | Database access, tenant-scoped queries, filtering | `DealerRepository`, `VehicleRepository` (custom methods) |
| **Validator** | Request validation (DTOs), business rule checks | `CreateDealerValidator`, `PriceRangeValidator` |
| **DTO** | Request/Response models (contract layer) | `CreateDealerRequest`, `DealerResponse`, `VehicleResponse` |
| **Exception Handler** | Global error handling, standardized responses | `TenantMissingException`, `CrossTenantAccessException` |

---

## 📊 **Data Model**

### Entity: Dealer

| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `id` | UUID | ❌ No | Primary Key, auto-generated |
| `tenant_id` | UUID | ❌ No | **Required for multi-tenancy** |
| `name` | String | ❌ No | Dealer name |
| `email` | String | ❌ No | Unique per tenant (composite unique key: tenant_id + email) |
| `subscriptionType` | Enum | ❌ No | Values: `BASIC`, `PREMIUM` |
| `createdAt` | LocalDateTime | ❌ No | Timestamp |
| `updatedAt` | LocalDateTime | ❌ No | Timestamp |

**Relationships**:
- One Dealer → Many Vehicles (1:N)

---

### Entity: Vehicle

| Field | Type | Nullable | Notes |
|-------|------|----------|-------|
| `id` | UUID | ❌ No | Primary Key, auto-generated |
| `tenant_id` | UUID | ❌ No | **Required for multi-tenancy** |
| `dealerId` | UUID | ❌ No | Foreign Key → Dealer.id (same tenant validation!) |
| `model` | String | ❌ No | Car model name |
| `price` | BigDecimal | ❌ No | Numeric precision (e.g., 19,2) |
| `status` | Enum | ❌ No | Values: `AVAILABLE`, `SOLD` |
| `createdAt` | LocalDateTime | ❌ No | Timestamp |
| `updatedAt` | LocalDateTime | ❌ No | Timestamp |

**Relationships**:
- Many Vehicles → One Dealer (N:1)
- Always validate dealerId belongs to same tenant

---

## 🔌 **API Endpoints**

### Dealers

#### 1️⃣ POST /dealers
**Create a new dealer**

```
POST /dealers
X-Tenant-Id: {tenantId}
Content-Type: application/json

{
  "name": "John's Auto",
  "email": "john@auto.com",
  "subscriptionType": "PREMIUM"
}
```

**Response (201 Created)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John's Auto",
  "email": "john@auto.com",
  "subscriptionType": "PREMIUM"
}
```

**Errors**:
- `400 Bad Request` — Missing X-Tenant-Id or invalid payload
- `409 Conflict` — Email already exists for this tenant

---

#### 2️⃣ GET /dealers/{id}
**Retrieve dealer by ID**

```
GET /dealers/550e8400-e29b-41d4-a716-446655440000
X-Tenant-Id: {tenantId}
```

**Response (200 OK)**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John's Auto",
  "email": "john@auto.com",
  "subscriptionType": "PREMIUM"
}
```

**Errors**:
- `400 Bad Request` — Missing X-Tenant-Id
- `403 Forbidden` — Dealer belongs to different tenant
- `404 Not Found` — Dealer doesn't exist

---

#### 3️⃣ GET /dealers
**List all dealers with pagination & sorting**

```
GET /dealers?page=0&size=10&sort=name,asc
X-Tenant-Id: {tenantId}
```

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "John's Auto",
      "email": "john@auto.com",
      "subscriptionType": "PREMIUM"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "name": "Mary's Motors",
      "email": "mary@motors.com",
      "subscriptionType": "BASIC"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "currentPage": 0
}
```

---

#### 4️⃣ PATCH /dealers/{id}
**Update dealer (partial)**

```
PATCH /dealers/550e8400-e29b-41d4-a716-446655440000
X-Tenant-Id: {tenantId}
Content-Type: application/json

{
  "subscriptionType": "BASIC"
}
```

**Response (200 OK)**: Updated dealer

**Errors**:
- `403 Forbidden` — Cross-tenant access
- `404 Not Found` — Dealer doesn't exist

---

#### 5️⃣ DELETE /dealers/{id}
**Delete dealer & cascade delete vehicles**

```
DELETE /dealers/550e8400-e29b-41d4-a716-446655440000
X-Tenant-Id: {tenantId}
```

**Response (204 No Content)**

⚠️ **Important**: Deleting a dealer should cascade-delete all associated vehicles

---

### Vehicles

#### 1️⃣ POST /vehicles
**Create a new vehicle**

```
POST /vehicles
X-Tenant-Id: {tenantId}
Content-Type: application/json

{
  "dealerId": "550e8400-e29b-41d4-a716-446655440000",
  "model": "Tesla Model 3",
  "price": 45000.00,
  "status": "AVAILABLE"
}
```

**Response (201 Created)**:
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "dealerId": "550e8400-e29b-41d4-a716-446655440000",
  "model": "Tesla Model 3",
  "price": 45000.00,
  "status": "AVAILABLE"
}
```

**Errors**:
- `400 Bad Request` — Missing X-Tenant-Id or invalid payload
- `404 Not Found` — dealerId doesn't exist (or belongs to different tenant!)

---

#### 2️⃣ GET /vehicles/{id}
**Retrieve vehicle by ID**

```
GET /vehicles/770e8400-e29b-41d4-a716-446655440002
X-Tenant-Id: {tenantId}
```

**Response (200 OK)**:
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "dealerId": "550e8400-e29b-41d4-a716-446655440000",
  "model": "Tesla Model 3",
  "price": 45000.00,
  "status": "AVAILABLE"
}
```

---

#### 3️⃣ GET /vehicles (with filters, pagination, sorting)
**List vehicles with advanced filtering**

```
GET /vehicles?model=Tesla&status=AVAILABLE&priceMin=40000&priceMax=50000&page=0&sort=price,desc
X-Tenant-Id: {tenantId}
```

**Supported Query Parameters**:
- `model` — Filter by model name (substring match)
- `status` — Filter by status (AVAILABLE or SOLD)
- `priceMin` — Filter by minimum price
- `priceMax` — Filter by maximum price
- `page` — Page number (0-indexed)
- `size` — Page size (default 10)
- `sort` — Sort criteria (e.g., `price,asc` or `model,desc`)

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "dealerId": "550e8400-e29b-41d4-a716-446655440000",
      "model": "Tesla Model 3",
      "price": 45000.00,
      "status": "AVAILABLE"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

---

#### 4️⃣ PATCH /vehicles/{id}
**Update vehicle (partial)**

```
PATCH /vehicles/770e8400-e29b-41d4-a716-446655440002
X-Tenant-Id: {tenantId}
Content-Type: application/json

{
  "status": "SOLD"
}
```

**Response (200 OK)**: Updated vehicle

---

#### 5️⃣ DELETE /vehicles/{id}
**Delete vehicle**

```
DELETE /vehicles/770e8400-e29b-41d4-a716-446655440002
X-Tenant-Id: {tenantId}
```

**Response (204 No Content)**

---

### Advanced Queries

#### 🔍 GET /vehicles?subscription=PREMIUM
**Filter vehicles by dealer subscription type**

```
GET /vehicles?subscription=PREMIUM&page=0
X-Tenant-Id: {tenantId}
```

**Logic**:
1. Filter vehicles by vehicles.tenant_id = X-Tenant-Id
2. **JOIN** Vehicle → Dealer
3. Filter where Dealer.subscriptionType = PREMIUM
4. Return paginated results

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "dealerId": "550e8400-e29b-41d4-a716-446655440000",
      "model": "Tesla Model 3",
      "price": 45000.00,
      "status": "AVAILABLE",
      "dealerSubscription": "PREMIUM"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

**⚠️ Key Point**: Only show vehicles from PREMIUM dealers **within the caller's tenant**

---

#### 👨‍💼 GET /admin/dealers/countBySubscription
**Admin-only: Count dealers by subscription type**

```
GET /admin/dealers/countBySubscription
X-Tenant-Id: {tenantId}
X-Role: GLOBAL_ADMIN
```

**Response (200 OK)**:
```json
{
  "BASIC": 3,
  "PREMIUM": 7
}
```

**Errors**:
- `400 Bad Request` — Missing X-Tenant-Id
- `403 Forbidden` — User is not GLOBAL_ADMIN

**❓ Decision Point**: Should this count be:
- **Per-tenant** (recommended) — Count dealers in the caller's tenant only
- **Global** — Count all dealers across all tenants

**Recommendation**: **Per-tenant** for isolation & security

---

## ✅ **Acceptance Criteria**

### 1. Tenant Isolation
- ✅ **Missing X-Tenant-Id** → Return `400 Bad Request`
  ```json
  { "error": "X-Tenant-Id header is required" }
  ```

- ✅ **Cross-Tenant Access** → Return `403 Forbidden`
  ```json
  { "error": "Access denied: resource belongs to different tenant" }
  ```

### 2. Filtering
- ✅ **subscription=PREMIUM** returns only vehicles whose dealer is PREMIUM **within the caller's tenant**
- ✅ All other filters work correctly (model, status, priceMin/Max)

### 3. Admin Access
- ✅ **GET /admin/dealers/countBySubscription** requires `GLOBAL_ADMIN` role
- ✅ Returns correct count (per-tenant recommended)

### 4. Data Integrity
- ✅ Deleting a dealer cascades to delete its vehicles
- ✅ Creating a vehicle validates that dealerId exists in same tenant
- ✅ Email uniqueness per tenant (not global)

---

## 🔐 **Security & Tenant Enforcement Strategy**

### At Every Layer:

#### 1. **Controller / Interceptor Layer**
```java
// Extract tenant from header
String tenantId = request.getHeader("X-Tenant-Id");
if (tenantId == null || tenantId.isEmpty()) {
    return ResponseEntity.badRequest().body("X-Tenant-Id is required");
}

// Store in ThreadLocal or RequestScope for service layer access
TenantContext.setTenantId(tenantId);
```

#### 2. **Service Layer**
```java
// NEVER trust the caller's input
// Always enforce tenant_id on queries
public Dealer getDealerById(String dealerId) {
    String tenantId = TenantContext.getTenantId();
    return dealerRepository.findByIdAndTenantId(dealerId, tenantId)
        .orElseThrow(() -> new EntityNotFoundException(...));
}
```

#### 3. **Repository Layer**
```java
// ALWAYS filter by tenant_id in WHERE clause
public interface DealerRepository extends JpaRepository<Dealer, String> {
    Optional<Dealer> findByIdAndTenantId(String id, String tenantId);
    
    @Query("SELECT d FROM Dealer d WHERE d.tenantId = ?1 AND d.name = ?2")
    Optional<Dealer> findByNameInTenant(String tenantId, String name);
}
```

#### 4. **Business Logic**
```java
// Validate entity ownership before modification
public void createVehicle(CreateVehicleRequest req) {
    String tenantId = TenantContext.getTenantId();
    
    // Verify dealer exists AND belongs to this tenant
    Dealer dealer = dealerRepository.findByIdAndTenantId(req.getDealerId(), tenantId)
        .orElseThrow(() -> new EntityNotFoundException("Dealer not found in your tenant"));
    
    Vehicle vehicle = new Vehicle();
    vehicle.setTenantId(tenantId); // Always set tenant_id
    vehicle.setDealerId(dealer.getId());
    // ... other fields ...
    vehicleRepository.save(vehicle);
}
```

---

## 📝 **Implementation Checklist**

### Phase 1: Foundation (HIGH PRIORITY)

- [ ] **Entities**
  - [ ] Create `Dealer` entity with JPA annotations
  - [ ] Create `Vehicle` entity with JPA annotations
  - [ ] Add enums: `SubscriptionType`, `VehicleStatus`
  - [ ] Add indexes: `(tenant_id)`, `(tenant_id, dealerId)`, `(tenant_id, email)` on Dealer

- [ ] **Repositories**
  - [ ] `DealerRepository` with custom methods:
    - `findByIdAndTenantId(id, tenantId)`
    - `findByEmailAndTenantId(email, tenantId)`
    - `findAllByTenantId(tenantId, Pageable)`
  - [ ] `VehicleRepository` with custom methods:
    - `findByIdAndTenantId(id, tenantId)`
    - `findAllByTenantIdAndFilters(...)`
    - `findByTenantIdAndDealerSubscription(tenantId, subscription, Pageable)`

- [ ] **DTOs**
  - [ ] `CreateDealerRequest`, `UpdateDealerRequest`, `DealerResponse`
  - [ ] `CreateVehicleRequest`, `UpdateVehicleRequest`, `VehicleResponse`
  - [ ] `VehicleFilterCriteria` (for complex filters)

- [ ] **Tenant Context**
  - [ ] Create `TenantContext` (ThreadLocal or RequestScoped bean)
  - [ ] Add `TenantInterceptor` to extract & validate X-Tenant-Id

### Phase 2: Service & Validation (HIGH PRIORITY)

- [ ] **Services**
  - [ ] `DealerService` — CRUD + tenant enforcement
  - [ ] `VehicleService` — CRUD + filters + subscription filter + tenant enforcement

- [ ] **Validators**
  - [ ] `CreateDealerValidator` (email format, required fields)
  - [ ] `CreateVehicleValidator` (price > 0, model length, dealerId exists in tenant)
  - [ ] `PriceRangeValidator` (priceMin < priceMax)

### Phase 3: Controllers (HIGH PRIORITY)

- [ ] **DealerController**
  - [ ] POST /dealers
  - [ ] GET /dealers/{id}
  - [ ] GET /dealers (with pagination/sort)
  - [ ] PATCH /dealers/{id}
  - [ ] DELETE /dealers/{id}

- [ ] **VehicleController**
  - [ ] POST /vehicles
  - [ ] GET /vehicles/{id}
  - [ ] GET /vehicles (with filters, pagination, sort)
  - [ ] PATCH /vehicles/{id}
  - [ ] DELETE /vehicles/{id}
  - [ ] GET /vehicles?subscription=PREMIUM

- [ ] **AdminController**
  - [ ] GET /admin/dealers/countBySubscription (GLOBAL_ADMIN only)

### Phase 4: Error Handling & Security (MEDIUM PRIORITY)

- [ ] **Global Exception Handler**
  - [ ] `TenantMissingException` → 400
  - [ ] `CrossTenantAccessException` → 403
  - [ ] `EntityNotFoundException` → 404
  - [ ] `ConflictException` (email exists) → 409

- [ ] **Authorization**
  - [ ] Add `@PreAuthorize("hasRole('GLOBAL_ADMIN')")` on admin endpoints
  - [ ] Verify role-based access

- [ ] **Request Validation**
  - [ ] Add `@Valid` annotations
  - [ ] Use `javax.validation.constraints`

### Phase 5: Testing & Documentation (MEDIUM PRIORITY)

- [ ] **Unit Tests**
  - [ ] `DealerServiceTest` — Test CRUD + tenant enforcement
  - [ ] `VehicleServiceTest` — Test filters + subscription filter
  - [ ] `DealerRepositoryTest` — Test custom queries

- [ ] **Integration Tests**
  - [ ] Test endpoints with X-Tenant-Id header
  - [ ] Test 403 on cross-tenant access
  - [ ] Test filters work correctly

- [ ] **Documentation**
  - [ ] Document admin count endpoint scope (per-tenant vs global)
  - [ ] Add API documentation (OpenAPI/Swagger)

---

## 💡 **Key Design Tips for Success**

### 1. **Don't Over-Engineer for Java 25**
- Focus on **SOLID principles** and **clean architecture**
- Don't use fancy Java 25 features just because they exist
- Readability & maintainability > language version

### 2. **Separate Concerns Rigorously**
```
Controller → DTO Validation → Service → Business Logic → Repository → JPA
```
Each layer has **one job**. Don't skip layers.

### 3. **Tenant Context is Cross-Cutting**
Use **ThreadLocal** or **Spring RequestScope** to avoid passing tenantId everywhere:
```java
// In controller
TenantContext.setTenantId(header);

// In service (no need to pass it)
String tenantId = TenantContext.getTenantId();

// Clean up with interceptor
```

### 4. **Query Complexity: Subscription Filter**
The `subscription=PREMIUM` filter requires a **JOIN**:
```sql
SELECT v.* FROM vehicles v
JOIN dealers d ON v.dealer_id = d.id
WHERE v.tenant_id = ? AND d.tenant_id = ? AND d.subscription_type = 'PREMIUM'
```

Do this in the **service layer**, not the controller:
```java
// VehicleService
public Page<VehicleResponse> findBySubscription(String subscription, Pageable pageable) {
    String tenantId = TenantContext.getTenantId();
    return vehicleRepository.findByTenantIdAndDealerSubscription(tenantId, subscription, pageable);
}
```

### 5. **Admin Counts Endpoint**
Decide **per-tenant** (recommended) or **global** early:
- **Per-tenant**: Only count dealers in current tenant (safer, isolated)
- **Global**: Count all dealers (admin oversight)

**Recommendation**: Go **per-tenant** for multi-tenant best practices.

### 6. **Email Uniqueness**
Email should be unique **per tenant**, not globally:
```java
// WRONG: @Column(unique = true)

// RIGHT: Composite unique constraint
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "email"})
})
```

### 7. **Pagination & Sorting**
Use **Spring Data JPA** `Pageable`:
```java
// Controller
@GetMapping("/dealers")
public ResponseEntity<?> listDealers(@PageableDefault(size = 10) Pageable pageable) {
    Page<DealerResponse> page = dealerService.findAll(pageable);
    return ResponseEntity.ok(page);
}
```

### 8. **Cascade Delete**
When deleting a dealer, cascade-delete vehicles:
```java
@Entity
public class Dealer {
    @OneToMany(mappedBy = "dealer", cascade = CascadeType.DELETE)
    private List<Vehicle> vehicles;
}
```

---

## 🎯 **Success Criteria Summary**

| Criterion | Expected | How to Verify |
|-----------|----------|--------------|
| **Tenant Isolation** | 400 if missing X-Tenant-Id | Test without header |
| **Cross-Tenant Block** | 403 if accessing other tenant's data | Use different tenant headers |
| **Subscription Filter** | Only PREMIUM dealer vehicles | Test with ?subscription=PREMIUM |
| **Admin Endpoint** | Only GLOBAL_ADMIN can access | Test with/without GLOBAL_ADMIN role |
| **Cascade Delete** | Deleting dealer removes vehicles | Count vehicles before/after |
| **Pagination** | Works with page, size, sort | Test ?page=0&size=5&sort=price,asc |
| **Filters** | model, status, priceMin/Max work | Test each filter combination |
| **Unique Email** | Per tenant, not global | Try duplicate email in same tenant |

---

## 📦 **Recommended Project Structure**

```
src/main/java/com/example/inventory/
├── config/
│   ├── TenantInterceptor.java
│   └── SecurityConfig.java
├── controller/
│   ├── DealerController.java
│   ├── VehicleController.java
│   └── AdminController.java
├── service/
│   ├── DealerService.java
│   └── VehicleService.java
├── repository/
│   ├── DealerRepository.java
│   └── VehicleRepository.java
├── entity/
│   ├── Dealer.java
│   ├── Vehicle.java
│   ├── SubscriptionType.java
│   └── VehicleStatus.java
├── dto/
│   ├── request/
│   │   ├── CreateDealerRequest.java
│   │   └── CreateVehicleRequest.java
│   └── response/
│       ├── DealerResponse.java
│       └── VehicleResponse.java
├── validator/
│   ├── CreateDealerValidator.java
│   └── CreateVehicleValidator.java
├── exception/
│   ├── TenantMissingException.java
│   ├── CrossTenantAccessException.java
│   └── GlobalExceptionHandler.java
├── context/
│   └── TenantContext.java
└── InventoryModuleApplication.java
```

---

## 🚀 **Next Steps**

1. **Start with entities** — Get your JPA mappings right
2. **Build repositories** with custom tenant-scoped methods
3. **Implement services** — Focus on business logic & tenant enforcement
4. **Write validators** — Fail fast with clear error messages
5. **Build controllers** — Keep them thin, delegate to services
6. **Add security** — X-Tenant-Id interceptor, role-based access
7. **Test thoroughly** — Unit tests, integration tests, edge cases
8. **Document decisions** — Especially admin counts scope (per-tenant vs global)

---

**Good luck! You've got this! 💪**

Remember: **Clean code > clever code**. Focus on clarity, separation of concerns, and tenant isolation.
