package com.example.solid.after.infra;

import com.example.solid.after.domain.Pedido;

public class PedidoRepository {
    // Responsabilidade única: persistência de pedidos
    public void salvar(Pedido pedido) {
        // Simula salvamento no banco
        System.out.println("INSERT INTO pedidos (cliente_id, total) VALUES (" 
                + pedido.getCliente().getId() + ", " + pedido.calcularTotal() + ")");
    }
}