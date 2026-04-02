// ── Config ──────────────────────────────────────────────────────────────────
const API_URL = 'http://localhost:8080/api/solve';

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
hamburger.addEventListener('click', () => navLinks.classList.toggle('open'));
document.addEventListener('click', e => {
  if (!navbar.contains(e.target)) navLinks.classList.remove('open');
});

// ── Preset buttons ───────────────────────────────────────────────────────────
const nInput = document.getElementById('nInput');
document.querySelectorAll('.preset-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    nInput.value = btn.dataset.n;
    document.querySelectorAll('.preset-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

// ── Solver ───────────────────────────────────────────────────────────────────
const solveBtn      = document.getElementById('solveBtn');
const errorBox      = document.getElementById('errorBox');
const resultsDiv    = document.getElementById('results');
const metricsGrid   = document.getElementById('metricsGrid');
const boardTabs     = document.getElementById('boardTabs');
const boardContainer= document.getElementById('boardContainer');

let lastResults = [];

solveBtn.addEventListener('click', solve);

async function solve() {
  const n           = parseInt(nInput.value, 10);
  const useRandom   = document.getElementById('randomToggle').checked;

  errorBox.classList.add('hidden');
  errorBox.textContent = '';

  if (!n || n < 1 || n > 64) {
    showError('N must be between 1 and 64.');
    return;
  }

  // Loading state
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
    renderMetrics(data.results);
    renderBoardTabs(data.results, 0);
    resultsDiv.classList.remove('hidden');

  } catch (err) {
    showError('Could not reach the server. Make sure the backend is running on port 8080.');
  } finally {
    solveBtn.disabled = false;
    solveBtn.innerHTML = '<i class="fa fa-play"></i> Solve';
  }
}

// ── Render metrics cards ─────────────────────────────────────────────────────
function renderMetrics(results) {
  metricsGrid.innerHTML = results.map(r => `
    <div class="metric-card">
      <div class="metric-algo">
        ${formatAlgo(r.algorithm)}
        <span class="badge ${r.solved ? 'badge-solved' : 'badge-unsolved'}">
          ${r.solved ? 'Solved' : 'No solution'}
        </span>
      </div>
      <div class="metric-stats">
        <div class="stat-item">
          <label>Time</label>
          <span>${r.timeMs} ms</span>
        </div>
        <div class="stat-item">
          <label>Checks</label>
          <span>${r.constraintChecks.toLocaleString()}</span>
        </div>
      </div>
    </div>
  `).join('');
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

  const n       = result.queenColumns.length;
  const queens  = result.queenColumns;

  // Cap cell size so large boards still fit
  const maxBoardPx = Math.min(480, boardContainer.clientWidth || 480);
  const cellPx     = Math.max(14, Math.floor(maxBoardPx / n));

  const board = document.createElement('div');
  board.className = 'board';
  board.style.gridTemplateColumns = `repeat(${n}, ${cellPx}px)`;
  board.style.gridTemplateRows    = `repeat(${n}, ${cellPx}px)`;

  for (let row = 0; row < n; row++) {
    for (let col = 0; col < n; col++) {
      const cell = document.createElement('div');
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

function showError(msg) {
  errorBox.textContent = msg;
  errorBox.classList.remove('hidden');
}
