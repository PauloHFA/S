package com.example.solid.after.notification;

import com.example.solid.after.domain.Pedido;

public interface Notificador {
    void enviarConfirmacao(Pedido pedido);
}