<template>
  <section class="card">
    <h2>Crear / Editar producto</h2>
    <form @submit.prevent="onSubmit" class="form-grid">
      <div class="form-field">
        <label>Nombre</label>
        <input v-model="name" type="text" required />
      </div>
      <div class="form-field">
        <label>Precio</label>
        <input
          v-model.number="price"
          type="number"
          min="0"
          step="0.01"
          required
        />
      </div>
      <div class="form-actions">
        <button type="submit" :disabled="loading">
          {{ editingProductId ? "Actualizar" : "Crear" }}
        </button>
        <button type="button" @click="onReset" :disabled="loading">
          Limpiar
        </button>
      </div>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">{{ success }}</p>
  </section>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  initialName: { type: String, default: "" },
  initialPrice: { type: [Number, String], default: "" },
  editingProductId: { type: [String, Number, null], default: null },
  loading: { type: Boolean, default: false },
  error: { type: String, default: null },
  success: { type: String, default: null },
});

const emit = defineEmits(["submit", "reset"]);

const name = ref(props.initialName);
const price = ref(props.initialPrice);

watch(
  () => props.initialName,
  (val) => {
    name.value = val;
  }
);

watch(
  () => props.initialPrice,
  (val) => {
    price.value = val;
  }
);

function onSubmit() {
  emit("submit", { name: name.value, price: price.value });
}

function onReset() {
  name.value = "";
  price.value = "";
  emit("reset");
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
  align-items: flex-end;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-field label {
  font-size: 0.875rem;
  font-weight: 600;
}

.form-field input {
  padding: 0.4rem 0.5rem;
  border-radius: 4px;
  border: 1px solid #cbd5f5;
}

.form-actions {
  display: flex;
  gap: 0.5rem;
}
</style>
