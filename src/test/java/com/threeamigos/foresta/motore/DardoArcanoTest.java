package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Elfo;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Il dardo arcano, l'incantesimo innato di Mago ed Elfo. Il critico è neutralizzato, così i danni sono deterministici.
 */
class DardoArcanoTest {

    private Guerriero difensore;

    @BeforeEach
    void preparaDifensore() {
        ModelloDati.setIstanza(new ModelloDati());
        difensore = new Guerriero("Bersaglio", 5);
        difensore.addModificatore(new ModificatoreAttributo(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
    }

    @Test
    void loConosconoSoloMagoEdElfo() {
        assertTrue(DardoArcano.conosciutoDa(ClassePersonaggio.MAGO));
        assertTrue(DardoArcano.conosciutoDa(ClassePersonaggio.MAGA));
        assertTrue(DardoArcano.conosciutoDa(ClassePersonaggio.ELFO));
        assertTrue(DardoArcano.conosciutoDa(ClassePersonaggio.ELFA));
        assertFalse(DardoArcano.conosciutoDa(ClassePersonaggio.GUERRIERO));
        assertFalse(DardoArcano.conosciutoDa(ClassePersonaggio.LADRO));
        assertFalse(DardoArcano.conosciutoDa(ClassePersonaggio.BARDO));
    }

    @Test
    void serveAbbastanzaMagia() {
        Mago mago = new Mago("Merlino", 5);
        assertTrue(DardoArcano.puoLanciarlo(mago));
        mago.subMagia(mago.getMagia());
        assertFalse(DardoArcano.puoLanciarlo(mago));
        assertFalse(DardoArcano.puoLanciarlo(new Ladro("Pippo", 5)));
    }

    @Test
    void alMagoRendeDiPiuECostaMenoCheAllElfo() {
        Mago mago = new Mago("Merlino", 5);
        Elfo elfo = new Elfo("Legolas", 5);
        assertEquals(Costanti.DARDO_ARCANO_DANNI_MAGO, new DardoArcano(mago).getDanni());
        assertEquals(Costanti.DARDO_ARCANO_DANNI_ELFO, new DardoArcano(elfo).getDanni());
        assertEquals(Costanti.DARDO_ARCANO_COSTO_LANCIO_MAGO, new DardoArcano(mago).getCostoLancio());
        assertEquals(Costanti.DARDO_ARCANO_COSTO_LANCIO_ELFO, new DardoArcano(elfo).getCostoLancio());
        assertTrue(danno(mago) > danno(elfo));
    }

    @Test
    void lElfoHaBisognoDiPiuMagia() {
        Elfo elfo = new Elfo("Legolas", 5);
        elfo.subMagia(elfo.getMagia() - Costanti.DARDO_ARCANO_COSTO_LANCIO_MAGO);
        assertFalse(DardoArcano.puoLanciarlo(elfo));
        Mago mago = new Mago("Merlino", 5);
        mago.subMagia(mago.getMagia() - Costanti.DARDO_ARCANO_COSTO_LANCIO_MAGO);
        assertTrue(DardoArcano.puoLanciarlo(mago));
    }

    @Test
    void ilLibroMagicoAiutaIlDardo() {
        double senzaLibro = danno(new Mago("Merlino", 5));
        Mago conLibro = new Mago("Merlino", 5);
        ArtefattoMD libro = new ArtefattoMD();
        libro.setTipo(TipoArtefatto.LIBRO_MAGICO);
        libro.setNome("il libro di prova");
        libro.setDescrizione("che serve ai test");
        libro.setLivello(5);
        libro.setPeso(1);
        conLibro.addArtefatto(Artefatto.di(libro));
        assertTrue(danno(conLibro) > senzaLibro);
    }

    private double danno(PersonaggioBase formulante) {
        formulante.addModificatore(new ModificatoreAttributo(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        formulante.addModificatore(new ModificatoreAttributo(TipoAttributo.INTELLIGENZA, TipoModificatore.QUANTITA_ASSOLUTA, 20));
        return CalcolatoreCombattimento.calcolaDannoRisultante(formulante, difensore, new DardoArcano(formulante)).getDanno();
    }
}
