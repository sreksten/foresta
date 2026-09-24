package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.incantesimi.ClasseIncantesimo;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.motore.modellodati.TipoModificatore;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.PersonaggioBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Le regole di combattimento legate all'equipaggiamento (vedi artefatti_e_incantamenti.md, §2 "Combattimento"):
 * resistenze di elmo, scudo e armatura, doppia arma, guardia aperta, parata dello scudo, libro magico.
 * Come in CalcolatoreCombattimentoBerserkTest il critico è neutralizzato, così i danni sono deterministici.
 */
class CalcolatoreCombattimentoEquipaggiamentoTest {

    private static final double DELTA = 1e-9;

    private Guerriero difensore;

    @BeforeEach
    void preparaDifensore() {
        ModelloDati.setIstanza(new ModelloDati());
        difensore = new Guerriero("Bersaglio", 5);
        difensore.addModificatore(new ModificatoreAttributo(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 1000));
        difensore.addModificatore(new ModificatoreAttributo(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.QUANTITA_ASSOLUTA, 30));
    }

    // --- Resistenze

    @Test
    void lArmaturaIncantataAlzaLaDifesaControQuelTipoDiDanno() {
        // Difesa base 30; armatura di livello 3 con un fuoco medio (+10, +10%, che sull'armatura vale la metà)
        difensore.addArtefatto(pezzoIncantato(TipoArtefatto.ARMATURA, 3, TipoDanno.FUOCO, 10, 0.10));
        // (30 + 10 × 3) × (1 + 0,05) = 63: il danno di fuoco passa dal 77% al 61%
        assertEquals(63.0, CalcolatoreCombattimento.difesaContro(difensore, TipoDanno.FUOCO), DELTA);
        assertEquals(30.0, CalcolatoreCombattimento.difesaContro(difensore, TipoDanno.GELO), DELTA);
    }

    @Test
    void siSommanoLeResistenzeDiElmoScudoEArmatura() {
        difensore.addArtefatto(pezzoIncantato(TipoArtefatto.ELMO, 2, TipoDanno.FUOCO, 5, 0.10));
        difensore.addArtefatto(pezzoIncantato(TipoArtefatto.SCUDO, 1, TipoDanno.FUOCO, 5, 0.10));
        // (30 + resistenza magica dello scudo + 5 × 2 + 5 × 1) × (1 + 0,05 + 0,05)
        double base = 30 + Costanti.SCUDO_RESISTENZA_MAGICA_PER_LIVELLO;
        assertEquals((base + 15) * 1.1, CalcolatoreCombattimento.difesaContro(difensore, TipoDanno.FUOCO), DELTA);
    }

    @Test
    void gliIncantamentiDelleArmiNonProteggono() {
        difensore.addArtefatto(pezzoIncantato(TipoArtefatto.SPADA, 3, TipoDanno.FUOCO, 10, 0.10));
        assertEquals(30.0, CalcolatoreCombattimento.difesaContro(difensore, TipoDanno.FUOCO), DELTA);
    }

