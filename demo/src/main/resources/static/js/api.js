const API = {
  ordenesActivas: async () => {
    const url = `/api/ordenes/activas?_=${Date.now()}`; // evita caché
    const res = await fetch(url, { cache: 'no-store' });
    if (!res.ok) throw new Error('Error al cargar órdenes activas');
    return res.json();
  },
  ordenesFinalizadas: async () => {
    const res = await fetch(`/api/ordenes/finalizadas?_=${Date.now()}`, { cache: 'no-store' });
    if (!res.ok) throw new Error('Error al cargar órdenes finalizadas');
    return res.json();
  },
  ordenById: async (id) => {
    const res = await fetch(`/api/ordenes/${encodeURIComponent(id)}`);
    if (!res.ok) throw new Error('Error al cargar orden');
    return res.json();
  },
  valesPorOP: async (opId) => {
    const res = await fetch(`/api/produccion/vales?opId=${encodeURIComponent(opId)}`);
    if (!res.ok) throw new Error('Error al cargar vales');
    return res.json();
  },
  iniciarVale: async (opId) => {
    const res = await fetch(`/api/produccion/iniciar?idOrden=${encodeURIComponent(opId)}`, { method: 'POST' });
    if (!res.ok) {
      let msg = 'Error al iniciar vale';
      try { const j = await res.json(); if (j?.error) msg = j.error; } catch {}
      throw new Error(msg);
    }
    return res.json();
  },
  requisitos: async ({ codigoVale, puesto }) => {
    const url = `/api/produccion/requisitos?codigoVale=${encodeURIComponent(codigoVale)}&puesto=${encodeURIComponent(puesto)}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Error al cargar checklist');
    return res.json();
  },
  escanear: async ({ codigoVale, puesto, codigoComponente }) => {
    const form = new URLSearchParams({ codigoVale, puesto, codigoComponente });
    const res = await fetch('/api/produccion/scan', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: form
    });
    if (!res.ok) throw new Error('Error al escanear');
    return res.text();
  },
  crearOrden: async ({ codigoOrden, productoFinalId, formulaId, cantidad, fechaInicio }) => {
    const res = await fetch('/api/ordenes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ codigoOrden, productoFinalId, formulaId, cantidad, fechaInicio })
    });
    if (!res.ok) {
      let msg = 'Error al crear la orden';
      try { const j = await res.json(); if (j?.error) msg = j.error; } catch {}
      throw new Error(msg);
    }
    return res.json();
  },
  productos: async () => {
    const res = await fetch('/api/catalogo/productos?_=' + Date.now(), { cache: 'no-store' });
    if (!res.ok) throw new Error('Error al cargar productos');
    return res.json();
  },
  formulasPorProducto: async (productoId) => {
    const res = await fetch(`/api/catalogo/formulas?productoId=${encodeURIComponent(productoId)}&_=${Date.now()}`, { cache: 'no-store' });
    if (!res.ok) throw new Error('Error al cargar fórmulas');
    return res.json();
  },
  deshacer: async ({ codigoVale, puesto }) => {
    const form = new URLSearchParams({ codigoVale, puesto });
    const res = await fetch('/api/produccion/scan/undo', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: form
    });
    if (!res.ok) throw new Error('Error al deshacer');
    return res.text();
  }
};

export default API;
