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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkMercadoria;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.modelo.MercadoriaAtualizador;
import com.autobots.automanager.modelo.MercadoriaSelecionador;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/mercadoria")
public class MercadoriaControle {
    @Autowired
    private MercadoriaRepositorio repositorio;
    @Autowired
    private MercadoriaSelecionador selecionador;
    @Autowired
    private AdicionadorLinkMercadoria adicionadorLink;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private EmpresaSelecionador empresaSelecionador;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private UsuarioSelecionador usuarioSelecionador;
    @Autowired
    private VendaRepositorio vendaRepositorio;

    @GetMapping("/mercadoria/{id}")
    public ResponseEntity<Mercadoria> obterMercadoria(@PathVariable long id) {
        Mercadoria mercadoria = selecionador.selecionar(repositorio, id);
        if (mercadoria == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(mercadoria);
        return new ResponseEntity<>(mercadoria, HttpStatus.FOUND);
    }

    @GetMapping("/mercadorias")
    public ResponseEntity<List<Mercadoria>> obterMercadorias() {
        List<Mercadoria> mercadorias = repositorio.findAll();
        if (mercadorias.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(mercadorias);
        return new ResponseEntity<>(mercadorias, HttpStatus.FOUND);
    }

    @PostMapping("/cadastro/{idEmpresa}")
    @Transactional
    public ResponseEntity<?> cadastrarMercadoria(@PathVariable long idEmpresa,
            @RequestParam(required = false) Long fornecedor, @RequestBody Mercadoria mercadoria) {
        if (mercadoria.getId() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Empresa empresa = empresaSelecionador.selecionar(empresaRepositorio, idEmpresa);
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Usuario usuarioFornecedor = null;
        if (fornecedor != null) {
            usuarioFornecedor = usuarioSelecionador.selecionar(usuarioRepositorio, fornecedor);
            if (usuarioFornecedor == null || !usuarioFornecedor.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (mercadoria.getCadastro() == null) {
            mercadoria.setCadastro(new Date());
        }
        Mercadoria salva = repositorio.save(mercadoria);
        empresa.getMercadorias().add(salva);
        if (usuarioFornecedor != null) {
            usuarioFornecedor.getMercadorias().add(salva);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarMercadoria(@RequestBody Mercadoria atualizacao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Mercadoria mercadoria = selecionador.selecionar(repositorio, atualizacao.getId());
        if (mercadoria != null) {
            MercadoriaAtualizador atualizador = new MercadoriaAtualizador();
            atualizador.atualizar(mercadoria, atualizacao);
            repositorio.save(mercadoria);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirMercadoria(@RequestBody Mercadoria exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Mercadoria mercadoria = selecionador.selecionar(repositorio, exclusao.getId());
        if (mercadoria != null) {
            Long id = mercadoria.getId();
            for (Empresa empresa : empresaRepositorio.findAll()) {
                empresa.getMercadorias().removeIf(item -> item.getId().equals(id));
            }
            for (Usuario usuario : usuarioRepositorio.findAll()) {
                usuario.getMercadorias().removeIf(item -> item.getId().equals(id));
            }
            for (Venda venda : vendaRepositorio.findAll()) {
                venda.getMercadorias().removeIf(item -> item.getId().equals(id));
            }
            repositorio.delete(mercadoria);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }
}
