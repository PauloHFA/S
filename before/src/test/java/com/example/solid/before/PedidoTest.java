package com.example.solid.before;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    @Test
    void calcularTotal_comClienteVip_deveAplicarDesconto10Porcento() {
        Cliente cliente = new Cliente("1", "João VIP", "joao@email.com", true);
        List<Item> itens = Arrays.asList(
                new Item("Produto A", 100.0, 1),
                new Item("Produto B", 50.0, 2)
        );
        Pedido pedido = new Pedido(itens, cliente);

        double total = pedido.calcularTotal();

        // Subtotal = 100 + 100 = 200, com 10% desconto = 180
        assertEquals(180.0, total, 0.001);
    }

    @Test
    void calcularTotal_comClienteNaoVip_deveNaoAplicarDesconto() {
        Cliente cliente = new Cliente("2", "Maria Comum", "maria@email.com", false);
        List<Item> itens = Arrays.asList(
                new Item("Produto A", 100.0, 1),
                new Item("Produto B", 50.0, 2)
        );
        Pedido pedido = new Pedido(itens, cliente);

        double total = pedido.calcularTotal();

        // Subtotal = 200, sem desconto = 200
        assertEquals(200.0, total, 0.001);
    }

    @Test
    void calcularTotal_comListaVazia_deveRetornarZero() {
        Cliente cliente = new Cliente("3", "Cliente Teste", "teste@email.com", false);
        Pedido pedido = new Pedido(Arrays.asList(), cliente);

        double total = pedido.calcularTotal();

        assertEquals(0.0, total, 0.001);
    }
}