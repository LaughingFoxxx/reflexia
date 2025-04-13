# Reflexia
Backend микросервисного приложения по обработке текстовых данных

### Ключевые технологии:
  - **Java (Concurrency + Collections):** работа с Kafka через CompletableFuture, ConcurrentHashMap (Central-service)
  - **Spring Boot**
  - **Spring Cloud Gateway**
  - **Nimbus JOSE + JWT:** реализован аутентификационный сервис для пользователей и сервисов. Через refresh и access токены для пользователей и OAuth2 для внутренних сервисов приложения
  - **FastAPI:** между Django и FastAPI был выбран второй, так как среди этих двух, именно FastAPI поддерживает асинхронность
  - **Kafka:** pipline для взаимодействия через нейросеть. Обеспечивает надежность и асинхронность
  - **Redis:** черный список JWT-токенов пользователей. Использование Redis обусловлено скоростью работы, так как черный список JWT-токенов проверяется при каждом запросе
  - **PostgreSQL:** база данных с авторизационными данными пользователей (изолированное взаимодействие только с Auth-сервисом)
  - **MongoDB:** база данных с документами пользователей. Использование NoSQL базы данных обусловлено предполагаемым функционалом по работе с файлами
