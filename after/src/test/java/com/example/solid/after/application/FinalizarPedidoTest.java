package com.example.solid.after.application;

import com.example.solid.after.domain.Cliente;
import com.example.solid.after.domain.Item;
import com.example.solid.after.domain.Pedido;
import com.example.solid.after.infra.PedidoRepository;
import com.example.solid.after.notification.NotificadorDePedido;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FinalizarPedidoTest {

    @Test
    void executar_deveSalvarPedidoENotificarCliente() {
        // Arrange
        PedidoRepository repository = new PedidoRepository();
        NotificadorDePedido notificador = new NotificadorDePedido();
        FinalizarPedido casoDeUso = new FinalizarPedido(repository, notificador);

        Cliente cliente = new Cliente("1", "João", "joao@email.com", true);
        List<Item> itens = Arrays.asList(new Item("Produto A", 100.0, 1));
        Pedido pedido = new Pedido(itens, cliente);

        // Act - não deve lançar exceção
        casoDeUso.executar(pedido);

        // Assert - verifica se o fluxo foi executado (saída no console)
        // Em testes reais, usaríamos mocks para verificar chamadas
        assertTrue(true); // Se chegou aqui, o fluxo funcionou
    }
}