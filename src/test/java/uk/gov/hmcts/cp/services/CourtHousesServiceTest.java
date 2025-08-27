package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.repositories.CourtHousesClient;
import uk.gov.hmcts.cp.repositories.InMemoryCourtHousesClientImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourtHousesServiceTest {

    private final CourtHousesClient courtHousesClient = new InMemoryCourtHousesClientImpl();
    private final CourtHousesService courtHousesService = new CourtHousesService(courtHousesClient);

    @Test
    void shouldReturnStubbedCourtHouseResponse_whenValidCourtIdProvided() {
        // Arrange
        String validCourtId = "123";
        String validCourtRoomId = "123";

        // Act
        CourtHouseResponse response = courtHousesService.getCourtHouse(validCourtId, validCourtRoomId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCourtHouseName()).isEqualTo("Central London County Court");
        assertThat(response.getCourtRoom()).isNotEmpty();
        assertThat(response.getCourtRoom().get(0).getCourtRoomName()).isEqualTo("Courtroom 1");
    }

    @Test
    void shouldThrowBadRequestException_whenCourtIdIsNull() {
        // Arrange
        String nullCourtId = null;

        // Act & Assert
        assertThatThrownBy(() -> courtHousesService.getCourtHouse(nullCourtId, null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("courtId and court room id is required");
    }

    @Test
    void shouldThrowBadRequestException_whenCourtIdIsEmpty() {
        // Arrange
        String emptyCourtId = "";
        String emptyCourtRoomId = "";

        // Act & Assert
        assertThatThrownBy(() -> courtHousesService.getCourtHouse(emptyCourtId, emptyCourtRoomId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("courtId and court room id is required");
    }
}
