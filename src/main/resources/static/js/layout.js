/*
 * Shared page chrome for the WedPlan HTML prototype: header, footer, staff sidebar.
 *
 * Each page sets attributes on <body>:
 *   data-root   path from the page back to /prototype/  ("" or "../")
 *   data-layout "public" (top header + footer) or "staff" (sidebar + top bar)
 *   data-active which nav link is highlighted (e.g. "vendors", "bookings")
 *   data-user   who is "logged in" for the mock: none | couple | staff | admin
 *
 * and leaves empty slots: <div data-slot="header|footer|sidebar|topbar"></div>
 *
 * WHEN CONVERTING TO JSP: copy the markup below into
 * WEB-INF/views/common/header.jsp, footer.jsp and sidebar.jsp, replace the
 * hard-coded user with the session user, and <%@ include %> them instead.
 */
(function () {
  const body = document.body;
  const root = body.dataset.root || '';
  const active = body.dataset.active || '';

  // Session detection from backend authentication
  let sessionUser = null;
  try {
    const raw = localStorage.getItem('wedplan_user');
    if (raw) sessionUser = JSON.parse(raw);
  } catch (e) {}

  let user = body.dataset.user || 'none';
  if (sessionUser) {
    if (sessionUser.role === 'ADMIN') user = 'admin';
    else if (sessionUser.role === 'STAFF') user = 'staff';
    else user = 'couple';
  }

  const url = (path) => root + path;
  const isActive = (key) => (key === active ? ' active' : '');

  // two interlocking rings with a small diamond
  const LOGO = `
    <svg viewBox="0 0 40 40" class="w-9 h-9 text-brass" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <circle cx="15" cy="24" r="9"/>
      <circle cx="25" cy="24" r="9"/>
      <path d="M25 15l-2.6-3.2 1-1.8h3.2l1 1.8z"/>
      <path d="M8 6.5l1 2 2 1-2 1-1 2-1-2-2-1 2-1z" stroke-width="1"/>
    </svg>`;

  const brand = (dark) => `
    <a href="${url('index.html')}" class="flex items-center gap-3 shrink-0" aria-label="WedPlan home">
      ${LOGO}
      <span class="leading-none">
        <span class="font-display text-[1.45rem] sm:text-[1.65rem] font-semibold block ${dark ? 'text-ivory' : 'text-forest'}">WedPlan<span class="text-brass">.</span></span>
        <span class="text-[9.5px] tracking-[.3em] uppercase ${dark ? 'text-ivory/50' : 'text-muted'} ${dark ? 'block' : 'hidden sm:block'} mt-1">Weddings · Sri Lanka</span>
      </span>
    </a>`;

  /* ------------------------------------------------------------------ */
  /* Public header                                                       */
  /* ------------------------------------------------------------------ */
  function publicHeader() {
    const links = [
      ['home', 'Home', 'index.html'],
      ['vendors', 'Find vendors', 'vendor/list.html'],
      ['stories', 'Couple stories', 'review/list.html'],
      ['how', 'How it works', 'index.html#how'],
    ];
    if (user === 'couple') links.push(['bookings', 'My bookings', 'booking/my-bookings.html']);

    const navLinks = links.map(([key, label, href]) =>
      `<a href="${url(href)}" class="nav-link${isActive(key)}">${label}</a>`).join('');

    const mobileLinks = links.map(([key, label, href]) =>
      `<a href="${url(href)}" class="block py-3 border-b border-line/60 font-medium ${key === active ? 'text-forest' : 'text-ink/75'}">${label}</a>`).join('');

    const coupleName = sessionUser ? sessionUser.name : 'Nethmi & Kavin';
    const coupleInitials = coupleName.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || 'NK';

    const right = user === 'couple' ? `
      <div class="dropdown" data-dropdown>
        <button class="flex items-center gap-2.5 rounded-full pl-1 pr-3 py-1 hover:bg-forest/5 transition" data-dropdown-toggle aria-haspopup="true">
          <span class="avatar avatar-brass">${coupleInitials}</span>
          <span class="hidden sm:block text-left leading-tight">
            <span class="block text-sm font-semibold">${coupleName}</span>
            <span class="block text-[10px] tracking-widest uppercase text-brass font-bold">Plus member</span>
          </span>
          <i data-lucide="chevron-down" class="w-4 h-4 text-muted"></i>
        </button>
        <div class="dropdown-menu">
          <a class="dropdown-item" href="${url('couple/dashboard.html')}"><i data-lucide="layout-dashboard"></i>Our wedding</a>
          <a class="dropdown-item" href="${url('booking/my-bookings.html')}"><i data-lucide="calendar-heart"></i>My bookings</a>
          <a class="dropdown-item" href="${url('payment/my-payments.html')}"><i data-lucide="wallet"></i>My payments</a>
          <a class="dropdown-item" href="${url('couple/profile.html')}"><i data-lucide="user"></i>Profile &amp; password</a>
          <div class="my-1 border-t border-line"></div>
          <a class="dropdown-item" data-logout href="${url('index.html')}"><i data-lucide="log-out"></i>Sign out</a>
        </div>
      </div>` : `
      <a href="${url('couple/login.html')}" class="btn btn-ghost btn-sm hidden sm:inline-flex">Sign in</a>
      <a href="${url('couple/register.html')}" class="btn btn-primary btn-sm hidden sm:inline-flex">Start planning</a>`;

    return `
      <header class="no-print header-glass sticky top-0 z-50">
        <div class="mx-auto max-w-site px-4 sm:px-6 lg:px-8 h-[76px] flex items-center gap-3 sm:gap-6">
          ${brand(false)}
          <nav class="hidden md:flex items-center gap-8 ml-6" aria-label="Main">${navLinks}</nav>
          <div class="ml-auto flex items-center gap-2">
            ${right}
            <button class="md:hidden btn btn-ghost btn-icon" data-toggle="#mobile-nav" aria-label="Open menu">
              <i data-lucide="menu" class="w-5 h-5"></i>
            </button>
          </div>
        </div>
        <div id="mobile-nav" class="md:hidden hidden border-t border-line/70 px-4 pb-4">
          ${mobileLinks}
          ${user === 'couple' ? '' : `<a href="${url('couple/login.html')}" class="block py-3 border-b border-line/60 font-medium text-ink/75">Sign in</a>
          <a href="${url('couple/register.html')}" class="btn btn-primary btn-block mt-4">Start planning</a>`}
        </div>
      </header>`;
  }

  /* ------------------------------------------------------------------ */
  /* Public footer                                                       */
  /* ------------------------------------------------------------------ */
  function publicFooter() {
    return `
      <footer class="no-print panel-dark mt-24">
        <div class="mx-auto max-w-site px-4 sm:px-6 lg:px-8 pt-16 pb-10">
          <div class="grid gap-12 md:grid-cols-12">
            <div class="md:col-span-5">
              ${brand(true)}
              <p class="mt-6 text-ivory/60 text-sm leading-relaxed max-w-sm">
                Find the people who get your vision, keep every detail in one place and enjoy the feeling
                of it all coming together, from the first idea to the last dance.
              </p>
              <div class="mt-6 flex gap-3 text-ivory/70">
                <span class="inline-flex items-center gap-2 text-sm"><i data-lucide="map-pin" class="w-4 h-4 text-brass"></i>48 Flower Road, Colombo 07</span>
              </div>
            </div>
            <div class="md:col-span-2">
              <h4 class="text-xs tracking-[.22em] uppercase text-brass font-bold mb-4">Explore</h4>
              <ul class="space-y-2.5 text-sm text-ivory/70">
                <li><a class="hover:text-ivory" href="${url('vendor/list.html')}">Find vendors</a></li>
                <li><a class="hover:text-ivory" href="${url('review/list.html')}">Couple stories</a></li>
                <li><a class="hover:text-ivory" href="${url('index.html')}#how">How it works</a></li>
              </ul>
            </div>
            <div class="md:col-span-2">
              <h4 class="text-xs tracking-[.22em] uppercase text-brass font-bold mb-4">Couples</h4>
              <ul class="space-y-2.5 text-sm text-ivory/70">
                <li><a class="hover:text-ivory" href="${url('couple/login.html')}">Sign in</a></li>
                <li><a class="hover:text-ivory" href="${url('couple/register.html')}">Create account</a></li>
                <li><a class="hover:text-ivory" href="${url('booking/my-bookings.html')}">My bookings</a></li>
              </ul>
            </div>
            <div class="md:col-span-3">
              <h4 class="text-xs tracking-[.22em] uppercase text-brass font-bold mb-4">Planner desk</h4>
              <ul class="space-y-2.5 text-sm text-ivory/70">
                <li class="flex items-center gap-2"><i data-lucide="phone" class="w-4 h-4 text-brass"></i>+94 11 255 0198</li>
                <li class="flex items-center gap-2"><i data-lucide="mail" class="w-4 h-4 text-brass"></i>hello@wedplan.lk</li>
                <li class="flex items-center gap-2"><i data-lucide="clock" class="w-4 h-4 text-brass"></i>Mon – Sat, 9 am – 6 pm</li>
              </ul>
            </div>
          </div>
          <div class="ornament mt-14 mb-6 opacity-60"><span></span></div>
          <div class="flex flex-col sm:flex-row gap-3 justify-between text-xs text-ivory/45">
            <p>&copy; <span data-year></span> WedPlan · Wedding planning prototype</p>
            <a href="${url('admin/login.html')}" class="inline-flex items-center gap-1.5 hover:text-ivory"><i data-lucide="key-round" class="w-3.5 h-3.5"></i>Staff portal</a>
          </div>
        </div>
      </footer>`;
  }

  /* ------------------------------------------------------------------ */
  /* Staff sidebar + top bar                                             */
  /* ------------------------------------------------------------------ */
  function staffSidebar() {
    const isAdmin = user !== 'staff';
    // [key, label, icon, href, adminOnly]
    const groups = [
      ['Overview', [
        ['dashboard', 'Dashboard', 'layout-dashboard', 'admin/dashboard.html', false],
      ]],
      ['Planner desk', [
        ['bookings', 'Bookings', 'calendar-heart', 'booking/manage.html', false],
        ['payments', 'Payments', 'wallet', 'payment/list.html', false],
        ['couples', 'Couples', 'users', 'couple/manage.html', false],
      ]],
      ['Marketplace', [
        ['vendors', 'Vendors', 'store', 'vendor/manage.html', true],
        ['reviews', 'Reviews', 'message-square-quote', 'review/moderate.html', true],
      ]],
      ['Administration', [
        ['staff', 'Staff accounts', 'user-cog', 'admin/staff.html', true],
      ]],
    ];

    const nav = groups.map(([title, items]) => {
      const visible = items.filter((i) => isAdmin || !i[4]);
      if (!visible.length) return '';
      return `<p class="side-group">${title}</p>` + visible.map(([key, label, icon, href, adminOnly]) =>
        `<a href="${url(href)}" class="side-link${isActive(key)}"><i data-lucide="${icon}"></i>${label}${adminOnly ? '<span class="side-tag">ADMIN</span>' : ''}</a>`
      ).join('');
    }).join('');

    const who = sessionUser
      ? {
          initials: sessionUser.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || 'ST',
          name: sessionUser.name,
          role: sessionUser.role === 'ADMIN' ? 'Administrator' : 'Planner support'
        }
      : (isAdmin
          ? { initials: 'AS', name: 'Amaya Silva', role: 'Administrator' }
          : { initials: 'IF', name: 'Ishara Fernando', role: 'Planner support' });

    return `
      <div id="side-overlay" class="no-print fixed inset-0 z-40 bg-forest-900/50 hidden lg:hidden" data-toggle="#side-overlay, #sidebar"></div>
      <aside id="sidebar" data-toggle-class="-translate-x-full" class="no-print panel-dark fixed inset-y-0 left-0 z-50 w-72 flex flex-col px-5 py-6 -translate-x-full lg:translate-x-0 transition-transform duration-300">
        <div class="px-1">${brand(true)}</div>
        <p class="mt-6 mx-1 text-[10px] tracking-[.28em] uppercase text-brass font-bold">Staff portal</p>
        <nav class="mt-2 flex-1 overflow-y-auto pl-3 -ml-3 pr-1" aria-label="Staff">${nav}</nav>
        <div class="mt-4 rounded-2xl bg-ivory/[.06] p-3 flex items-center gap-3">
          <span class="avatar avatar-brass">${who.initials}</span>
          <div class="min-w-0 leading-tight">
            <p class="text-sm font-semibold text-ivory truncate">${who.name}</p>
            <p class="text-[11px] text-ivory/50">${who.role}</p>
          </div>
          <a href="${url('admin/login.html')}" data-logout class="ml-auto p-2 rounded-lg text-ivory/60 hover:text-ivory hover:bg-ivory/10" title="Sign out" aria-label="Sign out"><i data-lucide="log-out" class="w-4 h-4"></i></a>
        </div>
      </aside>`;
  }

  function staffTopbar() {
    const today = new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    return `
      <div class="no-print header-glass sticky top-0 z-30">
        <div class="h-16 px-4 sm:px-8 flex items-center gap-4">
          <button class="lg:hidden btn btn-ghost btn-icon" data-toggle="#side-overlay, #sidebar" aria-label="Open menu"><i data-lucide="menu" class="w-5 h-5"></i></button>
          <p class="hidden sm:flex items-center gap-2 text-sm text-muted"><i data-lucide="calendar" class="w-4 h-4 text-brass"></i>${today}</p>
          <div class="ml-auto flex items-center gap-2">
            <a href="${url('index.html')}" class="btn btn-ghost btn-sm"><i data-lucide="heart"></i><span class="hidden sm:inline">Couple site</span></a>
            <button class="btn btn-ghost btn-icon relative" aria-label="Notifications">
              <i data-lucide="bell" class="w-5 h-5"></i>
              <span class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-brass"></span>
            </button>
          </div>
        </div>
      </div>`;
  }

  /* ------------------------------------------------------------------ */
  const fill = (slot, html) => {
    const el = document.querySelector(`[data-slot="${slot}"]`);
    if (el) el.outerHTML = html;
  };

  if (body.dataset.layout === 'public') {
    fill('header', publicHeader());
    fill('footer', publicFooter());
  } else if (body.dataset.layout === 'staff') {
    fill('sidebar', staffSidebar());
    fill('topbar', staffTopbar());
  }

  // Automatically inject API Bridge if not already loaded
  if (!document.querySelector('script[data-bridge]')) {
    const bridgeScript = document.createElement('script');
    bridgeScript.setAttribute('data-bridge', 'true');
    bridgeScript.src = (root ? root : '') + '../js/api-bridge.js';
    document.body.appendChild(bridgeScript);
  }
})();
