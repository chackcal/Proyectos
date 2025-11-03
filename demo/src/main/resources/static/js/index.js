import API from './api.js';

const tbody = document.querySelector('#tabla-op tbody');
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

async function tieneValeActivo(opId) {
  try {
    const vales = await API.valesPorOP(opId);
    return Array.isArray(vales) && vales.some(v => v.estado === 'EN_PROCESO');
  } catch (e) {
    console.error('Error consultando vales por OP', opId, e);
    return false; // si falla, no bloqueamos el botón
  }
}

async function load() {
  try {
    const ops = await API.ordenesActivas();
    renderKpis(ops);
    tbody.innerHTML = '';
    ops.forEach(op => tbody.appendChild(row(op)));

    // Después de renderizar, evaluar cada fila y ajustar su botón
    for (const op of ops) {
      (async () => {
        const hasActive = await tieneValeActivo(op.id);
        const btn = tbody.querySelector(`.btn-iniciar[data-op="${op.id}"]`);
        if (!btn) return;
        if (hasActive) {
          btn.disabled = true;
          btn.title = 'Ya hay un vale en proceso para esta OP';
          // Si preferís ocultar en lugar de deshabilitar, usa:
          // btn.style.display = 'none';
        } else {
          btn.disabled = false;
          btn.title = 'Iniciar un nuevo vale';
          // btn.style.display = '';
        }
      })();
    }
  } catch (e) {
    console.error(e);
    alert('No se pudieron cargar las órdenes.');
  }
}

// Delegación de eventos: manejar clicks en “Iniciar vale”
tbody.addEventListener('click', async (ev) => {
  const btn = ev.target.closest('.btn-iniciar');
  if (!btn) return;

  const opId = Number(btn.dataset.op);
  if (!opId) return;

  // Doble chequeo al momento del clic
  if (await tieneValeActivo(opId)) {
    alert('Ya existe un vale en proceso para esta OP. Finalizalo antes de iniciar otro.');
    return;
  }

  try {
    const vale = await API.iniciarVale(opId);
    alert(`Vale ${vale?.codigoVale ?? ''} iniciado correctamente.`);
    await load(); // refresca la tabla y re-aplica las reglas
  } catch (e) {
    alert('No se puede iniciar un nuevo vale hasta finalizar el actual.');
    console.error(e);
  }
});

if (refreshBtn) refreshBtn.addEventListener('click', load);

// Modo manual: no llamamos load() automáticamente.
// El usuario actualiza con el botón “Actualizar”.
