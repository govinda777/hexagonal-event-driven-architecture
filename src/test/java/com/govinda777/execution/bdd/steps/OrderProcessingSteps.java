package com.govinda777.execution.bdd.steps;

import com.govinda777.execution.ExecutionFlowApplication;
import com.govinda777.execution.business.domain.OrderEntity;
import com.govinda777.execution.business.domain.OrderStatus;
import com.govinda777.execution.business.usecase.CreateOrderUseCase;
import com.govinda777.execution.business.usecase.ExecuteOrderStepUseCase;
import com.govinda777.execution.business.gateway.ProcessRepositoryGateway;
import com.govinda777.execution.business.gateway.ProcessPublisherGateway;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@CucumberContextConfiguration
@SpringBootTest(classes = ExecutionFlowApplication.class, properties = {
        "spring.autoconfigure.exclude=io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration"
})
public class OrderProcessingSteps {

    @Autowired
    private CreateOrderUseCase createOrderUseCase;

    @Autowired
    private ExecuteOrderStepUseCase executeOrderStepUseCase;

    @Autowired
    private ProcessRepositoryGateway repositoryGateway;

    @MockBean
    private SqsTemplate sqsTemplate; // Mocked SqsTemplate to avoid real SQS connection

    @MockBean
    private ProcessPublisherGateway publisherGateway; // Mocked publisher

    private String customerId;
    private BigDecimal amount;
    private OrderEntity order;
    private UUID invalidOrderId;

    @Dado("que um cliente {string} solicita a criação de um pedido no valor de {string}")
    public void queUmClienteSolicitaACriacaoDeUmPedidoNoValorDe(String customerId, String amount) {
        this.customerId = customerId;
        this.amount = new BigDecimal(amount);
    }

    @Quando("o caso de uso de criação de pedido é executado")
    public void oCasoDeUsoDeCriacaoDePedidoEExecutado() {
        this.order = createOrderUseCase.execute(this.customerId, this.amount);
    }

    @Então("o pedido deve ser salvo com o status inicial {string}")
    public void oPedidoDeveSerSalvoComOStatusInicial(String expectedStatus) {
        assertNotNull(order);
        assertEquals(OrderStatus.valueOf(expectedStatus), order.getStatus());
    }

    @E("a mensagem do evento deve conter o histórico contendo {string}")
    public void aMensagemDoEventoDeveConterOHistoricoContendo(String expectedHistoryPart) {
        assertTrue(order.getHistoryLog().contains(expectedHistoryPart));
    }

    @Quando("a etapa de processamento de pagamento é acionada assincronamente")
    public void aEtapaDeProcessamentoDePagamentoEAcionadaAssincronamente() {
        // Run first state transition CREATED -> PAYMENT_APPROVED
        executeOrderStepUseCase.execute(order.getId());
        // Reload order state from fake/in-memory repo
        order = repositoryGateway.findById(order.getId()).orElseThrow();
    }

    @Então("o status do pedido deve evoluir para {string}")
    public void oStatusDoPedidoDeveEvoluirPara(String expectedStatus) {
        assertEquals(OrderStatus.valueOf(expectedStatus), order.getStatus());
    }

    @E("o histórico deve conter o log de aprovação de pagamento {string}")
    public void oHistoricoDeveConterOLogDeAprovacaoDePagamento(String expectedHistoryLog) {
        assertTrue(order.getHistoryLog().contains(expectedHistoryLog));
    }

    @Quando("a etapa de reserva de estoque é acionada assincronamente")
    public void aEtapaDeReservaDeEstoqueEAcionadaAssincronamente() {
        // Run second state transition PAYMENT_APPROVED -> COMPLETED
        executeOrderStepUseCase.execute(order.getId());
        order = repositoryGateway.findById(order.getId()).orElseThrow();
    }

    @E("o histórico deve conter o log de estoque reservado {string}")
    public void oHistoricoDeveConterOLogDeEstoqueReservado(String expectedHistoryLog) {
        assertTrue(order.getHistoryLog().contains(expectedHistoryLog));
    }

    @Dado("que existe um pedido com status inválido ou forçado a falhar")
    public void queExisteUmPedidoComStatusInvalidoOuForcadoAFalhar() {
        this.invalidOrderId = UUID.randomUUID();
        // Create an order directly inside the repository with a forced state
        OrderEntity invalidOrder = new OrderEntity();
        invalidOrder.setId(invalidOrderId);
        invalidOrder.setCustomerId("CUST-ERR");
        invalidOrder.setTotalAmount(BigDecimal.ONE);
        invalidOrder.setStatus(OrderStatus.COMPLETED); // Completed state has no strategy
        invalidOrder.setCreatedAt(LocalDateTime.now());
        invalidOrder.setUpdatedAt(LocalDateTime.now());
        invalidOrder.setHistoryLog("[CREATED]");
        repositoryGateway.save(invalidOrder);
    }

    @Quando("o caso de uso de processamento de etapa tenta executar uma etapa sem estratégia válida")
    public void oCasoDeUsoDeProcessamentoDeEtapaTentaExecutarUmaEtapaSemEstrategiaValida() {
        // Create an order in CREATED status
        OrderEntity failedOrder = new OrderEntity();
        UUID failedOrderId = UUID.randomUUID();
        failedOrder.setId(failedOrderId);
        failedOrder.setCustomerId("CUST-FAIL");
        failedOrder.setTotalAmount(BigDecimal.TEN);
        failedOrder.setStatus(OrderStatus.CREATED);
        failedOrder.setCreatedAt(LocalDateTime.now());
        failedOrder.setUpdatedAt(LocalDateTime.now());
        failedOrder.setHistoryLog("[CREATED]");
        repositoryGateway.save(failedOrder);

        // Instruct our mock publisher to throw an exception when called during step execution
        doThrow(new RuntimeException("SQS Connection Failed")).when(publisherGateway).publish(any());

        executeOrderStepUseCase.execute(failedOrderId);
        this.order = repositoryGateway.findById(failedOrderId).orElseThrow();
    }

    @E("o histórico do pedido deve conter a mensagem de falha {string}")
    public void oHistoricoDoPedidoDeveConterAMensagemDeFalha(String expectedHistoryPart) {
        assertTrue(order.getHistoryLog().contains(expectedHistoryPart));
    }
}
