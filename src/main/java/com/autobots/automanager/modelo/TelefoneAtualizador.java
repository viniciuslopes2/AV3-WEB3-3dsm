package com.autobots.automanager.modelo;

import java.util.HashSet;
import java.util.Set;

import com.autobots.automanager.entidades.Telefone;

public class TelefoneAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Telefone telefone, Telefone atualizacao) {
        if (!verificador.verificar(atualizacao.getDdd())) {
            telefone.setDdd(atualizacao.getDdd());
        }
        if (!verificador.verificar(atualizacao.getNumero())) {
            telefone.setNumero(atualizacao.getNumero());
        }
    }

    public void atualizar(Set<Telefone> telefones, Set<Telefone> atualizacoes) {
        if (atualizacoes == null) {
            return;
        }
        Set<Telefone> novos = new HashSet<>();
        for (Telefone atualizacao : atualizacoes) {
            if (atualizacao.getId() == null) {
                novos.add(atualizacao);
            } else {
                for (Telefone telefone : telefones) {
                    if (atualizacao.getId().equals(telefone.getId())) {
                        atualizar(telefone, atualizacao);
                    }
                }
            }
        }
        telefones.addAll(novos);
    }
}
