from fastapi import FastAPI
import asyncio
import json
from aiokafka import AIOKafkaProducer, AIOKafkaConsumer
from yandex_cloud_ml_sdk import YCloudML
from typing import Optional

app = FastAPI()

# Конфигурация Kafka
KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
REQUEST_TOPIC = "text-processing-requests"
RESPONSE_TOPIC = "text-processing-response"

# Конфигурация Yandex Cloud ML SDK
FOLDER_ID = ""
API_KEY = ""

# Глобальные переменные для producer, consumer и sdk
producer: Optional[AIOKafkaProducer] = None
consumer: Optional[AIOKafkaConsumer] = None
sdk: Optional[YCloudML] = None


async def process_text(instruction: str, text: str) -> str:
    """Асинхронно обрабатывает текст с помощью YandexGPT через asyncio.to_thread."""
    messages = [
        {"role": "system", "text": instruction},
        {"role": "user", "text": text},
    ]

    def sync_process():
        """Синхронная функция для выполнения запроса к YandexGPT."""
        try:
            result = sdk.models.completions("yandexgpt").configure(temperature=0.4).run(messages)
            for alternative in result:
                return alternative.text if hasattr(alternative, "text") else str(alternative)
            return "Ошибка: результат не получен"
        except Exception as e:
            print(f"Ошибка YandexGPT: {e}")
            return f"Ошибка обработки: {str(e)}"

    # Выполняем синхронный код в отдельном потоке
    return await asyncio.to_thread(sync_process)


async def kafka_consumer_loop():
    """Асинхронная обработка сообщений из Kafka."""
    global producer, consumer

    try:
        while True:
            msg = await consumer.getone()  # Асинхронное получение сообщения

            try:
                message = json.loads(msg.value.decode("utf-8"))
                request_id = message["requestId"]
                text = message["text"]
                instruction = message["instruction"]
            except (json.JSONDecodeError, KeyError) as e:
                print(f"Ошибка парсинга сообщения: {e}")
                continue

            # Асинхронная обработка текста
            processed_text = await process_text(instruction, text)

            # Формируем и отправляем ответ
            response = {
                "requestId": request_id,
                "processedText": processed_text
            }

            try:
                await producer.send_and_wait(
                    RESPONSE_TOPIC,
                    json.dumps(response).encode("utf-8")
                )
                print(f"Отправлен результат для requestId: {request_id}")
            except Exception as e:
                print(f"Ошибка при отправке в Kafka: {e}")

    except Exception as e:
        print(f"Ошибка в consumer loop: {e}")
    finally:
        await consumer.stop()


@app.on_event("startup")
async def startup_event():
    """Инициализация асинхронных компонентов при запуске."""
    global producer, consumer, sdk

    # Инициализация асинхронного Kafka Producer
    producer = AIOKafkaProducer(bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS)
    await producer.start()

    # Инициализация асинхронного Kafka Consumer
    consumer = AIOKafkaConsumer(
        REQUEST_TOPIC,
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        group_id="python-service-group",
        auto_offset_reset="earliest"
    )
    await consumer.start()

    # Инициализация синхронного Yandex Cloud ML SDK
    sdk = YCloudML(folder_id=FOLDER_ID, auth=API_KEY)

    # Запуск consumer loop как фоновой задачи
    asyncio.create_task(kafka_consumer_loop())
    print("Асинхронный Kafka Consumer запущен")


@app.on_event("shutdown")
async def shutdown_event():
    """Очистка ресурсов при остановке."""
    global producer, consumer

    if producer:
        await producer.stop()
    if consumer:
        await consumer.stop()
    print("Ресурсы очищены")


@app.get("/")
async def root():
    return {"message": "Асинхронный Python-сервис на FastAPI работает"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)