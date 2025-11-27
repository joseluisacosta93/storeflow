<template>
  <section class="card">
    <h2>Listado de productos</h2>

    <div class="toolbar">
      <div>
        <label>Tamaño de página</label>
        <select :value="pageSize" @change="onChangePageSize">
          <option v-for="size in pageSizeOptions" :key="size" :value="size">
            {{ size }}
          </option>
        </select>
      </div>
      <div class="pagination">
        <button @click="onPrevPage" :disabled="pageNumber === 0 || loading">
          Anterior
        </button>
        <span>Página {{ pageNumber + 1 }} de {{ totalPages || 1 }}</span>
        <button @click="onNextPage" :disabled="loading">Siguiente</button>
      </div>
    </div>

    <div v-if="loading">Cargando productos...</div>
    <div v-else-if="!products || products.length === 0">No hay productos.</div>
    <table v-else class="products-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Precio</th>
          <th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in products" :key="p.id">
          <td>{{ p.id }}</td>
          <td>{{ p.name }}</td>
          <td>{{ p.price }}</td>
          <td class="actions">
            <button @click="$emit('edit', p)">Editar</button>
            <button @click="$emit('detail', p)">Detalle / Comprar</button>
            <button @click="$emit('remove', p.id)">Eliminar</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p class="total-elements">Total elementos: {{ totalElements }}</p>
  </section>
</template>

<script setup>
const props = defineProps({
  products: { type: Array, default: () => [] },
  pageNumber: { type: Number, default: 0 },
  pageSize: { type: Number, default: 10 },
  pageSizeOptions: { type: Array, default: () => [5, 10, 20, 50] },
  totalPages: { type: Number, default: 0 },
  totalElements: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
});

const emit = defineEmits([
  "change-page-size",
  "prev-page",
  "next-page",
  "edit",
  "detail",
  "remove",
]);

function onChangePageSize(event) {
  const value = Number(event.target.value);
  emit("change-page-size", value);
}

function onPrevPage() {
  emit("prev-page");
}

function onNextPage() {
  emit("next-page");
}
</script>

<style scoped>
.card {
  background: #ffffff;
  border-radius: 8px;
  padding: 1rem 1.25rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.06);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.products-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 0.5rem;
}

.products-table th,
.products-table td {
  border: 1px solid #e5e7eb;
  padding: 0.5rem;
  font-size: 0.875rem;
}

.products-table th {
  background: #f9fafb;
  text-align: left;
}

.actions {
  display: flex;
  gap: 0.25rem;
}

.total-elements {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #4b5563;
}
</style>
