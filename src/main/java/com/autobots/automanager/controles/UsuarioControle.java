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
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.AdicionadorLinkUsuario;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.modelo.UsuarioAtualizador;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/usuario")
public class UsuarioControle {
    @Autowired
    private UsuarioRepositorio repositorio;
    @Autowired
    private UsuarioSelecionador selecionador;
    @Autowired
    private AdicionadorLinkUsuario adicionadorLink;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private EmpresaSelecionador empresaSelecionador;
    @Autowired
    private VeiculoRepositorio veiculoRepositorio;
    @Autowired
    private VendaRepositorio vendaRepositorio;

    @GetMapping("/usuario/{id}")
    public ResponseEntity<Usuario> obterUsuario(@PathVariable long id) {
        Usuario usuario = selecionador.selecionar(repositorio, id);
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(usuario);
        return new ResponseEntity<>(usuario, HttpStatus.FOUND);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obterUsuarios() {
        List<Usuario> usuarios = repositorio.findAll();
        if (usuarios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(usuarios);
        return new ResponseEntity<>(usuarios, HttpStatus.FOUND);
    }

    @PostMapping("/cadastro/{idEmpresa}")
    @Transactional
    public ResponseEntity<?> cadastrarUsuario(@PathVariable long idEmpresa, @RequestBody Usuario usuario) {
        if (usuario.getId() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Empresa empresa = empresaSelecionador.selecionar(empresaRepositorio, idEmpresa);
        if (empresa == null || usuario.getPerfis().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        usuario.getMercadorias().clear();
        usuario.getVendas().clear();
        usuario.getVeiculos().clear();
        for (Credencial credencial : usuario.getCredenciais()) {
            if (credencial.getCriacao() == null) {
                credencial.setCriacao(new Date());
            }
        }
        Usuario salvo = repositorio.save(usuario);
        empresa.getUsuarios().add(salvo);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarUsuario(@RequestBody Usuario atualizacao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Usuario usuario = selecionador.selecionar(repositorio, atualizacao.getId());
        if (usuario != null) {
            UsuarioAtualizador atualizador = new UsuarioAtualizador();
            atualizador.atualizar(usuario, atualizacao);
            repositorio.save(usuario);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirUsuario(@RequestBody Usuario exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Usuario usuario = selecionador.selecionar(repositorio, exclusao.getId());
        if (usuario != null) {
            Long id = usuario.getId();
            for (Empresa empresa : empresaRepositorio.findAll()) {
                empresa.getUsuarios().removeIf(item -> item.getId().equals(id));
            }
            for (Venda venda : vendaRepositorio.findAll()) {
                if (venda.getCliente() != null && venda.getCliente().getId().equals(id)) {
                    venda.setCliente(null);
                }
                if (venda.getFuncionario() != null && venda.getFuncionario().getId().equals(id)) {
                    venda.setFuncionario(null);
                }
            }
            for (Veiculo veiculo : veiculoRepositorio.findAll()) {
                if (veiculo.getProprietario() != null && veiculo.getProprietario().getId().equals(id)) {
                    veiculo.setProprietario(null);
                }
            }
            repositorio.delete(usuario);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }
}
