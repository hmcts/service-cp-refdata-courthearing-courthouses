package uk.gov.hmcts.cp.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CourtHousesClientTest {

    private CourtHousesClient courtHousesClient;

    @BeforeEach
    void setUp() {
        courtHousesClient = new InMemoryCourtHousesClientImpl();
    }

    @Test
    void getCourtScheduleByCaseUrn_shouldReturnCourtScheduleResponse() {
        UUID courtId = UUID.randomUUID();
        UUID courtRoomId = UUID.randomUUID();
        CourtHouseResponse response = courtHousesClient.getCourtHouse(
            courtId.toString(), courtRoomId.toString());

        assertNotNull(response.getCourtHouseCode());
        assertEquals(1, response.getCourtRoom().size());
    }
}
