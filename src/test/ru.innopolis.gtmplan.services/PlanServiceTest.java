package ru.innopolis.gtmtechcard.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.innopolis.gtmtechcard.AbstractTest;
import ru.innopolis.gtmtechcard.generated.dto.MethodDto;
import ru.innopolis.gtmtechcard.generated.dto.PlanDto;
import ru.innopolis.gtmtechcard.generated.dto.PlanShortDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class PlanServiceTest extends AbstractTest {

    @Autowired
    private PlanService planService;


    @Transactional
    @org.junit.jupiter.api.Test
    void getPlans() throws Exception {
        List<PlanDto> data = planService.getPlans(0, 1, null, null, null, null, null)
                .getBody().getData();
        assertEquals(1, data.size());
        data = planService.getPlans(0, 2, null, null, null, null, null)
                .getBody().getData();
        assertEquals(2, data.size());
        data = planService.getPlans(0, 2, null, null, null, "31.08.2022", "31.08.2022")
                .getBody().getData();
        assertTrue(!data.isEmpty());
        data = planService.getPlans(0, 1, null, null, null, null, null)
                .getBody().getData();
        assertEquals(1, data.size());
    }

    @Transactional
    @org.junit.jupiter.api.Test
    void getPlansByBaseDocumentId() throws Exception {
        List<PlanDto> data = planService.getPlansByBaseDocumentId("123", 0, 10).getBody().getData();
        assertTrue(!data.isEmpty());
        data = planService.getPlansByBaseDocumentId("123", 0, 1).getBody().getData();
        assertEquals(1, data.size());
    }

    @Transactional
    @org.junit.jupiter.api.Test
    @Order(10)
    void postPlan() throws Exception {
        String baseDocumentNumber = UUID.randomUUID().toString();
        PlanShortDto planDto = new PlanShortDto();
        planDto.setBaseDocumentDate("31.08.2022 14:58");
        planDto.setBaseDocumentNumber(baseDocumentNumber);
        planDto.setDate("31.08.2022");
        planDto.setPurposeId("d90a0c30-ed2c-4bbe-aabe-d8ef10883411");
        planDto.setTaskId("809e0155-aba2-46fc-a593-e5923fa204ab");
        planDto.setWellId("c0f7ce2a-e8da-4b58-9b13-5efdfb575934");
        planDto.setStatusId("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        planDto.setMethods(
                Collections.singletonList(MethodDto.builder().investigationIntervalFrom(BigDecimal.valueOf(950))
                        .investigationIntervalTo(BigDecimal.valueOf(1000))
                        .methodId("71561368-2081-47fb-8944-4107a234ed4f")
                        .build()));
        PlanShortDto newPlanDto1 = planService.postPlan(planDto).getBody();
        log.info("new object id1: {}", newPlanDto1);
        assertNotNull(newPlanDto1.getId());
        PlanShortDto newPlanDto2 = planService.postPlan(planDto).getBody();
        assertNotNull(newPlanDto2.getId());
        log.info("new object id2: {}", newPlanDto2);


        /** Проверка что исполнитель заполняется по умолчанию */
        List<PlanDto> data = planService.getPlansByBaseDocumentId(baseDocumentNumber, 0, 1).getBody().getData();
        log.info("id:{}, name: {}", data.get(0).getExecutor().getId(), data.get(0).getExecutor().getDescription());
        log.info("executor {}", data.get(0).getExecutor());
        assertTrue(!data.isEmpty());
        assertEquals(1, data.size());
        assertNotNull(data.get(0).getExecutor().getId());


        planDto = new PlanShortDto();
        planDto.setBaseDocumentDate("31.08.2022 14:58");
        planDto.setBaseDocumentNumber(baseDocumentNumber);
        planDto.setDate("31.08.2022");
        planDto.setPurposeId("d90a0c30-ed2c-4bbe-aabe-d8ef10883411");
        planDto.setTaskId("809e0155-aba2-46fc-a593-e5923fa204ab");
        planDto.setWellId("c0f7ce2a-e8da-4b58-9b13-5efdfb575934");
        planDto.setStatusId("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        planDto.setMethods(
                Collections.singletonList(MethodDto.builder().investigationIntervalFrom(BigDecimal.valueOf(950.00))
                        .investigationIntervalTo(BigDecimal.valueOf(1000.00))
                        .methodId("71561368-2081-47fb-8944-4107a234ed4f")
                        .build()));
        newPlanDto1 = planService.postPlan(planDto).getBody();
        log.info("new object id1: {}", newPlanDto1);
        assertNotNull(newPlanDto1.getId());
    }

    @Transactional
    @org.junit.jupiter.api.Test
    @Order(20)
    void putPlan() throws Exception {
        PlanShortDto planDto = new PlanShortDto();
        planDto.setBaseDocumentDate("31.08.2022 14:58");
        planDto.setBaseDocumentNumber("123");
        planDto.setDate("31.08.2022");
        planDto.setPurposeId("d90a0c30-ed2c-4bbe-aabe-d8ef10883411");
        planDto.setTaskId("809e0155-aba2-46fc-a593-e5923fa204ab");
        planDto.setWellId("c0f7ce2a-e8da-4b58-9b13-5efdfb575934");
        planDto.setStatusId("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        planDto.setMethods(
                Collections.singletonList(MethodDto.builder().investigationIntervalFrom(BigDecimal.valueOf(950))
                        .investigationIntervalTo(BigDecimal.valueOf(1000))
                        .methodId("71561368-2081-47fb-8944-4107a234ed4f")
                        .build()));
        String newDate = "31.08.2022";
        String newBaseDocumentDate = "01.09.2022 15:58";
        String newBaseDocumentNumber = "9999";
        PlanShortDto newPlanDto1 = planService.postPlan(planDto).getBody();
        assertNotNull(newPlanDto1.getId());
        /** Методы только создаются новые, старые никак не изменяются */
        newPlanDto1.setMethods(Collections.emptyList());
        /** тест изменения полей date, newBaseDocumentNumber, newBaseDocumentDate */
        newPlanDto1.setDate(newDate);
        newPlanDto1.setBaseDocumentNumber(newBaseDocumentNumber);
        newPlanDto1.setBaseDocumentDate(newBaseDocumentDate);
        PlanShortDto newPlanDto2 = planService.putPlan(newPlanDto1).getBody();
        assertNotNull(newPlanDto2.getId());
        assertEquals(newDate, newPlanDto2.getDate());
        assertEquals(newBaseDocumentDate, newPlanDto2.getBaseDocumentDate());
        assertEquals(newBaseDocumentNumber, newPlanDto2.getBaseDocumentNumber());

        /** Тест изменения поля purposeId */
        String newPurposeId = "85a09958-c487-4c06-80d8-8123a2f2a74a";
        newPlanDto2.setPurposeId(newPurposeId);
        newPlanDto2 = planService.putPlan(newPlanDto2).getBody();
        assertNotNull(newPlanDto2.getId());
        assertEquals(newPurposeId, newPlanDto2.getPurposeId());

        /** Тест изменения поля wellId */
        String newWellId = "651f86af-3bec-4171-8ec4-f0a9e286891d";
        newPlanDto2.setWellId(newWellId);
        newPlanDto2 = planService.putPlan(newPlanDto2).getBody();
        assertNotNull(newPlanDto2.getId());
        assertEquals(newWellId, newPlanDto2.getWellId());

        /** Тест изменения поля taskId */
        String newTaskId = "45d9e748-a0f1-44ee-af09-22031270f254";
        newPlanDto2.taskId(newTaskId);
        newPlanDto2 = planService.putPlan(newPlanDto2).getBody();
        assertNotNull(newPlanDto2.getId());
        assertEquals(newTaskId, newPlanDto2.getTaskId());

        /** Тест изменения поля StatusId */
        String newStatusId = "53c2d3ff-4070-4fef-96da-e39bb80669b8";
        newPlanDto2.setStatusId(newStatusId);
        newPlanDto2 = planService.putPlan(newPlanDto2).getBody();
        assertNotNull(newPlanDto2.getId());
        assertEquals(newStatusId, newPlanDto2.getStatusId());
    }
}