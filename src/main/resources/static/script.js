// ── Config ──────────────────────────────────────────────────────────────────
const API_URL = '/api/solve';

// ── AOS ─────────────────────────────────────────────────────────────────────
AOS.init({ duration: 700, easing: 'ease-out-cubic', offset: 80, once: true });

// ── Navbar scroll ────────────────────────────────────────────────────────────
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  navbar.classList.toggle('scrolled', window.scrollY > 50);
});

// ── Hamburger menu ───────────────────────────────────────────────────────────
const hamburger = document.getElementById('hamburger');
const navLinks  = document.getElementById('navLinks');
hamburger.addEventListener('click', () => {
  navLinks.classList.toggle('open');
  const icon = hamburger.querySelector('i');
  icon.classList.toggle('fa-bars');
  icon.classList.toggle('fa-times');
});
document.addEventListener('click', e => {
  if (!navbar.contains(e.target) && navLinks.classList.contains('open')) {
    navLinks.classList.remove('open');
    const icon = hamburger.querySelector('i');
    icon.classList.add('fa-bars');
    icon.classList.remove('fa-times');
  }
});
navLinks.querySelectorAll('a').forEach(a => {
  a.addEventListener('click', () => navLinks.classList.remove('open'));
});

// ── Preset buttons ───────────────────────────────────────────────────────────
const nInput    = document.getElementById('nInput');
const presetRow = document.getElementById('presetRow');

const PRESETS_OFF = [4, 8, 16, 20];
const PRESETS_ON  = [4, 8, 16, 20, 24, 32];

function renderPresets() {
  const presets = randomToggle.checked ? PRESETS_ON : PRESETS_OFF;
  presetRow.innerHTML = presets.map(n =>
    `<button class="preset-btn" data-n="${n}">${n}</button>`
  ).join('');
  presetRow.querySelectorAll('.preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      nInput.value = btn.dataset.n;
      presetRow.querySelectorAll('.preset-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });
}

// ── DOM refs ─────────────────────────────────────────────────────────────────
const solveBtn       = document.getElementById('solveBtn');
const errorBox       = document.getElementById('errorBox');
const demoGrid       = document.getElementById('demoGrid');
const boardCard      = document.getElementById('boardCard');
const boardTabs      = document.getElementById('boardTabs');
const boardContainer = document.getElementById('boardContainer');

let lastResults = [];

// Start with config card centered
demoGrid.classList.add('solo');

const randomToggle = document.getElementById('randomToggle');

function getMaxN() { return randomToggle.checked ? 32 : 20; }

function updateMaxN() {
  const max = getMaxN();
  nInput.max = max;
  nInput.placeholder = `1 – ${max}`;
  if (parseInt(nInput.value, 10) > max) nInput.value = max;
  renderPresets();
}

randomToggle.addEventListener('change', updateMaxN);
updateMaxN();

solveBtn.addEventListener('click', solve);

// ── Solver ───────────────────────────────────────────────────────────────────
async function solve() {
  const n         = parseInt(nInput.value, 10);
  const useRandom = randomToggle.checked;
  const maxN      = getMaxN();

  errorBox.classList.add('hidden');
  errorBox.textContent = '';

  if (!n || n < 1 || n > maxN) {
    showError(`N must be between 1 and ${maxN}${!useRandom ? ' when Random Start is off' : ''}.`);
    return;
  }

  solveBtn.disabled = true;
  solveBtn.innerHTML = '<span class="spinner"></span> Solving…';

  try {
    const res  = await fetch(API_URL, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ n, useRandomStart: useRandom }),
    });

    const data = await res.json();

    if (!res.ok) {
      showError(data.error || `Server error (${res.status})`);
      return;
    }

    lastResults = data.results;
    demoGrid.classList.remove('solo');
    renderMetrics(data.results);
    renderBoardTabs(data.results, 0);
    boardCard.classList.remove('hidden');

  } catch (err) {
    showError('Could not reach the server. Make sure the backend is running on port 8080.');
  } finally {
    solveBtn.disabled = false;
    solveBtn.innerHTML = '<i class="fa fa-play"></i> Solve';
  }
}