    @Test
    void laResistenzaRiduceIlDannoDiFuoco() {
        Guerriero attaccante = attaccante();
        Arma spadaDiFuoco = arma(TipoDanno.FUOCO);
        int senzaArmatura = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, spadaDiFuoco).getDanno();
        difensore.addArtefatto(pezzoIncantato(TipoArtefatto.ARMATURA, 3, TipoDanno.FUOCO, 10, 0.10));
        int conArmatura = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, spadaDiFuoco).getDanno();
        assertTrue(conArmatura < senzaArmatura, "con armatura " + conArmatura + ", senza " + senzaArmatura);
    }

    @Test
    void laResistenzaMitigaAncheGliIncantamentiDellArma() {
        Guerriero attaccante = attaccante();
        Artefatto spada = pezzoIncantato(TipoArtefatto.SPADA, 5, TipoDanno.FUOCO, 10, 0.10);
        spada.getModelloDati().setDanni(10);
        Arma arma = (Arma) spada;
        int senzaArmatura = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma).getDanno();
        // Una resistenza al fuoco non tocca il danno fisico della spada, solo il suo incantamento
        difensore.addArtefatto(pezzoIncantato(TipoArtefatto.ARMATURA, 3, TipoDanno.FUOCO, 10, 0.10));
        int conArmatura = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma).getDanno();
        assertTrue(conArmatura < senzaArmatura, "con armatura " + conArmatura + ", senza " + senzaArmatura);
    }

    // --- Doppia arma

    @Test
    void chiImpugnaDueArmiAttaccaDueVolteLaSecondaRidotta() {
        Ladro ladro = new Ladro("Pippo", 3);
        ladro.addArtefatto(arma(TipoArtefatto.SPADA, 3));
        ladro.addArtefatto(arma(TipoArtefatto.MAZZA, 3));
        List<FaseDiAttacco> fasi = CalcolatoreCombattimento.fasiDiAttacco(ladro);
        assertEquals(2, fasi.size());
        assertEquals(1.0, fasi.get(0).getFattore(), DELTA);
        assertEquals(Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA, fasi.get(1).getFattore(), DELTA);
    }

    @Test
    void conUnaSolaArmaSiAttaccaUnaVolta() {
        Guerriero guerriero = new Guerriero("Pippo", 3);
        guerriero.addArtefatto(arma(TipoArtefatto.SPADA, 3));
        assertEquals(1, CalcolatoreCombattimento.fasiDiAttacco(guerriero).size());
    }

    @Test
    void laSecondaArmaFaUnaQuotaDelDanno() {
        Guerriero attaccante = attaccante();
        Arma spada = arma(TipoDanno.TAGLIENTE);
        int pieno = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, spada, 1.0).getDanno();
        int ridotto = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, spada,
                Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA).getDanno();
        // Il danno mitigato si arrotonda per difetto: al più un punto di scarto
        assertEquals(pieno * Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA, ridotto, 1.0);
    }

    // --- Parata

    @Test
    void loScudoDaUnaParataMinimaPiuUnaPerLivello() {
        Guerriero guerriero = conParata(new Guerriero("Pippo", 5), 20);
        guerriero.addArtefatto(artefatto(TipoArtefatto.SCUDO, 4));
        assertEquals(20 + Costanti.SCUDO_PARATA_MINIMA + 4 * Costanti.SCUDO_PARATA_PER_LIVELLO, guerriero.getParata());
    }

    @Test
    void ancheElmoEArmaturaDannoUnaParataMinima() {
        Guerriero guerriero = conParata(new Guerriero("Pippo", 5), 20);
        guerriero.addArtefatto(artefatto(TipoArtefatto.ELMO, 1));
        guerriero.addArtefatto(artefatto(TipoArtefatto.ARMATURA, 3));
        assertEquals(20 + Costanti.ELMO_PARATA_MINIMA + Costanti.ELMO_PARATA_PER_LIVELLO
                + Costanti.ARMATURA_PARATA_MINIMA + 3 * Costanti.ARMATURA_PARATA_PER_LIVELLO, guerriero.getParata());
    }

    @Test
    void laVesteDaResistenzaMagicaInveceDiParata() {
        Mago mago = conParata(conResistenzaMagica(new Mago("Merlino", 5), 20), 20);
        mago.addArtefatto(artefatto(TipoArtefatto.VESTE, 3));
        assertEquals(20, mago.getParata());
        assertEquals(20 + Costanti.ARMATURA_PARATA_MINIMA + 3 * Costanti.ARMATURA_PARATA_PER_LIVELLO, mago.getResistenzaMagica());
    }

    @Test
    void loScudoDaResistenzaMagicaDiPiuSeERaro() {
        Guerriero comune = conResistenzaMagica(new Guerriero("Pippo", 5), 20);
        comune.addArtefatto(artefatto(TipoArtefatto.SCUDO, 4));
        assertEquals(20 + 4 * Costanti.SCUDO_RESISTENZA_MAGICA_PER_LIVELLO, comune.getResistenzaMagica());

        Guerriero raro = conResistenzaMagica(new Guerriero("Pippo", 5), 20);
        Artefatto scudoRaro = artefatto(TipoArtefatto.SCUDO, 4);
        scudoRaro.getModelloDati().setRarita(RaritaArtefatto.RARO);
        raro.addArtefatto(scudoRaro);
        assertEquals(20 + 4 * Costanti.SCUDO_RARO_RESISTENZA_MAGICA_PER_LIVELLO, raro.getResistenzaMagica());
    }

    @Test
    void conDueArmiLaGuardiaEAperta() {
        Ladro ladro = conParata(new Ladro("Pippo", 3), 20);
        ladro.addArtefatto(arma(TipoArtefatto.SPADA, 3));
        assertEquals(20, ladro.getParata());
        ladro.addArtefatto(arma(TipoArtefatto.MAZZA, 3));
        assertEquals(15, ladro.getParata());
    }

    @Test
    void conUnArmaADueManiLaGuardiaEAperta() {
        Guerriero guerriero = conParata(new Guerriero("Pippo", 3), 20);
        guerriero.addArtefatto(arma(TipoArtefatto.SPADONE, 3));
        assertEquals(15, guerriero.getParata());
    }

    // --- Libro magico

    @Test
    void ilBonusDelLibroEQuelloDelSuoGradoPiuUnQuarto() {
        // Livello 4: grado medio, +10 e +10%, più il 25%
        Incantamento bonus = CalcolatoreCombattimento.bonusLibroMagico(artefatto(TipoArtefatto.LIBRO_MAGICO, 4), TipoDanno.ARIA);
        assertEquals(TipoDanno.ARIA, bonus.getTipoDannoElementale());
        assertEquals(13, bonus.getDannoBonusFisso());
        assertEquals(0.125, bonus.getCoefficienteScala(), DELTA);
    }

    @Test
    void ilLibroMagicoAumentaIlDannoDegliIncantesimi() {
        Mago mago = new Mago("Merlino", 4);
        mago.addModificatore(new ModificatoreAttributo(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        IncantesimoMalefico aria = (IncantesimoMalefico) ClasseIncantesimo.ARIA.getIstanza(4);
        int senzaLibro = CalcolatoreCombattimento.calcolaDannoRisultante(mago, difensore, aria).getDanno();
        mago.addArtefatto(artefatto(TipoArtefatto.LIBRO_MAGICO, 4));
        int conLibro = CalcolatoreCombattimento.calcolaDannoRisultante(mago, difensore, aria).getDanno();
        assertTrue(conLibro > senzaLibro, "con libro " + conLibro + ", senza " + senzaLibro);
    }

    @Test
    void ilLibroMagicoNonAiutaLeArmi() {
        Mago mago = new Mago("Merlino", 4);
        mago.addModificatore(new ModificatoreAttributo(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        Arma bastone = arma(TipoDanno.CONTUNDENTE);
        int senzaLibro = CalcolatoreCombattimento.calcolaDannoRisultante(mago, difensore, bastone).getDanno();
        mago.addArtefatto(artefatto(TipoArtefatto.LIBRO_MAGICO, 4));
        assertEquals(senzaLibro, CalcolatoreCombattimento.calcolaDannoRisultante(mago, difensore, bastone).getDanno());
    }

    // --- Aiuti

    private static Guerriero attaccante() {
        Guerriero attaccante = new Guerriero("Attaccante", 5);
        attaccante.addModificatore(new ModificatoreAttributo(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        return attaccante;
    }

    private static <P extends PersonaggioBase> P conParata(P personaggio, int parata) {
        personaggio.addModificatore(new ModificatoreAttributo(TipoAttributo.PARATA, TipoModificatore.QUANTITA_ASSOLUTA, parata));
        return personaggio;
    }

    private static <P extends PersonaggioBase> P conResistenzaMagica(P personaggio, int resistenza) {
        personaggio.addModificatore(new ModificatoreAttributo(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.QUANTITA_ASSOLUTA, resistenza));
        return personaggio;
    }

    private static Artefatto artefatto(TipoArtefatto tipo, int livello) {
        ArtefattoMD md = new ArtefattoMD();
        md.setTipo(tipo);
        md.setNome("l'oggetto di prova");
        md.setDescrizione("che serve ai test");
        md.setLivello(livello);
        md.setPeso(1);
        return Artefatto.di(md);
    }

    private static Artefatto arma(TipoArtefatto tipo, int livello) {
        Artefatto arma = artefatto(tipo, livello);
        arma.getModelloDati().setDanni(10);
        return arma;
    }

    private static Artefatto pezzoIncantato(TipoArtefatto tipo, int livello, TipoDanno tipoDanno, int fisso, double coefficiente) {
        Artefatto pezzo = artefatto(tipo, livello);
        pezzo.getModelloDati().addIncantamento("Incantamento di prova", tipoDanno, fisso, coefficiente);
        return pezzo;
    }

    private static Arma arma(TipoDanno tipoDanno) {
        return new Arma() {
            @Override
            public int getDanni() {
                return 10;
            }

            @Override
            public int getLivello() {
                return 5;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return tipoDanno;
            }

            @Override
            public boolean isIncantata() {
                return false;
            }

            @Override
            public java.util.Collection<Incantamento> getIncantamenti() {
                return java.util.Collections.emptyList();
            }
        };
    }
}
