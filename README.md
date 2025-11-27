
# StoreFlow - Microservicios de Productos e Inventario

Esta solución implementa dos microservicios independientes que se comunican entre sí utilizando JSON:API:

- **storeflow-products-service**: gestión de productos.
- **storeflow-inventory-service**: gestión de inventario y compras, consultando al microservicio de productos.

Ambos servicios están desarrollados con **Spring Boot 3**, utilizan **PostgreSQL** (en Docker) en ejecución normal y **H2 en memoria** para la prueba integral automatizada.

---

## 1. Requisitos previos

- Java 17
- Maven 3.9+
- Docker y Docker Compose
- Git (opcional, si se clona el repositorio)

---

## 2. Estructura del proyecto

```text
StoreFlow/
├─ api/
│  ├─ storeflow-products-service/
│  └─ storeflow-inventory-service/
└─ docker/
   └─ docker-compose.yml
```

---

## 3. Levantar todo con Docker

Desde la raíz del proyecto (`StoreFlow/`):

```bash
cd docker
docker compose up -d
```

Esto levanta:

- `products-db`: PostgreSQL para **storeflow-products-service**
- `inventory-db`: PostgreSQL para **storeflow-inventory-service**
- `storeflow-products-service`: microservicio de productos en `http://localhost:8081`
- `storeflow-inventory-service`: microservicio de inventario en `http://localhost:8082`

Para ver el estado de los contenedores:

```bash
docker compose ps
```

Para apagar todo:

```bash
docker compose down
```

---

## 4. Ejecutar los microservicios localmente (sin Docker para los servicios)

> Nota: normalmente usas Docker para todo, pero también puedes levantar los microservicios desde Maven apuntando a las BDs del Docker Compose.

### 4.1. storeflow-products-service

Desde la raíz del proyecto:

```bash
cd api/storeflow-products-service
mvn spring-boot:run
```

El servicio quedará en:

- `http://localhost:8081`

### 4.2. storeflow-inventory-service

Desde la raíz del proyecto:

```bash
cd api/storeflow-inventory-service
mvn spring-boot:run
```

El servicio quedará en:

- `http://localhost:8082`

---

## 5. Endpoints principales

### 5.1. Products Service (puerto 8081)

Base path: `/api/v1/products`  
Header obligatorio: `X-API-KEY: secret-public-api-key`

- **Crear producto**

  ```http
  POST /api/v1/products
  X-API-KEY: secret-public-api-key
  Content-Type: application/json

  {
    "name": "Product A",
    "price": 10.50
  }
  ```

- **Obtener producto por ID**

  ```http
  GET /api/v1/products/{id}
  X-API-KEY: secret-public-api-key
  ```

- **Actualizar producto**

  ```http
  PUT /api/v1/products/{id}
  X-API-KEY: secret-public-api-key
  Content-Type: application/json

  {
    "name": "Product A (updated)",
    "price": 12.00
  }
  ```

- **Eliminar producto**

  ```http
  DELETE /api/v1/products/{id}
  X-API-KEY: secret-public-api-key
  ```

- **Listar productos con paginación**

  ```http
  GET /api/v1/products?page=0&size=10
  X-API-KEY: secret-public-api-key
  ```

Las respuestas siguen el estándar **JSON:API**.

---

### 5.2. Inventory Service (puerto 8082)

Base path: `/api/v1/inventories`  
Header obligatorio: `X-API-KEY: secret-public-api-key`

- **Crear o actualizar inventario de un producto**

  ```http
  POST /api/v1/inventories
  X-API-KEY: secret-public-api-key
  Content-Type: application/json

  {
    "productId": 1,
    "quantity": 10
  }
  ```

- **Registrar compra (descuenta del inventario)**

  ```http
  POST /api/v1/inventories/purchase
  X-API-KEY: secret-public-api-key
  Content-Type: application/json

  {
    "productId": 1,
    "quantity": 3
  }
  ```

- **Consultar inventario de un producto**

  ```http
  GET /api/v1/inventories?productId=1
  X-API-KEY: secret-public-api-key
  ```

También aquí las respuestas usan **JSON:API**.

---

## 6. Seguridad (API Key)

Ambos microservicios esperan la API Key en el header:

```http
X-API-KEY: secret-public-api-key
```

Si no se envía o es incorrecta, responden `401 Unauthorized`.

---

## 7. Documentación OpenAPI / Swagger

Cada microservicio expone Swagger UI:

- **Products**:  
  `http://localhost:8081/swagger-ui.html`

- **Inventory**:  
  `http://localhost:8082/swagger-ui.html`

---

## 8. Prueba integral automatizada (Inventory + Products)

Hay una prueba de integración que valida el flujo completo usando:

- BD H2 en memoria para **inventory**
- Microservicio real de **products** en `http://localhost:8081`
- API Key `secret-public-api-key`

### 8.1. Pasos previos

1. Asegúrate de tener `storeflow-products-service` levantado:

   - O bien con Docker (recomendado):

     ```bash
     cd docker
     docker compose up -d
     ```

     Esto ya levanta el microservicio de productos en `http://localhost:8081`.

   - O manualmente:

     ```bash
     cd api/storeflow-products-service
     mvn spring-boot:run
     ```

2. Verifica que `http://localhost:8081/api/v1/products` responde (por ejemplo, con navegador o curl/Postman).

### 8.2. Ejecutar la prueba integral

Desde la carpeta del inventario:

```bash
cd api/storeflow-inventory-service
IT_WITH_PRODUCTS=true mvn -Dtest=InventoryProductsIntegrationTest test
```

La prueba hace lo siguiente:

1. Crea un producto real llamando al microservicio de productos (8081) con `X-API-KEY: secret-public-api-key`.
2. Crea inventario para ese producto en el microservicio de inventario (usando H2 en memoria).
3. Registra una compra y descuenta cantidad.
4. Consulta el inventario final y valida que la cantidad se haya actualizado correctamente.

Si todo está OK, verás algo como:

```text
[INFO] BUILD SUCCESS
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

---

## 9. Resumen rápido

- Levantar todo con Docker:

  ```bash
  cd docker
  docker compose up -d
  ```

- Probar el flujo manualmente:
  - Crear producto en `http://localhost:8081/api/v1/products`
  - Crear inventario y comprar en `http://localhost:8082/api/v1/inventories`

- Probar el flujo automatizado:

  ```bash
  cd api/storeflow-inventory-service
  IT_WITH_PRODUCTS=true mvn -Dtest=InventoryProductsIntegrationTest test
  ```

Con esto cualquier persona puede instalar, levantar y probar la solución completa.
