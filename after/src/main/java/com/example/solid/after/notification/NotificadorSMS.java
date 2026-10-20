package com.example.solid.after.notification;

import com.example.solid.after.domain.Pedido;

public class NotificadorSMS implements Notificador {
    // Responsabilidade única: notificação de pedidos via SMS
    @Override
    public void enviarConfirmacao(Pedido pedido) {
        // Simula envio de SMS via gateway
        System.out.println("[SMS] Enviando para " + pedido.getCliente().getEmail() 
                + ": Pedido confirmado. Total: R$ " + pedido.calcularTotal());
    }
}