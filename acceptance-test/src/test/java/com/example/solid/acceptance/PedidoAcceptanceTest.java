package com.example.solid.acceptance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de aceitação compartilhado que valida equivalência comportamental
 * entre a versão BEFORE (viola SRP) e AFTER (respeita SRP).
 * 
 * Este teste roda DUAS VEZES - uma para cada implementação - provando que
 * o comportamento externo é idêntico apesar da arquitetura interna diferente.
 */
class PedidoAcceptanceTest {

    enum Versao {
        BEFORE,
        AFTER
    }

    // Factory para criar o sistema sob teste baseado na versão
    private SistemaPedido criarSistema(Versao versao) {
        return switch (versao) {
            case BEFORE -> new SistemaPedidoBefore();
            case AFTER -> new SistemaPedidoAfter();
        };
    }

    @ParameterizedTest(name = "Versão {0}: Calcular total com cliente VIP deve aplicar desconto 10%")
    @EnumSource(Versao.class)
    void calcularTotal_comClienteVip_deveAplicarDesconto10Porcento(Versao versao) {
        SistemaPedido sistema = criarSistema(versao);
        
        Cliente cliente = sistema.criarCliente("1", "João VIP", "joao@email.com", true);
        List<Item> itens = Arrays.asList(
                sistema.criarItem("Produto A", 100.0, 1),
                sistema.criarItem("Produto B", 50.0, 2)
        );
        Pedido pedido = sistema.criarPedido(itens, cliente);

        double total = sistema.calcularTotal(pedido);

        // Subtotal = 100 + 100 = 200, com 10% desconto = 180
        assertEquals(180.0, total, 0.001, "Versão " + versao + ": total incorreto");
    }

    @ParameterizedTest(name = "Versão {0}: Calcular total com cliente não-VIP não deve aplicar desconto")
    @EnumSource(Versao.class)
    void calcularTotal_comClienteNaoVip_deveNaoAplicarDesconto(Versao versao) {
        SistemaPedido sistema = criarSistema(versao);
        
        Cliente cliente = sistema.criarCliente("2", "Maria Comum", "maria@email.com", false);
        List<Item> itens = Arrays.asList(
                sistema.criarItem("Produto A", 100.0, 1),
                sistema.criarItem("Produto B", 50.0, 2)
        );
        Pedido pedido = sistema.criarPedido(itens, cliente);

        double total = sistema.calcularTotal(pedido);

        // Subtotal = 200, sem desconto = 200
        assertEquals(200.0, total, 0.001, "Versão " + versao + ": total incorreto");
    }

    @ParameterizedTest(name = "Versão {0}: Calcular total com lista vazia deve retornar zero")
    @EnumSource(Versao.class)
    void calcularTotal_comListaVazia_deveRetornarZero(Versao versao) {
        SistemaPedido sistema = criarSistema(versao);
        
        Cliente cliente = sistema.criarCliente("3", "Cliente Teste", "teste@email.com", false);
        Pedido pedido = sistema.criarPedido(Arrays.asList(), cliente);

        double total = sistema.calcularTotal(pedido);

        assertEquals(0.0, total, 0.001, "Versão " + versao + ": total incorreto");
    }

    @ParameterizedTest(name = "Versão {0}: Finalizar pedido deve salvar e notificar sem erros")
    @EnumSource(Versao.class)
    void finalizarPedido_deveExecutarFluxoCompletoSemErros(Versao versao) {
        SistemaPedido sistema = criarSistema(versao);
        
        Cliente cliente = sistema.criarCliente("4", "Cliente Teste", "teste@email.com", true);
        List<Item> itens = Arrays.asList(sistema.criarItem("Produto X", 50.0, 1));
        Pedido pedido = sistema.criarPedido(itens, cliente);

        // Não deve lançar exceção
        assertDoesNotThrow(() -> sistema.finalizarPedido(pedido), 
                "Versão " + versao + ": fluxo de finalizar pedido falhou");
    }

    @ParameterizedTest(name = "Versão {0}: Gerar nota HTML deve conter informações do pedido")
    @EnumSource(Versao.class)
    void gerarNotaHtml_deveConterInformacoesDoPedido(Versao versao) {
        SistemaPedido sistema = criarSistema(versao);
        
        Cliente cliente = sistema.criarCliente("5", "Cliente Nota", "nota@email.com", false);
        List<Item> itens = Arrays.asList(
                sistema.criarItem("Item 1", 10.0, 2),
                sistema.criarItem("Item 2", 25.0, 1)
        );
        Pedido pedido = sistema.criarPedido(itens, cliente);

        String notaHtml = sistema.gerarNotaHtml(pedido);

        assertNotNull(notaHtml);
        assertTrue(notaHtml.contains("Nota Fiscal"));
        assertTrue(notaHtml.contains("Cliente Nota"));
        assertTrue(notaHtml.contains("Item 1"));
        assertTrue(notaHtml.contains("Item 2"));
        assertTrue(notaHtml.contains("Total"));
    }

