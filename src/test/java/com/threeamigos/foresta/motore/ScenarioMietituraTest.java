package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La Mietitura: il minion quasi morto muore e al suo posto entra uno Scheletro a tempo, se nel gruppo c'è posto;
 * a gruppo pieno muore lo stesso, senza scheletro.
 */
class ScenarioMietituraTest {

    private static Personaggio minionQuasiMorto(PartitaDiTest partita) {
        GruppoAvversario avversari = GruppoAvversario.getIstanza();
        avversari.reimposta();
        Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1);
        avversari.aggiungiPersonaggio(goblin);
        goblin.getModelloDati().set(TipoAttributo.SALUTE, 1);
        return goblin;
    }

    private static DannoRisultante mietitura(Personaggio attaccante, Personaggio bersaglio) {
        DannoRisultante risultato = new DannoRisultante(attaccante, bersaglio);
        risultato.setMietituraAttiva(true);
        return risultato;
    }

    @Test
    void conPostoNascelloScheletroATempo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(251)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio goblin = minionQuasiMorto(partita);
            goblin.applicaRisultatoCombattimento(mietitura(partita.gruppo().getCapo(), goblin));

            assertFalse(goblin.isVivo());
            assertEquals(2, partita.gruppo().getNumeroPersonaggi());
            Personaggio scheletro = partita.gruppo().getPersonaggio(1);
            assertEquals(TipoPersonaggio.SCHELETRO, scheletro.getClasse());
            assertEquals(TipoMotivoNelGruppo.MIETITURA, scheletro.getMotivoNelGruppo());
            assertTrue(scheletro.isATempo());
        }
    }

    @Test
    void agruppoPienoIlMinionMuoreSenzaScheletro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(252)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            while (partita.gruppo().getNumeroPersonaggi() < Costanti.MAX_PERSONAGGI_GRUPPO_TOTALE) {
                partita.gruppo().aggiungiPersonaggio(new Guerriero("Compagno" + partita.gruppo().getNumeroPersonaggi(), 1));
            }
            Personaggio goblin = minionQuasiMorto(partita);
            goblin.applicaRisultatoCombattimento(mietitura(partita.gruppo().getCapo(), goblin));

            assertFalse(goblin.isVivo());
            assertEquals(Costanti.MAX_PERSONAGGI_GRUPPO_TOTALE, partita.gruppo().getNumeroPersonaggi());
        }
    }

    private static final String FALCE = "FALCE_DEL_TRISTO_MIETITORE";
    private static final int DADI_DEL_COLPO = 5;

    private static DannoRisultante colpoDellaFalce(PartitaDiTest partita, Personaggio bersaglio) {
        Personaggio capo = partita.gruppo().getCapo();
        capo.addArtefatto(Leggendari.con(FALCE).costruisci());
        return CalcolatoreCombattimento.calcolaDannoRisultante(capo, bersaglio, capo.getArmaEquipaggiata());
    }

    @Test
    void laFalceDelTristoMietitoreMieteUnBersaglioMaledetto() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(253)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio goblin = minionQuasiMorto(partita);
            goblin.addEffettoDiStato(TipoEffettoDiStato.MALEDETTO, 5, 0);

            DannoRisultante risultato = colpoDellaFalce(partita, goblin);
            assertTrue(risultato.isMietituraAttiva());
            assertTrue(risultato.getInterazioni().contains(TipoInterazioneConEffettiDiStato.MIETITURA));

            goblin.applicaRisultatoCombattimento(risultato);
            assertFalse(goblin.isVivo());
            Personaggio scheletro = partita.gruppo().getPersonaggio(partita.gruppo().getNumeroPersonaggi() - 1);
            assertEquals(TipoMotivoNelGruppo.MIETITURA, scheletro.getMotivoNelGruppo());
        }
    }

    /**
     * I dadi truccati a 1 fanno andare a segno ogni lancio contro una probabilità: il colpo arriva, e anche gli
     * incantamenti applicano il loro stato. Un colpo della Falce lancia cinque dadi (contati provando; prima del tiro che decideva se lanciare un incantesimo erano sei); se un giorno ne
     * lancia di più o di meno il test se ne accorge.
     */
    private static void colpisciConDadiTruccati(Personaggio attaccante, Personaggio bersaglio, int lanci) {
        Integer[] unoDopoUno = new Integer[lanci];
        Arrays.fill(unoDopoUno, 1);
        Dado.trucca(unoDopoUno);
        try {
            attaccante.attaccaConLeArmi(bersaglio);
            assertEquals(0, Dado.trucchiRimasti(), "il colpo lancia meno dadi di quelli previsti");
        } finally {
            Dado.ripristina();
        }
    }

    @Test
    void laFalceRendeMaledettoEPoiMieteSenzaAiutiDelCaso() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(255)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio capo = partita.gruppo().getCapo();
            capo.addArtefatto(Leggendari.con(FALCE).costruisci());
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.reimposta();
            // Di livello alto, per sopravvivere al primo colpo
            Personaggio goblin = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 30);
            avversari.aggiungiPersonaggio(goblin);
            assertFalse(goblin.hasEffettoDiStato(TipoEffettoDiStato.MALEDETTO));

            colpisciConDadiTruccati(capo, goblin, DADI_DEL_COLPO);
            assertTrue(goblin.isVivo(), "il primo colpo non deve ucciderlo");
            assertTrue(goblin.hasEffettoDiStato(TipoEffettoDiStato.MALEDETTO), "la Maledizione della Falce lo rende maledetto");

            // Ora è quasi morto: il secondo colpo lo miete
            goblin.getModelloDati().set(TipoAttributo.SALUTE, 1);
            int prima = partita.gruppo().getNumeroPersonaggi();
            colpisciConDadiTruccati(capo, goblin, DADI_DEL_COLPO);
            assertFalse(goblin.isVivo());
            assertEquals(prima + 1, partita.gruppo().getNumeroPersonaggi());
            Personaggio scheletro = partita.gruppo().getPersonaggio(prima);
            assertEquals(TipoPersonaggio.SCHELETRO, scheletro.getClasse());
            assertEquals(TipoMotivoNelGruppo.MIETITURA, scheletro.getMotivoNelGruppo());
        }
    }

    @Test
    void laFalceNonMieteUnBersaglioNonMaledetto() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(254)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio goblin = minionQuasiMorto(partita);

            assertFalse(colpoDellaFalce(partita, goblin).isMietituraAttiva());
        }
    }

    private static boolean inventarioHaLaFalce(PartitaDiTest partita) {
        String nome = Leggendari.con(FALCE).getNome();
        return partita.gruppo().getInventario().stream().anyMatch(a -> nome.equals(a.getNome()));
    }

    @Test
    void inModalitaDiProvaLaFalceEnellInventarioDelGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(256)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            assertTrue(inventarioHaLaFalce(partita));
        }
    }

    @Test
    void senzaModalitaDiProvaLaFalceNonCe() {
        try (PartitaDiTest partita = PartitaDiTest.nuovaSenzaTrucchi(257)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            assertFalse(inventarioHaLaFalce(partita));
        }
    }

    @Test
    void ildannoMaledizioneRendeMaledetto() {
        assertTrue(Arrays.asList(TipoDanno.MALEDIZIONE.getEffetti()).contains(TipoEffettoDiStato.MALEDETTO));
    }
}
