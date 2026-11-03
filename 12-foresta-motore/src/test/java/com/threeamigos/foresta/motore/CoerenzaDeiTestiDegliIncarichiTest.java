package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.incarichi.IncaricoInCitta;
import com.threeamigos.foresta.missioni.incarichi.RichiestaDiMateriali;
import com.threeamigos.foresta.missioni.incarichi.TestiDiUnIncarico;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.oggetti.RaccoltaBottino;
import com.threeamigos.foresta.testi.Testi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I testi di un incarico in città, presi tutti insieme nell'ordine in cui il giocatore li incontra (il titolo, la scena
 * dell'incarico, l'accettazione, le descrizioni del diario, la fine di un passo, il ringraziamento, la consegna, la
 * ricompensa), devono essere coerenti fra loro: nessun segnaposto rimasto da riempire, frasi che cominciano con la
 * maiuscola e finiscono con la punteggiatura, e gli stessi valori (città, quantità, monete) dove devono ricorrere.
 * Si controlla in tutte le lingue.
 */
class CoerenzaDeiTestiDegliIncarichiTest {

    private static final String[] LINGUE = {"it", "en", "en-GB"};

    @Test
    void ITestiDellaCacciaAiGoblinParlanoDelloStessoIncarico() {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(61)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                        () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
                CacciaAiGoblin caccia = prendiIncaricoAllaSecondaVisita(trova(CacciaAiGoblin.class));
                String dove = lingua + ": ";
                String monete = "15";
                String goblin = String.valueOf(CacciaAiGoblin.GOBLIN_DA_SCONFIGGERE);

                String nome = caccia.getNome();
                assertEquals(nome, caccia.getNome(), dove + "il titolo non cambia da una lettura all'altra");
                assertEquals(nome, caccia.getModelloDati().getNome(), dove + "il titolo è quello conservato nei dati");
                List<String> scena = TestiDiUnIncarico.sceneDellIncarico(caccia);
                String accettazione = TestiDiUnIncarico.accettazione(caccia);
                String durante = caccia.getDescrizione();
                for (int i = 0; i < CacciaAiGoblin.GOBLIN_DA_SCONFIGGERE; i++) {
                    partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.GOBLIN));
                }
                caccia.controllaPostLocazione();
                String fineCaccia = partita.ultimoTesto();
                String ritorno = caccia.getDescrizione();
                List<String> ringraziamento = TestiDiUnIncarico.sceneDelRingraziamento(caccia);
                String ricompensa = TestiDiUnIncarico.ricompensa(caccia);

                List<String> frasi = new ArrayList<>(scena);
                frasi.addAll(Arrays.asList(accettazione, durante, fineCaccia, ritorno));
                frasi.addAll(ringraziamento);
                frasi.add(ricompensa);
                frasi.forEach(f -> controllaFrase(dove, f));
                assertFalse(nome.isEmpty(), dove + "il titolo");

                String lettaScena = String.join(" ", scena);
                assertTrue(lettaScena.contains(goblin) && lettaScena.contains(monete), dove + lettaScena);
                assertTrue(accettazione.contains(goblin) && accettazione.contains(monete), dove + accettazione);
                assertTrue(durante.contains(goblin) && durante.contains("0"), dove + durante);
                assertTrue(ricompensa.contains(monete), dove + ricompensa);
                for (String testo : Arrays.asList(durante, fineCaccia, ritorno)) {
                    assertTrue(testo.contains("Fleena"), dove + "la città dev'essere la stessa: " + testo);
                }
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    @Test
    void ITestiDellaRichiestaDiMaterialiParlanoDelloStessoIncarico() {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(62)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                        () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
                RichiestaDiMateriali richiesta = prendiIncaricoAllaSecondaVisita(Alchimie.conLaMandragola());
                String dove = lingua + ": ";
                String quanti = richiesta.getMateriale().quanti(Alchimie.RADICI);
                String plurale = richiesta.getMateriale().getPlurale();
                String monete = "25";

                String nome = richiesta.getNome();
                assertEquals(nome, richiesta.getNome(), dove + "il titolo non cambia da una lettura all'altra");
                assertEquals(nome, richiesta.getModelloDati().getNome(), dove + "il titolo è quello conservato nei dati");
                List<String> scena = TestiDiUnIncarico.sceneDellIncarico(richiesta);
                String accettazione = TestiDiUnIncarico.accettazione(richiesta);
                String raccolta = richiesta.getDescrizione();
                RaccoltaBottino.raccogli(new OggettoMissione(richiesta.getClasse().name(), RichiestaDiMateriali.MATERIALE,
                        richiesta.getMateriali().getNome(), Alchimie.RADICI), partita.gruppo(), null);
                richiesta.controllaPostLocazione();
                String pronti = partita.ultimoTesto();
                String ritorno = richiesta.getDescrizione();
                List<String> ringraziamento = TestiDiUnIncarico.sceneDelRingraziamento(richiesta);
                String consegna = TestiDiUnIncarico.consegna(richiesta);
                String ricompensa = TestiDiUnIncarico.ricompensa(richiesta);

                List<String> frasi = new ArrayList<>(scena);
                frasi.addAll(Arrays.asList(accettazione, raccolta, pronti, ritorno, consegna, ricompensa));
                frasi.addAll(ringraziamento);
                frasi.forEach(f -> controllaFrase(dove, f));
                assertFalse(nome.isEmpty(), dove + "il titolo");

                String lettaScena = String.join(" ", scena);
                for (String testo : Arrays.asList(lettaScena, accettazione, raccolta)) {
                    assertTrue(testo.contains(quanti), dove + "la quantità dev'essere la stessa (" + quanti + "): " + testo);
                }
                assertTrue(lettaScena.contains(monete) && accettazione.contains(monete) && ricompensa.contains(monete),
                        dove + "le monete dell'accordo (" + monete + ")");
                assertTrue(String.join(" ", ringraziamento).contains(plurale), dove + "i materiali consegnati (" + plurale + ")");
                assertTrue(raccolta.contains("0"), dove + raccolta);
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    /**
     * Una frase pronta da mostrare: senza segnaposto o valori mancanti, con l'iniziale maiuscola e la punteggiatura finale.
     */
    private static void controllaFrase(String dove, String frase) {
        String descrizione = dove + "«" + frase + "»";
        assertNotNull(frase, descrizione);
        assertFalse(frase.trim().isEmpty(), descrizione);
        assertFalse(frase.matches("(?s).*%[A-Z_0-9]+%.*"), descrizione + ": segnaposto rimasto");
        assertFalse(frase.contains("{") || frase.contains("}"), descrizione + ": segnaposto rimasto");
        assertFalse(frase.contains("null"), descrizione + ": valore mancante");
        assertFalse(frase.contains("  "), descrizione + ": spazi doppi");
        assertEquals(frase, frase.trim(), descrizione + ": spazi ai bordi");
        int iniziale = frase.codePointAt(0);
        assertTrue(Character.isUpperCase(iniziale) || "“\"«¡¿".indexOf(iniziale) >= 0, descrizione + ": non comincia con la maiuscola");
        char ultimo = frase.charAt(frase.length() - 1);
        assertTrue(".!?:”\"…".indexOf(ultimo) >= 0, descrizione + ": non finisce con la punteggiatura");
    }

    private static <T extends IncaricoInCitta> T prendiIncaricoAllaSecondaVisita(T incarico) {
        incarico.controllaPreLocazione();
        assertEquals("ACCETTAZIONE", incarico.getPassoCorrente(), incarico.getClass().getSimpleName());
        incarico.segnaIntermezzoPassoMostrato("INCARICO");
        incarico.controllaInLocazione();
        assertTrue(incarico.isAttiva());
        return incarico;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Missione> T trova(Class<T> tipo) {
        return (T) RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
