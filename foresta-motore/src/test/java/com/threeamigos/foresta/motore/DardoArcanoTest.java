package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.abilita.AbilitaInnate;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.abilita.DardoArcano;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.tipi.*;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il dardo arcano, l'incantesimo innato di Mago ed Elfo. Il critico è neutralizzato, così i danni sono deterministici.
 */
class DardoArcanoTest {

    private Guerriero difensore;

    @BeforeEach
    void preparaDifensore() {
        StatoDelMotore.azzera();
        difensore = new Guerriero("Bersaglio", 5);
        difensore.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
    }

    @Test
    void loConosconoSoloMagoEdElfo() {
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.MAGO, TipoAbilita.DARDO_ARCANO));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.MAGA, TipoAbilita.DARDO_ARCANO));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.ELFO, TipoAbilita.DARDO_ARCANO));
        assertTrue(AbilitaInnate.conosciutaDa(TipoPersonaggio.ELFA, TipoAbilita.DARDO_ARCANO));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.GUERRIERO, TipoAbilita.DARDO_ARCANO));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.LADRO, TipoAbilita.DARDO_ARCANO));
        assertFalse(AbilitaInnate.conosciutaDa(TipoPersonaggio.BARDO, TipoAbilita.DARDO_ARCANO));
    }

    @Test
    void serveAbbastanzaMagia() {
        Mago mago = new Mago("Merlino", 5);
        assertTrue(AbilitaInnate.puoUsare(mago, TipoAbilita.DARDO_ARCANO));
        mago.subMagia(mago.getMagia());
        assertFalse(AbilitaInnate.puoUsare(mago, TipoAbilita.DARDO_ARCANO));
        assertFalse(AbilitaInnate.puoUsare(new Ladro("Pippo", 5), TipoAbilita.DARDO_ARCANO));
    }

    @Test
    void alMagoRendeDiPiuECostaMenoCheAllElfo() {
        Mago mago = new Mago("Merlino", 5);
        Elfo elfo = new Elfo("Legolas", 5);
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_DANNI_MAGO, new DardoArcano(mago).getDanni());
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_DANNI_ELFO, new DardoArcano(elfo).getDanni());
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO, new DardoArcano(mago).getCostoLancio());
        assertEquals(CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_ELFO, new DardoArcano(elfo).getCostoLancio());
        assertTrue(danno(mago) > danno(elfo));
    }

    @Test
    void lElfoHaBisognoDiPiuMagia() {
        Elfo elfo = new Elfo("Legolas", 5);
        elfo.subMagia(elfo.getMagia() - CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO);
        assertFalse(AbilitaInnate.puoUsare(elfo, TipoAbilita.DARDO_ARCANO));
        Mago mago = new Mago("Merlino", 5);
        mago.subMagia(mago.getMagia() - CostantiAbilitaInnate.DARDO_ARCANO_COSTO_LANCIO_MAGO);
        assertTrue(AbilitaInnate.puoUsare(mago, TipoAbilita.DARDO_ARCANO));
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
        formulante.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        formulante.addModificatore(new ModificatoreAttributoMD(TipoAttributo.INTELLIGENZA, TipoModificatore.QUANTITA_ASSOLUTA, 20));
        return CalcolatoreCombattimento.calcolaDannoRisultante(formulante, difensore, new DardoArcano(formulante)).getDanno();
    }
}
