package uk.gov.hmcts.cp.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CourtHouseMapperTest {

    @Spy
    ObjectMapper objectMapper;
    @InjectMocks
    CourtHouseMapper courtHouseMapper;

    @SneakyThrows
    @Test
    void cp_response_string_should_map_to_object(){
        String cpResponse = Files.readString(Path.of("src/test/resources/courtRoomResponse.json"));
        CourtResponse response = courtHouseMapper.mapStringToCourtResponse(cpResponse);
        assertThat(response.getOucode()).isEqualTo("B42CM00");
        assertThat(response.getCourtrooms().get(0).getCourtroomName()).isEqualTo("Courtroom 01");
    }

    @Test
    void cp_response_object_should_map_to_amp_response() {
        UUID courtRoomId = UUID.fromString("3a8da3da-02b5-45f8-b81c-bda81e54f3bc");
        CourtResponse.CourtRoom courtRoom = CourtResponse.CourtRoom.builder()
            .id(courtRoomId.toString())
            .courtroomId("2330")
            .courtroomName("Courtroom 01")
            .build();
        CourtResponse courtResponse = CourtResponse.builder()
            .oucodeL1Name("Magistrates Courts")
            .address1("Line1")
            .address2("Line2")
            .address3("Line3")
            .address4("Line4")
            .postcode("SW11 1JU")
            .oucode("B01LY00")
            .oucodeL3Name("Lavender Hill Magistrates' Court")
            .courtrooms(List.of(courtRoom))
            .build();

        CourtHouseResponse response = courtHouseMapper.mapCourtHouseCPResponseWithCourtRoomId(courtResponse, courtRoomId);

        assertResponse(response);
    }

    private void assertResponse(CourtHouseResponse courtResponse) {
        assertThat(courtResponse.getCourtHouseType()).isEqualTo(CourtHouseResponse.CourtHouseTypeEnum.MAGISTRATE);
        assertThat(courtResponse.getCourtHouseCode()).isEqualTo("B01LY00");
        assertThat(courtResponse.getCourtHouseName()).isEqualTo("Lavender Hill Magistrates' Court");
        assertResponseAddress(courtResponse.getAddress());
        assertThat(courtResponse.getCourtRoom().get(0).getCourtRoomId()).isEqualTo(2330);
        assertThat(courtResponse.getCourtRoom().get(0).getCourtRoomName()).isEqualTo("Courtroom 01");
    }

    private void assertResponseAddress(Address address) {
        assertThat(address.getAddress1()).isEqualTo("Line1");
        assertThat(address.getAddress2()).isEqualTo("Line2");
        assertThat(address.getAddress3()).isEqualTo("Line3");
        assertThat(address.getAddress4()).isEqualTo("Line4");
        assertThat(address.getPostalCode()).isEqualTo("SW11 1JU");
        assertThat(address.getCountry()).isEqualTo("UK");
    }
}
