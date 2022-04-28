package br.com.alura.leilao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;

public class GeradorDePagamentoTest {

    @Mock
    PagamentoDao pagamentoDao;

    @Captor
    ArgumentCaptor<Pagamento> pagamentoCaptor;

    GeradorDePagamento geradorDePagamento;

    private Leilao leilao() {
        Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("Fulano"));
        Lance lance = new Lance(new Usuario("Ciclano"), new BigDecimal("900"));

        leilao.propoe(lance);
        leilao.setLanceVencedor(lance);

        return leilao;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        geradorDePagamento = new GeradorDePagamento(pagamentoDao);
    }

    @Test
    void deveCriarPagamentoParaVencedorDoLeilao() {
        Leilao leilao = leilao();
        Lance lanceVencedor = leilao.getLanceVencedor();

        geradorDePagamento.gerarPagamento(lanceVencedor);

        verify(pagamentoDao).salvar(pagamentoCaptor.capture());
        Pagamento pagamento = pagamentoCaptor.getValue();

        assertEquals(LocalDate.now().plusDays(1), pagamento.getVencimento());
        assertEquals(lanceVencedor.getValor(), pagamento.getValor());
        assertFalse(pagamento.getPago());
        assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
        assertEquals(leilao, pagamento.getLeilao());
    }

}
