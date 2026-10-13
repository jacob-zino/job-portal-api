// src/main/resources/static/js/admin.js

document.addEventListener('DOMContentLoaded', () => {
    // Check if user is admin, if not redirect to home
    const auth = AuthService.initAuth();
    if (!auth.isAdmin()) {
        window.location.href = '/';
        return;
    }

    // Initialize the admin dashboard
    initAdminDashboard();
});

function initAdminDashboard() {
    // Load dashboard stats
    loadDashboardStats();

    // Initially load users tab data
    loadUsers();

    // Set up tab change listeners
    document.querySelectorAll('button[data-bs-toggle="tab"]').forEach(tabButton => {
        tabButton.addEventListener('shown.bs.tab', event => {
            const targetTab = event.target.getAttribute('id');

            // Load data based on the selected tab
            if (targetTab === 'users-tab') {
                loadUsers();
            } else if (targetTab === 'jobs-tab') {
                loadJobs();
            } else if (targetTab === 'recruiters-tab') {
                loadRecruiters();
            } else if (targetTab === 'applications-tab') {
                loadApplications();
            }
        });
    });

    // Set up refresh buttons
    document.getElementById('refresh-users').addEventListener('click', loadUsers);
    document.getElementById('refresh-jobs').addEventListener('click', loadJobs);
    document.getElementById('refresh-recruiters').addEventListener('click', loadRecruiters);
    document.getElementById('refresh-applications').addEventListener('click', loadApplications);
}

// Load dashboard stats
async function loadDashboardStats() {
    try {
        const stats = await ApiClient.get('/admin/stats');

        // Update stats display
        document.getElementById('total-users').textContent = stats.totalUsers || 0;
        document.getElementById('total-jobs').textContent = stats.totalJobs || 0;
        document.getElementById('total-applications').textContent = stats.totalApplications || 0;
        document.getElementById('total-recruiters').textContent = stats.totalRecruiters || 0;
    } catch (error) {
        console.error('Failed to load dashboard stats:', error);
    }
}

// Load users data
async function loadUsers() {
    const usersTableBody = document.getElementById('users-table-body');

    try {
        // Show loading state
        usersTableBody.innerHTML = '<tr><td colspan="5" class="text-center">Loading users...</td></tr>';

        const users = await ApiClient.get('/admin/users');

        if (users.length === 0) {
            usersTableBody.innerHTML = '<tr><td colspan="5" class="text-center">No users found</td></tr>';
            return;
        }

        // Generate table rows
        let html = '';
        users.forEach(user => {
            html += `
                <tr>
                    <td>${user.username}</td>
                    <td>${user.email}</td>
                    <td>${user.roles.join(', ')}</td>
                    <td>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" role="switch" 
                                id="user-status-${user.id}" ${user.enabled ? 'checked' : ''}
                                onchange="toggleUserStatus('${user.id}', ${!user.enabled})">
                            <label class="form-check-label" for="user-status-${user.id}">
                                ${user.enabled ? 'Active' : 'Inactive'}
                            </label>
                        </div>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="viewUserDetails('${user.id}')">
                            <i class="fas fa-eye"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        usersTableBody.innerHTML = html;
    } catch (error) {
        usersTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Failed to load users</td></tr>';
        console.error('Failed to load users:', error);
    }
}

// Load jobs data
async function loadJobs() {
    const jobsTableBody = document.getElementById('jobs-table-body');

    try {
        // Show loading state
        jobsTableBody.innerHTML = '<tr><td colspan="6" class="text-center">Loading jobs...</td></tr>';

        const response = await ApiClient.get('/jobs?page=0&size=100'); // Get all jobs
        const jobs = response.content || [];

        if (jobs.length === 0) {
            jobsTableBody.innerHTML = '<tr><td colspan="6" class="text-center">No jobs found</td></tr>';
            return;
        }

        // Generate table rows
        let html = '';
        jobs.forEach(job => {
            html += `
                <tr>
                    <td>${job.title}</td>
                    <td>${job.company}</td>
                    <td>${job.location}</td>
                    <td>${Utils.formatDate(job.postDate)}</td>
                    <td>
                        <span class="badge ${job.active ? 'bg-success' : 'bg-danger'}">
                            ${job.active ? 'Active' : 'Inactive'}
                        </span>
                    </td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <a href="/job-detail.html?id=${job.id}" class="btn btn-outline-primary">
                                <i class="fas fa-eye"></i>
                            </a>
                            <button class="btn btn-outline-danger" onclick="deleteJob('${job.id}')">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });

        jobsTableBody.innerHTML = html;
    } catch (error) {
        jobsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Failed to load jobs</td></tr>';
        console.error('Failed to load jobs:', error);
    }
}

