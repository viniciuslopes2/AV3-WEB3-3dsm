package com.autobots.automanager.controles;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelo.AdicionadorLinkVenda;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.modelo.MercadoriaSelecionador;
import com.autobots.automanager.modelo.ServicoSelecionador;
import com.autobots.automanager.modelo.UsuarioSelecionador;
import com.autobots.automanager.modelo.VeiculoSelecionador;
import com.autobots.automanager.modelo.VendaAtualizador;
import com.autobots.automanager.modelo.VendaSelecionador;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.UsuarioRepositorio;
import com.autobots.automanager.repositorios.VeiculoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/venda")
public class VendaControle {
    @Autowired
    private VendaRepositorio repositorio;
    @Autowired
    private VendaSelecionador selecionador;
    @Autowired
    private AdicionadorLinkVenda adicionadorLink;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private EmpresaSelecionador empresaSelecionador;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private UsuarioSelecionador usuarioSelecionador;
    @Autowired
    private VeiculoRepositorio veiculoRepositorio;
    @Autowired
    private VeiculoSelecionador veiculoSelecionador;
    @Autowired
    private MercadoriaRepositorio mercadoriaRepositorio;
    @Autowired
    private MercadoriaSelecionador mercadoriaSelecionador;
    @Autowired
    private ServicoRepositorio servicoRepositorio;
    @Autowired
    private ServicoSelecionador servicoSelecionador;

