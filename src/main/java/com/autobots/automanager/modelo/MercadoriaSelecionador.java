package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;

@Component
public class MercadoriaSelecionador {
    public Mercadoria selecionar(MercadoriaRepositorio repositorio, Long id) {
        if (id == null) {
            return null;
        }
        return repositorio.findById(id).orElse(null);
    }
}
