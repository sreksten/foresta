package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Stefano Reksten
 */
class ElementoIntermezzoTest {

    private static final double DELTA = 1e-9;

    private static ElementoIntermezzo elemento() {
        return ElementoIntermezzo.personaggio("prova", ClassePersonaggio.MAGO, 0, 0.5);
    }

    @Test
    void senzaTappeRestaFermo() {
        ElementoIntermezzo elemento = elemento().conScala(2).conOpacita(0.5);

        StatoElemento stato = elemento.getStatoAl(10);

        assertEquals(0, stato.getX(), DELTA);
        assertEquals(0.5, stato.getY(), DELTA);
        assertEquals(2, stato.getScala(), DELTA);
        assertEquals(0.5, stato.getOpacita(), DELTA);
        assertEquals(0, elemento.getDurata(), DELTA);
    }

    @Test
    void interpolaLinearmenteETieneIValoriNonIndicati() {
        ElementoIntermezzo elemento = elemento()
                .poi(Tappa.inSecondi(2).verso(1, 0.5))
                .poi(Tappa.inSecondi(2).conOpacita(0));

        assertEquals(0.5, elemento.getStatoAl(1).getX(), DELTA);
        assertEquals(1, elemento.getStatoAl(1).getOpacita(), DELTA);
        // Nella seconda tappa la posizione resta quella raggiunta, cambia solo l'opacità
        assertEquals(1, elemento.getStatoAl(3).getX(), DELTA);
        assertEquals(0.5, elemento.getStatoAl(3).getOpacita(), DELTA);
        // Una volta sola: dopo la fine resta sull'ultimo stato
        assertEquals(0, elemento.getStatoAl(100).getOpacita(), DELTA);
        assertEquals(4, elemento.getDurata(), DELTA);
    }

    @Test
    void attendiLasciaFermoLElemento() {
        ElementoIntermezzo elemento = elemento().attendi(2).poi(Tappa.inSecondi(2).verso(1, 0.5));

        assertEquals(0, elemento.getStatoAl(1.5).getX(), DELTA);
        assertEquals(0.5, elemento.getStatoAl(3).getX(), DELTA);
    }

    @Test
    void ciclicaRicominciaDallInizio() {
        ElementoIntermezzo elemento = elemento().poi(Tappa.inSecondi(4).verso(1, 0.5)).ripeti(Ripetizione.CICLICA);

        assertEquals(0.25, elemento.getStatoAl(5).getX(), DELTA);
        assertTrue(Double.isInfinite(elemento.getDurata()));
    }

    @Test
    void avantiEIndietroRipercorreLeTappe() {
        ElementoIntermezzo elemento = elemento().poi(Tappa.inSecondi(4).verso(1, 0.5)).ripeti(Ripetizione.AVANTI_E_INDIETRO);

        assertEquals(0.5, elemento.getStatoAl(2).getX(), DELTA);
        assertEquals(0.75, elemento.getStatoAl(5).getX(), DELTA);
        assertEquals(0, elemento.getStatoAl(8).getX(), DELTA);
        assertTrue(Double.isInfinite(elemento.getDurata()));
    }

    @Test
    void ilVersoDiUnaTappaValePerTuttoIlTratto() {
        ElementoIntermezzo elemento = elemento()
                .poi(Tappa.inSecondi(2).verso(1, 0.5))
                .poi(Tappa.inSecondi(2).verso(0, 0.5).specchiata(true));

        assertFalse(elemento.getStatoAl(1).isSpecchiato());
        assertTrue(elemento.getStatoAl(3).isSpecchiato());
        // Resta come nell'ultima tappa anche a animazione finita
        assertTrue(elemento.getStatoAl(10).isSpecchiato());
    }

    @Test
    void orientatoNelVersoDelMotoSiGiraAlRitorno() {
        // Immagine che guarda a sinistra, che va verso sinistra e poi torna indietro
        ElementoIntermezzo elemento = ElementoIntermezzo.personaggio("drago", ClassePersonaggio.DRAGO, 1, 0.2)
                .poi(Tappa.inSecondi(4).verso(0, 0.2))
                .ripeti(Ripetizione.AVANTI_E_INDIETRO)
                .orientaNelVersoDelMoto(Verso.SINISTRA);

        assertFalse(elemento.getStatoAl(0).isSpecchiato());
        assertFalse(elemento.getStatoAl(2).isSpecchiato());
        assertTrue(elemento.getStatoAl(6).isSpecchiato());
        assertFalse(elemento.getStatoAl(10).isSpecchiato());
    }

    @Test
    void orientatoNelVersoDelMotoTieneIlVersoDelleTappeQuandoEFermo() {
        ElementoIntermezzo elemento = elemento().specchiato()
                .attendi(2)
                .poi(Tappa.inSecondi(2).verso(1, 0.5))
                .orientaNelVersoDelMoto(Verso.DESTRA);

        assertTrue(elemento.getStatoAl(1).isSpecchiato());
        assertFalse(elemento.getStatoAl(3).isSpecchiato());
    }
}
