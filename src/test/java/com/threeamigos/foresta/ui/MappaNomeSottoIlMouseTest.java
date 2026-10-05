package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Sulla mappa a tutto schermo, passando col mouse sopra una casella conosciuta con un nome se ne legge il nome.
 */
class MappaNomeSottoIlMouseTest {

    // Più grande della mappa: così la mappa è centrata e si sa dove sta ogni casella
    private static final int LATO_SCHERMO = 4000;

    private ModelloDati modelloDatiPrecedente;
    private DisplayableCanvasMappaATuttoSchermo mappa;
    private int scostamentoX;
    private int scostamentoY;

    @BeforeEach
    void preparaMappa() {
        modelloDatiPrecedente = ModelloDati.getIstanza();
        ModelloDati.setIstanza(new ModelloDati());
        ModelloDati.getIstanza().getForestaMD().reimposta(20, 20);
        // Come alla creazione del mondo, dove non c'è altro c'è bosco
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                ModelloDati.getIstanza().getForestaMD().impostaLocazione(new CoordinateMD(x, y), TipoLocazione.BOSCO);
            }
        }
        GruppoGiocatore.getIstanza().setModelloDati(ModelloDati.getIstanza().getGruppoGiocatoreMD());
        ModelloDati.getIstanza().getGruppoGiocatoreMD().setCoordinate(new CoordinateMD(0, 0));
        mappa = new DisplayableCanvasMappaATuttoSchermo(LATO_SCHERMO, LATO_SCHERMO);
        mappa.centraSuGiocatore();
        scostamentoX = (LATO_SCHERMO - Foresta.getDimensioneX() * DisegnatoreMappa.LARGHEZZA_ICONA) / 2;
        scostamentoY = (LATO_SCHERMO - Foresta.getDimensioneY() * DisegnatoreMappa.ALTEZZA_ICONA) / 2;
    }

    @AfterEach
    void ripristina() {
        ModelloDati.setIstanza(modelloDatiPrecedente);
        GruppoGiocatore.getIstanza().setModelloDati(modelloDatiPrecedente.getGruppoGiocatoreMD());
    }

    @Test
    void ilNomeCompareSoloSopraUnaCasellaConosciutaConUnNome() {
        Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_NYENA, new CoordinateMD(3, 4), true);
        Foresta.costruisciLocazioneUnica(TipoLocazione.CASTELLO_LICH, new CoordinateMD(10, 10), false);

        sopra(3, 4);
        assertEquals("la città di Nyena", mappa.getNomeSottoIlMouse());

        sopra(10, 10);
        assertNull(mappa.getNomeSottoIlMouse(), "il castello non è ancora conosciuto");
        Foresta.setLocazioneConosciuta(new CoordinateMD(10, 10));
        assertEquals("il Castello dell'Ombra", mappa.getNomeSottoIlMouse());

        sopra(5, 5);
        assertNull(mappa.getNomeSottoIlMouse(), "una casella senza nome non dice niente");

        sopra(3, 4);
        mappa.processaUscita(0, 0);
        assertNull(mappa.getNomeSottoIlMouse(), "il mouse è uscito dalla mappa");
    }

    @Test
    void mentreSiTrascinaNonCompareNiente() {
        Foresta.costruisciLocazioneUnica(TipoLocazione.CITTA_RUUNA, new CoordinateMD(2, 2), true);
        sopra(2, 2);
        mappa.processaPressione(centroX(2), centroY(2), Finestra.Tasto.SINISTRO);
        assertNull(mappa.getNomeSottoIlMouse());
        mappa.processaRilascio(centroX(2), centroY(2), Finestra.Tasto.SINISTRO);
        assertEquals("la città di Ruuna", mappa.getNomeSottoIlMouse());
    }

    private void sopra(int x, int y) {
        mappa.processaMovimento(centroX(x), centroY(y));
    }

    private int centroX(int x) {
        return scostamentoX + x * DisegnatoreMappa.LARGHEZZA_ICONA + DisegnatoreMappa.LARGHEZZA_ICONA / 2;
    }

    private int centroY(int y) {
        return scostamentoY + y * DisegnatoreMappa.ALTEZZA_ICONA + DisegnatoreMappa.ALTEZZA_ICONA / 2;
    }
}
