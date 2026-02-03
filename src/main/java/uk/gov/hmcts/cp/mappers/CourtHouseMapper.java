package uk.gov.hmcts.cp.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;

import java.util.Arrays;
import java.util.UUID;

@Component
@AllArgsConstructor
public class CourtHouseMapper {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public CourtResponse mapStringToCourtResponse(String cpResponse) {
        return objectMapper.readValue(cpResponse, CourtResponse.class);
    }

    public CourtHouseResponse mapCommonPlatformResponse(final CourtResponse cpCourtResponse, final UUID courtRoomId) {
        final CourtResponse.CourtRoom cpCourtroom = cpCourtResponse.getCourtrooms()
            .stream()
            .filter(cr -> cr.getId().equals(courtRoomId.toString()))
            .findFirst()
            .orElse(null);

        final Address address = Address.builder()
            .address1(cpCourtResponse.getAddress1())
            .address2(cpCourtResponse.getAddress2())
            .address3(cpCourtResponse.getAddress3())
            .address4(cpCourtResponse.getAddress4())
            .postalCode(cpCourtResponse.getPostcode())
            .country("UK")
            .build();

        final CourtRoom courtRoom = CourtRoom.builder()
            .courtRoomId(Integer.valueOf(cpCourtroom.getCourtroomId()))
            .courtRoomName(cpCourtroom.getCourtroomName())
            .build();

        return CourtHouseResponse.builder()
            .courtHouseType(getCourtHouseType(cpCourtResponse))
            .courtHouseCode(cpCourtResponse.getOucode())
            .courtHouseName(cpCourtResponse.getOucodeL3Name())
            .address(address)
            .courtRoom(Arrays.asList(courtRoom))
            .build();
    }

    private CourtHouseResponse.CourtHouseTypeEnum getCourtHouseType(final CourtResponse cpCourtResponse) {
        return cpCourtResponse.getOucodeL1Name().contains("Magistrates") ?
            CourtHouseResponse.CourtHouseTypeEnum.MAGISTRATE :
            CourtHouseResponse.CourtHouseTypeEnum.CROWN;
    }
}
