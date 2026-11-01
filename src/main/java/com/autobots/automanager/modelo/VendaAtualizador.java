package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Venda;

public class VendaAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Venda venda, Venda atualizacao) {
        if (!verificador.verificar(atualizacao.getIdentificacao())) {
            venda.setIdentificacao(atualizacao.getIdentificacao());
        }
        if (atualizacao.getCadastro() != null) {
            venda.setCadastro(atualizacao.getCadastro());
        }
    }
}
