// src/main/resources/static/js/my-jobs.js

document.addEventListener('DOMContentLoaded', () => {
    // Check if user is recruiter, if not redirect to home
    const auth = AuthService.initAuth();
    if (!auth.isAuthenticated() || (!auth.hasRole('RECRUITER') && !auth.hasRole('ADMIN'))) {
        window.location.href = '/';
        return;
    }

    // Initialize recruiter jobs page
    initMyJobsPage();
});

function initMyJobsPage() {
    // Load recruiter's jobs
    loadMyJobs();

    // Initialize job form
    initJobForm();

    // Initialize modals
    const jobFormModal = new bootstrap.Modal(document.getElementById('jobFormModal'));
    const applicationsModal = new bootstrap.Modal(document.getElementById('applicationsModal'));

    // Reset form when the modal is hidden
    document.getElementById('jobFormModal').addEventListener('hidden.bs.modal', () => {
        document.getElementById('job-form').reset();
        document.getElementById('job-id').value = '';
        document.getElementById('jobFormModalTitle').textContent = 'Post New Job';
    });
}

// Load recruiter's jobs
async function loadMyJobs() {
    const container = document.getElementById('my-jobs-container');

    try {
        // Show loading state
        container.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        `;

        // Get current user info
        const user = AuthService.getUser();
        console.log("Current user:", user);

        // Try to get recruiter info by email
        let recruiterId = null;
        try {
            // Use the new endpoint we're creating
            const response = await ApiClient.get(`/recruiters/byEmail?email=${encodeURIComponent(user.email)}`);

            if (response && response.id) {
                recruiterId = response.id;
                console.log("Found recruiter ID:", recruiterId);
            } else {
                console.warn("No recruiter found with email:", user.email);
            }
        } catch (err) {
            console.error("Error finding recruiter:", err);

            // Fallback to listing all recruiters and finding by email
            try {
                const recruiters = await ApiClient.get(`/recruiters`);
                console.log("Retrieved recruiters:", recruiters);

                // Find the recruiter that matches the user's email
                const recruiterArray = recruiters.content || recruiters || [];
                const recruiter = recruiterArray.find(r => r.email === user.email);

                if (recruiter) {
                    recruiterId = recruiter.id;
                    console.log("Found recruiter ID from list:", recruiterId);
                }
            } catch (listErr) {
                console.error("Error listing recruiters:", listErr);
            }
        }

        // Load jobs either by recruiter ID or all jobs for admin
        let jobs = [];
        const isAdmin = user.roles.includes('ADMIN');

        if (recruiterId) {
            console.log("Loading jobs for recruiter:", recruiterId);
            const response = await ApiClient.get(`/jobs/recruiter/${recruiterId}`);
            jobs = response.content || response || [];
        } else if (isAdmin) {
            // Admin can see all jobs
            console.log("Loading all jobs for admin");
            const response = await ApiClient.get('/jobs');
            jobs = response.content || response || [];
        } else {
            console.warn("No recruiter ID found and user is not admin.");
            container.innerHTML = `
                <div class="alert alert-warning">
                    <h4 class="alert-heading">Recruiter Profile Not Found</h4>
                    <p>We couldn't find your recruiter profile. Please contact an administrator.</p>
                </div>
            `;
            return;
        }

        console.log("Retrieved jobs:", jobs);

        if (jobs.length === 0) {
            container.innerHTML = `
                <div class="alert alert-info">
                    <p>You don't have any job listings yet.</p>
                    <p>Click "Post New Job" to create your first job listing.</p>
                </div>
            `;
            return;
        }

        // Create job cards
        let html = '<div class="row">';

        jobs.forEach(job => {
            html += `
                <div class="col-md-6 mb-4">
                    <div class="card h-100">
                        <div class="card-header">
                            <div class="d-flex justify-content-between align-items-center">
                                <h5 class="mb-0">${job.title}</h5>
                                <span class="badge ${job.active ? 'bg-success' : 'bg-danger'}">
                                    ${job.active ? 'Active' : 'Inactive'}
                                </span>
                            </div>
                        </div>
                        <div class="card-body">
                            <h6 class="card-subtitle mb-2 text-muted">${job.company}</h6>
                            <p><i class="fas fa-map-marker-alt"></i> ${job.location}</p>
                            <p><i class="fas fa-dollar-sign"></i> $${job.minSalary} - $${job.maxSalary}</p>
                            <p><i class="fas fa-briefcase"></i> ${job.employmentType}</p>
                            <p><i class="fas fa-calendar"></i> Posted: ${Utils.formatDate(job.postDate)}</p>
                            <p><i class="fas fa-clock"></i> Deadline: ${Utils.formatDate(job.deadlineDate)}</p>
                        </div>
                        <div class="card-footer bg-transparent border-0">
                            <div class="btn-group w-100">
                                <button class="btn btn-outline-primary" onclick="editJob('${job.id}')">
                                    <i class="fas fa-edit"></i> Edit
                                </button>
                                <button class="btn btn-outline-info" onclick="viewApplications('${job.id}')">
                                    <i class="fas fa-users"></i> Applications
                                </button>
                                <a href="/job-detail.html?id=${job.id}" class="btn btn-outline-secondary">
                                    <i class="fas fa-eye"></i> View
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });

        html += '</div>';
        container.innerHTML = html;
    } catch (error) {
        console.error('Failed to load jobs:', error);
        container.innerHTML = `
            <div class="alert alert-danger">
                <h4 class="alert-heading">Error Loading Jobs</h4>
                <p>Failed to load your job listings. Please try again later.</p>
                <p>Error: ${error.message || 'Unknown error'}</p>
                <hr>
                <p class="mb-0">If this problem persists, please contact support.</p>
            </div>
        `;
    }
}

