package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.tipi.TipoMotivoNelGruppo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I dati di un personaggio: i suoi campi, gli attributi con i loro limiti, le collezioni (artefatti, modificatori, effetti di
 * stato), la tattica e come si salvano.
 */
class PersonaggioMDTest {

    private static String salva(PersonaggioMD personaggio) throws IOException {
        StringWriter scritto = new StringWriter();
        try (PrintWriter stream = new PrintWriter(scritto)) {
            personaggio.salva(stream);
        }
        return scritto.toString();
    }

    private static PersonaggioMD rileggi(PersonaggioMD destinazione, String salvato) throws IOException {
        destinazione.leggi(new BufferedReader(new StringReader(salvato)));
        return destinazione;
    }

    private static PersonaggioMD guerriero() {
        PersonaggioMD personaggio = new PersonaggioMD();
        personaggio.setClasse(TipoPersonaggio.GUERRIERO);
        personaggio.setVivo(true);
        personaggio.setLivello(3);
        return personaggio;
    }

    @Test
    void iCampiSiLeggonoESiCambiano() {
        PersonaggioMD personaggio = guerriero();
        assertNotNull(personaggio.getUuid());
        assertNotEquals(personaggio.getUuid(), new PersonaggioMD().getUuid());
        assertSame(TipoMotivoNelGruppo.PERMANENTE, personaggio.getMotivoNelGruppo(), "di solito si è nel gruppo per restarci");

        personaggio.setPng(true);
        personaggio.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_MERCENARIO);
        personaggio.setEsperienza(120);
        personaggio.setPuntiAbilitaDisponibili(2);
        personaggio.setCausaTrapasso("trafitto|da una lancia\nnel bosco");

