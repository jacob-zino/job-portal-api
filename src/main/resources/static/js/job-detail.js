// src/main/resources/static/js/job-detail.js

document.addEventListener('DOMContentLoaded', () => {
    // Get job ID from URL
    const jobId = Utils.getUrlParam('id');

    if (!jobId) {
        window.location.href = '/jobs.html';
        return;
    }

    loadJobDetails(jobId);
});

// Load job details
async function loadJobDetails(jobId) {
    const container = document.getElementById('job-detail-container');

    try {
        Utils.showLoading('job-detail-container');

        // Fetch job details
        const job = await ApiClient.get(`/jobs/${jobId}`);

        if (!job) {
            throw new Error('Job not found');
        }

        displayJobDetails(job);
        document.title = `${job.title} - Job Portal`;
    } catch (error) {
        if (container) {
            container.innerHTML = `
                <div class="alert alert-danger" role="alert">
                    Failed to load job details. Please try again later.
                </div>
                <div class="text-center">
                    <a href="/jobs.html" class="btn btn-primary">Back to Jobs</a>
                </div>
            `;
        }
        console.error(error);
    }
}

// Display job details
function displayJobDetails(job) {
    const container = document.getElementById('job-detail-container');
    const auth = AuthService.initAuth();

    if (!container) return;

    // Format dates
    const postDate = Utils.formatDate(job.postDate);
    const deadlineDate = Utils.formatDate(job.deadlineDate);

    // Format skills list
    const skillsHtml = job.requiredSkills && job.requiredSkills.length > 0
        ? job.requiredSkills.map(skill => `<span class="badge bg-light text-dark me-2">${skill}</span>`).join('')
        : '<span class="text-muted">No specific skills listed</span>';

    // Create the job detail HTML
    let html = `
        <div class="card">
            <div class="card-body">
                <div class="job-header">
                    <h1 class="card-title">${job.title}</h1>
                    <div class="d-flex align-items-center mb-2">
                        <h5 class="job-company mb-0 me-3">${job.company}</h5>
                        <span class="text-muted"><i class="fas fa-map-marker-alt me-1"></i>${job.location}</span>
                    </div>
                    <div class="mb-3">
                        <span class="badge bg-primary me-2">${job.employmentType}</span>
                        <span class="badge bg-success">$${job.minSalary} - $${job.maxSalary}</span>
                    </div>
                </div>
                
                <div class="job-details-section">
                    <h4>Job Description</h4>
                    <p>${job.description}</p>
                </div>
                
                <div class="job-details-section">
                    <h4>Required Skills</h4>
                    <div class="skills-container mb-3">
                        ${skillsHtml}
                    </div>
                </div>
                
                <div class="job-details-section">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Posted:</strong> ${postDate}</p>
                            <p><strong>Application Deadline:</strong> ${deadlineDate}</p>
                        </div>
                        <div class="col-md-6 text-md-end">
                            <div class="d-flex justify-content-md-end">
    `;

    // Add appropriate action buttons based on user role
    if (auth.isAuthenticated()) {
        if (!auth.hasRole('RECRUITER') && !auth.hasRole('ADMIN')) {
            // Regular user - show apply button
            html += `<a href="/apply-job.html?id=${job.id}" class="btn btn-primary">Apply Now</a>`;
        } else {
            // Recruiter or admin - show edit/manage buttons
            html += `
                <a href="/edit-job.html?id=${job.id}" class="btn btn-outline-primary me-2">Edit Job</a>
                <a href="/job-applications.html?jobId=${job.id}" class="btn btn-outline-secondary">View Applications</a>
            `;
        }
    } else {
        // Not authenticated - prompt to login
        html += `
            <a href="/login.html" class="btn btn-primary">Login to Apply</a>
        `;
    }

    html += `
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="text-center mt-4">
            <a href="/jobs.html" class="btn btn-outline-secondary">Back to Jobs</a>
        </div>
    `;

    container.innerHTML = html;
}