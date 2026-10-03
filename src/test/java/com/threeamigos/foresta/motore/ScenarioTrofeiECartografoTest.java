package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.CacciaAiTrofei;
import com.threeamigos.foresta.missioni.IlCartografo;
import com.threeamigos.foresta.missioni.IncaricoInCitta;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli incarichi della caccia ai trofei (il capitano delle guardie) e del cartografo.
 */
class ScenarioTrofeiECartografoTest {

    private static <T extends IncaricoInCitta> T prendiLIncarico(Class<T> tipo, String... parametri) {
        T incarico = RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast)
                .findFirst().orElseThrow(AssertionError::new);
        for (int i = 0; i < parametri.length; i += 2) {
            incarico.aggiungiProprieta("PARAMETRO_" + parametri[i], parametri[i + 1]);
        }
        // Come a una visita tranquilla della città
        incarico.controllaPreLocazione();
        incarico.segnaIntermezzoPassoMostrato("INCARICO");
        incarico.controllaInLocazione();
        assertTrue(incarico.isAttiva());
        return incarico;
    }

    private static void tornaARiscuotere(PartitaDiTest partita, IncaricoInCitta incarico) {
        partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA));
        incarico.controllaPreLocazione();
        incarico.segnaIntermezzoPassoMostrato("RITORNO");
        incarico.controllaInLocazione();
    }

    @Test
    void ilCapitanoVuoleLeOrecchieDiGoblinEContaQuelleCheGliSiPortano() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(191)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            CacciaAiTrofei caccia = prendiLIncarico(CacciaAiTrofei.class, "TROFEO", "ORECCHIE_DI_GOBLIN", "QUANTITA", "3");
            assertEquals("Prove di caccia: orecchie di goblin", caccia.getNome());
            assertTrue(caccia.getDescrizione().contains("vuole tre orecchie di goblin come prova che hai sfoltito i goblin"),
                    caccia.getDescrizione());

            // Le orecchie le portano i goblin
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.rimuoviPersonaggi();
            for (int i = 0; i < 3; i++) {
                avversari.aggiungiPersonaggio(ClassePersonaggio.GOBLIN.getIstanza(1));
            }
            Oggetto orecchie = caccia.getOggettoInLocazione(new CoordinateMD(0, 0), ClassiLocazione.ROVINE, true)
                    .orElseThrow(AssertionError::new);
            assertEquals("orecchie di goblin", orecchie.getNomePlurale());
            new com.threeamigos.foresta.oggetti.OggettoMissione(caccia.getId(), CacciaAiTrofei.TROFEI, caccia.getTrofeo().getNome(), 3)
                    .prendi(partita.gruppo(), null);
            caccia.controllaPostLocazione();
            assertEquals("RITORNO", caccia.getPassoCorrente());

            int monete = partita.gruppo().getMonete();
            tornaARiscuotere(partita, caccia);
            assertEquals(monete + 6 * 3 + 5, partita.gruppo().getMonete());
            assertTrue(caccia.isCompleta());
            List<String> testi = partita.testi();
            assertTrue(testi.contains("Il capitano delle guardie conta le tre orecchie di goblin, una per una."), String.valueOf(testi));
        }
    }

    @Test
    void ilCartografoVuoleZoneMaiVisitate() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(192)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            IlCartografo cartografo = prendiLIncarico(IlCartografo.class, "CASELLE", "6");
            assertTrue(cartografo.getDescrizione().contains("che cosa c'è in sei zone della foresta"), cartografo.getDescrizione());

            int esplorate = 0;
            for (int x = 0; x < Foresta.getDimensioneX() && esplorate < 6; x++) {
                for (int y = 0; y < Foresta.getDimensioneY() && esplorate < 6; y++) {
                    CoordinateMD casella = new CoordinateMD(x, y);
                    if (!Foresta.isLocazioneVisitata(casella)) {
                        partita.gruppo().setCoordinate(casella);
                        cartografo.controllaPreLocazione();
                        esplorate++;
                    }
                }
            }
            assertEquals("RITORNO", cartografo.getPassoCorrente());
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("Avete visto abbastanza foresta")), String.valueOf(partita.testi()));

            int monete = partita.gruppo().getMonete();
            tornaARiscuotere(partita, cartografo);
            assertEquals(monete + 3 * 6 + 5, partita.gruppo().getMonete());
            assertTrue(cartografo.isCompleta());
            assertEquals(2, RegistroMissioni.getTutteLeMissioni().stream().filter(IlCartografo.class::isInstance).count(),
                    "il cartografo avrà altre zone da disegnare");
        }
    }
}
