import API from './api.js';

const tbody = document.querySelector('#tabla-op tbody');
const tbodyFin = document.querySelector('#tabla-op-fin tbody');
const kpis = document.getElementById('kpis');
const refreshBtn = document.getElementById('refresh');

function pct(a, b) { return b === 0 ? 0 : Math.round((a / b) * 100); }

function renderKpis(ops) {
  const total = ops.length;
  const totalCantidad = ops.reduce((s, op) => s + op.cantidad, 0);
  const totalProd = ops.reduce((s, op) => s + op.producidas, 0);
  const prom = pct(totalProd, totalCantidad);

  kpis.innerHTML = `
    <div class="kpi"><div class="label">OP Activas</div><div class="value">${total}</div></div>
    <div class="kpi"><div class="label">Unidades planificadas</div><div class="value">${totalCantidad}</div></div>
    <div class="kpi"><div class="label">Unidades producidas</div><div class="value">${totalProd}</div></div>
    <div class="kpi"><div class="label">Avance global</div><div class="value">${prom}%</div></div>
  `;
}

function row(op) {
  const avance = pct(op.producidas, op.cantidad);
  const tr = document.createElement('tr');
  tr.dataset.opId = op.id;

  tr.innerHTML = `
    <td>${op.codigoOrden}</td>
    <td>${op.productoFinal?.nombre ?? '-'}</td>
    <td>${op.cantidad}</td>
    <td>${op.producidas}</td>
    <td>
      <div class="progress"><div class="bar" style="width:${avance}%"></div></div>
      <div class="small">${avance}%</div>
    </td>
    <td class="row" style="gap:8px">
      <a class="btn" href="/op.html?op=${op.id}">Ver vales</a>
      <button class="btn btn-iniciar" data-op="${op.id}" title="Iniciar un nuevo vale">Iniciar vale</button>
    </td>
  `;
  return tr;
}

function rowFin(op) {
  const tr = document.createElement('tr');
  tr.innerHTML = `
    <td>${op.codigoOrden}</td>
    <td>${op.productoFinal?.nombre ?? '-'}</td>
    <td>${op.cantidad}</td>
    <td>${op.producidas}</td>
    <td><span class="badge">${op.estado}</span></td>
    <td>${op.fechaFin ?? '-'}</td>
  `;
  return tr;
}

async function tieneValeActivo(opId) {
  try {
    const vales = await API.valesPorOP(opId);
    return Array.isArray(vales) && vales.some(v => v.estado === 'EN_PROCESO');
  } catch (e) {
    console.error('Error consultando vales por OP', opId, e);
    return false;
  }
}

async function load() {
  try {
    const [ops, fin] = await Promise.all([
      API.ordenesActivas(),
      API.ordenesFinalizadas()
    ]);

    console.log('[INDEX] /api/ordenes/activas =>', ops);
    console.log('[INDEX] /api/ordenes/finalizadas =>', fin);

    renderKpis(ops);

    tbody.innerHTML = '';
    ops.forEach(op => tbody.appendChild(row(op)));

    for (const op of ops) {
      (async () => {
        const hasActive = await tieneValeActivo(op.id);
        const btn = tbody.querySelector(`.btn-iniciar[data-op="${op.id}"]`);
        if (!btn) return;
        btn.disabled = hasActive;
        btn.title = hasActive ? 'Ya hay un vale en proceso para esta OP' : 'Iniciar un nuevo vale';
      })();
    }

    if (tbodyFin) {
      tbodyFin.innerHTML = '';
      (fin ?? []).forEach(op => tbodyFin.appendChild(rowFin(op)));
    }
  } catch (e) {
    console.error(e);
    alert('No se pudieron cargar las órdenes.');
  }
}

// Iniciar vale
tbody.addEventListener('click', async (ev) => {
  const btn = ev.target.closest('.btn-iniciar');
  if (!btn) return;

  const opId = Number(btn.dataset.op);
  if (!opId) return;

  if (await tieneValeActivo(opId)) {
    alert('Ya existe un vale en proceso para esta OP. Finalizalo antes de iniciar otro.');
    return;
  }

  try {
    const vale = await API.iniciarVale(opId);
    alert(`Vale ${vale?.codigoVale ?? ''} iniciado correctamente.`);
    await load();
  } catch (e) {
    alert('No se puede iniciar un nuevo vale hasta finalizar el actual.');
    console.error(e);
  }
});

document.addEventListener('DOMContentLoaded', load);
document.addEventListener('visibilitychange', () => {
  if (document.visibilityState === 'visible') load();
});
if (refreshBtn) refreshBtn.addEventListener('click', load);

const modal = document.getElementById('modal-op');
const btnNueva = document.getElementById('btn-nueva-op');
const btnCerrar = document.getElementById('btn-cerrar-modal');
const form = document.getElementById('form-op');
const selProducto = document.getElementById('sel-producto');
const selFormula  = document.getElementById('sel-formula');

function option(v, t){ const o=document.createElement('option'); o.value=v; o.textContent=t; return o; }

async function loadProductos() {
  selProducto.innerHTML = '';
  const productos = await API.productos();
  selProducto.appendChild(option('', 'Seleccioná producto'));
  productos.forEach(p => selProducto.appendChild(option(p.id, p.nombre)));
  selFormula.innerHTML = ''; // limpiar fórmulas
  selFormula.appendChild(option('', 'Seleccioná fórmula'));
}

async function loadFormulas(productoId) {
  selFormula.innerHTML = '';
  selFormula.appendChild(option('', 'Seleccioná fórmula'));
  if (!productoId) return;
  const formulas = await API.formulasPorProducto(productoId);
  formulas.forEach(f => selFormula.appendChild(option(f.id, f.nombre)));
}

function openModal(){ modal?.classList.remove('hidden'); }
function closeModal(){ modal?.classList.add('hidden'); form?.reset(); selFormula.innerHTML=''; }

btnNueva?.addEventListener('click', async () => {
  await loadProductos();
  openModal();
});

btnCerrar?.addEventListener('click', closeModal);

selProducto?.addEventListener('change', async (e) => {
  const id = Number(e.target.value || 0);
  await loadFormulas(id);
});

form?.addEventListener('submit', async (ev) => {
  ev.preventDefault();
  const fd = new FormData(form);
  const payload = {
    // codigoOrden: null -> lo genera el backend
    productoFinalId: Number(fd.get('productoFinalId')),
    formulaId: Number(fd.get('formulaId')),
    cantidad: Number(fd.get('cantidad')),
    fechaInicio: (fd.get('fechaInicio') || '').trim() || null,
  };
  if (!payload.productoFinalId || !payload.formulaId || !payload.cantidad) {
    alert('Completá producto, fórmula y cantidad');
    return;
  }
  try {
    const op = await API.crearOrden(payload);
    alert(`Orden creada: ${op.codigoOrden}`);
    closeModal();
    await load(); // refresca activas/finalizadas
  } catch (e) {
    alert(e.message || 'Error creando la orden');
    console.error(e);
  }
});