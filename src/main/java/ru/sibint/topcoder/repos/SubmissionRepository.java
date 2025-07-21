package ru.sibint.topcoder.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.sibint.topcoder.model.Submission;

import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID>, QuerydslPredicateExecutor<Submission> {
}
