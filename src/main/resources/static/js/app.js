// Revenue Recognition & Accounting System Frontend Controller
const API_BASE = '/api/v1';

let appState = {
  projects: [],
  selectedProject: null,
  summary: null,
  activeTab: 'overview',
  revenueChart: null,
  modelChart: null,
};

// Initialize Application on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  initApp();
  setupEventListeners();
});

async function initApp() {
  await loadPortfolioSummary();
  await loadProjects();
}

// -------------------------------------------------------------
// API Calls & Data Fetching
// -------------------------------------------------------------
async function loadPortfolioSummary() {
  try {
    const res = await fetch(`${API_BASE}/analysis/summary`);
    if (!res.ok) throw new Error('Failed to load financial summary');
    const data = await res.json();
    appState.summary = data;
    renderSummaryMetrics(data);
    renderCharts(data);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function loadProjects() {
  try {
    const pricingModel = document.getElementById('filter-pricing-model').value;
    const status = document.getElementById('filter-status').value;
    
    let url = `${API_BASE}/projects?`;
    if (pricingModel) url += `pricingModel=${pricingModel}&`;
    if (status) url += `status=${status}&`;

    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to load projects');
    const projects = await res.json();
    appState.projects = projects;
    renderProjectsList(projects);

    if (projects.length > 0 && !appState.selectedProject) {
      selectProject(projects[0].id);
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function selectProject(projectId) {
  try {
    const [projectRes, analysisRes, milestonesRes, activitiesRes, invoicesRes] = await Promise.all([
      fetch(`${API_BASE}/projects/${projectId}`),
      fetch(`${API_BASE}/analysis/projects/${projectId}`),
      fetch(`${API_BASE}/projects/${projectId}/milestones`),
      fetch(`${API_BASE}/projects/${projectId}/activities`),
      fetch(`${API_BASE}/invoices/project/${projectId}`)
    ]);

    if (!projectRes.ok || !analysisRes.ok) throw new Error('Failed to load project details');

    const project = await projectRes.json();
    const analysis = await analysisRes.json();
    const milestones = milestonesRes.ok ? await milestonesRes.json() : [];
    const activities = activitiesRes.ok ? await activitiesRes.json() : [];
    const invoices = invoicesRes.ok ? await invoicesRes.json() : [];

    appState.selectedProject = {
      ...project,
      analysis,
      milestones,
      activities,
      invoices
    };

    highlightSelectedProjectCard(projectId);
    renderProjectDetailView();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// -------------------------------------------------------------
// Render UI Components
// -------------------------------------------------------------
function renderSummaryMetrics(data) {
  document.getElementById('metric-recognized-revenue').textContent = formatCurrency(data.totalPortfolioRecognizedRevenue);
  document.getElementById('metric-invoiced-amount').textContent = formatCurrency(data.totalPortfolioInvoicedAmount);
  document.getElementById('metric-incurred-cost').textContent = formatCurrency(data.totalPortfolioIncurredCost);
  document.getElementById('metric-gross-profit').textContent = formatCurrency(data.totalPortfolioGrossProfit);
  document.getElementById('metric-gross-margin').textContent = `${data.portfolioGrossProfitMarginPercentage || 0}%`;
  document.getElementById('metric-unbilled-wip').textContent = formatCurrency(data.totalUnbilledRevenueWIP);
  document.getElementById('metric-deferred-revenue').textContent = formatCurrency(data.totalDeferredRevenue);
  document.getElementById('metric-total-projects').textContent = data.totalProjectsCount;
}

function renderProjectsList(projects) {
  const container = document.getElementById('projects-list-container');
  const countBadge = document.getElementById('project-count-badge');
  countBadge.textContent = `${projects.length} Contracts`;

  const query = document.getElementById('project-search').value.toLowerCase();
  const filtered = projects.filter(p => 
    p.name.toLowerCase().includes(query) ||
    p.projectCode.toLowerCase().includes(query) ||
    p.clientName.toLowerCase().includes(query)
  );

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="p-6 text-center text-slate-400">
        <i class="fa-solid fa-folder-open text-3xl mb-2"></i>
        <p class="text-sm">No contract projects found.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(p => {
    const isSelected = appState.selectedProject && appState.selectedProject.id === p.id;
    const modelBadge = getPricingModelBadge(p.pricingModel);
    return `
      <div onclick="selectProject(${p.id})" 
           class="p-4 border-b border-slate-100 hover:bg-slate-50 cursor-pointer transition ${isSelected ? 'bg-blue-50/70 border-l-4 border-l-blue-600' : ''}">
        <div class="flex items-center justify-between mb-1">
          <span class="text-xs font-mono font-medium text-slate-500">${escapeHtml(p.projectCode)}</span>
          <span class="text-xs px-2 py-0.5 rounded-full font-medium ${modelBadge.cls}">${modelBadge.label}</span>
        </div>
        <h4 class="text-sm font-semibold text-slate-800 truncate">${escapeHtml(p.name)}</h4>
        <div class="flex items-center justify-between text-xs text-slate-500 mt-2">
          <span><i class="fa-regular fa-building mr-1"></i>${escapeHtml(p.clientName)}</span>
          <span class="font-medium text-slate-700">${p.totalContractValue ? formatCurrency(p.totalContractValue) : (p.defaultHourlyRate ? `$${p.defaultHourlyRate}/hr` : `Cost + ${p.costMarkupPercentage}%`)}</span>
        </div>
      </div>
    `;
  }).join('');
}

function highlightSelectedProjectCard(projectId) {
  document.querySelectorAll('#projects-list-container > div').forEach(el => {
    el.classList.remove('bg-blue-50/70', 'border-l-4', 'border-l-blue-600');
  });
}

function renderProjectDetailView() {
  const p = appState.selectedProject;
  if (!p) return;

  document.getElementById('detail-placeholder').classList.add('hidden');
  document.getElementById('detail-content').classList.remove('hidden');

  document.getElementById('detail-project-name').textContent = p.name;
  document.getElementById('detail-project-code').textContent = p.projectCode;
  document.getElementById('detail-client-name').textContent = p.clientName;
  
  const modelBadge = getPricingModelBadge(p.pricingModel);
  const badgeEl = document.getElementById('detail-pricing-badge');
  badgeEl.className = `text-xs px-2.5 py-1 rounded-full font-medium ${modelBadge.cls}`;
  badgeEl.textContent = modelBadge.label;

  const a = p.analysis;
  document.getElementById('card-contract-val').textContent = a.totalContractValue ? formatCurrency(a.totalContractValue) : 'N/A';
  document.getElementById('card-recognized-rev').textContent = formatCurrency(a.totalRecognizedRevenue);
  document.getElementById('card-invoiced-amt').textContent = formatCurrency(a.totalInvoicedAmount);
  document.getElementById('card-incurred-cost').textContent = formatCurrency(a.totalIncurredCost);
  document.getElementById('card-gross-profit').textContent = formatCurrency(a.grossProfit);
  document.getElementById('card-gross-margin').textContent = `${a.grossProfitMarginPercentage}%`;
  document.getElementById('card-unbilled-wip').textContent = formatCurrency(a.unbilledRevenue);
  document.getElementById('card-deferred-rev').textContent = formatCurrency(a.deferredRevenue);

  // Health Pill
  const healthEl = document.getElementById('card-health-pill');
  if (a.financialHealth === 'HEALTHY') {
    healthEl.className = 'text-xs px-2.5 py-0.5 rounded-full font-medium bg-emerald-100 text-emerald-800';
    healthEl.textContent = '● Healthy Margin';
  } else if (a.financialHealth === 'LOSS_WARNING') {
    healthEl.className = 'text-xs px-2.5 py-0.5 rounded-full font-medium bg-rose-100 text-rose-800';
    healthEl.textContent = '● Loss Warning';
  } else {
    healthEl.className = 'text-xs px-2.5 py-0.5 rounded-full font-medium bg-amber-100 text-amber-800';
    healthEl.textContent = '● Moderate Margin';
  }

  // Progress Bar
  const progPct = Math.min(100, Math.max(0, a.contractConsumptionPercentage || 0));
  const progBar = document.getElementById('contract-progress-bar');
  progBar.style.width = `${progPct}%`;
  document.getElementById('contract-progress-text').textContent = `${progPct}% Recognized`;

  // Model-specific UI state
  const btnAddMilestone = document.getElementById('btn-add-milestone');
  const milestonesTabBtn = document.getElementById('tab-btn-milestones');
  if (p.pricingModel === 'FIXED_PRICE') {
    btnAddMilestone.classList.remove('hidden');
    milestonesTabBtn.classList.remove('hidden');
  } else {
    btnAddMilestone.classList.add('hidden');
    milestonesTabBtn.classList.add('hidden');
    if (appState.activeTab === 'milestones') {
      switchTab('activities');
    }
  }

  renderMilestonesList();
  renderActivitiesList();
  renderInvoicesList();
}

function switchTab(tabName) {
  appState.activeTab = tabName;
  ['overview', 'milestones', 'activities', 'invoices'].forEach(t => {
    const content = document.getElementById(`tab-content-${t}`);
    const btn = document.getElementById(`tab-btn-${t}`);
    if (content) content.classList.toggle('hidden', t !== tabName);
    if (btn) {
      if (t === tabName) {
        btn.classList.add('border-blue-600', 'text-blue-600');
        btn.classList.remove('border-transparent', 'text-slate-500');
      } else {
        btn.classList.remove('border-blue-600', 'text-blue-600');
        btn.classList.add('border-transparent', 'text-slate-500');
      }
    }
  });
}

function renderMilestonesList() {
  const container = document.getElementById('milestones-table-body');
  const milestones = appState.selectedProject?.milestones || [];

  if (milestones.length === 0) {
    container.innerHTML = `<tr><td colspan="5" class="py-8 text-center text-slate-400 text-sm">No milestones configured for this contract. Click "Add Milestone" to create one.</td></tr>`;
    return;
  }

  container.innerHTML = milestones.map(m => {
    let statusBadge = '<span class="px-2 py-0.5 text-xs rounded-full bg-slate-100 text-slate-700">PENDING</span>';
    if (m.status === 'COMPLETED') statusBadge = '<span class="px-2 py-0.5 text-xs rounded-full bg-emerald-100 text-emerald-700 font-semibold">COMPLETED</span>';
    if (m.status === 'INVOICED') statusBadge = '<span class="px-2 py-0.5 text-xs rounded-full bg-blue-100 text-blue-700 font-semibold">INVOICED</span>';

    const canComplete = m.status === 'PENDING' || m.status === 'IN_PROGRESS';
    return `
      <tr class="border-b border-slate-100 hover:bg-slate-50 text-sm">
        <td class="py-3 px-4 font-medium text-slate-800">${escapeHtml(m.title)}</td>
        <td class="py-3 px-4 font-semibold text-slate-700">${formatCurrency(m.deliverableAmount)}</td>
        <td class="py-3 px-4 text-slate-500">${m.completionPercentage ? `${m.completionPercentage}%` : '-'}</td>
        <td class="py-3 px-4">${statusBadge}</td>
        <td class="py-3 px-4 text-right">
          ${canComplete ? `
            <button onclick="markMilestoneComplete(${m.id})" class="text-xs px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-medium transition">
              <i class="fa-solid fa-check mr-1"></i> Mark Delivered
            </button>
          ` : `<span class="text-xs text-slate-400 font-medium">Ready / Billed</span>`}
        </td>
      </tr>
    `;
  }).join('');
}

function renderActivitiesList() {
  const container = document.getElementById('activities-table-body');
  const activities = appState.selectedProject?.activities || [];

  if (activities.length === 0) {
    container.innerHTML = `<tr><td colspan="6" class="py-8 text-center text-slate-400 text-sm">No activities or costs logged yet. Click "Log Activity / Cost" to record time or materials.</td></tr>`;
    return;
  }

  container.innerHTML = activities.map(a => {
    const isLabor = a.activityType === 'LABOR_HOURS';
    return `
      <tr class="border-b border-slate-100 hover:bg-slate-50 text-sm">
        <td class="py-3 px-4 text-slate-500 font-mono text-xs">${a.logDate}</td>
        <td class="py-3 px-4">
          <span class="text-xs px-2 py-0.5 rounded font-medium ${isLabor ? 'bg-indigo-50 text-indigo-700' : 'bg-amber-50 text-amber-700'}">
            ${a.activityType}
          </span>
        </td>
        <td class="py-3 px-4 text-slate-800">${escapeHtml(a.description)}</td>
        <td class="py-3 px-4 font-mono text-slate-600">${isLabor ? `${a.hoursWorked} hrs @ $${a.hourlyBillingRate}/hr` : '-'}</td>
        <td class="py-3 px-4 font-semibold text-rose-600">${formatCurrency(a.incurredCost)}</td>
        <td class="py-3 px-4">
          ${a.invoiced ? '<span class="text-xs px-2 py-0.5 rounded-full bg-blue-100 text-blue-700 font-medium">Invoiced</span>' : '<span class="text-xs px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 font-medium">Unbilled WIP</span>'}
        </td>
      </tr>
    `;
  }).join('');
}

function renderInvoicesList() {
  const container = document.getElementById('invoices-table-body');
  const invoices = appState.selectedProject?.invoices || [];

  if (invoices.length === 0) {
    container.innerHTML = `<tr><td colspan="6" class="py-8 text-center text-slate-400 text-sm">No invoices generated for this project. Click "Generate Automated Invoice" above.</td></tr>`;
    return;
  }

  container.innerHTML = invoices.map(inv => {
    let statusClass = 'bg-blue-100 text-blue-800';
    if (inv.status === 'PAID') statusClass = 'bg-emerald-100 text-emerald-800 font-semibold';
    if (inv.status === 'CANCELLED') statusClass = 'bg-rose-100 text-rose-800';

    return `
      <tr class="border-b border-slate-100 hover:bg-slate-50 text-sm">
        <td class="py-3 px-4 font-mono font-medium text-blue-600">${inv.invoiceNumber}</td>
        <td class="py-3 px-4 text-slate-500 font-mono text-xs">${inv.issueDate}</td>
        <td class="py-3 px-4 font-semibold text-slate-800">${formatCurrency(inv.totalAmount)}</td>
        <td class="py-3 px-4 font-semibold text-emerald-600">${formatCurrency(inv.recognizedRevenueAmount)}</td>
        <td class="py-3 px-4"><span class="text-xs px-2 py-0.5 rounded-full ${statusClass}">${inv.status}</span></td>
        <td class="py-3 px-4 text-right">
          <button onclick="viewInvoiceModal(${inv.id})" class="text-xs px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded font-medium transition mr-1">
            <i class="fa-regular fa-eye mr-1"></i> View
          </button>
          ${inv.status === 'ISSUED' ? `
            <button onclick="markInvoicePaid(${inv.id})" class="text-xs px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-medium transition">
              <i class="fa-solid fa-check-double mr-1"></i> Paid
            </button>
          ` : ''}
        </td>
      </tr>
    `;
  }).join('');
}

// -------------------------------------------------------------
// Interactive Operations (Modals & Actions)
// -------------------------------------------------------------
async function handleCreateProject(event) {
  event.preventDefault();
  const form = event.target;
  const payload = {
    projectCode: form.projectCode.value.trim(),
    name: form.name.value.trim(),
    clientName: form.clientName.value.trim(),
    pricingModel: form.pricingModel.value,
    totalContractValue: form.totalContractValue.value ? parseFloat(form.totalContractValue.value) : null,
    defaultHourlyRate: form.defaultHourlyRate.value ? parseFloat(form.defaultHourlyRate.value) : null,
    costMarkupPercentage: form.costMarkupPercentage.value ? parseFloat(form.costMarkupPercentage.value) : null,
    startDate: form.startDate.value || null,
  };

  try {
    const res = await fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to create contract project');
    }
    const created = await res.json();
    showToast(`Contract ${created.projectCode} successfully created!`, 'success');
    closeModal('modal-create-project');
    form.reset();
    await loadPortfolioSummary();
    await loadProjects();
    selectProject(created.id);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleAddMilestone(event) {
  event.preventDefault();
  const form = event.target;
  const projectId = appState.selectedProject?.id;
  if (!projectId) return;

  const payload = {
    title: form.title.value.trim(),
    description: form.description.value.trim(),
    deliverableAmount: parseFloat(form.deliverableAmount.value),
    completionPercentage: form.completionPercentage.value ? parseFloat(form.completionPercentage.value) : null,
    targetCompletionDate: form.targetCompletionDate.value || null
  };

  try {
    const res = await fetch(`${API_BASE}/projects/${projectId}/milestones`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to add milestone');
    }
    showToast('Milestone successfully added', 'success');
    closeModal('modal-add-milestone');
    form.reset();
    await selectProject(projectId);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function markMilestoneComplete(milestoneId) {
  const projectId = appState.selectedProject?.id;
  if (!projectId) return;

  try {
    const res = await fetch(`${API_BASE}/projects/${projectId}/milestones/${milestoneId}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'COMPLETED' })
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to update milestone');
    }
    showToast('Milestone delivered! Revenue is now recognized and ready for billing.', 'success');
    await loadPortfolioSummary();
    await selectProject(projectId);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleLogActivity(event) {
  event.preventDefault();
  const form = event.target;
  const projectId = appState.selectedProject?.id;
  if (!projectId) return;

  const type = form.activityType.value;
  const payload = {
    activityType: type,
    description: form.description.value.trim(),
    logDate: form.logDate.value || new Date().toISOString().split('T')[0],
    hoursWorked: type === 'LABOR_HOURS' ? parseFloat(form.hoursWorked.value) : null,
    hourlyBillingRate: type === 'LABOR_HOURS' && form.hourlyBillingRate.value ? parseFloat(form.hourlyBillingRate.value) : null,
    incurredCost: parseFloat(form.incurredCost.value || 0),
    materialOrDirectCost: type !== 'LABOR_HOURS' ? parseFloat(form.materialOrDirectCost.value || 0) : null
  };

  try {
    const res = await fetch(`${API_BASE}/projects/${projectId}/activities`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to record activity');
    }
    showToast('Activity logged! Unbilled WIP updated.', 'success');
    closeModal('modal-log-activity');
    form.reset();
    await loadPortfolioSummary();
    await selectProject(projectId);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function openGenerateInvoiceModal() {
  const p = appState.selectedProject;
  if (!p) return;

  document.getElementById('invoice-project-code-disp').textContent = `${p.projectCode} - ${p.name}`;
  document.getElementById('invoice-pricing-model-disp').textContent = p.pricingModel;

  const milestoneSelectGroup = document.getElementById('invoice-milestone-select-group');
  const milestoneSelect = document.getElementById('invoice-target-milestone');
  
  if (p.pricingModel === 'FIXED_PRICE') {
    milestoneSelectGroup.classList.remove('hidden');
    const completed = p.milestones.filter(m => m.status === 'COMPLETED');
    if (completed.length === 0) {
      showToast('No completed milestones available to invoice on this Fixed-Price contract.', 'warning');
    }
    milestoneSelect.innerHTML = `<option value="">All Completed Milestones (${completed.length})</option>` +
      completed.map(m => `<option value="${m.id}">${escapeHtml(m.title)} - ${formatCurrency(m.deliverableAmount)}</option>`).join('');
  } else {
    milestoneSelectGroup.classList.add('hidden');
  }

  openModal('modal-generate-invoice');
}

async function handleGenerateInvoice(event) {
  event.preventDefault();
  const form = event.target;
  const projectId = appState.selectedProject?.id;
  if (!projectId) return;

  const targetMilestoneId = form.targetMilestoneId.value ? parseInt(form.targetMilestoneId.value) : null;
  const payload = {
    projectId: projectId,
    issueDate: form.issueDate.value || null,
    dueDate: form.dueDate.value || null,
    targetMilestoneId: targetMilestoneId,
    taxRate: form.taxRate.value ? parseFloat(form.taxRate.value) : 0,
    notes: form.notes.value.trim()
  };

  try {
    const res = await fetch(`${API_BASE}/invoices/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Invoice generation failed');
    }
    const invoice = await res.json();
    showToast(`Invoice ${invoice.invoiceNumber} generated! Total: ${formatCurrency(invoice.totalAmount)}`, 'success');
    closeModal('modal-generate-invoice');
    form.reset();
    await loadPortfolioSummary();
    await selectProject(projectId);
    viewInvoiceModal(invoice.id);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function viewInvoiceModal(invoiceId) {
  try {
    const res = await fetch(`${API_BASE}/invoices/${invoiceId}`);
    if (!res.ok) throw new Error('Failed to fetch invoice details');
    const invoice = await res.json();

    document.getElementById('view-inv-number').textContent = invoice.invoiceNumber;
    document.getElementById('view-inv-issue-date').textContent = invoice.issueDate;
    document.getElementById('view-inv-due-date').textContent = invoice.dueDate;
    document.getElementById('view-inv-status').textContent = invoice.status;
    document.getElementById('view-inv-client').textContent = appState.selectedProject?.clientName || 'Valued Client';
    document.getElementById('view-inv-project').textContent = `${invoice.projectCode} (${invoice.pricingModel})`;
    document.getElementById('view-inv-subtotal').textContent = formatCurrency(invoice.subtotal);
    document.getElementById('view-inv-tax').textContent = `${formatCurrency(invoice.taxAmount)} (${invoice.taxRate}%)`;
    document.getElementById('view-inv-total').textContent = formatCurrency(invoice.totalAmount);
    document.getElementById('view-inv-recognized').textContent = formatCurrency(invoice.recognizedRevenueAmount);

    const itemsContainer = document.getElementById('view-inv-line-items');
    itemsContainer.innerHTML = invoice.lineItems.map(item => `
      <tr class="border-b border-slate-100 text-sm">
        <td class="py-2.5 px-3 font-medium text-slate-800">${escapeHtml(item.description)}</td>
        <td class="py-2.5 px-3 text-right text-slate-600 font-mono">${item.quantity}</td>
        <td class="py-2.5 px-3 text-right text-slate-600 font-mono">${formatCurrency(item.unitPrice)}</td>
        <td class="py-2.5 px-3 text-right font-semibold text-slate-800 font-mono">${formatCurrency(item.amount)}</td>
      </tr>
    `).join('');

    openModal('modal-view-invoice');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function markInvoicePaid(invoiceId) {
  try {
    const res = await fetch(`${API_BASE}/invoices/${invoiceId}/status?status=PAID`, {
      method: 'PATCH'
    });
    if (!res.ok) throw new Error('Failed to update invoice status');
    showToast('Invoice marked as PAID', 'success');
    await loadPortfolioSummary();
    await selectProject(appState.selectedProject.id);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// -------------------------------------------------------------
// Quick Demo Seeder
// -------------------------------------------------------------
async function seedDemoData() {
  const seedBtn = document.getElementById('btn-seed-demo');
  seedBtn.disabled = true;
  seedBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1.5"></i> Seeding...';

  try {
    // 1. Fixed Price Project
    const fpRes = await fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        projectCode: 'PRJ-FP-ALPHA',
        name: 'Enterprise Core Banking Migration',
        clientName: 'Global Horizon Financial',
        pricingModel: 'FIXED_PRICE',
        totalContractValue: 120000.00
      })
    });
    const fp = await fpRes.json();

    if (fp.id) {
      // Add milestones
      const m1Res = await fetch(`${API_BASE}/projects/${fp.id}/milestones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: 'M1: Architecture & Technical Blueprint',
          deliverableAmount: 40000.00,
          completionPercentage: 33.33
        })
      });
      const m1 = await m1Res.json();

      await fetch(`${API_BASE}/projects/${fp.id}/milestones`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: 'M2: Core Engine & Ledger Deployment',
          deliverableAmount: 50000.00,
          completionPercentage: 41.67
        })
      });

      // Mark M1 completed and generate invoice
      await fetch(`${API_BASE}/projects/${fp.id}/milestones/${m1.id}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'COMPLETED' })
      });

      // Log direct labor cost
      await fetch(`${API_BASE}/projects/${fp.id}/activities`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          activityType: 'LABOR_HOURS',
          description: 'Chief Architect Solution Blueprint Sprints',
          logDate: '2026-10-15',
          hoursWorked: 80.0,
          incurredCost: 16000.00
        })
      });

      await fetch(`${API_BASE}/invoices/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          projectId: fp.id,
          targetMilestoneId: m1.id,
          taxRate: 5.0
        })
      });
    }

    // 2. Time and Materials Project
    const tmRes = await fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        projectCode: 'PRJ-TM-BETA',
        name: 'Cloud Microservices & Kafka Integration',
        clientName: 'Omni Retail Supply Chain',
        pricingModel: 'TIME_AND_MATERIALS',
        defaultHourlyRate: 175.00,
        costMarkupPercentage: 10.00
      })
    });
    const tm = await tmRes.json();

    if (tm.id) {
      await fetch(`${API_BASE}/projects/${tm.id}/activities`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          activityType: 'LABOR_HOURS',
          description: 'Senior Kubernetes Infrastructure Setup',
          logDate: '2026-10-20',
          hoursWorked: 60.0,
          hourlyBillingRate: 175.0,
          incurredCost: 6000.0
        })
      });

      await fetch(`${API_BASE}/projects/${tm.id}/activities`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          activityType: 'MATERIAL_EXPENSE',
          description: 'Dedicated TLS Hardware Security Module',
          logDate: '2026-10-22',
          incurredCost: 2000.0,
          materialOrDirectCost: 2000.0
        })
      });

      await fetch(`${API_BASE}/invoices/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          projectId: tm.id,
          taxRate: 0.0
        })
      });
    }

    // 3. Cost-Plus Project
    const cpRes = await fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        projectCode: 'PRJ-CP-GAMMA',
        name: 'Smart Grid Infrastructure Research',
        clientName: 'Energy Logistics Authority',
        pricingModel: 'COST_PLUS',
        costMarkupPercentage: 20.00
      })
    });
    const cp = await cpRes.json();

    if (cp.id) {
      await fetch(`${API_BASE}/projects/${cp.id}/activities`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          activityType: 'MATERIAL_EXPENSE',
          description: 'High-frequency telemetry sensors',
          logDate: '2026-10-18',
          incurredCost: 15000.0,
          materialOrDirectCost: 15000.0
        })
      });
    }

    showToast('Demo data successfully generated across all 3 pricing models!', 'success');
    await loadPortfolioSummary();
    await loadProjects();
    if (fp.id) selectProject(fp.id);
  } catch (err) {
    showToast(`Seeding notice: ${err.message}`, 'info');
    await loadPortfolioSummary();
    await loadProjects();
  } finally {
    seedBtn.disabled = false;
    seedBtn.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles mr-1.5"></i> Seed Demo Contracts';
  }
}

