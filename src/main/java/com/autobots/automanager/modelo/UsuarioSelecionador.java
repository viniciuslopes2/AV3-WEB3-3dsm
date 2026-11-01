package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@Component
public class UsuarioSelecionador {
    public Usuario selecionar(UsuarioRepositorio repositorio, Long id) {
        if (id == null) {
            return null;
        }
        return repositorio.findById(id).orElse(null);
    }
}
