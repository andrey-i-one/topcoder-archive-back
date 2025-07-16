package ru.sibint.topcoder.services.card;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.innopolis.gtm.db.dict.models.DeliveryEntity;
import ru.innopolis.gtm.db.dict.models.ResearchTaskEntity;
import ru.innopolis.gtm.db.dict.models.TechCardStatusEntity;
import ru.innopolis.gtm.db.dict.repos.ResearchTaskRepository;
import ru.innopolis.gtm.db.techcard.models.TechCardEntity;
import ru.innopolis.gtm.db.techcard.repos.TechCardRepository;
import ru.innopolis.gtmcommonlib.services.LookupService;
import ru.sibint.topcoder.generated.dto.TechCardCopyDto;
import ru.sibint.topcoder.mappers.TechCardTransformer;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author b.khafizullin@innopolis.ru
 */
@ExtendWith(SpringExtension.class)
class TechCardServiceTest {

    @Mock
    private TechCardTransformer techCardTransformer;

    @Mock
    private TechCardRepository techCardRepository;

    @Mock
    private ResearchTaskRepository researchTaskRepository;

    @Mock
    private LookupService lookupService;

    @InjectMocks
    private TechCardService techCardService;

    @Captor
    private ArgumentCaptor<TechCardEntity> techCardCopyCaptor;

    @Test
    void positive_fullCopyWithoutId_andChangeTechCardsResearchTasksName() {
        //arrange
        UUID techCardId = UUID.randomUUID();

        ResearchTaskEntity oldTask = ResearchTaskEntity.builder().id(UUID.randomUUID()).build();
        ResearchTaskEntity newTask = ResearchTaskEntity.builder().id(UUID.randomUUID()).build();

        DeliveryEntity deliveryEntity = new DeliveryEntity();
        deliveryEntity.setId(UUID.randomUUID());

        TechCardEntity techCard = TechCardEntity.builder()
                .id(techCardId)
                .status(TechCardStatusEntity.builder().id(UUID.randomUUID()).build())
                .delivery(deliveryEntity)
                .researchTask(oldTask)
                .performanceWayEntities(new HashSet<>())
                .purposeEntity(new HashSet<>())
                .typeEntities(new HashSet<>())
                .wellCategoryEntities(new HashSet<>())
                .build();

        TechCardCopyDto techCardCopyDto = TechCardCopyDto.builder()
                .taskId(newTask.getId().toString())
                .build();

        when(techCardRepository.findById(techCard.getId())).thenReturn(Optional.of(techCard));
        when(techCardRepository.getByIdWithJoins(techCard.getId())).thenReturn(Optional.of(techCard));
        when(lookupService.lookupEntity(newTask.getId(), ResearchTaskEntity.class)).thenReturn(newTask);
        when(researchTaskRepository.findById(UUID.fromString(techCardCopyDto.getTaskId()))).thenReturn(Optional.of(newTask));

        //act
        techCardService.createCopyOfTechCard(techCard.getId(), techCardCopyDto);

        //assert
        verify(techCardRepository).save(techCardCopyCaptor.capture());
        TechCardEntity copy = techCardCopyCaptor.getValue();

        assertThat(copy.getId()).isNull();
        assertThat(copy)
                .usingRecursiveComparison()
                .ignoringFields("id", "researchTask")
                .isEqualTo(techCard);
        assertThat(copy.getResearchTask().getId()).isNotEqualTo(techCard.getResearchTask().getId());

        verify(techCardRepository, times(1)).save(any());
    }

}