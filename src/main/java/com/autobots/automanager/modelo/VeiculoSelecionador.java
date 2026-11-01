package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.repositorios.VeiculoRepositorio;

@Component
public class VeiculoSelecionador {
    public Veiculo selecionar(VeiculoRepositorio repositorio, Long id) {
        if (id == null) {
            return null;
        }
        return repositorio.findById(id).orElse(null);
    }
}
