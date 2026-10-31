/**
 * WedPlan API Bridge
 * Connects the static prototype UI seamlessly to the Spring Boot backend REST APIs.
 * Supports all 6 modules:
 *   1. userandAdminMgt   (Auth, Profiles, Staff, Couples)
 *   2. vendorMgt         (Marketplace list, Manage, Add/Edit)
 *   3. bookingMgt        (Book now, My bookings, Confirmation, Staff manage)
 *   4. packageAndServiceMgt (Package listings & selection)
 *   5. reviewAndAlertMgt (Submit stories, Stories list, Staff moderate)
 *   6. paymentMgt        (Live payments list, Record payment)
 */
(function () {
  'use strict';

  if (window.__wedplan_bridge_initialized) return;
  window.__wedplan_bridge_initialized = true;

  const API_BASE = '/api';
  const STORAGE_KEY = 'wedplan_user';

  /* ------------------------------------------------------------- */
  /* Helpers                                                       */
  /* ------------------------------------------------------------- */
  const $ = (sel, el = document) => el.querySelector(sel);
  const $$ = (sel, el = document) => Array.from(el.querySelectorAll(sel));

  function getStoredUser() {
    try {
      const data = localStorage.getItem(STORAGE_KEY);
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  }

  function setStoredUser(user) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    } catch (e) {}
  }

  function clearStoredUser() {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch (e) {}
  }

  function showToast(message, isError = false) {
    if (typeof window.toast === 'function') {
      window.toast(message);
    } else {
      alert(message);
    }
  }

  function formatLKR(amount) {
    if (amount == null || isNaN(amount)) return '—';
    return 'LKR ' + Math.round(Number(amount)).toLocaleString('en-US');
  }

  function formatDate(dStr) {
    if (!dStr) return '—';
    try {
      const d = new Date(dStr + (dStr.includes('T') ? '' : 'T00:00:00'));
      return d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
    } catch (e) {
      return dStr;
    }
  }

  function refreshIcons() {
    if (window.lucide && typeof window.lucide.createIcons === 'function') {
      window.lucide.createIcons();
    }
  }

  async function apiRequest(path, options = {}) {
    const url = API_BASE + path;
    const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
    const config = Object.assign({}, options, { headers });

    try {
      const resp = await fetch(url, config);
      if (!resp.ok) {
        let errorMsg = `Server error: ${resp.status}`;
        try {
          const errData = await resp.json();
          errorMsg = errData.message || errData.error || errorMsg;
        } catch (_) {
          const text = await resp.text();
          if (text) errorMsg = text;
        }
        throw new Error(errorMsg);
      }
      const contentType = resp.headers.get('content-type') || '';
      if (contentType.includes('application/json')) {
        return await resp.json();
      }
      return await resp.text();
    } catch (err) {
      console.warn(`[WedPlan API] Error fetching ${path}:`, err);
      throw err;
    }
  }

  /* ------------------------------------------------------------- */
  /* Module 1: Auth & User Management                              */
  /* ------------------------------------------------------------- */
  function initAuthBridge() {
    // 1. Couple login
    const coupleLoginForm = $('main form[data-demo*="Signed in"], body[data-root="../"] form input#email')?.form;
    if (coupleLoginForm && location.pathname.includes('/couple/login')) {
      coupleLoginForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();

        const email = $('#email', coupleLoginForm)?.value?.trim();
        const password = $('#password', coupleLoginForm)?.value;

        if (!email || !password) return;

        const submitBtn = $('button[type="submit"]', coupleLoginForm);
        if (submitBtn) submitBtn.disabled = true;

        try {
          const user = await apiRequest('/users/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
          });
          setStoredUser(user);
          showToast(`Welcome back, ${user.name}!`);
          setTimeout(() => { location.href = 'dashboard.html'; }, 600);
        } catch (err) {
          showToast('Invalid email or password. Please try again.', true);
          if (submitBtn) submitBtn.disabled = false;
        }
      }, true);
    }

    // 2. Couple register
    const registerForm = $('form input#p1')?.form;
    if (registerForm && location.pathname.includes('/couple/register')) {
      registerForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();

        const p1 = $('#p1', registerForm)?.value?.trim();
        const p2 = $('#p2', registerForm)?.value?.trim();
        const email = $('#email', registerForm)?.value?.trim();
        const password = $('#password', registerForm)?.value;
        const confirm = $('#confirm', registerForm)?.value;

        if (password !== confirm) {
          showToast("Passwords don't match.", true);
          return;
        }

        const name = (p1 && p2) ? `${p1} & ${p2}` : (p1 || p2 || 'Valued Couple');
        const submitBtn = $('button[type="submit"]', registerForm);
        if (submitBtn) submitBtn.disabled = true;

        try {
          const user = await apiRequest('/users', {
            method: 'POST',
            body: JSON.stringify({
              name,
              email,
              password,
              role: 'COUPLE'
            })
          });
          setStoredUser(user);
          showToast('Account created successfully! Welcome to WedPlan.');
          setTimeout(() => { location.href = 'dashboard.html'; }, 700);
        } catch (err) {
          showToast(`Registration failed: ${err.message}`, true);
          if (submitBtn) submitBtn.disabled = false;
        }
      }, true);
    }

    // 3. Admin / Staff login
    const adminLoginForm = $('form input#username')?.form;
    if (adminLoginForm && location.pathname.includes('/admin/login')) {
      adminLoginForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();

        const username = $('#username', adminLoginForm)?.value?.trim();
        const password = $('#password', adminLoginForm)?.value;

        const submitBtn = $('button[type="submit"]', adminLoginForm);
        if (submitBtn) submitBtn.disabled = true;

        try {
          const user = await apiRequest('/users/login', {
            method: 'POST',
            body: JSON.stringify({ email: username, password })
          });
          setStoredUser(user);
          showToast(`Welcome, ${user.name}!`);
          setTimeout(() => { location.href = 'dashboard.html'; }, 600);
        } catch (err) {
          showToast('Invalid staff email/ID or password. (Hint: admin@wedplan.com / admin123)', true);
          if (submitBtn) submitBtn.disabled = false;
        }
      }, true);
    }

    // 4. Global Sign out interceptor
    document.addEventListener('click', function (e) {
      const logoutLink = e.target.closest('a[href*="index.html"], a[title="Sign out"], [data-logout]');
      if (logoutLink && (logoutLink.textContent.includes('Sign out') || logoutLink.title === 'Sign out' || logoutLink.hasAttribute('data-logout'))) {
        clearStoredUser();
      }
    });

    // 5. Staff Accounts list
    if (location.pathname.includes('/admin/staff.html')) {
      loadStaffList();
    }

    // 6. Staff account add form
    const staffForm = $('form input#id[value="S005"]')?.form || $('form[data-next="staff.html"]');
    if (staffForm && location.pathname.includes('/admin/staff-form')) {
      staffForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();

        const name = $('#name', staffForm)?.value?.trim();
        const email = $('#email', staffForm)?.value?.trim();
        const roleRadio = $('input[name="role"]:checked', staffForm);
        const role = (roleRadio && roleRadio.value === 'admin') ? 'ADMIN' : 'STAFF';

        try {
          await apiRequest('/users', {
            method: 'POST',
            body: JSON.stringify({
              name,
              email,
              password: 'staffPassword123',
              role
            })
          });
          showToast('Staff member added successfully.');
          setTimeout(() => { location.href = 'staff.html'; }, 700);
        } catch (err) {
          showToast(`Could not add staff: ${err.message}`, true);
        }
      }, true);
    }

    // 7. Couple Profile page sync
    if (location.pathname.includes('/couple/profile')) {
      syncCoupleProfile();
    }

    // 8. Couples list (Staff desk)
    if (location.pathname.includes('/couple/manage')) {
      loadCouplesList();
    }
  }

  async function loadStaffList() {
    const grid = $('main .grid.md\\:grid-cols-2');
    if (!grid) return;

    try {
      const users = await apiRequest('/users');
      const staffMembers = (users || []).filter(u => u.role === 'ADMIN' || u.role === 'STAFF');
      if (staffMembers.length === 0) return;

      grid.innerHTML = staffMembers.map(s => {
        const initials = s.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || 'ST';
        const isAdmin = s.role === 'ADMIN';
        return `
          <article class="card card-pad" data-user-id="${s.id}">
            <div class="flex items-start gap-4">
              <span class="avatar avatar-lg ${isAdmin ? 'avatar-brass' : ''}">${initials}</span>
              <div class="min-w-0">
                <p class="font-display text-2xl font-semibold text-forest leading-tight">${s.name}</p>
                <p class="text-sm text-muted">${s.email}</p>
                <div class="mt-2 flex gap-2">
                  <span class="badge ${isAdmin ? 'badge-admin' : 'badge-staff'} no-dot">${isAdmin ? 'Admin' : 'Planner support'}</span>
                  <span class="badge badge-active">Active</span>
                </div>
              </div>
            </div>
            <p class="mt-5 text-xs text-muted mono">ID: #${s.id} · WedPlan Staff</p>
            <div class="mt-4 flex gap-2">
              <a href="staff-form.html" class="btn btn-outline btn-sm"><i data-lucide="pencil"></i>Edit</a>
              <button class="btn btn-ghost btn-sm text-clay ml-auto" data-action="delete-staff" data-id="${s.id}"><i data-lucide="user-x"></i>Remove</button>
            </div>
          </article>
        `;
      }).join('');

      $$('[data-action="delete-staff"]', grid).forEach(btn => {
        btn.addEventListener('click', async () => {
          if (!confirm('Remove this staff account?')) return;
          try {
            await apiRequest(`/users/${btn.dataset.id}`, { method: 'DELETE' });
            showToast('Staff account removed.');
            loadStaffList();
          } catch (e) {
            showToast('Could not remove staff account.', true);
          }
        });
      });

      refreshIcons();
    } catch (e) {
      console.warn('Could not load live staff list:', e);
    }
  }

  async function loadCouplesList() {
    const tbody = $('#couple-table tbody');
    if (!tbody) return;

    try {
      const users = await apiRequest('/users');
      const couples = (users || []).filter(u => u.role === 'COUPLE' || u.role === 'USER');
      if (couples.length === 0) return;

      tbody.innerHTML = couples.map(c => {
        const initials = c.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || 'CP';
        return `
          <tr data-item data-tier="plus">
            <td>
              <div class="flex items-center gap-3">
                <span class="avatar avatar-brass">${initials}</span>
                <div>
                  <p class="font-semibold">${c.name}</p>
                  <p class="text-xs text-muted mono">#${c.id}</p>
                </div>
              </div>
            </td>
            <td>
              <p>${c.email}</p>
              <p class="text-xs text-muted">+94 77 123 4567</p>
            </td>
            <td>Active</td>
            <td>1+ booking</td>
            <td><span class="badge badge-plus no-dot"><i data-lucide="gem"></i>Plus</span></td>
            <td class="text-right">
              <button class="btn btn-ghost btn-sm text-clay" data-action="delete-couple" data-id="${c.id}">Remove</button>
            </td>
          </tr>
        `;
      }).join('');

      $$('[data-action="delete-couple"]', tbody).forEach(btn => {
        btn.addEventListener('click', async () => {
          if (!confirm('Remove this couple account?')) return;
          try {
            await apiRequest(`/users/${btn.dataset.id}`, { method: 'DELETE' });
            showToast('Couple account removed.');
            loadCouplesList();
          } catch (e) {
            showToast('Could not remove couple account.', true);
          }
        });
      });

      refreshIcons();
    } catch (e) {
      console.warn('Could not load live couples:', e);
    }
  }

  function syncCoupleProfile() {
    const user = getStoredUser();
    if (!user) return;

    const emailEl = $('#email');
    if (emailEl) emailEl.value = user.email;

    const p1El = $('#p1');
    const p2El = $('#p2');
    if (p1El && user.name) {
      const parts = user.name.split('&').map(s => s.trim());
      p1El.value = parts[0] || user.name;
      if (p2El && parts[1]) p2El.value = parts[1];
    }

    const titleEl = $('h1.font-display');
    if (titleEl) titleEl.textContent = user.name;

    const avatarEl = $('.avatar.avatar-brass');
    if (avatarEl) {
      avatarEl.textContent = user.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
    }
  }

  /* ------------------------------------------------------------- */
  /* Module 2: Vendor Management & Marketplace                      */
  /* ------------------------------------------------------------- */
  function initVendorBridge() {
    // 1. Marketplace listing (vendor/list.html)
    if (location.pathname.includes('/vendor/list')) {
      loadMarketplaceVendors();
    }

    // 2. Staff vendor manage table (vendor/manage.html)
    if (location.pathname.includes('/vendor/manage')) {
      loadVendorManageTable();
    }

    // 3. Vendor Form (Add / Edit) (vendor/form.html)
    const vendorForm = $('#vendor-form');
    if (vendorForm && location.pathname.includes('/vendor/form')) {
      setupVendorForm(vendorForm);
    }
  }

  const categoryImages = {
    venue: '../../img/reception.jpg',
    venues: '../../img/reception.jpg',
    photography: '../../img/photography.jpg',
    beauty: '../../img/beauty.jpg',
    florals: '../../img/florals.jpg',
    catering: '../../img/catering.jpg',
    music: '../../img/music.jpg'
  };

  async function loadMarketplaceVendors() {
    const grid = $('#vendor-grid');
    if (!grid) return;

    try {
      const vendors = await apiRequest('/vendors');
      if (!vendors || vendors.length === 0) return;

      grid.innerHTML = vendors.map(v => {
        const catKey = (v.category || 'venue').toLowerCase();
        const imgPath = v.imageUrl || categoryImages[catKey] || '../../img/reception.jpg';
        const rating = v.rating ? v.rating.toFixed(1) : '4.8';
        const loc = v.location || 'Colombo';

        return `
          <article data-item data-category="${catKey}" data-location="${loc.toLowerCase()}" class="card overflow-hidden grid md:grid-cols-[320px_1fr] card-hover">
            <a href="detail.html?id=${v.id}" class="block h-60 md:h-full img-zoom">
              <img class="img-cover" src="${imgPath}" alt="${v.businessName}">
            </a>
            <div class="p-6 sm:p-7 flex flex-col">
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p class="text-xs tracking-[.2em] uppercase text-brass font-bold">${v.category} · ${loc}</p>
                  <h2 class="font-display text-3xl font-semibold text-forest mt-1">${v.businessName}</h2>
                </div>
                <span class="badge badge-active">Available</span>
              </div>
              <p class="mt-3 text-sm text-ink/65 max-w-xl">${v.description || 'Dedicated wedding professional ready to make your celebration unforgettable.'}</p>
              <ul class="mt-4 flex flex-wrap gap-x-5 gap-y-2 text-sm text-ink/70">
                <li class="flex items-center gap-1.5"><i data-lucide="map-pin" class="w-4 h-4 text-brass"></i>${loc}</li>
                <li class="flex items-center gap-1.5"><i data-lucide="star" class="w-4 h-4 text-brass"></i>${rating} (verified)</li>
              </ul>
              <div class="mt-auto pt-6 flex flex-wrap items-end justify-between gap-4">
                <p><span class="text-sm text-muted">verified partner</span></p>
                <div class="flex gap-2">
                  <a href="../booking/book.html?vendorId=${v.id}&vendorName=${encodeURIComponent(v.businessName)}&location=${encodeURIComponent(loc)}&category=${encodeURIComponent(v.category)}" class="btn btn-primary btn-sm">Book now</a>
                </div>
              </div>
            </div>
          </article>
        `;
      }).join('');

      const countEl = $('[data-count-for="#vendor-grid"]');
      if (countEl) countEl.textContent = vendors.length;

      refreshIcons();
    } catch (e) {
      console.warn('Could not load live vendors from backend:', e);
    }
  }

  async function loadVendorManageTable() {
    const tbody = $('#vendor-table tbody');
    if (!tbody) return;

    try {
      const vendors = await apiRequest('/vendors');
      if (!vendors || vendors.length === 0) return;

      tbody.innerHTML = vendors.map(v => {
        const catKey = (v.category || 'venue').toLowerCase();
        const imgPath = v.imageUrl || categoryImages[catKey] || '../../img/reception.jpg';

        return `
          <tr data-item data-category="${catKey}" data-status="active">
            <td>
              <div class="flex items-center gap-3">
                <img src="${imgPath}" class="w-12 h-12 rounded-xl object-cover" alt="">
                <div>
                  <p class="font-semibold">${v.businessName}</p>
                  <p class="text-xs text-muted mono">V${v.id}</p>
                </div>
              </div>
            </td>
            <td>${v.category}</td>
            <td>${v.location}</td>
            <td>${v.email}</td>
            <td><span data-stars="${Math.round(v.rating || 5)}">${v.rating || 5} ★</span></td>
            <td><span class="badge badge-active">Active</span></td>
            <td class="text-right whitespace-nowrap">
              <a href="form.html?id=${v.id}" class="btn btn-ghost btn-sm"><i data-lucide="pencil"></i>Edit</a>
              <button class="btn btn-ghost btn-sm text-clay" data-action="delete-vendor" data-id="${v.id}" title="Delete"><i data-lucide="trash-2"></i></button>
            </td>
          </tr>
        `;
      }).join('');

      $$('[data-action="delete-vendor"]', tbody).forEach(btn => {
        btn.addEventListener('click', async () => {
          if (!confirm('Are you sure you want to delete this vendor?')) return;
          try {
            await apiRequest(`/vendors/${btn.dataset.id}`, { method: 'DELETE' });
            showToast('Vendor deleted successfully.');
            loadVendorManageTable();
          } catch (e) {
            showToast('Could not delete vendor.', true);
          }
        });
      });

      refreshIcons();
    } catch (e) {
      console.warn('Could not load manage vendors list:', e);
    }
  }

  async function setupVendorForm(form) {
    const params = new URLSearchParams(location.search);
    const vendorId = params.get('id');

    if (vendorId) {
      const titleSpan = $('h1.font-display span.mono');
      if (titleSpan) titleSpan.textContent = `V${vendorId}`;

      try {
        const v = await apiRequest(`/vendors/${vendorId}`);
        if (v) {
          if ($('#name', form)) $('#name', form).value = v.businessName || '';
          if ($('#category', form)) $('#category', form).value = v.category || 'Venue';
          if ($('#location', form)) $('#location', form).value = v.location || 'Colombo';
          if ($('#email', form)) $('#email', form).value = v.email || '';
          if ($('#desc', form)) $('#desc', form).value = v.description || '';
        }
      } catch (e) {
        console.warn('Could not fetch vendor details:', e);
      }
    }

    form.addEventListener('submit', async function (e) {
      e.preventDefault();
      e.stopImmediatePropagation();

      const businessName = $('#name', form)?.value?.trim();
      const category = $('#category', form)?.value || 'Venue';
      const loc = $('#location', form)?.value || 'Colombo';
      const email = $('#email', form)?.value?.trim();
      const description = $('#desc', form)?.value?.trim() || '';

      const payload = {
        businessName,
        category,
        location: loc,
        email,
        description,
        rating: 5.0,
        imageUrl: categoryImages[category.toLowerCase()] || '../../img/reception.jpg'
      };

      try {
        if (vendorId) {
          await apiRequest(`/vendors/${vendorId}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
          });
          showToast('Vendor updated successfully.');
        } else {
          await apiRequest('/vendors', {
            method: 'POST',
            body: JSON.stringify(payload)
          });
          showToast('Vendor created successfully.');
        }
        setTimeout(() => { location.href = 'manage.html'; }, 600);
      } catch (err) {
        showToast(`Error saving vendor: ${err.message}`, true);
      }
    }, true);
  }

  /* ------------------------------------------------------------- */
  /* Module 3: Booking Management                                  */
  /* ------------------------------------------------------------- */
  function initBookingBridge() {
    // 1. Book page (booking/book.html)
    const bookForm = $('#book-form');
    if (bookForm && location.pathname.includes('/booking/book')) {
      setupBookingPage(bookForm);
    }

    // 2. Booking confirmation (booking/confirmation.html)
    if (location.pathname.includes('/booking/confirmation')) {
      setupConfirmationPage();
    }

    // 3. My bookings (booking/my-bookings.html)
    if (location.pathname.includes('/booking/my-bookings')) {
      loadMyBookings();
    }

    // 4. Staff booking management (booking/manage.html)
    if (location.pathname.includes('/booking/manage')) {
      loadStaffBookingsTable();
    }
  }

  function setupBookingPage(form) {
    const params = new URLSearchParams(location.search);
    const vendorId = params.get('vendorId');
    const vendorName = params.get('vendorName');
    const loc = params.get('location');

    if (vendorName) {
      $$('h2.font-display').forEach(el => {
        if (el.textContent.includes('The Garden House')) el.textContent = vendorName;
      });
      const vendorInput = form.querySelector('[name="vendorId"]');
      if (vendorInput) vendorInput.value = vendorId || '1';
    }

    const user = getStoredUser();
    if (user) {
      const inputs = $$('section.card input[readonly]', form);
      if (inputs[0]) inputs[0].value = user.name || 'Nethmi Perera & Kavin Jayawardena';
      if (inputs[1]) inputs[1].value = user.email || 'nethmi@example.com';
    }

    form.addEventListener('submit', async function (e) {
      e.preventDefault();
      e.stopImmediatePropagation();

      let eventDate = form.querySelector('[name="eventDate"]')?.value;
      if (!eventDate) {
        const d = new Date();
        d.setMonth(d.getMonth() + 6);
        eventDate = d.toISOString().slice(0, 10);
      }
      const guests = parseInt(form.querySelector('[name="guests"]')?.value || '100', 10);
      const pkgRadio = form.querySelector('[name="package"]:checked');
      const packageName = (pkgRadio && pkgRadio.dataset.label) || 'Classic package';
      const notes = form.querySelector('[name="note"]')?.value || '';

      const totalEl = $('[data-out="total"]');
      let totalAmount = 400000;
      if (totalEl && totalEl.textContent) {
        const raw = totalEl.textContent.replace(/[^0-9]/g, '');
        if (raw) totalAmount = parseFloat(raw);
      }

      const depositAmount = totalAmount * 0.3;
      const vName = vendorName || 'The Garden House';
      const custName = user?.name || 'Nethmi & Kavin';
      const custEmail = user?.email || 'nethmi@example.com';

      const bookingRef = 'WP-' + Math.floor(100000 + Math.random() * 900000);

      const payload = {
        bookingReference: bookingRef,
        customerName: custName,
        customerEmail: custEmail,
        partnerName: custName,
        weddingDate: eventDate,
        location: loc || 'Colombo',
        vendorId: vendorId ? parseInt(vendorId, 10) : 1,
        vendorName: vName,
        packageId: 1,
        packageName: packageName,
        guestCount: guests,
        totalAmount: totalAmount,
        depositAmount: depositAmount,
        isPlusMember: true,
        status: 'PENDING',
        specialNotes: notes
      };

      try {
        const result = await apiRequest('/bookings', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        showToast('Booking requested successfully!');
        const ref = result.bookingReference || bookingRef;
        setTimeout(() => { location.href = `confirmation.html?ref=${ref}`; }, 600);
      } catch (err) {
        showToast(`Could not create booking: ${err.message}`, true);
      }
    }, true);
  }

  async function setupConfirmationPage() {
    const params = new URLSearchParams(location.search);
    const ref = params.get('ref');
    if (!ref) return;

    try {
      const b = await apiRequest(`/bookings/reference/${ref}`);
      if (!b) return;

      const refEl = $('aside .mono.font-semibold');
      if (refEl) refEl.textContent = b.bookingReference;

      const vendorEl = $('aside h2.font-display');
      if (vendorEl) vendorEl.textContent = b.vendorName;

      const pkgEl = $('aside p.text-muted');
      if (pkgEl) pkgEl.textContent = `${b.packageName || 'Wedding Package'} · ${b.guestCount || 100} guests`;

      const dateEl = $('aside dd');
      if (dateEl) dateEl.textContent = formatDate(b.weddingDate);

      const totals = $$('aside dd.font-semibold.text-forest, aside dl:last-of-type dd');
      if (totals[0]) totals[0].textContent = formatLKR(b.totalAmount);
      if (totals[1]) totals[1].textContent = formatLKR(b.depositAmount);
    } catch (e) {
      console.warn('Could not load booking reference:', e);
    }
  }

  async function loadMyBookings() {
    const container = $('[data-tab-panel="upcoming"]');
    if (!container) return;

    const user = getStoredUser();
    const email = user ? user.email : 'nethmi@example.com';

    try {
      let bookings = await apiRequest(`/bookings?customerEmail=${encodeURIComponent(email)}`);
      if (!bookings || bookings.length === 0) {
        bookings = await apiRequest('/bookings');
      }
      if (!bookings || bookings.length === 0) return;

      container.innerHTML = bookings.map(b => {
        const statusBadge = b.status === 'CONFIRMED'
          ? '<span class="badge badge-confirmed">Confirmed</span><span class="badge badge-deposit">Deposit paid</span>'
          : b.status === 'CANCELLED'
          ? '<span class="badge badge-cancelled">Cancelled</span>'
          : '<span class="badge badge-pending">Awaiting vendor</span>';

        return `
          <article class="card overflow-hidden grid md:grid-cols-[260px_1fr]" data-booking-id="${b.id}">
            <div class="h-52 md:h-full"><img class="img-cover" src="../../img/reception.jpg" alt="${b.vendorName}"></div>
            <div class="p-6 sm:p-7">
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p class="text-xs tracking-[.2em] uppercase text-brass font-bold">Booking · <span class="mono normal-case tracking-normal">${b.bookingReference}</span></p>
                  <h2 class="font-display text-3xl font-semibold text-forest mt-1">${b.vendorName}</h2>
                </div>
                <div class="flex gap-2">${statusBadge}</div>
              </div>
              <dl class="mt-5 grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
                <div><dt class="label !mb-1">Date</dt><dd class="font-semibold">${formatDate(b.weddingDate)}</dd></div>
                <div><dt class="label !mb-1">Package</dt><dd class="font-semibold">${b.packageName || 'Package'} · ${b.guestCount || 100}</dd></div>
                <div><dt class="label !mb-1">Total</dt><dd class="font-semibold">${formatLKR(b.totalAmount)}</dd></div>
                <div><dt class="label !mb-1">Deposit</dt><dd class="font-semibold">${formatLKR(b.depositAmount)}</dd></div>
              </dl>
              <div class="mt-6 flex flex-wrap gap-2">
                <a href="../vendor/detail.html" class="btn btn-outline btn-sm">View details</a>
                ${b.status !== 'CANCELLED' ? `<button class="btn btn-ghost btn-sm text-clay ml-auto" data-action="cancel-booking" data-id="${b.id}"><i data-lucide="x"></i>Cancel</button>` : ''}
              </div>
            </div>
          </article>
        `;
      }).join('');

      $$('[data-action="cancel-booking"]', container).forEach(btn => {
        btn.addEventListener('click', async () => {
          if (!confirm('Are you sure you want to cancel this booking?')) return;
          try {
            await apiRequest(`/bookings/${btn.dataset.id}/status?status=CANCELLED`, { method: 'PATCH' });
            showToast('Booking cancelled.');
            loadMyBookings();
          } catch (e) {
            showToast('Could not cancel booking.', true);
          }
        });
      });

      refreshIcons();
    } catch (e) {
      console.warn('Could not load user bookings:', e);
    }
  }

  async function loadStaffBookingsTable() {
    const tbody = $('#bookings-table tbody');
    if (!tbody) return;

    try {
      const bookings = await apiRequest('/bookings');
      if (!bookings || bookings.length === 0) return;

      tbody.innerHTML = bookings.map(b => {
        const initials = (b.customerName || 'NK').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
        const isPending = b.status === 'PENDING';
        const isConfirmed = b.status === 'CONFIRMED';
        const isCancelled = b.status === 'CANCELLED';

        const statusBadge = isConfirmed
          ? '<span class="badge badge-confirmed">Confirmed</span>'
          : isCancelled
          ? '<span class="badge badge-cancelled">Cancelled</span>'
          : '<span class="badge badge-pending">Awaiting vendor</span>';

        return `
          <tr data-item data-status="${(b.status || 'pending').toLowerCase()}">
            <td class="mono font-semibold">${b.bookingReference}</td>
            <td>
              <div class="flex items-center gap-3">
                <span class="avatar avatar-brass">${initials}</span>
                <div>
                  <p class="font-semibold">${b.customerName}</p>
                  <p class="text-xs text-muted">${b.customerEmail}</p>
                </div>
              </div>
            </td>
            <td>${b.vendorName}</td>
            <td>
              <p class="font-medium">${formatDate(b.weddingDate)}</p>
              <p class="text-xs text-muted">${b.packageName || 'Package'} · ${b.guestCount || 100} guests</p>
            </td>
            <td>${statusBadge}</td>
            <td><span class="badge badge-outline">${formatLKR(b.totalAmount)}</span></td>
            <td class="text-right whitespace-nowrap">
              ${isPending ? `<button class="btn btn-primary btn-sm" data-action="confirm-booking" data-id="${b.id}"><i data-lucide="check"></i>Confirm</button>` : ''}
              ${!isCancelled ? `<button class="btn btn-ghost btn-sm text-clay" data-action="staff-cancel-booking" data-id="${b.id}" title="Cancel"><i data-lucide="x"></i></button>` : '<span class="text-xs text-muted">Cancelled</span>'}
            </td>
          </tr>
        `;
      }).join('');

      $$('[data-action="confirm-booking"]', tbody).forEach(btn => {
        btn.addEventListener('click', async () => {
          try {
            await apiRequest(`/bookings/${btn.dataset.id}/status?status=CONFIRMED`, { method: 'PATCH' });
            showToast('Booking confirmed!');
            loadStaffBookingsTable();
          } catch (e) {
            showToast('Could not confirm booking.', true);
          }
        });
      });

      $$('[data-action="staff-cancel-booking"]', tbody).forEach(btn => {
        btn.addEventListener('click', async () => {
          if (!confirm('Cancel this booking?')) return;
          try {
            await apiRequest(`/bookings/${btn.dataset.id}/status?status=CANCELLED`, { method: 'PATCH' });
            showToast('Booking cancelled.');
            loadStaffBookingsTable();
          } catch (e) {
            showToast('Could not cancel booking.', true);
          }
        });
      });

      refreshIcons();
    } catch (e) {
      console.warn('Could not load staff bookings:', e);
    }
  }

  /* ------------------------------------------------------------- */
  /* Module 4: Reviews & Alerts                                    */
  /* ------------------------------------------------------------- */
  function initReviewBridge() {
    // 1. Write review (review/write.html)
    const writeForm = $('main form[data-demo*="review was sent"], main form input[name="bookingId"]')?.form;
    if (writeForm && location.pathname.includes('/review/write')) {
      writeForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();

        const ratingRadio = writeForm.querySelector('[name="rating"]:checked');
        const rating = ratingRadio ? parseInt(ratingRadio.value, 10) : 5;
        const title = $('#title', writeForm)?.value?.trim() || 'Wonderful experience';
        const story = $('#text', writeForm)?.value?.trim();

        if (!story) return;

        const user = getStoredUser();
        const coupleName = user ? user.name : 'Nethmi & Kavin';
        const coupleEmail = user ? user.email : 'nethmi@example.com';

        const payload = {
          coupleName,
          coupleEmail,
          vendorId: 1,
          vendorName: 'Golden Hour Studio',
          title,
          story,
          rating,
          status: 'PENDING',
          alertType: 'REVIEW'
        };

        try {
          await apiRequest('/reviews', {
            method: 'POST',
            body: JSON.stringify(payload)
          });
          showToast('Thank you! Your story has been submitted for moderation.');
          setTimeout(() => { location.href = 'list.html'; }, 700);
        } catch (err) {
          showToast(`Error submitting story: ${err.message}`, true);
        }
      }, true);
    }

    // 2. Stories list (review/list.html)
    if (location.pathname.includes('/review/list')) {
      loadReviewsList();
    }

    // 3. Moderate reviews (review/moderate.html)
    if (location.pathname.includes('/review/moderate')) {
      loadReviewModeration();
    }
  }

  async function loadReviewsList() {
    const grid = $('#review-grid');
    if (!grid) return;

    try {
      const reviews = await apiRequest('/reviews?status=APPROVED');
      if (!reviews || reviews.length === 0) return;

      grid.innerHTML = reviews.map(r => {
        const initials = (r.coupleName || 'CK').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
        return `
          <figure data-item class="card card-pad mb-6 break-inside-avoid">
            <div class="flex items-center justify-between">
              <span class="text-brass font-bold">${'★'.repeat(r.rating || 5)}</span>
              <span class="badge badge-verified">Verified</span>
            </div>
            <blockquote class="mt-4 font-display text-2xl leading-snug text-forest">"${r.title || 'Exceptional experience'}"</blockquote>
            <p class="mt-3 text-sm text-ink/70 leading-relaxed">${r.story}</p>
            <figcaption class="mt-5 flex items-center gap-3 text-sm">
              <span class="avatar">${initials}</span>
              <span>
                <span class="font-semibold block">${r.coupleName}</span>
                <span class="text-muted">${r.vendorName || 'WedPlan Vendor'}</span>
              </span>
            </figcaption>
          </figure>
        `;
      }).join('');

      const countEl = $('[data-count-for="#review-grid"]');
      if (countEl) countEl.textContent = reviews.length;

      refreshIcons();
    } catch (e) {
      console.warn('Could not load reviews:', e);
    }
  }

  async function loadReviewModeration() {
    const pendingPanel = $('[data-tab-panel="pending"]');
    const publishedPanel = $('[data-tab-panel="published"]');
    if (!pendingPanel) return;

    try {
      const reviews = await apiRequest('/reviews');
      if (!reviews || reviews.length === 0) return;

      const pending = reviews.filter(r => r.status === 'PENDING');
      const approved = reviews.filter(r => r.status === 'APPROVED');

      pendingPanel.innerHTML = pending.length ? pending.map(r => `
        <article class="card card-pad" data-review-id="${r.id}">
          <div class="flex flex-wrap items-center gap-3">
            <span class="avatar">CR</span>
            <div class="flex-1">
              <p class="font-semibold">${r.coupleName} <span class="text-muted font-normal">on</span> ${r.vendorName || 'Vendor'}</p>
              <p class="text-xs text-muted mono">#${r.id} · Rating: ${r.rating} ★</p>
            </div>
          </div>
          <p class="mt-4 font-display text-2xl text-forest">"${r.title || 'Wedding Story'}"</p>
          <p class="mt-2 text-ink/75 leading-relaxed">${r.story}</p>
          <div class="mt-5 flex flex-wrap justify-end gap-2">
            <button class="btn btn-ghost btn-sm text-clay" data-action="reject-review" data-id="${r.id}"><i data-lucide="eye-off"></i>Hide</button>
            <button class="btn btn-primary btn-sm" data-action="approve-review" data-id="${r.id}"><i data-lucide="check"></i>Publish</button>
          </div>
        </article>
      `).join('') : '<div class="card card-pad text-center text-muted">No reviews pending moderation.</div>';

      if (publishedPanel) {
        publishedPanel.innerHTML = approved.length ? approved.map(r => `
          <article class="card card-pad mb-4">
            <div class="flex items-center justify-between">
              <p class="font-semibold">${r.coupleName} · ${r.vendorName}</p>
              <span class="badge badge-verified">Published</span>
            </div>
            <p class="mt-2 text-ink/75 font-medium">"${r.title}"</p>
            <p class="text-sm text-muted mt-1">${r.story}</p>
          </article>
        `).join('') : '<div class="card card-pad text-center text-muted">No published reviews yet.</div>';
      }

      $$('[data-action="approve-review"]', pendingPanel).forEach(btn => {
        btn.addEventListener('click', async () => {
          try {
            await apiRequest(`/reviews/${btn.dataset.id}/moderate?status=APPROVED`, { method: 'PATCH' });
            showToast('Review approved & published!');
            loadReviewModeration();
          } catch (e) {
            showToast('Could not approve review.', true);
          }
        });
      });

      $$('[data-action="reject-review"]', pendingPanel).forEach(btn => {
        btn.addEventListener('click', async () => {
          try {
            await apiRequest(`/reviews/${btn.dataset.id}/moderate?status=REJECTED`, { method: 'PATCH' });
            showToast('Review hidden.');
            loadReviewModeration();
          } catch (e) {
            showToast('Could not hide review.', true);
          }
        });
      });

      refreshIcons();
    } catch (e) {
      console.warn('Could not load review moderation:', e);
    }
  }

  /* ------------------------------------------------------------- */
  /* Module 5 & 6: Payments Management & Checkout                 */
  /* ------------------------------------------------------------- */
  function initPaymentBridge() {
    // 1. Staff Payments list (payment/list.html)
    if (location.pathname.includes('/payment/list')) {
      loadPaymentsTable();
    }

    // 2. Staff Record payment form (payment/record.html)
    const recordForm = $('form[data-payment-calc]');
    if (recordForm && location.pathname.includes('/payment/record')) {
      setupPaymentRecordForm(recordForm);
    }

    // 3. User-side Checkout process (payment/checkout.html)
    if (location.pathname.includes('/payment/checkout')) {
      setupUserCheckout();
    }

    // 4. Dynamic Digital Invoice / Receipt (payment/invoice.html)
    if (location.pathname.includes('/payment/invoice')) {
      setupDynamicInvoice();
    }

    // 5. Couple My Payments history (payment/my-payments.html)
    if (location.pathname.includes('/payment/my-payments')) {
      setupMyPayments();
    }
  }

  async function loadPaymentsTable() {
    const tbody = $('#pay-table tbody');
    if (!tbody) return;

    // Load KPI summaries
    try {
      const summary = await apiRequest('/payments/summary');
      if (summary) {
        if ($('#kpi-collected')) $('#kpi-collected').textContent = formatLKR(summary.totalRevenue || 0);
        if ($('#kpi-pending')) $('#kpi-pending').textContent = summary.pending || 0;
        if ($('#kpi-refunded')) $('#kpi-refunded').textContent = formatLKR(summary.refundedRevenue || 0);
        if ($('#kpi-total')) $('#kpi-total').textContent = summary.total || 0;
      }
    } catch (_) {}

    try {
      const data = await apiRequest('/payments?size=100');
      const payments = data.content || data || [];
      if (payments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center p-8 text-muted">No payment records found.</td></tr>';
        return;
      }

      function renderRows(items) {
        tbody.innerHTML = items.map(p => {
          const initials = (p.customerName || 'NK').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
          const isCard = p.paymentMethod === 'CARD' || p.paymentMethod === 'ONLINE';
          const isBank = p.paymentMethod === 'BANK_TRANSFER';
          const isPending = p.paymentStatus === 'PENDING';
          const isCompleted = p.paymentStatus === 'COMPLETED';
          const isRefunded = p.paymentStatus === 'REFUNDED';

          const statusBadge = isCompleted
            ? '<span class="badge badge-paid">Completed</span>'
            : isPending
            ? '<span class="badge badge-pending">Pending</span>'
            : isRefunded
            ? '<span class="badge badge-refunded">Refunded</span>'
            : '<span class="badge badge-cancelled">' + p.paymentStatus + '</span>';

          const verifyBtn = isPending
            ? `<button class="btn btn-ghost btn-sm p-1.5 text-sage hover:bg-sage/10" data-action="quick-verify" data-id="${p.id}" title="Verify Bank Transfer"><i data-lucide="check-circle" class="w-4 h-4"></i></button>`
            : '';

          const refundBtn = isCompleted
            ? `<button class="btn btn-ghost btn-sm p-1.5 text-clay hover:bg-clay/10" data-action="quick-refund" data-id="${p.id}" title="Issue Refund"><i data-lucide="rotate-ccw" class="w-4 h-4"></i></button>`
            : '';

          return `
            <tr data-item data-id="${p.id}" data-method="${p.paymentMethod}" data-status="${p.paymentStatus}">
              <td class="mono font-semibold text-forest">
                <a href="invoice.html?id=${p.id}" class="hover:underline">${p.receiptNumber || ('#P' + p.id)}</a>
              </td>
              <td class="mono">${p.bookingReference}</td>
              <td>
                <div class="flex items-center gap-2">
                  <span class="avatar avatar-brass">${initials}</span>
                  <div>
                    <span class="font-semibold block text-forest">${p.customerName}</span>
                    <span class="text-xs text-muted block">${p.customerEmail || ''}</span>
                  </div>
                </div>
              </td>
              <td class="uppercase text-xs font-semibold text-ink/70">${p.paymentType || 'Deposit'}</td>
              <td>
                <span class="inline-flex items-center gap-1.5 font-medium">
                  <i data-lucide="${isCard ? 'credit-card' : isBank ? 'landmark' : 'wallet'}" class="w-4 h-4 text-brass"></i>
                  ${p.paymentMethod.replace('_', ' ')}
                </span>
              </td>
              <td class="text-ink/75">${formatDate(p.paymentDate)}</td>
              <td class="text-right font-semibold text-forest">${formatLKR(p.amount)}</td>
              <td>${statusBadge}</td>
              <td class="text-right">
                <div class="flex items-center justify-end gap-1">
                  <a href="invoice.html?id=${p.id}" class="btn btn-ghost btn-sm p-1.5" title="Digital Receipt"><i data-lucide="receipt" class="w-4 h-4"></i></a>
                  ${verifyBtn}
                  ${refundBtn}
                  <button class="btn btn-ghost btn-sm p-1.5 text-clay hover:bg-clay/10" data-action="delete-payment" data-id="${p.id}" title="Delete Record"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </div>
              </td>
            </tr>
          `;
        }).join('');

        refreshIcons();
        attachTableActions();
      }

      function attachTableActions() {
        $$('[data-action="quick-verify"]', tbody).forEach(btn => {
          btn.addEventListener('click', async (e) => {
            e.stopPropagation();
            if (!confirm('Verify bank transfer funds received and mark COMPLETED?')) return;
            try {
              await apiRequest(`/payments/${btn.dataset.id}/verify`, { method: 'PATCH' });
              showToast('Payment verified and marked COMPLETED!');
              loadPaymentsTable();
            } catch (err) {
              showToast('Could not verify payment: ' + err.message, true);
            }
          });
        });

        $$('[data-action="quick-refund"]', tbody).forEach(btn => {
          btn.addEventListener('click', async (e) => {
            e.stopPropagation();
            const reason = prompt('Enter reason for issuing refund:', 'Wedding schedule changed');
            if (reason === null) return;
            try {
              await apiRequest(`/payments/${btn.dataset.id}/refund?reason=${encodeURIComponent(reason || 'Customer request')}`, { method: 'PATCH' });
              showToast('Payment marked REFUNDED.');
              loadPaymentsTable();
            } catch (err) {
              showToast('Could not process refund: ' + err.message, true);
            }
          });
        });

        $$('[data-action="delete-payment"]', tbody).forEach(btn => {
          btn.addEventListener('click', async (e) => {
            e.stopPropagation();
            if (!confirm('Permanently delete this payment record? This cannot be undone.')) return;
            try {
              await apiRequest(`/payments/${btn.dataset.id}`, { method: 'DELETE' });
              showToast('Payment record deleted successfully.');
              loadPaymentsTable();
            } catch (err) {
              showToast('Could not delete payment: ' + err.message, true);
            }
          });
        });
      }

      renderRows(payments);

      const searchInput = $('#proto-pay-search');
      const methodFilter = $('#proto-pay-method-filter');
      const statusFilter = $('#proto-pay-status-filter');

      function filterPayments() {
        const q = (searchInput?.value || '').toLowerCase().trim();
        const m = methodFilter?.value || '';
        const s = statusFilter?.value || '';

        const filtered = payments.filter(p => {
          const matchQ = !q || (p.bookingReference || '').toLowerCase().includes(q) ||
                                (p.customerName || '').toLowerCase().includes(q) ||
                                (p.receiptNumber || '').toLowerCase().includes(q) ||
                                (p.transactionReference || '').toLowerCase().includes(q);
          const matchM = !m || p.paymentMethod === m;
          const matchS = !s || p.paymentStatus === s;
          return matchQ && matchM && matchS;
        });

        renderRows(filtered);
      }

      if (searchInput) searchInput.addEventListener('input', filterPayments);
      if (methodFilter) methodFilter.addEventListener('change', filterPayments);
      if (statusFilter) statusFilter.addEventListener('change', filterPayments);

    } catch (e) {
      console.warn('Could not load payments table:', e);
    }
  }

  async function setupPaymentRecordForm(form) {
    const bookingSelect = $('#bookingId', form);
    if (bookingSelect) {
      try {
        const bookings = await apiRequest('/bookings');
        if (bookings && bookings.length > 0) {
          bookingSelect.innerHTML = '<option value="">Choose a live booking…</option>' +
            bookings.map(b => `
              <option value="${b.bookingReference}" data-amount="${b.totalAmount}" data-plus="${b.isPlusMember ? 0.10 : 0}" data-couple="${b.customerName}" data-vendor="${b.vendorName}" data-event="${formatDate(b.weddingDate)}">
                ${b.bookingReference} · ${b.customerName} · ${b.vendorName}
              </option>
            `).join('');
        }
      } catch (e) {
        console.warn('Could not populate bookings dropdown:', e);
      }
    }

    form.addEventListener('submit', async function (e) {
      e.preventDefault();
      e.stopImmediatePropagation();

      const opt = bookingSelect?.selectedOptions[0];
      if (!opt || !opt.value) {
        showToast('Please select a booking to record payment for.', true);
        return;
      }

      const bookingRef = opt.value;
      const coupleName = opt.dataset.couple || 'Couple';
      const vendorName = opt.dataset.vendor || 'Vendor';

      const methodRadio = form.querySelector('[name="method"]:checked');
      const isBank = methodRadio && methodRadio.value === 'bank';

      let amount = parseFloat(opt.dataset.amount || '100000');
      const totalOut = $('[data-out="total"]', form);
      if (totalOut && totalOut.textContent) {
        const parsed = parseFloat(totalOut.textContent.replace(/[^0-9]/g, ''));
        if (parsed) amount = parsed;
      }

      const payload = {
        bookingReference: bookingRef,
        customerName: coupleName,
        customerEmail: 'couple@wedplan.lk',
        description: `Deposit payment for ${vendorName}`,
        amount: amount,
        currency: 'LKR',
        paymentMethod: isBank ? 'BANK_TRANSFER' : 'CARD',
        paymentStatus: 'COMPLETED',
        paymentDate: new Date().toISOString().slice(0, 10),
        transactionReference: 'TXN-' + Math.floor(100000 + Math.random() * 900000),
        receiptNumber: 'INV-' + new Date().getFullYear() + '-' + Math.floor(1000 + Math.random() * 9000),
        notes: 'Recorded via planner desk'
      };

      try {
        const created = await apiRequest('/payments', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        showToast('Payment recorded successfully!');
        setTimeout(() => { location.href = 'invoice.html?id=' + created.id; }, 600);
      } catch (err) {
        showToast(`Could not record payment: ${err.message}`, true);
      }
    }, true);
  }

  // User-Side Checkout Process
  async function setupUserCheckout() {
    const form = $('#user-checkout-form');
    if (!form) return;

    const bookingSelect = $('#checkoutBookingSelect');
    const customerNameInput = $('#customerName');
    const customerEmailInput = $('#customerEmail');
    const baseOut = $('#breakdown-base');
    const portionLabel = $('#breakdown-portion-label');
    const portionOut = $('#breakdown-portion');
    const plusRow = $('#breakdown-plus-row');
    const plusOut = $('#breakdown-plus');
    const bankRow = $('#breakdown-bank-row');
    const bankOut = $('#breakdown-bank');
    const totalOut = $('#breakdown-total');
    const plusCheck = $('#plusMemberCheck');
    const cardPanel = $('#proto-card-panel');
    const bankPanel = $('#proto-bank-panel');

    try {
      const bookings = await apiRequest('/bookings');
      if (bookings && bookings.length > 0) {
        bookingSelect.innerHTML = bookings.map(b => `
          <option value="${b.bookingReference}" data-amount="${b.totalAmount}" data-plus="${b.isPlusMember}" data-couple="${b.customerName}" data-email="${b.customerEmail}" data-vendor="${b.vendorName}" data-package="${b.packageName || 'Package'}">
            ${b.bookingReference} · ${b.customerName} · ${b.vendorName} (${formatLKR(b.totalAmount)})
          </option>
        `).join('');
      }
    } catch (_) {}

    const urlParams = new URLSearchParams(location.search);
    const paramBooking = urlParams.get('booking');
    if (paramBooking && bookingSelect) {
      for (let i = 0; i < bookingSelect.options.length; i++) {
        if (bookingSelect.options[i].value === paramBooking) {
          bookingSelect.selectedIndex = i;
          break;
        }
      }
    }

    function syncBookingDetails() {
      const opt = bookingSelect?.selectedOptions[0];
      if (!opt) return;
      if (customerNameInput && opt.dataset.couple) customerNameInput.value = opt.dataset.couple;
      if (customerEmailInput && opt.dataset.email) customerEmailInput.value = opt.dataset.email;
      if (plusCheck && opt.dataset.plus !== undefined) plusCheck.checked = (opt.dataset.plus === 'true');
      recalc();
    }

    function recalc() {
      const opt = bookingSelect?.selectedOptions[0];
      const base = parseFloat(opt?.dataset.amount || '873000');
      if (baseOut) baseOut.textContent = formatLKR(base);

      const type = $('[name="paymentType"]:checked', form)?.value || 'DEPOSIT';
      let pct = 30;
      let label = 'Portion (30% Deposit)';
      if (type === 'BALANCE') { pct = 70; label = 'Portion (70% Balance)'; }
      if (type === 'FULL') { pct = 100; label = 'Portion (100% Full)'; }
      if (portionLabel) portionLabel.textContent = label;

      const installment = base * (pct / 100);
      if (portionOut) portionOut.textContent = formatLKR(installment);

      const isPlus = plusCheck?.checked;
      let plusDisc = 0;
      if (isPlus) {
        plusRow?.classList.remove('hidden');
        plusDisc = installment * 0.10;
        if (plusOut) plusOut.textContent = '- ' + formatLKR(plusDisc);
      } else {
        plusRow?.classList.add('hidden');
      }

      const method = $('[name="checkoutMethod"]:checked', form)?.value || 'CARD';
      const isBank = (method === 'BANK_TRANSFER');
      let bankDisc = 0;
      if (isBank) {
        bankRow?.classList.remove('hidden');
        cardPanel?.classList.add('hidden');
        bankPanel?.classList.remove('hidden');
        bankDisc = (installment - plusDisc) * 0.05;
        if (bankOut) bankOut.textContent = '- ' + formatLKR(bankDisc);
      } else {
        bankRow?.classList.add('hidden');
        cardPanel?.classList.remove('hidden');
        bankPanel?.classList.add('hidden');
      }

      const total = installment - plusDisc - bankDisc;
      if (totalOut) totalOut.textContent = formatLKR(total);
    }

    if (bookingSelect) bookingSelect.addEventListener('change', syncBookingDetails);
    $$('[name="paymentType"]', form).forEach(r => r.addEventListener('change', recalc));
    $$('[name="checkoutMethod"]', form).forEach(r => r.addEventListener('change', recalc));
    if (plusCheck) plusCheck.addEventListener('change', recalc);

    syncBookingDetails();

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const opt = bookingSelect?.selectedOptions[0];
      const type = $('[name="paymentType"]:checked', form)?.value || 'DEPOSIT';
      const method = $('[name="checkoutMethod"]:checked', form)?.value || 'CARD';

      const payload = {
        bookingReference: opt ? opt.value : 'BK2041',
        customerName: customerNameInput ? customerNameInput.value : 'Nethmi & Kavin',
        customerEmail: customerEmailInput ? customerEmailInput.value : 'nethmi@example.com',
        vendorName: opt?.dataset.vendor || 'The Garden House',
        packageName: opt?.dataset.package || 'Classic Venue Package',
        paymentType: type,
        baseAmount: parseFloat(opt?.dataset.amount || '873000'),
        isPlusMember: plusCheck ? plusCheck.checked : true,
        paymentMethod: method,
        cardholderName: $('#protoCardholder')?.value || 'NETHMI PERERA',
        cardNumber: $('#protoCardNumber')?.value || '4242 4242 4242 4242',
        cardExpiry: $('#protoExpiry')?.value || '12/28',
        cardCvv: $('#protoCvv')?.value || '888',
        bankName: $('#protoBankSelect')?.value || 'Commercial Bank',
        bankReference: $('#protoBankRef')?.value || 'COMB-20261031-118'
      };

      try {
        const result = await apiRequest('/payments/checkout', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        showToast('Payment successful! Digital receipt generated.');
        setTimeout(() => { location.href = 'invoice.html?id=' + result.id; }, 600);
      } catch (err) {
        showToast('Payment error: ' + err.message, true);
      }
    });
  }

  // Dynamic Invoice loader for prototype/payment/invoice.html
  async function setupDynamicInvoice() {
    const urlParams = new URLSearchParams(location.search);
    const id = urlParams.get('id');
    const ref = urlParams.get('ref');

    try {
      let p = null;
      if (id) {
        p = await apiRequest('/payments/' + id);
      } else if (ref) {
        const list = await apiRequest('/payments/booking/' + encodeURIComponent(ref));
        if (list && list.length > 0) p = list[0];
      } else {
        const data = await apiRequest('/payments?size=1');
        const list = data.content || data || [];
        if (list.length > 0) p = list[0];
      }

      if (!p) return;

      if ($('#inv-num')) $('#inv-num').textContent = p.receiptNumber || ('#P' + p.id);
      if ($('#inv-date')) $('#inv-date').textContent = formatDate(p.paymentDate);
      if ($('#inv-couple')) $('#inv-couple').textContent = p.customerName;
      if ($('#inv-email')) $('#inv-email').textContent = p.customerEmail;
      if ($('#inv-booking')) $('#inv-booking').textContent = p.bookingReference;
      if ($('#inv-method')) $('#inv-method').textContent = (p.paymentMethod || 'CARD').replace('_', ' ');
      if ($('#inv-desc')) $('#inv-desc').textContent = p.description || 'Wedding Service Reservation';
      if ($('#inv-type')) $('#inv-type').textContent = p.paymentType || 'Deposit';
      if ($('#inv-table-amount')) $('#inv-table-amount').textContent = formatLKR(p.amount);
      if ($('#inv-total')) $('#inv-total').textContent = formatLKR(p.amount);

      if ($('#inv-orig') && p.originalAmount) {
        $('#inv-orig').textContent = formatLKR(p.originalAmount);
      }
      if ($('#inv-disc') && p.discountAmount) {
        $('#inv-disc').textContent = '- ' + formatLKR(p.discountAmount);
      }

      const stamp = $('#invoice-stamp');
      const badgeStamp = $('#inv-badge-stamp');
      if (p.paymentStatus === 'COMPLETED') {
        if (stamp) stamp.innerHTML = '<span class="border-4 border-forest font-display font-bold text-5xl tracking-widest uppercase p-4 rounded-xl rotate-[-12deg] block text-forest">VERIFIED PAID</span>';
        if (badgeStamp) badgeStamp.textContent = 'VERIFIED PAID';
      } else if (p.paymentStatus === 'PENDING') {
        if (stamp) stamp.innerHTML = '<span class="border-4 border-brass font-display font-bold text-5xl tracking-widest uppercase p-4 rounded-xl rotate-[-12deg] block text-brass">PENDING VERIFICATION</span>';
        if (badgeStamp) badgeStamp.textContent = 'PENDING VERIFICATION';
      } else if (p.paymentStatus === 'REFUNDED') {
        if (stamp) stamp.innerHTML = '<span class="border-4 border-clay font-display font-bold text-5xl tracking-widest uppercase p-4 rounded-xl rotate-[-12deg] block text-clay">REFUNDED</span>';
        if (badgeStamp) badgeStamp.textContent = 'REFUNDED';
      }

    } catch (e) {
      console.warn('Could not load dynamic invoice data:', e);
    }
  }

  // Couple My Payments history loader
  async function setupMyPayments() {
    const tbody = $('#my-pay-tbody');
    if (!tbody) return;

    const user = getStoredUser();
    const email = user ? user.email : 'nethmi@example.com';

    try {
      let payments = await apiRequest('/payments/customer/' + encodeURIComponent(email));
      if (!payments || payments.length === 0) {
        const data = await apiRequest('/payments?size=20');
        payments = data.content || data || [];
      }

      if (!payments || payments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="p-8 text-center text-muted">No payments recorded yet.</td></tr>';
        return;
      }

      tbody.innerHTML = payments.map(p => {
        const isCard = p.paymentMethod === 'CARD' || p.paymentMethod === 'ONLINE';
        const isCompleted = p.paymentStatus === 'COMPLETED';
        const badge = isCompleted
          ? '<span class="badge badge-paid">Completed</span>'
          : p.paymentStatus === 'PENDING'
          ? '<span class="badge badge-pending">Pending</span>'
          : '<span class="badge badge-refunded">' + p.paymentStatus + '</span>';

        return `
          <tr>
            <td class="mono font-semibold text-forest">${p.receiptNumber || ('#P' + p.id)}</td>
            <td class="mono">${p.bookingReference}</td>
            <td class="text-xs uppercase font-semibold text-ink/70">${p.paymentType || 'Deposit'}</td>
            <td>
              <span class="inline-flex items-center gap-1.5">
                <i data-lucide="${isCard ? 'credit-card' : 'landmark'}" class="w-4 h-4 text-brass"></i>
                ${p.paymentMethod.replace('_', ' ')}
              </span>
            </td>
            <td>${formatDate(p.paymentDate)}</td>
            <td class="text-right font-semibold">${formatLKR(p.amount)}</td>
            <td>${badge}</td>
            <td class="text-right"><a href="invoice.html?id=${p.id}" class="btn btn-ghost btn-sm"><i data-lucide="receipt"></i></a></td>
          </tr>
        `;
      }).join('');

      refreshIcons();
    } catch (e) {
      console.warn('Could not load couple payments:', e);
    }
  }

  /* ------------------------------------------------------------- */
  /* Module 7: Dashboards Sync                                     */
  /* ------------------------------------------------------------- */
  function initDashboardBridge() {
    const user = getStoredUser();

    // 1. Staff Admin Dashboard (admin/dashboard.html)
    if (location.pathname.includes('/admin/dashboard')) {
      if (user && user.name) {
        const h1 = $('main h1.font-display');
        if (h1) h1.innerHTML = `Good morning, <em class="italic text-brass">${user.name.split(' ')[0]}.</em>`;
      }

      loadAdminDashboardMetrics();
    }

    // 2. Couple Dashboard (couple/dashboard.html)
    if (location.pathname.includes('/couple/dashboard')) {
      if (user && user.name) {
        const h1 = $('main h1.font-display');
        if (h1) h1.innerHTML = `Hello, <em class="italic text-brass-light">${user.name}.</em>`;
      }

      loadCoupleDashboardMetrics(user);
    }
  }

  async function loadAdminDashboardMetrics() {
    try {
      const bookings = await apiRequest('/bookings');
      if (!bookings) return;

      const pending = bookings.filter(b => b.status === 'PENDING');

      // Update "New requests" stat
      const statValue = $$('.stat-value')[0] || $('p.stat-value');
      if (statValue) statValue.textContent = pending.length;

      // Update "Waiting for confirmation" list
      const list = $('section.card ul.divide-y');
      if (list && pending.length > 0) {
        list.innerHTML = pending.map(b => {
          const initials = (b.customerName || 'NK').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
          return `
            <li class="p-5 flex flex-wrap items-center gap-4">
              <span class="avatar avatar-brass">${initials}</span>
              <div class="flex-1 min-w-[10rem]">
                <p class="font-semibold">${b.customerName}</p>
                <p class="text-sm text-muted">${b.vendorName} · ${formatDate(b.weddingDate)} · <span class="mono">${b.bookingReference}</span></p>
              </div>
              <span class="badge badge-pending">Pending</span>
              <button class="btn btn-primary btn-sm" data-action="quick-confirm" data-id="${b.id}">
                <i data-lucide="check"></i>Confirm
              </button>
            </li>
          `;
        }).join('');

        $$('[data-action="quick-confirm"]', list).forEach(btn => {
          btn.addEventListener('click', async () => {
            try {
              await apiRequest(`/bookings/${btn.dataset.id}/status?status=CONFIRMED`, { method: 'PATCH' });
              showToast('Booking confirmed!');
              loadAdminDashboardMetrics();
            } catch (e) {
              showToast('Could not confirm booking.', true);
            }
          });
        });

        refreshIcons();
      }
    } catch (e) {
      console.warn('Could not load dashboard metrics:', e);
    }
  }

  async function loadCoupleDashboardMetrics(user) {
    const email = user ? user.email : 'nethmi@example.com';
    try {
      let bookings = await apiRequest(`/bookings?customerEmail=${encodeURIComponent(email)}`);
      if (!bookings || bookings.length === 0) {
        bookings = await apiRequest('/bookings');
      }
      if (!bookings || bookings.length === 0) return;

      const teamList = $('section.card ul.divide-y');
      if (teamList) {
        teamList.innerHTML = bookings.map(b => {
          const isConfirmed = b.status === 'CONFIRMED';
          const badge = isConfirmed
            ? '<span class="badge badge-deposit">Deposit paid</span>'
            : b.status === 'CANCELLED'
            ? '<span class="badge badge-cancelled">Cancelled</span>'
            : '<span class="badge badge-pending">Awaiting vendor</span>';

          return `
            <li class="p-5 flex flex-wrap items-center gap-4">
              <span class="stat-icon"><i data-lucide="heart"></i></span>
              <div class="flex-1 min-w-[10rem]">
                <p class="font-semibold">${b.vendorName}</p>
                <p class="text-sm text-muted">${b.packageName || 'Package'} · ${formatDate(b.weddingDate)}</p>
              </div>
              ${badge}
            </li>
          `;
        }).join('');

        refreshIcons();
      }
    } catch (e) {
      console.warn('Could not load couple dashboard:', e);
    }
  }

  /* ------------------------------------------------------------- */
  /* Main Boot                                                     */
  /* ------------------------------------------------------------- */
  function bootBridge() {
    initAuthBridge();
    initVendorBridge();
    initBookingBridge();
    initReviewBridge();
    initPaymentBridge();
    initDashboardBridge();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bootBridge);
  } else {
    bootBridge();
  }
})();
