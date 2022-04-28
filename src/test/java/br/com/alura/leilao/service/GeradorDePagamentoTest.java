package br.com.alura.leilao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @Mock
    Clock clock;

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
        geradorDePagamento = new GeradorDePagamento(pagamentoDao, clock);
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 7, 8, 9, 10})
    void deveCriarPagamentoParaVencedorDoLeilaoDeDomingoAQuinta(int day) {
        Leilao leilao = leilao();
        Lance lanceVencedor = leilao.getLanceVencedor();
        LocalDate monday = LocalDate.of(2020, 12, day);
        Instant instant = monday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Clock fixedClock = Clock.fixed(instant, ZoneId.systemDefault());

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        geradorDePagamento.gerarPagamento(lanceVencedor);

        verify(pagamentoDao).salvar(pagamentoCaptor.capture());
        Pagamento pagamento = pagamentoCaptor.getValue();

        assertEquals(LocalDate.now(fixedClock).plusDays(1), pagamento.getVencimento());
        assertEquals(lanceVencedor.getValor(), pagamento.getValor());
        assertFalse(pagamento.getPago());
        assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
        assertEquals(leilao, pagamento.getLeilao());
    }

    @Test
    void deveCriarPagamentoParaVencedorDoLeilaoNaSexta() {
        Leilao leilao = leilao();
        Lance lanceVencedor = leilao.getLanceVencedor();
        LocalDate friday = LocalDate.of(2020, 12, 11);
        Instant instant = friday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Clock fixedClock = Clock.fixed(instant, ZoneId.systemDefault());

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        geradorDePagamento.gerarPagamento(lanceVencedor);

        verify(pagamentoDao).salvar(pagamentoCaptor.capture());
        Pagamento pagamento = pagamentoCaptor.getValue();

        assertEquals(LocalDate.now(fixedClock).plusDays(3), pagamento.getVencimento());
        assertEquals(lanceVencedor.getValor(), pagamento.getValor());
        assertFalse(pagamento.getPago());
        assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
        assertEquals(leilao, pagamento.getLeilao());
    }

    @Test
    void deveCriarPagamentoParaVencedorDoLeilaoNoSabado() {
        Leilao leilao = leilao();
        Lance lanceVencedor = leilao.getLanceVencedor();
        LocalDate saturday = LocalDate.of(2020, 12, 12);
        Instant instant = saturday.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Clock fixedClock = Clock.fixed(instant, ZoneId.systemDefault());

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        geradorDePagamento.gerarPagamento(lanceVencedor);

        verify(pagamentoDao).salvar(pagamentoCaptor.capture());
        Pagamento pagamento = pagamentoCaptor.getValue();

        assertEquals(LocalDate.now(fixedClock).plusDays(2), pagamento.getVencimento());
        assertEquals(lanceVencedor.getValor(), pagamento.getValor());
        assertFalse(pagamento.getPago());
        assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
        assertEquals(leilao, pagamento.getLeilao());
    }

}
