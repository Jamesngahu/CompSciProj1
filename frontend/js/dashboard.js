// --- 1. SESSION MANAGEMENT & INITIALIZATION ---
const currentUser = JSON.parse(localStorage.getItem('currentUser'));

if (!currentUser) {
    window.location.href = 'login.html';
}

// Which side-panel tabs each role sees, and in what order.
const NAV_CONFIG = {
    DONOR: [
        { id: 'requests-section', label: 'Resource Requests' },
        { id: 'donor-history-section', label: 'My Donations' },
        { id: 'notifications-section', label: 'Alerts' },
        { id: 'feedback-section', label: 'Feedback' }
    ],
    SCHOOL_ADMIN: [
        { id: 'admin-section', label: 'Post New Needs' },
        { id: 'requests-section', label: 'My Requests' },
        { id: 'vacancies-section', label: 'My Vacancies' },
        { id: 'applications-section', label: 'Applications' },
        { id: 'notifications-section', label: 'Alerts' },
        { id: 'feedback-section', label: 'Feedback' }
    ],
    EDUCATOR: [
        { id: 'educator-profile-section', label: 'My Profile' },
        { id: 'vacancies-section', label: 'Vacancies' },
        { id: 'educator-history-section', label: 'My Applications' },
        { id: 'notifications-section', label: 'Alerts' },
        { id: 'feedback-section', label: 'Feedback' }
    ]
};

function switchView(sectionId) {
    document.querySelectorAll('.content-view').forEach(el => el.classList.add('hidden'));
    document.getElementById(sectionId)?.classList.remove('hidden');

    document.querySelectorAll('#side-nav button').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.target === sectionId);
    });
}

function buildSideNav() {
    const items = NAV_CONFIG[currentUser.role] || [];
    const sideNav = document.getElementById('side-nav');
    sideNav.innerHTML = '';

    items.forEach(item => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = item.label;
        btn.dataset.target = item.id;
        btn.onclick = () => switchView(item.id);
        sideNav.appendChild(btn);
    });

    if (items.length > 0) switchView(items[0].id);
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('user-greeting').textContent = `Welcome, ${currentUser.fullName} (${currentUser.role})`;

    buildSideNav();

    if (currentUser.role === 'SCHOOL_ADMIN') {
        loadSchoolApplications();
    }

    if (currentUser.role === 'DONOR') {
        loadDonorHistory();
    }

    if (currentUser.role === 'EDUCATOR') {
        loadEducatorProfile();
        loadEducatorHistory();
    }

    loadRequests();
    loadVacancies();
    loadNotifications();
});

function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
}

function showMessage(el, text, isError) {
    el.textContent = text;
    el.className = 'message ' + (isError ? 'error' : 'success');
}

