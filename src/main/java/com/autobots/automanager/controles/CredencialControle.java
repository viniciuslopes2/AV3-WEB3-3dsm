package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelo.AdicionadorLinkCredencial;
import com.autobots.automanager.modelo.CredencialAtualizador;
import com.autobots.automanager.modelo.CredencialSelecionador;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.repositorios.CredencialRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;

@RestController
@RequestMapping("/credencial")
public class CredencialControle {
    @Autowired
    private CredencialRepositorio repositorio;
    @Autowired
    private CredencialSelecionador selecionador;
    @Autowired
    private AdicionadorLinkCredencial adicionadorLink;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @GetMapping("/credencial/{id}")
    public ResponseEntity<Credencial> obterCredencial(@PathVariable long id) {
        Credencial credencial = selecionador.selecionar(repositorio, id);
        if (credencial == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(credencial);
        return new ResponseEntity<>(credencial, HttpStatus.FOUND);
    }

    @GetMapping("/credenciais")
    public ResponseEntity<List<Credencial>> obterCredenciais() {
        List<Credencial> credenciais = repositorio.findAll();
        if (credenciais.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(credenciais);
        return new ResponseEntity<>(credenciais, HttpStatus.FOUND);
    }

    @PostMapping("/cadastro/{idUsuario}")
    @Transactional
    public ResponseEntity<?> cadastrarCredencial(@PathVariable long idUsuario, @RequestBody Credencial credencial) {
        if (credencial.getId() != null || jaExiste(credencial)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Usuario usuario = usuarioSelecionador.selecionar(usuarioRepositorio, idUsuario);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (credencial.getCriacao() == null) {
            credencial.setCriacao(new Date());
        }
        Credencial salva = repositorio.save(credencial);
        usuario.getCredenciais().add(salva);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarCredencial(@RequestBody Credencial atualizacao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Credencial credencial = selecionador.selecionar(repositorio, atualizacao.getId());
        if (credencial != null) {
            CredencialAtualizador atualizador = new CredencialAtualizador();
            atualizador.atualizar(credencial, atualizacao);
            repositorio.save(credencial);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirCredencial(@RequestBody Credencial exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Credencial credencial = selecionador.selecionar(repositorio, exclusao.getId());
        if (credencial != null) {
            Long id = credencial.getId();
            boolean vinculada = false;
            for (Usuario usuario : usuarioRepositorio.findAll()) {
                if (usuario.getCredenciais().removeIf(item -> item.getId().equals(id))) {
                    vinculada = true;
                }
            }
            if (!vinculada) {
                repositorio.delete(credencial);
            }
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    private boolean jaExiste(Credencial nova) {
        for (Credencial existente : repositorio.findAll()) {
            if (nova instanceof CredencialUsuarioSenha novaUsuarioSenha
                    && existente instanceof CredencialUsuarioSenha usuarioSenha
                    && usuarioSenha.getNomeUsuario().equals(novaUsuarioSenha.getNomeUsuario())) {
                return true;
            }
            if (nova instanceof CredencialCodigoBarra novoCodigoBarra
                    && existente instanceof CredencialCodigoBarra codigoBarra
                    && codigoBarra.getCodigo() == novoCodigoBarra.getCodigo()) {
                return true;
            }
        }
        return false;
    }
}
