package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.incarichi.IlContrabbandiere;
import com.threeamigos.foresta.missioni.incarichi.CacciatoreDiTaglie;
import com.threeamigos.foresta.missioni.incarichi.IlCartografo;
import com.threeamigos.foresta.missioni.incarichi.IlColpo;
import com.threeamigos.foresta.missioni.incarichi.IlCorriere;
import com.threeamigos.foresta.missioni.incarichi.IlNegozio;
import com.threeamigos.foresta.missioni.incarichi.IlNegozioDellAlchimista;
import com.threeamigos.foresta.missioni.incarichi.IlNegozioDellArmaiolo;
import com.threeamigos.foresta.missioni.incarichi.IlNegozioDellIncantatore;
import com.threeamigos.foresta.missioni.incarichi.IlNegozioDelVenditoreDiPergamene;
import com.threeamigos.foresta.missioni.incarichi.IlPellegrino;
import com.threeamigos.foresta.missioni.incarichi.IlRapimento;
import com.threeamigos.foresta.missioni.incarichi.IlRituale;
import com.threeamigos.foresta.missioni.incarichi.IlSoccorso;
import com.threeamigos.foresta.missioni.incarichi.IlTorneo;
import com.threeamigos.foresta.missioni.incarichi.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.incarichi.IncaricoInCitta;
import com.threeamigos.foresta.missioni.incarichi.LIndagine;
import com.threeamigos.foresta.missioni.incarichi.LaForgia;
import com.threeamigos.foresta.missioni.incarichi.LOggettoSmarrito;
import com.threeamigos.foresta.missioni.incarichi.LaDocumentazione;
import com.threeamigos.foresta.missioni.incarichi.LaTagliaSullaBanda;
import com.threeamigos.foresta.missioni.incarichi.LaSorveglianza;
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

    @Test
    void ITestiDelCorriereEDelContrabbandiereParlanoDelloStessoIncarico() {
        for (Class<? extends IlCorriere> classe : Arrays.asList(IlCorriere.class, IlContrabbandiere.class)) {
            for (String lingua : LINGUE) {
                Testi.impostaLingua(lingua);
                try (PartitaDiTest partita = PartitaDiTest.nuova(63)) {
                    partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                            () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
                    IlCorriere corriere = prendiIncaricoAllaSecondaVisita(trova(classe));
                    String dove = classe.getSimpleName() + " " + lingua + ": ";
                    String monete = String.valueOf(TestiDiUnIncarico.monete(corriere));
                    String ore = String.valueOf(corriere.getOreConcesse());
                    boolean urgente = corriere.getSpedizione().isUrgente();
                    String citta = corriere.getDestinazione().name().substring("CITTA_".length());

                    String nome = corriere.getNome();
                    assertEquals(nome, corriere.getNome(), dove + "il titolo non cambia da una lettura all'altra");
                    assertEquals(nome, corriere.getModelloDati().getNome(), dove + "il titolo è quello conservato nei dati");
                    List<String> scena = TestiDiUnIncarico.sceneDellIncarico(corriere);
                    String accettazione = TestiDiUnIncarico.accettazione(corriere);
                    String descrizione = corriere.getDescrizione();
                    List<String> ringraziamento = TestiDiUnIncarico.sceneDelRingraziamento(corriere);
                    String consegna = TestiDiUnIncarico.consegna(corriere);
                    String ricompensa = TestiDiUnIncarico.ricompensa(corriere);

                    List<String> frasi = new ArrayList<>(scena);
                    frasi.addAll(Arrays.asList(accettazione, descrizione, consegna, ricompensa));
                    frasi.addAll(ringraziamento);
                    frasi.forEach(f -> controllaFrase(dove, f));
                    assertFalse(nome.isEmpty(), dove + "il titolo");

                    String lettaScena = String.join(" ", scena);
                    for (String testo : Arrays.asList(lettaScena, accettazione, descrizione, String.join(" ", ringraziamento), ricompensa)) {
                        assertTrue(testo.contains(monete), dove + "le monete dell'accordo (" + monete + "): " + testo);
                    }
                    if (urgente) {
                        assertTrue(lettaScena.contains(ore) && accettazione.contains(ore), dove + "le ore concesse (" + ore + ")");
                        assertTrue(descrizione.contains(String.valueOf(corriere.getOreRimaste())), dove + descrizione);
                    }
                    assertEquals(IlContrabbandiere.class.equals(classe), lettaScena.contains(contrabbandoNelleTreLingue(lingua)),
                            dove + "solo il contrabbandiere chiede di non farsi notare: " + lettaScena);
                    assertTrue(nome.toLowerCase(java.util.Locale.ROOT).contains(citta.toLowerCase(java.util.Locale.ROOT)), dove + "il titolo parla della destinazione: " + nome);
                    assertTrue(descrizione.toLowerCase(java.util.Locale.ROOT).contains(citta.toLowerCase(java.util.Locale.ROOT)), dove + descrizione);
                } finally {
                    Testi.impostaLingua("it");
                }
            }
        }
    }

    @Test
    void ITestiDellaSorveglianzaParlanoDelloStessoIncarico() {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(64)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                        () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
                LaSorveglianza sorveglianza = prendiIncaricoAllaSecondaVisita(trova(LaSorveglianza.class));
                String dove = lingua + ": ";
                String monete = String.valueOf(TestiDiUnIncarico.monete(sorveglianza));
                String visite = String.valueOf(sorveglianza.getSorveglianza().getVisite());
                String ore = String.valueOf(sorveglianza.getSorveglianza().getOre());

                String nome = sorveglianza.getNome();
                assertEquals(nome, sorveglianza.getNome(), dove + "il titolo non cambia da una lettura all'altra");
                List<String> scena = TestiDiUnIncarico.sceneDellIncarico(sorveglianza);
                String accettazione = TestiDiUnIncarico.accettazione(sorveglianza);
                String descrizione = sorveglianza.getDescrizione();
                List<String> ringraziamento = TestiDiUnIncarico.sceneDelRingraziamento(sorveglianza);
                String ricompensa = TestiDiUnIncarico.ricompensa(sorveglianza);
                List<String> durante = TestiDiUnIncarico.testiDuranteLaSorveglianza(sorveglianza);

                List<String> frasi = new ArrayList<>(scena);
                frasi.addAll(Arrays.asList(accettazione, descrizione, ricompensa));
                frasi.addAll(ringraziamento);
                frasi.addAll(durante);
                frasi.forEach(f -> controllaFrase(dove, f));
                assertFalse(nome.isEmpty(), dove + "il titolo");

                String lettaScena = String.join(" ", scena);
                assertTrue(lettaScena.contains(monete) && accettazione.contains(monete) && ricompensa.contains(monete)
                        && String.join(" ", ringraziamento).contains(monete), dove + "le monete dell'accordo (" + monete + ")");
                assertTrue(lettaScena.contains(ore) && descrizione.contains(ore), dove + "le ore fra una visita e l'altra (" + ore + ")");
                assertTrue(durante.get(durante.size() - 1).contains("Fleena"), dove + "l'attesa nomina la città: " + durante);
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    @Test
    void ITestiDelSoccorsoParlanoDelloStessoIncarico() {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(65)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                        () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
                IlSoccorso soccorso = prendiIncaricoAllaSecondaVisita(trova(IlSoccorso.class));
                String dove = lingua + ": ";
                String monete = String.valueOf(TestiDiUnIncarico.monete(soccorso));
                String persona = soccorso.getNomeDellaPersona();

                String nome = soccorso.getNome();
                assertEquals(nome, soccorso.getNome(), dove + "il titolo non cambia da una lettura all'altra");
                List<String> scena = TestiDiUnIncarico.sceneDellIncarico(soccorso);
                String accettazione = TestiDiUnIncarico.accettazione(soccorso);
                String descrizione = soccorso.getDescrizione();
                List<String> ringraziamento = TestiDiUnIncarico.sceneDelRingraziamento(soccorso);
                String ricompensa = TestiDiUnIncarico.ricompensa(soccorso);
                List<String> durante = TestiDiUnIncarico.testiDuranteIlSoccorso(soccorso);

                List<String> frasi = new ArrayList<>(scena);
                frasi.addAll(Arrays.asList(accettazione, descrizione, ricompensa));
                frasi.addAll(ringraziamento);
                frasi.addAll(durante);
                frasi.forEach(f -> controllaFrase(dove, f));
                assertFalse(nome.isEmpty(), dove + "il titolo");

                assertTrue(String.join(" ", scena).contains(monete) && accettazione.contains(monete) && ricompensa.contains(monete)
                        && String.join(" ", ringraziamento).contains(monete), dove + "le monete dell'accordo (" + monete + ")");
                assertTrue(accettazione.contains(persona) && descrizione.contains(persona), dove + "la persona (" + persona + ")");
                assertTrue(durante.get(durante.size() - 1).contains(persona) && durante.get(durante.size() - 2).contains("Fleena"), dove + durante);
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    @Test
    void ITestiDelRapimentoParlanoDelloStessoIncarico() {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(66)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                        () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
                IlRapimento rapimento = prendiIncaricoAllaSecondaVisita(trova(IlRapimento.class));
                String dove = lingua + ": ";
                String monete = String.valueOf(TestiDiUnIncarico.monete(rapimento));
                String ostaggio = rapimento.getOstaggio();
                String capobanda = rapimento.getCapobanda();

                String nome = rapimento.getNome();
                assertEquals(nome, rapimento.getNome(), dove + "il titolo non cambia da una lettura all'altra");
                assertEquals(nome, rapimento.getModelloDati().getNome(), dove + "il titolo è quello conservato nei dati");
                List<String> scena = TestiDiUnIncarico.sceneDellIncarico(rapimento);
                String accettazione = TestiDiUnIncarico.accettazione(rapimento);
                String descrizione = rapimento.getDescrizione();
                List<String> ringraziamento = TestiDiUnIncarico.sceneDelRingraziamento(rapimento);
                String ricompensa = TestiDiUnIncarico.ricompensa(rapimento);
                List<String> durante = TestiDiUnIncarico.testiDuranteIlRapimento(rapimento);

                List<String> frasi = new ArrayList<>(scena);
                frasi.addAll(Arrays.asList(accettazione, descrizione, ricompensa));
                frasi.addAll(ringraziamento);
                frasi.addAll(durante);
                frasi.forEach(f -> controllaFrase(dove, f));

                String lettaScena = String.join(" ", scena);
                assertTrue(lettaScena.contains(ostaggio) && lettaScena.contains(capobanda) && lettaScena.contains(monete), dove + lettaScena);
                assertTrue(nome.contains(ostaggio) && accettazione.contains(ostaggio) && accettazione.contains(monete) && ricompensa.contains(monete), dove + accettazione);
                assertTrue(descrizione.contains(capobanda) && descrizione.contains("Fleena"), dove + descrizione);
                assertTrue(String.join(" ", ringraziamento).contains(monete), dove + ringraziamento);
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    @Test
    void ITestiDelColpoParlanoDelloStessoIncarico() {
        controllaUnIncarico(IlColpo.class, 67, new String[] {"COVO", "COLPO", "RITORNO"});
    }

    /**
     * Per gli incarichi portati alla grammatica: le scene, l'accettazione, la ricompensa, la descrizione di ogni passo e gli altri testi
     * dati, tutti in tre lingue, senza segnaposto rimasti, con le monete dell'accordo dove devono stare e la città nelle descrizioni.
     */
    private static <T extends IncaricoInCitta> void controllaUnIncarico(Class<T> classe, long seme, String[] passi, String... altriTesti) {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                        () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
                T daPrendere = trova(classe);
                if (daPrendere instanceof IlNegozio) { // la bottega si offre solo nella città che le è stata assegnata
                    partita.spostaGruppoIn(((IlNegozio) daPrendere).getCittaAssegnata());
                }
                T incarico = prendiIncaricoAllaSecondaVisita(daPrendere);
                String dove = classe.getSimpleName() + " " + lingua + ": ";
                String monete = String.valueOf(TestiDiUnIncarico.monete(incarico));

                String nome = incarico.getNome();
                assertEquals(nome, incarico.getNome(), dove + "il titolo non cambia da una lettura all'altra");
                assertFalse(nome.isEmpty(), dove + "il titolo");
                List<String> scena = TestiDiUnIncarico.sceneDellIncarico(incarico);
                String accettazione = TestiDiUnIncarico.accettazione(incarico);
                String descrizione = incarico.getDescrizione();
                List<String> ringraziamento = TestiDiUnIncarico.sceneDelRingraziamento(incarico);
                String ricompensa = TestiDiUnIncarico.ricompensa(incarico);
                List<String> durante = TestiDiUnIncarico.testiDuranteLaMissione(incarico, passi, altriTesti);

                List<String> frasi = new ArrayList<>(scena);
                frasi.addAll(Arrays.asList(accettazione, descrizione, ricompensa));
                frasi.addAll(ringraziamento);
                frasi.addAll(durante);
                frasi.forEach(f -> controllaFrase(dove, f));

                if (!"0".equals(monete)) { // la forgia paga con un'arma, non con monete
                    assertTrue(ricompensa.contains(monete), dove + "le monete della ricompensa (" + monete + "): " + ricompensa);
                    assertTrue(String.join(" ", scena).contains(monete) || String.join(" ", ringraziamento).contains(monete), dove + "le monete nelle scene (" + monete + ")");
                }
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    @Test
    void ITestiDelCartografoParlanoDelloStessoIncarico() {
        controllaUnIncarico(IlCartografo.class, 68, new String[] {"ESPLORAZIONE", "RITORNO"});
    }

    @Test
    void ITestiDelRitualeParlanoDelloStessoIncarico() {
        controllaUnIncarico(IlRituale.class, 70, new String[] {"LUOGO", "RACCOLTA", "RITO", "GUARDIANO", "RITORNO"}, "getAttesa");
    }

    @Test
    void ITestiDelTorneoParlanoDelloStessoIncarico() {
        controllaUnIncarico(IlTorneo.class, 71, new String[] {"LIZZA", "PRIMO_TURNO", "SECONDO_TURNO", "FINALE", "RITORNO"});
    }

    @Test
    void ITestiDelPellegrinoParlanoDelloStessoIncarico() {
        controllaUnIncarico(IlPellegrino.class, 72, new String[] {"META", "PARTENZA", "VIAGGIO", "RITORNO"});
    }

    @Test
    void ITestiDellOggettoSmarritoParlanoDelloStessoIncarico() {
        controllaUnIncarico(LOggettoSmarrito.class, 73, new String[] {"PARTENZA", "RICERCA", "RITORNO"}, "testoConsegna");
    }

    @Test
    void ITestiDellaDocumentazioneParlanoDelloStessoIncarico() {
        controllaUnIncarico(LaDocumentazione.class, 74, new String[] {"TRACCIA_1", "REPERTO_1", "RITORNO"});
    }

    @Test
    void ITestiDellIndagineParlanoDelloStessoIncarico() {
        controllaUnIncarico(LIndagine.class, 75, new String[] {"TRACCIA_1", "INDIZIO_1", "TRACCIA_2", "ACCUSA", "NASCONDIGLIO", "CATTURA", "RITORNO"}, "getAttesa");
    }

    @Test
    void ITestiDellaTagliaSullaBandaParlanoDelloStessoIncarico() {
        controllaUnIncarico(LaTagliaSullaBanda.class, 76, new String[] {"COVO", "CACCIA", "RITORNO"});
    }

    @Test
    void ITestiDelCacciatoreDiTaglieParlanoDelloStessoIncarico() {
        controllaUnIncarico(CacciatoreDiTaglie.class, 77, new String[] {"COVO", "CACCIA", "RITORNO"});
    }

    @Test
    void ITestiDellaForgiaParlanoDelloStessoIncarico() {
        controllaUnIncarico(LaForgia.class, 78, new String[] {"RACCOLTA_METALLO", "RACCOLTA_TROFEO", "FORGIATURA", "SALUTO", "RAFFREDDAMENTO", "RITORNO"}, "testoForgiatura", "testoSaluto");
    }

    @Test
    void ITestiDellIncaricoDiCombattimentoParlanoDelloStessoIncarico() {
        controllaUnIncarico(IncaricoDiCombattimento.class, 79, new String[] {"COVO", "CACCIA", "RITORNO"});
    }

    @Test
    void ITestiDeiNegoziParlanoDelloStessoIncarico() {
        String[] passi = {"COVO", "DISINFESTAZIONE", "RITORNO"};
        controllaUnIncarico(IlNegozioDellArmaiolo.class, 80, passi);
        controllaUnIncarico(IlNegozioDellAlchimista.class, 81, passi);
        controllaUnIncarico(IlNegozioDellIncantatore.class, 82, passi);
        controllaUnIncarico(IlNegozioDelVenditoreDiPergamene.class, 83, passi);
    }

    @Test
    void ITestiDellaChiaveDelCustodeSonoCompleti() {
        for (String lingua : LINGUE) {
            Testi.impostaLingua(lingua);
            try (PartitaDiTest partita = PartitaDiTest.nuova(69)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
                TestiDiUnIncarico.testiDellaChiaveDelCustode().forEach(f -> controllaFrase(lingua + ": ", f));
            } finally {
                Testi.impostaLingua("it");
            }
        }
    }

    private static String contrabbandoNelleTreLingue(String lingua) {
        return lingua.startsWith("en") ? "let no one notice you" : "nessuno vi noti";
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
