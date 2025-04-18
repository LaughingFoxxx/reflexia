Backend микросервисного приложения по обработке текстовых данных

*!! Проект не залит одним коммитом !!* 
- **Основной репозиторий приватный** и обновляется мной постоянно по мере изменений проекта. **Данный репозиторий является публичной демонстрацией и обновляется по мере накопления большого количества изменений**, в которых я буду уверен

### Основные реализованные функции и фичи:
- Три микросервиса на языке Java (+ Spring Boot (Central, Gateway, Auth)), а также один микросервис на языке Python (+ FastAPI)
- **Gateway API** реактивный Gateway, который проверяет JWT-токены на Auth-сервисе перед тем, как пропускать пользователя к бизнес-логике, также проверяет Redis базу с черным списком JWT-токенов
- **Auth-сервис** написан на Spring Boot + Spring Security + Nimbus JOSE (знаю, что можно было использовать Keycloak, но мне хотелось научиться работать с авторизацией и аутентификацией самостоятельно и понять лучше этот процесс. В случае чего, можно легко перейти на Keycloak), работает с пользователями по схеме refresh-access токенов. Также планируется добавить авторизацию микросервисов. Имеется своя PostgreSQL база данных пользователей, а также добавляет JWT в черный список в базе Redis при выходе пользователя из аккаунта
- **Central-сервис** написан на Spring Boot. В нем же используется Spring Kafka для работы с брокером сообщений Kafka. Кафку решил использовать для асинхронного и более надежного взаимодействия с Python-сервисом, так как это всё таки самая основная логика и большинство запросов будет именно на нее, поэтому pipline реализован именно таким образом. Основная цепочка обращений к нейросети также реализована через CompletableFuture и ConcurrentHashMap для реализации буфера запросов. Также у него имеется своя MongoDB база данных, в которой хранятся документы пользователей. Использование именно MongoDB было обуч=словлено именно тем, что в приложении предполагается работа с файлами.
- **Python-сервис** небольшой асинхронный сервис для нейросети и взаимодействия с ней

### Ключевые технологии:
  - **Java (Concurrency, Collections):** работа с Kafka через CompletableFuture, ConcurrentHashMap (Central-service)
  - **Spring Boot**
  - **Spring Cloud Gateway**
  - **Spring Boot, Spring Security, Nimbus JOSE + JWT:** реализован аутентификационный сервис для пользователей и сервисов. Через refresh и access токены для пользователей (планируется также OAuth2 авторизация для сервисов)
  - **FastAPI:** между Django и FastAPI был выбран второй, так как среди этих двух, именно FastAPI поддерживает асинхронность
  - **Kafka (Spring Kafka):** pipline для взаимодействия через нейросеть. Обеспечивает надежность и асинхронность
  - **Redis:** черный список JWT-токенов пользователей. Использование Redis обусловлено скоростью работы, так как черный список JWT-токенов проверяется при каждом запросе
  - **PostgreSQL:** база данных с авторизационными данными пользователей (изолированное взаимодействие только с Auth-сервисом)
  - **MongoDB (Spring Data, Mongo Template):** база данных с документами пользователей. Использование NoSQL базы данных обусловлено предполагаемым функционалом по работе с файлами
