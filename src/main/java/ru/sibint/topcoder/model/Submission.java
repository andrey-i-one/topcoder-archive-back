package ru.sibint.topcoder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submission", schema = "oj")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Submission {

    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private Problem problem;

    @Column(name = "sources")
    private String sources;

    @Column(name = "author")
    private String author;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

}