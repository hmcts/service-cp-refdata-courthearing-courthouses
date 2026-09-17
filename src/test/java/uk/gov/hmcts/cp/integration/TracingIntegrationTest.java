package uk.gov.hmcts.cp.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.cp.security.TestJwksConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Captures System.out and asserts on the exact log lines one request emits, so it needs a
// context of its own - the marker property below keeps it out of the shared one.
@SpringBootTest(properties = {
    "auth.mode=ENFORCE",
    "auth.tenant-id=11111111-1111-1111-1111-111111111111",
    "auth.audience=22222222-2222-2222-2222-222222222222",
    "auth.roles=CourtHouses.Read.All",
    "test.stdout-capture=tracing"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestJwksConfig.class)
@Slf4j
public class TracingIntegrationTest {

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";

    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.application.name}")
    private String springApplicationName;

    private PrintStream originalStdOut = System.out;

    @AfterEach
    void afterEach() {
        System.setOut(originalStdOut);
    }

    @Test
    void incoming_request_should_add_new_tracing() throws Exception {
        ByteArrayOutputStream capturedStdOut = captureStdOut();
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String loggedMessage = capturedStdOut.toString();
        assertThat(loggedMessage).isNotEmpty();
        Map<String, Object> capturedFields = new ObjectMapper().readValue(loggedMessage, new TypeReference<>() {
        });
        assertThat(capturedFields.get(TRACE_ID)).isNotNull();
        assertThat(capturedFields.get(SPAN_ID)).isNotNull();
        assertThat(capturedFields.get("logger_name")).isEqualTo("uk.gov.hmcts.cp.controllers.RootController");
        assertThat(capturedFields.get("message")).isEqualTo("START\n");
    }

    @Test
    void incoming_request_with_traceId_should_pass_through() throws Exception {
        ByteArrayOutputStream capturedStdOut = captureStdOut();
        MvcResult result = mockMvc.perform(get("/")
                        .header(TRACE_ID, "1234-1234")
                        .header(SPAN_ID, "567-567"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String loggedMessage = capturedStdOut.toString();
        assertThat(loggedMessage).isNotEmpty();
        Map<String, Object> capturedFields = new ObjectMapper().readValue(loggedMessage, new TypeReference<>() {
        });
        assertThat(capturedFields.get(TRACE_ID)).isEqualTo("1234-1234");
        assertThat(capturedFields.get(SPAN_ID)).isEqualTo("567-567");
        assertThat(capturedFields.get("applicationName")).isEqualTo(springApplicationName);

        assertThat(result.getResponse().getHeader(TRACE_ID)).isEqualTo(capturedFields.get(TRACE_ID));
        assertThat(result.getResponse().getHeader(SPAN_ID)).isEqualTo(capturedFields.get(SPAN_ID));
    }

    private ByteArrayOutputStream captureStdOut() {
        final ByteArrayOutputStream capturedStdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedStdOut));
        return capturedStdOut;
    }
}
