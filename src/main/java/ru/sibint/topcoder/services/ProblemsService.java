package ru.sibint.topcoder.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sibint.topcoder.generated.dto.PageInfo;
import ru.sibint.topcoder.generated.dto.ProblemDetailsDto;
import ru.sibint.topcoder.generated.dto.ProblemDto;
import ru.sibint.topcoder.generated.dto.ProblemsPageDto;
import ru.sibint.topcoder.model.Problem;
import ru.sibint.topcoder.model.QProblem;
import ru.sibint.topcoder.repos.ProblemRepository;
import ru.sibint.topcoder.utils.ExamplesParser;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProblemsService {

    private final ProblemRepository problemRepository;
    private final ExamplesParser examplesParser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public ProblemsPageDto retrieveProblems(Integer page,
                                            Integer perPage,
                                            String sortField,
                                            String sortOrder,
                                            String tags,
                                            String div1Level,
                                            String div2Level) throws Exception {
        Pageable pageable = PageRequest.of(page, perPage).withSort(Sort.by(Sort.Direction.valueOf(sortOrder), sortField));
        Page<Problem> pages = problemRepository.findAll(
                QProblem.problem.tags.likeIgnoreCase("%" + (tags == null ? "" : tags) + "%").and(
                        div1Level == null ? QProblem.problem.div1Level.isNotNull() : QProblem.problem.div1Level.likeIgnoreCase("%" + div1Level + "%")
                ).and(
                        div2Level == null ? QProblem.problem.div2Level.isNotNull() : QProblem.problem.div2Level.likeIgnoreCase("%" + div2Level + "%")
                ), pageable);
        return ProblemsPageDto.builder()
                .data(pages.getContent().stream().map(it -> ProblemDto.builder()
                        .id(it.getId())
                        .name(it.getName())
                        .srm(it.getSrm())
                        .div1Level(it.getDiv1Level())
                        .div2Level(it.getDiv2Level())
                        .div1SuccessRate(it.getDiv1SuccessRate())
                        .div2SuccessRate(it.getDiv2SuccessRate())
                        .date(formatter.format(it.getDate()))
                        .tags(it.getTags())
                        .build()).toList())
                .pageInfo(PageInfo.builder()
                        .number(pages.getNumber())
                        .numberOfElements(pages.getNumberOfElements())
                        .totalElements(pages.getTotalElements())
                        .totalPages(pages.getTotalPages())
                        .build())
                .build();
    }

    public ProblemDetailsDto retrieveProblemById(UUID id) throws Exception {
        Problem problem = problemRepository.findById(id).orElse(null);
        return problem == null ? null : ProblemDetailsDto.builder()
                .id(problem.getId())
                .name(problem.getName())
                .srm(problem.getSrm())
                .div1Level(problem.getDiv1Level())
                .div2Level(problem.getDiv2Level())
                .div1SuccessRate(problem.getDiv1SuccessRate())
                .div2SuccessRate(problem.getDiv2SuccessRate())
                .date(formatter.format(problem.getDate()))
                .definition(problem.getDefinition())
                .constraints(problem.getConstraints())
                .tags(problem.getTags())
                .statement(problem.getStatement())
                .examples(problem.getExamples())
                .tests(examplesParser.parseExamples("<root>" + problem.getExamples() + "</root>"))
                .build();
    }

}