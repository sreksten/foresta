package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPrecaricamentoMotoreCompletato;
import com.threeamigos.foresta.eventi.interni.InternoUiInattiva;
import com.threeamigos.foresta.eventi.interni.InternoUiOccupata;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.FonteDiIntermezzi;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.interfacce.GestoreSalvataggi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Casi dell'automa che i test di scenario non toccano: i costruttori che precaricano da soli, la UI occupata mentre un intermezzo vuole
 * partire, gli intermezzi senza pagine o che non avanzano da soli, un salvataggio che non riesce, e gli stati che non hanno un gestore per i
 * comandi (con la reflection sui campi privati, dove non c'è altro modo di arrivarci).
 */
class AutomaCasiRimastiTest {

    /**
     * Una fonte di intermezzi di prova, che il registro consulta come quella delle missioni: dà gli intermezzi messi in coda nel momento che
     * dicono, finché non sono stati segnati come scattati. Fuori dai test che la usano la coda è vuota e non dà niente.
     */
    private static final class FonteDiProva implements FonteDiIntermezzi {
        static final FonteDiProva ISTANZA = new FonteDiProva();
        final List<IntermezzoDiProva> coda = new ArrayList<>();

        @Override
        public Intermezzo getProssimo(MomentoIntermezzo momento) {
            for (IntermezzoDiProva intermezzo : coda) {
                if (intermezzo.deveScattare(momento) && !ModelloDati.getIstanza().getIntermezziMD().isScattato(intermezzo.getId())) {
                    return intermezzo;
                }
            }
            return null;
        }

        @Override
        public boolean haUnIntermezzoInArrivo(MomentoIntermezzo momento) {
            return false;
        }
    }

    private static final class IntermezzoDiProva implements Intermezzo {
        private final String id;
        private final List<PaginaIntermezzo> pagine;
        private final int secondiPerPagina;

        IntermezzoDiProva(String id, List<PaginaIntermezzo> pagine, int secondiPerPagina) {
            this.id = id;
            this.pagine = pagine;
            this.secondiPerPagina = secondiPerPagina;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public boolean deveScattare(MomentoIntermezzo momento) {
            return momento == MomentoIntermezzo.INIZIO_LOCAZIONE;
        }

        @Override
        public List<PaginaIntermezzo> getPagine() {
            return pagine;
        }

        @Override
        public int getSecondiPerPagina() {
            return secondiPerPagina;
        }
    }

    @AfterEach
    void ripulisce() {
        FonteDiProva.ISTANZA.coda.clear();
    }

    static PartitaDiTest partitaFuoriDallaCitta(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
        partita.assertStato(Stato.IN_LOCAZIONE);
        partita.comando(Comando.ESCI_DA_CITTA);
        partita.assertStato(Stato.SCELTA_DIREZIONE);
        return partita;
    }

    // Un passo qualunque nella Foresta, verso la prima direzione possibile
    static void camminaUnPasso(PartitaDiTest partita) {
        for (Comando direzione : new Comando[]{Comando.NORD, Comando.SUD, Comando.EST, Comando.OVEST}) {
            if (partita.comandiDisponibili().contains(direzione)) {
                partita.comando(direzione);
                partita.comando(Comando.NUMERO_1);
                return;
            }
        }
        throw new AssertionError("nessuna direzione possibile: " + partita.comandiDisponibili());
    }

    static Automa automaDi(PartitaDiTest partita) throws Exception {
        Field campo = PartitaDiTest.class.getDeclaredField("automa");
        campo.setAccessible(true);
        return (Automa) campo.get(partita);
    }

    static void impostaCampo(Object oggetto, String nome, Object valore) throws Exception {
        Field campo = Automa.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(oggetto, valore);
    }

    // ---- costruttori e precaricamento ----

    @Test
    void l_automaCostruitoSenzaEsecutorePrecaricaSuUnThreadAParte() throws Exception {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        try {
            ModelloDati.installa();
            StatoDelMotore.azzera();
            CountDownLatch pronto = new CountDownLatch(1);
            BusEventi.iscriviti(InternoPrecaricamentoMotoreCompletato.class, e -> pronto.countDown());
            Automa automa = new Automa(new TemporizzatoreManuale(), new GestoreSalvataggiInMemoria(), new GestorePunteggiInMemoria());

            automa.inizia();

            assertTrue(pronto.await(120, TimeUnit.SECONDS), "il precaricamento non è finito");
            automa.chiudi();
        } finally {
            BusEventi.azzera();
            BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
            StatoDelMotore.azzera();
        }
    }

    @Test
    void conIlPrecaricamentoInSospesoIlLogoNonPassaAllIntro() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        try {
            ModelloDati.installa();
            StatoDelMotore.azzera();
            // Un esecutore che non esegue mai: il motore non è mai precaricato
            Automa automa = new Automa(new TemporizzatoreManuale(), new GestoreSalvataggiInMemoria(), new GestorePunteggiInMemoria(), comando -> {
            });
            automa.inizia();

            BusEventi.pubblica(new com.threeamigos.foresta.eventi.interni.InternoFineLogoIniziale());

            assertEquals(Stato.LOGO_INIZIALE, automa.getStato());
            automa.chiudi();
        } finally {
            BusEventi.azzera();
            BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
            StatoDelMotore.azzera();
        }
    }

    @Test
    void ilPrecaricamentoCompletatoDopoL_introNonFaNiente() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(91)) {
            partita.assertStato(Stato.INTRO);

            partita.pubblica(new InternoPrecaricamentoMotoreCompletato());

            partita.assertStato(Stato.INTRO);
        }
    }

    // ---- comandi e testi dove non servono ----

    @Test
    void unTestoDovePartitaNonLoChiedeEUnErrore() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(92)) {
            assertThrows(AssertionError.class, () -> partita.testo("un nome"));
        }
    }

    @Test
    void unComandoNullNonSiAccettaEUnoStatoSenzaGestoreSiSegnala() throws Exception {
        try (PartitaDiTest partita = PartitaDiTest.nuova(93)) {
            Automa automa = automaDi(partita);
            assertThrows(IllegalArgumentException.class, () -> automa.processaComando(null));

            impostaCampo(automa, "stato", Stato.INIZIO_GIOCO);
            assertThrows(IllegalStateException.class, () -> automa.processaComando(Comando.NORD));
        }
    }

    @Test
    void conTroppeTransizioniAutomaticheDiFilaLAutomaSiFerma() throws Exception {
        try (PartitaDiTest partita = PartitaDiTest.nuova(94)) {
            Automa automa = automaDi(partita);
            // Uno stato che a ogni comando e a ogni ingresso rimanda a se stesso: un ciclo
            Class<?> esito = Class.forName("com.threeamigos.foresta.motore.Automa$Esito");
            Field continua = esito.getDeclaredField("CONTINUA_CON_INGRESSO");
            continua.setAccessible(true);
            Object ciclo = continua.get(null);
            Field ingressi = Automa.class.getDeclaredField("gestoriIngresso");
            ingressi.setAccessible(true);
            Field comandi = Automa.class.getDeclaredField("gestoriComando");
            comandi.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Stato, java.util.function.Supplier<Object>> mappaIngressi = (java.util.Map<Stato, java.util.function.Supplier<Object>>) ingressi.get(automa);
            @SuppressWarnings("unchecked")
            java.util.Map<Stato, java.util.function.Function<Comando, Object>> mappaComandi = (java.util.Map<Stato, java.util.function.Function<Comando, Object>>) comandi.get(automa);
            mappaIngressi.put(Stato.CONFERMA_USCITA, () -> ciclo);
            mappaComandi.put(Stato.CONFERMA_USCITA, comando -> ciclo);
            impostaCampo(automa, "stato", Stato.CONFERMA_USCITA);

            assertThrows(IllegalStateException.class, () -> automa.processaComando(Comando.SI));
        }
    }

    @Test
    void daPunteggiQualunqueComandoRiportaAllIntro() throws Exception {
        try (PartitaDiTest partita = PartitaDiTest.nuova(95)) {
            Automa automa = automaDi(partita);
            impostaCampo(automa, "stato", Stato.PUNTEGGI);

            automa.processaComando(Comando.PERGAMENA);

            assertEquals(Stato.INTRO, automa.getStato());
        }
    }

    @Test
    void unNomeSegretoFaGiocareUnPersonaggioNascosto() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(96)) {
            partita.comando(Comando.PERGAMENA);

            partita.testo("OmbraFiamma");

            assertEquals("Alakazam", partita.gruppo().getCapo().getNomeProprio().orElse(""));
        }
    }

    @Test
    void caricareDaUnoSlotVuotoOAnnullareRiportaAllIntro() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(97)) {
            partita.comando(Comando.FLOPPY_CARICA);
            partita.assertStato(Stato.PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE);
            partita.comando(Comando.NUMERO_3);
            partita.assertStato(Stato.INTRO);

            partita.comando(Comando.FLOPPY_CARICA);
            partita.comando(Comando.ANNULLA);
            partita.assertStato(Stato.INTRO);
        }
    }

    // ---- la UI occupata e gli intermezzi ----

    @Test
    void conLaUiOccupataUnIntermezzoAspettaECheLaUiTorniInattiva() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(101)) {
            partita.nonSaltareIntermezzi();
            // La UI che torna inattiva senza che nessuno l'aspetti non cambia niente
            partita.pubblica(new InternoUiInattiva());
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            RegistroIntermezzi.aggiungiFonte(FonteDiProva.ISTANZA);
            FonteDiProva.ISTANZA.coda.add(new IntermezzoDiProva("PROVA_ATTESA", Collections.singletonList(new PaginaIntermezzo("Una pagina")), 8));
            partita.pubblica(new InternoUiOccupata());

            camminaUnPasso(partita);

            partita.assertStato(Stato.ATTESA_UI_PER_INTERMEZZO);
            // I comandi nel frattempo si ignorano
            partita.comando(Comando.PERGAMENA);
            partita.assertStato(Stato.ATTESA_UI_PER_INTERMEZZO);
            partita.pubblica(new InternoUiInattiva());
            partita.assertStato(Stato.INTERMEZZO);
            partita.comando(Comando.PERGAMENA);
            partita.assertStato(Stato.IN_LOCAZIONE);
        }
    }

    @Test
    void gliIntermezziSenzaPagineSiSaltano() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(102)) {
            partita.nonSaltareIntermezzi();
            RegistroIntermezzi.aggiungiFonte(FonteDiProva.ISTANZA);
            FonteDiProva.ISTANZA.coda.add(new IntermezzoDiProva("PROVA_VUOTO", Collections.emptyList(), 8));
            FonteDiProva.ISTANZA.coda.add(new IntermezzoDiProva("PROVA_NULLO", null, 8));

            camminaUnPasso(partita);

            partita.assertStato(Stato.IN_LOCAZIONE);
            assertTrue(ModelloDati.getIstanza().getIntermezziMD().isScattato("PROVA_VUOTO"));
            assertTrue(ModelloDati.getIstanza().getIntermezziMD().isScattato("PROVA_NULLO"));
        }
    }

    @Test
    void duePaginePiuIntermezziDiFilaESenzaAvanzamentoAutomatico() throws Exception {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(103)) {
            partita.nonSaltareIntermezzi();
            RegistroIntermezzi.aggiungiFonte(FonteDiProva.ISTANZA);
            // Il primo non avanza da solo, il secondo sì; entrambi di una pagina sola
            FonteDiProva.ISTANZA.coda.add(new IntermezzoDiProva("PROVA_UNO", Collections.singletonList(new PaginaIntermezzo("Uno")), 0));
            FonteDiProva.ISTANZA.coda.add(new IntermezzoDiProva("PROVA_DUE", Collections.singletonList(new PaginaIntermezzo("Due")), 8));

            camminaUnPasso(partita);
            partita.assertStato(Stato.INTERMEZZO);
            // Con il ritorno alla schermata di gioco in sospeso, un secondo intermezzo non la manda
            impostaCampo(automaDi(partita), "mostraSchermataGiocoAllaRipresa", true);
            partita.comando(Comando.PERGAMENA);
            partita.assertStato(Stato.INTERMEZZO);
            partita.comando(Comando.PERGAMENA);
            partita.assertStato(Stato.IN_LOCAZIONE);
        }
    }

    // ---- il salvataggio che non riesce ----

    private static final class GestoreCheNonSalva implements GestoreSalvataggi {
        @Override
        public List<TestataSalvataggio> getSalvataggiDisponibili() {
            return Collections.emptyList();
        }

        @Override
        public boolean leggi(Comando id) {
            return false;
        }

        @Override
        public boolean salva(Comando id) {
            return false;
        }

        @Override
        public void elimina(Comando id) {
        }
    }

    @Test
    void seIlSalvataggioAutomaticoNonRiescePassandoInBackgroundSiAvvisa() throws Exception {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(104)) {
            impostaCampo(automaDi(partita), "gestoreSalvataggi", new GestoreCheNonSalva());
            partita.eventi().ascolta(com.threeamigos.foresta.eventi.interni.InternoMessaggio.class);

            partita.inBackground();

            assertTrue(partita.eventi().tutti(com.threeamigos.foresta.eventi.interni.InternoMessaggio.class).stream()
                    .anyMatch(m -> m.getMessaggioInterno().contains("Salvataggio automatico non riuscito")));
        }
    }

    @Test
    void seIlSalvataggioManualeNonRiesceSiTornaAlGioco() throws Exception {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(105)) {
            impostaCampo(automaDi(partita), "gestoreSalvataggi", new GestoreCheNonSalva());
            partita.comando(Comando.FLOPPY_SALVA);
            partita.assertStato(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE);
            // Un comando che non è uno slot si ignora
            partita.comando(Comando.NORD);
            partita.assertStato(Stato.SELEZIONE_SALVATAGGIO_DA_SCRIVERE);

            partita.comando(Comando.NUMERO_1);

            partita.assertStato(Stato.SCELTA_DIREZIONE);
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("Il salvataggio non è riuscito")));
        }
    }

    // ---- le scelte annullate ----

    @Test
    void lePozioniSiPossonoAnnullareNellaScelta() {
        try (PartitaDiTest partita = partitaFuoriDallaCitta(106)) {
            GruppoGiocatore gruppo = partita.gruppo();
            gruppo.aggiungiPersonaggio(new Guerriero("Secondo", 1));
            gruppo.addPozioniSalute(1);
            gruppo.addPozioniSaluteGrande(1);
            gruppo.addPozioniMagia(1);
            gruppo.addPozioniMagiaGrande(1);
            gruppo.addPozioniAumentoLivello(1);
            for (Comando pozione : new Comando[]{Comando.POZIONE_SALUTE, Comando.POZIONE_SALUTE_GRANDE, Comando.POZIONE_MAGIA,
                    Comando.POZIONE_MAGIA_GRANDE, Comando.POZIONE_AUMENTO_LIVELLO}) {
                partita.assertStato(Stato.SCELTA_DIREZIONE);
                partita.comando(pozione);
                partita.assertStato(Stato.SCELTA_MANUALE_PERSONAGGIO);
                partita.comando(Comando.ANNULLA);
                partita.assertStato(Stato.SCELTA_DIREZIONE);
            }
        }
    }
}
