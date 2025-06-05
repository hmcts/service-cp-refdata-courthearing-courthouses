package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CourtHousesRepositoryTest {

    private CourtHousesRepository courtHousesRepository;

    @BeforeEach
    void setUp() {
        courtHousesRepository = new InMemoryCourtHousesRepositoryImpl();
    }

    @Test
    void getCourtScheduleByCaseUrn_shouldReturnCourtScheduleResponse() {
        UUID courtId = UUID.randomUUID();
        CourtHouseResponse response = courtHousesRepository.getCourtHouse(
            courtId.toString());

        assertNotNull(response.getCourtHouseCode());
        assertEquals(1, response.getCourtRoom().size());
    }
}
