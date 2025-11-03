const API = {
  ordenesActivas: async () => {
    const res = await fetch('/api/ordenes/activas');
    if (!res.ok) throw new Error('Error al cargar órdenes activas');
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
    const form = new URLSearchParams({ opId });
    const res = await fetch('/api/produccion/vales/iniciar', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: form
    });
    if (!res.ok) throw new Error('Error al iniciar vale');
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