// -------------------------------------------------------------
// Chart.js Visualizations
// -------------------------------------------------------------
function renderCharts(data) {
  // Revenue Breakdown Bar Chart
  const ctxRevenue = document.getElementById('chart-revenue-comparison');
  if (ctxRevenue) {
    const analyses = data.projectAnalyses || [];
    const labels = analyses.map(a => a.projectCode);
    const recognizedData = analyses.map(a => a.totalRecognizedRevenue);
    const invoicedData = analyses.map(a => a.totalInvoicedAmount);
    const costData = analyses.map(a => a.totalIncurredCost);

    if (appState.revenueChart) {
      appState.revenueChart.destroy();
    }

    appState.revenueChart = new Chart(ctxRevenue, {
      type: 'bar',
      data: {
        labels: labels.length ? labels : ['No Projects'],
        datasets: [
          {
            label: 'Recognized Revenue ($)',
            data: recognizedData.length ? recognizedData : [0],
            backgroundColor: '#2563eb',
            borderRadius: 4,
          },
          {
            label: 'Invoiced Amount ($)',
            data: invoicedData.length ? invoicedData : [0],
            backgroundColor: '#06b6d4',
            borderRadius: 4,
          },
          {
            label: 'Incurred Cost ($)',
            data: costData.length ? costData : [0],
            backgroundColor: '#f43f5e',
            borderRadius: 4,
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top', labels: { boxWidth: 12, font: { size: 11, family: 'Inter' } } }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: value => '$' + Number(value).toLocaleString()
            }
          }
        }
      }
    });
  }

  // Pricing Model Doughnut Chart
  const ctxModel = document.getElementById('chart-model-distribution');
  if (ctxModel) {
    const models = data.projectsByPricingModel || {};
    const modelLabels = Object.keys(models).map(k => k.replace('_', ' '));
    const modelCounts = Object.values(models);

    if (appState.modelChart) {
      appState.modelChart.destroy();
    }

    appState.modelChart = new Chart(ctxModel, {
      type: 'doughnut',
      data: {
        labels: modelLabels.length ? modelLabels : ['None'],
        datasets: [{
          data: modelCounts.length ? modelCounts : [1],
          backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6'],
          hoverOffset: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11, family: 'Inter' } } }
        },
        cutout: '65%'
      }
    });
  }
}

