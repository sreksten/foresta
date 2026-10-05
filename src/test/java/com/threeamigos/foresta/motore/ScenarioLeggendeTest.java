package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.LaLeggenda;
import com.threeamigos.foresta.missioni.LaLeggendaDelLocandiere;
import com.threeamigos.foresta.missioni.LaLeggendaDellArmaiolo;
import com.threeamigos.foresta.missioni.OggettoLeggendario;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le leggende: l'armaiolo in città o il locandiere in una locanda raccontano la leggenda di un oggetto leggendario di
 * leggendari.txt, che si pesca una volta sola per partita; sorge un tempio che lo custodisce, con i suoi guardiani, e
 * raccoglierlo completa la missione.
 */
class ScenarioLeggendeTest {


    @Test
    void ogniLeggendarioSiLeggeESiCostruisce() {
        List<OggettoLeggendario> leggendari = ProduttoreDiTestiCasuale.tuttiGliOggettiLeggendari().stream()
                .map(OggettoLeggendario::da).collect(Collectors.toList());
        Set<String> chiavi = leggendari.stream().map(OggettoLeggendario::getChiave).collect(Collectors.toSet());
        assertEquals(leggendari.size(), chiavi.size(), "la chiave identifica il leggendario");
        assertEquals(chiavi, CatalogoLeggendari.getTuttiILeggendari().keySet());
        // La Spada della Morte e lo Scudo Fiscale ci devono essere sempre
        assertTrue(chiavi.contains(Leggendari.SPADA_DI_ROMY_JONA) && chiavi.contains(Leggendari.SCUDO_DELL_ESATTORE), String.valueOf(chiavi));
        Set<TipoArtefatto> tipi = leggendari.stream().map(OggettoLeggendario::getTipo).collect(Collectors.toSet());
        assertTrue(tipi.containsAll(EnumSet.of(TipoArtefatto.SPADA, TipoArtefatto.SCUDO, TipoArtefatto.ELMO,
                TipoArtefatto.NINNOLO, TipoArtefatto.PERGAMENA)), String.valueOf(tipi));
        for (OggettoLeggendario leggendario : leggendari) {
            Artefatto artefatto = leggendario.costruisci();
            assertEquals(TipoRaritaArtefatto.LEGGENDARIO, artefatto.getRarita());
            assertEquals(leggendario.getTipo(), artefatto.getTipo());
            assertTrue(leggendario.getLeggenda().size() >= 2);
        }

        // Una riga giusta si legge; ognuna di quelle sotto ha un solo difetto
        String elmo = "CHIAVE=X;TIPO=ELMO;NOME=x;BREVE=x;DESCRIZIONE=x;LIVELLO=5;COSTO=6;PESO=1;LEGGENDA=a;LEGGENDA=b";
        assertEquals("X", OggettoLeggendario.da(elmo).getChiave());
        assertThrows(IllegalArgumentException.class, () -> OggettoLeggendario.da(elmo.replace("CHIAVE=X;", "")), "senza chiave");
        assertThrows(IllegalArgumentException.class, () -> OggettoLeggendario.da(elmo.replace("TIPO=ELMO", "TIPO=SPADA")),
                "una spada senza danni");
        assertThrows(IllegalArgumentException.class, () -> OggettoLeggendario.da(elmo + ";MOD=ALTEZZA +3"), "un attributo che non c'è");
        assertThrows(IllegalArgumentException.class, () -> OggettoLeggendario.da(elmo.replace(";LEGGENDA=b", "")),
                "una leggenda di una battuta");
    }

    @Test
    void unLeggendarioSiPescaUnaVoltaSolaFinchéCeNeSono() {
        List<String> tutti = ProduttoreDiTestiCasuale.tuttiGliOggettiLeggendari();
        String ultimo = tutti.get(0);
        assertEquals(Optional.of(ultimo), ProduttoreDiTestiCasuale.oggettoLeggendario(riga -> !riga.equals(ultimo)));
        assertEquals(Optional.empty(), ProduttoreDiTestiCasuale.oggettoLeggendario(riga -> true));
    }

