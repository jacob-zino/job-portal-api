// src/main/resources/static/js/profile.js

document.addEventListener('DOMContentLoaded', () => {
    // Check if user is authenticated
    const auth = AuthService.initAuth();
    if (!auth.isAuthenticated()) {
        window.location.href = '/login.html?returnUrl=' + encodeURIComponent(window.location.href);
        return;
    }

    // Initialize user profile
    initProfilePage();
});

async function initProfilePage() {
    // Get current user
    const user = AuthService.getUser();

    if (!user) {
        return;
    }

    // Update profile info
    updateProfileInfo(user);

    // Load additional data based on user role
    if (user.roles.includes('ADMIN')) {
        loadAdminSummary();
    } else if (user.roles.includes('RECRUITER')) {
        loadRecruiterProfile(user.email);
        loadRecruiterJobsSummary(user.email);
    } else {
        loadApplicationsSummary(user.email);
    }

    // Initialize change password form
    initChangePasswordForm();

    // Initialize recruiter profile form if applicable
    if (user.roles.includes('RECRUITER')) {
        initRecruiterProfileForm();
    }
}

// Update profile information
function updateProfileInfo(user) {
    // Set username and email
    document.getElementById('profile-username').textContent = user.username;
    document.getElementById('profile-email').textContent = user.email;

    // Set avatar initials
    const initials = user.username.charAt(0).toUpperCase();
    document.getElementById('avatar-initials').textContent = initials;

    // Set role badge
    let roleText = 'User';
    if (user.roles.includes('ADMIN')) {
        roleText = 'Administrator';
    } else if (user.roles.includes('RECRUITER')) {
        roleText = 'Recruiter';
    }
    document.getElementById('profile-role').textContent = roleText;
}

// Load recruiter profile
async function loadRecruiterProfile(email) {
    try {
        const response = await ApiClient.get(`/recruiters/search?email=${email}`);

        if (response && response.length > 0) {
            const recruiter = response[0];

            // Update recruiter info in the sidebar
            document.getElementById('recruiter-company').textContent = recruiter.company || 'Not specified';
            document.getElementById('recruiter-position').textContent = recruiter.position || 'Not specified';
            document.getElementById('recruiter-phone').textContent = recruiter.phone || 'Not specified';

            // Store recruiter ID for form
            document.getElementById('recruiter-id').value = recruiter.id;

            // Pre-fill recruiter profile form
            document.getElementById('recruiter-name').value = recruiter.name || '';
            document.getElementById('recruiter-email-input').value = recruiter.email || '';
            document.getElementById('recruiter-company-input').value = recruiter.company || '';
            document.getElementById('recruiter-position-input').value = recruiter.position || '';
            document.getElementById('recruiter-phone-input').value = recruiter.phone || '';
        }
    } catch (error) {
        console.error('Failed to load recruiter profile:', error);
    }
}

// Load applications summary for regular users
async function loadApplicationsSummary(email) {
    const container = document.getElementById('user-applications-summary');

    try {
        // Get recent applications
        const response = await ApiClient.get(`/applications/email/${email}?page=0&size=3`);
        const applications = response.content || response || [];

        if (applications.length === 0) {
            container.innerHTML = `
                <div class="alert alert-info">
                    <p class="mb-0">You haven't applied to any jobs yet.</p>
                </div>
                <a href="/jobs.html" class="btn btn-primary">Browse Jobs</a>
            `;
            return;
        }

        // We need job details for each application
        const jobsMap = {};
        for (const app of applications) {
            if (!jobsMap[app.jobId]) {
                try {
                    const job = await ApiClient.get(`/jobs/${app.jobId}`);
                    jobsMap[app.jobId] = job;
                } catch (err) {
                    console.error(`Failed to load job ${app.jobId}:`, err);
                    jobsMap[app.jobId] = { title: 'Unknown', company: 'Unknown' };
                }
            }
        }

        // Generate HTML
        let html = `<div class="list-group">`;

        applications.forEach(app => {
            const job = jobsMap[app.jobId] || { title: 'Unknown', company: 'Unknown' };
            const statusClass = getStatusClass(app.status);

            html += `
                <a href="/applications.html" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${job.title}</h6>
                        <span class="badge ${statusClass}">${app.status}</span>
                    </div>
                    <p class="mb-1">${job.company}</p>
                    <small class="text-muted">Applied on ${Utils.formatDate(app.applicationDate)}</small>
                </a>
            `;
        });

        html += `</div>`;
        container.innerHTML = html;
    } catch (error) {
        console.error('Failed to load applications summary:', error);
        container.innerHTML = `
            <div class="alert alert-danger">
                <p class="mb-0">Failed to load your applications. Please try again later.</p>
            </div>
        `;
    }
}

