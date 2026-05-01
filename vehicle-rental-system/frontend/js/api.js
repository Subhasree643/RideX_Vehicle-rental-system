// ===== RideX API CONFIG =====
const API_BASE = 'http://localhost:8080/api';

function showToast(message, type = 'info') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${icons[type] || icons.info}</span><span>${message}</span><button class="toast-close" onclick="this.parentElement.remove()">×</button>`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

async function apiGet(path) {
  const res = await fetch(API_BASE + path);
  if (!res.ok) throw new Error((await res.json()).error || 'Request failed');
  return res.json();
}

async function apiPost(path, data) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.error || 'Request failed');
  return json;
}

async function apiPut(path, data) {
  const res = await fetch(API_BASE + path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.error || 'Request failed');
  return json;
}

async function apiDelete(path) {
  const res = await fetch(API_BASE + path, { method: 'DELETE' });
  if (!res.ok) throw new Error((await res.json()).error || 'Request failed');
  return res.json();
}

function fmtDate(d) {
  return d ? new Date(d).toLocaleDateString('en-IN') : '—';
}

function fmtDateTime(d) {
  if (!d) return new Date().toLocaleString('en-IN');
  return new Date(d).toLocaleString('en-IN');
}

function fmtCurrency(n) {
  return '₹' + Number(n || 0).toLocaleString('en-IN');
}

function fmtNumber(n) {
  return Number(n || 0).toLocaleString('en-IN');
}

function generateTransactionId() {
  return 'TXN' + Date.now() + Math.floor(Math.random() * 10000);
}

function statusBadge(status) {
  const map = {
    ACTIVE: 'badge-green', ACCEPTED: 'badge-purple', ONGOING: 'badge-yellow',
    COMPLETED: 'badge-gray', CANCELLED: 'badge-red', PENDING: 'badge-yellow'
  };
  return `<span class="badge ${map[status] || 'badge-gray'}">${status}</span>`;
}

function vehicleEmoji(type, category) {
  if (type === 'Bike') return '🏍️';
  return category === 'SUV' ? '🚙' : '🚗';
}

function saveSession(role, data) {
  localStorage.setItem('ridex_role', role);
  localStorage.setItem('ridex_user', JSON.stringify(data));
}

function getSession() {
  return JSON.parse(localStorage.getItem('ridex_user') || 'null');
}

function clearSession() {
  localStorage.removeItem('ridex_role');
  localStorage.removeItem('ridex_user');
}

function buildVehicleCard(v, selectedId, onSelect) {
  const card = document.createElement('div');
  card.className = `vehicle-card ${v.id === selectedId ? 'selected' : ''}`;
  card.innerHTML = `
    <div class="v-type">
      <span class="v-emoji">${vehicleEmoji(v.type, v.category)}</span>
    </div>
    <h4>${v.brand} ${v.model}</h4>
    <div class="v-cat">${v.type} · ${v.category}</div>
    <div class="v-price">${fmtCurrency(v.rentPerDay)}<span>/day</span></div>
    <div class="v-reg">${v.regNo}</div>
  `;
  if (v.available && onSelect) card.addEventListener('click', () => onSelect(v, card));
  return card;
}

// ===== INVOICE GENERATOR =====
function downloadInvoice(rental, customer, paymentMode, pointsUsed) {
  try {
    let baseAmount = 0, distanceFare = 0, timeFare = 0, subtotal = 0;

    if (rental.rentalType === 'WITH_DRIVER') {
      baseAmount = 100;
      distanceFare = (rental.distance || 0) * 15;
      timeFare = (rental.hours || 1) * 100;
      subtotal = baseAmount + distanceFare + timeFare;
    } else {
      subtotal = (rental.vehicle?.rentPerDay || 500) * (rental.days || 1);
    }

    const discount = (pointsUsed || 0) * 10;
    const afterDiscount = Math.max(0, subtotal - discount);
    const gst = afterDiscount * 0.05;
    let total = afterDiscount + gst;
    if (rental.damageFee && rental.damageFee > 0) total += rental.damageFee;

    const invoiceId = 'RX-INV-' + String(rental.id || Date.now()).padStart(5, '0');
    const transactionId = generateTransactionId();
    const now = new Date().toLocaleString('en-IN');

    const invoiceHTML = `<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>RideX Invoice ${invoiceId}</title>
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family: Arial, sans-serif; background:#f0f4ff; display:flex; justify-content:center; padding:40px 20px; }
  .invoice { max-width:720px; width:100%; background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 4px 24px rgba(24,73,169,0.12); }
  .inv-header { background:#1849A9; color:#fff; padding:28px 32px; display:flex; justify-content:space-between; align-items:center; }
  .inv-logo { font-size:28px; font-weight:800; letter-spacing:-1px; }
  .inv-logo span { color:#93C5FD; }
  .inv-logo small { display:block; font-size:12px; font-weight:400; opacity:0.8; letter-spacing:2px; margin-top:2px; }
  .inv-meta { text-align:right; font-size:13px; opacity:0.9; line-height:1.8; }
  .inv-meta strong { font-size:18px; display:block; }
  .inv-body { padding:28px 32px; }
  .inv-section { margin-bottom:22px; }
  .inv-section-title { font-size:11px; font-weight:700; letter-spacing:1.5px; text-transform:uppercase; color:#1849A9; border-bottom:2px solid #EFF6FF; padding-bottom:6px; margin-bottom:12px; }
  .inv-grid { display:grid; grid-template-columns:1fr 1fr; gap:8px 24px; }
  .inv-field { font-size:13px; }
  .inv-field label { color:#64748B; display:block; font-size:11px; margin-bottom:2px; }
  .inv-field span { color:#1e293b; font-weight:500; }
  .fare-table { width:100%; border-collapse:collapse; font-size:14px; }
  .fare-table tr td { padding:8px 0; border-bottom:1px solid #F1F5F9; color:#475569; }
  .fare-table tr td:last-child { text-align:right; font-weight:500; color:#1e293b; }
  .fare-table tr.discount td { color:#16a34a; }
  .fare-table tr.damage td { color:#dc2626; }
  .fare-table tr.total td { border-top:2px solid #1849A9; border-bottom:none; padding-top:12px; font-weight:700; font-size:16px; color:#1849A9; }
  .status-badge { display:inline-block; background:#EFF6FF; color:#1849A9; border:1px solid #BFDBFE; padding:4px 14px; border-radius:100px; font-size:12px; font-weight:700; }
  .inv-footer { background:#F8FAFC; border-top:1px solid #E2E8F0; padding:20px 32px; text-align:center; }
  .inv-footer p { font-size:12px; color:#94A3B8; line-height:1.8; }
  .print-btn { margin-top:16px; }
  .print-btn button { background:#1849A9; color:#fff; border:none; padding:10px 28px; border-radius:8px; font-size:14px; font-weight:600; cursor:pointer; }
  .print-btn button:hover { background:#2563EB; }
  .loyalty-box { background:#EFF6FF; border:1px solid #BFDBFE; border-radius:8px; padding:12px 16px; display:flex; justify-content:space-between; align-items:center; }
  .loyalty-box span { font-size:13px; color:#1849A9; }
  .loyalty-box strong { font-size:18px; color:#1849A9; font-weight:700; }
  @media print { body{background:#fff;padding:0;} .print-btn{display:none;} .invoice{box-shadow:none;} }
</style>
</head>
<body>
<div class="invoice">
  <div class="inv-header">
    <div class="inv-logo">Ride<span>X</span><small>VEHICLE RENTAL PLATFORM</small></div>
    <div class="inv-meta">
      <strong>${invoiceId}</strong>
      ${now}<br>
      Txn: ${transactionId}
    </div>
  </div>

  <div class="inv-body">

    <div class="inv-section">
      <div class="inv-section-title">Customer Details</div>
      <div class="inv-grid">
        <div class="inv-field"><label>Customer Name</label><span>${customer?.name || '—'}</span></div>
        <div class="inv-field"><label>Customer ID</label><span>#${customer?.id || '—'}</span></div>
        <div class="inv-field"><label>Phone</label><span>${customer?.phone || '—'}</span></div>
        <div class="inv-field"><label>Address</label><span>${customer?.address || '—'}</span></div>
      </div>
    </div>

    <div class="inv-section">
      <div class="inv-section-title">Trip Details</div>
      <div class="inv-grid">
        <div class="inv-field"><label>Vehicle</label><span>${rental.vehicle?.brand || ''} ${rental.vehicle?.model || ''}</span></div>
        <div class="inv-field"><label>Registration</label><span>${rental.vehicle?.regNo || '—'}</span></div>
        <div class="inv-field"><label>Trip Type</label><span>${rental.rentalType === 'WITH_DRIVER' ? '🧑‍✈️ With Driver' : '🔑 Self Drive'}</span></div>
        <div class="inv-field"><label>Date</label><span>${fmtDate(rental.rentDate)}</span></div>
        ${rental.rentalType === 'WITH_DRIVER' ? `
        <div class="inv-field"><label>Pickup</label><span>${rental.pickupLocation || '—'}</span></div>
        <div class="inv-field"><label>Drop</label><span>${rental.dropLocation || '—'}</span></div>
        <div class="inv-field"><label>Distance</label><span>${rental.distance || 0} km</span></div>
        <div class="inv-field"><label>Duration</label><span>${rental.hours || 0} hour(s)</span></div>
        <div class="inv-field"><label>Driver</label><span>${rental.driver?.name || '—'}</span></div>
        ` : `
        <div class="inv-field"><label>Days</label><span>${rental.days || 1} day(s)</span></div>
        `}
      </div>
    </div>

    <div class="inv-section">
      <div class="inv-section-title">Fare Breakdown</div>
      <table class="fare-table">
        ${rental.rentalType === 'WITH_DRIVER' ? `
        <tr><td>Base Fare</td><td>${fmtCurrency(baseAmount)}</td></tr>
        <tr><td>Distance Fare (₹15/km × ${rental.distance || 0} km)</td><td>${fmtCurrency(distanceFare)}</td></tr>
        <tr><td>Time Fare (₹100/hr × ${rental.hours || 0} hrs)</td><td>${fmtCurrency(timeFare)}</td></tr>
        ` : `
        <tr><td>Rent/Day (${fmtCurrency(rental.vehicle?.rentPerDay || 0)}) × ${rental.days || 1} days</td><td>${fmtCurrency(subtotal)}</td></tr>
        `}
        ${discount > 0 ? `<tr class="discount"><td>Loyalty Discount (${pointsUsed} pts)</td><td>-${fmtCurrency(discount)}</td></tr>` : ''}
        ${rental.damageFee > 0 ? `<tr class="damage"><td>Damage Fee</td><td>+${fmtCurrency(rental.damageFee)}</td></tr>` : ''}
        <tr><td>GST (5%)</td><td>${fmtCurrency(gst)}</td></tr>
        <tr class="total"><td>Total Paid</td><td>${fmtCurrency(total)}</td></tr>
      </table>
    </div>

    <div class="inv-section">
      <div class="inv-section-title">Payment</div>
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div class="inv-field"><label>Payment Mode</label><span>${paymentMode || 'Cash'}</span></div>
        <span class="status-badge">✓ PAID</span>
      </div>
    </div>

    <div class="inv-section">
      <div class="inv-section-title">Loyalty Points</div>
      <div class="loyalty-box">
        <span>Points earned this trip</span>
        <strong>+10 pts</strong>
      </div>
    </div>

  </div>

  <div class="inv-footer">
    <p>Thank you for choosing <strong>RideX</strong> — Your trusted vehicle rental platform</p>
    <p>Support: support@ridex.in &nbsp;|&nbsp; www.ridex.in</p>
    <div class="print-btn"><button onclick="window.print()">🖨️ Print / Save as PDF</button></div>
  </div>
</div>
</body>
</html>`;

    const blob = new Blob([invoiceHTML], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `RideX_${invoiceId}.html`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    showToast(`Invoice downloaded: ${invoiceId}`, 'success');
    return true;
  } catch (error) {
    showToast('Invoice error: ' + error.message, 'error');
    return false;
  }
}

window.downloadInvoice = downloadInvoice;
window.fmtCurrency = fmtCurrency;

// ===== THEME =====
function initTheme() {
  const saved = localStorage.getItem('ridex-theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
  updateThemeIcon();
}

function toggleTheme() {
  const curr = document.documentElement.getAttribute('data-theme');
  const next = curr === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('ridex-theme', next);
  updateThemeIcon();
  showToast(`${next === 'dark' ? '🌙 Dark' : '☀️ Light'} mode`, 'info');
}

function updateThemeIcon() {
  const theme = document.documentElement.getAttribute('data-theme');
  document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
    btn.textContent = theme === 'dark' ? '☀️' : '🌙';
  });
}

window.toggleTheme = toggleTheme;
window.initTheme = initTheme;
document.addEventListener('DOMContentLoaded', initTheme);