// -------------------------------------------------------------
// Modal & DOM Helpers
// -------------------------------------------------------------
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('hidden');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('hidden');
}

function setupEventListeners() {
  document.getElementById('project-search').addEventListener('input', () => {
    renderProjectsList(appState.projects);
  });

  document.getElementById('filter-pricing-model').addEventListener('change', loadProjects);
  document.getElementById('filter-status').addEventListener('change', loadProjects);

  // Dynamic fields in New Project Modal
  const modelSelect = document.getElementById('new-pricing-model');
  if (modelSelect) {
    modelSelect.addEventListener('change', (e) => {
      const val = e.target.value;
      const groupVal = document.getElementById('group-contract-val');
      const groupRate = document.getElementById('group-hourly-rate');
      const groupMarkup = document.getElementById('group-cost-markup');

      groupVal.classList.toggle('hidden', val !== 'FIXED_PRICE');
      groupRate.classList.toggle('hidden', val !== 'TIME_AND_MATERIALS');
      groupMarkup.classList.toggle('hidden', val === 'FIXED_PRICE');
    });
  }

  // Dynamic fields in Log Activity Modal
  const actTypeSelect = document.getElementById('new-activity-type');
  if (actTypeSelect) {
    actTypeSelect.addEventListener('change', (e) => {
      const isLabor = e.target.value === 'LABOR_HOURS';
      document.getElementById('group-activity-labor').classList.toggle('hidden', !isLabor);
      document.getElementById('group-activity-material').classList.toggle('hidden', isLabor);
    });
  }
}

