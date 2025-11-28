const baseUrl = import.meta.env.VITE_API_INVENTORY_URL;
const apiKey = import.meta.env.VITE_API_KEY;

async function parseJson(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }
  return JSON.parse(text);
}

export async function getInventoryQuantity(productId) {
  const url = `${baseUrl}/inventories?productId=${encodeURIComponent(
    String(productId)
  )}`;

  const response = await fetch(url, {
    method: "GET",
    headers: {
      "X-API-KEY": apiKey,
    },
  });

  if (response.status === 404) {
    return 0;
  }

  if (!response.ok) {
    const body = await response.text();
    throw new Error(
      `Error obteniendo inventario: ${response.status} ${body || ""}`.trim()
    );
  }

  const json = await parseJson(response);
  if (!json || !json.data || !json.data.attributes) {
    throw new Error("Respuesta inesperada del servidor de inventario");
  }

  return json.data.attributes.quantity;
}

export async function createOrUpdateInventory(productId, quantity) {
  const url = `${baseUrl}/inventories`;

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-API-KEY": apiKey,
    },
    body: JSON.stringify({
      productId,
      quantity,
    }),
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error(
      `Error guardando inventario: ${response.status} ${body || ""}`.trim()
    );
  }

  const json = await parseJson(response);
  if (!json || !json.data || !json.data.attributes) {
    throw new Error("Respuesta inesperada del servidor de inventario");
  }

  return json.data.attributes;
}
