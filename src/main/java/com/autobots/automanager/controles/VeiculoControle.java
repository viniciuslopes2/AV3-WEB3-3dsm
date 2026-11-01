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

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.AdicionadorLinkVeiculo;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.modelo.VeiculoAtualizador;
import com.autobots.automanager.modelo.VeiculoSelecionador;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/veiculo")
public class VeiculoControle {
    @Autowired
    private VeiculoRepositorio repositorio;
    @Autowired
    private VeiculoSelecionador selecionador;
    @Autowired
    private AdicionadorLinkVeiculo adicionadorLink;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private UsuarioSelecionador usuarioSelecionador;
    @Autowired
    private VendaRepositorio vendaRepositorio;

    @GetMapping("/veiculo/{id}")
    public ResponseEntity<Veiculo> obterVeiculo(@PathVariable long id) {
        Veiculo veiculo = selecionador.selecionar(repositorio, id);
        if (veiculo == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(veiculo);
        return new ResponseEntity<>(veiculo, HttpStatus.FOUND);
    }

    @GetMapping("/veiculos")
    public ResponseEntity<List<Veiculo>> obterVeiculos() {
        List<Veiculo> veiculos = repositorio.findAll();
        if (veiculos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(veiculos);
        return new ResponseEntity<>(veiculos, HttpStatus.FOUND);
    }

    @PostMapping("/cadastro/{idProprietario}")
    @Transactional
    public ResponseEntity<?> cadastrarVeiculo(@PathVariable long idProprietario, @RequestBody Veiculo veiculo) {
        if (veiculo.getId() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Usuario proprietario = usuarioSelecionador.selecionar(usuarioRepositorio, idProprietario);
        if (proprietario == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        veiculo.getVendas().clear();
        veiculo.setProprietario(proprietario);
        Veiculo salvo = repositorio.save(veiculo);
        proprietario.getVeiculos().add(salvo);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarVeiculo(@RequestBody Veiculo atualizacao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Veiculo veiculo = selecionador.selecionar(repositorio, atualizacao.getId());
        if (veiculo != null) {
            VeiculoAtualizador atualizador = new VeiculoAtualizador();
            atualizador.atualizar(veiculo, atualizacao);
            repositorio.save(veiculo);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirVeiculo(@RequestBody Veiculo exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Veiculo veiculo = selecionador.selecionar(repositorio, exclusao.getId());
        if (veiculo != null) {
            Long id = veiculo.getId();
            for (Usuario usuario : usuarioRepositorio.findAll()) {
                usuario.getVeiculos().removeIf(item -> item.getId().equals(id));
            }
            for (Venda venda : vendaRepositorio.findAll()) {
                if (venda.getVeiculo() != null && venda.getVeiculo().getId().equals(id)) {
                    venda.setVeiculo(null);
                }
            }
            veiculo.getVendas().clear();
            repositorio.delete(veiculo);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }
}
