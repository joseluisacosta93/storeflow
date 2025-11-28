<template>
  <div v-if="visible" class="modal-backdrop">
    <div class="modal">
      <header class="modal-header">
        <h2>Detalle de producto</h2>
        <button class="close-btn" @click="emit('close')">×</button>
      </header>

      <section class="modal-body" v-if="product">
        <div class="product-info">
          <p><strong>ID:</strong> {{ product.id }}</p>
          <p><strong>Nombre:</strong> {{ product.name }}</p>
          <p><strong>Precio:</strong> {{ product.price }}</p>
        </div>

        <div class="inventory-section">
          <h3>Inventario</h3>

          <div v-if="inventoryLoading">Cargando inventario...</div>
          <div v-else>
            <p v-if="inventoryError" class="error">{{ inventoryError }}</p>
            <p v-else>
              Cantidad disponible:
              <strong>
                {{
                  inventoryQuantity === null
                    ? "Sin registro"
                    : inventoryQuantity
                }}
              </strong>
            </p>

            <div class="inventory-forms">
              <div class="inventory-card">
                <h4>Actualizar inventario</h4>
                <label>Nueva cantidad</label>
                <input
                  v-model.number="inventoryFormQuantity"
                  type="number"
                  min="0"
                  :disabled="inventorySaving"
                />
                <button
                  @click="saveInventory"
                  :disabled="inventorySaving || inventoryFormQuantity === ''"
                >
                  Guardar inventario
                </button>
              </div>

              <div class="inventory-card">
                <h4>Comprar</h4>
                <label>Cantidad a comprar</label>
                <input
                  v-model.number="purchaseQuantity"
                  type="number"
                  min="1"
                  :disabled="inventorySaving"
                />
                <p v-if="purchaseError" class="error">{{ purchaseError }}</p>
                <button
                  @click="handlePurchase"
                  :disabled="inventorySaving || !purchaseQuantity"
                >
                  Comprar
                </button>
              </div>
            </div>

            <p v-if="inventorySuccess" class="success">
              {{ inventorySuccess }}
            </p>
          </div>
        </div>
      </section>

      <footer class="modal-footer">
        <button @click="emit('close')" :disabled="inventorySaving">
          Cerrar
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import {
  getInventoryQuantity,
  createOrUpdateInventory,
} from "../api/inventoryClient";

const props = defineProps({
  visible: { type: Boolean, default: false },
  product: { type: Object, default: null },
});

const emit = defineEmits(["close"]);

const inventoryQuantity = ref(null);
const inventoryLoading = ref(false);
const inventoryError = ref(null);
const inventoryFormQuantity = ref("");
const inventorySaving = ref(false);
const inventorySuccess = ref(null);

const purchaseQuantity = ref("");
const purchaseError = ref(null);

watch(
  () => ({ visible: props.visible, id: props.product?.id }),
  (val) => {
    if (val.visible && val.id != null) {
      resetState();
      loadInventory(val.id);
    }
  }
);

function resetState() {
  inventoryQuantity.value = null;
  inventoryFormQuantity.value = "";
  inventoryError.value = null;
  inventorySuccess.value = null;
  purchaseQuantity.value = "";
  purchaseError.value = null;
}

async function loadInventory(productId) {
  inventoryLoading.value = true;
  inventoryError.value = null;

  try {
    const result = await getInventoryQuantity(productId);
    let quantity = null;

    if (typeof result === "number") {
      quantity = result;
    } else if (result && typeof result.quantity === "number") {
      quantity = result.quantity;
    } else if (result?.data?.attributes?.quantity != null) {
      quantity = result.data.attributes.quantity;
    } else if (
      Array.isArray(result?.data) &&
      result.data[0]?.attributes?.quantity != null
    ) {
      quantity = result.data[0].attributes.quantity;
    } else if (result?.data?.data?.attributes?.quantity != null) {
      quantity = result.data.data.attributes.quantity;
    }

    inventoryQuantity.value = quantity;
  } catch (e) {
    console.error(e);
    inventoryError.value = "Error cargando inventario";
    inventoryQuantity.value = null;
  } finally {
    inventoryLoading.value = false;
  }
}

async function saveInventory() {
  if (!props.product) return;
  if (inventoryFormQuantity.value === "") return;

  const newQuantity = Number(inventoryFormQuantity.value);
  if (Number.isNaN(newQuantity) || newQuantity < 0) {
    inventoryError.value = "La cantidad debe ser un número mayor o igual a 0";
    return;
  }

  inventorySaving.value = true;
  inventoryError.value = null;
  inventorySuccess.value = null;

  try {
    await createOrUpdateInventory(props.product.id, newQuantity);
    inventoryQuantity.value = newQuantity;
    inventorySuccess.value = "Inventario actualizado correctamente";
    inventoryFormQuantity.value = "";
  } catch (e) {
    console.error(e);
    inventoryError.value = "Error actualizando inventario";
  } finally {
    inventorySaving.value = false;
  }
}

async function handlePurchase() {
  if (!props.product) return;

  const qty = Number(purchaseQuantity.value);
  purchaseError.value = null;
  inventorySuccess.value = null;

  if (Number.isNaN(qty) || qty <= 0) {
    purchaseError.value = "La cantidad a comprar debe ser un número mayor a 0";
    return;
  }

  if (inventoryQuantity.value === null) {
    purchaseError.value = "No hay inventario registrado para este producto";
    return;
  }

  if (qty > inventoryQuantity.value) {
    purchaseError.value = "No hay suficiente inventario para esa compra";
    return;
  }

  const newQuantity = inventoryQuantity.value - qty;

  inventorySaving.value = true;
  try {
    await createOrUpdateInventory(props.product.id, newQuantity);
    inventoryQuantity.value = newQuantity;
    purchaseQuantity.value = "";
    inventorySuccess.value = "Compra registrada y stock actualizado";
  } catch (e) {
    console.error(e);
    purchaseError.value = "Error registrando la compra";
  } finally {
    inventorySaving.value = false;
  }
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 100%;
  padding: 0.75rem 1rem 1rem;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.25);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.close-btn {
  background: transparent;
  color: #111827;
  font-size: 1.25rem;
  line-height: 1;
  padding: 0;
}

.product-info p {
  margin: 0.15rem 0;
}

.inventory-section h3 {
  margin-bottom: 0.5rem;
}

.inventory-forms {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.inventory-card {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 0.5rem;
}

.inventory-card h4 {
  margin-bottom: 0.5rem;
}

.inventory-card label {
  display: block;
  font-size: 0.8rem;
  margin-bottom: 0.25rem;
}

.inventory-card input {
  width: 100%;
  padding: 0.35rem 0.4rem;
  border-radius: 4px;
  border: 1px solid #cbd5f5;
  margin-bottom: 0.4rem;
}

.modal-footer {
  margin-top: 0.75rem;
  text-align: right;
}
</style>
