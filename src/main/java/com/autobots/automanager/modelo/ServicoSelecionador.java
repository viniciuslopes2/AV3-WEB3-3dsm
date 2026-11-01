package com.autobots.automanager.modelo;

import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.repositorios.ServicoRepositorio;

@Component
public class ServicoSelecionador {
    public Servico selecionar(ServicoRepositorio repositorio, Long id) {
        if (id == null) {
            return null;
        }
        return repositorio.findById(id).orElse(null);
    }
}
