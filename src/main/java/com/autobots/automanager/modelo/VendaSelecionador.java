package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.repositorios.VendaRepositorio;

@Component
public class VendaSelecionador {
    public Venda selecionar(VendaRepositorio repositorio, Long id) {
        if (id == null) {
            return null;
        }
        return repositorio.findById(id).orElse(null);
    }
}
