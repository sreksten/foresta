package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.IlPellegrino;
import com.threeamigos.foresta.missioni.IncaricoInCitta;
import com.threeamigos.foresta.missioni.LAlchimistaELaMandragola;
import com.threeamigos.foresta.missioni.LaTagliaSullaBanda;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I passi COMBATTI, SCORTA (con lo scortato come ospite del gruppo) e CONSEGNA (passi_missioni.md, §2) sugli incarichi in città che li usano: la taglia su
 * una banda di hobgoblin, il pellegrino e le radici di mandragola.
 */
class ScenarioCombattiScortaConsegnaTest {

    @Test
    void nelCovoDiSgranfCiSonoLuiELaSuaBandaESconfittiSiTornaARiscuotere() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(71)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            LaTagliaSullaBanda taglia = prendiIncaricoAllaSecondaVisita(LaTagliaSullaBanda.class);
            CoordinateMD covo = taglia.getCovo();
            assertNotNull(covo, "la missione ha trovato il bosco della banda");
            assertEquals(ClassiLocazione.BOSCO, Foresta.getLocazione(covo));
            assertTrue(Foresta.isLocazioneConosciuta(covo));
            assertEquals("CACCIA", taglia.getPassoCorrente());

            // Altrove la banda non c'è; nel covo sì, con Sgranf in testa un livello sopra gli altri
            assertEquals(Optional.empty(), RegistroMissioni.getIncontroMissione(new CoordinateMD(covo.getX(), covo.getY() + 1)));
            List<Personaggio> banda = RegistroMissioni.getIncontroMissione(covo).orElseThrow(AssertionError::new);
            assertEquals(3, banda.size());
            banda.forEach(p -> assertEquals(ClassePersonaggio.HOBGOBLIN, p.getClasse()));
            assertEquals(taglia.getCapobanda(), banda.get(0).getNome());
            assertEquals(banda.get(1).getLivello() + 1, banda.get(0).getLivello());

            // Entrando nel covo si trova la banda, al posto degli avversari del bosco
            partita.comando(Comando.ESCI_DA_CITTA);
            entraDaSud(partita, covo);
            assertEquals(3, GruppoAvversario.getIstanza().getNumeroPersonaggi());
            assertEquals(taglia.getCapobanda(), GruppoAvversario.getIstanza().getCapo().getNome());

