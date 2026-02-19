# Tool Store Demo

A Spring Boot REST application simulating a point-of-sale tool rental system. The app exposes a single checkout endpoint that validates a request, calculates chargeable days using weekend/holiday pricing rules, and returns a **Rental Agreement**. The agreement is also printed to the application console — a demo stand-in for production logging. There is no UI or database.

---

## Project Structure

```
src/
├── main/java/com.toolstore.demo/
│   ├── controller/       # Checkout REST endpoint
│   ├── model/            # Tool, ToolType, RentalAgreement
│   ├── service/          # Checkout orchestration and charge calculation logic
│   ├── dto/              # Checkout Request logic
│   ├── view/             # Formatting console output
│   └── repository/       # In memory map
└── test/java/
    ├── controller/       # Controller-level JUnit tests (6 spec scenarios + extras)
    └── service/          # Service-level JUnit tests (6 spec scenarios duplicated)

AI_Conversations/         # All AI prompts and responses (PDF + TXT)
testCoverageReport/       # IntelliJ-generated test coverage report
```

---

## Running the App

> Requires Java 25 and Gradle.

**Build:**
```bash
./gradlew build
```

**Run:**
```bash
./gradlew bootRun
```

The app starts on `http://localhost:8080` by default.

**Run Tests:**
```bash
./gradlew test
```

**Test Coverage:**
Coverage was measured using IntelliJ's built-in coverage runner and exported to the `testCoverageReport/` directory. Current coverage sits at **94%**.

---

## API

### `POST /api/checkout`

Accepts a checkout request and returns a Rental Agreement.

**Request Body (JSON):**
```json
{
  "toolCode": "JAKD",
  "rentalDayCount": 6,
  "discountPercent": 0,
  "checkoutDate": "09/03/15"
}
```

**Field Validation:**
| Field | Rules |
|---|---|
| `toolCode` | Required |
| `rentalDayCount` | Required, must be `>= 1` |
| `discountPercent` | Required, must be `0–100` |
| `checkoutDate` | Required, format `MM/dd/yy` |

**Error Response Format:**
Validation errors return a user-friendly message, e.g.:
`Request Body Invalid: Rental day count must be 1 or greater`

---

## Sample curl Commands

**Valid checkout — Jackhammer, 6 days, no discount (Spec Test 4):**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"toolCode":"JAKD","rentalDayCount":6,"discountPercent":0,"checkoutDate":"09/03/15"}'
```

**Valid checkout — Ladder, 3 days, 10% discount (Spec Test 2):**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"toolCode":"LADW","rentalDayCount":3,"discountPercent":10,"checkoutDate":"07/02/20"}'
```

**Valid checkout — Chainsaw, 5 days, 25% discount (Spec Test 3):**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"toolCode":"CHNS","rentalDayCount":5,"discountPercent":25,"checkoutDate":"07/02/15"}'
```

**Valid checkout — Jackhammer, 4 days, 50% discount (Spec Test 6):**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"toolCode":"JAKR","rentalDayCount":4,"discountPercent":50,"checkoutDate":"07/02/20"}'
```

**Invalid — discount out of range (Spec Test 1, expects error):**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"toolCode":"JAKR","rentalDayCount":5,"discountPercent":101,"checkoutDate":"09/03/15"}'
```

**Invalid — rental day count less than 1 (expects error):**
```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"toolCode":"LADW","rentalDayCount":0,"discountPercent":10,"checkoutDate":"07/02/20"}'
```

---

## Testing

JUnit tests are organized at two levels to independently verify HTTP wiring and business logic:

- **Controller tests** — cover the 6 required specification scenarios plus a few additional validation/edge cases using `MockMvc`.
- **Service/charge tests** — duplicate the same 6 scenarios directly against the charge service, independent of the HTTP layer.

### Specification Scenarios Covered

| Test | Tool Code | Checkout Date | Rental Days | Discount | Expected Outcome |
|------|-----------|---------------|-------------|----------|-----------------|
| 1 | JAKR | 09/03/15 | 5 | 101% | Validation error |
| 2 | LADW | 07/02/20 | 3 | 10% | Valid agreement |
| 3 | CHNS | 07/02/15 | 5 | 25% | Valid agreement |
| 4 | JAKD | 09/03/15 | 6 | 0% | Valid agreement |
| 5 | JAKR | 07/02/15 | 9 | 0% | Valid agreement |
| 6 | JAKR | 07/02/20 | 4 | 50% | Valid agreement |

---

## Edge Cases & Business Rules

- **Charge days** are counted from the **day after checkout** through and including the **due date**.
- Tool-type chargeability:
    - Ladder: weekday ✅ weekend ✅ holiday ❌
    - Chainsaw: weekday ✅ weekend ❌ holiday ✅
    - Jackhammer: weekday ✅ weekend ❌ holiday ❌
- **Holidays recognized:**
    - Independence Day (July 4th) — observed on the nearest weekday if it falls on a weekend.
    - Labor Day — first Monday of September.
- **Rounding:** All monetary values rounded half-up to cents.

---

## AI Usage

AI was used transparently throughout the project:

1. **Test case analysis** — the 6 required specification scenarios were analyzed with an LLM to confirm expected outputs and surface edge cases before implementation.
2. **SOLID review** — after initial template coding, each class was passed through an LLM with a focused prompt to check for SOLID principle adherence and readability improvements.
3. **High-level design** — early architectural discussions were assisted by AI.

All conversations (prompts, responses, and refinements) are saved in `AI_Conversations/` in both PDF and docx format, per the project's AI usage guidelines.

---

## Out of Scope / Future Improvements

- Assign a UUID to each Rental Agreement and persist it as a receipt.
- Add a `GET /api/agreements/{id}` endpoint to retrieve prior agreements.
- Introduce a database layer.
- Reduce test duplication by extracting shared input/output fixtures reusable across controller and service tests.