// Initialize job form
function initJobForm() {
    const saveBtn = document.getElementById('save-job-btn');

    saveBtn.addEventListener('click', async () => {
        // Validate form
        const form = document.getElementById('job-form');
        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        // Get form values
        const jobId = document.getElementById('job-id').value;
        const job = {
            title: document.getElementById('job-title').value,
            company: document.getElementById('job-company').value,
            description: document.getElementById('job-description').value,
            location: document.getElementById('job-location').value,
            employmentType: document.getElementById('job-employment-type').value,
            minSalary: parseFloat(document.getElementById('job-min-salary').value),
            maxSalary: parseFloat(document.getElementById('job-max-salary').value),
            requiredSkills: document.getElementById('job-required-skills').value.split(',').map(skill => skill.trim()),
            deadlineDate: document.getElementById('job-deadline-date').value || null,
            active: document.getElementById('job-active').checked
        };

        try {
            saveBtn.disabled = true;
            saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...';

            if (jobId) {
                // Update existing job
                await ApiClient.put(`/jobs/${jobId}`, job);
            } else {
                // Create new job
                await ApiClient.post('/jobs', job);
            }

            // Hide modal and reload jobs
            const modal = bootstrap.Modal.getInstance(document.getElementById('jobFormModal'));
            modal.hide();
            loadMyJobs();
        } catch (error) {
            alert(`Failed to save job: ${error.message || 'Unknown error'}`);
            console.error('Failed to save job:', error);
        } finally {
            saveBtn.disabled = false;
            saveBtn.innerHTML = 'Save Job';
        }
    });
}

// Edit job
async function editJob(jobId) {
    try {
        // Get job details
        const job = await ApiClient.get(`/jobs/${jobId}`);

        // Fill form with job details
        document.getElementById('job-id').value = job.id;
        document.getElementById('job-title').value = job.title;
        document.getElementById('job-company').value = job.company;
        document.getElementById('job-description').value = job.description;
        document.getElementById('job-location').value = job.location;
        document.getElementById('job-employment-type').value = job.employmentType;
        document.getElementById('job-min-salary').value = job.minSalary;
        document.getElementById('job-max-salary').value = job.maxSalary;
        document.getElementById('job-required-skills').value = job.requiredSkills.join(', ');

        // Format the deadline date for the date input
        if (job.deadlineDate) {
            const date = new Date(job.deadlineDate);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            document.getElementById('job-deadline-date').value = `${year}-${month}-${day}`;
        } else {
            document.getElementById('job-deadline-date').value = '';
        }

        document.getElementById('job-active').checked = job.active;

        // Update modal title
        document.getElementById('jobFormModalTitle').textContent = 'Edit Job';

        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('jobFormModal'));
        modal.show();
    } catch (error) {
        alert(`Failed to load job details: ${error.message || 'Unknown error'}`);
        console.error('Failed to load job details:', error);
    }
}

