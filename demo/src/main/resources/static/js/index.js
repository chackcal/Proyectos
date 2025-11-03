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
  tr.innerHTML = `
    <td>${op.codigoOrden}</td>
    <td>${op.productoFinal?.nombre ?? '-'}</td>
    <td>${op.cantidad}</td>
    <td>${op.producidas}</td>
    <td>
      <div class="progress"><div class="bar" style="width:${avance}%"></div></div>
      <div class="small">${avance}%</div>
    </td>
    <td><a class="btn" href="/op.html?op=${op.id}">Ver vales</a></td>
  `;
  return tr;
}

async function load() {
  try {
    const ops = await API.ordenesActivas();
    renderKpis(ops);
    tbody.innerHTML = '';
    ops.forEach(op => tbody.appendChild(row(op)));
  } catch (e) {
    console.error(e);
    alert('No se pudieron cargar las órdenes.');
  }
}

if (refreshBtn) refreshBtn.addEventListener('click', load);
