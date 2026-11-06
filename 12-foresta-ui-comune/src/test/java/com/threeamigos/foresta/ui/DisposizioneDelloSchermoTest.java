package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dove stanno le parti dello schermo di gioco, con le misure vere delle cornici.
 */
class DisposizioneDelloSchermoTest {

    private static final double ZOOM = 2.0;
    private final MisureDaiFile misure = new MisureDaiFile();

    private DisposizioneDelloSchermo disposizione(Orientamento orientamento) {
        Misura dimensioni = DisposizioneDelloSchermo.dimensioniDelloSchermo(orientamento, misure, ZOOM);
        return new DisposizioneDelloSchermo(orientamento, dimensioni.larghezza, dimensioni.altezza, misure, ZOOM);
    }

    @Test
    void inOrizzontaleLaBarraStaInFondoEInVerticaleADestra() {
        DisposizioneDelloSchermo orizzontale = disposizione(Orientamento.ORIZZONTALE);
        assertEquals(orizzontale.getAltezzaContenuto(), orizzontale.getBarra().getY());
        assertEquals(orizzontale.getLarghezzaTotale(), orizzontale.getBarra().getLarghezza());

        DisposizioneDelloSchermo verticale = disposizione(Orientamento.VERTICALE);
        assertEquals(verticale.getLarghezzaContenuto(), verticale.getBarra().getX());
        assertEquals(verticale.getAltezzaTotale(), verticale.getBarra().getAltezza());
        // L'area di contenuto è la stessa nelle due disposizioni
        assertEquals(orizzontale.getLarghezzaContenuto(), verticale.getLarghezzaContenuto());
        assertEquals(orizzontale.getAltezzaContenuto(), verticale.getAltezzaContenuto());
    }

    @Test
    void iRiquadriStannoNelContenutoESenzaSovrapporsiTraColonne() {
        DisposizioneDelloSchermo d = disposizione(Orientamento.ORIZZONTALE);
        for (Area area : new Area[]{d.getMappa(), d.getLocazione(), d.getStatistiche(), d.getTesto(), d.getGruppo()}) {
            assertTrue(area.getX() >= 0 && area.getY() >= 0, "dentro il contenuto");
            assertTrue(area.getX() + area.getLarghezza() <= d.getLarghezzaContenuto(), "dentro il contenuto");
            assertTrue(area.getY() + area.getAltezza() <= d.getAltezzaContenuto(), "dentro il contenuto");
        }
        // Mappa e statistiche sono allineate a destra, alla colonna dei riquadri impilati
        assertEquals(d.getGruppo().getX() + d.getGruppo().getLarghezza(), d.getMappa().getX() + d.getMappa().getLarghezza());
        assertEquals(d.getGruppo().getX() + d.getGruppo().getLarghezza(), d.getStatistiche().getX() + d.getStatistiche().getLarghezza());
        // Il testo sta a sinistra della colonna
        assertTrue(d.getTesto().getX() + d.getTesto().getLarghezza() < d.getColonnaDestraX());
    }

    @Test
    void iRiquadriImpilatiScendonoDiUnPassoEStannoNelloSchermo() {
        DisposizioneDelloSchermo d = disposizione(Orientamento.ORIZZONTALE);
        assertTrue(d.getPassoPila() > 0);
        assertEquals(d.getQuotaPila() + d.getPassoPila(), d.quotaNellaPila(1));
        int ultima = d.quotaNellaPila(DisposizioneDelloSchermo.RIQUADRI_IMPILATI - 1);
        assertTrue(ultima + d.getGruppo().getAltezza() <= d.getAltezzaContenuto());
    }
}
