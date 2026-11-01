package com.autobots.automanager.modelo;

import java.util.HashSet;
import java.util.Set;

import com.autobots.automanager.entidades.Email;

public class EmailAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Email email, Email atualizacao) {
        if (!verificador.verificar(atualizacao.getEndereco())) {
            email.setEndereco(atualizacao.getEndereco());
        }
    }

    public void atualizar(Set<Email> emails, Set<Email> atualizacoes) {
        if (atualizacoes == null) {
            return;
        }
        Set<Email> novos = new HashSet<>();
        for (Email atualizacao : atualizacoes) {
            if (atualizacao.getId() == null) {
                novos.add(atualizacao);
            } else {
                for (Email email : emails) {
                    if (atualizacao.getId().equals(email.getId())) {
                        atualizar(email, atualizacao);
                    }
                }
            }
        }
        emails.addAll(novos);
    }
}
