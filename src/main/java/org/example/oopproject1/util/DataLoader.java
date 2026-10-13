package org.example.oopproject1.util;

import org.example.oopproject1.model.Job;
import org.example.oopproject1.model.Recruiter;
import org.example.oopproject1.repository.JobRepository;
import org.example.oopproject1.repository.RecruiterRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Loads sample data into the database on application startup when enabled.
 * <p>
 * Creates a default recruiter and 20 sample jobs if no jobs exist.
 * Controlled by the 'app.load-sample-data' property.
 * </p>
 *
 * @since 1.0
 */
@Component
@ConditionalOnProperty(name = "app.load-sample-data", havingValue = "true", matchIfMissing = false)
public class DataLoader implements CommandLineRunner {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    /**
     * Runs on application startup to load initial sample data.
     * <p>
     * Checks if jobs already exist; if not, creates a sample recruiter
     * and populates 20 sample job postings with varying attributes.
     * </p>
     *
     * @param args command-line arguments (ignored)
     * @throws Exception if an error occurs during data loading
     */
    @Override
    public void run(String... args) throws Exception {
        if (jobRepository.count() > 0) {
            return;
        }

        Recruiter recruiter = new Recruiter();
        recruiter.setName("John Smith");
        recruiter.setEmail("john@example.com");
        recruiter.setCompany("ABC Corp");
        recruiter.setPosition("HR Manager");
        recruiter.setPhone("123-456-7890");
        recruiter = recruiterRepository.save(recruiter);

        for (int i = 1; i <= 20; i++) {
            Job job = new Job();
            job.setTitle("Software Engineer " + i);
            job.setCompany(i % 3 == 0 ? "ABC Corp" : (i % 3 == 1 ? "XYZ Inc" : "Tech Solutions"));
            job.setDescription("Job description for position " + i);
            job.setRequiredSkills(Arrays.asList("Java", "Spring", "MongoDB"));
            job.setMinSalary(50000.0 + (i * 1000));
            job.setMaxSalary(80000.0 + (i * 1000));
            job.setLocation(i % 2 == 0 ? "Remote" : "New York");
            job.setEmploymentType(i % 4 == 0 ? "Contract" : "Full-time");
            job.setPostDate(LocalDate.now().minusDays(i));
            job.setDeadlineDate(LocalDate.now().plusMonths(1));
            job.setActive(true);
            job.setRecruiterId(recruiter.getId());

            jobRepository.save(job);
        }
    }
}