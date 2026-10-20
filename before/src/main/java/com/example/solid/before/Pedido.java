package com.example.solid.before;

import java.util.List;

public class Pedido {
    private final List<Item> itens;
    private final Cliente cliente;

    public Pedido(List<Item> itens, Cliente cliente) {
        this.itens = itens;
        this.cliente = cliente;
    }

    public List<Item> getItens() {
        return itens;
    }

    public Cliente getCliente() {
        return cliente;
    }

    // 1) Regra de negócio - calcular total com desconto VIP
    public double calcularTotal() {
        double subtotal = itens.stream()
                .mapToDouble(Item::getSubtotal)
                .sum();
        return cliente.isVip() ? subtotal * 0.9 : subtotal;
    }

    // 2) Persistência - salva no banco de dados
    public void salvar() {
        // Simula salvamento no banco
        System.out.println("INSERT INTO pedidos (cliente_id, total) VALUES (" 
                + cliente.getId() + ", " + calcularTotal() + ")");
    }

    // 3) Notificação - envia e-mail de confirmação
    public void enviarEmailConfirmacao() {
        // Simula envio de e-mail via SMTP
        System.out.println("Enviando e-mail para " + cliente.getEmail() 
                + ": Pedido confirmado. Total: " + calcularTotal());
    }

    // 4) Relatório - gera nota fiscal em HTML
    public String gerarNotaHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<h1>Nota Fiscal</h1>");
        html.append("<p>Cliente: ").append(cliente.getNome()).append("</p>");
        html.append("<ul>");
        for (Item item : itens) {
            html.append("<li>")
                .append(item.getNome())
                .append(" - ")
                .append(item.getQuantidade())
                .append("x R$ ")
                .append(item.getPreco())
                .append("</li>");
        }
        html.append("</ul>");
        html.append("<p><strong>Total: R$ ").append(calcularTotal()).append("</strong></p>");
        return html.toString();
    }
}