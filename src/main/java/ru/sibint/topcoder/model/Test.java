package ru.sibint.topcoder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test", schema = "oj")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Test {

    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private Problem problem;

    @Column(name = "input")
    private String input;

    @Column(name = "expected_output")
    private String expectedOutput;

    @Column(name = "num")
    private Integer number;

}