// --- 2. RESOURCE REQUESTS & DONATIONS ---
async function loadRequests() {
    try {
        let endpoint = API_BASE + '/api/requests/all';
        if (currentUser.role === 'SCHOOL_ADMIN') {
            endpoint = `${API_BASE}/api/requests/school/${currentUser.id}`;
            const sectionTitle = document.querySelector('#requests-section h2');
            if (sectionTitle) sectionTitle.textContent = "My Posted Resource Requests";
        }

        const response = await fetch(endpoint);
        const requests = await response.json();
        const container = document.getElementById('requests-container');
        if (!container) return;

        container.innerHTML = '';

        if (requests.length === 0) {
            container.innerHTML = '<p>No active resource requests at the moment.</p>';
            return;
        }

        requests.forEach(req => {
            const isFulfilled = req.quantityFulfilled >= req.quantityRequested;
            const isMonetary = req.donationType === 'MONEY';
            const card = document.createElement('div');
            card.className = 'item-card';

            let donateHtml = '';
            if (currentUser.role === 'DONOR' && !isFulfilled) {
                donateHtml = isMonetary ? `
                    <div class="donate-row mpesa-row">
                        <input type="tel" id="donate-phone-${req.id}" placeholder="M-Pesa phone e.g. 0712345678">
                        <input type="number" id="donate-amount-${req.id}" placeholder="KES" min="1" max="${req.quantityRequested - req.quantityFulfilled}">
                        <button class="btn-primary" onclick="payWithMpesa(${req.id})">Pay with M-Pesa</button>
                    </div>
                    <p id="mpesa-status-${req.id}" class="message"></p>
                ` : `
                    <div class="donate-row">
                        <input type="number" id="donate-qty-${req.id}" placeholder="Qty" min="1" max="${req.quantityRequested - req.quantityFulfilled}">
                        <button class="btn-primary" onclick="makeDonation(${req.id})">Donate</button>
                    </div>
                `;
            }

            card.innerHTML = `
                <h4>${req.title}</h4>
                <span class="badge badge-info">${isMonetary ? 'Monetary donation' : req.itemType}</span>
                <p>🏫 ${req.institutionName || 'Unknown School'} &nbsp;·&nbsp; 📍 ${req.county || 'Unknown Location'}</p>
                <p>${req.description}</p>
                <div class="progress-box"><strong>Progress:</strong> ${isMonetary ? `KES ${req.quantityFulfilled} / ${req.quantityRequested}` : `${req.quantityFulfilled} / ${req.quantityRequested}`}</div>
                ${isFulfilled ? '<p class="fully-funded">Fully Funded!</p>' : donateHtml}
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load requests:", error);
    }
}

function toggleRequestType() {
    const donationType = document.querySelector('input[name="req-donation-type"]:checked').value;
    const itemTypeInput = document.getElementById('req-type');
    const qtyInput = document.getElementById('req-qty');

    if (donationType === 'MONEY') {
        itemTypeInput.classList.add('hidden');
        itemTypeInput.required = false;
        qtyInput.placeholder = 'Amount needed (KES)';
    } else {
        itemTypeInput.classList.remove('hidden');
        itemTypeInput.required = true;
        qtyInput.placeholder = 'Quantity Needed';
    }
}

document.getElementById('request-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById('request-message');
    const donationType = document.querySelector('input[name="req-donation-type"]:checked').value;
    const requestData = {
        title: document.getElementById('req-title').value,
        itemType: donationType === 'MONEY' ? 'Monetary Donation' : document.getElementById('req-type').value,
        donationType: donationType,
        quantityRequested: parseInt(document.getElementById('req-qty').value),
        description: document.getElementById('req-desc').value
    };
    try {
        const response = await fetch(`${API_BASE}/api/requests/create/${currentUser.id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        });
        if (response.ok) {
            showMessage(messageEl, "Request posted successfully!", false);
            document.getElementById('request-form').reset();
            loadRequests();
        } else {
            showMessage(messageEl, "Failed to post request.", true);
        }
    } catch (error) {
        showMessage(messageEl, "Server error.", true);
    }
});

async function makeDonation(requestId) {
    const qtyInput = document.getElementById(`donate-qty-${requestId}`);
    const quantityDonated = parseInt(qtyInput.value);

    if (!quantityDonated || quantityDonated <= 0) {
        alert("Please enter a valid quantity.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/donations/make/${currentUser.id}/${requestId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantityDonated })
        });

        if (response.ok) {
            alert("Thank you for your donation!");
            loadRequests();
            if (currentUser.role === 'DONOR') loadDonorHistory();
        } else {
            alert("Failed to process donation.");
        }
    } catch (error) {
        console.error("Donation error:", error);
    }
}

