RUN ORDER (Eclipse / STS):
1. eureka-server          (8761)
2. flight-service         (8081)
3. hotel-service          (8082)
4. ai-assitent-service    (8083)  -> needs env variable GROQ_API_KEY (and internet on the very first run)
5. api-gateway            (8080)

Open: http://localhost:8080

Set the key in Eclipse: Run > Run Configurations > ai-assitent-service > Environment > New
   Name: GROQ_API_KEY    Value: gsk_xxxxx
Optional: GROQ_MODEL (default openai/gpt-oss-120b)

Databases: H2 files are created automatically in each service's ./data folder.
H2 console: http://localhost:8081/h2-console and http://localhost:8082/h2-console
  JDBC URL: jdbc:h2:file:./data/flightdb (or hoteldb), user: sa, empty password
After importing: right click each project > Maven > Update Project (Force Update).
