# StoreFlow – Microservicios de Productos e Inventario + Frontend

Esta solución implementa dos microservicios independientes que se comunican entre sí utilizando **JSON:API**, y un frontend que consume dichos servicios:

- **storeflow-products-service**: gestión de productos.
- **storeflow-inventory-service**: gestión de inventario y compras, consultando al microservicio de productos.
- **storeflow-ui**: interfaz web para listar productos, ver detalle y gestionar inventario.

Los microservicios están desarrollados con **Spring Boot 3**, utilizan **PostgreSQL** (en Docker) en ejecución normal y **H2 en memoria** para la prueba integral automatizada.  
El frontend está desarrollado con **Vue 3 + Vite** y corre en `http://localhost:5173/`.

---

## 1. Requisitos previos

- Java 17
- Maven 3.9+
- Node.js 18+ y npm (para el frontend)
- Docker y Docker Compose
- Git (opcional, si se clona el repositorio)

---

## 2. Estructura del proyecto

```text
StoreFlow/
├─ api/
│  ├─ storeflow-products-service/
│  └─ storeflow-inventory-service/
├─ client/
│  └─ storeflow-ui/           ← frontend (Vue 3 + Vite)
└─ docker/
   └─ docker-compose.yml
