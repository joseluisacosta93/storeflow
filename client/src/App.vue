<template>
  <div class="app">
    <h1>Gestión de Productos e Inventario</h1>

    <ProductForm
      :initial-name="formInitialName"
      :initial-price="formInitialPrice"
      :editing-product-id="editingProductId"
      :loading="loading"
      :error="error"
      :success="success"
      @submit="handleSubmitProduct"
      @reset="handleResetForm"
    />

    <ProductList
      :products="products"
      :page-number="pageNumber"
      :page-size="pageSize"
      :page-size-options="pageSizeOptions"
      :total-pages="totalPages"
      :total-elements="totalElements"
      :loading="loading"
      @change-page-size="handleChangePageSize"
      @prev-page="handlePrevPage"
      @next-page="handleNextPage"
      @edit="handleEditProduct"
      @detail="handleOpenDetail"
      @remove="handleRemoveProduct"
    />

    <ProductDetailModal
      :visible="showDetailModal"
      :product="detailProduct"
      @close="closeDetail"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import ProductForm from "./components/ProductForm.vue";
import ProductList from "./components/ProductList.vue";
import ProductDetailModal from "./components/ProductDetailModal.vue";
import {
  createProduct,
  listProducts,
  updateProduct,
  deleteProduct,
} from "./api/productsClient";

const products = ref([]);
const pageNumber = ref(0);
const pageSize = ref(10);
const pageSizeOptions = [5, 10, 20, 50];
const totalPages = ref(0);
const totalElements = ref(0);

const loading = ref(false);
const error = ref(null);
const success = ref(null);

const editingProductId = ref(null);
const formInitialName = ref("");
const formInitialPrice = ref("");

const showDetailModal = ref(false);
const detailProduct = ref(null);

async function loadProducts() {
  loading.value = true;
  error.value = null;

  try {
    const result = await listProducts(pageNumber.value, pageSize.value);

    let rawData = [];

    if (Array.isArray(result?.data)) {
      rawData = result.data;
    } else if (Array.isArray(result?.content)) {
      rawData = result.content;
    } else if (Array.isArray(result)) {
      rawData = result;
    } else if (Array.isArray(result?.data?.data)) {
      rawData = result.data.data;
    }

    const items = rawData.map((item) => ({
      id: item.id ?? item.productId ?? item.uuid,
      name: item.attributes?.name ?? item.name,
      price: item.attributes?.price ?? item.price,
    }));

    products.value = items;

    const metaSource = result?.meta ?? result;
    const pageMeta = metaSource.page ?? metaSource;

    let totalPagesFromMeta =
      pageMeta.totalPages ?? pageMeta.total_pages ?? undefined;

    let totalElementsFromMeta =
      pageMeta.totalElements ??
      pageMeta.total_elements ??
      pageMeta.totalItems ??
      undefined;

    if (!Number.isFinite(totalElementsFromMeta)) {
      totalElementsFromMeta = items.length;
    }

    totalElements.value = totalElementsFromMeta;

    if (!Number.isFinite(totalPagesFromMeta)) {
      totalPagesFromMeta = Math.max(
        1,
        Math.ceil(totalElements.value / pageSize.value)
      );
    }

    totalPages.value = totalPagesFromMeta;
  } catch (e) {
    console.error(e);
    error.value = "Error cargando productos";
    products.value = [];
    totalPages.value = 1;
    totalElements.value = 0;
  } finally {
    loading.value = false;
  }
}

async function handleSubmitProduct(payload) {
  error.value = null;
  success.value = null;

  try {
    if (editingProductId.value) {
      await updateProduct(editingProductId.value, {
        name: payload.name,
        price: payload.price,
      });
      success.value = "Producto actualizado correctamente";
    } else {
      await createProduct({
        name: payload.name,
        price: payload.price,
      });
      success.value = "Producto creado correctamente";
    }

    handleResetForm();
    await loadProducts();
  } catch (e) {
    console.error(e);
    error.value = "Error guardando el producto";
  }
}

function handleResetForm() {
  editingProductId.value = null;
  formInitialName.value = "";
  formInitialPrice.value = "";
  error.value = null;
  success.value = null;
}

function handleEditProduct(product) {
  editingProductId.value = product.id;
  formInitialName.value = product.name;
  formInitialPrice.value = product.price;
  error.value = null;
  success.value = null;
}

async function handleRemoveProduct(productId) {
  if (!confirm("¿Seguro que deseas eliminar este producto?")) return;
  error.value = null;
  success.value = null;
  try {
    await deleteProduct(productId);
    success.value = "Producto eliminado correctamente";
    await loadProducts();
  } catch (e) {
    console.error(e);
    error.value = "Error eliminando el producto";
  }
}

function handleChangePageSize(newSize) {
  pageSize.value = newSize;
  pageNumber.value = 0;
  loadProducts();
}

function handleNextPage() {
  pageNumber.value += 1;
  loadProducts();
}

function handlePrevPage() {
  if (pageNumber.value > 0) {
    pageNumber.value -= 1;
    loadProducts();
  }
}

function handleOpenDetail(product) {
  detailProduct.value = product;
  showDetailModal.value = true;
}

function closeDetail() {
  showDetailModal.value = false;
  detailProduct.value = null;
}

onMounted(() => {
  loadProducts();
});
</script>

<style>
:root {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI",
    sans-serif;
}

body {
  margin: 0;
  background: #f3f4f6;
}

.app {
  max-width: 960px;
  margin: 0 auto;
  padding: 1.5rem;
}

h1 {
  margin-bottom: 1.5rem;
  text-align: center;
}

button {
  border: none;
  border-radius: 4px;
  padding: 0.4rem 0.75rem;
  cursor: pointer;
  font-size: 0.875rem;
  background: #2563eb;
  color: white;
}

button[disabled] {
  opacity: 0.6;
  cursor: default;
}

.error {
  margin-top: 0.5rem;
  color: #b91c1c;
  font-size: 0.875rem;
}

.success {
  margin-top: 0.5rem;
  color: #15803d;
  font-size: 0.875rem;
}
</style>
