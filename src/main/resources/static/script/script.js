    const themeToggle=document.querySelector('#themeToggle');
    const themeIcon=document.querySelector('#themeIcon');
    const themeColorMeta=document.querySelector('#themeColorMeta');
    function setTheme(theme,save=true){
      const dark=theme==='dark';
      document.body.dataset.theme=dark?'dark':'light';
      themeIcon.textContent=dark?'☀':'☾';
      themeToggle.setAttribute('aria-label',dark?'Switch to light mode':'Switch to dark mode');
      themeToggle.setAttribute('aria-pressed',String(dark));
      themeColorMeta.content=dark?'#1b1919':'#fbf8f3';
      if(save)localStorage.setItem('wedPlanTheme',dark?'dark':'light');
    }
    setTheme(localStorage.getItem('wedPlanTheme')||'light',false);
    themeToggle.addEventListener('click',()=>setTheme(document.body.dataset.theme==='dark'?'light':'dark'));
    const cards=[...document.querySelectorAll('.vendor-card')];
    const searchText=document.querySelector('#searchText');
    const searchCategory=document.querySelector('#searchCategory');
    const searchLocation=document.querySelector('#searchLocation');
    const resultsCount=document.querySelector('#resultsCount');
    const emptyState=document.querySelector('#emptyState');
    const toast=document.querySelector('#toast');
    let activeCategory='all';
    const favorites=new Set(JSON.parse(localStorage.getItem('wedPlanFavorites')||'[]'));
    function notify(message){toast.textContent=message;toast.classList.add('show');clearTimeout(notify.timer);notify.timer=setTimeout(()=>toast.classList.remove('show'),2600)}
    function applyFilters(){
      const q=searchText.value.trim().toLowerCase();
      const category=searchCategory.value!=='all'?searchCategory.value:activeCategory;
      const location=searchLocation.value;
      let visible=0;
      cards.forEach(card=>{const matchCategory=category==='all'||card.dataset.category===category;const matchLocation=location==='all'||card.dataset.location===location;const matchText=!q||card.dataset.name.includes(q)||card.dataset.category.toLowerCase().includes(q)||card.dataset.location.toLowerCase().includes(q);const show=matchCategory&&matchLocation&&matchText;card.style.display=show?'':'none';if(show)visible++});
      resultsCount.textContent=`${visible} sample listing${visible===1?'':'s'}`;
      emptyState.style.display=visible?'none':'block';
      document.querySelectorAll('.filter-pill').forEach(btn=>btn.classList.toggle('active',btn.dataset.filter===category));
      document.querySelectorAll('.category').forEach(btn=>btn.classList.toggle('active',btn.dataset.category===category));
    }
    document.querySelector('#searchForm').addEventListener('submit',e=>{e.preventDefault();activeCategory=searchCategory.value;applyFilters();document.querySelector('#vendors').scrollIntoView({behavior:'smooth'});});
    searchText.addEventListener('input',applyFilters);searchLocation.addEventListener('change',applyFilters);searchCategory.addEventListener('change',()=>{activeCategory=searchCategory.value;applyFilters()});
    document.querySelector('#filterPills').addEventListener('click',e=>{const btn=e.target.closest('[data-filter]');if(!btn)return;activeCategory=btn.dataset.filter;searchCategory.value=activeCategory;applyFilters()});
    document.querySelector('#categoryTiles').addEventListener('click',e=>{const btn=e.target.closest('[data-category]');if(!btn)return;activeCategory=btn.dataset.category;searchCategory.value=activeCategory;applyFilters();document.querySelector('#vendors').scrollIntoView({behavior:'smooth'})});
    document.querySelectorAll('.heart').forEach((button,index)=>{const card=button.closest('.vendor-card');const key=card.dataset.name;function update(){const saved=favorites.has(key);button.classList.toggle('saved',saved);button.textContent=saved?'♥':'♡';button.setAttribute('aria-pressed',saved)}update();button.addEventListener('click',()=>{favorites.has(key)?favorites.delete(key):favorites.add(key);localStorage.setItem('wedPlanFavorites',JSON.stringify([...favorites]));update();notify(favorites.has(key)?'Saved to your shortlist':'Removed from your shortlist')})});
    document.querySelector('#savedLink').addEventListener('click',()=>{const savedCards=cards.filter(c=>favorites.has(c.dataset.name));if(!savedCards.length){notify('Tap a heart to save vendors to your shortlist');return}activeCategory='all';searchCategory.value='all';searchText.value='';searchLocation.value='all';cards.forEach(c=>c.style.display=favorites.has(c.dataset.name)?'':'none');resultsCount.textContent=`${savedCards.length} saved listing${savedCards.length===1?'':'s'}`;emptyState.style.display='none';document.querySelectorAll('.filter-pill,.category').forEach(b=>b.classList.remove('active'));document.querySelector('#vendors').scrollIntoView({behavior:'smooth'})});
    const modal=document.querySelector('#bookingModal');let selectedVendor='';function openModal(vendor){selectedVendor=vendor;document.querySelector('#modalIntro').textContent=`Send a demo inquiry to ${vendor}.`;modal.classList.add('open');document.body.style.overflow='hidden';modal.querySelector('input').focus()}function closeModal(){modal.classList.remove('open');document.body.style.overflow=''}
    document.querySelectorAll('.book-btn').forEach(btn=>btn.addEventListener('click',()=>openModal(btn.dataset.vendor)));
    document.querySelector('#modalClose').addEventListener('click',closeModal);modal.addEventListener('click',e=>{if(e.target===modal)closeModal()});document.addEventListener('keydown',e=>{if(e.key==='Escape')closeModal()});
    document.querySelector('#bookingForm').addEventListener('submit',e=>{e.preventDefault();const data=new FormData(e.currentTarget);closeModal();e.currentTarget.reset();notify(`Inquiry preview ready for ${selectedVendor} — nothing was sent.`)});
    const menu=document.querySelector('#navlinks');document.querySelector('#menuToggle').addEventListener('click',e=>{const open=menu.classList.toggle('open');e.currentTarget.setAttribute('aria-expanded',open)});menu.querySelectorAll('a').forEach(a=>a.addEventListener('click',()=>{menu.classList.remove('open');document.querySelector('#menuToggle').setAttribute('aria-expanded','false')}));
    applyFilters();