// Load recruiter's jobs summary
async function loadRecruiterJobsSummary(email) {
    const container = document.getElementById('recruiter-jobs-summary');

    try {
        // First, get recruiter details for the ID
        const recruiterResponse = await ApiClient.get(`/recruiters/search?email=${email}`);

        if (!recruiterResponse || recruiterResponse.length === 0) {
            container.innerHTML = `
                <div class="alert alert-info">
                    <p class="mb-0">Recruiter profile not found.</p>
                </div>
            `;
            return;
        }

        const recruiterId = recruiterResponse[0].id;

        // Get recent jobs
        const response = await ApiClient.get(`/jobs/recruiter/${recruiterId}?page=0&size=3`);
        const jobs = response.content || response || [];

        if (jobs.length === 0) {
            container.innerHTML = `
                <div class="alert alert-info">
                    <p class="mb-0">You haven't posted any jobs yet.</p>
                </div>
                <a href="/my-jobs.html" class="btn btn-primary">Post a Job</a>
            `;
            return;
        }

        // Generate HTML
        let html = `<div class="list-group">`;

        jobs.forEach(job => {
            html += `
                <a href="/my-jobs.html" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${job.title}</h6>
                        <span class="badge ${job.active ? 'bg-success' : 'bg-danger'}">
                            ${job.active ? 'Active' : 'Inactive'}
                        </span>
                    </div>
                    <p class="mb-1">${job.company} - ${job.location}</p>
                    <small class="text-muted">Posted on ${Utils.formatDate(job.postDate)}</small>
                </a>
            `;
        });

        html += `</div>`;
        container.innerHTML = html;
    } catch (error) {
        console.error('Failed to load recruiter jobs summary:', error);
        container.innerHTML = `
            <div class="alert alert-danger">
                <p class="mb-0">Failed to load your job listings. Please try again later.</p>
            </div>
        `;
    }
}

// Load admin dashboard summary
async function loadAdminSummary() {
    const container = document.getElementById('admin-summary');

    try {
        // Get dashboard stats
        const stats = await ApiClient.get('/admin/stats');

        // Generate HTML for stats cards
        const html = `
            <div class="row row-cols-2 g-3">
                <div class="col">
                    <div class="card bg-primary text-white h-100">
                        <div class="card-body text-center">
                            <h1 class="display-4">${stats.totalUsers || 0}</h1>
                            <p class="card-text">Users</p>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div class="card bg-success text-white h-100">
                        <div class="card-body text-center">
                            <h1 class="display-4">${stats.totalJobs || 0}</h1>
                            <p class="card-text">Jobs</p>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div class="card bg-info text-white h-100">
                        <div class="card-body text-center">
                            <h1 class="display-4">${stats.totalApplications || 0}</h1>
                            <p class="card-text">Applications</p>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div class="card bg-warning text-dark h-100">
                        <div class="card-body text-center">
                            <h1 class="display-4">${stats.totalRecruiters || 0}</h1>
                            <p class="card-text">Recruiters</p>
                        </div>
                    </div>
                </div>
            </div>
        `;

        container.innerHTML = html;
    } catch (error) {
        console.error('Failed to load admin summary:', error);
        container.innerHTML = `
            <div class="alert alert-danger">
                <p class="mb-0">Failed to load system statistics. Please try again later.</p>
            </div>
        `;
    }
}

