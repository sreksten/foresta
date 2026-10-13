package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;

/**
 * Il fattore del danno atteso contro un difensore (CalcolatoreCombattimento.moltiplicatoreAtteso): la debolezza della
 * classe, quella degli stati e la difesa, senza tirare i dadi.
 */
class MoltiplicatoreAttesoTest {

    private static final double DELTA = 1e-9;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    private static Personaggio difensore(TipoPersonaggio tipo) {
        Personaggio difensore = FabbricaPersonaggi.crea(tipo, 5);
        // Le statistiche seguono il seme, ma qui si fissano comunque, così il test non dipende da come si tirano
        for (TipoAttributo attributo : new TipoAttributo[] {TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE,
                TipoAttributo.INTELLIGENZA, TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA}) {
            ((PersonaggioBase) difensore).addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA, 12));
        }
        return difensore;
    }

    private static double mitigazione(Personaggio difensore, TipoDanno tipoDanno) {
        return 100.0d / (100.0d + CalcolatoreCombattimento.difesaContro(difensore, tipoDanno));
    }

    @Test
    void senzaStatiEDebolezzeESoloLaMitigazioneDellaDifesa() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            assertTrue(CalcolatoreCombattimento.interazioniAttive(guerriero, tipoDanno).isEmpty(), tipoDanno.name());
            assertEquals(mitigazione(guerriero, tipoDanno), CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, tipoDanno), DELTA, tipoDanno.name());
        }
    }

    @Test
    void unBagnatoEDebolAlFulmineEResistenteAlFuoco() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        double fulmine = CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.FULMINE);
        double fuoco = CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.FUOCO);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        assertEquals(Collections.singletonList(TipoInterazioneConEffettiDiStato.ELETTROCUZIONE),
                CalcolatoreCombattimento.interazioniAttive(guerriero, TipoDanno.FULMINE));
        assertEquals(fulmine * 1.5d, CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.FULMINE), DELTA);
        assertEquals(fuoco * 0.5d, CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.FUOCO), DELTA);
        // Gli altri danni non cambiano
        assertEquals(mitigazione(guerriero, TipoDanno.ACIDO), CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.ACIDO), DELTA);
    }

    @Test
    void unNonMortoEImmuneAlVelenoEDeboleAlSacro() {
        Personaggio scheletro = difensore(TipoPersonaggio.SCHELETRO);
        assertEquals(0.0d, CalcolatoreCombattimento.moltiplicatoreAtteso(scheletro, TipoDanno.VELENO), DELTA);
        assertTrue(CalcolatoreCombattimento.interazioniAttive(scheletro, TipoDanno.VELENO).isEmpty());
        // La debolezza della classe non è un'interazione con uno stato: è nel moltiplicatore del danno subito
        assertTrue(CalcolatoreCombattimento.interazioniAttive(scheletro, TipoDanno.SACRO).isEmpty());
        assertEquals(2.0d, scheletro.getMoltiplicatoreDannoSubito(TipoDanno.SACRO), DELTA);
        assertEquals(2.0d * mitigazione(scheletro, TipoDanno.SACRO),
                CalcolatoreCombattimento.moltiplicatoreAtteso(scheletro, TipoDanno.SACRO), DELTA);
        assertEquals(mitigazione(scheletro, TipoDanno.FUOCO), CalcolatoreCombattimento.moltiplicatoreAtteso(scheletro, TipoDanno.FUOCO), DELTA);
    }

    @Test
    void ilGuscioDiGhiaccioParaLeLameMaNonIlContundenteEIlFulmineLaIndebolisce() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.CONGELATO, 3, 0);
        double difesaTagliente = CalcolatoreCombattimento.difesaContro(guerriero, TipoDanno.TAGLIENTE);
        assertEquals(100.0d / (100.0d + difesaTagliente * 1.5d), CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.TAGLIENTE), DELTA);
        // Il contundente frantuma il ghiaccio: danno doppio e la difesa di prima
        assertEquals(2.0d * mitigazione(guerriero, TipoDanno.CONTUNDENTE),
                CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.CONTUNDENTE), DELTA);
        double difesaFulmine = CalcolatoreCombattimento.difesaContro(guerriero, TipoDanno.FULMINE);
        assertEquals(1.3d * 100.0d / (100.0d + difesaFulmine * 0.7d), CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.FULMINE), DELTA);
    }

    @Test
    void conPiuStatiLeInterazioniSiApplicanoNellOrdineDelCalcolo() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.BRUCIATO, 3, 0);
        assertEquals(Arrays.asList(TipoInterazioneConEffettiDiStato.CONGELAMENTO, TipoInterazioneConEffettiDiStato.SCIOGLIMENTO_TERMICO),
                CalcolatoreCombattimento.interazioniAttive(guerriero, TipoDanno.GELO));
        assertEquals(1.5d * mitigazione(guerriero, TipoDanno.GELO), CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.GELO), DELTA);
    }

    @Test
    void chiHaPauraEConfusioneSubisceLeReazioniDiEntrambi() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.CONFUSO, 3, 0);
        // La Follia Cosmica vuole il CONFUSO, anche se è pure SPAVENTATO
        assertEquals(Collections.singletonList(TipoInterazioneConEffettiDiStato.FOLLIA_COSMICA),
                CalcolatoreCombattimento.interazioniAttive(guerriero, TipoDanno.VUOTO));
        assertEquals(Collections.singletonList(TipoInterazioneConEffettiDiStato.SHOCK_DI_REALTA),
                CalcolatoreCombattimento.interazioniAttive(guerriero, TipoDanno.TAGLIENTE));
        assertEquals(Collections.singletonList(TipoInterazioneConEffettiDiStato.SOVRACCARICO_MENTALE),
                CalcolatoreCombattimento.interazioniAttive(guerriero, TipoDanno.ARCANO));
    }

    @Test
    void ilCalcoloNonCambiaNienteENonTiraIDadi() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO, 3, 0);
        Dado.impostaSeme(42);
        int primo = Dado.tira(1000);
        Dado.impostaSeme(42);
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, tipoDanno);
        }
        assertEquals(primo, Dado.tira(1000), "il calcolo ha usato i dadi");
        assertTrue(guerriero.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO) && guerriero.hasEffettoDiStato(TipoEffettoDiStato.SPAVENTATO));
        assertEquals(2, guerriero.getEffettiDiStato().size());
    }

    @Test
    void ilFulmineDopoLAcquaTrovaUnBagnatoEPesaDiPiu() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        Arma acqua = (Arma) FabbricaIncantesimi.crea(ClasseIncantesimo.ACQUA, 5);
        assertEquals(java.util.EnumSet.of(TipoEffettoDiStato.BAGNATO), CalcolatoreCombattimento.statiCausabili(guerriero, acqua));
        double adesso = CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.FULMINE);
        double dopo = CalcolatoreCombattimento.moltiplicatoreAttesoDopo(guerriero, TipoDanno.FULMINE, acqua);
        assertEquals(adesso * TipoInterazioneConEffettiDiStato.ELETTROCUZIONE.getMoltiplicatore(), dopo, DELTA);
        // Il difensore non è cambiato
        assertFalse(guerriero.hasEffettoDiStato(TipoEffettoDiStato.BAGNATO));
    }

    @Test
    void ilGeloPuoCausareIlCongelamentoEQuindiIlGuscioDiGhiaccio() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        Arma gelo = (Arma) FabbricaIncantesimi.crea(ClasseIncantesimo.GELO, 5);
        assertTrue(CalcolatoreCombattimento.statiCausabili(guerriero, gelo).contains(TipoEffettoDiStato.CONGELATO));
        // Un CONGELATO ha un guscio che para i colpi di lama (x1,5 di difesa): il colpo che segue fa meno danno
        double adesso = CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, TipoDanno.TAGLIENTE);
        double dopo = CalcolatoreCombattimento.moltiplicatoreAttesoDopo(guerriero, TipoDanno.TAGLIENTE,
                java.util.EnumSet.of(TipoEffettoDiStato.CONGELATO));
        assertTrue(dopo > 0 && dopo < adesso, adesso + " -> " + dopo);
    }

    @Test
    void chiEImmuneAUnStatoOAUnTipoDiDannoNonLoRiceveDaiStatiCausabili() {
        Personaggio drago = difensore(TipoPersonaggio.DRAGO);
        Arma fuoco = (Arma) FabbricaIncantesimi.crea(ClasseIncantesimo.FUOCO, 5);
        assertTrue(CalcolatoreCombattimento.statiCausabili(drago, fuoco).isEmpty(), "il Drago è immune al fuoco");
    }

    @Test
    void senzaStatiFuturiLaVarianteDaLoStessoRisultato() {
        Personaggio guerriero = difensore(TipoPersonaggio.GUERRIERO);
        guerriero.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            assertEquals(CalcolatoreCombattimento.moltiplicatoreAtteso(guerriero, tipoDanno),
                    CalcolatoreCombattimento.moltiplicatoreAttesoDopo(guerriero, tipoDanno, java.util.EnumSet.noneOf(TipoEffettoDiStato.class)),
                    DELTA, tipoDanno.name());
        }
    }
}
