import { mount } from "@vue/test-utils";
import ProductList from "../src/components/ProductList.vue";

const products = [
  { id: 1, name: "Prod 1", price: 10 },
  { id: 2, name: "Prod 2", price: 20 },
];

describe("ProductList", () => {
  it("muestra las filas de productos", () => {
    const wrapper = mount(ProductList, {
      props: {
        products,
        pageNumber: 0,
        pageSize: 5,
        pageSizeOptions: [5, 10],
        totalPages: 1,
        totalElements: products.length,
        loading: false,
      },
    });

    const rows = wrapper.findAll("tbody tr");
    expect(rows.length).toBe(2);
    expect(rows[0].text()).toContain("Prod 1");
    expect(rows[1].text()).toContain("Prod 2");
  });

  it("emite edit, detail y remove", async () => {
    const wrapper = mount(ProductList, {
      props: {
        products,
        pageNumber: 0,
        pageSize: 5,
        pageSizeOptions: [5, 10],
        totalPages: 1,
        totalElements: products.length,
        loading: false,
      },
    });

    const firstRowButtons = wrapper.findAll("tbody tr")[0].findAll("button");
    await firstRowButtons[0].trigger("click");
    await firstRowButtons[1].trigger("click");
    await firstRowButtons[2].trigger("click");

    expect(wrapper.emitted("edit")).toBeTruthy();
    expect(wrapper.emitted("detail")).toBeTruthy();
    expect(wrapper.emitted("remove")).toBeTruthy();
    expect(wrapper.emitted("remove")[0][0]).toBe(1);
  });

  it("emite next-page al hacer click en Siguiente", async () => {
    const wrapper = mount(ProductList, {
      props: {
        products,
        pageNumber: 1,
        pageSize: 5,
        pageSizeOptions: [5, 10],
        totalPages: 3,
        totalElements: products.length,
        loading: false,
      },
    });

    const buttons = wrapper.findAll(".pagination button");
    const nextButton = buttons[1];

    await nextButton.trigger("click");

    expect(wrapper.emitted("next-page")).toBeTruthy();
  });
});
