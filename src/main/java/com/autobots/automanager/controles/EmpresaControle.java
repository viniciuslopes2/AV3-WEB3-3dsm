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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelo.AdicionadorLinkEmpresa;
import com.autobots.automanager.modelo.EmpresaAtualizador;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@RestController
@RequestMapping("/empresa")
public class EmpresaControle {
    @Autowired
    private EmpresaRepositorio repositorio;
    @Autowired
    private EmpresaSelecionador selecionador;
    @Autowired
    private AdicionadorLinkEmpresa adicionadorLink;

    @GetMapping("/empresa/{id}")
    public ResponseEntity<Empresa> obterEmpresa(@PathVariable long id) {
        Empresa empresa = selecionador.selecionar(repositorio, id);
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(empresa);
        return new ResponseEntity<>(empresa, HttpStatus.FOUND);
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<Empresa>> obterEmpresas() {
        List<Empresa> empresas = repositorio.findAll();
        if (empresas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(empresas);
        return new ResponseEntity<>(empresas, HttpStatus.FOUND);
    }

    @PostMapping("/cadastro")
    @Transactional
    public ResponseEntity<?> cadastrarEmpresa(@RequestBody Empresa empresa) {
        HttpStatus status = HttpStatus.CONFLICT;
        if (empresa.getId() == null) {
            if (empresa.getCadastro() == null) {
                empresa.setCadastro(new Date());
            }
            empresa.getUsuarios().clear();
            empresa.getMercadorias().clear();
            empresa.getServicos().clear();
            empresa.getVendas().clear();
            repositorio.save(empresa);
            status = HttpStatus.CREATED;
        }
        return new ResponseEntity<>(status);
    }

    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarEmpresa(@RequestBody Empresa atualizacao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Empresa empresa = selecionador.selecionar(repositorio, atualizacao.getId());
        if (empresa != null) {
            EmpresaAtualizador atualizador = new EmpresaAtualizador();
            atualizador.atualizar(empresa, atualizacao);
            repositorio.save(empresa);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirEmpresa(@RequestBody Empresa exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Empresa empresa = selecionador.selecionar(repositorio, exclusao.getId());
        if (empresa != null) {
            empresa.getUsuarios().clear();
            empresa.getMercadorias().clear();
            empresa.getServicos().clear();
            empresa.getVendas().clear();
            repositorio.delete(empresa);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }
}
