package com.autobots.automanager.modelo;

import java.util.HashSet;
import java.util.Set;

import com.autobots.automanager.entidades.Documento;

public class DocumentoAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Documento documento, Documento atualizacao) {
        if (atualizacao.getTipo() != null) {
            documento.setTipo(atualizacao.getTipo());
        }
        if (atualizacao.getDataEmissao() != null) {
            documento.setDataEmissao(atualizacao.getDataEmissao());
        }
        if (!verificador.verificar(atualizacao.getNumero())) {
            documento.setNumero(atualizacao.getNumero());
        }
    }

    public void atualizar(Set<Documento> documentos, Set<Documento> atualizacoes) {
        if (atualizacoes == null) {
            return;
        }
        Set<Documento> novos = new HashSet<>();
        for (Documento atualizacao : atualizacoes) {
            if (atualizacao.getId() == null) {
                novos.add(atualizacao);
            } else {
                for (Documento documento : documentos) {
                    if (atualizacao.getId().equals(documento.getId())) {
                        atualizar(documento, atualizacao);
                    }
                }
            }
        }
        documentos.addAll(novos);
    }
}
