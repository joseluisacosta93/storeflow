import { mount } from "@vue/test-utils";
import ProductForm from "../src/components/ProductForm.vue";

describe("ProductForm", () => {
  it("emite submit con name y price", async () => {
    const wrapper = mount(ProductForm, {
      props: {
        initialName: "",
        initialPrice: "",
        editingProductId: null,
        loading: false,
      },
    });

    const nameInput = wrapper.get("input[type='text']");
    const priceInput = wrapper.get("input[type='number']");
    await nameInput.setValue("Producto test");
    await priceInput.setValue("123.45");

    await wrapper.find("form").trigger("submit.prevent");

    const events = wrapper.emitted("submit");
    expect(events).toBeTruthy();
    const payload = events[0][0];
    expect(payload.name).toBe("Producto test");
    expect(payload.price).toBe(123.45);
  });

  it("emite reset al presionar el botón limpiar", async () => {
    const wrapper = mount(ProductForm, {
      props: {
        initialName: "Inicial",
        initialPrice: 10,
        editingProductId: 1,
        loading: false,
      },
    });

    await wrapper.get("button[type='button']").trigger("click");

    expect(wrapper.emitted("reset")).toBeTruthy();
  });
});
