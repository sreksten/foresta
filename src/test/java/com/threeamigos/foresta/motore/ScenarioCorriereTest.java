package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.IlCorriere;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Spedizione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il corriere: una cosa da portare da una città all'altra, che il destinatario paga alla consegna; le spedizioni
 * urgenti hanno una scadenza.
 */
class ScenarioCorriereTest {

    private static final String LETTERA = "F;lettera sigillata;il notaio;il borgomastro;CON_CALMA;14;Ho una lettera.;E cosa c'è scritto?;Non si dice.;Il sigillo è intatto!";
    private static final String ANTIDOTO = "F;fiala di antidoto;l'erborista;il fabbro;URGENTE;20;Il fabbro è stato morso.;E se arriviamo tardi?;Non arrivate tardi.;Mi sento le dita dei piedi!";

    @Test
    void ogniSpedizioneSiLeggeEOgniTantoEUrgente() {
        Set<String> oggetti = new HashSet<>();
        boolean urgenti = false;
        boolean conCalma = false;
        for (int i = 0; i < 300; i++) {
            Spedizione spedizione = Spedizione.da(ProduttoreDiTestiCasuale.spedizione());
            assertFalse("aeiou".contains(spedizione.getOggetto().substring(0, 1)), spedizione.getOggetto());
            assertFalse(spedizione.getRingraziamento().isEmpty());
            urgenti |= spedizione.isUrgente();
            conCalma |= !spedizione.isUrgente();
            oggetti.add(spedizione.getOggetto());
        }
        assertTrue(urgenti && conCalma);
        assertTrue(oggetti.size() >= 8, String.valueOf(oggetti));

        assertEquals("la fiala di antidoto", Spedizione.da(ANTIDOTO).getOggettoConArticolo());
        assertEquals("questa fiala di antidoto", Spedizione.da(ANTIDOTO).getQuestoOggetto());
        assertThrows(IllegalArgumentException.class, () -> Spedizione.da("F;a;b;c;DOMANI;3;d;e;f;g"));
        assertThrows(IllegalArgumentException.class, () -> Spedizione.da("F;a;b;c;URGENTE;3;d;e;f"));
    }

