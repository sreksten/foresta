package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.DatiDiProva;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Chi resiste a un tipo di danno (moltiplicatore fra 0 e 1) ne subisce meno anche gli effetti: gli stati nativi del tipo di danno
 * e le reazioni con gli stati scattano con quella probabilità. Chi è debole (più di 1) ha più danno ma non più effetti.
 */
class RiduzioneDegliEffettiTest {

    private static final int PROVE = 4000;

    // Il seme con cui si tirano le statistiche dell'attaccante: senza, variavano da una corsa all'altra e con loro le probabilità
    // (vedi ilModificatoreDellArmaSulBersaglioSegueLaResistenza)
    private static final long SEME_ATTACCANTE = 1L;

    @BeforeEach
    void prepara() {
        DatiDiProva.nuovaPartita();
        // Nessun lancio truccato lasciato da un altro test
        Dado.ripristina();
    }

    /** Un guerriero che per il tipo di danno indicato ha quel moltiplicatore */
    private static Personaggio difensore(TipoDanno tipoDanno, double moltiplicatore) {
        return difensore(tipoDanno, moltiplicatore, 100);
    }

    private static Personaggio difensore(TipoDanno tipoDanno, double moltiplicatore, int forza) {
        Personaggio guerriero = new Guerriero("Difensore", 5) {
            @Override
            public double getMoltiplicatoreDannoSubito(TipoDanno tipo) {
                return tipo == tipoDanno ? moltiplicatore : super.getMoltiplicatoreDannoSubito(tipo);
            }
        };
        // Le statistiche seguono il seme, ma qui si fissano comunque, così il test non dipende da come si tirano. La FORZA è alta perché la probabilità
        // degli stati nativi è il danno contro la FORZA del difensore, e deve restare lontana dal 100%
        for (TipoAttributo attributo : new TipoAttributo[] {TipoAttributo.FORZA, TipoAttributo.DESTREZZA, TipoAttributo.COSTITUZIONE,
                TipoAttributo.INTELLIGENZA, TipoAttributo.SAGGEZZA, TipoAttributo.CARISMA, TipoAttributo.FORTUNA}) {
            guerriero.addModificatore(new ModificatoreAttributoMD(attributo, TipoModificatore.QUANTITA_ASSOLUTA,
                    attributo == TipoAttributo.FORZA ? forza : 12));
        }
        return guerriero;
    }

    private static Arma arma(TipoDanno tipo, int danni, IncantamentoMD... incantamenti) {
        return new Arma() {
            @Override public int getDanni() { return danni; }
            @Override public int getLivello() { return 5; }
            @Override public TipoDanno getTipoDanno() { return tipo; }
            @Override public boolean isIncantata() { return incantamenti.length > 0; }
            @Override public Collection<IncantamentoMD> getIncantamenti() { return Arrays.asList(incantamenti); }
        };
    }

    private interface Prova {
        boolean riuscita(DannoRisultante risultato);
    }

    /** Quante volte su PROVE il colpo ha quell'esito, con un difensore che per il tipo di danno ha quel moltiplicatore */
    private static int quante(TipoDanno resistenza, double moltiplicatore, TipoDanno tipoDanno, TipoEffettoDiStato statoDelDifensore,
                              Arma arma, Prova prova) {
        return quante(resistenza, moltiplicatore, 100, tipoDanno, statoDelDifensore, arma, prova);
    }

