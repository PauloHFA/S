package com.example.solid.after.application;

import com.example.solid.after.domain.Pedido;
import com.example.solid.after.infra.PedidoRepository;
import com.example.solid.after.notification.Notificador;

public class FinalizarPedido {
    private final PedidoRepository repository;
    private final Notificador notificador;

    // Injeção de dependência pelo construtor (prepara para o D do SOLID)
    // Depende de abstração (Notificador), não de implementação concreta
    public FinalizarPedido(PedidoRepository repository, Notificador notificador) {
        this.repository = repository;
        this.notificador = notificador;
    }

    // Responsabilidade única: orquestrar o fluxo do caso de uso "finalizar pedido"
    public void executar(Pedido pedido) {
        repository.salvar(pedido);
        notificador.enviarConfirmacao(pedido);
    }
}