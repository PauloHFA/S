package com.example.solid.after.notification;

import com.example.solid.after.domain.Pedido;

public class NotificadorDePedido implements Notificador {
    // Responsabilidade única: notificação de pedidos via e-mail
    @Override
    public void enviarConfirmacao(Pedido pedido) {
        // Simula envio de e-mail via SMTP
        System.out.println("[EMAIL] Enviando para " + pedido.getCliente().getEmail() 
                + ": Pedido confirmado. Total: " + pedido.calcularTotal());
    }
}