# UI prototype (WedPlan)

Static HTML designs for every WedPlan page, filled with sample data. They show what each page
should look like before the Java backend exists. Buttons and forms only pretend to work: they show a
small message and move to the next page.

The structure, shared files and conventions follow the
[hotel-room-reservation-system](https://github.com/IT26100781/hotel-room-reservation-system) prototype,
adapted from hotel rooms to wedding vendors:

| Hotel prototype     | WedPlan                                   |
|---------------------|-------------------------------------------|
| `guest/`            | `couple/` (one account per couple)        |
| `room/`             | `vendor/` (venues, photographers, …)      |
| `reservation/`      | `booking/` (wedding date + package)       |
| `payment/`          | `payment/` (30% deposit, then balance)    |
| `review/`           | `review/` (couple stories)                |
| `admin/`            | `admin/` (staff portal)                   |
| VIP 10% off         | Plus member 10% off                       |
| Cash 5% off         | Bank transfer 5% off                      |

## How to open them

- **Without running anything:** double-click `prototype/pages.html`, which links to every page.
  (Some browsers block local files from loading each other; if the page looks unstyled, use a server.)
- **With a tiny server:** from the `WedPlan` folder run `python -m http.server 5500` and open
  http://localhost:5500/prototype/pages.html.

You need internet: Tailwind, the Google fonts and the icons (Lucide) load from the web. All photos are local in `img/`.

## Shared files (use these in your JSPs too)

| File | What it does |
|------|--------------|
| `css/theme.css` | Colours and ready-made classes: `btn btn-primary`, `card`, `input`, `badge badge-paid`, `table`, `modal` ... |
| `js/tailwind.config.js` | Gives Tailwind our colours (`text-forest`, `bg-brass`, `text-muted`) and fonts (`font-display`). |
| `js/app.js` | Shared behaviour: menus, pop-ups, tabs, filters, package price and discount preview, star rating. |
| `js/layout.js` | Draws the header, footer and staff sidebar in the prototype only. Replace it with JSP includes. |

The colour token names (`forest`, `brass`) are kept from the shared theme so the same classes work;
in WedPlan `forest` is a deep slate-blue and `brass` is rose-gold, taken from the hero photo.

## Converting your page to JSP

1. Copy `prototype/<component>/<page>.html` to `src/main/webapp/WEB-INF/views/<component>/<page>.jsp`.
2. Add `<%@ page contentType="text/html;charset=UTF-8" %>` and
   `<%@ taglib prefix="c" uri="jakarta.tags.core" %>` at the top.
3. Change the asset paths to absolute ones: `../../css/theme.css` becomes
   `${pageContext.request.contextPath}/css/theme.css`, and the same for the js and img files.
4. Replace the sample rows and cards with `<c:forEach>` over the list your servlet puts in the request.
   The `<!-- JSP: ... -->` comments in each page show where.
5. On each `<form>`, remove `data-demo` and `data-next` and set the real `action` and `method="post"`.
6. Header, footer and sidebar: copy the markup from `js/layout.js` into `WEB-INF/views/common/header.jsp`,
   `footer.jsp` and `sidebar.jsp`, then `<%@ include %>` them and stop loading `layout.js`.

## Rules to keep it consistent

- Use the existing classes before inventing new styles. Badge per status: `badge-pending` (awaiting vendor),
  `badge-confirmed`, `badge-deposit`, `badge-paid`, `badge-completed`, `badge-cancelled`, `badge-refunded`,
  `badge-active`, `badge-suspended`, `badge-retired`, `badge-plus`, `badge-verified`.
- Booking total = package price + guests × per-guest rate, minus the Plus discount. Payments stack
  discounts as `1 − (1 − plus) × (1 − bank)`. All prices are calculated in Java; the JS is only a preview.
- Hiding a button is not security. Every servlet must still check the session user's role.