    @Test
    void entrandoInCittaLArmaioloRaccontaLaLeggendaESorgeIlTempioConIGuardiani() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(41)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
                partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA);
                partita.eventi().ascolta(NotificaPaginaIntermezzo.class, NotificaAggiornamentoStatoMissione.class);
            });
            LaLeggenda leggenda = trova(LaLeggendaDellArmaiolo.class);
            OggettoLeggendario leggendario = leggenda.getLeggendario();
            assertNotNull(leggendario);
            assertTrue(leggenda.isAttiva());
            assertEquals("Recupera " + leggendario.getNomeBreve(), leggenda.getNome());

            // Prima la pagina con la leggenda, poi l'avviso della nuova missione
            List<Object> eventi = partita.eventi().inOrdine(NotificaPaginaIntermezzo.class, NotificaAggiornamentoStatoMissione.class);
            int pagina = -1;
            int avviso = -1;
            for (int i = 0; i < eventi.size(); i++) {
                Object evento = eventi.get(i);
                if (pagina < 0 && evento instanceof NotificaPaginaIntermezzo
                        && battute((NotificaPaginaIntermezzo) evento).contains(leggendario.getLeggenda().get(0))) {
                    pagina = i;
                }
                if (evento instanceof NotificaAggiornamentoStatoMissione
                        && leggenda.getNome().equals(((NotificaAggiornamentoStatoMissione) evento).getDescrizione())) {
                    avviso = i;
                }
            }
            assertTrue(pagina >= 0, "l'armaiolo racconta la leggenda");
            assertTrue(avviso > pagina, "l'avviso di nuova missione arriva dopo l'intermezzo");

            // Il tempio nuovo custodisce il leggendario, è della missione ed è segnato sulla mappa
            CoordinateMD tempio = RegistroMissioni.getLocazioneOccupata(leggenda);
            assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(tempio));
            Artefatto custodito = RegistroArtefatti.getArtefattoInLocazione(tempio);
            assertEquals(leggendario.getNome(), custodito.getNome());
            assertEquals(TipoRaritaArtefatto.LEGGENDARIO, custodito.getRarita());
            assertTrue(Foresta.isLocazioneConosciuta(tempio));
            assertTrue(leggenda.getDescrizione().startsWith("Secondo la leggenda raccontata dall'armaiolo, "), leggenda.getDescrizione());

            // Nel tempio ci sono i guardiani della leggenda: quelli della riga, o quelli del livello del gruppo
            TipoPersonaggio guardiano = leggendario.getGuardiani().map(IncontroDiMissione::getClasse).orElse(TipoPersonaggio.HOBGOBLIN);
            List<Personaggio> guardiani = leggenda.getIncontroInLocazione(tempio).orElseThrow(AssertionError::new);
            assertEquals(leggenda.getGuardiani().getNumero(), guardiani.size());
            assertTrue(guardiani.stream().allMatch(p -> p.getClasse() == guardiano));

            // Raccolto il leggendario, a fine locazione la missione è completa, e ne resta una nuova
            partita.gruppo().setCoordinate(tempio);
            leggenda.controllaPostLocazione();
            assertFalse(leggenda.isCompleta(), "il leggendario è ancora nel tempio");
            RegistroArtefatti.rimuoviArtefattoInLocazione(tempio);
            leggenda.controllaPostLocazione();
            assertTrue(leggenda.isCompleta());
            assertTrue(RegistroMissioni.getRicordo(tempio).orElse("").endsWith(" custodivano " + leggendario.getNomeBreve() + "."));
            assertEquals(2, RegistroMissioni.getTutteLeMissioni().stream().filter(LaLeggendaDellArmaiolo.class::isInstance).count());
        }
    }

    @Test
    void ilLocandiereRaccontaDallaTerzaVisitaEDopoTrentaseiOreUnAltroLeggendario() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(42)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaLeggenda armaiolo = trova(LaLeggendaDellArmaiolo.class);
            armaiolo.controllaPreLocazione();
            armaiolo.segnaIntermezzoPassoMostrato("INCARICO");
            armaiolo.controllaInLocazione();
            assertTrue(armaiolo.isAttiva());

            CoordinateMD locanda = unaLocanda();
            partita.gruppo().setCoordinate(locanda);
            LaLeggenda locandiere = trova(LaLeggendaDelLocandiere.class);
            // Alla prima e alla seconda visita no
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, "1");
            LineaTemporale.aggiungiOre(LaLeggenda.ORE_FRA_DUE_LEGGENDE);
            locandiere.controllaPreLocazione();
            assertNull(locandiere.getLeggendario());
            // Alla terza sì, ma non prima di trentasei ore dall'altra leggenda
            Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, "2");
            armaiolo.aggiungiProprieta("RACCONTATA_ALLE", String.valueOf(LineaTemporale.getGiorno() * 24L + LineaTemporale.getOra() - 1));
            locandiere.controllaPreLocazione();
            assertNull(locandiere.getLeggendario());
            LineaTemporale.aggiungiOre(LaLeggenda.ORE_FRA_DUE_LEGGENDE);
            locandiere.controllaPreLocazione();
            locandiere.segnaIntermezzoPassoMostrato("INCARICO");
            locandiere.controllaInLocazione();
            assertTrue(locandiere.isAttiva());
            assertNotEquals(armaiolo.getLeggendario().getChiave(), locandiere.getLeggendario().getChiave());
            assertTrue(locandiere.getDescrizione().contains("raccontata dal locandiere"), locandiere.getDescrizione());
        }
    }

    private static String battute(NotificaPaginaIntermezzo evento) {
        return evento.getPagina().getBattuteProgrammate().stream().map(BattutaProgrammata::getBattuta)
                .map(b -> b.getTesto()).collect(Collectors.joining(" "));
    }

    private static CoordinateMD unaLocanda() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("Nessuna locanda nella foresta");
    }

    private static LaLeggenda trova(Class<? extends LaLeggenda> tipo) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(LaLeggenda.class::cast).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