    @GetMapping("/venda/{id}")
    public ResponseEntity<Venda> obterVenda(@PathVariable long id) {
        Venda venda = selecionador.selecionar(repositorio, id);
        if (venda == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(venda);
        return new ResponseEntity<>(venda, HttpStatus.FOUND);
    }

    @GetMapping("/vendas")
    public ResponseEntity<List<Venda>> obterVendas() {
        List<Venda> vendas = repositorio.findAll();
        if (vendas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        adicionadorLink.adicionarLink(vendas);
        return new ResponseEntity<>(vendas, HttpStatus.FOUND);
    }

    @PostMapping("/cadastro/{idEmpresa}")
    @Transactional
    public ResponseEntity<?> cadastrarVenda(@PathVariable long idEmpresa, @RequestBody Venda venda) {
        if (venda.getId() != null || identificacaoEmUso(venda.getIdentificacao(), null)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Empresa empresa = empresaSelecionador.selecionar(empresaRepositorio, idEmpresa);
        Usuario cliente = buscarUsuario(venda.getCliente(), PerfilUsuario.CLIENTE);
        Usuario funcionario = buscarUsuario(venda.getFuncionario(), PerfilUsuario.FUNCIONARIO);
        Set<Mercadoria> mercadorias = buscarMercadorias(venda.getMercadorias());
        Set<Servico> servicos = buscarServicos(venda.getServicos());
        Veiculo veiculo = null;
        if (venda.getVeiculo() != null) {
            veiculo = veiculoSelecionador.selecionar(veiculoRepositorio, venda.getVeiculo().getId());
            if (veiculo == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (empresa == null || cliente == null || funcionario == null || mercadorias == null || servicos == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (venda.getCadastro() == null) {
            venda.setCadastro(new Date());
        }
        venda.setCliente(cliente);
        venda.setFuncionario(funcionario);
        venda.setVeiculo(veiculo);
        venda.setMercadorias(mercadorias);
        venda.setServicos(servicos);
        Venda salva = repositorio.save(venda);
        empresa.getVendas().add(salva);
        cliente.getVendas().add(salva);
        if (veiculo != null) {
            veiculo.getVendas().add(salva);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/atualizar")
    @Transactional
    public ResponseEntity<?> atualizarVenda(@RequestBody Venda atualizacao) {
        Venda venda = selecionador.selecionar(repositorio, atualizacao.getId());
        if (venda == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (identificacaoEmUso(atualizacao.getIdentificacao(), venda.getId())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Usuario cliente = null;
        if (atualizacao.getCliente() != null) {
            cliente = buscarUsuario(atualizacao.getCliente(), PerfilUsuario.CLIENTE);
            if (cliente == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        Usuario funcionario = null;
        if (atualizacao.getFuncionario() != null) {
            funcionario = buscarUsuario(atualizacao.getFuncionario(), PerfilUsuario.FUNCIONARIO);
            if (funcionario == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        Veiculo veiculo = null;
        if (atualizacao.getVeiculo() != null) {
            veiculo = veiculoSelecionador.selecionar(veiculoRepositorio, atualizacao.getVeiculo().getId());
            if (veiculo == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        Set<Mercadoria> mercadorias = buscarMercadorias(atualizacao.getMercadorias());
        Set<Servico> servicos = buscarServicos(atualizacao.getServicos());
        if (mercadorias == null || servicos == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        VendaAtualizador atualizador = new VendaAtualizador();
        atualizador.atualizar(venda, atualizacao);
        if (cliente != null) {
            if (venda.getCliente() != null) {
                venda.getCliente().getVendas().removeIf(item -> item.getId().equals(venda.getId()));
            }
            cliente.getVendas().add(venda);
            venda.setCliente(cliente);
        }
        if (funcionario != null) {
            venda.setFuncionario(funcionario);
        }
        if (veiculo != null) {
            if (venda.getVeiculo() != null) {
                venda.getVeiculo().getVendas().removeIf(item -> item.getId().equals(venda.getId()));
            }
            veiculo.getVendas().add(venda);
            venda.setVeiculo(veiculo);
        }
        if (!mercadorias.isEmpty()) {
            venda.getMercadorias().clear();
            venda.getMercadorias().addAll(mercadorias);
        }
        if (!servicos.isEmpty()) {
            venda.getServicos().clear();
            venda.getServicos().addAll(servicos);
        }
        repositorio.save(venda);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/excluir")
    @Transactional
    public ResponseEntity<?> excluirVenda(@RequestBody Venda exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Venda venda = selecionador.selecionar(repositorio, exclusao.getId());
        if (venda != null) {
            Long id = venda.getId();
            for (Empresa empresa : empresaRepositorio.findAll()) {
                empresa.getVendas().removeIf(item -> item.getId().equals(id));
            }
            for (Usuario usuario : usuarioRepositorio.findAll()) {
                usuario.getVendas().removeIf(item -> item.getId().equals(id));
            }
            for (Veiculo veiculo : veiculoRepositorio.findAll()) {
                veiculo.getVendas().removeIf(item -> item.getId().equals(id));
            }
            repositorio.delete(venda);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<>(status);
    }

    private boolean identificacaoEmUso(String identificacao, Long idIgnorado) {
        if (identificacao == null) {
            return false;
        }
        for (Venda existente : repositorio.findAll()) {
            if (identificacao.equals(existente.getIdentificacao()) && !existente.getId().equals(idIgnorado)) {
                return true;
            }
        }
        return false;
    }

    private Usuario buscarUsuario(Usuario referencia, PerfilUsuario perfil) {
        if (referencia == null) {
            return null;
        }
        Usuario usuario = usuarioSelecionador.selecionar(usuarioRepositorio, referencia.getId());
        if (usuario == null || !usuario.getPerfis().contains(perfil)) {
            return null;
        }
        return usuario;
    }

    private Set<Mercadoria> buscarMercadorias(Set<Mercadoria> referencias) {
        Set<Mercadoria> encontradas = new HashSet<>();
        if (referencias == null) {
            return encontradas;
        }
        for (Mercadoria referencia : referencias) {
            Mercadoria mercadoria = mercadoriaSelecionador.selecionar(mercadoriaRepositorio, referencia.getId());
            if (mercadoria == null) {
                return null;
            }
            encontradas.add(mercadoria);
        }
        return encontradas;
    }

    private Set<Servico> buscarServicos(Set<Servico> referencias) {
        Set<Servico> encontrados = new HashSet<>();
        if (referencias == null) {
            return encontrados;
        }
        for (Servico referencia : referencias) {
            Servico servico = servicoSelecionador.selecionar(servicoRepositorio, referencia.getId());
            if (servico == null) {
                return null;
            }
            encontrados.add(servico);
        }
        return encontrados;
    }
}
