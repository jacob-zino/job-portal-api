// src/main/resources/static/js/apply-job.js

document.addEventListener('DOMContentLoaded', () => {
    // Check if user is authenticated
    const auth = AuthService.initAuth();
    if (!auth.isAuthenticated()) {
        // Redirect to login page with return URL
        window.location.href = `/login.html?returnUrl=${encodeURIComponent(window.location.href)}`;
        return;
    }

    // Check if user is a recruiter (recruiters can't apply to jobs)
    if (auth.hasRole('RECRUITER')) {
        window.location.href = '/jobs.html';
        return;
    }

    // Get job ID from URL
    const jobId = Utils.getUrlParam('id');
    if (!jobId) {
        window.location.href = '/jobs.html';
        return;
    }

    // Initialize application page
    initApplicationPage(jobId);
});

async function initApplicationPage(jobId) {
    try {
        // Load job details
        const job = await ApiClient.get(`/jobs/${jobId}`);

        // Update page title
        document.title = `Apply for ${job.title} - Job Portal`;

        // Display job details in header
        displayJobHeader(job);

        // Fill form with user data if available
        prefillFormWithUserData();

        // Initialize form submission
        initApplicationForm(jobId);
    } catch (error) {
        displayErrorState(error);
    }
}

// Display job header
function displayJobHeader(job) {
    const headerContainer = document.getElementById('job-header');

    if (!headerContainer) return;

    headerContainer.innerHTML = `
        <div class="card bg-light mb-4">
            <div class="card-body">
                <h2 class="card-title">${job.title}</h2>
                <div class="d-flex align-items-center mb-2">
                    <h5 class="job-company mb-0 me-3">${job.company}</h5>
                    <span class="text-muted"><i class="fas fa-map-marker-alt me-1"></i>${job.location}</span>
                </div>
                <div class="mb-3">
                    <span class="badge bg-primary me-2">${job.employmentType}</span>
                    <span class="badge bg-success">$${job.minSalary} - $${job.maxSalary}</span>
                </div>
                <div class="d-flex align-items-center text-muted small">
                    <div class="me-3">
                        <i class="fas fa-calendar-alt me-1"></i> Posted: ${Utils.formatDate(job.postDate)}
                    </div>
                    <div>
                        <i class="fas fa-clock me-1"></i> Apply by: ${Utils.formatDate(job.deadlineDate)}
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Prefill form with user data
function prefillFormWithUserData() {
    const user = AuthService.getUser();

    if (user) {
        // Fill name field with username as default
        const nameField = document.getElementById('candidate-name');
        if (nameField) nameField.value = user.username;

        // Fill email field
        const emailField = document.getElementById('candidate-email');
        if (emailField) emailField.value = user.email;
    }
}

// Initialize application form submission
function initApplicationForm(jobId) {
    const form = document.getElementById('application-form');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Get form values
        const candidateName = document.getElementById('candidate-name').value;
        const email = document.getElementById('candidate-email').value;
        const phone = document.getElementById('candidate-phone').value;
        const coverLetterText = document.getElementById('cover-letter').value;
        const resumeUrl = document.getElementById('resume-url').value;

        // Create application object
        const application = {
            jobId,
            candidateName,
            email,
            phone,
            coverLetterText,
            resumeUrl,
            status: 'SUBMITTED'
        };

        // Disable submit button to prevent multiple submissions
        const submitButton = form.querySelector('button[type="submit"]');
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Submitting...';

        try {
            console.log("Submitting application:", application);

            // Submit application
            const response = await ApiClient.post('/applications', application);
            console.log("Application submitted successfully:", response);

            // Show success message and redirect to applications page
            alert('Your application has been submitted successfully!');
            window.location.href = '/applications.html';
        } catch (error) {
            console.error('Application submission error:', error);
            alert(`Failed to submit application: ${error.message || 'Unknown error'}`);

            // Re-enable submit button
            submitButton.disabled = false;
            submitButton.textContent = 'Submit Application';
        }
    });
}

// Display error state
function displayErrorState(error) {
    const headerContainer = document.getElementById('job-header');
    const formContainer = document.querySelector('.card');

    if (headerContainer) {
        headerContainer.innerHTML = `
            <div class="alert alert-danger">
                <h4 class="alert-heading">Error Loading Job</h4>
                <p>We couldn't load the job details. The job may no longer be available.</p>
                <p>${error.message || 'Unknown error'}</p>
                <hr>
                <p class="mb-0">
                    <a href="/jobs.html" class="btn btn-outline-danger">Browse Other Jobs</a>
                </p>
            </div>
        `;
    }

    if (formContainer) {
        formContainer.style.display = 'none';
    }
}