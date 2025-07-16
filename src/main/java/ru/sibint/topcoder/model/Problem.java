package ru.sibint.topcoder.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "problem", schema = "oj")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Problem {

    @Id
    @JdbcTypeCode(Types.VARCHAR)
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(name = "problem_name")
    private String name;

    @Column(name = "srm")
    private String srm;

    @Column(name = "tags")
    private String tags;

    @Column(name = "div1_level")
    private String div1Level;

    @Column(name = "div2_level")
    private String div2Level;

    @Column(name = "div1_success_rate")
    private String div1SuccessRate;

    @Column(name = "div2_success_rate")
    private String div2SuccessRate;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "problem_description")
    private String description;

    @Column(name = "statement")
    private String statement;

    @Column(name = "examples")
    private String examples;

    @Column(name = "constraints")
    private String constraints;

    @Column(name = "definition")
    private String definition;

}
