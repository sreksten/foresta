package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.IlCartografo;
import com.threeamigos.foresta.missioni.IncaricoInCitta;
import com.threeamigos.foresta.missioni.Mandante;
import com.threeamigos.foresta.missioni.RichiestaDiMateriali;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.oggetti.Oggetto;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le richieste del capitano delle guardie (i trofei di una caccia) e l'incarico del cartografo.
 */
class ScenarioTrofeiECartografoTest {

    private static <T extends IncaricoInCitta> T prendiLIncarico(Class<T> tipo, String... parametri) {
        T incarico = RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast)
                .findFirst().orElseThrow(AssertionError::new);
        for (int i = 0; i < parametri.length; i += 2) {
            incarico.aggiungiProprieta("PARAMETRO_" + parametri[i], parametri[i + 1]);
        }
        return prendiLIncarico(incarico);
    }

    private static <T extends IncaricoInCitta> T prendiLIncarico(T incarico) {
        // Come a una visita tranquilla della città
        incarico.controllaPreLocazione();
        incarico.segnaIntermezzoPassoMostrato("INCARICO");
        incarico.controllaInLocazione();
        assertTrue(incarico.isAttiva());
        return incarico;
    }

    private static void tornaARiscuotere(PartitaDiTest partita, IncaricoInCitta incarico) {
        partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
        incarico.controllaPreLocazione();
        incarico.segnaIntermezzoPassoMostrato("RITORNO");
        incarico.controllaInLocazione();
    }

    @Test
    void ilCapitanoVuoleLeOrecchieDiGoblinEContaQuelleCheGliSiPortano() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(191)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            RichiestaDiMateriali capitano = Alchimie.fissa(Alchimie.richiestaDi(Mandante.CAPITANO),
                    "M/F;orecchio di goblin;orecchie di goblin;NEMICI GOBLIN;3-4;6;Prove? Non vi fidate di noi?;Mi fido delle prove.", 3);
            prendiLIncarico(capitano);
            assertEquals("Il capitano delle guardie e le orecchie di goblin", capitano.getNome());
            assertTrue(capitano.getDescrizione().contains("ti ha chiesto tre orecchie di goblin, che si prendono sconfiggendo i Goblin"),
                    capitano.getDescrizione());

            // Le orecchie le portano i goblin
            GruppoAvversario avversari = GruppoAvversario.getIstanza();
            avversari.rimuoviPersonaggi();
            for (int i = 0; i < 3; i++) {
                avversari.aggiungiPersonaggio(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 1));
            }
            Oggetto orecchie = capitano.getOggettoInLocazione(new CoordinateMD(0, 0), TipoLocazione.ROVINE, true)
                    .orElseThrow(AssertionError::new);
            assertEquals("orecchie di goblin", orecchie.getNomePlurale());
            new com.threeamigos.foresta.oggetti.OggettoMissione(capitano.getId(), RichiestaDiMateriali.MATERIALE,
                    capitano.getMateriali().getNome(), 3).prendi(partita.gruppo(), null);
            capitano.controllaPostLocazione();
            assertEquals("RITORNO", capitano.getPassoCorrente());

            int monete = partita.gruppo().getMonete();
            tornaARiscuotere(partita, capitano);
            assertEquals(monete + 6 * 3 + 5, partita.gruppo().getMonete());
            assertTrue(capitano.isCompleta());
            List<String> testi = partita.testi();
            assertTrue(testi.contains("Le tre orecchie di goblin passano al capitano delle guardie, che le conta per bene."), String.valueOf(testi));
        }
    }

    @Test
    void ilCartografoVuoleZoneMaiVisitate() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(192)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
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
