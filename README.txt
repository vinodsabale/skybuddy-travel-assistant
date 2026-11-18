# SkyBuddy Travel Assistant

A microservices travel platform with an AI assistant. Search and book flights and hotels, check weather, and ask policy questions answered from uploaded documents.

## Tech stack

- Java 17, Spring Boot 3.5, Spring Cloud (Eureka, API Gateway)
- Spring AI with Groq (OpenAI compatible API), tool calling and RAG
- H2 file database (no installation needed)
- Local ONNX embeddings and a file based vector store (no Postgres or Ollama)

## Services

| Service | Port | Purpose |
|---|---|---|
| eureka-server | 8761 | Service discovery |
| flight-service | 8081 | Flights and flight bookings |
| hotel-service | 8082 | Hotels and hotel bookings |
| ai-assitent-service | 8083 | AI chat, tools, policy documents (RAG) |
| api-gateway | 8080 | Single entry point and UI |

## How to run

1. Clone the repository and import every folder in Eclipse or STS: **Import > Existing Maven Projects**.
2. Right click each project: **Maven > Update Project (Force Update)**.
3. Create a free API key at [console.groq.com](https://console.groq.com).
4. In `ai-assitent-service` add the environment variable:
   `GROQ_API_KEY=your_key` (Run Configurations > Environment).
   Optional: `GROQ_MODEL` (default `openai/gpt-oss-120b`).
5. Start the services in this order:
   1. eureka-server
   2. flight-service
   3. hotel-service
   4. ai-assitent-service (needs internet on the first run to download the embedding model)
   5. api-gateway
6. Open the app at `http://localhost:8080`.

## Swagger UI

| Service | URL |
|---|---|
| flight-service | http://localhost:8081/swagger-ui.html |
| hotel-service | http://localhost:8082/swagger-ui.html |
| ai-assitent-service | http://localhost:8083/swagger-ui.html |

How to test in Swagger: click an endpoint, press **Try it out**, fill the values, press **Execute**.

## H2 database console

| Service | URL | JDBC URL |
|---|---|---|
| flight-service | http://localhost:8081/h2-console | `jdbc:h2:file:./data/flightdb;DB_CLOSE_ON_EXIT=FALSE` |
| hotel-service | http://localhost:8082/h2-console | `jdbc:h2:file:./data/hoteldb;DB_CLOSE_ON_EXIT=FALSE` |

User name `sa`, password empty.

---

# API guide

Flights are seeded automatically for the next 10 days. Use a date from today onward.

## 1. Flight service (`http://localhost:8081`)

### API 1: Search flights

`GET /api/flights?origin=Mumbai&destination=Goa&date=2026-11-20`

Steps:
1. Open the Swagger page of flight-service.
2. Expand `GET /api/flights` and press **Try it out**.
3. Enter origin `Mumbai`, destination `Goa` and a date.
4. Press **Execute** and note the `id` of a flight.

Seeded routes: Mumbai, Goa, Delhi, Pune, Bengaluru (for example Mumbai to Goa, Mumbai to Delhi, Pune to Goa).

### API 2: Book a flight

`POST /api/bookings`

Steps:
1. Expand `POST /api/bookings` and press **Try it out**.
2. Paste this body. Use the flight `id` from API 1 as `flightId`.
3. Press **Execute** and copy the `pnr` from the response.

```json
{
  "flightId": 1,
  "passengerName": "Vinod Sabale",
  "passengerAge": 25,
  "mobile": "9876543210"
}
```

Mobile must be a 10 digit Indian number starting with 6 to 9.

### API 3: Cancel a booking

`PUT /api/bookings/{pnr}/cancel`

Steps:
1. Expand `PUT /api/bookings/{pnr}/cancel` and press **Try it out**.
2. Enter the `pnr` from API 2 (no body needed).
3. Press **Execute**. The response shows status `CANCELLED` and the refund amount.

Refund rule: 80 percent if cancelled 24 hours or more before departure, otherwise 50 percent.

Other booking endpoints:
- `GET /api/bookings/{pnr}` view a booking
- `GET /api/bookings?mobile=9876543210` list bookings by mobile
- `PUT /api/bookings/{pnr}/passenger` update passenger details
- `PUT /api/bookings/{pnr}/seat` change seat, body `{"seatNumber":"7C"}`

---

## 2. Hotel service (`http://localhost:8082`)

### API 1: Search hotels

`GET /api/hotels?city=Goa&maxPrice=5000`

Steps:
1. Open the Swagger page of hotel-service.
2. Expand `GET /api/hotels` and press **Try it out**.
3. Enter city `Goa` (`maxPrice` is optional).
4. Press **Execute** and note the hotel `id`.

Seeded cities: Goa, Mumbai, Delhi, Pune, Bengaluru.

### API 2: Book a hotel

`POST /api/hotel-bookings`

Steps:
1. Expand `POST /api/hotel-bookings` and press **Try it out**.
2. Paste this body. Use the hotel `id` from API 1 as `hotelId`.
3. Press **Execute** and copy the `reference` from the response.

```json
{
  "hotelId": 1,
  "guestName": "Vinod Sabale",
  "mobile": "9876543210",
  "checkIn": "2026-11-25",
  "checkOut": "2026-11-27",
  "rooms": 1
}
```

Check-in cannot be in the past and check-out must be after check-in.

### API 3: Cancel a hotel booking

`PUT /api/hotel-bookings/{reference}/cancel`

Steps:
1. Expand `PUT /api/hotel-bookings/{reference}/cancel` and press **Try it out**.
2. Enter the `reference` from API 2 (no body needed).
3. Press **Execute**. The status becomes `CANCELLED`.

Other endpoint:
- `GET /api/hotel-bookings/{reference}` view a booking

---

## 3. AI service (`http://localhost:8083`)

Requires `GROQ_API_KEY`.

### API 1: Chat with the assistant

`POST /api/ai/chat`

Steps:
1. Open the Swagger page of ai-assitent-service.
2. Expand `POST /api/ai/chat` and press **Try it out**.
3. Send the first message:

```json
{
  "conversationId": "test1",
  "message": "Mumbai to Goa flights tomorrow"
}
```

4. Send follow up messages with the same `conversationId` so the assistant remembers the chat:

```json
{
  "conversationId": "test1",
  "message": "Book the cheapest one for Vinod Sabale, age 25, mobile 9876543210"
}
```

5. The assistant asks for confirmation. Reply with `yes` to book.

The assistant can also search hotels, show weather, and manage bookings using tool calling.

### API 2: Upload a policy document

`POST /api/ai/documents`

Steps:
1. Expand `POST /api/ai/documents` and press **Try it out**.
2. Under **file**, choose `travel-policy.txt` (PDF, DOCX and TXT are supported).
3. Press **Execute**. The response shows how many chunks were stored.

### API 3: Ask a policy question (RAG)

`POST /api/ai/policy/ask`

Steps:
1. Upload a document first (API 2).
2. Expand `POST /api/ai/policy/ask` and press **Try it out**.
3. Paste this body and press **Execute**:

```json
{
  "question": "How much refund if I cancel 10 hours before departure?"
}
```

Expected answer: 50 percent refund.

Other endpoints:
- `POST /api/ai/chat/stream` streaming chat
- `POST /api/ai/intent` extract travel intent as structured JSON
- `GET /api/ai/history/{conversationId}` and `DELETE` for chat history

---

## All requests through the gateway

Every API above also works through the gateway on port `8080` using the same paths, for example `http://localhost:8080/api/flights?origin=Mumbai&destination=Goa`.

## Notes

- Add `springdoc-openapi-starter-webmvc-ui` (version 2.8.9) to the pom of flight, hotel and AI service to enable Swagger.
- Chat history is kept in memory and is cleared on restart. Uploaded documents are saved in `./data/vector-store.json`.
- Do not commit your Groq API key. Keep it in an environment variable.
