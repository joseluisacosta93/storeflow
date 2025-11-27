import {
  getInventoryQuantity,
  createOrUpdateInventory,
} from "../src/api/inventoryClient";

describe("inventoryClient", () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  it("getInventoryQuantity devuelve la cantidad cuando la respuesta es 200 OK", async () => {
    global.fetch.mockResolvedValue({
      status: 200,
      ok: true,
      text: vi.fn().mockResolvedValue(
        JSON.stringify({
          data: {
            attributes: {
              quantity: 7,
            },
          },
        })
      ),
    });

    const qty = await getInventoryQuantity(1);

    expect(global.fetch).toHaveBeenCalledTimes(1);
    const [url, options] = global.fetch.mock.calls[0];
    expect(url).toContain("/inventories?productId=1");
    expect(options.method).toBe("GET");
    expect(options.headers["X-API-KEY"]).toBeDefined();
    expect(qty).toBe(7);
  });

  it("getInventoryQuantity devuelve 0 cuando la respuesta es 404", async () => {
    global.fetch.mockResolvedValue({
      status: 404,
      ok: false,
      text: vi.fn().mockResolvedValue("not found"),
    });

    const qty = await getInventoryQuantity(2);

    expect(global.fetch).toHaveBeenCalledTimes(1);
    expect(qty).toBe(0);
  });

  it("getInventoryQuantity lanza error cuando el status no es OK ni 404", async () => {
    global.fetch.mockResolvedValue({
      status: 500,
      ok: false,
      text: vi.fn().mockResolvedValue("server error"),
    });

    await expect(getInventoryQuantity(3)).rejects.toThrow(
      "Error obteniendo inventario: 500 server error"
    );
  });

  it("getInventoryQuantity lanza error cuando el cuerpo no tiene la estructura esperada", async () => {
    global.fetch.mockResolvedValue({
      status: 200,
      ok: true,
      text: vi.fn().mockResolvedValue("{}"),
    });

    await expect(getInventoryQuantity(4)).rejects.toThrow(
      "Respuesta inesperada del servidor de inventario"
    );
  });

  it("createOrUpdateInventory devuelve los atributos cuando la respuesta es OK", async () => {
    global.fetch.mockResolvedValue({
      status: 201,
      ok: true,
      text: vi.fn().mockResolvedValue(
        JSON.stringify({
          data: {
            attributes: {
              productId: 5,
              quantity: 12,
            },
          },
        })
      ),
    });

    const attrs = await createOrUpdateInventory(5, 12);

    expect(global.fetch).toHaveBeenCalledTimes(1);
    const [url, options] = global.fetch.mock.calls[0];
    expect(url).toContain("/inventories");
    expect(options.method).toBe("POST");
    expect(options.headers["Content-Type"]).toBe("application/json");
    expect(JSON.parse(options.body)).toEqual({ productId: 5, quantity: 12 });
    expect(attrs).toEqual({ productId: 5, quantity: 12 });
  });

  it("createOrUpdateInventory lanza error cuando la respuesta no es OK", async () => {
    global.fetch.mockResolvedValue({
      status: 400,
      ok: false,
      text: vi.fn().mockResolvedValue("bad request"),
    });

    await expect(createOrUpdateInventory(1, 5)).rejects.toThrow(
      "Error guardando inventario: 400 bad request"
    );
  });

  it("createOrUpdateInventory lanza error cuando el cuerpo no tiene la estructura esperada", async () => {
    global.fetch.mockResolvedValue({
      status: 200,
      ok: true,
      text: vi.fn().mockResolvedValue(""),
    });

    await expect(createOrUpdateInventory(1, 5)).rejects.toThrow(
      "Respuesta inesperada del servidor de inventario"
    );
  });
});
