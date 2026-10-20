package com.example.solid.after.notification;

import com.example.solid.after.domain.Pedido;

public class NotificadorDePedido {
    // Responsabilidade única: notificação de pedidos
    public void enviarConfirmacao(Pedido pedido) {
        // Simula envio de e-mail via SMTP
        System.out.println("Enviando e-mail para " + pedido.getCliente().getEmail() 
                + ": Pedido confirmado. Total: " + pedido.calcularTotal());
    }
}