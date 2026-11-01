package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.repositorios.CredencialRepositorio;

@Component
public class CredencialSelecionador {
    public Credencial selecionar(CredencialRepositorio repositorio, Long id) {
        if (id == null) {
            return null;
        }
        return repositorio.findById(id).orElse(null);
    }
}
