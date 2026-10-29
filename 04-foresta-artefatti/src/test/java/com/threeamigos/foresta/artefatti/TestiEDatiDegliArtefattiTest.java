package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.artefatti.leggendari.LibroTecnico;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.tipi.TipoDanno;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I testi a caso degli artefatti, quelli delle enum e i dati degli incantamenti.
 */
class TestiEDatiDegliArtefattiTest {

    @Test
    void ogniEnumHaIlSuoTesto() {
        for (TipoArtefatto tipo : TipoArtefatto.values()) {
            assertFalse(tipo.getDescrizione().isEmpty(), tipo.name());
            assertFalse(tipo.getUtilizzo().isEmpty(), tipo.name());
        }
        for (TipoRaritaArtefatto rarita : TipoRaritaArtefatto.values()) {
            assertFalse(rarita.getNome().isEmpty(), rarita.name());
        }
    }

    @Test
    void soloLeArmiHannoUnTipoDiDanno() {
        assertEquals(TipoDanno.TAGLIENTE, TipoArtefatto.SPADA.getTipoDanno());
    }

    @Test
    void ilProduttoreSiPrecaricaEDaTestiDiLibriTecnici() {
        ProduttoreDiArtefattiCasuale.precarica();
        assertFalse(ProduttoreDiArtefattiCasuale.erroreDiRiprogrammazione().isEmpty());
        assertTrue(ProduttoreDiArtefattiCasuale.letturaDelLibroTecnico("Il Titolo").contains("Il Titolo"));
        for (LibroTecnico libro : LibroTecnico.tutti()) {
            assertFalse(ProduttoreDiArtefattiCasuale.conclusioneSulLibroTecnico(libro.getChiave()).isEmpty(), libro.getChiave());
        }
        assertFalse(ProduttoreDiArtefattiCasuale.nomeAggiornamento().isEmpty());
    }

    @Test
    void ilProduttorePescaUnaVoltaSolaOgniLeggendario() {
        Optional<String> primo = ProduttoreDiArtefattiCasuale.oggettoLeggendario(riga -> false);
        assertTrue(primo.isPresent());
        // Se tutti sono già stati pescati non ce ne sono più
        assertFalse(ProduttoreDiArtefattiCasuale.oggettoLeggendario(riga -> true).isPresent());
        // Se è già stato pescato quello, se ne dà un altro
        Optional<String> altro = ProduttoreDiArtefattiCasuale.oggettoLeggendario(riga -> riga.equals(primo.get()));
        assertTrue(altro.isPresent() && !altro.get().equals(primo.get()));
        assertFalse(ProduttoreDiArtefattiCasuale.introduzioneDellaLeggenda("lo Scudo Fiscale").isEmpty());
    }

    @Test
    void unaGrammaticaCheNonSiCaricaDaNull() {
        assertNull(GrammaticaArtefatti.caricaOppureNull("/non/esiste.txt"));
    }

    @Test
    void ilSoprannomeEDescrizioneSonoIPrimiCheNonSonoVuoti() {
        GrammaticaArtefatti.Risultato risultato = GrammaticaArtefatti.interpreta(
                "la spada<soprannome:><soprannome:Pippo><soprannome:Pluto><descrizione:><descrizione:che taglia><descrizione:che pesa>");
        assertEquals("la spada", risultato.getNome());
        assertEquals("Pippo", risultato.getSoprannome());
        assertEquals("che taglia", risultato.getDescrizione());
    }

    @Test
    void unMarcatoreNonChiusoSiRifiuta() {
        assertThrows(IllegalArgumentException.class, () -> GrammaticaArtefatti.interpreta("la spada>"));
        assertThrows(IllegalArgumentException.class, () -> GrammaticaArtefatti.interpreta("la <spada"));
    }

    @Test
    void ilNomeDiUnIncantamentoVuotoONulloENessunNome() {
        assertEquals(ArtefattoMD.NESSUN_NOME, new IncantamentoMD(null, TipoDanno.FUOCO, 1, 0.1).getNomeIncantamento());
        assertEquals(ArtefattoMD.NESSUN_NOME, new IncantamentoMD("   ", TipoDanno.FUOCO, 1, 0.1).getNomeIncantamento());
        assertEquals("Fiamma", new IncantamentoMD("Fiamma", TipoDanno.FUOCO, 1, 0.1).getNomeIncantamento());
    }

    @Test
    void siPossonoTogliereTuttiGliIncantamenti() {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(TipoArtefatto.SPADA);
        md.addIncantamento("Fiamma", TipoDanno.FUOCO, 1, 0.1);
        md.addIncantamento("Gelo", TipoDanno.GELO, 1, 0.1);
        md.rimuoviTuttiGliIncantamenti();
        assertTrue(md.getIncantamenti().isEmpty());
    }

    @Test
    void ilNomeEDescrizioneDiUnoConNomeProprioEIlNomeSeLaDescrizioneManca() {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(TipoArtefatto.SPADA);
        md.setNome("la spada");
        md.setNomeProprio("Fiamma");
        md.setDescrizione("che brucia");
        assertEquals("la spada, che brucia", md.getDescrizioneBreve());
        md.setDescrizione("");
        assertEquals("la spada", md.getDescrizioneBreve());
        md.setDescrizione(null);
        assertEquals("la spada", md.getDescrizioneBreve());
        assertEquals("la spada", md.getDescrizioneBreve());
    }
}