        assertTrue(personaggio.isPng());
        assertSame(TipoMotivoNelGruppo.AIUTO_MERCENARIO, personaggio.getMotivoNelGruppo());
        assertEquals(120, personaggio.getEsperienza());
        assertEquals(2, personaggio.getPuntiAbilitaDisponibili());
        assertEquals("trafittoda una lancia nel bosco", personaggio.getCausaTrapasso());
    }

    @Test
    void unNomeVuotoValeComeNessunNome() {
        PersonaggioMD personaggio = guerriero();
        personaggio.setNome("Arsenio|il Grande");
        assertEquals("Arsenioil Grande", personaggio.getNome());
        personaggio.setNome("   ");
        assertNull(personaggio.getNome());
        personaggio.setNome(null);
        assertNull(personaggio.getNome());
    }

    @Test
    void unAttributoStaFraIlMinimoEIlMassimoSeCiSono() {
        PersonaggioMD personaggio = guerriero();
        assertFalse(personaggio.getMinimo(TipoAttributo.SALUTE).isPresent());
        assertFalse(personaggio.getMassimo(TipoAttributo.SALUTE).isPresent());
        personaggio.setSalute(-50);
        assertEquals(-50, personaggio.get(TipoAttributo.SALUTE), "senza limiti vale quel che si mette");

        personaggio.setMinimo(TipoAttributo.SALUTE, 0.0d);
        personaggio.setMassimo(TipoAttributo.SALUTE, 100.0d);
        assertEquals(Optional.of(0.0d), personaggio.getMinimo(TipoAttributo.SALUTE));
        assertEquals(Optional.of(100.0d), personaggio.getMassimo(TipoAttributo.SALUTE));
        personaggio.setSalute(-5);
        assertEquals(0, personaggio.get(TipoAttributo.SALUTE), "portato al minimo");
        personaggio.setSalute(150);
        assertEquals(100, personaggio.get(TipoAttributo.SALUTE), "portato al massimo");
        personaggio.setSalute(42);
        assertEquals(42, personaggio.get(TipoAttributo.SALUTE), "dentro i limiti resta com'è");

        personaggio.setMinimo(TipoAttributo.SALUTE, null);
        personaggio.setMassimo(TipoAttributo.SALUTE, null);
        assertFalse(personaggio.getMinimo(TipoAttributo.SALUTE).isPresent());
        assertFalse(personaggio.getMassimo(TipoAttributo.SALUTE).isPresent());
        personaggio.setSalute(500);
        assertEquals(500, personaggio.get(TipoAttributo.SALUTE));
    }

    @Test
    void unAttributoCheNonCeNonSiLegge() {
        PersonaggioMD personaggio = guerriero();
        IllegalStateException errore = assertThrows(IllegalStateException.class, () -> personaggio.get(TipoAttributo.TEMPO));
        assertTrue(errore.getMessage().contains("TEMPO"), errore.getMessage());
        assertFalse(personaggio.getOptional(TipoAttributo.TEMPO).isPresent());
        personaggio.set(TipoAttributo.TEMPO, 4);
        assertEquals(Optional.of(4.0d), personaggio.getOptional(TipoAttributo.TEMPO));
    }

    @Test
    void leScorciatoieScrivonoIlLoroAttributo() {
        PersonaggioMD personaggio = guerriero();
        personaggio.setSalute(1);
        personaggio.setForza(2);
        personaggio.setDestrezza(3);
        personaggio.setCostituzione(4);
        personaggio.setIntelligenza(5);
        personaggio.setSaggezza(6);
        personaggio.setCarisma(7);
        personaggio.setFortuna(8);
        personaggio.setMagia(9);
        personaggio.setStanchezza(10);
        TipoAttributo[] attributi = {TipoAttributo.SALUTE, TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE,
                TipoAttributo.INTELLIGENZA, TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA, TipoAttributo.MAGIA,
                TipoAttributo.STANCHEZZA};
        for (int i = 0; i < attributi.length; i++) {
            assertEquals(i + 1, personaggio.get(attributi[i]), attributi[i].name());
        }
    }

    @Test
    void ilCaricoMassimoEIlMassimoDelCarico() {
        PersonaggioMD personaggio = guerriero();
        assertEquals(0, personaggio.getCaricoMassimo(), "senza un massimo non c'è limite");
        personaggio.setMassimo(TipoAttributo.CARICO_MASSIMO, 142.8d);
        assertEquals(142, personaggio.getCaricoMassimo());
    }

    @Test
    void artefattiModificatoriEdEffettiSiAggiungonoESiTolgono() {
        PersonaggioMD personaggio = guerriero();
        ArtefattoMD spada = ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia");
        ArtefattoMD elmo = ArtefattiDiTest.creaArtefatto(TipoArtefatto.ELMO, "l'elmo", "che protegge");
        personaggio.aggiungiArtefatto(spada);
        personaggio.aggiungiArtefatto(elmo);
        List<ArtefattoMD> prima = new ArrayList<>(personaggio.getArtefatti());
        assertTrue(personaggio.togliArtefatto(spada));
        assertFalse(personaggio.togliArtefatto(spada));
        personaggio.ripristinaArtefatti(prima);
        assertEquals(prima, new ArrayList<>(personaggio.getArtefatti()), "stesso contenuto, stesso ordine");
        assertThrows(UnsupportedOperationException.class, () -> personaggio.getArtefatti().clear());

        ModificatoreAttributoMD modificatore = new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2.0d);
        personaggio.aggiungiModificatore(modificatore);
        assertTrue(personaggio.getModificatori().contains(modificatore));
        assertTrue(personaggio.togliModificatore(modificatore));
        assertFalse(personaggio.togliModificatore(modificatore));

        EffettoDiStatoMD bruciato = new EffettoDiStatoMD(TipoEffettoDiStato.BRUCIATO, 3, 4);
        personaggio.aggiungiEffettoDiStato(bruciato);
        assertTrue(personaggio.getEffettiDiStato().contains(bruciato));
        assertTrue(personaggio.togliEffettoDiStato(bruciato));
        assertFalse(personaggio.togliEffettoDiStato(bruciato));
    }

    @Test
    void unPersonaggioCompletoSiSalvaESiRilegge() throws IOException {
        PersonaggioMD personaggio = guerriero();
        personaggio.setNome("Arsenio");
        personaggio.setEsperienza(77);
        personaggio.setPuntiAbilitaDisponibili(1);
        personaggio.setPng(true);
        personaggio.setMotivoNelGruppo(TipoMotivoNelGruppo.AIUTO_GRATUITO);
        personaggio.setMinimo(TipoAttributo.SALUTE, 0.0d);
        personaggio.setMassimo(TipoAttributo.SALUTE, 120.0d);
        personaggio.setSalute(80);
        personaggio.setForza(15);
        personaggio.aggiungiModificatore(new ModificatoreAttributoMD(TipoAttributo.FORZA, TipoModificatore.VARIAZIONE_FISSA, 2.0d,
                "nota con § dentro", UnitaDurataModificatore.TURNI, 3, BersaglioModificatore.PORTATORE));
        personaggio.aggiungiEffettoDiStato(new EffettoDiStatoMD(TipoEffettoDiStato.ISPIRATO, 4, 0, 0.25d));
        personaggio.aggiungiArtefatto(ArtefattiDiTest.creaArtefatto(TipoArtefatto.SPADA, "la spada", "che taglia"));
        String salvato = salva(personaggio);

        PersonaggioMD destinazione = guerriero();
        destinazione.aggiungiModificatore(new ModificatoreAttributoMD(TipoAttributo.CARISMA, TipoModificatore.VARIAZIONE_FISSA, 1.0d));
        destinazione.aggiungiEffettoDiStato(new EffettoDiStatoMD(TipoEffettoDiStato.BAGNATO, 1, 0));
        PersonaggioMD riletto = rileggi(destinazione, salvato);

        assertEquals(personaggio.getUuid(), riletto.getUuid());
        assertSame(TipoPersonaggio.GUERRIERO, riletto.getClasse());
        assertEquals("Arsenio", riletto.getNome());
        assertTrue(riletto.isVivo());
        assertNull(riletto.getCausaTrapasso());
        assertEquals(3, riletto.getLivello());
        assertEquals(77, riletto.getEsperienza());
        assertEquals(1, riletto.getPuntiAbilitaDisponibili());
        assertTrue(riletto.isPng());
        assertSame(TipoMotivoNelGruppo.AIUTO_GRATUITO, riletto.getMotivoNelGruppo());
        assertEquals(Optional.of(0.0d), riletto.getMinimo(TipoAttributo.SALUTE));
        assertEquals(Optional.of(120.0d), riletto.getMassimo(TipoAttributo.SALUTE));
        assertEquals(80, riletto.get(TipoAttributo.SALUTE));
        assertEquals(new ArrayList<>(personaggio.getModificatori()), new ArrayList<>(riletto.getModificatori()), "quelli di prima si sostituiscono");
        assertEquals(1, riletto.getEffettiDiStato().size(), "quelli di prima si sostituiscono");
        assertSame(TipoEffettoDiStato.ISPIRATO, riletto.getEffettiDiStato().iterator().next().getTipoEffettoDiStato());
        assertEquals("la spada", riletto.getArtefatti().iterator().next().getNome());
        assertEquals(salvato, salva(riletto));
    }

    @Test
    void laCausaDellaMorteSiSalvaSoloPerChiEMorto() throws IOException {
        PersonaggioMD morto = guerriero();
        morto.setVivo(false);
        morto.setCausaTrapasso("divorato da un troll");
        assertEquals("divorato da un troll", rileggi(new PersonaggioMD(), salva(morto)).getCausaTrapasso());

        PersonaggioMD vivo = guerriero();
        vivo.setCausaTrapasso("una causa rimasta da prima");
        String salvato = salva(vivo);
        assertFalse(salvato.contains("una causa rimasta da prima"), salvato);
        PersonaggioMD riletto = rileggi(new PersonaggioMD(), salvato);
        assertNull(riletto.getCausaTrapasso());
    }

    @Test
    void leRicaricheSiSalvanoESiRileggono() throws IOException {
        PersonaggioMD personaggio = guerriero();
        personaggio.setRicarica("RADICI", 3);
        personaggio.setRicarica("CURATIVO", 2);
        assertEquals(3, personaggio.getRicarica("RADICI"));
        assertEquals(0, personaggio.getRicarica("SCIAME"), "chi non è in ricarica è pronto");

        PersonaggioMD riletto = rileggi(new PersonaggioMD(), salva(personaggio));

        assertEquals(personaggio.getRicariche(), riletto.getRicariche());
        assertEquals(2, riletto.getRicarica("CURATIVO"));
    }

    @Test
    void unaRicaricaAZeroNonSiConservaENonSiSalva() throws IOException {
        PersonaggioMD personaggio = guerriero();
        personaggio.setRicarica("RADICI", 1);
        personaggio.setRicarica("RADICI", 0);
        assertTrue(personaggio.getRicariche().isEmpty());
        assertTrue(rileggi(new PersonaggioMD(), salva(personaggio)).getRicariche().isEmpty());
    }

    @Test
    void rileggereSostituisceLeRicariche() throws IOException {
        PersonaggioMD pronto = guerriero();
        PersonaggioMD inRicarica = guerriero();
        inRicarica.setRicarica("RADICI", 4);

        rileggi(inRicarica, salva(pronto));

        assertTrue(inRicarica.getRicariche().isEmpty());
    }
}