    @Test
    void laLetteraArrivaAlBorgomastroDiRuunaCheLaPaga() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(181)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlCorriere corriere = prendiLIncarico(LETTERA, ClassiLocazione.CITTA_RUUNA);
            CoordinateMD ruuna = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_RUUNA);
            CoordinateMD nyena = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA);
            int distanza = Math.abs(ruuna.getX() - nyena.getX()) + Math.abs(ruuna.getY() - nyena.getY());
            assertEquals(distanza, corriere.getDistanza());
            assertTrue(Foresta.isLocazioneConosciuta(ruuna));
            assertEquals("La lettera sigillata per il borgomastro di Ruuna", corriere.getNome());
            assertEquals("Porta la lettera sigillata al borgomastro di Ruuna, che ti pagherà " + (14 + distanza / 2) + " monete.",
                    corriere.getDescrizione());

            // Tornare a Nyena non serve: si riscuote a Ruuna
            int monete = partita.gruppo().getMonete();
            corriere.controllaPreLocazione();
            corriere.controllaInLocazione();
            assertEquals(monete, partita.gruppo().getMonete());

            consegnaA(partita, corriere, ruuna);
            assertEquals(monete + 14 + distanza / 2, partita.gruppo().getMonete());
            List<String> testi = partita.testi();
            assertTrue(testi.contains("La lettera sigillata passa al borgomastro."), String.valueOf(testi));
            assertTrue(corriere.isCompleta());

            // Il prossimo corriere porterà altro, altrove
            assertTrue(RegistroMissioni.getTutteLeMissioni().stream().anyMatch(m -> m instanceof IlCorriere && m != corriere));
        }
    }

    @Test
    void lAntidotoArrivaInTempoSoloSeIlGruppoSiSbriga() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(182)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlCorriere corriere = prendiLIncarico(ANTIDOTO, ClassiLocazione.CITTA_RUUNA);
            assertEquals(corriere.getDistanza() * 2 + 12, corriere.getOreConcesse());
            assertTrue(corriere.getDescrizione().endsWith("Ore rimaste: " + corriere.getOreConcesse() + "."), corriere.getDescrizione());

            LineaTemporale.aggiungiOre(corriere.getOreConcesse());
            assertEquals(0, corriere.getOreRimaste());
            int monete = partita.gruppo().getMonete();
            consegnaA(partita, corriere, Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_RUUNA));
            assertTrue(corriere.isCompleta());
            assertTrue(partita.gruppo().getMonete() > monete);
        }
    }

    @Test
    void lAntidotoArrivatoTardiFaFallireLaMissione() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(183)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlCorriere corriere = prendiLIncarico(ANTIDOTO, ClassiLocazione.CITTA_RUUNA);
            LineaTemporale.aggiungiOre(corriere.getOreConcesse() + 1);
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_RUUNA));
            corriere.controllaPreLocazione();
            assertTrue(corriere.isFallita());
            assertTrue(partita.testi().contains("Troppo tardi: la fiala di antidoto non arriverà più in tempo al fabbro di Ruuna."),
                    String.valueOf(partita.testi()));
        }
    }

    @Test
    void seLaCittaDiDestinazioneVieneDistruttaLaMissioneFallisce() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(184)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlCorriere corriere = prendiLIncarico(LETTERA, ClassiLocazione.CITTA_RUUNA);
            LineaTemporale.setCittaDistrutta(ClassiLocazione.CITTA_RUUNA);
            corriere.controllaPreLocazione();
            assertTrue(corriere.isFallita());
            assertTrue(partita.testi().contains("Ruuna è stata distrutta: la lettera sigillata non arriverà più al borgomastro."),
                    String.valueOf(partita.testi()));
        }
    }

    @Test
    void laDestinazioneEUnAltraCittaESenzaAltreCittaLIncaricoNonSiOffre() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(185)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlCorriere corriere = corriere();
            // Una destinazione già fissata ma uguale alla città dell'incarico non vale
            corriere.aggiungiProprieta(IlCorriere.DESTINAZIONE, ClassiLocazione.CITTA_NYENA.name());
            corriere.controllaPreLocazione();
            assertNotEquals(ClassiLocazione.CITTA_NYENA, corriere.getDestinazione());
            assertTrue(corriere.getDistanza() > 0);
        }
        try (PartitaDiTest partita = PartitaDiTest.nuova(186)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            LineaTemporale.setCittaDistrutta(ClassiLocazione.CITTA_RUUNA);
            LineaTemporale.setCittaDistrutta(ClassiLocazione.CITTA_FLEENA);
            LineaTemporale.setCittaDistrutta(ClassiLocazione.CITTA_MALGAARD);
            IlCorriere corriere = corriere();
            corriere.controllaPreLocazione();
            assertEquals("INCARICO", corriere.getPassoCorrente());
            assertNull(corriere.getDestinazione());
        }
    }

    private static IlCorriere corriere() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(IlCorriere.class::isInstance)
                .map(IlCorriere.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    /**
     * Come a una visita tranquilla della città, con la spedizione e la destinazione fissate.
     */
    private static IlCorriere prendiLIncarico(String spedizione, ClassiLocazione destinazione) {
        IlCorriere corriere = corriere();
        corriere.aggiungiProprieta("PARAMETRO_" + IlCorriere.SPEDIZIONE, spedizione);
        corriere.aggiungiProprieta(IlCorriere.DESTINAZIONE, destinazione.name());
        corriere.controllaPreLocazione();
        corriere.segnaIntermezzoPassoMostrato("INCARICO");
        corriere.controllaInLocazione();
        assertTrue(corriere.isAttiva());
        assertEquals(destinazione, corriere.getDestinazione());
        return corriere;
    }

    private static void consegnaA(PartitaDiTest partita, IlCorriere corriere, CoordinateMD citta) {
        partita.gruppo().setCoordinate(citta);
        corriere.controllaPreLocazione();
        corriere.segnaIntermezzoPassoMostrato("RITORNO");
        corriere.controllaInLocazione();
        assertEquals(Passo.FINE, corriere.getPassoCorrente());
    }
}