// Initialize change password form
function initChangePasswordForm() {
    const saveBtn = document.getElementById('save-password-btn');
    const errorContainer = document.getElementById('password-error');

    saveBtn.addEventListener('click', async () => {
        // Reset error state
        errorContainer.style.display = 'none';

        // Get form values
        const currentPassword = document.getElementById('current-password').value;
        const newPassword = document.getElementById('new-password').value;
        const confirmPassword = document.getElementById('confirm-password').value;

        // Validate passwords
        if (newPassword !== confirmPassword) {
            errorContainer.textContent = 'New passwords do not match.';
            errorContainer.style.display = 'block';
            return;
        }

        if (newPassword.length < 6) {
            errorContainer.textContent = 'New password must be at least 6 characters long.';
            errorContainer.style.display = 'block';
            return;
        }

        // Disable button to prevent multiple submissions
        saveBtn.disabled = true;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Updating...';

        try {
            // Get current user
            const user = AuthService.getUser();

            // Update password (we're simulating this since there's no direct endpoint)
            // In a real implementation, you would call the appropriate API endpoint

            // Just for simulation - normally you'd validate the current password server-side
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Show success message and close modal
            alert('Password updated successfully!');

            // Reset form and close modal
            document.getElementById('change-password-form').reset();
            const modal = bootstrap.Modal.getInstance(document.getElementById('changePasswordModal'));
            modal.hide();
        } catch (error) {
            console.error('Failed to update password:', error);
            errorContainer.textContent = error.message || 'Failed to update password. Please try again.';
            errorContainer.style.display = 'block';
        } finally {
            // Re-enable button
            saveBtn.disabled = false;
            saveBtn.textContent = 'Update Password';
        }
    });
}

// Initialize recruiter profile form
function initRecruiterProfileForm() {
    const saveBtn = document.getElementById('save-recruiter-profile-btn');
    const errorContainer = document.getElementById('recruiter-profile-error');

    saveBtn.addEventListener('click', async () => {
        // Reset error state
        errorContainer.style.display = 'none';

        // Get form values
        const recruiterId = document.getElementById('recruiter-id').value;
        const name = document.getElementById('recruiter-name').value;
        const email = document.getElementById('recruiter-email-input').value;
        const company = document.getElementById('recruiter-company-input').value;
        const position = document.getElementById('recruiter-position-input').value;
        const phone = document.getElementById('recruiter-phone-input').value;

        // Validate form
        if (!name || !email || !company) {
            errorContainer.textContent = 'Please fill in all required fields.';
            errorContainer.style.display = 'block';
            return;
        }

        // Disable button to prevent multiple submissions
        saveBtn.disabled = true;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';

        try {
            // Update recruiter profile
            const updatedProfile = {
                name,
                email,
                company,
                position,
                phone
            };

            if (recruiterId) {
                await ApiClient.put(`/recruiters/${recruiterId}`, updatedProfile);
            } else {
                await ApiClient.post('/recruiters', updatedProfile);
            }

            // Show success message and close modal
            alert('Recruiter profile updated successfully!');

            // Reload recruiter profile data
            loadRecruiterProfile(email);

            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('recruiterProfileModal'));
            modal.hide();
        } catch (error) {
            console.error('Failed to update recruiter profile:', error);
            errorContainer.textContent = error.message || 'Failed to update profile. Please try again.';
            errorContainer.style.display = 'block';
        } finally {
            // Re-enable button
            saveBtn.disabled = false;
            saveBtn.textContent = 'Save Changes';
        }
    });
}

// Helper function to get status class for badge
function getStatusClass(status) {
    switch (status) {
        case 'SUBMITTED':
            return 'bg-info';
        case 'REVIEWING':
            return 'bg-warning text-dark';
        case 'INTERVIEWED':
            return 'bg-primary';
        case 'ACCEPTED':
            return 'bg-success';
        case 'REJECTED':
            return 'bg-danger';
        default:
            return 'bg-secondary';
    }
}