// View applications for a job
async function viewApplications(jobId) {
    const container = document.getElementById('applications-container');

    try {
        // Show loading state
        container.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        `;

        // Get job details
        const job = await ApiClient.get(`/jobs/${jobId}`);

        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('applicationsModal'));
        modal.show();

        // Get applications for this job
        const response = await ApiClient.get(`/applications/job/${jobId}`);
        const applications = response.content || response || [];

        // Update modal title
        document.querySelector('#applicationsModal .modal-title').textContent =
            `Applications for: ${job.title}`;

        if (applications.length === 0) {
            container.innerHTML = `
                <div class="alert alert-info">
                    No applications received for this job yet.
                </div>
            `;
            return;
        }

        // Generate applications list
        let html = `
            <div class="list-group">
        `;

        applications.forEach(app => {
            const statusBadge = getStatusBadge(app.status);

            html += `
                <div class="list-group-item">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-1">${app.candidateName}</h5>
                        ${statusBadge}
                    </div>
                    <p class="mb-1"><strong>Email:</strong> ${app.email}</p>
                    <p class="mb-1"><strong>Phone:</strong> ${app.phone}</p>
                    <p class="mb-1"><strong>Applied:</strong> ${Utils.formatDate(app.applicationDate)}</p>
                    
                    <div class="d-flex mt-2">
                        <div class="btn-group">
                            <button class="btn btn-sm btn-outline-secondary" type="button" data-bs-toggle="collapse" data-bs-target="#coverLetter${app.id}">
                                Cover Letter
                            </button>
                            <div class="dropdown">
                                <button class="btn btn-sm btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                                    Update Status
                                </button>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="#" onclick="updateApplicationStatus('${app.id}', 'SUBMITTED')">Submitted</a></li>
                                    <li><a class="dropdown-item" href="#" onclick="updateApplicationStatus('${app.id}', 'REVIEWING')">Reviewing</a></li>
                                    <li><a class="dropdown-item" href="#" onclick="updateApplicationStatus('${app.id}', 'INTERVIEWED')">Interviewed</a></li>
                                    <li><a class="dropdown-item" href="#" onclick="updateApplicationStatus('${app.id}', 'ACCEPTED')">Accepted</a></li>
                                    <li><a class="dropdown-item" href="#" onclick="updateApplicationStatus('${app.id}', 'REJECTED')">Rejected</a></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                    
                    <div class="collapse mt-3" id="coverLetter${app.id}">
                        <div class="card card-body">
                            ${app.coverLetterText || 'No cover letter provided.'}
                        </div>
                    </div>
                </div>
            `;
        });

        html += `</div>`;
        container.innerHTML = html;
    } catch (error) {
        container.innerHTML = `
            <div class="alert alert-danger">
                Failed to load applications. Please try again later.
            </div>
        `;
        console.error('Failed to load applications:', error);
    }
}

// Get badge HTML for application status
function getStatusBadge(status) {
    let badgeClass = '';
    switch (status) {
        case 'SUBMITTED':
            badgeClass = 'bg-info';
            break;
        case 'REVIEWING':
            badgeClass = 'bg-warning text-dark';
            break;
        case 'INTERVIEWED':
            badgeClass = 'bg-primary';
            break;
        case 'ACCEPTED':
            badgeClass = 'bg-success';
            break;
        case 'REJECTED':
            badgeClass = 'bg-danger';
            break;
        default:
            badgeClass = 'bg-secondary';
    }

    return `<span class="badge ${badgeClass}">${status}</span>`;
}

// Update application status
async function updateApplicationStatus(applicationId, status) {
    try {
        // Get current application data
        const application = await ApiClient.get(`/applications/${applicationId}`);

        // Update status
        application.status = status;

        // Save changes
        await ApiClient.put(`/applications/${applicationId}`, application);

        // Refresh applications list
        await viewApplications(application.jobId);
    } catch (error) {
        alert(`Failed to update status: ${error.message || 'Unknown error'}`);
        console.error('Failed to update application status:', error);
    }
}