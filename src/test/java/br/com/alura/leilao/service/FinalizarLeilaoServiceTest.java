package br.com.alura.leilao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;

public class FinalizarLeilaoServiceTest {

    private FinalizarLeilaoService finalizarLeilaoService;

    @Mock
    private LeilaoDao leilaoDao;

    @Mock
    private EnviadorDeEmails enviadorDeEmails;

    private List<Leilao> leiloes() {
        List<Leilao> lista = new ArrayList<>();

        Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("Fulano"));
        Lance primeiro = new Lance(new Usuario("Beltrano"), new BigDecimal("600"));
        Lance segundo = new Lance(new Usuario("Ciclano"), new BigDecimal("900"));

        leilao.propoe(primeiro);
        leilao.propoe(segundo);

        lista.add(leilao);

        return lista;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        finalizarLeilaoService = new FinalizarLeilaoService(leilaoDao, enviadorDeEmails);
    }

    @Test
    void deveFinalizarUmLeilao() {
        List<Leilao> leiloes = leiloes();

        when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);

        finalizarLeilaoService.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);

        assertEquals(new BigDecimal("900"), leilao.getLanceVencedor().getValor());
        assertTrue(leilao.isFechado());
        verify(leilaoDao).salvar(any(Leilao.class));
    }

    @Test
    void deveEnviarEmailParaVencedorDoLeilao() {
        List<Leilao> leiloes = leiloes();

        when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);

        finalizarLeilaoService.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        Lance lanceVencedor = leilao.getLanceVencedor();

        verify(enviadorDeEmails).enviarEmailVencedorLeilao(lanceVencedor);
    }

    @Test
    void naoDeveEnviarEmailParaVencedorDoLeilaoEmCasooDeErro() {

        List<Leilao> leiloes = leiloes();

        when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
        when(leilaoDao.salvar(any(Leilao.class))).thenThrow(RuntimeException.class);

        try {
            finalizarLeilaoService.finalizarLeiloesExpirados();
        } catch (Exception e) {

        }

        verifyNoInteractions(enviadorDeEmails);
    }

}
