package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.modellodati.AzioneMD;
import com.threeamigos.foresta.modellodati.FiltroMD;
import com.threeamigos.foresta.modellodati.OrdinamentoMD;
import com.threeamigos.foresta.modellodati.PredicatoMD;
import com.threeamigos.foresta.modellodati.SelettoreMD;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.VoceMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoFiltro;
import com.threeamigos.foresta.tipi.TipoOrdinamento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRosa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I casi di bordo di tattiche e contesto: chi non ha un fornitore di dati, una tattica scritta male, e dati diversi che non si scambiano
 * per uguali.
 */
class LettureEFornitoriDelleTatticheTest {

    @BeforeEach
    void preparaUnaPartita() {
        DatiDiProvaDelleTattiche.nuovaPartita();
        StatoDelCombattimento.getIstanza().reimposta();
    }

    @AfterEach
    void ridaiIFornitori() {
        DatiDiProvaDelleTattiche.nuovaPartita();
    }

    @Test
    void senzaUnFornitoreLeTatticheNonSannoDoveLeggere() {
        TatticheDeiPersonaggi.setFornitoreDiDati(null);
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        assertThrows(IllegalStateException.class, () -> TatticheDeiPersonaggi.di(guerriero));
    }

    @Test
    void unaTatticaConUnNumeroScrittoMaleNonSiLegge() throws IOException {
        TatticaMD tattica = TatticaMD.di(VoceMD.su(null, AzioneMD.di(TipoAzione.ATTACCO_FISICO)));
        StringWriter scrittura = new StringWriter();
        try (PrintWriter writer = new PrintWriter(scrittura)) {
            tattica.salva(writer);
        }
        String guasta = scrittura.toString().replaceFirst("TATTICA\\|1\\|", "TATTICA|uno|");
        BufferedReader stream = new BufferedReader(new StringReader(guasta));
        IOException errore = assertThrows(IOException.class, () -> TatticaMD.leggi(stream));
        assertTrue(errore.getMessage().contains("uno"), errore.getMessage());
    }

    @Test
    void ilContestoDelPersonaggioUsaIGruppiDellaPartita() {
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(guerriero);

        ContestoTattico contesto = ContestoTattico.per(guerriero);

        assertSame(guerriero, contesto.getAttore());
        assertTrue(contesto.isAttoreDelGiocatore());
        assertTrue(contesto.getAlleati().contains(guerriero));
    }

    @Test
    void ilContestoPuoGuardareUnAltroStatoDelloScontro() {
        Personaggio guerriero = FabbricaPersonaggi.crea(TipoPersonaggio.GUERRIERO, 1);
        GruppoGiocatore.getIstanza().aggiungiPersonaggio(guerriero);
        ContestoTattico contesto = ContestoTattico.per(guerriero);
        StatoDelCombattimento altro = StatoDelCombattimento.getIstanza();

        ContestoTattico conAltroStato = contesto.conStato(altro);

        assertNotSame(contesto, conAltroStato);
        assertSame(altro, conAltroStato.getStato());
        assertSame(guerriero, conAltroStato.getAttore());
    }

    @Test
    void datiDiUnAltroTipoNonSonoMaiUguali() {
        AzioneMD azione = AzioneMD.di(TipoAzione.ATTACCO_FISICO);
        FiltroMD filtro = FiltroMD.senzaParametro(TipoFiltro.MAGICO);
        PredicatoMD predicato = PredicatoMD.probabilita(50);
        SelettoreMD selettore = new SelettoreMD(TipoRosa.AVVERSARI, Collections.emptyList(), OrdinamentoMD.di(TipoOrdinamento.PRIMO));
        VoceMD voce = VoceMD.su(selettore, azione);

        assertNotEquals(azione, "ATTACCO_FISICO");
        assertNotEquals(filtro, "MAGICO");
        assertNotEquals(predicato, "50");
        assertNotEquals(selettore, "AVVERSARI");
        assertNotEquals(voce, "voce");
    }
}
