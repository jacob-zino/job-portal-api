// src/main/resources/static/js/applications.js

document.addEventListener('DOMContentLoaded', () => {
    // Check if user is authenticated
    const auth = AuthService.initAuth();
    if (!auth.isAuthenticated()) {
        window.location.href = '/login.html?returnUrl=' + encodeURIComponent(window.location.href);
        return;
    }

    // Load user's applications
    loadMyApplications();
});

async function loadMyApplications() {
    const container = document.getElementById('applications-container');

    try {
        // Show loading state
        if (container) {
            container.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            `;
        }

        // Get current user's email
        const user = AuthService.getUser();

        if (!user || !user.email) {
            throw new Error('User information not available');
        }

        // Load applications by email
        const response = await ApiClient.get(`/applications/email/${user.email}`);
        const applications = response.content || response || [];

        if (applications.length === 0) {
            container.innerHTML = `
                <div class="alert alert-info">
                    <h4 class="alert-heading">No Applications Found</h4>
                    <p>You haven't applied to any jobs yet.</p>
                    <hr>
                    <p class="mb-0">
                        <a href="/jobs.html" class="btn btn-primary">Browse Jobs</a>
                    </p>
                </div>
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

        // Generate applications list
        let html = '<div class="row">';

        applications.forEach(app => {
            const job = jobsMap[app.jobId] || { title: 'Unknown', company: 'Unknown' };
            const statusClass = getStatusClass(app.status);
            const statusText = app.status || 'SUBMITTED';

            html += `
                <div class="col-md-6 col-lg-4 mb-4">
                    <div class="card h-100">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h5 class="mb-0">${job.title}</h5>
                            <span class="badge ${statusClass}">${statusText}</span>
                        </div>
                        <div class="card-body">
                            <h6 class="card-subtitle mb-2 text-muted">${job.company}</h6>
                            <p class="card-text"><i class="fas fa-map-marker-alt"></i> ${job.location || 'N/A'}</p>
                            <p class="card-text"><i class="fas fa-calendar"></i> Applied: ${Utils.formatDate(app.applicationDate)}</p>
                        </div>
                        <div class="card-footer bg-transparent">
                            <div class="d-grid gap-2">
                                <button class="btn btn-outline-primary btn-sm" onclick="viewApplicationDetails('${app.id}')">
                                    <i class="fas fa-eye"></i> View Details
                                </button>
                                <a href="/job-detail.html?id=${app.jobId}" class="btn btn-outline-secondary btn-sm">
                                    <i class="fas fa-briefcase"></i> View Job
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
        console.error('Failed to load applications:', error);

        if (container) {
            container.innerHTML = `
                <div class="alert alert-danger">
                    <h4 class="alert-heading">Error Loading Applications</h4>
                    <p>We couldn't load your applications. Please try again later.</p>
                    <p>${error.message || 'Unknown error'}</p>
                </div>
            `;
        }
    }
}

// View application details
async function viewApplicationDetails(applicationId) {
    const detailsContainer = document.getElementById('application-details-container');

    try {
        // Show loading state
        detailsContainer.innerHTML = `
            <div class="loading-spinner">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        `;

        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('applicationModal'));
        modal.show();

        // Load application details
        const application = await ApiClient.get(`/applications/${applicationId}`);

        // Load job details
        let job;
        try {
            job = await ApiClient.get(`/jobs/${application.jobId}`);
        } catch (err) {
            console.error(`Failed to load job ${application.jobId}:`, err);
            job = { title: 'Unknown', company: 'Unknown' };
        }

        // Generate application details HTML
        let html = `
            <div class="card mb-3">
                <div class="card-header">
                    <h5 class="mb-0">Job Information</h5>
                </div>
                <div class="card-body">
                    <h5>${job.title}</h5>
                    <p class="mb-1"><strong>Company:</strong> ${job.company}</p>
                    <p class="mb-1"><strong>Location:</strong> ${job.location || 'N/A'}</p>
                    <p class="mb-0"><strong>Employment Type:</strong> ${job.employmentType || 'N/A'}</p>
                </div>
            </div>
            
            <div class="card mb-3">
                <div class="card-header">
                    <h5 class="mb-0">Application Status</h5>
                </div>
                <div class="card-body">
                    <div class="d-flex align-items-center">
                        <span class="badge ${getStatusClass(application.status)} me-2">${application.status}</span>
                        <span>Applied on: ${Utils.formatDate(application.applicationDate)}</span>
                    </div>
                    <div class="mt-3">
                        <h6>Application Status Meaning:</h6>
                        <ul class="list-unstyled">
                            <li><span class="badge bg-info me-2">SUBMITTED</span> Your application has been received</li>
                            <li><span class="badge bg-warning text-dark me-2">REVIEWING</span> Your application is being reviewed</li>
                            <li><span class="badge bg-primary me-2">INTERVIEWED</span> You've been or will be interviewed</li>
                            <li><span class="badge bg-success me-2">ACCEPTED</span> Your application has been accepted</li>
                            <li><span class="badge bg-danger me-2">REJECTED</span> Your application was not selected</li>
                        </ul>
                    </div>
                </div>
            </div>
            
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0">Your Submission</h5>
                </div>
                <div class="card-body">
                    <p class="mb-1"><strong>Name:</strong> ${application.candidateName}</p>
                    <p class="mb-1"><strong>Email:</strong> ${application.email}</p>
                    <p class="mb-1"><strong>Phone:</strong> ${application.phone}</p>
                    
                    ${application.resumeUrl ? `
                        <p class="mb-1"><strong>Resume:</strong> <a href="${application.resumeUrl}" target="_blank">View Resume</a></p>
                    ` : ''}
                    
                    <hr>
                    
                    <h6>Cover Letter:</h6>
                    <div class="p-3 bg-light rounded">
                        ${application.coverLetterText || 'No cover letter provided.'}
                    </div>
                </div>
            </div>
        `;

        detailsContainer.innerHTML = html;
    } catch (error) {
        console.error('Failed to load application details:', error);

        detailsContainer.innerHTML = `
            <div class="alert alert-danger">
                <h4 class="alert-heading">Error Loading Details</h4>
                <p>We couldn't load the application details. Please try again later.</p>
                <p>${error.message || 'Unknown error'}</p>
            </div>
        `;
    }
}

// Get status badge class based on application status
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