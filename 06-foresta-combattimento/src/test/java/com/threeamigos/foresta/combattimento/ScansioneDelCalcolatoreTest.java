package com.threeamigos.foresta.combattimento;

import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle;
import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.personaggi.Mago;
import com.threeamigos.foresta.personaggi.OmbraFiamma;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole del combattimento passate in rassegna con tutte le combinazioni che contano: ogni tipo di danno, ogni stato del bersaglio e
 * dell'attaccante, armi semplici, incantate, con modificatori sul bersaglio e incantesimi, classi che resistono o sono immuni. Il dado ha un
 * seme fisso, quindi la prova è sempre la stessa; ogni scenario gira due volte, con il registro spento e acceso (le due strade del codice).
 */
class ScansioneDelCalcolatoreTest {

    private static final DestinazioneDiLog POZZO = new DestinazioneDiLog() {
        @Override
        public void scrivi(String messaggio) {
        }

        @Override
        public void scrivi(Exception e) {
        }
    };

    @BeforeEach
    void prepara(RepetitionInfo ripetizione) {
        DatiDiProva.nuovaPartita();
        Dado.impostaSeme(12345L + ripetizione.getCurrentRepetition());
        Logger.impostaDestinazione(ripetizione.getCurrentRepetition() == 2 ? POZZO : DestinazioneDiLog.NESSUNA);
    }

    @AfterEach
    void ripulisce() {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
        ModalitaDiProva.setAttiva(false);
        Dado.ripristina();
    }

    // --- Armi

    private static Arma arma(TipoDanno tipoDanno, boolean magica, List<IncantamentoMD> incantamenti, List<ModificatoreAttributoMD> sulBersaglio) {
        return new Arma() {
            @Override
            public int getDanni() {
                return 12;
            }

            @Override
            public int getLivello() {
                return 4;
            }

            @Override
            public TipoDanno getTipoDanno() {
                return tipoDanno;
            }

            @Override
            public boolean isIncantata() {
                return !incantamenti.isEmpty();
            }

            @Override
            public boolean isMagica() {
                return magica;
            }

            @Override
            public Collection<IncantamentoMD> getIncantamenti() {
                return incantamenti;
            }

            @Override
            public Collection<ModificatoreAttributoMD> getModificatoriSulBersaglio() {
                return sulBersaglio;
            }
        };
    }

