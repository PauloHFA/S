package com.example.solid.after.report;

import com.example.solid.after.domain.Pedido;

public class GeradorDeNota {
    // Responsabilidade única: geração de relatório/nota fiscal
    public String gerarHtml(Pedido pedido) {
        StringBuilder html = new StringBuilder();
        html.append("<h1>Nota Fiscal</h1>");
        html.append("<p>Cliente: ").append(pedido.getCliente().getNome()).append("</p>");
        html.append("<ul>");
        for (var item : pedido.getItens()) {
            html.append("<li>")
                .append(item.getNome())
                .append(" - ")
                .append(item.getQuantidade())
                .append("x R$ ")
                .append(item.getPreco())
                .append("</li>");
        }
        html.append("</ul>");
        html.append("<p><strong>Total: R$ ").append(pedido.calcularTotal()).append("</strong></p>");
        return html.toString();
    }
}