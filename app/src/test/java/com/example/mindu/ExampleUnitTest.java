package com.example.mindu;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExampleUnitTest {
    @Test
    public void saudacaoUsaNomeDaSessaoERemoveEspacosExternos() {
        assertEquals("Olá, Manuela Costa!", SaudacaoFormatter.format("  Manuela Costa  "));
    }

    @Test
    public void saudacaoTemFallbackQuandoNomeNaoEstaDisponivel() {
        assertEquals("Olá, seja bem-vindo(a)!", SaudacaoFormatter.format(" "));
        assertEquals("Olá, seja bem-vindo(a)!", SaudacaoFormatter.format(null));
    }

    @Test
    public void agendaFiltraDataPeloPrefixoIso() {
        assertTrue(AgendamentoUiRules.belongsToDate("2026-11-26T09:00:00", "2026-11-26"));
        assertFalse(AgendamentoUiRules.belongsToDate("2026-11-27T09:00:00", "2026-11-26"));
        assertFalse(AgendamentoUiRules.belongsToDate(null, "2026-11-26"));
    }

    @Test
    public void somenteConsultaAgendadaPermiteAcoes() {
        assertTrue(AgendamentoUiRules.isActive("AGENDADO"));
        assertFalse(AgendamentoUiRules.isActive("CANCELADO"));
        assertFalse(AgendamentoUiRules.isActive("REALIZADO"));
    }

    @Test
    public void modalidadeDaApiReconheceAtendimentoVirtualOuHibrido() {
        assertTrue(AgendamentoUiRules.supportsOnline("VIRTUAL"));
        assertTrue(AgendamentoUiRules.supportsOnline("HIBRIDO"));
        assertFalse(AgendamentoUiRules.supportsOnline("PRESENCIAL"));
        assertFalse(AgendamentoUiRules.supportsOnline(null));
    }
}
