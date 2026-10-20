/*
 * WedPlan - shared UI behaviour for every page (prototype and, later, the JSPs).
 * Everything is driven by data-* attributes, so pages need no inline script.
 *
 *   data-toggle="#id, #id2"        toggle a class on targets (default "hidden",
 *                                  or the target's own data-toggle-class)
 *   data-dropdown / -toggle        small menus that close on outside click
 *   data-modal-open="id"           open <div class="modal" id="id">; any data-* on the
 *                                  button fills [data-fill="key"] inside the modal
 *   data-modal-close               close the open modal (also Esc / backdrop click)
 *   data-tab / data-tab-panel      simple tabs (scope: closest [data-tab-scope])
 *   data-chip-filter / data-filter-target   client-side filtering of [data-item]s
 *   data-booking-calc              package + guests x per-head, Plus discount, total, deposit
 *   data-payment-calc              payment breakdown (Plus + bank transfer stack multiplicatively)
 *   data-bind / data-bound         live preview of form values
 *   data-demo="message"            prototype only: fake submit -> toast (+ data-next redirect)
 *
 * Front-end only. Real validation, prices and permissions live in the Java backend.
 */
(function () {
  const $ = (sel, el = document) => el.querySelector(sel);
  const $$ = (sel, el = document) => Array.from(el.querySelectorAll(sel));
  const LKR = (n) => 'LKR ' + Math.round(n).toLocaleString('en-US');
  const pct = (r) => (Math.round(r * 1000) / 10).toString().replace(/\.0$/, '') + '%';
  const DAY = 86400000;

  const STAR = '<svg viewBox="0 0 24 24" aria-hidden="true"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>';

  /* ---------------- toast ---------------- */
  function toast(message) {
    let stack = $('.toast-stack');
    if (!stack) {
      stack = document.createElement('div');
      stack.className = 'toast-stack';
      document.body.appendChild(stack);
    }
    const t = document.createElement('div');
    t.className = 'toast';
    t.innerHTML = '<i data-lucide="circle-check"></i><span></span>';
    t.querySelector('span').textContent = message;
    stack.appendChild(t);
    icons();
    setTimeout(() => { t.style.opacity = '0'; t.style.transition = 'opacity .4s'; }, 2600);
    setTimeout(() => t.remove(), 3100);
  }
  window.toast = toast;

  function icons() { if (window.lucide) window.lucide.createIcons(); }

  /* ---------------- stars ---------------- */
  function renderStars() {
    $$('[data-stars]').forEach((el) => {
      const n = Math.round(parseFloat(el.dataset.stars) || 0);
      el.classList.add('stars');
      el.setAttribute('aria-label', n + ' out of 5 stars');
      el.innerHTML = Array.from({ length: 5 }, (_, i) => `<span class="${i < n ? '' : 'off'}">${STAR}</span>`).join('');
    });
    $$('i[data-star]').forEach((i) => { i.outerHTML = STAR; });
  }

  /* ---------------- clicks: toggles, dropdowns, modals ---------------- */
  let lastModalTrigger = null;

  function openModal(id, trigger) {
    const modal = document.getElementById(id);
    if (!modal) return;
    if (trigger) {
      $$('[data-fill]', modal).forEach((el) => {
        const v = trigger.dataset[el.dataset.fill];
        if (v !== undefined) {
          if ('value' in el && el.tagName !== 'BUTTON') el.value = v; else el.textContent = v;
        }
      });
    }
    lastModalTrigger = trigger || null;
    modal.classList.add('open');
    const focusable = $('input, select, textarea, button:not([data-modal-close])', modal);
    if (focusable) setTimeout(() => focusable.focus(), 50);
  }
  function closeModals() {
    $$('.modal.open').forEach((m) => m.classList.remove('open'));
    if (lastModalTrigger) lastModalTrigger.focus();
  }
  window.openModal = openModal;

  document.addEventListener('click', (e) => {
    const toggle = e.target.closest('[data-toggle]');
    if (toggle) {
      toggle.dataset.toggle.split(',').forEach((sel) => {
        const el = $(sel.trim());
        if (el) el.classList.toggle(el.dataset.toggleClass || 'hidden');
      });
    }

    const ddToggle = e.target.closest('[data-dropdown-toggle]');
    $$('[data-dropdown].open').forEach((d) => {
      if (!ddToggle || d !== ddToggle.closest('[data-dropdown]')) d.classList.remove('open');
    });
    if (ddToggle) ddToggle.closest('[data-dropdown]').classList.toggle('open');

    const mOpen = e.target.closest('[data-modal-open]');
    if (mOpen) { e.preventDefault(); openModal(mOpen.dataset.modalOpen, mOpen); }
    if (e.target.closest('[data-modal-close]') || e.target.classList.contains('modal')) closeModals();

    if (e.target.closest('[data-print]')) window.print();
  });
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeModals();
      $$('[data-dropdown].open').forEach((d) => d.classList.remove('open'));
    }
  });

  /* ---------------- tabs ---------------- */
  function initTabs() {
    $$('[data-tab]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const scope = btn.closest('[data-tab-scope]') || document;
        $$('[data-tab]', scope).forEach((b) => b.classList.toggle('active', b === btn));
        $$('[data-tab-panel]', scope).forEach((p) => p.classList.toggle('hidden', p.dataset.tabPanel !== btn.dataset.tab));
      });
    });
  }

  /* ---------------- filtering (chips, search boxes, selects) ---------------- */
  const filterState = new Map(); // target selector -> { key: value }

  function applyFilter(targetSel) {
    const target = $(targetSel);
    if (!target) return;
    const state = filterState.get(targetSel) || {};
    let visible = 0;
    $$('[data-item]', target).forEach((item) => {
      const ok = Object.entries(state).every(([key, value]) => {
        if (!value || value === 'all') return true;
        if (key === '__text') return item.textContent.toLowerCase().includes(value.toLowerCase());
        return (item.dataset[key] || '').split(' ').includes(value);
      });
      item.classList.toggle('hidden', !ok);
      if (ok) visible++;
    });
    $$(`[data-count-for="${targetSel}"]`).forEach((el) => { el.textContent = visible; });
    $$(`[data-empty-for="${targetSel}"]`).forEach((el) => el.classList.toggle('hidden', visible > 0));
  }
  function setFilter(targetSel, key, value) {
    const s = filterState.get(targetSel) || {};
    s[key] = value;
    filterState.set(targetSel, s);
    applyFilter(targetSel);
  }
  function initFilters() {
    $$('[data-chip-filter]').forEach((group) => {
      const target = group.dataset.chipFilter;
      const key = group.dataset.chipAttr || 'type';
      $$('[data-chip]', group).forEach((chip) => {
        chip.addEventListener('click', () => {
          $$('[data-chip]', group).forEach((c) => c.classList.toggle('active', c === chip));
          setFilter(target, key, chip.dataset.chip);
        });
      });
      // pre-select a chip from the URL, e.g. list.html?category=venue
      const fromQuery = new URLSearchParams(location.search).get(key);
      const match = fromQuery && $(`[data-chip="${fromQuery.toLowerCase()}"]`, group);
      if (match) match.click();
    });
    $$('[data-filter-target]').forEach((ctrl) => {
      const target = ctrl.dataset.filterTarget;
      const key = ctrl.dataset.filterAttr || '__text';
      const run = () => setFilter(target, key, ctrl.value.trim());
      ctrl.addEventListener(ctrl.tagName === 'SELECT' ? 'change' : 'input', run);
    });
  }

  /* ---------------- dates ---------------- */
  const iso = (d) => new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  const parse = (s) => (s ? new Date(s + 'T00:00:00') : null);
  const nice = (d) => d.toLocaleDateString('en-GB', { weekday: 'short', day: 'numeric', month: 'short' });

  function fillFromQuery() {
    const params = new URLSearchParams(location.search);
    $$('form[data-fill-query]').forEach((form) => {
      params.forEach((v, k) => {
        const field = form.elements[k];
        if (field && v) field.value = v;
      });
    });
  }

  /*
   * booking: package price + guests x per-head rate, Plus discount, total, deposit.
   *   form data-rate="base price" data-per-guest="0" data-discount="0.10" data-deposit="0.30"
   *   fields: eventDate, guests, and optionally a "package" radio/select whose option carries
   *   data-price / data-per-guest / data-label to override the form's values.
   */
  function initBookingCalc() {
    $$('[data-booking-calc]').forEach((form) => {
      const dateEl = form.querySelector('[name="eventDate"]');
      if (!dateEl) return;
      const guestsEl = form.querySelector('[name="guests"]');
      const discount = parseFloat(form.dataset.discount || '0');
      const depositRate = parseFloat(form.dataset.deposit || '0.30');
      const today = new Date(); today.setHours(0, 0, 0, 0);

      dateEl.min = iso(new Date(today.getTime() + DAY));
      if (!dateEl.value && form.hasAttribute('data-default-dates')) {
        dateEl.value = iso(new Date(today.getTime() + 180 * DAY));
      }

      const out = (key, val) => $$(`[data-out="${key}"]`, form.closest('[data-calc-scope]') || document).forEach((el) => { el.textContent = val; });

      const selectedPackage = () => {
        const radio = form.querySelector('[name="package"]:checked');
        if (radio) return radio;
        const sel = form.querySelector('select[name="package"]');
        return sel ? sel.selectedOptions[0] : null;
      };

      const update = () => {
        const d = parse(dateEl.value);
        if (d) {
          const days = Math.round((d - today) / DAY);
          out('date', nice(d) + ' ' + d.getFullYear());
          out('countdown', days > 0 ? days + ' days to go' : '—');
        } else {
          out('date', '—');
          out('countdown', '—');
        }

        const pkg = selectedPackage();
        const rate = parseFloat((pkg && pkg.dataset.price) || form.dataset.rate || '0');
        const perGuest = parseFloat((pkg && pkg.dataset.perGuest) || form.dataset.perGuest || '0');
        const guests = parseInt((guestsEl && guestsEl.value) || '0', 10) || 0;
        if (pkg && pkg.dataset.label) out('package', pkg.dataset.label);
        if (!rate && !perGuest) return;

        const sub = rate + perGuest * guests;
        const total = sub * (1 - discount);
        out('base', LKR(rate));
        out('guests', guests ? guests + ' guests' : '—');
        out('per-guest', perGuest ? LKR(perGuest) + ' × ' + guests : '—');
        out('per-guest-total', perGuest ? LKR(perGuest * guests) : '—');
        out('subtotal', LKR(sub));
        out('discount', discount ? '− ' + LKR(sub * discount) : '—');
        out('total', LKR(total));
        out('deposit', LKR(total * depositRate));
        out('balance', LKR(total * (1 - depositRate)));
      };
      form.addEventListener('change', update);
      form.addEventListener('input', update);
      update();
    });
  }

  /* payment: finalRate = 1 - (1 - plusRate) * (1 - methodRate); bank transfers save 5% */
  function initPaymentCalc() {
    $$('[data-payment-calc]').forEach((form) => {
      const booking = form.querySelector('[name="bookingId"]');
      const out = (key, val) => $$(`[data-out="${key}"]`, form).forEach((el) => { el.textContent = val; });
      const BANK_RATE = parseFloat(form.dataset.bankRate || '0.05');

      const update = () => {
        const opt = booking.selectedOptions[0];
        const method = (form.querySelector('[name="method"]:checked') || {}).value || 'card';
        $$('[data-show-when]', form).forEach((el) => el.classList.toggle('hidden', el.dataset.showWhen !== method));
        if (!opt || !opt.dataset.amount) {
          $$('[data-booking-summary]', form).forEach((el) => el.classList.add('hidden'));
          return;
        }
        $$('[data-booking-summary]', form).forEach((el) => el.classList.remove('hidden'));
        const amount = parseFloat(opt.dataset.amount);
        const g = parseFloat(opt.dataset.plus || '0');
        const m = method === 'bank' ? BANK_RATE : 0;
        const finalRate = 1 - (1 - g) * (1 - m);

        out('couple', opt.dataset.couple);
        out('vendor', opt.dataset.vendor);
        out('event', opt.dataset.event);
        out('subtotal', LKR(amount));
        out('plus', g ? '− ' + LKR(amount * g) : '—');
        out('plus-label', g ? 'Plus discount (' + pct(g) + ')' : 'Plus discount');
        out('bank', m ? '− ' + LKR(amount * (1 - g) * m) : '—');
        out('combined', pct(finalRate));
        out('total', LKR(amount * (1 - finalRate)));
        out('formula', `1 − (1 − ${g}) × (1 − ${m}) = ${(Math.round(finalRate * 1000) / 1000)}`);
        $$('[data-plus-badge]', form).forEach((el) => el.classList.toggle('hidden', !g));
      };
      form.addEventListener('change', update);
      update();
    });
  }

  /* ---------------- live preview ---------------- */
  function initBindings() {
    const push = (input) => {
      const key = input.dataset.bind;
      const raw = input.type === 'radio' ? (input.checked ? (input.dataset.label || input.value) : null) : input.value;
      if (raw === null) return;
      $$(`[data-bound="${key}"]`).forEach((el) => {
        el.textContent = el.dataset.format === 'lkr' ? (raw ? LKR(parseFloat(raw) || 0) : '—') : (raw || el.dataset.empty || '—');
      });
      if (input.dataset.img) $$(`[data-bound-img="${key}"]`).forEach((img) => { img.src = input.dataset.img; });
    };
    $$('[data-bind]').forEach((input) => {
      input.addEventListener('input', () => push(input));
      input.addEventListener('change', () => {
        // radios may preset other fields: data-set="capacity=150;rate=450000"
        if (input.dataset.set && input.checked) {
          input.dataset.set.split(';').forEach((pair) => {
            const [name, value] = pair.split('=');
            const f = input.form && input.form.elements[name];
            if (f) { f.value = value; f.dispatchEvent(new Event('input')); }
          });
        }
        push(input);
      });
      push(input);
    });
  }

  /* ---------------- forms ---------------- */
  function initForms() {
    // show / hide password
    $$('[data-password-toggle]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const input = btn.parentElement.querySelector('input');
        const show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        btn.querySelectorAll('[data-eye]').forEach((i) => i.classList.toggle('hidden', (i.dataset.eye === 'off') !== show));
        btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
      });
    });

    // password strength
    $$('[data-strength]').forEach((input) => {
      const bar = $(input.dataset.strength + ' [data-bar]');
      const label = $(input.dataset.strength + ' [data-label]');
      input.addEventListener('input', () => {
        const v = input.value;
        let s = 0;
        if (v.length >= 8) s++;
        if (/[A-Z]/.test(v) && /[a-z]/.test(v)) s++;
        if (/\d/.test(v)) s++;
        if (/[^A-Za-z0-9]/.test(v)) s++;
        const names = ['Too short', 'Weak', 'Fair', 'Good', 'Strong'];
        const colors = ['rgb(var(--c-clay))', 'rgb(var(--c-clay))', 'rgb(var(--c-amber))', 'rgb(var(--c-sage))', 'rgb(var(--c-forest))'];
        bar.style.width = (v ? Math.max(s, 1) * 25 : 0) + '%';
        bar.style.background = colors[s];
        label.textContent = v ? names[s] : '';
      });
    });

    // confirm password
    $$('[data-match]').forEach((input) => {
      const other = $(input.dataset.match);
      const msg = input.parentElement.parentElement.querySelector('[data-match-msg]');
      const check = () => {
        const bad = input.value && input.value !== other.value;
        input.classList.toggle('is-invalid', !!bad);
        if (msg) msg.classList.toggle('hidden', !bad);
      };
      input.addEventListener('input', check);
      other.addEventListener('input', check);
    });

    // character counter
    $$('[data-counter]').forEach((ta) => {
      const out = $(ta.dataset.counter);
      const update = () => { out.textContent = ta.value.length + ' / ' + ta.maxLength; };
      ta.addEventListener('input', update);
      update();
    });

    // star rating text
    $$('.star-input').forEach((group) => {
      const text = $('[data-rating-text]', group.parentElement);
      const words = ['', 'Poor', 'Fair', 'Good', 'Very good', 'Exceptional'];
      group.addEventListener('change', (e) => { if (text) text.textContent = words[e.target.value] || ''; });
    });

    // prototype-only fake submit
    $$('form[data-demo]').forEach((form) => {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        if (!form.checkValidity()) { form.reportValidity(); return; }
        if (form.closest('.modal')) closeModals();
        toast(form.dataset.demo);
        if (form.dataset.next) setTimeout(() => { location.href = form.dataset.next; }, 900);
      });
    });
    $$('[data-demo-click]').forEach((btn) => {
      btn.addEventListener('click', () => { closeModals(); toast(btn.dataset.demoClick); });
    });
  }

  /* ---------------- scroll reveal ---------------- */
  function initReveal() {
    const els = $$('.reveal');
    if (!('IntersectionObserver' in window)) { els.forEach((el) => el.classList.add('in')); return; }
    const io = new IntersectionObserver((entries) => {
      entries.forEach((en) => { if (en.isIntersecting) { en.target.classList.add('in'); io.unobserve(en.target); } });
    }, { threshold: 0.12 });
    els.forEach((el) => io.observe(el));
  }

  /* ---------------- boot ---------------- */
  function boot() {
    $$('[data-year]').forEach((el) => { el.textContent = new Date().getFullYear(); });
    renderStars();
    fillFromQuery();
    initTabs();
    initFilters();
    initBookingCalc();
    initPaymentCalc();
    initBindings();
    initForms();
    initReveal();
    icons();
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot); else boot();
})();