    private static List<Arma> armiPer(TipoDanno tipoDanno) {
        List<Arma> armi = new ArrayList<>();
        armi.add(arma(tipoDanno, false, Collections.emptyList(), Collections.emptyList()));
        armi.add(arma(tipoDanno, true, Collections.emptyList(), Collections.emptyList()));
        List<IncantamentoMD> tutti = new ArrayList<>();
        for (TipoDanno elemento : TipoDanno.values()) {
            tutti.add(new IncantamentoMD("Elemento " + elemento, elemento, 3, 0.2d));
        }
        armi.add(arma(tipoDanno, false, tutti, Collections.emptyList()));
        armi.add(arma(tipoDanno, true, tutti, Collections.emptyList()));
        ModificatoreAttributoMD sulBersaglio = new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0, "colpo",
                UnitaDurataModificatore.TURNI, 2, BersaglioModificatore.BERSAGLIO, true);
        armi.add(arma(tipoDanno, false, Collections.emptyList(), Collections.singletonList(sulBersaglio)));
        return armi;
    }

    // --- Personaggi

    private static Personaggio conStato(Personaggio p, TipoEffettoDiStato stato) {
        if (stato != null) {
            p.addEffettoDiStato(stato, 3, 0);
        }
        return p;
    }

    private static List<TipoEffettoDiStato> statiPossibili() {
        List<TipoEffettoDiStato> stati = new ArrayList<>(Arrays.asList(TipoEffettoDiStato.values()));
        stati.add(0, null);
        return stati;
    }

    private static Personaggio attaccanteConLibro() {
        Personaggio mago = new Mago("Merlino", 8);
        Artefatto libro = new GeneratoreArtefattiTabelle(new Random(5)).generaArtefatto(TipoArtefatto.LIBRO_MAGICO, 3);
        mago.addArtefatto(libro);
        return mago;
    }

    // --- Colpire

    @RepeatedTest(2)
    void laProbabilitaDiColpireTieneContoDiStatiELimiti() {
        for (CategoriaDanno categoria : CategoriaDanno.values()) {
            for (TipoEffettoDiStato statoAttaccante : statiPossibili()) {
                for (TipoEffettoDiStato statoDifensore : statiPossibili()) {
                    Personaggio attaccante = conStato(new Guerriero("A", 6), statoAttaccante);
                    Personaggio difensore = conStato(new Guerriero("D", 6), statoDifensore);
                    int probabilita = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore, categoria);
                    assertTrue(probabilita >= 0 && probabilita <= 100);
                    CalcolatoreCombattimento.colpisce(attaccante, difensore, categoria);
                }
            }
        }
        // Un attaccante stordito non colpisce; un difensore a terra, congelato o stordito è colpito sempre
        Personaggio stordito = conStato(new Guerriero("A", 6), TipoEffettoDiStato.STORDITO);
        assertEquals(0, CalcolatoreCombattimento.calcolaProbabilitaDiColpire(stordito, new Guerriero("D", 6), CategoriaDanno.FISICO));
        for (TipoEffettoDiStato stato : new TipoEffettoDiStato[] {TipoEffettoDiStato.ATTERRATO, TipoEffettoDiStato.CONGELATO, TipoEffettoDiStato.STORDITO}) {
            assertEquals(100, CalcolatoreCombattimento.calcolaProbabilitaDiColpire(new Guerriero("A", 1), conStato(new Guerriero("D", 6), stato), CategoriaDanno.FISICO));
        }
        // I limiti: 5 al minimo e 95 al massimo
        Personaggio debole = new Guerriero("Debole", 1);
        debole.addModificatore(new ModificatoreAttributoMD(TipoAttributo.PRECISIONE, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        debole.addModificatore(new ModificatoreAttributoMD(TipoAttributo.DESTREZZA, TipoModificatore.QUANTITA_ASSOLUTA, 0));
        Personaggio forte = new Guerriero("Forte", 1);
        forte.addModificatore(new ModificatoreAttributoMD(TipoAttributo.VELOCITA, TipoModificatore.QUANTITA_ASSOLUTA, 500));
        assertEquals(5, CalcolatoreCombattimento.calcolaProbabilitaDiColpire(debole, forte, CategoriaDanno.FISICO));
        Personaggio fortissimo = new Guerriero("Fortissimo", 1);
        fortissimo.addModificatore(new ModificatoreAttributoMD(TipoAttributo.PRECISIONE, TipoModificatore.QUANTITA_ASSOLUTA, 900));
        assertEquals(95, CalcolatoreCombattimento.calcolaProbabilitaDiColpire(fortissimo, debole, CategoriaDanno.FISICO));
        // In prova l'OmbraFiamma non sbaglia mai
        ModalitaDiProva.setAttiva(true);
        assertEquals(100, CalcolatoreCombattimento.calcolaProbabilitaDiColpire(new OmbraFiamma(3), forte, CategoriaDanno.FISICO));
        assertTrue(CalcolatoreCombattimento.calcolaDannoRisultante(new OmbraFiamma(3), new Guerriero("D", 1), arma(TipoDanno.TAGLIENTE, false,
                Collections.emptyList(), Collections.emptyList())).getDanno() >= 100);
        ModalitaDiProva.setAttiva(false);
    }

    @RepeatedTest(2)
    void leQuoteDegliIncantesimiAPiuBersagli() {
        assertEquals(1.0d, CalcolatoreCombattimento.quotaDelDannoPerPosizione(0), 1e-9);
        assertEquals(0.5d, CalcolatoreCombattimento.quotaDelDannoPerPosizione(1), 1e-9);
        assertEquals(1.0d, CalcolatoreCombattimento.quotaDelDannoPerPosizione(-4), 1e-9);
        assertTrue(CalcolatoreCombattimento.quotaDelDannoPerPosizione(50) > 0);
    }

    // --- Il danno

    @RepeatedTest(2)
    void ilDannoPerOgniTipoStatoEArma() {
        List<Personaggio> attaccanti = new ArrayList<>();
        attaccanti.add(new Guerriero("Guerriero", 6));
        attaccanti.add(attaccanteConLibro());
        Personaggio furioso = new Guerriero("Furioso", 6);
        furioso.addEffettoDiStato(TipoEffettoDiStato.BERSERK, 1, 0);
        furioso.getModelloDati().setSalute(5);
        attaccanti.add(furioso);
        Personaggio fratturato = new Mago("Fratturato", 6);
        fratturato.addEffettoDiStato(TipoEffettoDiStato.MENTE_FRATTURATA, 3, 0);
        attaccanti.add(fratturato);
        Personaggio saggio = new Guerriero("Saggio", 6);
        saggio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.CRITICO, TipoModificatore.QUANTITA_ASSOLUTA, 100));
        saggio.addModificatore(new ModificatoreAttributoMD(TipoAttributo.FURIA, TipoModificatore.QUANTITA_ASSOLUTA, 50));
        attaccanti.add(saggio);
        TipoPersonaggio[] classiDifensori = {TipoPersonaggio.GUERRIERO, TipoPersonaggio.DRAGO, TipoPersonaggio.SCHELETRO, TipoPersonaggio.GARGOYLE,
                TipoPersonaggio.MAGO, TipoPersonaggio.TROLL};
        int calcolati = 0;
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            for (Arma arma : armiPer(tipoDanno)) {
                for (Personaggio attaccante : attaccanti) {
                    for (TipoPersonaggio classe : classiDifensori) {
                        for (TipoEffettoDiStato stato : statiPossibili()) {
                            Personaggio difensore = conStato(FabbricaPersonaggi.crea(classe, 6), stato);
                            DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma);
                            assertNotNull(risultato);
                            assertTrue(risultato.getDanno() >= 0);
                            calcolati++;
                        }
                    }
                }
            }
        }
        assertTrue(calcolati > 10000, "calcoli: " + calcolati);
    }

    @RepeatedTest(2)
    void laSecondaArmaFaMenoDanno() {
        Personaggio ladro = new Ladro("Arsenio", 6);
        Personaggio difensore = new Guerriero("Bersaglio", 6);
        Arma arma = arma(TipoDanno.TAGLIENTE, false, Collections.emptyList(), Collections.emptyList());
        int pieno = CalcolatoreCombattimento.calcolaDannoRisultante(ladro, difensore, arma, 1.0d).getDanno();
        int ridotto = CalcolatoreCombattimento.calcolaDannoRisultante(ladro, difensore, arma, 0.4d).getDanno();
        assertTrue(ridotto <= pieno);
    }

    // --- Danno ad area

    @RepeatedTest(2)
    void leReazioniAdAreaInvestonoIVicini() {
        List<Personaggio> fila = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            fila.add(new Guerriero("G" + i, 5));
        }
        fila.get(4).muore("prova");
        Personaggio bersaglio = fila.get(2);
        for (Personaggio p : fila) {
            p.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
        }
        fila.get(5).rimuoviEffettoDiStato(TipoEffettoDiStato.BAGNATO);
        // Fuori dalla fila non investe nessuno
        assertTrue(CalcolatoreCombattimento.viciniInvestiti(fila, new Guerriero("Altro", 1), TipoInterazioneConEffettiDiStato.RIGETTO).isEmpty());
        assertEquals(2, CalcolatoreCombattimento.viciniInvestiti(fila, bersaglio, TipoInterazioneConEffettiDiStato.RIGETTO).size());
        // La pozza di elettrocuzione si estende finché ci sono bagnati
        List<Personaggio> pozza = CalcolatoreCombattimento.viciniInvestiti(fila, bersaglio, TipoInterazioneConEffettiDiStato.ELETTROCUZIONE);
        assertEquals(3, pozza.size());
        Personaggio attaccante = new Mago("Merlino", 6);
        for (TipoInterazioneConEffettiDiStato interazione : TipoInterazioneConEffettiDiStato.values()) {
            if (interazione.getDannoAdArea() == null) {
                continue;
            }
            DannoRisultante.DannoAdArea area = new DannoRisultante.DannoAdArea(interazione, 40.0d);
            for (TipoPersonaggio classe : new TipoPersonaggio[] {TipoPersonaggio.GUERRIERO, TipoPersonaggio.DRAGO, TipoPersonaggio.SCHELETRO}) {
                for (TipoEffettoDiStato stato : statiPossibili()) {
                    Personaggio vicino = conStato(FabbricaPersonaggi.crea(classe, 5), stato);
                    DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoAdArea(attaccante, vicino, area);
                    assertTrue(risultato.getDanno() >= 0);
                }
            }
        }
    }

    // --- Stime per le tattiche

    @RepeatedTest(2)
    void leStimeDelleTattiche() {
        Personaggio ladro = new Ladro("Arsenio", 8);
        ladro.addArtefatto(new GeneratoreArtefattiTabelle(new Random(7)).generaArtefatto(TipoArtefatto.SPADA, 3));
        ladro.addArtefatto(new GeneratoreArtefattiTabelle(new Random(8)).generaArtefatto(TipoArtefatto.SPADA, 3));
        Personaggio mago = attaccanteConLibro();
        Personaggio difensore = new Guerriero("Bersaglio", 6);
        assertEquals(2, CalcolatoreCombattimento.fasiDiAttacco(ladro).size());
        assertEquals(1, CalcolatoreCombattimento.fasiDiAttacco(difensore).size());
        assertTrue(CalcolatoreCombattimento.dannoAttesoDellaMischia(ladro, difensore) > 0);
        Personaggio ombra = new OmbraFiamma(5);
        assertEquals(1, CalcolatoreCombattimento.fasiDiAttacco(ombra, FabbricaPersonaggi.crea(TipoPersonaggio.DRAGO, 5)).size());
        for (TipoDanno tipoDanno : TipoDanno.values()) {
            for (Arma arma : armiPer(tipoDanno)) {
                for (Personaggio attaccante : new Personaggio[] {ladro, mago}) {
                    assertTrue(CalcolatoreCombattimento.dannoAttesoDellArma(attaccante, arma) >= 0);
                    for (TipoPersonaggio classe : new TipoPersonaggio[] {TipoPersonaggio.GUERRIERO, TipoPersonaggio.DRAGO, TipoPersonaggio.SCHELETRO}) {
                        for (TipoEffettoDiStato stato : statiPossibili()) {
                            Personaggio bersaglio = conStato(FabbricaPersonaggi.crea(classe, 6), stato);
                            assertTrue(CalcolatoreCombattimento.dannoAtteso(attaccante, bersaglio, arma) >= 0);
                            assertTrue(CalcolatoreCombattimento.moltiplicatoreAttesoDopo(bersaglio, tipoDanno, arma) >= 0);
                            Set<TipoEffettoDiStato> causabili = CalcolatoreCombattimento.statiCausabili(bersaglio, arma);
                            Set<TipoEffettoDiStato> tolti = CalcolatoreCombattimento.statiTolti(bersaglio, arma);
                            assertTrue(Collections.disjoint(causabili, tolti));
                            assertTrue(CalcolatoreCombattimento.moltiplicatoreAttesoDopo(bersaglio, tipoDanno, causabili) >= 0);
                            assertTrue(CalcolatoreCombattimento.moltiplicatoreAtteso(bersaglio, tipoDanno) >= 0);
                        }
                    }
                }
            }
        }
    }

    @RepeatedTest(2)
    void durataEDannoPeriodicoDiOgniStato() {
        Personaggio attaccante = new Mago("Merlino", 8);
        Personaggio difensore = new Guerriero("Bersaglio", 8);
        for (TipoEffettoDiStato stato : TipoEffettoDiStato.values()) {
            int durata = CalcolatoreCombattimento.calcolaDurataStato(difensore, stato);
            assertTrue(durata >= 1 && durata <= 4, stato + " " + durata);
            assertTrue(CalcolatoreCombattimento.calcolaDannoPeriodico(attaccante, difensore, stato) >= 0, stato.name());
        }
        assertEquals(1, CalcolatoreCombattimento.calcolaDurataStato(difensore, TipoEffettoDiStato.ATTERRATO));
        assertEquals(1, CalcolatoreCombattimento.calcolaDurataStato(difensore, TipoEffettoDiStato.BERSERK));
        assertEquals(0, CalcolatoreCombattimento.calcolaDannoPeriodico(attaccante, difensore, TipoEffettoDiStato.CONFUSO));
    }

    @RepeatedTest(2)
    void ladifesaTieneContoDegliIncantamentiDeiPezziDifensivi() {
        Personaggio difensore = new Guerriero("Bersaglio", 8);
        Artefatto scudo = new GeneratoreArtefattiTabelle(new Random(9)).generaArtefatto(TipoArtefatto.SCUDO, 4);
        scudo.getModelloDati().rimuoviTuttiGliIncantamenti();
        scudo.getModelloDati().addIncantamento(new IncantamentoMD("Scudo di fuoco", TipoDanno.FUOCO, 5, 0.3d));
        difensore.addArtefatto(scudo);
        double senza = CalcolatoreCombattimento.difesaContro(new Guerriero("Altro", 8), TipoDanno.FUOCO);
        assertTrue(CalcolatoreCombattimento.difesaContro(difensore, TipoDanno.FUOCO) > senza);
        assertTrue(CalcolatoreCombattimento.difesaContro(difensore, TipoDanno.GELO) >= senza - 100);
    }
}
