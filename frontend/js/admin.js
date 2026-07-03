const currentUser = JSON.parse(localStorage.getItem('currentUser'));

if (!currentUser || currentUser.role !== 'SYSTEM_ADMIN') {
    alert("Unauthorized access. Redirecting to login.");
    window.location.href = 'login.html';
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('admin-greeting').textContent = `Admin: ${currentUser.fullName}`;
    loadUnapprovedSchools();
    loadUnvettedEducators();
    loadAllUsers();
    loadFeedback();
});

function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
}

async function loadUnapprovedSchools() {
    try {
        const response = await fetch(API_BASE + '/api/admin/schools/unapproved');
        const profiles = await response.json();
        const container = document.getElementById('unapproved-schools-container');
        container.innerHTML = '';

        if (profiles.length === 0) {
            container.innerHTML = '<p class="all-clear">No pending school registrations!</p>';
            return;
        }

        profiles.forEach(profile => {
            const card = document.createElement('div');
            card.className = 'item-card warning-accent';

            card.innerHTML = `
                <h4>${profile.institutionName}</h4>
                <p><strong>Admin Name:</strong> ${profile.user.fullName}</p>
                <p><strong>Email:</strong> ${profile.user.email}</p>
                <p><strong>County:</strong> ${profile.county}</p>
                <p><strong>MOE No:</strong> <span class="badge badge-warning">${profile.moeNumber}</span></p>
                <button class="btn-primary btn-block btn-approve" onclick="approveAccount(${profile.user.id})">Verify MOE & Approve Login</button>
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load schools:", error);
    }
}

async function approveAccount(userId) {
    if (!confirm("Are you sure you want to approve this account? They will be granted login access.")) return;

    try {
        const response = await fetch(`${API_BASE}/api/admin/users/approve/${userId}`, { method: 'PUT' });
        if (response.ok) {
            alert("Account approved successfully!");
            loadUnapprovedSchools();
            loadAllUsers();
        } else {
            alert("Failed to approve account.");
        }
    } catch (error) {
        console.error("Approval error:", error);
    }
}

async function loadUnvettedEducators() {
    try {
        const response = await fetch(API_BASE + '/api/admin/educators/unvetted');
        const profiles = await response.json();
        const container = document.getElementById('unvetted-container');
        container.innerHTML = '';

        if (profiles.length === 0) {
            container.innerHTML = '<p class="all-clear">All educators are currently verified!</p>';
            return;
        }

        profiles.forEach(profile => {
            const card = document.createElement('div');
            card.className = 'item-card error-accent';

            card.innerHTML = `
                <h4>${profile.user.fullName}</h4>
                <p><strong>Email:</strong> ${profile.user.email}</p>
                <p><strong>TSC No:</strong> <span class="badge badge-warning">${profile.tscRegistrationNumber}</span></p>
                <p><strong>Qualifications:</strong> ${profile.qualifications}</p>
                <button class="btn-primary btn-block btn-approve" onclick="verifyEducator(${profile.id})">Approve TSC & Verify</button>
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Failed to load educators:", error);
    }
}

async function verifyEducator(profileId) {
    if (!confirm("Are you sure you want to verify this TSC number? This allows the educator to apply for vacancies.")) return;

    try {
        const response = await fetch(`${API_BASE}/api/admin/educators/verify/${profileId}`, { method: 'PUT' });
        if (response.ok) {
            alert("Educator verified successfully!");
            loadUnvettedEducators();
            loadAllUsers();
        } else {
            alert("Failed to verify educator.");
        }
    } catch (error) {
        console.error("Verification error:", error);
    }
}

async function loadAllUsers() {
    try {
        const response = await fetch(API_BASE + '/api/admin/users');
        const users = await response.json();
        const tbody = document.getElementById('users-table-body');
        tbody.innerHTML = '';

        users.forEach(user => {
            const row = document.createElement('tr');

            const statusHtml = user.approved
                ? '<span class="badge badge-success">Approved</span>'
                : '<span class="badge badge-warning">Pending</span>';

            row.innerHTML = `
                <td>${user.id}</td>
                <td><strong>${user.fullName}</strong></td>
                <td>${user.email}</td>
                <td><span class="badge badge-info">${user.role}</span></td>
                <td>${statusHtml}</td>
            `;
            tbody.appendChild(row);
        });
    } catch (error) {
        console.error("Failed to load users:", error);
    }
}

async function loadFeedback() {
    try {
        const response = await fetch(API_BASE + '/api/feedback/all');
        const feedbacks = await response.json();
        const container = document.getElementById('feedback-container');
        container.innerHTML = '';

        if (feedbacks.length === 0) {
            container.innerHTML = '<p>No feedback submitted yet.</p>';
            return;
        }

        feedbacks.forEach(fb => {
            const div = document.createElement('div');
            div.className = 'item-card feedback-card';

            const date = new Date(fb.submittedAt).toLocaleString();

            div.innerHTML = `
                <div class="feedback-header">
                    <h4>${fb.subject}</h4>
                    <small class="muted">${date}</small>
                </div>
                <p><strong>From:</strong> ${fb.sender.fullName} (${fb.sender.email})</p>
                <p class="feedback-body">${fb.message}</p>
            `;
            container.appendChild(div);
        });
    } catch (error) {
        console.error("Error loading feedback:", error);
    }
}
