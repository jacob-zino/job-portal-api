// src/main/resources/static/js/auth.js

const API_URL = '/api'; // This will use the relative path to your Spring Boot API

// Auth service for handling authentication
const AuthService = {
    // Store JWT token in localStorage
    setToken: (token) => {
        localStorage.setItem('token', token);
    },

    // Get the stored token
    getToken: () => {
        return localStorage.getItem('token');
    },

    // Store user info
    setUser: (user) => {
        localStorage.setItem('user', JSON.stringify(user));
    },

    // Get stored user info
    getUser: () => {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    },

    // Check if user is authenticated
    isAuthenticated: () => {
        return !!AuthService.getToken();
    },

    // Check if user has a specific role
    hasRole: (role) => {
        const user = AuthService.getUser();
        return user && user.roles && user.roles.includes(role);
    },

    // Check if token is expired
    isTokenExpired: () => {
        const token = AuthService.getToken();
        if (!token) return true;

        try {
            // Parse the JWT token (simplified - in production, consider using jwt-decode library)
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            const { exp } = JSON.parse(jsonPayload);
            return Date.now() >= exp * 1000;
        } catch (e) {
            return true;
        }
    },

    // Clear auth data
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/';
    },

    // Login user
    login: async (username, password) => {
        try {
            const response = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Login failed');
            }

            const data = await response.json();
            AuthService.setToken(data.token);
            AuthService.setUser({
                id: data.id,
                username: data.username,
                email: data.email,
                roles: data.roles
            });
            return data;
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    },

    // Register user
    // Register user
    register: async (userData) => {
        try {
            console.log("Sending registration data:", userData);

            const response = await fetch(`${API_URL}/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Registration failed');
            }

            return await response.json();
        } catch (error) {
            console.error('Registration error:', error);
            throw error;
        }
    },

    // Initialize authentication elements on the page
    initAuth: () => {
        // Handle logout click
        const logoutButton = document.getElementById('logout');
        if (logoutButton) {
            logoutButton.addEventListener('click', (e) => {
                e.preventDefault();
                AuthService.logout();
            });
        }

        // Set username if authenticated
        const usernameElement = document.getElementById('username');
        if (usernameElement) {
            const user = AuthService.getUser();
            if (user) {
                usernameElement.innerText = user.username;
            }
        }

        // Show/hide elements based on authentication status
        const updateAuthDisplay = () => {
            const isAuth = AuthService.isAuthenticated();
            const isAdmin = AuthService.hasRole('ADMIN');
            const isRecruiter = AuthService.hasRole('RECRUITER') || isAdmin;
            const isUser = AuthService.hasRole('USER') || !isRecruiter;

            // Elements that should be shown when authenticated
            document.querySelectorAll('.authenticated').forEach(el => {
                el.style.display = isAuth ? 'block' : 'none';
            });

            // Elements that should be hidden when authenticated
            document.querySelectorAll('.unauthenticated').forEach(el => {
                el.style.display = isAuth ? 'none' : 'block';
            });

            // Role-specific elements
            document.querySelectorAll('.admin-only').forEach(el => {
                el.style.display = isAdmin ? 'block' : 'none';
            });

            document.querySelectorAll('.recruiter-only').forEach(el => {
                el.style.display = isRecruiter ? 'block' : 'none';
            });

            document.querySelectorAll('.user-only').forEach(el => {
                el.style.display = isUser ? 'block' : 'none';
            });
        };

        // Update display based on current auth status
        updateAuthDisplay();

        // Return function to check if user is authenticated and has specific role
        return {
            isAuthenticated: AuthService.isAuthenticated,
            hasRole: AuthService.hasRole
        };
    }
};

// HTTP client with JWT authentication
const ApiClient = {
    // Base fetch method with auth token
    fetchWithAuth: async (url, options = {}) => {
        // Refresh token if expired
        if (AuthService.isTokenExpired()) {
            AuthService.logout();
            window.location.href = '/login.html';
            return;
        }

        // Add auth header if token exists
        const token = AuthService.getToken();
        const headers = options.headers || {};

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(options.body);
        }

        const config = {
            ...options,
            headers
        };

        console.log(`API Request: ${API_URL}${url}`, config);

        try {
            const response = await fetch(`${API_URL}${url}`, config);
            console.log(`API Response status: ${response.status} for ${url}`);

            // Handle unauthorized error
            if (response.status === 401) {
                console.error('Unauthorized access - logging out');
                AuthService.logout();
                window.location.href = '/login.html';
                return;
            }

            // Handle other errors
            if (!response.ok) {
                const errorData = await response.json();
                console.error(`API error (${response.status}):`, errorData);
                throw new Error(errorData.message || 'Request failed');
            }

            // Return parsed response
            const data = await response.json();
            return data;
        } catch (error) {
            console.error(`API error for ${url}:`, error);
            throw error;
        }
    },

    // GET request
    get: (url) => ApiClient.fetchWithAuth(url),

    // POST request
    post: (url, data) => ApiClient.fetchWithAuth(url, {
        method: 'POST',
        body: data
    }),

    // PUT request
    put: (url, data) => ApiClient.fetchWithAuth(url, {
        method: 'PUT',
        body: data
    }),

    // DELETE request
    delete: (url) => ApiClient.fetchWithAuth(url, {
        method: 'DELETE'
    })
};

// Initialize auth when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    AuthService.initAuth();
});