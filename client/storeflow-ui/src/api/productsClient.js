const baseUrl = import.meta.env.VITE_API_PRODUCTS_URL;
const apiKey = import.meta.env.VITE_API_KEY;

async function parseJson(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }
  return JSON.parse(text);
}

export async function createProduct(request) {
  const response = await fetch(`${baseUrl}/products`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-API-KEY": apiKey,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(`Error creando producto: ${response.status}`);
  }

  const json = await parseJson(response);
  if (!json || !json.data || !json.data.attributes) {
    throw new Error("Respuesta inesperada del servidor");
  }

  return json.data.attributes;
}

export async function listProducts(pageNumber = 0, pageSize = 10) {
  const params = new URLSearchParams();
  params.set("page[number]", String(pageNumber));
  params.set("page[size]", String(pageSize));

  const response = await fetch(`${baseUrl}/products?${params.toString()}`, {
    headers: {
      "X-API-KEY": apiKey,
    },
  });

  if (!response.ok) {
    throw new Error(`Error obteniendo productos: ${response.status}`);
  }

  const json = await parseJson(response);
  if (!json || !Array.isArray(json.data)) {
    throw new Error("Respuesta inesperada del servidor");
  }

  return json.data.map((item) => item.attributes);
}

export async function getProductById(id) {
  const response = await fetch(`${baseUrl}/products/${id}`, {
    headers: {
      "X-API-KEY": apiKey,
    },
  });

  if (!response.ok) {
    throw new Error(`Error obteniendo producto: ${response.status}`);
  }

  const json = await parseJson(response);
  if (!json || !json.data || !json.data.attributes) {
    throw new Error("Respuesta inesperada del servidor");
  }

  return json.data.attributes;
}

export async function updateProduct(id, request) {
  const response = await fetch(`${baseUrl}/products/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      "X-API-KEY": apiKey,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(`Error actualizando producto: ${response.status}`);
  }

  const json = await parseJson(response);
  if (!json || !json.data || !json.data.attributes) {
    throw new Error("Respuesta inesperada del servidor");
  }

  return json.data.attributes;
}

export async function deleteProduct(id) {
  const response = await fetch(`${baseUrl}/products/${id}`, {
    method: "DELETE",
    headers: {
      "X-API-KEY": apiKey,
    },
  });

  if (!response.ok) {
    throw new Error(`Error eliminando producto: ${response.status}`);
  }
}
