package com.autobots.automanager.modelo;

public class StringVerificadorNulo {
    public boolean verificar(String dado) {
        return dado == null || dado.isBlank();
    }
}