    // ===== Interfaces comuns para ambas versões =====

    interface SistemaPedido {
        Cliente criarCliente(String id, String nome, String email, boolean vip);
        Item criarItem(String nome, double preco, int quantidade);
        Pedido criarPedido(List<Item> itens, Cliente cliente);
        double calcularTotal(Pedido pedido);
        void finalizarPedido(Pedido pedido);
        String gerarNotaHtml(Pedido pedido);
    }

    interface Cliente {
        String getId();
        String getNome();
        String getEmail();
        boolean isVip();
    }

    interface Item {
        String getNome();
        double getPreco();
        int getQuantidade();
        double getSubtotal();
    }

    interface Pedido {
        List<Item> getItens();
        Cliente getCliente();
    }

    // ===== Implementação BEFORE =====

    static class SistemaPedidoBefore implements SistemaPedido {
        @Override
        public Cliente criarCliente(String id, String nome, String email, boolean vip) {
            return new BeforeClienteAdapter(id, nome, email, vip);
        }

        @Override
        public Item criarItem(String nome, double preco, int quantidade) {
            return new BeforeItemAdapter(nome, preco, quantidade);
        }

        @Override
        public Pedido criarPedido(List<Item> itens, Cliente cliente) {
            // Converter adapters para classes before reais
            List<com.example.solid.before.Item> itensBefore = itens.stream()
                    .map(i -> (BeforeItemAdapter) i)
                    .map(BeforeItemAdapter::getOriginal)
                    .toList();
            
            com.example.solid.before.Cliente clienteBefore = 
                    ((BeforeClienteAdapter) cliente).getOriginal();
            
            com.example.solid.before.Pedido pedidoBefore = 
                    new com.example.solid.before.Pedido(itensBefore, clienteBefore);
            
            return new BeforePedidoAdapter(pedidoBefore);
        }

        @Override
        public double calcularTotal(Pedido pedido) {
            return ((BeforePedidoAdapter) pedido).getOriginal().calcularTotal();
        }

        @Override
        public void finalizarPedido(Pedido pedido) {
            ((BeforePedidoAdapter) pedido).getOriginal().salvar();
            ((BeforePedidoAdapter) pedido).getOriginal().enviarEmailConfirmacao();
        }

        @Override
        public String gerarNotaHtml(Pedido pedido) {
            return ((BeforePedidoAdapter) pedido).getOriginal().gerarNotaHtml();
        }
    }

    static class BeforeClienteAdapter implements Cliente {
        private final com.example.solid.before.Cliente original;
        
        BeforeClienteAdapter(String id, String nome, String email, boolean vip) {
            this.original = new com.example.solid.before.Cliente(id, nome, email, vip);
        }
        
        BeforeClienteAdapter(com.example.solid.before.Cliente original) {
            this.original = original;
        }
        
        com.example.solid.before.Cliente getOriginal() { return original; }
        
        @Override public String getId() { return original.getId(); }
        @Override public String getNome() { return original.getNome(); }
        @Override public String getEmail() { return original.getEmail(); }
        @Override public boolean isVip() { return original.isVip(); }
    }

    static class BeforeItemAdapter implements Item {
        private final com.example.solid.before.Item original;
        
        BeforeItemAdapter(String nome, double preco, int quantidade) {
            this.original = new com.example.solid.before.Item(nome, preco, quantidade);
        }
        
        BeforeItemAdapter(com.example.solid.before.Item original) {
            this.original = original;
        }
        
        com.example.solid.before.Item getOriginal() { return original; }
        
        @Override public String getNome() { return original.getNome(); }
        @Override public double getPreco() { return original.getPreco(); }
        @Override public int getQuantidade() { return original.getQuantidade(); }
        @Override public double getSubtotal() { return original.getSubtotal(); }
    }

    static class BeforePedidoAdapter implements Pedido {
        private final com.example.solid.before.Pedido original;
        
        BeforePedidoAdapter(com.example.solid.before.Pedido original) {
            this.original = original;
        }
        
        com.example.solid.before.Pedido getOriginal() { return original; }
        