    private static int quante(TipoDanno resistenza, double moltiplicatore, int forzaDifensore, TipoDanno tipoDanno,
                              TipoEffettoDiStato statoDelDifensore, Arma arma, Prova prova) {
        Dado.impostaSeme(SEME_ATTACCANTE);
        Personaggio attaccante = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5);
        int riuscite = 0;
        for (int i = 1; i <= PROVE; i++) {
            Personaggio difensore = difensore(resistenza, moltiplicatore, forzaDifensore);
            if (statoDelDifensore != null) {
                difensore.addEffettoDiStato(statoDelDifensore, 3, 0);
            }
            Dado.impostaSeme(i);
            if (prova.riuscita(CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma))) {
                riuscite++;
            }
        }
        return riuscite;
    }

    private static boolean haAggiunto(DannoRisultante risultato, TipoEffettoDiStato stato) {
        for (EffettoDiStatoMD effetto : risultato.getEffettiDiStatoDaAggiungere()) {
            if (effetto.getTipoEffettoDiStato() == stato) {
                return true;
            }
        }
        return false;
    }

    @Test
    void ilFattoreDegliEffettiESoloLaResistenza() {
        assertEquals(0.5d, CalcolatoreCombattimento.fattoreEffetti(difensore(TipoDanno.FUOCO, 0.5d), TipoDanno.FUOCO), 1e-9);
        assertEquals(1.0d, CalcolatoreCombattimento.fattoreEffetti(difensore(TipoDanno.FUOCO, 2.0d), TipoDanno.FUOCO), 1e-9);
        assertEquals(1.0d, CalcolatoreCombattimento.fattoreEffetti(difensore(TipoDanno.FUOCO, 0.5d), TipoDanno.GELO), 1e-9);
    }

    @Test
    void glistatiNativiDelDannoScattanoConLaProbabilitaRidotta() {
        // Un colpo di fuoco debole: la probabilità di bruciare di base è lontana dal 100%
        Arma fuoco = arma(TipoDanno.FUOCO, 3);
        int normale = quante(TipoDanno.FUOCO, 1.0d, TipoDanno.FUOCO, null, fuoco, r -> haAggiunto(r, TipoEffettoDiStato.BRUCIATO));
        int resistente = quante(TipoDanno.FUOCO, 0.5d, TipoDanno.FUOCO, null, fuoco, r -> haAggiunto(r, TipoEffettoDiStato.BRUCIATO));
        assertTrue(normale > PROVE / 10 && normale < PROVE * 8 / 10, "senza resistenza: " + normale);
        assertEquals(normale / 2.0d, resistente, PROVE * 0.04d, "con resistenza 0,5: " + resistente + " contro " + normale);
    }

    @Test
    void chiEDeboleNonPerdeEffetti() {
        // Con un moltiplicatore maggiore di 1 la probabilità non si riduce (anzi, il colpo fa più danno e quindi più stati)
        Arma fuoco = arma(TipoDanno.FUOCO, 3);
        int normale = quante(TipoDanno.FUOCO, 1.0d, TipoDanno.FUOCO, null, fuoco, r -> haAggiunto(r, TipoEffettoDiStato.BRUCIATO));
        int debole = quante(TipoDanno.FUOCO, 2.0d, TipoDanno.FUOCO, null, fuoco, r -> haAggiunto(r, TipoEffettoDiStato.BRUCIATO));
        assertTrue(debole >= normale, "debole " + debole + ", normale " + normale);
    }

    @Test
    void leReazioniConGliStatiScattanoConLaProbabilitaRidotta() {
        // Un BAGNATO colpito dal fulmine: Elettrocuzione, danno ×1,5. Chi resiste al fulmine la subisce meno spesso
        Arma fulmine = arma(TipoDanno.FULMINE, 3);
        Prova elettrocuzione = r -> r.getInterazioni().contains(TipoInterazioneConEffettiDiStato.ELETTROCUZIONE);
        assertEquals(PROVE, quante(TipoDanno.FULMINE, 1.0d, TipoDanno.FULMINE, TipoEffettoDiStato.BAGNATO, fulmine, elettrocuzione));
        int resistente = quante(TipoDanno.FULMINE, 0.5d, TipoDanno.FULMINE, TipoEffettoDiStato.BAGNATO, fulmine, elettrocuzione);
        assertEquals(PROVE / 2.0d, resistente, PROVE * 0.04d, "con resistenza 0,5: " + resistente);
    }

    @Test
    void seLaReazioneNonScattaNonCeNeppureIlSuoMoltiplicatore() {
        Arma fulmine = arma(TipoDanno.FULMINE, 40);
        Personaggio attaccante = FabbricaPersonaggi.crea(TipoPersonaggio.LADRO, 5);
        double sommaConReazione = 0;
        double sommaSenza = 0;
        int conReazione = 0;
        int senza = 0;
        for (int i = 1; i <= PROVE; i++) {
            Personaggio difensore = difensore(TipoDanno.FULMINE, 0.5d);
            difensore.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
            Dado.impostaSeme(i);
            DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, fulmine);
            if (risultato.getInterazioni().contains(TipoInterazioneConEffettiDiStato.ELETTROCUZIONE)) {
                sommaConReazione += risultato.getDanno();
                conReazione++;
            } else {
                sommaSenza += risultato.getDanno();
                senza++;
            }
        }
        assertTrue(conReazione > 0 && senza > 0, conReazione + " con la reazione, " + senza + " senza");
        // Con la reazione il danno è 1,5 volte quello senza (i critici raddoppiano entrambi)
        assertEquals(1.5d, (sommaConReazione / conReazione) / (sommaSenza / senza), 0.15d);
    }

    @Test
    void ilModificatoreDellArmaSulBersaglioSegueLaResistenza() {
        // Il modificatore sul bersaglio dell'arma (vedi Arma.getModificatoriSulBersaglio) ha la stessa probabilità ridotta
        Arma conModificatore = new Arma() {
            @Override public int getDanni() { return 3; }
            @Override public int getLivello() { return 5; }
            @Override public TipoDanno getTipoDanno() { return TipoDanno.GELO; }
            @Override public boolean isIncantata() { return false; }
            @Override public Collection<IncantamentoMD> getIncantamenti() { return Collections.emptyList(); }
            @Override public Collection<ModificatoreAttributoMD> getModificatoriSulBersaglio() {
                return Collections.singletonList(new ModificatoreAttributoMD(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0));
            }
        };
        Prova modificatore = r -> !r.getModificatoriDaAggiungere().isEmpty();
        // La probabilità del modificatore è (danno × 100 / FORZA del difensore) + INTELLIGENZA di chi colpisce; il danno, con la resistenza,
        // si dimezza anch'esso, quindi il rapporto fra le due prove è un mezzo solo se il primo termine è trascurabile: con un difensore
        // forte (FORZA 1000) lo è, e resta l'INTELLIGENZA dell'attaccante (fissata dal seme)
        int normale = quante(TipoDanno.GELO, 1.0d, 1000, TipoDanno.GELO, null, conModificatore, modificatore);
        int resistente = quante(TipoDanno.GELO, 0.5d, 1000, TipoDanno.GELO, null, conModificatore, modificatore);
        assertTrue(normale > PROVE / 20 && normale < PROVE * 8 / 10, "senza resistenza: " + normale);
        assertEquals(normale / 2.0d, resistente, PROVE * 0.02d, "con resistenza 0,5: " + resistente + " contro " + normale);
    }
}
