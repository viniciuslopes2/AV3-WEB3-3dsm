package com.autobots.automanager.controles;

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
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.AdicionadorLinkServico;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.modelo.ServicoAtualizador;
import com.autobots.automanager.modelo.ServicoSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/servico")
public class ServicoControle {
    @Autowired
    private ServicoRepositorio repositorio;
    @Autowired
    private ServicoSelecionador selecionador;
    @Autowired
    private AdicionadorLinkServico adicionadorLink;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private EmpresaSelecionador empresaSelecionador;
    @Autowired
    private VendaRepositorio vendaRepositorio;

    @GetMapping("/servico/{id}")
    public ResponseEntity<Servico> obterServico(@PathVariable long id) {
        Servico servico = selecionador.selecionar(repositorio, id);
        if (servico == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(servico);
        return new ResponseEntity<>(servico, HttpStatus.FOUND);
    }

    @GetMapping("/servicos")
    public ResponseEntity<List<Servico>> obterServicos() {
        List<Servico> servicos = repositorio.findAll();
        if (servicos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(servicos);
        return new ResponseEntity<>(servicos, HttpStatus.FOUND);
    }

    @PostMapping("/cadastro/{idEmpresa}")
    @Transactional
    public ResponseEntity<?> cadastrarServico(@PathVariable long idEmpresa, @RequestBody Servico servico) {
        if (servico.getId() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Empresa empresa = empresaSelecionador.selecionar(empresaRepositorio, idEmpresa);
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Servico salvo = repositorio.save(servico);
        empresa.getServicos().add(salvo);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarServico(@RequestBody Servico atualizacao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Servico servico = selecionador.selecionar(repositorio, atualizacao.getId());
        if (servico != null) {
            ServicoAtualizador atualizador = new ServicoAtualizador();
            atualizador.atualizar(servico, atualizacao);
            repositorio.save(servico);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirServico(@RequestBody Servico exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Servico servico = selecionador.selecionar(repositorio, exclusao.getId());
        if (servico != null) {
            Long id = servico.getId();
            for (Empresa empresa : empresaRepositorio.findAll()) {
                empresa.getServicos().removeIf(item -> item.getId().equals(id));
            }
            for (Venda venda : vendaRepositorio.findAll()) {
                venda.getServicos().removeIf(item -> item.getId().equals(id));
            }
            repositorio.delete(servico);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }
}
