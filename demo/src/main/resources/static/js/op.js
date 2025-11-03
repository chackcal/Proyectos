import API from './api.js';

document.addEventListener('DOMContentLoaded', () => {
  const qs = new URLSearchParams(location.search);
  const opId = qs.get('op');

  if (!opId) {
    alert('Falta el parámetro ?op=<id>');
    location.href = '/index.html';
    return;
  }

  const title = document.getElementById('op-title');
  const meta = document.getElementById('op-meta');
  const tbody = document.querySelector('#tabla-vales tbody');
  const btnInit = document.getElementById('btn-iniciar');
  const btnRefresh = document.getElementById('btn-refresh');
  const valecode = document.getElementById('valecode');
  const puesto = document.getElementById('puesto');
  const compcode = document.getElementById('compcode');
  const btnScan = document.getElementById('btn-scan');
  const btnUndo = document.getElementById('btn-undo');
  const btnChkRefresh = document.getElementById('btn-chk-refresh');

  async function fetchOP() {
    try { return await API.ordenById(opId); }
    catch (e) { console.error(e); alert('No se pudo cargar la OP'); return null; }
  }

  function row(vale) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${vale.codigoVale}</td>
      <td>${vale.estado}</td>
      <td>${vale.puestoActual ?? '-'}</td>
      <td><span class="badge">${vale.estado === 'EN_PROCESO' ? 'En línea' : 'Finalizado'}</span></td>
    `;
    return tr;
  }

  async function loadChecklist(codigoVale, puestoNum) {
    const body = document.getElementById('checklist-body');
    body.innerHTML = '<tr><td colspan="4" class="small">Cargando...</td></tr>';
    try {
      const lista = await API.requisitos({ codigoVale, puesto: puestoNum });
      body.innerHTML = '';
      if (!lista.length) {
        body.innerHTML = '<tr><td colspan="4" class="small">Sin requisitos para este puesto</td></tr>';
        return;
      }
      lista.forEach(it => {
        const pct = it.requerido === 0 ? 100 : Math.round((it.escaneado / it.requerido) * 100);
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td>${it.codigo}</td>
          <td>${it.nombre}</td>
          <td style="min-width:160px">
            <div class="progress"><div class="bar" style="width:${pct}%"></div></div>
            <div class="small">${it.escaneado}/${it.requerido} (${pct}%)</div>
          </td>
          <td><span class="badge ${it.completo ? 'ok' : 'warn'}">${it.completo ? 'Completo' : 'Falta'}</span></td>
        `;
        body.appendChild(tr);
      });
    } catch (e) {
      console.error(e);
      body.innerHTML = '<tr><td colspan="4" class="small">No se pudo cargar el checklist</td></tr>';
    }
  }

  async function load() {
    const chkPuesto = document.getElementById('chk-puesto'); // <-- aquí, antes de usarla
    const op = await fetchOP();
    if (!op) return;

    title.textContent = `OP ${op.codigoOrden}`;
    meta.textContent  = `${op.productoFinal?.nombre ?? '-'} · ${op.producidas}/${op.cantidad} unidades`;

    const vales = await API.valesPorOP(op.id);
    tbody.innerHTML = '';
    vales.forEach(v => tbody.appendChild(row(v)));

    const enProceso = vales.find(v => v.estado === 'EN_PROCESO');
    if (enProceso && chkPuesto && typeof enProceso.puestoActual === 'number') {
      chkPuesto.value = enProceso.puestoActual;
      if (!valecode.value) valecode.value = enProceso.codigoVale;
      await loadChecklist(valecode.value, Number(chkPuesto.value));
    }
  }

  btnScan?.addEventListener('click', async () => {
    if (!valecode.value || !puesto.value || !compcode.value) return alert('Completá todos los campos');
    await API.escanear({ codigoVale: valecode.value, puesto: Number(puesto.value), codigoComponente: compcode.value });
    await load();
    const chkPuesto = document.getElementById('chk-puesto');
    if (valecode.value && chkPuesto?.value) await loadChecklist(valecode.value, Number(chkPuesto.value));
  });

  btnUndo?.addEventListener('click', async () => {
    if (!valecode.value || !puesto.value) return alert('Indicá vale y puesto');
    await API.deshacer({ codigoVale: valecode.value, puesto: Number(puesto.value) });
    await load();
    const chkPuesto = document.getElementById('chk-puesto');
    if (valecode.value && chkPuesto?.value) await loadChecklist(valecode.value, Number(chkPuesto.value));
  });

  btnChkRefresh?.addEventListener('click', async () => {
    const chkPuesto = document.getElementById('chk-puesto');
    if (!valecode.value) return alert('Indicá un código de vale');
    if (!chkPuesto?.value) return alert('Indicá el número de puesto');
    await loadChecklist(valecode.value, Number(chkPuesto.value));
  });

  btnRefresh?.addEventListener('click', load);

  load();
  setInterval(load, 4000);
});
