// src/main/resources/static/js/main.js

// General utility functions
const Utils = {
    // Format date to a readable format
    formatDate: (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString();
    },

    // Show loading spinner
    showLoading: (containerId) => {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            `;
        }
    },

    // Show error message
    showError: (containerId, message) => {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="alert alert-danger" role="alert">
                    ${message}
                </div>
            `;
        }
    },

    // Show success message
    showSuccess: (containerId, message) => {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="alert alert-success" role="alert">
                    ${message}
                </div>
            `;
        }
    },

    // Get URL parameter
    getUrlParam: (param) => {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(param);
    }
};

// Page-specific initialization
document.addEventListener('DOMContentLoaded', () => {
    // Detect which page we're on based on URL
    const currentPath = window.location.pathname;

    // Initialize specific page functionality
    if (currentPath === '/' || currentPath === '/index.html') {
        // Home page
        console.log('Home page loaded');
    }
    else if (currentPath.includes('/jobs.html')) {
        // Jobs listing page
        initJobsPage();
    }
    else if (currentPath.includes('/login.html')) {
        // Login page
        initLoginPage();
    }
    else if (currentPath.includes('/register.html')) {
        // Register page
        initRegisterPage();
    }
});

// Jobs page functionality
function initJobsPage() {
    const jobsContainer = document.getElementById('jobs-container');
    const searchInput = document.getElementById('search-jobs');
    const searchButton = document.getElementById('search-button');
    const searchTypeSelect = document.getElementById('search-type');
    const paginationContainer = document.getElementById('pagination-container');
    let currentPage = 0;
    let totalPages = 0;
    let searchTerm = '';
    let searchType = 'title'; // Default search type

    // Load jobs with optional search parameters
    async function loadJobs(page = 0, search = '', type = 'title') {
        try {
            Utils.showLoading('jobs-container');

            let url = `/jobs/paged?page=${page}&size=6`;

            // Different search endpoints based on search type
            if (search) {
                if (type === 'location') {
                    url = `/jobs/location/${encodeURIComponent(search)}/paged?page=${page}&size=6`;
                } else if (type === 'company') {
                    url = `/jobs/company/${encodeURIComponent(search)}/paged?page=${page}&size=6`;
                } else if (type === 'keyword') {
                    url = `/jobs/search?keyword=${encodeURIComponent(search)}&page=${page}&size=6`;
                } else {
                    // Default to title search
                    url = `/jobs/search/paged?title=${encodeURIComponent(search)}&page=${page}&size=6`;
                }
            }

            console.log("Searching with URL:", url); // Debug info

            const response = await ApiClient.get(url);

            if (response && response.content) {
                displayJobs(response.content);
                currentPage = response.number;
                totalPages = response.totalPages;
                updatePagination();
            } else {
                throw new Error('Failed to load jobs');
            }
        } catch (error) {
            Utils.showError('jobs-container', 'Failed to load jobs. Please try again later.');
            console.error("Error loading jobs:", error);
        }
    }

    // Display jobs in the container
    function displayJobs(jobs) {
        if (!jobsContainer) return;

        if (jobs.length === 0) {
            jobsContainer.innerHTML = `
                <div class="alert alert-info" role="alert">
                    No jobs found. Try a different search term or category.
                </div>
            `;
            return;
        }

        let html = '<div class="row">';

        jobs.forEach(job => {
            html += `
                <div class="col-md-6 col-lg-4 mb-4">
                    <div class="card job-card h-100">
                        <div class="card-body">
                            <h5 class="card-title">${job.title}</h5>
                            <h6 class="card-subtitle mb-2 text-muted">${job.company}</h6>
                            <p class="card-text mb-1">
                                <i class="fas fa-map-marker-alt"></i> ${job.location}
                            </p>
                            <p class="card-text mb-1">
                                <i class="fas fa-dollar-sign"></i> $${job.minSalary} - $${job.maxSalary}
                            </p>
                            <p class="card-text mb-1">
                                <i class="fas fa-calendar"></i> Posted: ${Utils.formatDate(job.postDate)}
                            </p>
                            <div class="mt-3">
                                <span class="badge bg-secondary">${job.employmentType}</span>
                            </div>
                        </div>
                        <div class="card-footer bg-transparent border-0">
                            <a href="/job-detail.html?id=${job.id}" class="btn btn-primary btn-sm">View Details</a>
                        </div>
                    </div>
                </div>
            `;
        });

        html += '</div>';
        jobsContainer.innerHTML = html;
    }

    // Update pagination controls
    function updatePagination() {
        if (!paginationContainer) return;

        if (totalPages <= 1) {
            paginationContainer.innerHTML = '';
            return;
        }

        let html = '<ul class="pagination justify-content-center">';

        // Previous button
        html += `
            <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${currentPage - 1}" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>
        `;

        // Page numbers
        for (let i = 0; i < totalPages; i++) {
            html += `
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
                </li>
            `;
        }

        // Next button
        html += `
            <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
                <a class="page-link" href="#" data-page="${currentPage + 1}" aria-label="Next">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
        `;

        html += '</ul>';
        paginationContainer.innerHTML = html;

        // Add event listeners to pagination links
        document.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(e.target.getAttribute('data-page') || e.target.parentElement.getAttribute('data-page'));
                if (!isNaN(page)) {
                    loadJobs(page, searchTerm, searchType);
                }
            });
        });
    }

    // Initialize search functionality
    if (searchTypeSelect) {
        searchTypeSelect.addEventListener('change', (e) => {
            searchType = e.target.value;
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                searchTerm = e.target.value;
                loadJobs(0, searchTerm, searchType);
            }
        });
    }

    if (searchButton) {
        searchButton.addEventListener('click', () => {
            searchTerm = searchInput.value;
            loadJobs(0, searchTerm, searchType);
        });
    }

    // Load initial jobs
    loadJobs();
}

// Login page functionality
function initLoginPage() {
    const loginForm = document.getElementById('login-form');
    const errorContainer = document.getElementById('error-container');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const successMessage = document.querySelector('.success-message');
                if (successMessage) {
                    successMessage.style.display = 'none';
                }

                if (errorContainer) {
                    errorContainer.innerHTML = '';
                }

                await AuthService.login(username, password);
                window.location.href = '/';
            } catch (error) {
                if (errorContainer) {
                    errorContainer.innerHTML = `
                        <div class="alert alert-danger" role="alert">
                            ${error.message || 'Login failed. Please check your credentials.'}
                        </div>
                    `;
                }
            }
        });
    }

    // Check for success message from registration
    const message = Utils.getUrlParam('message');
    if (message) {
        const successContainer = document.getElementById('success-container');
        if (successContainer) {
            successContainer.innerHTML = `
                <div class="alert alert-success" role="alert">
                    ${decodeURIComponent(message)}
                </div>
            `;
        }
    }
}

// Register page functionality
function initRegisterPage() {
    const registerForm = document.getElementById('register-form');
    const errorContainer = document.getElementById('error-container');

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            const recruiterCheckbox = document.getElementById('is-recruiter');

            // Get recruiter fields
            const company = document.getElementById('company')?.value || '';
            const phone = document.getElementById('phone')?.value || '';
            const position = ''; // You can add a position field in the form if needed

            try {
                if (errorContainer) {
                    errorContainer.innerHTML = '';
                }

                // Validate form
                if (password !== confirmPassword) {
                    throw new Error('Passwords do not match.');
                }

                // Determine role
                const role = recruiterCheckbox && recruiterCheckbox.checked ? 'RECRUITER' : 'USER';

                // Create request data
                const userData = {
                    username,
                    email,
                    password,
                    role
                };

                // Add recruiter fields if registering as recruiter
                if (role === 'RECRUITER') {
                    userData.company = company;
                    userData.phone = phone;
                    userData.position = position;

                    // Validate company field
                    if (!company) {
                        throw new Error('Company name is required for recruiter registration.');
                    }
                }

                console.log("Registering user with data:", userData);

                // Register user
                await AuthService.register(userData);

                // Redirect to login with success message
                window.location.href = `/login.html?message=${encodeURIComponent('Registration successful! Please log in.')}`;
            } catch (error) {
                if (errorContainer) {
                    errorContainer.innerHTML = `
                        <div class="alert alert-danger" role="alert">
                            ${error.message || 'Registration failed. Please try again.'}
                        </div>
                    `;
                }
            }
        });
    }
}