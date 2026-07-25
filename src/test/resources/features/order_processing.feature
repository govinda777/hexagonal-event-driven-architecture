# language: pt
Funcionalidade: Processamento de Pedidos de E-commerce (Order Flow)

  Como um sistema de e-commerce de larga escala
  Eu quero gerenciar o ciclo de vida dos pedidos por meio de uma máquina de estados guiada por eventos
  Para garantir que cada etapa (Pagamento, Estoque) seja executada de forma assíncrona e resiliente

  Cenário: Sucesso no processamento de ponta a ponta do fluxo de pedido
    Dado que um cliente "CUST-BDD-1" solicita a criação de um pedido no valor de "450.00"
    Quando o caso de uso de criação de pedido é executado
    Então o pedido deve ser salvo com o status inicial "CREATED"
    E a mensagem do evento deve conter o histórico contendo "[CREATED]"

    Quando a etapa de processamento de pagamento é acionada assincronamente
    Então o status do pedido deve evoluir para "PAYMENT_APPROVED"
    E o histórico deve conter o log de aprovação de pagamento "[PAYMENT] Approved"

    Quando a etapa de reserva de estoque é acionada assincronamente
    Então o status do pedido deve evoluir para "COMPLETED"
    E o histórico deve conter o log de estoque reservado "[INVENTORY] Reserved items"

  Cenário: Transição para estado FAILED em caso de erro ou exceção
    Dado que existe um pedido com status inválido ou forçado a falhar
    Quando o caso de uso de processamento de etapa tenta executar uma etapa sem estratégia válida
    Então o status do pedido deve evoluir para "FAILED"
    E o histórico do pedido deve conter a mensagem de falha "[FAILED]"
