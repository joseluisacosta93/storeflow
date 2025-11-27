import { createRouter, createWebHistory } from "vue-router";
import ProductsView from "../views/ProductsView.vue";
import ProductDetailView from "../views/ProductDetailView.oldvue";

const routes = [
  {
    path: "/",
    name: "Products",
    component: ProductsView,
  },
  {
    path: "/products/:id",
    name: "ProductDetail",
    component: ProductDetailView,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
