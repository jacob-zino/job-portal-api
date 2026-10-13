package org.example.oopproject1.cli;

import org.example.oopproject1.dto.CurrentUserDto;
import org.example.oopproject1.dto.MessageResponse;
import org.example.oopproject1.model.Application;
import org.example.oopproject1.model.Job;
import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

/**
 * ConsoleMenuRunner now acts as an HTTP client against your REST API.
 */
@Component
@Profile("console")
@ConditionalOnProperty(name = "org.example.oopproject1.cli.enabled", havingValue = "true", matchIfMissing = true)
public class ConsoleMenuRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ApplicationContext applicationContext;
    private final Scanner scanner = new Scanner(System.in);
    private String jwtToken;
    private User currentUser;

    public ConsoleMenuRunner(
            RestTemplate restTemplate,
            @Value("${cli.base-url:http://localhost:8080/api}") String baseUrl,
            ApplicationContext applicationContext
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.applicationContext = applicationContext;

        // ←– STEP 2: register interceptor here
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            if (this.jwtToken != null) {
                request.getHeaders().setBearerAuth(this.jwtToken);
            }
            return execution.execute(request, body);
        });
    }

    /**
     * The entry point for running the Job Portal Console Interface.
     * This method is called when the application starts.
     * It shows the main menu to the user.
     *
     * @param args command line arguments
     */
    @Override
    public void run(String... args) {
        System.out.println("Starting Job Portal Console Interface (HTTP client mode)...");
        showMainMenu();
    }

    /**
     * Displays the main menu with options for login, registration, and guest browsing.
     * It processes the user's choice and performs the corresponding action by calling your REST API.
     */
    private void showMainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n===== Job Portal System =====");
            System.out.println("1. Login");
            System.out.println("2. Register New User");
            System.out.println("3. Browse Jobs (Guest)");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            switch (scanner.nextLine()) {
                case "1":
                    login();
                    break;
                case "2":
                    registerUser();      // you’ll re-implement this to POST /api/auth/register
                    break;
                case "3":
                    browseJobsAsGuest(); // you’ll re-implement this to GET /api/jobs
                    break;
                case "4":
                    running = false;
                    System.out.println("Thank you for using Job Portal System. Goodbye!");
                    exit();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Prompts for credentials, calls POST /api/auth/login,
     * stores the returned JWT, fetches the current user profile,
     * and then moves into the appropriate role menu.
     */
    private void login() {
        System.out.println("\n===== Login =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // build login payload
        Map<String, String> creds = Map.of("username", username, "password", password);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(creds);

        try {
            // 1) POST to /auth/login
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    baseUrl + "/auth/login", request, Map.class
            );

            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                // 2) extract the token
                jwtToken = (String) resp.getBody().get("token");
                System.out.println("Login successful.");

                // 3) fetch only the slim DTO
                CurrentUserDto dto = restTemplate.getForObject(
                        baseUrl + "/users/me", CurrentUserDto.class
                );

                // 4) map DTO → your console User model
                currentUser = new User();
                currentUser.setId(dto.getId());
                currentUser.setUsername(dto.getUsername());
                currentUser.setEmail(dto.getEmail());
                currentUser.setRoles(dto.getRoles());

                // 5) branch on role
                if (hasRole(currentUser, "ADMIN")) {
                    showAdminMenu();
                } else if (hasRole(currentUser, "RECRUITER")) {
                    showRecruiterMenu();
                } else {
                    showClientMenu();
                }
            } else {
                System.out.println("Login failed: " + resp.getStatusCode());
            }
        } catch (RestClientException e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }


    /**
     * Checks if the user has the specified role.
     *
     * @param user The user to check.
     * @param role The role to check for.
     * @return true if the user has the specified role, otherwise false.
     */
    private boolean hasRole(User user, String role) {
        return user.getRoles().contains(role);
    }

    /**
     * Registers a new user with the specified details.
     * This includes username, email, and password.
     */
    private void registerUser() {
        System.out.println("\n===== User Registration =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        var payload = new HashMap<String,String>();
        payload.put("username", username);
        payload.put("email",    email);
        payload.put("password", password);

        try {
            ResponseEntity<Void> resp = restTemplate.postForEntity(
                    baseUrl + "/auth/register",
                    payload,
                    Void.class
            );
            if (resp.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("Registration successful! Please log in.");
            } else {
                System.out.println("Registration returned: " + resp.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            System.out.println("Registration failed: " + ex.getResponseBodyAsString());
        }
    }


    /**
     * Updates the current user's profile with new information.
     * Users can update their email, password, and recruiter-specific information.
     */
    private void updateProfile() {
        System.out.println("\n===== Update Profile =====");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Email:    " + currentUser.getEmail());

        // --- read new email/password ---
        System.out.print("\nNew Email (leave blank to keep): ");
        String newEmail    = scanner.nextLine().trim();
        System.out.print("New Password (leave blank to keep): ");
        String newPassword = scanner.nextLine().trim();

        // capture the old email so we can still find the recruiter doc
        String oldEmail = currentUser.getEmail();

        // --- update USER collection ---
        if (!newEmail.isEmpty() || !newPassword.isEmpty()) {
            try {
                Map<String, Object> updates = new HashMap<>();
                if (!newEmail.isEmpty())    updates.put("email",    newEmail);
                if (!newPassword.isEmpty()) updates.put("password", newPassword);

                restTemplate.put(
                        baseUrl + "/users/" + currentUser.getId(),
                        updates
                );
                if (!newEmail.isEmpty()) currentUser.setEmail(newEmail);
                System.out.println("User profile updated successfully!");
            } catch (HttpClientErrorException ex) {
                System.out.println("Error updating user profile: " + ex.getResponseBodyAsString());
            }
        }

        // --- update RECRUITER collection, if recruiter ---
        if (hasRole(currentUser, "RECRUITER")) {
            System.out.println("\n===== Update Recruiter Info =====");
            System.out.print("New Company  (leave blank to keep): ");
            String company  = scanner.nextLine().trim();
            System.out.print("New Position (leave blank to keep): ");
            String position = scanner.nextLine().trim();
            System.out.print("New Phone    (leave blank to keep): ");
            String phone    = scanner.nextLine().trim();

            try {
                // 1) fetch the recruiter using the old email
                Recruiter rec = restTemplate.getForObject(
                        baseUrl + "/recruiters/byEmail?email={email}",
                        Recruiter.class,
                        oldEmail
                );

                // 2) apply any field changes
                if (!company.isEmpty())  rec.setCompany(company);
                if (!position.isEmpty()) rec.setPosition(position);
                if (!phone.isEmpty())    rec.setPhone(phone);

                // 3) if we changed the email for the user, update it on the recruiter too
                if (!newEmail.isEmpty()) {
                    rec.setEmail(newEmail);
                }

                // 4) PUT the **entire** Recruiter back
                restTemplate.put(
                        baseUrl + "/recruiters/" + rec.getId(),
                        rec
                );
                System.out.println("Recruiter profile updated successfully!");
            } catch (HttpClientErrorException ex) {
                System.out.println("Error updating recruiter profile: " + ex.getResponseBodyAsString());
            }
        }

        System.out.println("\nPress Enter to continue…");
        scanner.nextLine();
    }

    /**
     * Lists all active job postings available to unauthenticated (guest) users.
     */
    private void browseJobsAsGuest() {
        boolean browsing = true;
        while (browsing) {
            System.out.println("\n===== Job Browser (Guest) =====");
            System.out.println("1. View All Jobs");
            System.out.println("2. Search Jobs by Keyword");
            System.out.println("3. Filter Jobs by Location");
            System.out.println("4. Filter Jobs by Company");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayAllJobs();
                    break;
                case "2":
                    searchJobsByKeyword();
                    break;
                case "3":
                    filterJobsByLocation();
                    break;
                case "4":
                    filterJobsByCompany();
                    break;
                case "5":
                    browsing = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays and handles the client‐user menu options (Browse Jobs, View Applications, Update Profile, Logout).
     */
    private void showClientMenu() {
        boolean inClientMenu = true;
        while (inClientMenu) {
            System.out.println("\n===== User Menu =====");
            System.out.println("1. View All Jobs");
            System.out.println("2. Search Jobs");
            System.out.println("3. Apply for a Job");
            System.out.println("4. View My Applications");
            System.out.println("5. Update Profile");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayAllJobs();
                    break;
                case "2":
                    searchJobsByKeyword();
                    break;
                case "3":
                    applyForJob();
                    break;
                case "4":
                    viewMyApplications();
                    break;
                case "5":
                    updateProfile();
                    break;
                case "6":
                    inClientMenu = false;
                    currentUser = null;
                    SecurityContextHolder.clearContext();
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays and handles the recruiter‐user menu options (View My Jobs, Post New Job, Manage Applications, Update Profile, Logout).
     */
    private void showRecruiterMenu() {
        boolean inRecruiterMenu = true;
        while (inRecruiterMenu) {
            System.out.println("\n===== Recruiter Menu =====");
            System.out.println("1. View My Jobs");
            System.out.println("2. Post New Job");
            System.out.println("3. Manage Applications");
            System.out.println("4. Update Profile");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewMyJobs();
                    break;
                case "2":
                    postNewJob();
                    break;
                case "3":
                    manageApplications();
                    break;
                case "4":
                    updateProfile();
                    break;
                case "5":
                    inRecruiterMenu = false;
                    currentUser = null;
                    SecurityContextHolder.clearContext();
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays and handles the admin‐user menu options (Manage Jobs, Recruiters, Users, View Statistics, Logout).
     */
    private void showAdminMenu() {
        boolean inAdminMenu = true;
        while (inAdminMenu) {
            System.out.println("\n===== Admin Menu =====");
            System.out.println("1. Manage Jobs");
            System.out.println("2. Manage Recruiters");
            System.out.println("3. Manage Users");
            System.out.println("4. View Statistics");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    manageJobs();
                    break;
                case "2":
                    manageRecruiters();
                    break;
                case "3":
                    manageUsers();
                    break;
                case "4":
                    viewStatistics();
                    break;
                case "5":
                    inAdminMenu = false;
                    currentUser = null;
                    SecurityContextHolder.clearContext();
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Prints every job in the system in a tabular console format.
     */
    private void displayAllJobs() {
        try {
            Job[] jobs = restTemplate.getForObject(baseUrl + "/jobs", Job[].class);
            if (jobs == null || jobs.length == 0) {
                System.out.println("No jobs found.");
                return;
            }
            System.out.println("\n===== All Jobs =====");
            for (Job job : jobs) {
                System.out.printf("ID: %s\nTitle: %s\nCompany: %s\nLocation: %s\nSalary: $%.2f - $%.2f\n\n",
                        job.getId(), job.getTitle(), job.getCompany(), job.getLocation(),
                        job.getMinSalary(), job.getMaxSalary());
            }
        } catch (RestClientException ex) {
            System.out.println("Error fetching jobs: " + ex.getMessage());
        }
    }

    /**
     * Searches and prints jobs whose title or description contains the given keyword.
     */
    private void searchJobsByKeyword() {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine();

        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/jobs/search")
                    .queryParam("keyword", keyword)
                    .toUriString();
            Job[] jobs = restTemplate.getForObject(uri, Job[].class);
            if (jobs == null || jobs.length == 0) {
                System.out.println("No jobs found matching: " + keyword);
                return;
            }
            System.out.println("\n===== Search Results for '" + keyword + "' =====");
            for (Job job : jobs) {
                System.out.printf("ID: %s\nTitle: %s\nCompany: %s\nLocation: %s\n\n",
                        job.getId(), job.getTitle(), job.getCompany(), job.getLocation());
            }
        } catch (RestClientException ex) {
            System.out.println("Error searching jobs: " + ex.getMessage());
        }
    }

    /**
     * Filters and prints jobs matching the specified location.
     */
    private void filterJobsByLocation() {
        System.out.print("Enter location: ");
        String location = scanner.nextLine();

        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/jobs/location")
                    .queryParam("location", location)
                    .toUriString();
            Job[] jobs = restTemplate.getForObject(uri, Job[].class);
            if (jobs == null || jobs.length == 0) {
                System.out.println("No jobs found in location: " + location);
                return;
            }
            System.out.println("\n===== Jobs in " + location + " =====");
            for (Job job : jobs) {
                System.out.printf("ID: %s\nTitle: %s\nCompany: %s\n\n",
                        job.getId(), job.getTitle(), job.getCompany());
            }
        } catch (RestClientException ex) {
            System.out.println("Error filtering by location: " + ex.getMessage());
        }
    }

    /**
     * Filters and prints jobs matching the specified company name.
     */
    private void filterJobsByCompany() {
        System.out.print("Enter company name: ");
        String company = scanner.nextLine();

        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/jobs/company")
                    .queryParam("company", company)
                    .toUriString();
            Job[] jobs = restTemplate.getForObject(uri, Job[].class);
            if (jobs == null || jobs.length == 0) {
                System.out.println("No jobs found for company: " + company);
                return;
            }
            System.out.println("\n===== Jobs at " + company + " =====");
            for (Job job : jobs) {
                System.out.printf("ID: %s\nTitle: %s\nLocation: %s\n\n",
                        job.getId(), job.getTitle(), job.getLocation());
            }
        } catch (RestClientException ex) {
            System.out.println("Error filtering by company: " + ex.getMessage());
        }
    }

    /**
     * (If present) Prompts for a job ID and submits an application on behalf of the current user.
     */
    private void applyForJob() {
        System.out.print("Enter Job ID to apply for: ");
        String jobId = scanner.nextLine();

        try {
            // Fetch job over HTTP
            Job job = restTemplate.getForObject(baseUrl + "/jobs/" + jobId, Job.class);
            System.out.println("\nJob Details:");
            System.out.printf("Title: %s\nCompany: %s\nLocation: %s\n\n",
                    job.getTitle(), job.getCompany(), job.getLocation());

            System.out.println("Do you want to apply for this job? (y/n)");
            String confirm = scanner.nextLine();

            if (confirm.equalsIgnoreCase("y")) {
                // Build application payload
                Application application = new Application();
                application.setJobId(jobId);
                application.setCandidateName(currentUser.getUsername());
                application.setEmail(currentUser.getEmail());

                System.out.print("Enter your phone number: ");
                application.setPhone(scanner.nextLine());

                System.out.println("Enter a short cover letter or message (press Enter when done):");
                application.setCoverLetterText(scanner.nextLine());

                application.setStatus(Application.ApplicationStatus.SUBMITTED);

                // POST to /applications
                Application created = restTemplate.postForObject(
                        baseUrl + "/applications",
                        application,
                        Application.class
                );

                System.out.println("Application submitted successfully! ID: " + created.getId());
            }
        } catch (RestClientException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    /**
     * Retrieves and prints all job applications submitted by the current client user.
     */
    private void viewMyApplications() {
        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/applications")
                    .queryParam("email", currentUser.getEmail())
                    .toUriString();

            ResponseEntity<List<Application>> resp = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Application>>() {
                    }
            );

            List<Application> applications = resp.getBody();
            if (applications == null || applications.isEmpty()) {
                System.out.println("You haven't submitted any applications yet.");
                return;
            }

            System.out.println("\n===== My Applications =====");
            for (Application app : applications) {
                Job job = restTemplate.getForObject(
                        baseUrl + "/jobs/" + app.getJobId(),
                        Job.class
                );
                System.out.printf("Application ID: %s\nJob: %s at %s\nStatus: %s\nApplied Date: %s\n\n",
                        app.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        app.getStatus(),
                        app.getApplicationDate()
                );
            }
        } catch (RestClientException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    /**
     * Fetches and prints all jobs posted by the current recruiter.
     */
    private void viewMyJobs() {
        try {
            // GET /recruiters/me/jobs?page=0&size=20
            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/recruiters/me/jobs")
                    .queryParam("page", 0)
                    .queryParam("size", 20)
                    .toUriString();

            ResponseEntity<PageResponse<Job>> resp = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PageResponse<Job>>() {}
            );
            List<Job> jobs = resp.getBody().getContent();

            if (jobs.isEmpty()) {
                System.out.println("You haven't posted any jobs yet.");
                return;
            }

            System.out.println("\n===== My Posted Jobs =====");
            for (Job job : jobs) {
                System.out.printf(
                        "ID: %s\nTitle: %s\nCompany: %s\nLocation: %s\n\n",
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation()
                );
            }
        } catch (RestClientException ex) {
            System.out.println("Error fetching your jobs: " + ex.getMessage());
        }
    }

    /**
     * Prompts the recruiter for all required fields and creates a new job posting.
     */
    private void postNewJob() {
        System.out.println("\n===== Post New Job =====");
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Company: ");
        String company = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Minimum Salary: ");
        double minSalary = Double.parseDouble(scanner.nextLine());
        System.out.print("Maximum Salary: ");
        double maxSalary = Double.parseDouble(scanner.nextLine());
        System.out.print("Location: ");
        String location = scanner.nextLine();
        System.out.print("Employment Type: ");
        String employmentType = scanner.nextLine();
        System.out.print("Required Skills (comma-separated): ");
        List<String> requiredSkills = Arrays.asList(scanner.nextLine().split(","));
        System.out.print("Deadline (YYYY-MM-DD): ");
        String deadlineStr = scanner.nextLine();

        try {
            Job payload = new Job();
            payload.setTitle(title);
            payload.setCompany(company);
            payload.setDescription(description);
            payload.setMinSalary(minSalary);
            payload.setMaxSalary(maxSalary);
            payload.setLocation(location);
            payload.setEmploymentType(employmentType);
            payload.setRequiredSkills(requiredSkills);
            payload.setPostDate(LocalDate.now());
            payload.setRecruiterId(currentUser.getId());
            payload.setDeadlineDate(
                    !deadlineStr.isEmpty()
                            ? LocalDate.parse(deadlineStr)
                            : LocalDate.now().plusMonths(1)
            );
            payload.setActive(true);

            Job created = restTemplate.postForObject(
                    baseUrl + "/jobs",
                    payload,
                    Job.class
            );
            System.out.println("Job posted successfully with ID: " + created.getId());
        } catch (RestClientException ex) {
            System.out.println("Error posting job: " + ex.getMessage());
        }
    }

    /**
     * Lets the recruiter browse applications for each of their jobs over HTTP.
     */
    private void manageApplications() {
        try {
            // 1) Lookup recruiter by email
            Recruiter recruiter = restTemplate.getForObject(
                    baseUrl + "/recruiters/byEmail?email={email}",
                    Recruiter.class,
                    currentUser.getEmail()
            );
            if (recruiter == null) {
                System.out.println("No recruiter profile found for your account.");
                return;
            }
            String recruiterId = recruiter.getId();

            // 2) Fetch all jobs for that recruiter (path param, not query param)
            String jobsUri = baseUrl + "/jobs/recruiter/" + recruiterId;
            ResponseEntity<List<Job>> jobsResp = restTemplate.exchange(
                    jobsUri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Job>>() {}
            );
            List<Job> recruiterJobs = jobsResp.getBody();
            if (recruiterJobs == null || recruiterJobs.isEmpty()) {
                System.out.println("You haven't posted any jobs yet.");
                return;
            }

            // 3) For each job, fetch its applications via the /applications/job/{jobId} endpoint
            System.out.println("\n===== Applications for Your Jobs =====");
            for (Job job : recruiterJobs) {
                String appsUri = baseUrl + "/applications/job/" + job.getId();
                ResponseEntity<List<Application>> appsResp = restTemplate.exchange(
                        appsUri,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Application>>() {}
                );
                List<Application> apps = appsResp.getBody();
                if (apps == null || apps.isEmpty()) {
                    continue;
                }

                System.out.printf("\n--- Applications for: %s ---%n", job.getTitle());
                for (Application app : apps) {
                    System.out.printf(
                            "ID: %s | Candidate: %s | Email: %s | Status: %s | Date: %s%n",
                            app.getId(), app.getCandidateName(), app.getEmail(),
                            app.getStatus(), app.getApplicationDate()
                    );
                    System.out.println("1. View Details   2. Update Status   3. Next Application");
                    String choice = scanner.nextLine().trim();
                    switch (choice) {
                        case "1": viewApplicationDetails(app); break;
                        case "2": updateApplicationStatus(app); break;
                        default: /* next */ ;
                    }
                }
            }

        } catch (RestClientException ex) {
            System.out.println("Error fetching applications: " + ex.getMessage());
        }
    }

    /**
     * Displays detailed information about a single application, fetching fresh over HTTP.
     */
    private void viewApplicationDetails(Application app) {
        try {
            Application fresh = restTemplate.getForObject(
                    baseUrl + "/applications/" + app.getId(),
                    Application.class
            );
            System.out.println("\n----- Application Details -----");
            System.out.println("Candidate: " + fresh.getCandidateName());
            System.out.println("Email: " + fresh.getEmail());
            System.out.println("Phone: " + fresh.getPhone());
            System.out.println("Status: " + fresh.getStatus());
            System.out.println("Applied on: " + fresh.getApplicationDate());

            if (fresh.getCoverLetterText() != null && !fresh.getCoverLetterText().isEmpty()) {
                System.out.println("\nCover Letter:");
                System.out.println(fresh.getCoverLetterText());
            }
            if (fresh.getResumeUrl() != null && !fresh.getResumeUrl().isEmpty()) {
                System.out.println("\nResume URL: " + fresh.getResumeUrl());
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        } catch (RestClientException ex) {
            System.out.println("Error fetching details: " + ex.getMessage());
        }
    }

    /**
     * Allows a recruiter to change the status of a particular application via PUT over HTTP.
     */
    private void updateApplicationStatus(Application app) {
        System.out.println("\nCurrent Status: " + app.getStatus());
        System.out.println("Select new status:");
        System.out.println("1. SUBMITTED");
        System.out.println("2. REVIEWING");
        System.out.println("3. INTERVIEWED");
        System.out.println("4. ACCEPTED");
        System.out.println("5. REJECTED");
        System.out.print("Enter your choice: ");

        String choice = scanner.nextLine();
        Application.ApplicationStatus newStatus;
        switch (choice) {
            case "1":
                newStatus = Application.ApplicationStatus.SUBMITTED;
                break;
            case "2":
                newStatus = Application.ApplicationStatus.REVIEWING;
                break;
            case "3":
                newStatus = Application.ApplicationStatus.INTERVIEWED;
                break;
            case "4":
                newStatus = Application.ApplicationStatus.ACCEPTED;
                break;
            case "5":
                newStatus = Application.ApplicationStatus.REJECTED;
                break;
            default:
                System.out.println("Invalid choice. Status not updated.");
                return;
        }

        try {
            app.setStatus(newStatus);
            restTemplate.put(
                    baseUrl + "/applications/" + app.getId(),
                    app
            );
            System.out.println("Application status updated to " + newStatus);
        } catch (RestClientException ex) {
            System.out.println("Error updating status: " + ex.getMessage());
        }
    }

    /**
     * Offers the admin sub‐menu to view, add, update, or delete job listings.
     */
    private void manageJobs() {
        boolean managing = true;
        while (managing) {
            System.out.println("\n===== Manage Jobs =====");
            System.out.println("1. View All Jobs");
            System.out.println("2. Add New Job");
            System.out.println("3. Update Job");
            System.out.println("4. Delete Job");
            System.out.println("5. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    displayAllJobs();
                    break;
                case "2":
                    addNewJob();
                    break;
                case "3":
                    updateJob();
                    break;
                case "4":
                    deleteJob();
                    break;
                case "5":
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Adds a new job via HTTP POST.
     */
    private void addNewJob() {
        System.out.println("\n===== Add New Job =====");
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Company: ");
        String company = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Minimum Salary: ");
        double minSalary = Double.parseDouble(scanner.nextLine());
        System.out.print("Maximum Salary: ");
        double maxSalary = Double.parseDouble(scanner.nextLine());
        System.out.print("Location: ");
        String location = scanner.nextLine();
        System.out.print("Employment Type: ");
        String employmentType = scanner.nextLine();
        System.out.print("Required Skills (comma-separated): ");
        List<String> requiredSkills = Arrays.asList(scanner.nextLine().split(","));
        System.out.print("Deadline (YYYY-MM-DD): ");
        String deadlineStr = scanner.nextLine();

        try {
            Job payload = new Job();
            payload.setTitle(title);
            payload.setCompany(company);
            payload.setDescription(description);
            payload.setMinSalary(minSalary);
            payload.setMaxSalary(maxSalary);
            payload.setLocation(location);
            payload.setEmploymentType(employmentType);
            payload.setRequiredSkills(requiredSkills);
            payload.setPostDate(LocalDate.now());
            payload.setRecruiterId(currentUser.getId());
            payload.setDeadlineDate(
                    !deadlineStr.isEmpty()
                            ? LocalDate.parse(deadlineStr)
                            : LocalDate.now().plusMonths(1)
            );
            payload.setActive(true);

            String uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/jobs")
                    .toUriString();

            Job created = restTemplate.postForObject(uri, payload, Job.class);
            System.out.println("Job posted successfully with ID: " + created.getId());
        } catch (RestClientException ex) {
            System.out.println("Error posting job: " + ex.getMessage());
        }
    }

    /**
     * Prompts for a job ID, fetches it, allows edits, then PUTs back over HTTP.
     */
    private void updateJob() {
        System.out.print("Enter Job ID to update: ");
        String id = scanner.nextLine();
        try {
            String getUri = baseUrl + "/jobs/" + id;
            Job job = restTemplate.getForObject(getUri, Job.class);
            if (job == null) {
                System.out.println("Job not found.");
                return;
            }

            System.out.println("\nCurrent Job Details:");
            System.out.println("Title: " + job.getTitle());
            System.out.println("Company: " + job.getCompany());
            System.out.println("Description: " + job.getDescription());
            System.out.println("Salary Range: $" + job.getMinSalary() + " - $" + job.getMaxSalary());
            System.out.println("Location: " + job.getLocation());
            System.out.println("Employment Type: " + job.getEmploymentType());
            System.out.println("Active: " + (job.isActive() ? "Yes" : "No"));

            System.out.println("\nEnter new details (leave blank to keep current value):");

            System.out.print("Title: ");
            String title = scanner.nextLine();
            if (!title.isEmpty()) job.setTitle(title);

            System.out.print("Company: ");
            String company = scanner.nextLine();
            if (!company.isEmpty()) job.setCompany(company);

            System.out.print("Description: ");
            String description = scanner.nextLine();
            if (!description.isEmpty()) job.setDescription(description);

            System.out.print("Minimum Salary: ");
            String minSalaryStr = scanner.nextLine();
            if (!minSalaryStr.isEmpty()) job.setMinSalary(Double.parseDouble(minSalaryStr));

            System.out.print("Maximum Salary: ");
            String maxSalaryStr = scanner.nextLine();
            if (!maxSalaryStr.isEmpty()) job.setMaxSalary(Double.parseDouble(maxSalaryStr));

            System.out.print("Location: ");
            String location = scanner.nextLine();
            if (!location.isEmpty()) job.setLocation(location);

            System.out.print("Employment Type: ");
            String employmentType = scanner.nextLine();
            if (!employmentType.isEmpty()) job.setEmploymentType(employmentType);

            System.out.print("Active (true/false): ");
            String activeStr = scanner.nextLine();
            if (!activeStr.isEmpty()) job.setActive(Boolean.parseBoolean(activeStr));

            restTemplate.put(baseUrl + "/jobs/" + id, job);
            System.out.println("Job updated successfully!");
        } catch (RestClientException ex) {
            System.out.println("Error updating job: " + ex.getMessage());
        }
    }

    /**
     * Deletes a job by ID via HTTP DELETE.
     */
    private void deleteJob() {
        System.out.print("Enter Job ID to delete: ");
        String id = scanner.nextLine();
        try {
            restTemplate.delete(baseUrl + "/jobs/" + id);
            System.out.println("Job deleted successfully!");
        } catch (RestClientException ex) {
            System.out.println("Error deleting job: " + ex.getMessage());
        }
    }

    /**
     * Offers the admin sub‐menu to view, add, update, or delete recruiter profiles.
     */
    private void manageRecruiters() {
        boolean managing = true;
        while (managing) {
            System.out.println("\n===== Manage Recruiters =====");
            System.out.println("1. View All Recruiters");
            System.out.println("2. Add New Recruiter");
            System.out.println("3. Update Recruiter");
            System.out.println("4. Delete Recruiter");
            System.out.println("5. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    displayAllRecruiters();
                    break;
                case "2":
                    addNewRecruiter();
                    break;
                case "3":
                    updateRecruiter();
                    break;
                case "4":
                    deleteRecruiter();
                    break;
                case "5":
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Prints every recruiter record by HTTP GET /recruiters.
     */
    private void displayAllRecruiters() {
        try {
            ResponseEntity<List<Recruiter>> resp = restTemplate.exchange(
                    baseUrl + "/recruiters",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Recruiter>>() {
                    }
            );
            List<Recruiter> recruiters = resp.getBody();
            if (recruiters == null || recruiters.isEmpty()) {
                System.out.println("No recruiters found.");
                return;
            }
            System.out.println("\n===== All Recruiters =====");
            for (Recruiter r : recruiters) {
                System.out.printf("ID: %s\nName: %s\nCompany: %s\nEmail: %s\n\n",
                        r.getId(), r.getName(), r.getCompany(), r.getEmail());
            }
        } catch (RestClientException ex) {
            System.out.println("Error fetching recruiters: " + ex.getMessage());
        }
    }

    /**
     * Offers the admin sub‐menu to view all users and toggle their enabled/disabled status.
     */
    private void manageUsers() {
        boolean managing = true;
        while (managing) {
            System.out.println("\n===== Manage Users =====");
            System.out.println("1. View All Users");
            System.out.println("2. Enable/Disable User");
            System.out.println("3. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    displayAllUsers();
                    break;
                case "2":
                    toggleUserStatus();
                    break;
                case "3":
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Prints every registered user via HTTP GET /api/admin/users,
     * binding to CurrentUserDto rather than the raw User entity.
     */
    private void displayAllUsers() {
        try {
            // 1) Fetch a list of CurrentUserDto from the admin endpoint
            ResponseEntity<List<CurrentUserDto>> resp = restTemplate.exchange(
                    baseUrl + "/admin/users",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CurrentUserDto>>() {}
            );

            List<CurrentUserDto> users = resp.getBody();
            if (users == null || users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }

            // 2) Print a header
            System.out.println("\n===== All Users =====");
            System.out.printf("%-24s %-15s %-25s %-20s%n", "ID", "Username", "Email", "Roles");
            System.out.println("--------------------------------------------------------------------------------");

            // 3) Loop over the DTOs and print their fields
            for (CurrentUserDto u : users) {
                String roles = String.join(",", u.getRoles());
                System.out.printf(
                        "%-24s %-15s %-25s %-20s%n",
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        roles
                );
            }
        } catch (RestClientException ex) {
            System.out.println("Error fetching users: " + ex.getMessage());
        }
    }

    /**
     * Enables or disables a user via HTTP PUT /api/admin/users/{id}/status?enabled={true|false}.
     */
    private void toggleUserStatus() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("No User ID provided.");
            return;
        }

        // Ask admin which action
        System.out.print("Do you want to (e)nable or (d)isable this user? ");
        String choice = scanner.nextLine().trim().toLowerCase();
        boolean enabled;
        if ("e".equals(choice)) {
            enabled = true;
        } else if ("d".equals(choice)) {
            enabled = false;
        } else {
            System.out.println("Invalid choice; please enter 'e' or 'd'.");
            return;
        }

        try {
            // Use PUT instead of PATCH (RestTemplate out-of-the-box supports PUT)
            restTemplate.put(
                    baseUrl + "/admin/users/" + id + "/status?enabled=" + enabled,
                    null
            );
            System.out.printf("User %s has been %s.%n",
                    id,
                    enabled ? "enabled" : "disabled"
            );
        } catch (HttpClientErrorException ex) {
            System.out.println("Error updating user status: " + ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    /**
     * Retrieves and prints high‐level system statistics via HTTP GET /api/admin/stats.
     */
    private void viewStatistics() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = restTemplate.getForObject(
                    baseUrl + "/admin/stats",
                    Map.class
            );
            System.out.println("\n===== System Statistics =====");
            System.out.println("Total Users:        " + stats.get("totalUsers"));
            System.out.println("Total Jobs:         " + stats.get("totalJobs"));
            System.out.println("Total Applications: " + stats.get("totalApplications"));
            System.out.println("Total Recruiters:   " + stats.get("totalRecruiters"));
        } catch (RestClientException ex) {
            System.out.println("Error fetching statistics: " + ex.getMessage());
        }
    }

    /**
     * Gracefully shuts down the Spring application context and terminates the JVM.
     */
    private void exit() {
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

    /**
     * Prompts the admin for recruiter details, registers a user via POST /users,
     * then creates their profile via POST /recruiters.
     */
    private void addNewRecruiter() {
        System.out.println("\n===== Add New Recruiter =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Company: ");
        String company = scanner.nextLine();
        System.out.print("Position: ");
        String position = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();

        // Build the signup payload – match your SignupRequest DTO
        Map<String, Object> signup = new HashMap<>();
        signup.put("username", username);
        signup.put("email",    email);
        signup.put("password", password);
        signup.put("role",     "RECRUITER");
        signup.put("company",  company);
        signup.put("position", position);
        signup.put("phone",    phone);

        try {
            // This hits POST /api/auth/register
            ResponseEntity<MessageResponse> resp = restTemplate.postForEntity(
                    baseUrl + "/auth/register",
                    new HttpEntity<>(signup),
                    MessageResponse.class
            );

            if (resp.getStatusCode() == HttpStatus.OK) {
                System.out.println("Recruiter registered successfully!");
            } else {
                System.out.println("Registration returned: " + resp.getStatusCode());
            }
        } catch (HttpClientErrorException ex) {
            System.out.println("Error adding recruiter: " + ex.getResponseBodyAsString());
        }
    }


    /**
     * Prompts for a recruiter ID, fetches via GET, updates fields, then PUT /recruiters/{id}.
     */
    private void updateRecruiter() {
        System.out.print("Enter Recruiter ID: ");
        String id = scanner.nextLine();
        try {
            String uri = baseUrl + "/recruiters/" + id;
            Recruiter r = restTemplate.getForObject(uri, Recruiter.class);
            if (r == null) {
                System.out.println("Recruiter not found.");
                return;
            }

            System.out.println("\nCurrent Recruiter Details:");
            System.out.println("Name: " + r.getName());
            System.out.println("Email: " + r.getEmail());
            System.out.println("Company: " + r.getCompany());
            System.out.println("Position: " + r.getPosition());
            System.out.println("Phone: " + r.getPhone());

            System.out.println("\nEnter new details (leave blank to keep current):");
            System.out.print("Name: ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) r.setName(name);
            System.out.print("Email: ");
            String email = scanner.nextLine();
            if (!email.isEmpty()) r.setEmail(email);
            System.out.print("Company: ");
            String company = scanner.nextLine();
            if (!company.isEmpty()) r.setCompany(company);
            System.out.print("Position: ");
            String pos = scanner.nextLine();
            if (!pos.isEmpty()) r.setPosition(pos);
            System.out.print("Phone: ");
            String phone = scanner.nextLine();
            if (!phone.isEmpty()) r.setPhone(phone);

            restTemplate.put(uri, r);
            System.out.println("Recruiter updated successfully!");
        } catch (RestClientException ex) {
            System.out.println("Error updating recruiter: " + ex.getMessage());
        }
    }

    /**
     * Deletes a recruiter via HTTP DELETE /recruiters/{id}.
     */
    private void deleteRecruiter() {
        System.out.print("Enter Recruiter ID: ");
        String id = scanner.nextLine();
        try {
            restTemplate.delete(baseUrl + "/recruiters/" + id);
            System.out.println("Recruiter deleted successfully!");
        } catch (RestClientException ex) {
            System.out.println("Error deleting recruiter: " + ex.getMessage());
        }
    }

    /**
     * A simple helper DTO to deserialize Spring Data {@code Page<T>} responses.
     *
     * @param <T> the type of each element in the page
     */
    private static class PageResponse<T> {
        private List<T> content;
        public List<T> getContent() { return content; }
        public void setContent(List<T> content) { this.content = content; }
    }

}