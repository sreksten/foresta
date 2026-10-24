package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.danni.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoTrofeo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.trofei.RegistroTrofei;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.DannoRisultante;

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
    void loScheletroRisvegliatoEntraNelGruppoSenzaEssereFelice() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(257)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.eventi().ascolta(NotificaTestoFrase.class);
            Personaggio goblin = minionQuasiMorto(partita);
            goblin.applicaRisultatoCombattimento(mietitura(partita.gruppo().getCapo(), goblin));

            List<String> frasi = partita.eventi().tutti(NotificaTestoFrase.class).stream()
                    .map(NotificaTestoFrase::getMessaggio).collect(Collectors.toList());
            assertTrue(frasi.contains("Lo Scheletro entra a far parte del gruppo."), frasi.toString());
            assertTrue(frasi.stream().noneMatch(f -> f.contains("felice")), frasi.toString());
        }
    }

    @Test
    void ilCompagnoChePermaneNelGruppoESempreFelice() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(258)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            partita.eventi().ascolta(NotificaTestoFrase.class);
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Reginald", 1));

            assertTrue(partita.eventi().tutti(NotificaTestoFrase.class).stream()
                    .anyMatch(e -> e.getMessaggio().equals("Reginald è felice di poter far parte del gruppo.")));
        }
    }

    @Test
    void loScheletroCheCadeTornaPolvereELasciaIlGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(259)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio goblin = minionQuasiMorto(partita);
            goblin.applicaRisultatoCombattimento(mietitura(partita.gruppo().getCapo(), goblin));
            Personaggio scheletro = partita.gruppo().getPersonaggio(1);
            partita.eventi().ascolta(NotificaTestoFrase.class);

            scheletro.subSalute(scheletro.getSalute() + 100, goblin, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.SI);
            assertFalse(scheletro.isVivo());
            // Non lo si toglie nell'istante in cui cade: lo si congeda fra un'azione e l'altra
            assertEquals(2, partita.gruppo().getNumeroPersonaggi());
            partita.gruppo().congedaChiNonRisorge();

            assertEquals(1, partita.gruppo().getNumeroPersonaggi());
            assertFalse(partita.gruppo().contiene(scheletro));
            List<String> frasi = partita.eventi().tutti(NotificaTestoFrase.class).stream()
                    .map(NotificaTestoFrase::getMessaggio).collect(Collectors.toList());
            assertTrue(frasi.contains("Lo Scheletro torna ad essere polvere."), frasi.toString());
            assertTrue(frasi.stream().noneMatch(f -> f.contains("ferite riportate")), frasi.toString());
        }
    }

    @Test
    void loScheletroRisvegliatoFaVincereIlNecromante() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(256)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            Personaggio goblin = minionQuasiMorto(partita);
            goblin.applicaRisultatoCombattimento(mietitura(partita.gruppo().getCapo(), goblin));
            partita.pubblica(new InternoFineLocazione());

            assertTrue(RegistroTrofei.isVinto(TipoTrofeo.NECROMANTE));
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
            partita.pubblica(new InternoFineLocazione());
            assertFalse(RegistroTrofei.isVinto(TipoTrofeo.NECROMANTE), "senza scheletro non c'è nessun aiuto dall'aldilà");
        }
    }

    private static final String FALCE = "FALCE_DEL_TRISTO_MIETITORE";
    private static final int DADI_DEL_COLPO = 5;

    private static DannoRisultante colpoDellaFalce(PartitaDiTest partita, Personaggio bersaglio) {
        Personaggio capo = partita.gruppo().getCapo();
        capo.addArtefatto(Leggendari.con(FALCE).costruisci(Statistiche.getLivello()));
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
            capo.addArtefatto(Leggendari.con(FALCE).costruisci(Statistiche.getLivello()));
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
