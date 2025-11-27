import { mount, flushPromises } from "@vue/test-utils";
import ProductDetailModal from "../src/components/ProductDetailModal.vue";
import * as inventoryClient from "../src/api/inventoryClient";

describe("ProductDetailModal", () => {
  const product = { id: 1, name: "Prod 1", price: 10 };

  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("loads and shows inventory quantity when modal opens", async () => {
    vi.spyOn(inventoryClient, "getInventoryQuantity").mockResolvedValue({
      data: { attributes: { quantity: 5 } },
    });

    const wrapper = mount(ProductDetailModal, {
      props: {
        visible: false,
        product,
      },
    });

    await wrapper.setProps({ visible: true });
    await flushPromises();

    expect(inventoryClient.getInventoryQuantity).toHaveBeenCalledWith(1);
    expect(wrapper.text()).toContain("Cantidad disponible");
    expect(wrapper.text()).toContain("5");
  });

  it("calls createOrUpdateInventory when saving inventory", async () => {
    vi.spyOn(inventoryClient, "getInventoryQuantity").mockResolvedValue({
      data: { attributes: { quantity: 5 } },
    });
    vi.spyOn(inventoryClient, "createOrUpdateInventory").mockResolvedValue({});

    const wrapper = mount(ProductDetailModal, {
      props: {
        visible: true,
        product,
      },
    });

    await flushPromises();

    const numberInputs = wrapper.findAll("input[type='number']");
    const inventoryInput = numberInputs[0];

    await inventoryInput.setValue("8");

    const buttons = wrapper.findAll("button");
    const saveButton = buttons.find((b) =>
      b.text().includes("Guardar inventario")
    );
    await saveButton.trigger("click");
    await flushPromises();

    expect(inventoryClient.createOrUpdateInventory).toHaveBeenCalledWith(1, 8);
  });

  it("sets error when saving inventory with negative quantity", async () => {
    const wrapper = mount(ProductDetailModal, {
      props: {
        visible: true,
        product,
      },
    });

    wrapper.vm.inventoryFormQuantity = -5;
    await wrapper.vm.saveInventory();

    expect(wrapper.vm.inventoryError).toBe(
      "La cantidad debe ser un número mayor o igual a 0"
    );
  });

  it("sets error when purchasing with invalid quantity", async () => {
    const wrapper = mount(ProductDetailModal, {
      props: {
        visible: true,
        product,
      },
    });

    wrapper.vm.inventoryQuantity = 10;
    wrapper.vm.purchaseQuantity = 0;
    await wrapper.vm.handlePurchase();

    expect(wrapper.vm.purchaseError).toBe(
      "La cantidad a comprar debe ser un número mayor a 0"
    );
  });

  it("sets error when purchasing more than available stock", async () => {
    const wrapper = mount(ProductDetailModal, {
      props: {
        visible: true,
        product,
      },
    });

    wrapper.vm.inventoryQuantity = 3;
    wrapper.vm.purchaseQuantity = 5;
    await wrapper.vm.handlePurchase();

    expect(wrapper.vm.purchaseError).toBe(
      "No hay suficiente inventario para esa compra"
    );
  });

  it("sets error when inventory loading fails", async () => {
    vi.spyOn(inventoryClient, "getInventoryQuantity").mockRejectedValue(
      new Error("fail")
    );

    const wrapper = mount(ProductDetailModal, {
      props: {
        visible: false,
        product,
      },
    });

    await wrapper.setProps({ visible: true });
    await flushPromises();

    expect(wrapper.vm.inventoryError).toBe("Error cargando inventario");
    expect(wrapper.vm.inventoryQuantity).toBeNull();
  });

  it("registers a successful purchase and updates stock", async () => {
    vi.spyOn(inventoryClient, "getInventoryQuantity").mockResolvedValue({
      data: { attributes: { quantity: 10 } },
    });
    vi.spyOn(inventoryClient, "createOrUpdateInventory").mockResolvedValue({});

    const wrapper = mount(ProductDetailModal, {
      props: {
        visible: false,
        product,
      },
    });

    await wrapper.setProps({ visible: true });
    await flushPromises();

    wrapper.vm.purchaseQuantity = 3;
    await wrapper.vm.handlePurchase();
    await flushPromises();

    expect(inventoryClient.createOrUpdateInventory).toHaveBeenCalledWith(1, 7);
    expect(wrapper.vm.inventoryQuantity).toBe(7);
    expect(wrapper.vm.purchaseQuantity).toBe("");
    expect(wrapper.vm.inventorySuccess).toBe(
      "Compra registrada y stock actualizado"
    );
    expect(wrapper.vm.purchaseError).toBeNull();
  });
});
