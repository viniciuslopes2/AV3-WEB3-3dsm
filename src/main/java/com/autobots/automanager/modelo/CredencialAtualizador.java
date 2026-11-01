package com.autobots.automanager.modelo;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

public class CredencialAtualizador {
    private StringVerificadorNulo verificador = new StringVerificadorNulo();

    public void atualizar(Credencial credencial, Credencial atualizacao) {
        credencial.setInativo(atualizacao.isInativo());
        if (atualizacao.getUltimoAcesso() != null) {
            credencial.setUltimoAcesso(atualizacao.getUltimoAcesso());
        }
        if (credencial instanceof CredencialUsuarioSenha usuarioSenha
                && atualizacao instanceof CredencialUsuarioSenha novaUsuarioSenha) {
            if (!verificador.verificar(novaUsuarioSenha.getNomeUsuario())) {
                usuarioSenha.setNomeUsuario(novaUsuarioSenha.getNomeUsuario());
            }
            if (!verificador.verificar(novaUsuarioSenha.getSenha())) {
                usuarioSenha.setSenha(novaUsuarioSenha.getSenha());
            }
        }
        if (credencial instanceof CredencialCodigoBarra codigoBarra
                && atualizacao instanceof CredencialCodigoBarra novoCodigoBarra) {
            if (novoCodigoBarra.getCodigo() > 0) {
                codigoBarra.setCodigo(novoCodigoBarra.getCodigo());
            }
        }
    }
}