            // Gli hobgoblin sconfitti altrove non contano
            partita.gruppo().setCoordinate(new CoordinateMD(covo.getX(), covo.getY() + 1));
            for (int i = 0; i < 3; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.HOBGOBLIN));
            }
            partita.gruppo().setCoordinate(covo);
            taglia.controllaPostLocazione();
            assertEquals("CACCIA", taglia.getPassoCorrente(), "quelli sconfitti fuori dal covo non contano");

            // Nel covo, due non bastano, tre sì
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.HOBGOBLIN));
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.HOBGOBLIN));
            taglia.controllaPostLocazione();
            assertEquals("CACCIA", taglia.getPassoCorrente());
            partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.HOBGOBLIN));
            taglia.controllaPostLocazione();
            assertEquals("RITORNO", taglia.getPassoCorrente());
            assertEquals(Optional.empty(), RegistroMissioni.getIncontroMissione(covo), "la banda non c'è più");
        }
    }

    @Test
    void anselmoViaggiaColGruppoComeOspiteESeNeSeparaAlTempio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(72)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlPellegrino pellegrino = prendiIncaricoAllaSecondaVisita(IlPellegrino.class);
            CoordinateMD tempio = pellegrino.getTempio();
            assertEquals(ClassiLocazione.TEMPIO, Foresta.getLocazione(tempio));
            assertEquals("VIAGGIO", pellegrino.getPassoCorrente());
            Personaggio anselmo = pellegrino.getScortato().orElseThrow(AssertionError::new);
            assertEquals(pellegrino.getPellegrino(), anselmo.getNome());
            assertEquals(ClassePersonaggio.VIANDANTE, anselmo.getClasse());
            assertTrue(partita.gruppo().getOspiti().contains(anselmo));
            assertFalse(partita.gruppo().getPersonaggi().contains(anselmo), "un ospite non combatte");

            partita.gruppo().setCoordinate(tempio);
            pellegrino.controllaPreLocazione();
            assertEquals("RITORNO", pellegrino.getPassoCorrente());
            assertTrue(partita.gruppo().getOspiti().isEmpty(), "Anselmo resta al tempio");
        }
    }

    @Test
    void anselmoViaggiaAncheColGruppoPienoESenzaContare() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(74)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            while (partita.gruppo().getNumeroPersonaggi() < Costanti.MAX_PERSONAGGI_GRUPPO_TOTALE) {
                partita.gruppo().aggiungiPersonaggioSenzaNotificare(new com.threeamigos.foresta.personaggi.Guerriero("Compagno", 1));
            }
            IlPellegrino pellegrino = prendiIncaricoAllaSecondaVisita(IlPellegrino.class);
            assertTrue(pellegrino.getScortato().isPresent());
            assertEquals(Costanti.MAX_PERSONAGGI_GRUPPO_TOTALE, partita.gruppo().getNumeroPersonaggi());
        }
    }

    @Test
    void anselmoSopravviveAUnSalvataggioESeLaMissioneFallisceSiSeparaDalGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(73)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            prendiIncaricoAllaSecondaVisita(IlPellegrino.class);

            GestoreSalvataggi.salva(Comando.NUMERO_2);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
            IlPellegrino pellegrino = RegistroMissioni.getTutteLeMissioni().stream().filter(IlPellegrino.class::isInstance)
                    .map(IlPellegrino.class::cast).findFirst().orElseThrow(AssertionError::new);
            Personaggio anselmo = pellegrino.getScortato().orElseThrow(() -> new AssertionError("Anselmo dopo il caricamento"));
            assertEquals(pellegrino.getPellegrino(), anselmo.getNome());

            pellegrino.fallisciMissione();
            assertTrue(partita.gruppo().getOspiti().isEmpty());
        }
    }

    @Test
    void alRitornoLeRadiciSiConsegnanoPrimaDellaRicompensa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(75)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            LAlchimistaELaMandragola mandragola = prendiIncaricoAllaSecondaVisita(LAlchimistaELaMandragola.class);
            new OggettoMissione(mandragola.getId(), LAlchimistaELaMandragola.MANDRAGOLA,
                    LAlchimistaELaMandragola.RADICI_DI_MANDRAGOLA.getNome(), LAlchimistaELaMandragola.RADICI)
                    .prendi(partita.gruppo(), null);
            mandragola.controllaPostLocazione();
            assertEquals("RITORNO", mandragola.getPassoCorrente());

            int monete = partita.gruppo().getMonete();
            mandragola.controllaPreLocazione();
            mandragola.controllaInLocazione();
            assertEquals(0, mandragola.getContatore(LAlchimistaELaMandragola.MANDRAGOLA), "le radici sono passate all'alchimista");
            assertEquals(monete + 25, partita.gruppo().getMonete());
            assertTrue(mandragola.isCompleta());
            List<String> testi = partita.testi();
            int consegna = indice(testi, "radici di mandragola passano all'alchimista");
            int ricompensa = indice(testi, "L'alchimista paga");
            assertTrue(consegna >= 0 && ricompensa > consegna, String.valueOf(testi));
            assertTrue(testi.get(consegna).startsWith("Le quattro radici"), testi.get(consegna));
        }
    }

    private static int indice(List<String> testi, String frammento) {
        for (int i = 0; i < testi.size(); i++) {
            if (testi.get(i).contains(frammento)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Mette il gruppo nella casella a sud e lo fa entrare andando a nord.
     */
    private static void entraDaSud(PartitaDiTest partita, CoordinateMD destinazione) {
        partita.gruppo().setCoordinate(new CoordinateMD(destinazione.getX(), destinazione.getY() + 1));
        partita.assertStato(Stato.SCELTA_DIREZIONE);
        partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
        assertEquals(destinazione, partita.gruppo().getCoordinate());
    }

    /**
     * Come se il gruppo tornasse nella città in cui si trova: l'intermezzo della prima visita è già stato mostrato e
     * l'incarico si può prendere.
     */
    private static <T extends IncaricoInCitta> T prendiIncaricoAllaSecondaVisita(Class<T> tipo) {
        T incarico = RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast)
                .findFirst().orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
        incarico.controllaPreLocazione();
        assertEquals("ACCETTAZIONE", incarico.getPassoCorrente(), tipo.getSimpleName());
        incarico.segnaIntermezzoPassoMostrato("INCARICO");
        incarico.controllaInLocazione();
        assertTrue(incarico.isAttiva());
        return incarico;
    }
}