// Load recruiters data
async function loadRecruiters() {
    const recruitersTableBody = document.getElementById('recruiters-table-body');

    try {
        // Show loading state
        recruitersTableBody.innerHTML = '<tr><td colspan="5" class="text-center">Loading recruiters...</td></tr>';

        const response = await ApiClient.get('/recruiters?page=0&size=100'); // Get all recruiters
        const recruiters = response.content || [];

        if (recruiters.length === 0) {
            recruitersTableBody.innerHTML = '<tr><td colspan="5" class="text-center">No recruiters found</td></tr>';
            return;
        }

        // Generate table rows
        let html = '';
        recruiters.forEach(recruiter => {
            html += `
                <tr>
                    <td>${recruiter.name}</td>
                    <td>${recruiter.email}</td>
                    <td>${recruiter.company}</td>
                    <td>${recruiter.position || 'N/A'}</td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-primary" onclick="viewRecruiterDetails('${recruiter.id}')">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-outline-danger" onclick="deleteRecruiter('${recruiter.id}')">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });

        recruitersTableBody.innerHTML = html;
    } catch (error) {
        recruitersTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Failed to load recruiters</td></tr>';
        console.error('Failed to load recruiters:', error);
    }
}

// Load applications data
async function loadApplications() {
    const applicationsTableBody = document.getElementById('applications-table-body');

    try {
        // Show loading state
        applicationsTableBody.innerHTML = '<tr><td colspan="6" class="text-center">Loading applications...</td></tr>';

        const response = await ApiClient.get('/applications?page=0&size=100'); // Get all applications
        const applications = response.content || [];

        if (applications.length === 0) {
            applicationsTableBody.innerHTML = '<tr><td colspan="6" class="text-center">No applications found</td></tr>';
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

        // Generate table rows
        let html = '';
        applications.forEach(app => {
            const job = jobsMap[app.jobId] || { title: 'Unknown', company: 'Unknown' };
            const statusClass = getStatusBadgeClass(app.status);

            html += `
                <tr>
                    <td>${app.candidateName}</td>
                    <td>${job.title}</td>
                    <td>${job.company}</td>
                    <td>${Utils.formatDate(app.applicationDate)}</td>
                    <td><span class="badge ${statusClass}">${app.status}</span></td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-primary" onclick="viewApplicationDetails('${app.id}')">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-outline-danger" onclick="deleteApplication('${app.id}')">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });

        applicationsTableBody.innerHTML = html;
    } catch (error) {
        applicationsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Failed to load applications</td></tr>';
        console.error('Failed to load applications:', error);
    }
}

// Helper function to get badge class for application status
function getStatusBadgeClass(status) {
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

// Toggle user status (enabled/disabled)
async function toggleUserStatus(userId, enabled) {
    const checkbox = document.getElementById(`user-status-${userId}`);
    const modal = new bootstrap.Modal(document.getElementById('toggleStatusModal'));

    // Set modal content
    document.getElementById('toggleStatusModalTitle').textContent = `${enabled ? 'Enable' : 'Disable'} User`;
    document.getElementById('toggleStatusModalBody').textContent =
        `Are you sure you want to ${enabled ? 'enable' : 'disable'} this user?`;

    // Set up confirmation button
    const confirmBtn = document.getElementById('confirmToggleStatus');
    confirmBtn.onclick = async () => {
        try {
            await ApiClient.put(`/admin/users/${userId}/status`, { enabled });
            modal.hide();
            // Reload users to get updated data
            loadUsers();
        } catch (error) {
            console.error('Failed to toggle user status:', error);
            // Revert checkbox state
            checkbox.checked = !enabled;
            modal.hide();
            alert('Failed to update user status. Please try again.');
        }
    };

    // Show modal
    modal.show();
}

// View user details
function viewUserDetails(userId) {
    // For now, just show an alert
    alert(`View details for user ID: ${userId}`);
    // In a real implementation, you might open a modal with user details
}

// View recruiter details
function viewRecruiterDetails(recruiterId) {
    // For now, just show an alert
    alert(`View details for recruiter ID: ${recruiterId}`);
    // In a real implementation, you might open a modal with recruiter details
}

// View application details
function viewApplicationDetails(applicationId) {
    // For now, just show an alert
    alert(`View details for application ID: ${applicationId}`);
    // In a real implementation, you might open a modal with application details
}

// Delete job
async function deleteJob(jobId) {
    if (confirm('Are you sure you want to delete this job? This action cannot be undone.')) {
        try {
            await ApiClient.delete(`/jobs/${jobId}`);
            loadJobs(); // Reload the jobs list
            loadDashboardStats(); // Update stats
        } catch (error) {
            console.error('Failed to delete job:', error);
            alert('Failed to delete job. Please try again.');
        }
    }
}

// Delete recruiter
async function deleteRecruiter(recruiterId) {
    if (confirm('Are you sure you want to delete this recruiter? This action cannot be undone.')) {
        try {
            await ApiClient.delete(`/recruiters/${recruiterId}`);
            loadRecruiters(); // Reload the recruiters list
            loadDashboardStats(); // Update stats
        } catch (error) {
            console.error('Failed to delete recruiter:', error);
            alert('Failed to delete recruiter. Please try again.');
        }
    }
}

// Delete application
async function deleteApplication(applicationId) {
    if (confirm('Are you sure you want to delete this application? This action cannot be undone.')) {
        try {
            await ApiClient.delete(`/applications/${applicationId}`);
            loadApplications(); // Reload the applications list
            loadDashboardStats(); // Update stats
        } catch (error) {
            console.error('Failed to delete application:', error);
            alert('Failed to delete application. Please try again.');
        }
    }
}