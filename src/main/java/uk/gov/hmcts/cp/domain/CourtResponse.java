package uk.gov.hmcts.cp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CourtResponse {
    private String id;
    private String oucode;
    private String oucodeL1Name;
    private String oucodeL3Name;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postcode;
    private List<CourtRoom> courtrooms;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CourtRoom {
        private String id;
        private String venueName;
        private String courtroomId;
        private String courtroomName;
    }
}