function getPricingModelBadge(model) {
  switch (model) {
    case 'FIXED_PRICE':
      return { label: 'Fixed-Price', cls: 'badge-fixed-price' };
    case 'TIME_AND_MATERIALS':
      return { label: 'Time & Materials', cls: 'badge-time-materials' };
    case 'COST_PLUS':
      return { label: 'Cost-Plus', cls: 'badge-cost-plus' };
    default:
      return { label: model, cls: 'bg-slate-100 text-slate-800' };
  }
}

function formatCurrency(val) {
  if (val === null || val === undefined) return '$0.00';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, m => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  })[m]);
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  let icon = 'fa-circle-info text-blue-500';
  let border = 'border-blue-500';

  if (type === 'success') {
    icon = 'fa-circle-check text-emerald-500';
    border = 'border-emerald-500';
  } else if (type === 'error') {
    icon = 'fa-circle-exclamation text-rose-500';
    border = 'border-rose-500';
  } else if (type === 'warning') {
    icon = 'fa-triangle-exclamation text-amber-500';
    border = 'border-amber-500';
  }

  toast.className = `toast max-w-sm w-full bg-white shadow-lg rounded-lg pointer-events-auto flex ring-1 ring-black ring-opacity-5 border-l-4 ${border} p-4`;
  toast.innerHTML = `
    <div class="flex items-start">
      <i class="fa-solid ${icon} text-lg mt-0.5 mr-3"></i>
      <div class="flex-1">
        <p class="text-sm font-medium text-slate-900">${escapeHtml(message)}</p>
      </div>
      <button onclick="this.parentElement.parentElement.remove()" class="ml-3 text-slate-400 hover:text-slate-600">
        <i class="fa-solid fa-xmark text-sm"></i>
      </button>
    </div>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}
