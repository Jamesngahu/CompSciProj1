const STRONG_PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/;

function switchTab(tab) {
    document.getElementById('login-form').classList.toggle('hidden', tab !== 'login');
    document.getElementById('verify-form').classList.add('hidden');
    document.getElementById('register-form').classList.toggle('hidden', tab !== 'register');

    document.getElementById('tab-login').classList.toggle('active', tab === 'login');
    document.getElementById('tab-register').classList.toggle('active', tab === 'register');
}

function backToLogin() {
    document.getElementById('verify-form').classList.add('hidden');
    document.getElementById('login-form').classList.remove('hidden');
}

function toggleRoleFields() {
    const role = document.getElementById('reg-role').value;
    const edFields = document.getElementById('educator-fields');
    const scFields = document.getElementById('school-fields');

    edFields.classList.add('hidden');
    scFields.classList.add('hidden');
    document.getElementById('reg-tsc').required = false;
    document.getElementById('reg-inst-name').required = false;
    document.getElementById('reg-county').required = false;
    document.getElementById('reg-moe').required = false;

    if (role === 'EDUCATOR') {
        edFields.classList.remove('hidden');
        document.getElementById('reg-tsc').required = true;
    } else if (role === 'SCHOOL_ADMIN') {
        scFields.classList.remove('hidden');
        document.getElementById('reg-inst-name').required = true;
        document.getElementById('reg-county').required = true;
        document.getElementById('reg-moe').required = true;
    }
}

function showMessage(el, text, isError) {
    el.textContent = text;
    el.className = 'message ' + (isError ? 'error' : 'success');
}

let pendingLoginEmail = '';

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const messageEl = document.getElementById('login-message');

    try {
        const response = await fetch(`${API_BASE}/api/users/login?email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`, {
            method: 'POST'
        });
        const text = await response.text();

        if (response.ok) {
            pendingLoginEmail = email;
            document.getElementById('verify-email-label').textContent = email;
            document.getElementById('login-form').classList.add('hidden');
            document.getElementById('verify-form').classList.remove('hidden');
            document.getElementById('verify-message').textContent = '';
            document.getElementById('verify-code').value = '';
            document.getElementById('verify-code').focus();
        } else {
            showMessage(messageEl, text || 'Invalid email or password.', true);
        }
    } catch (error) {
        showMessage(messageEl, 'Server error. Is the backend running?', true);
    }
});

document.getElementById('verify-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const code = document.getElementById('verify-code').value;
    const messageEl = document.getElementById('verify-message');

    try {
        const response = await fetch(`${API_BASE}/api/users/login/verify?email=${encodeURIComponent(pendingLoginEmail)}&code=${encodeURIComponent(code)}`, {
            method: 'POST'
        });

        if (response.ok) {
            const user = await response.json();
            localStorage.setItem('currentUser', JSON.stringify(user));
            showMessage(messageEl, 'Login successful!', false);

            setTimeout(() => {
                window.location.href = user.role === 'SYSTEM_ADMIN' ? 'admin-dashboard.html' : 'dashboard.html';
            }, 700);
        } else {
            const errorText = await response.text();
            showMessage(messageEl, errorText || 'Invalid or expired code.', true);
        }
    } catch (error) {
        showMessage(messageEl, 'Server error. Is the backend running?', true);
    }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById('reg-message');

    const user = {
        fullName: document.getElementById('reg-name').value,
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value,
        role: document.getElementById('reg-role').value
    };

    if (!STRONG_PASSWORD.test(user.password)) {
        showMessage(messageEl, 'Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a digit and a special character.', true);
        return;
    }

    try {
        const userResponse = await fetch(API_BASE + '/api/users/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(user)
        });

        if (userResponse.ok) {
            const savedUser = await userResponse.json();

            if (user.role === 'EDUCATOR') {
                const profile = {
                    tscRegistrationNumber: document.getElementById('reg-tsc').value,
                    qualifications: document.getElementById('reg-qualifications').value
                };
                await fetch(`${API_BASE}/api/educators/profile/${savedUser.id}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(profile)
                });
            } else if (user.role === 'SCHOOL_ADMIN') {
                const schoolProfile = {
                    institutionName: document.getElementById('reg-inst-name').value,
                    county: document.getElementById('reg-county').value,
                    moeNumber: document.getElementById('reg-moe').value
                };
                const schoolResponse = await fetch(`${API_BASE}/api/schools/profile/${savedUser.id}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(schoolProfile)
                });

                if (!schoolResponse.ok) {
                    const errText = await schoolResponse.text();
                    throw new Error(errText || "Failed to save School Profile.");
                }
            }

            if (user.role === 'DONOR') {
                showMessage(messageEl, 'Registration successful! You can now log in.', false);
            } else {
                showMessage(messageEl, 'Registration successful! Please wait for admin approval to log in.', false);
            }

            document.getElementById('register-form').reset();
            setTimeout(() => switchTab('login'), 2500);

        } else {
            const errText = await userResponse.text();
            showMessage(messageEl, errText || 'Registration failed. Email might already exist.', true);
        }
    } catch (error) {
        showMessage(messageEl, error.message || 'Server error during registration.', true);
    }
});
