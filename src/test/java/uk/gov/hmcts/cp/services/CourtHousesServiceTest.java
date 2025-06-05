package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.repositories.CourtHousesRepository;
import uk.gov.hmcts.cp.repositories.InMemoryCourtHousesRepositoryImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourtHousesServiceTest {

    private final CourtHousesRepository courtHousesRepository = new InMemoryCourtHousesRepositoryImpl();
    private final CourtHousesService courtHousesService = new CourtHousesService(courtHousesRepository);

    @Test
    void shouldReturnStubbedCourtHouseResponse_whenValidCourtIdProvided() {
        // Arrange
        String validCourtId = "123";

        // Act
        CourtHouseResponse response = courtHousesService.getCourtHouse(validCourtId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCourtHouseName()).isEqualTo("Central London County Court");
        assertThat(response.getCourtHouseDescription()).isEqualTo("Main Crown Court in London handling major cases");
        assertThat(response.getCourtRoom()).isNotEmpty();
        assertThat(response.getCourtRoom().get(0).getCourtRoomName()).isEqualTo("Courtroom 1");
    }

    @Test
    void shouldThrowBadRequestException_whenCourtIdIsNull() {
        // Arrange
        String nullCourtId = null;

        // Act & Assert
        assertThatThrownBy(() -> courtHousesService.getCourtHouse(nullCourtId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("courtId is required");
    }

    @Test
    void shouldThrowBadRequestException_whenCourtIdIsEmpty() {
        // Arrange
        String emptyCourtId = "";

        // Act & Assert
        assertThatThrownBy(() -> courtHousesService.getCourtHouse(emptyCourtId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST")
            .hasMessageContaining("courtId is required");
    }
}
