package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Mandante;
import com.threeamigos.foresta.missioni.MaterialeRichiesto;
import com.threeamigos.foresta.missioni.RichiestaDiMateriali;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le richieste di materiali dell'alchimista, dell'armaiolo e del capitano delle guardie: ogni riga di missioni.txt si
 * legge, e i testi si accordano con il materiale e con il mandante.
 */
class ScenarioRichiesteDiMaterialiTest {

    private static final String ACONITO = "M;fiore di aconito;fiori di aconito;LUOGHI RADURA;3-4;6;Li raccogliamo con i guanti?;Se ci tenete alle dita, sì.";
    private static final String VIVERNA = "F;scaglia di viverna;scaglie di viverna;NEMICI VIVERNA;2-3;12;Per farci cosa?;Uno scudo che non brucia.";

    @Test
    void ogniRigaDiOgniMandanteSiLeggeEIMaterialiSonoVari() {
        for (Mandante mandante : Mandante.values()) {
            Set<String> plurali = new HashSet<>();
            for (int i = 0; i < 300; i++) {
                MaterialeRichiesto materiale = MaterialeRichiesto.da(ProduttoreDiTestiCasuale.materialeRichiesto(mandante.getProduzione()));
                assertTrue(materiale.getPrezzo() > 0);
                assertFalse(materiale.getBattutaDelCapo().isEmpty());
                assertFalse(materiale.getRispostaDelMandante().isEmpty());
                materiale.getNemici().forEach(nemico -> nemico.getMoltiplicatoriDiClasse());
                plurali.add(materiale.getPlurale());
            }
            assertTrue(plurali.size() >= 5, mandante + ": " + plurali);
        }
    }

    @Test
    void daDoveVieneUnMaterialeDipendeDaLuoghiONemici() {
        MaterialeRichiesto aconito = MaterialeRichiesto.da(ACONITO);
        assertFalse(aconito.isTrofeo());
        assertEquals("i fiori di aconito", aconito.getPluraleConArticolo());
        assertEquals("tre fiori di aconito", aconito.quanti(3));
        assertEquals("si trovano nelle radure", aconito.getDaDoveViene());
        assertEquals("si trovano nelle radure e nei boschi", MaterialeRichiesto.da(Alchimie.MANDRAGOLA).getDaDoveViene());

        MaterialeRichiesto viverna = MaterialeRichiesto.da(VIVERNA);
        assertTrue(viverna.isTrofeo());
        assertEquals("si prendono sconfiggendo le Viverne", viverna.getDaDoveViene());
        assertTrue(viverna.daRaccogliere("X", 2).getNemici().contains(ClassePersonaggio.VIVERNA));

        assertThrows(IllegalArgumentException.class, () -> MaterialeRichiesto.da("F;uno;due;ALTROVE BOSCO;1-2;3;a;b"));
        assertThrows(IllegalArgumentException.class, () -> MaterialeRichiesto.da("F;uno;due;LUOGHI BOSCO;3-1;3;a;b"));
    }

    @Test
    void lArmaioloVuoleLeScaglieDiVivernaEPagaPerPezzo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(171)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            RichiestaDiMateriali armaiolo = Alchimie.fissa(Alchimie.richiestaDi(Mandante.ARMAIOLO), VIVERNA, 2);
            armaiolo.controllaPreLocazione();
            armaiolo.segnaIntermezzoPassoMostrato("INCARICO");
            armaiolo.controllaInLocazione();
            assertTrue(armaiolo.isAttiva());
            assertEquals("L'armaiolo e le scaglie di viverna", armaiolo.getNome());
            assertTrue(armaiolo.getDescrizione().contains("L'armaiolo di Nyena ti ha chiesto due scaglie di viverna, che si prendono sconfiggendo le Viverne"),
                    armaiolo.getDescrizione());

            // Le scaglie le portano le viverne, dovunque siano
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.rimuoviPersonaggi();
            avversari.aggiungiPersonaggio(ClassePersonaggio.VIVERNA.getIstanza(1));
            assertTrue(armaiolo.getOggettoInLocazione(new CoordinateMD(0, 0), ClassiLocazione.TEMPIO, true).isPresent());

            new OggettoMissione(armaiolo.getId(), RichiestaDiMateriali.MATERIALE, armaiolo.getMateriali().getNome(), 2)
                    .prendi(partita.gruppo(), null);
            armaiolo.controllaPostLocazione();
            int monete = partita.gruppo().getMonete();
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
            armaiolo.controllaPreLocazione();
            armaiolo.segnaIntermezzoPassoMostrato("RITORNO");
            armaiolo.controllaInLocazione();
            assertEquals(monete + 12 * 2 + 5, partita.gruppo().getMonete());
            List<String> testi = partita.testi();
            assertTrue(testi.contains("Le due scaglie di viverna passano all'armaiolo, che le soppesa con l'occhio del mestiere."),
                    String.valueOf(testi));
            assertTrue(armaiolo.isCompleta());

            // Il nuovo armaiolo chiederà un materiale suo, pescato quando si offrirà
            RichiestaDiMateriali nuovo = RegistroMissioni.getTutteLeMissioni().stream().filter(RichiestaDiMateriali.class::isInstance)
                    .map(RichiestaDiMateriali.class::cast).filter(r -> r.getMandante() == Mandante.ARMAIOLO && r != armaiolo)
                    .findFirst().orElseThrow(AssertionError::new);
            assertNull(nuovo.getParametro(RichiestaDiMateriali.MATERIALE));
        }
    }

    @Test
    void lAlchimistaChiedeIFioriDiAconitoSoloNelleRadure() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(172)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            RichiestaDiMateriali alchimista = Alchimie.fissa(Alchimie.alchimista(), ACONITO, 3);
            alchimista.controllaPreLocazione();
            alchimista.segnaIntermezzoPassoMostrato("INCARICO");
            alchimista.controllaInLocazione();
            assertEquals("L'alchimista e i fiori di aconito", alchimista.getNome());

            boolean nelBosco = false;
            for (int i = 0; i < 100; i++) {
                nelBosco |= alchimista.getOggettoInLocazione(new CoordinateMD(0, 0), ClassiLocazione.BOSCO, false).isPresent();
            }
            assertFalse(nelBosco);

            new OggettoMissione(alchimista.getId(), RichiestaDiMateriali.MATERIALE, alchimista.getMateriali().getNome(), 3)
                    .prendi(partita.gruppo(), null);
            alchimista.controllaPostLocazione();
            int monete = partita.gruppo().getMonete();
            alchimista.controllaPreLocazione();
            alchimista.controllaInLocazione();
            assertEquals(monete + 6 * 3 + 5, partita.gruppo().getMonete());
            List<String> testi = partita.testi();
            assertTrue(testi.contains("I tre fiori di aconito passano all'alchimista, che li annusa soddisfatto."), String.valueOf(testi));
            assertTrue(testi.stream().anyMatch(t -> t.startsWith("I fiori di aconito ci sono tutti")), String.valueOf(testi));
        }
    }
}
