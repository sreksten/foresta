package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.modellodati.tattiche.*;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.tattiche.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le tattiche si salvano e si rileggono con il personaggio (tattiche_di_combattimento_automatiche.md §1, §11.12): una tattica complessa torna uguale,
 * e un salvataggio incoerente è rifiutato.
 */
class SalvataggioTatticheTest {

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    /** Una tattica con almeno un esemplare di ogni forma di filtro, predicato e azione */
    private static TatticaMD complessa() {
        SelettoreMD tutto = new SelettoreMD(TipoRosa.AVVERSARI, Arrays.asList(
                FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE, TipoConfronto.MINORE, 50),
                FiltroMD.conConfrontoENumero(TipoFiltro.SALUTE_PERCENTUALE, TipoConfronto.MAGGIORE, 30).negato(),
                FiltroMD.conConfronto(TipoFiltro.LIVELLO_RISPETTO_AL_MIO, TipoConfronto.UGUALE),
                FiltroMD.conClasse(TipoFiltro.CLASSE, TipoPersonaggio.MAGO),
                FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.NON_MORTO),
                FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.SILENZIATO).negato(),
                FiltroMD.conTipoDanno(TipoFiltro.DEBOLE_A, TipoDanno.FUOCO),
                FiltroMD.senzaParametro(TipoFiltro.MAGICO),
                FiltroMD.senzaParametro(TipoFiltro.DEBOLE_ALL_AZIONE)),
                OrdinamentoMD.di(TipoOrdinamento.PIU_DEBOLE_A, TipoDanno.GELO));
        VoceMD silenzia = new VoceMD(tutto, Arrays.asList(
                PredicatoMD.conConfrontoENumero(TipoPredicato.ROUND, TipoConfronto.UGUALE, 1),
                PredicatoMD.conConfrontoENumero(TipoPredicato.ASTUZIA, TipoConfronto.MINORE, 20).negato(),
                PredicatoMD.probabilita(67),
                PredicatoMD.quanti(TipoRosa.AVVERSARI, Collections.singletonList(FiltroMD.conCategoria(TipoFiltro.CATEGORIA, CategoriaPersonaggio.VOLANTE)),
                        TipoConfronto.MAGGIORE, 0).negato()),
                Arrays.asList(AzioneMD.di(TipoAzione.SILENZIO), AzioneMD.di(TipoAzione.FULMINE).soloSePiuForteDellAttacco(),
                        AzioneMD.incantesimoDiDanno(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, true), AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        VoceMD cura = new VoceMD(new SelettoreMD(TipoRosa.ALLEATI_E_OSPITI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.MENO_SALUTE)),
                Collections.emptyList(), Arrays.asList(AzioneMD.di(TipoAzione.POZIONE_SALUTE), AzioneMD.di(TipoAzione.CANTO_CURATIVO)));
        VoceMD cambia = new VoceMD(new SelettoreMD(TipoRosa.SE_STESSO, Collections.singletonList(FiltroMD.conTipoDanno(TipoFiltro.ARMA_CHE_DA, TipoDanno.SACRO).negato()),
                OrdinamentoMD.di(TipoOrdinamento.PRIMO)), Collections.emptyList(), Collections.singletonList(AzioneMD.cambiaArma(TipoDanno.SACRO)));
        VoceMD senzaSelettore = new VoceMD(null, Collections.emptyList(), Collections.singletonList(AzioneMD.di(TipoAzione.CANTO_DI_VALORE)));
        VoceMD disattivata = VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.A_CASO)),
                AzioneMD.di(TipoAzione.DARDO_ARCANO)).disattivata();
        return new TatticaMD(Arrays.asList(silenzia, cura, cambia, senzaSelettore, disattivata));
    }

    private static String scrivi(TatticaMD tattica) {
        StringWriter testo = new StringWriter();
        PrintWriter stream = new PrintWriter(testo);
        tattica.salva(stream);
        stream.flush();
        return testo.toString();
    }

    private static TatticaMD rileggi(String testo) throws IOException {
        return TatticaMD.leggi(new BufferedReader(new StringReader(testo)));
    }

    @Test
    void unaTatticaComplessaTornaUguale() throws IOException {
        TatticaMD originale = complessa();
        String testo = scrivi(originale);
        TatticaMD riletta = rileggi(testo);
        assertEquals(originale, riletta);
        assertEquals(originale.hashCode(), riletta.hashCode());
        assertEquals(testo, scrivi(riletta), "scrivere di nuovo dà lo stesso testo");
        // Il valore di ogni campo è davvero stato riletto (non solo uguale per caso)
        VoceMD prima = riletta.getVoci().get(0);
        assertEquals(9, prima.getSelettore().getFiltri().size());
        assertTrue(prima.getSelettore().getFiltri().get(1).isNegato());
        assertEquals(TipoDanno.GELO, prima.getSelettore().getOrdinamento().getTipoDanno());
        assertEquals(4, prima.getCondizioni().size());
        assertEquals(CriterioIncantesimo.PIU_ECONOMICO_CHE_BASTA, prima.getAzioni().get(2).getCriterio());
        assertTrue(prima.getAzioni().get(1).isSoloSePiuForteDellAttacco());
        assertEquals(TipoDanno.SACRO, riletta.getVoci().get(2).getAzioni().get(0).getArmaDi());
        assertNull(riletta.getVoci().get(3).getSelettore());
        assertFalse(riletta.getVoci().get(4).isAttiva());
    }

    @Test
    void ilNomeDellaVoceSiSalvaESiRilegge() throws IOException {
        VoceMD base = VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO));
        VoceMD battezzata = new VoceMD(null, Collections.emptyList(), base.getAzioni(), true, "  Sempre all'attacco ");
        TatticaMD riletta = rileggi(scrivi(TatticaMD.di(battezzata, base)));
        assertEquals("Sempre all'attacco", riletta.getVoci().get(0).getNome());
        assertNull(riletta.getVoci().get(1).getNome());
        assertEquals(TatticaMD.di(battezzata, base), riletta);
    }

    @Test
    void unaTatticaVuotaTornaVuotaENonENulla() throws IOException {
        TatticaMD riletta = rileggi(scrivi(TatticaMD.VUOTA));
        assertEquals(TatticaMD.VUOTA, riletta);
        assertTrue(riletta.getVoci().isEmpty());
    }

    @Test
    void ilTestoESemplice() {
        String testo = scrivi(TatticaMD.di(VoceMD.su(new SelettoreMD(TipoRosa.AVVERSARI,
                Collections.singletonList(FiltroMD.conStato(TipoFiltro.STATO, TipoEffettoDiStato.BAGNATO)), OrdinamentoMD.di(TipoOrdinamento.PRIMO)),
                AzioneMD.di(TipoAzione.FULMINE))));
        assertEquals("TATTICA|1|0\nVOCE|true|true|0|1|\nSELETTORE|AVVERSARI|PRIMO||1\nFILTRO|STATO|false||0|BAGNATO|||\nAZIONE|FULMINE||false|\n",
                testo.replace("\r\n", "\n"));
    }

    @Test
    void unSalvataggioIncoerenteERifiutato() {
        String buono = scrivi(complessa());
        assertThrows(IOException.class, () -> rileggi(buono.replace("TATTICA|", "TATTICHE|")), "intestazione sbagliata");
        assertThrows(IOException.class, () -> rileggi(buono.replace("SELETTORE|", "SELEZIONE|")), "elemento atteso e non trovato");
        assertThrows(IOException.class, () -> rileggi(buono.replace("|SILENZIO|", "|NON_ESISTE|")), "azione sconosciuta");
        assertThrows(IOException.class, () -> rileggi(buono.substring(0, buono.length() / 2)), "salvataggio troncato");
        assertThrows(IOException.class, () -> rileggi("TATTICA|1|0\nVOCE|true|false|0|0|\n"), "una voce senza azioni");
        // Un filtro di classe senza la classe
        assertThrows(IOException.class, () -> rileggi("TATTICA|1|0\nVOCE|true|true|0|1|\nSELETTORE|AVVERSARI|PRIMO||1\nFILTRO|CLASSE|false||0||||\nAZIONE|ATTACCO_FISICO||false|\n"));
        // Un ordinamento che vuole un tipo di danno, senza
        assertThrows(IOException.class, () -> rileggi("TATTICA|1|0\nVOCE|true|true|0|1|\nSELETTORE|AVVERSARI|PIU_DEBOLE_A||0\nAZIONE|ATTACCO_FISICO||false|\n"));
    }

    @Test
    void ilPersonaggioSalvaELeggeLaSuaTattica() throws IOException {
        Personaggio mago = FabbricaPersonaggi.crea(TipoPersonaggio.MAGO, 3);
        mago.setTattica(complessa());
        PersonaggioMD letto = passaDalSalvataggio(mago.getModelloDati());
        assertEquals(complessa(), letto.getTattica());
        assertEquals(mago.getModelloDati().getClasse(), letto.getClasse());
        assertEquals(mago.getModelloDati().getLivello(), letto.getLivello(), "gli altri campi non si spostano");
    }

    @Test
    void ilPersonaggioSenzaTatticaLaRilegge_NullaEUnaVuotaRestaVuota() throws IOException {
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        assertNull(passaDalSalvataggio(goblin.getModelloDati()).getTattica(), "un mostro senza tattica resta senza");
        goblin.setTattica(TatticaMD.VUOTA);
        PersonaggioMD letto = passaDalSalvataggio(goblin.getModelloDati());
        assertNotNull(letto.getTattica());
        assertTrue(letto.getTattica().getVoci().isEmpty());
    }

    private static PersonaggioMD passaDalSalvataggio(PersonaggioMD md) throws IOException {
        StringWriter testo = new StringWriter();
        PrintWriter stream = new PrintWriter(testo);
        md.salva(stream);
        stream.flush();
        PersonaggioMD letto = new PersonaggioMD();
        letto.leggi(new BufferedReader(new StringReader(testo.toString())));
        return letto;
    }
}
