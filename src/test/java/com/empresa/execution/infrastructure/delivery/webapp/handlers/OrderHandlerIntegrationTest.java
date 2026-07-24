package com.empresa.execution.infrastructure.delivery.webapp.handlers;

import com.empresa.execution.ExecutionFlowApplication;
import com.empresa.execution.infrastructure.delivery.webapp.requests.CreateOrderRequest;
import com.empresa.execution.infrastructure.repository.SpringDataOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExecutionFlowApplication.class, properties = {
        "spring.autoconfigure.exclude=io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration"
})
@AutoConfigureMockMvc
class OrderHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataOrderRepository repository;

    @MockBean
    private SqsTemplate sqsTemplate;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("CUST-1", new BigDecimal("150.00"));

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.customerId").value("CUST-1"))
                .andExpect(jsonPath("$.totalAmount").value(150.00))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.historyLog", containsString("[CREATED]")));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingOrderWithInvalidData() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("", new BigDecimal("-1.00"));

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        CreateOrderRequest createRequest = new CreateOrderRequest("CUST-2", new BigDecimal("99.99"));

        String responseJson = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID orderId = UUID.fromString(objectMapper.readTree(responseJson).get("id").asText());

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerId").value("CUST-2"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/orders/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
