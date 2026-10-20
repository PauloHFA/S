package com.example.solid.after.notification;

import com.example.solid.after.domain.Pedido;

public class NotificadorWhatsApp implements Notificador {
    // Responsabilidade única: notificação de pedidos via WhatsApp
    @Override
    public void enviarConfirmacao(Pedido pedido) {
        // Simula envio via WhatsApp Business API
        System.out.println("[WHATSAPP] Enviando para " + pedido.getCliente().getEmail() 
                + ": ✅ Pedido confirmado! Total: R$ " + pedido.calcularTotal());
    }
}