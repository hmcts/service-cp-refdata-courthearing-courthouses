package uk.gov.hmcts.cp.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.domain.CourtResponse;
import uk.gov.hmcts.cp.openapi.model.Address;
import uk.gov.hmcts.cp.openapi.model.CourtHouseResponse;
import uk.gov.hmcts.cp.openapi.model.CourtRoom;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CourtHouseMapper {

    private static final String COUNTRY_UK = "UK";

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public CourtResponse mapStringToCourtResponse(String cpResponse) {
        return objectMapper.readValue(cpResponse, CourtResponse.class);
    }

    public CourtHouseResponse mapCourtHouseCPResponseWithCourtRoomId(final CourtResponse cpCourtResponse,
                                                                     final UUID courtRoomId) {
        List<CourtRoom> courtRooms = Optional.ofNullable(cpCourtResponse.getCourtrooms())
            .orElse(Collections.emptyList())
            .stream()
            .filter(cr -> cr.getId().equals(courtRoomId.toString()))
            .findFirst()
            .stream()
            .map(cr -> mapCpCourtRoomToCourtRoom(cr))
            .collect(Collectors.toList());

        return buildCourtHouseResponse(cpCourtResponse, courtRooms);
    }

    public CourtHouseResponse mapCPResponseToCourtHouse(final CourtResponse cpCourtResponse) {
        return buildCourtHouseResponse(cpCourtResponse, Collections.emptyList());
    }

    private CourtHouseResponse buildCourtHouseResponse(final CourtResponse cpCourtResponse,
                                                       final List<CourtRoom> courtRooms) {
        return CourtHouseResponse.builder()
            .courtHouseType(getCourtHouseType(cpCourtResponse))
            .courtHouseCode(cpCourtResponse.getOucode())
            .courtHouseName(cpCourtResponse.getOucodeL3Name())
            .address(buildAddress(cpCourtResponse))
            .courtRoom(courtRooms)
            .build();
    }

    private CourtRoom mapCpCourtRoomToCourtRoom(final CourtResponse.CourtRoom cpCourtRoom) {
        return CourtRoom.builder()
            .courtRoomId(Integer.valueOf(cpCourtRoom.getCourtroomId()))
            .courtRoomName(cpCourtRoom.getCourtroomName())
            .build();
    }

    private Address buildAddress(final CourtResponse cpCourtResponse) {
        return Address.builder()
            .address1(cpCourtResponse.getAddress1())
            .address2(cpCourtResponse.getAddress2())
            .address3(cpCourtResponse.getAddress3())
            .address4(cpCourtResponse.getAddress4())
            .postalCode(cpCourtResponse.getPostcode())
            .country(COUNTRY_UK)
            .build();
    }

    private CourtHouseResponse.CourtHouseTypeEnum getCourtHouseType(final CourtResponse cpCourtResponse) {
        return cpCourtResponse.getOucodeL1Name().contains("Magistrates") ?
            CourtHouseResponse.CourtHouseTypeEnum.MAGISTRATE :
            CourtHouseResponse.CourtHouseTypeEnum.CROWN;
    }
}