        @Override public List<Item> getItens() {
            return original.getItens().stream()
                    .map(BeforeItemAdapter::new)
                    .toList();
        }
        
        @Override public Cliente getCliente() {
            return new BeforeClienteAdapter(original.getCliente());
        }
    }

    // ===== Implementação AFTER =====

    static class SistemaPedidoAfter implements SistemaPedido {
        @Override
        public Cliente criarCliente(String id, String nome, String email, boolean vip) {
            return new AfterClienteAdapter(id, nome, email, vip);
        }

        @Override
        public Item criarItem(String nome, double preco, int quantidade) {
            return new AfterItemAdapter(nome, preco, quantidade);
        }

        @Override
        public Pedido criarPedido(List<Item> itens, Cliente cliente) {
            List<com.example.solid.after.domain.Item> itensAfter = itens.stream()
                    .map(i -> (AfterItemAdapter) i)
                    .map(AfterItemAdapter::getOriginal)
                    .toList();
            
            com.example.solid.after.domain.Cliente clienteAfter = 
                    ((AfterClienteAdapter) cliente).getOriginal();
            
            com.example.solid.after.domain.Pedido pedidoAfter = 
                    new com.example.solid.after.domain.Pedido(itensAfter, clienteAfter);
            
            return new AfterPedidoAdapter(pedidoAfter);
        }

        @Override
        public double calcularTotal(Pedido pedido) {
            return ((AfterPedidoAdapter) pedido).getOriginal().calcularTotal();
        }

        @Override
        public void finalizarPedido(Pedido pedido) {
            com.example.solid.after.domain.Pedido pedidoAfter = 
                    ((AfterPedidoAdapter) pedido).getOriginal();
            
            // Usa a aplicação com notificador de e-mail (comportamento padrão)
            com.example.solid.after.infra.PedidoRepository repo = 
                    new com.example.solid.after.infra.PedidoRepository();
            com.example.solid.after.notification.NotificadorDePedido notificador = 
                    new com.example.solid.after.notification.NotificadorDePedido();
            com.example.solid.after.application.FinalizarPedido casoDeUso = 
                    new com.example.solid.after.application.FinalizarPedido(repo, notificador);
            
            casoDeUso.executar(pedidoAfter);
        }

        @Override
        public String gerarNotaHtml(Pedido pedido) {
            com.example.solid.after.domain.Pedido pedidoAfter = 
                    ((AfterPedidoAdapter) pedido).getOriginal();
            
            com.example.solid.after.report.GeradorDeNota gerador = 
                    new com.example.solid.after.report.GeradorDeNota();
            return gerador.gerarHtml(pedidoAfter);
        }
    }

    static class AfterClienteAdapter implements Cliente {
        private final com.example.solid.after.domain.Cliente original;
        
        AfterClienteAdapter(String id, String nome, String email, boolean vip) {
            this.original = new com.example.solid.after.domain.Cliente(id, nome, email, vip);
        }
        
        AfterClienteAdapter(com.example.solid.after.domain.Cliente original) {
            this.original = original;
        }
        
        com.example.solid.after.domain.Cliente getOriginal() { return original; }
        
        @Override public String getId() { return original.getId(); }
        @Override public String getNome() { return original.getNome(); }
        @Override public String getEmail() { return original.getEmail(); }
        @Override public boolean isVip() { return original.isVip(); }
    }

    static class AfterItemAdapter implements Item {
        private final com.example.solid.after.domain.Item original;
        
        AfterItemAdapter(String nome, double preco, int quantidade) {
            this.original = new com.example.solid.after.domain.Item(nome, preco, quantidade);
        }
        
        AfterItemAdapter(com.example.solid.after.domain.Item original) {
            this.original = original;
        }
        
        com.example.solid.after.domain.Item getOriginal() { return original; }
        
        @Override public String getNome() { return original.getNome(); }
        @Override public double getPreco() { return original.getPreco(); }
        @Override public int getQuantidade() { return original.getQuantidade(); }
        @Override public double getSubtotal() { return original.getSubtotal(); }
    }

    static class AfterPedidoAdapter implements Pedido {
        private final com.example.solid.after.domain.Pedido original;
        
        AfterPedidoAdapter(com.example.solid.after.domain.Pedido original) {
            this.original = original;
        }
        
        com.example.solid.after.domain.Pedido getOriginal() { return original; }
        
        @Override public List<Item> getItens() {
            return original.getItens().stream()
                    .map(AfterItemAdapter::new)
                    .toList();
        }
        
        @Override public Cliente getCliente() {
            return new AfterClienteAdapter(original.getCliente());
        }
    }
}