async function payWithMpesa(requestId) {
    const phoneInput = document.getElementById(`donate-phone-${requestId}`);
    const amountInput = document.getElementById(`donate-amount-${requestId}`);
    const statusEl = document.getElementById(`mpesa-status-${requestId}`);
    const phoneNumber = phoneInput.value.trim();
    const amount = parseInt(amountInput.value);

    if (!phoneNumber) {
        showMessage(statusEl, "Enter the M-Pesa phone number to pay with.", true);
        return;
    }
    if (!amount || amount <= 0) {
        showMessage(statusEl, "Enter a valid amount.", true);
        return;
    }

    showMessage(statusEl, "Sending M-Pesa prompt...", false);

    try {
        const response = await fetch(`${API_BASE}/api/mpesa/stkpush/${currentUser.id}/${requestId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phoneNumber, amount })
        });

        if (response.ok) {
            const result = await response.json();
            showMessage(statusEl, result.customerMessage || "Check your phone to complete payment. This is a test transaction and will be automatically refunded.", false);
        } else {
            const errText = await response.text();
            showMessage(statusEl, errText || "Failed to start M-Pesa payment.", true);
        }
    } catch (error) {
        showMessage(statusEl, "Server error starting M-Pesa payment.", true);
    }
}

async function loadDonorHistory() {
    try {
        const response = await fetch(`${API_BASE}/api/donations/history/${currentUser.id}`);
        const history = await response.json();
        const container = document.getElementById('donor-history-container');
        if (!container) return;

        container.innerHTML = '';

        if (history.length === 0) {
            container.innerHTML = '<p>You have not made any donations yet.</p>';
            return;
        }

        const statusBadge = { PENDING: '<span class="badge badge-info">Awaiting payment</span>', COMPLETED: '<span class="badge badge-success">Paid</span>', FAILED: '<span class="badge badge-warning">Failed</span>' };

        history.forEach(don => {
            const card = document.createElement('div');
            card.className = 'item-card success-accent';
            const date = new Date(don.donatedAt).toLocaleDateString();
            const isMonetary = don.donationType === 'MONEY';

            card.innerHTML = `
                <h4>${don.resourceRequest.title}</h4>
                <p><strong>Donated:</strong> ${isMonetary ? `KES ${don.quantity}` : `${don.quantity} units`}</p>
                <p><strong>To:</strong> 🏫 ${don.resourceRequest.institutionName}</p>
                ${isMonetary ? `<p>${statusBadge[don.status] || ''}</p>` : ''}
                <p class="muted"><strong>Date:</strong> ${date}</p>
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load donation history:", error);
    }
}

// --- 3. TEACHING VACANCIES & APPLICATIONS ---
async function loadVacancies() {
    try {
        let endpoint = API_BASE + '/api/vacancies/active';
        if (currentUser.role === 'SCHOOL_ADMIN') {
            endpoint = `${API_BASE}/api/vacancies/school/${currentUser.id}`;
            const sectionTitle = document.querySelector('#vacancies-section h2');
            if (sectionTitle) sectionTitle.textContent = "My Posted Teaching Vacancies";
        }

        const response = await fetch(endpoint);
        const vacancies = await response.json();
        const container = document.getElementById('vacancies-container');
        if (!container) return;

        container.innerHTML = '';

        if (vacancies.length === 0) {
            container.innerHTML = '<p>No active vacancies at the moment.</p>';
            return;
        }

        vacancies.forEach(vac => {
            const card = document.createElement('div');
            card.className = 'item-card';

            const applyHtml = (currentUser.role === 'EDUCATOR') ? `
                <button class="btn-primary btn-block" onclick="applyForVacancy(${vac.id})">Apply for Position</button>
            ` : '';

            // Handle both 'active'/'isActive' JSON serialization quirks
            const isPositionFilled = (vac.active === false || vac.isActive === false);
            const statusBadgeHtml = isPositionFilled
                ? `<span class="badge badge-warning">FILLED</span>`
                : `<span class="badge badge-success">OPEN</span>`;

            card.innerHTML = `
                <div class="item-card-title">
                    <h4>${vac.title}</h4>
                    ${currentUser.role === 'SCHOOL_ADMIN' ? statusBadgeHtml : ''}
                </div>
                <span class="badge badge-info">${vac.subjectArea}</span>
                <p>🏫 ${vac.institutionName || 'Unknown School'} &nbsp;·&nbsp; 📍 ${vac.county || 'Unknown Location'}</p>
                <p>${vac.description}</p>
                ${applyHtml}
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load vacancies:", error);
    }
}

document.getElementById('vacancy-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById('vacancy-message');
    const vacancyData = {
        title: document.getElementById('vac-title').value,
        subjectArea: document.getElementById('vac-subject').value,
        description: document.getElementById('vac-desc').value
    };
    try {
        const response = await fetch(`${API_BASE}/api/vacancies/create/${currentUser.id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(vacancyData)
        });
        if (response.ok) {
            showMessage(messageEl, "Vacancy posted successfully!", false);
            document.getElementById('vacancy-form').reset();
            loadVacancies();
        } else {
            showMessage(messageEl, "Failed to post vacancy.", true);
        }
    } catch (error) {
        showMessage(messageEl, "Server error.", true);
    }
});

async function applyForVacancy(vacancyId) {
    if (!confirm("Are you sure you want to apply for this position using your verified educator profile?")) return;
    try {
        const response = await fetch(`${API_BASE}/api/applications/apply/${vacancyId}/${currentUser.id}`, { method: 'POST' });
        if (response.ok) {
            alert("Application submitted successfully! The school administrator will review your profile.");
            if (currentUser.role === 'EDUCATOR') loadEducatorHistory();
        } else {
            const errorMsg = await response.text();
            alert(`Application failed: ${errorMsg}`);
        }
    } catch (error) {
        console.error("Application error:", error);
    }
}

// --- EDUCATOR PROFILE HANDLING ---
async function loadEducatorProfile() {
    try {
        const response = await fetch(`${API_BASE}/api/educators/profile/${currentUser.id}`);
        if (response.ok) {
            const profile = await response.json();
            document.getElementById('prof-tsc').value = profile.tscRegistrationNumber || '';
            document.getElementById('prof-qualifications').value = profile.qualifications || '';
        }
    } catch (error) {
        console.error("Failed to load profile:", error);
    }
}

document.getElementById('educator-profile-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById('prof-message');
    const profileData = {
        tscRegistrationNumber: document.getElementById('prof-tsc').value,
        qualifications: document.getElementById('prof-qualifications').value
    };

    try {
        const response = await fetch(`${API_BASE}/api/educators/profile/${currentUser.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(profileData)
        });

        if (response.ok) {
            showMessage(messageEl, "Profile updated successfully!", false);
        } else {
            showMessage(messageEl, "Failed to update profile.", true);
        }
    } catch (error) {
        showMessage(messageEl, "Server error.", true);
    }
});

async function loadEducatorHistory() {
    try {
        const response = await fetch(`${API_BASE}/api/applications/educator/${currentUser.id}`);
        const apps = await response.json();
        const container = document.getElementById('educator-history-container');
        if (!container) return;

        container.innerHTML = '';

        if (apps.length === 0) {
            container.innerHTML = '<p>You have not applied for any vacancies yet.</p>';
            return;
        }

        const statusClass = { PENDING: 'badge-info', ACCEPTED: 'badge-success', REJECTED: 'badge-warning' };

        apps.forEach(app => {
            const card = document.createElement('div');
            card.className = 'item-card';
            const date = new Date(app.appliedAt).toLocaleDateString();

            card.innerHTML = `
                <h4>${app.vacancy.title}</h4>
                <p><strong>Status:</strong> <span class="badge ${statusClass[app.status] || 'badge-info'}">${app.status}</span></p>
                <p class="muted"><strong>Date:</strong> ${date}</p>
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load application history:", error);
    }
}

// --- 4. NOTIFICATIONS & FEEDBACK ---
async function loadNotifications() {
    try {
        const response = await fetch(`${API_BASE}/api/notifications/${currentUser.id}`);
        const notifs = await response.json();
        const container = document.getElementById('notifications-container');
        if (!container) return;
        container.innerHTML = '';

        if (notifs.length === 0) {
            container.innerHTML = '<p class="muted">No new alerts.</p>';
            return;
        }

        notifs.forEach(n => {
            const div = document.createElement('div');
            div.className = 'notification-row' + (n.read ? '' : ' unread');

            div.innerHTML = `
                <span class="${n.read ? 'muted' : 'notification-text'}">${n.message}</span>
                ${!n.read ? `<button class="btn-link" onclick="markNotificationRead(${n.id})">Mark Read</button>` : '<span class="muted">Read</span>'}
            `;
            container.appendChild(div);
        });
    } catch (error) {
        console.error("Error loading notifications:", error);
    }
}

async function markNotificationRead(id) {
    await fetch(`${API_BASE}/api/notifications/read/${id}`, { method: 'PUT' });
    loadNotifications();
}

document.getElementById('feedback-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const msgEl = document.getElementById('feedback-message');
    const feedbackData = {
        subject: document.getElementById('fb-subject').value,
        message: document.getElementById('fb-message').value
    };
    try {
        const response = await fetch(`${API_BASE}/api/feedback/submit/${currentUser.id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(feedbackData)
        });
        if (response.ok) {
            showMessage(msgEl, "Feedback submitted to administrators successfully!", false);
            document.getElementById('feedback-form').reset();
        } else {
            showMessage(msgEl, "Failed to submit feedback.", true);
        }
    } catch (error) {
        showMessage(msgEl, "Server error.", true);
    }
});

// --- 5. SCHOOL ADMIN APPLICATION PROCESSING ---
async function loadSchoolApplications() {
    try {
        const response = await fetch(`${API_BASE}/api/applications/school/${currentUser.id}`);
        const apps = await response.json();
        const container = document.getElementById('applications-container');
        if (!container) return;
        container.innerHTML = '';

        if (apps.length === 0) {
            container.innerHTML = '<p>No applications received yet.</p>';
            return;
        }

        apps.forEach(app => {
            const card = document.createElement('div');
            card.className = 'item-card info-accent';

            let actionHtml = '';
            if (app.status === 'PENDING') {
                actionHtml = `
                    <div class="action-row">
                        <button class="btn-accept" onclick="updateAppStatus(${app.id}, 'ACCEPTED')">Accept</button>
                        <button class="btn-reject" onclick="updateAppStatus(${app.id}, 'REJECTED')">Reject</button>
                    </div>
                `;
            } else {
                const badgeClass = app.status === 'ACCEPTED' ? 'badge-success' : 'badge-warning';
                actionHtml = `<p class="status-line"><span class="badge ${badgeClass}">${app.status}</span></p>`;
            }

            card.innerHTML = `
                <h4>${app.vacancy.title}</h4>
                <p><strong>Applicant:</strong> ${app.educator.fullName}</p>
                <p><strong>Email:</strong> <a href="mailto:${app.educator.email}">${app.educator.email}</a></p>

                <div class="applicant-details">
                    <p><strong>TSC No:</strong> <span class="badge badge-warning">${app.applicantTsc || 'Not Provided'}</span></p>
                    <p><strong>Qualifications:</strong> ${app.applicantQualifications || 'Not Provided'}</p>
                </div>

                <p class="muted"><strong>Applied On:</strong> ${new Date(app.appliedAt).toLocaleDateString()}</p>
                ${actionHtml}
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load applications:", error);
    }
}

async function updateAppStatus(appId, status) {
    if (!confirm(`Are you sure you want to mark this application as ${status}?`)) return;
    try {
        const response = await fetch(`${API_BASE}/api/applications/${appId}/status?status=${status}`, { method: 'PUT' });
        if (response.ok) {
            alert(`Application ${status.toLowerCase()} successfully!`);
            loadSchoolApplications();
        } else {
            alert("Failed to update status.");
        }
    } catch (error) {
        console.error("Status update error:", error);
    }
}
