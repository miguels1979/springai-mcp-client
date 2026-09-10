# springain-mcp-client

Cliente Spring Boot que expone un asistente conversacional de clima construido con [Spring AI](https://docs.spring.io/spring-ai/reference/). El asistente usa un modelo LLM servido por **Ollama** y, mediante el protocolo **MCP (Model Context Protocol)**, delega en un servidor MCP remoto las herramientas necesarias para consultar el clima y sugerir recomendaciones de vestimenta.

## Arquitectura

```
Cliente HTTP  ──►  WeatherController  ──►  WeatherAiService  ──►  ChatClient (Spring AI)
                                                                        │
                                                                        ├──► Ollama (qwen3:4b) — LLM
                                                                        │
                                                                        └──► Servidor MCP "weather-server"
                                                                             (streamable-http, tools de clima)
```

- **`WeatherController`**: expone el endpoint REST `GET /api/weather/ask`.
- **`WeatherAiService` / `WeatherAiServiceImpl`**: orquesta el `ChatClient`, define el prompt de sistema (asistente de clima que responde en español) y delega la ejecución al modelo.
- **`AiConfig`**: registra el `ChatClient` con las *tools* del MCP client (`SyncMcpToolCallbackProvider`), permitiendo que el LLM invoque automáticamente las herramientas expuestas por el servidor MCP cuando la pregunta lo requiera.
- **Servidor MCP `weather-server`**: proceso externo (no incluido en este repositorio) que expone herramientas de clima vía HTTP *streamable*.

## Tecnologías

| Componente          | Detalle                                  |
|---------------------|-------------------------------------------|
| Lenguaje            | Java 25                                   |
| Framework           | Spring Boot 4.1.1                         |
| IA                  | Spring AI 2.0.1                           |
| Proveedor de modelo | Ollama (`qwen3:4b`)                       |
| Integración MCP     | `spring-ai-starter-mcp-client` (transporte *streamable-http*) |
| Build               | Maven (con wrapper `mvnw`)                |

## Requisitos previos

1. **Java 25** instalado y configurado.
2. **Ollama** corriendo localmente en `http://localhost:11434` con el modelo `qwen3:4b` descargado:
   ```bash
   ollama pull qwen3:4b
   ollama serve
   ```
3. Un **servidor MCP de clima** disponible en `http://localhost:8081/mcp` (transporte streamable-http). Este servidor debe exponer las *tools* que el asistente utilizará para responder preguntas sobre el clima.

## Configuración

La configuración se encuentra en `src/main/resources/application.properties`:

```properties
spring.application.name=springain-mcp-client

# Modelo LLM (Ollama)
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen3:4b

# Cliente MCP
spring.ai.mcp.client.enabled=true
spring.ai.mcp.client.initialized=true
spring.ai.mcp.client.request-timeout=6000s

# Conexión al servidor MCP de clima
spring.ai.mcp.client.streamable-http.connections.weather-server.url=http://localhost:8081/mcp
```

Ajusta estos valores según tu entorno (host/puerto de Ollama, modelo, URL del servidor MCP, etc.).

## Cómo ejecutar

Con el wrapper de Maven incluido en el proyecto:

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

La aplicación se levanta por defecto en `http://localhost:8080`.

## Uso de la API

### `GET /api/weather/ask`

Realiza una pregunta en lenguaje natural sobre el clima. El asistente responderá en español y, si es necesario, invocará las herramientas del servidor MCP para obtener datos actualizados.

**Parámetros de query:**

| Parámetro  | Tipo   | Obligatorio | Descripción                      |
|------------|--------|--------------|-----------------------------------|
| `question` | string | Sí           | Pregunta del usuario en lenguaje natural |

**Ejemplo de solicitud:**

```bash
curl -G "http://localhost:8080/api/weather/ask" \
  --data-urlencode "question=¿Qué clima hace hoy en Buenos Aires y qué ropa me recomiendas?"
```

**Ejemplo de respuesta:**

```
Hoy en Buenos Aires se esperan 22°C con cielo parcialmente nublado.
Te recomiendo llevar una campera liviana para la noche.
```

## Compilar y testear

```bash
./mvnw clean verify
```

Los tests se encuentran en `src/test/java/.../SpringainMcpClientApplicationTests.java` y validan el arranque del contexto de Spring.

## Estructura del proyecto

```
src/main/java/com/miguel/course/springai/apps/mcpclient/
├── SpringainMcpClientApplication.java   # Punto de entrada Spring Boot
├── config/
│   └── AiConfig.java                    # Configuración del ChatClient + tools MCP
├── controllers/
│   └── WeatherController.java           # Endpoint REST
└── services/
    ├── WeatherAiService.java            # Contrato del servicio
    └── WeatherAiServiceImpl.java        # Implementación con Spring AI ChatClient
```

## Notas

- El proyecto está preparado para cambiar de proveedor de modelo: la dependencia de OpenAI está incluida como comentario en `pom.xml` por si se desea reemplazar Ollama.
- El `request-timeout` del cliente MCP está configurado en `6000s`, pensado para modelos locales que pueden tardar en responder.