// ── Render metric cards into the demo grid (cols 2–4) ────────────────────────
function renderMetrics(results) {
  // Remove any previously rendered metric cards
  demoGrid.querySelectorAll('.metric-card').forEach(el => el.remove());

  results.forEach(r => {
    const card = document.createElement('div');
    card.className = 'metric-card';
    card.innerHTML = `
      <div class="metric-algo-name">${formatAlgo(r.algorithm)}</div>
      <span class="badge ${r.solved ? 'badge-solved' : 'badge-unsolved'}">
        ${r.solved ? 'Solved' : 'No solution'}
      </span>
      <div class="metric-stats">
        <div class="stat-item">
          <label>Checks</label>
          <span>${r.constraintChecks.toLocaleString()}</span>
        </div>
      </div>
      <div class="metric-stats">
        <div class="stat-item">
          <label>Time</label>
          <span>${formatTime(r.timeMs)}</span>
        </div>
      </div>
    `;
    demoGrid.appendChild(card);
  });
}

// ── Render board tabs ────────────────────────────────────────────────────────
function renderBoardTabs(results, activeIdx) {
  boardTabs.innerHTML = results.map((r, i) => `
    <button class="tab-btn ${i === activeIdx ? 'active' : ''}" data-idx="${i}">
      ${formatAlgo(r.algorithm)}
    </button>
  `).join('');

  boardTabs.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      boardTabs.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderBoard(lastResults[parseInt(btn.dataset.idx, 10)]);
    });
  });

  renderBoard(results[activeIdx]);
}

// ── Render chess board ───────────────────────────────────────────────────────
function renderBoard(result) {
  boardContainer.innerHTML = '';

  if (!result.solved || !result.queenColumns) {
    boardContainer.innerHTML = `
      <div class="board-unsolved">
        <i class="fa fa-ban" style="font-size:2rem;margin-bottom:12px;display:block;color:var(--red)"></i>
        No solution found for this configuration.
      </div>`;
    return;
  }

  const n      = result.queenColumns.length;
  const queens = result.queenColumns;

  const cardPadding = 48; // 24px each side from .demo-card padding
  const containerW  = (boardContainer.getBoundingClientRect().width || boardContainer.clientWidth || 520) - cardPadding;
  const maxBoardPx  = Math.min(520, containerW);
  const cellPx      = Math.floor(maxBoardPx / n);

  const board = document.createElement('div');
  board.className = 'board';
  board.style.gridTemplateColumns = `repeat(${n}, ${cellPx}px)`;
  board.style.gridTemplateRows    = `repeat(${n}, ${cellPx}px)`;

  for (let row = 0; row < n; row++) {
    for (let col = 0; col < n; col++) {
      const cell    = document.createElement('div');
      const isLight = (row + col) % 2 === 0;
      const isQueen = queens[row] === col;
      cell.className = `cell ${isLight ? 'light' : 'dark'} ${isQueen ? 'queen' : ''}`;
      cell.style.width  = `${cellPx}px`;
      cell.style.height = `${cellPx}px`;
      if (n > 24) cell.style.fontSize = `${Math.max(8, cellPx * 0.65)}px`;
      board.appendChild(cell);
    }
  }

  boardContainer.appendChild(board);
}

// ── Helpers ──────────────────────────────────────────────────────────────────
function formatAlgo(key) {
  return { BACKTRACKING: 'Backtracking', FORWARD_CHECKING: 'Forward Checking', MAC: 'MAC' }[key] || key;
}

function formatTime(ms) {
  if (ms >= 1000) return (ms / 1000).toFixed(2) + ' s';
  return ms + ' ms';
}

function showError(msg) {
  errorBox.textContent = msg;
  errorBox.classList.remove('hidden');
}
