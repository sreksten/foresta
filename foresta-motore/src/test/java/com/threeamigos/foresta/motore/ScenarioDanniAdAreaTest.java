package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.artefatti.Arma;
import com.threeamigos.foresta.artefatti.IncantamentoMD;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.danni.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le reazioni ad area (TipoInterazioneConEffettiDiStato.getDannoAdArea): l'esplosione di gas, il rigetto e l'elettrocuzione
 * investono anche i vicini del bersaglio nella fila del suo gruppo, con metà del danno grezzo del colpo, passando dalla loro
 * difesa e dalle loro reazioni, senza tiro per colpire e senza catene.
 */
class ScenarioDanniAdAreaTest {

    private static Arma arma(TipoDanno tipo) {
        return new Arma() {
            @Override public int getDanni() { return 20; }
            @Override public int getLivello() { return 5; }
            @Override public TipoDanno getTipoDanno() { return tipo; }
            @Override public boolean isIncantata() { return false; }
            @Override public Collection<IncantamentoMD> getIncantamenti() { return new ArrayList<>(); }
        };
    }

    private static List<Personaggio> goblin(int quanti) {
        List<Personaggio> fila = new ArrayList<>();
        for (int i = 0; i < quanti; i++) {
            fila.add(FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5));
        }
        return fila;
    }

    private static PartitaDiTest partita() {
        PartitaDiTest partita = PartitaDiTest.nuova(31);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
        return partita;
    }

    @Test
    void sonoInvestitiIVicinoInFilaEChiEFuoriCombattimentoNonSepara() {
        try (PartitaDiTest partita = partita()) {
            List<Personaggio> fila = goblin(5);
            assertEquals(Arrays.asList(fila.get(1), fila.get(3)),
                    CalcolatoreCombattimento.viciniInvestiti(fila, fila.get(2), TipoInterazioneConEffettiDiStato.ESPLOSIONE_DI_GAS));
            assertEquals(Arrays.asList(fila.get(1)),
                    CalcolatoreCombattimento.viciniInvestiti(fila, fila.get(0), TipoInterazioneConEffettiDiStato.RIGETTO));
            fila.get(3).subSalute(10000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
            assertEquals(Arrays.asList(fila.get(1), fila.get(4)),
                    CalcolatoreCombattimento.viciniInvestiti(fila, fila.get(2), TipoInterazioneConEffettiDiStato.ESPLOSIONE_DI_GAS));
        }
    }

    @Test
    void ilColpoSenzaViciniNonPortaReazioniAdArea() {
        try (PartitaDiTest partita = partita()) {
            List<Personaggio> fila = goblin(3);
            fila.forEach(GruppoAvversario.getIstanza()::aggiungiPersonaggio);
            Personaggio bersaglio = fila.get(1);
            bersaglio.addEffettoDiStato(TipoEffettoDiStato.BRUCIATO, 3, 0);
            Personaggio arsenio = partita.gruppo().getCapo();
            int[] saluteDiPrima = fila.stream().mapToInt(Personaggio::getSalute).toArray();

            DannoRisultante colpo = CalcolatoreCombattimento.calcolaDannoRisultante(arsenio, bersaglio, arma(TipoDanno.VELENO));
            assertEquals(1, colpo.getDanniAdArea().size());
            colpo.togliDanniAdArea();
            bersaglio.applicaRisultatoCombattimento(colpo);

            assertTrue(colpo.getDanniAdArea().isEmpty());
            assertEquals(saluteDiPrima[0], fila.get(0).getSalute());
            assertEquals(saluteDiPrima[2], fila.get(2).getSalute());
        }
    }

    @Test
    void lElettrocuzioneCorreNellaPozzaFinoAChiNonEBagnato() {
        try (PartitaDiTest partita = partita()) {
            List<Personaggio> fila = goblin(5);
            for (int i : new int[]{0, 2, 3, 4}) {
                fila.get(i).addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
            }
            // Il secondo non è bagnato e interrompe la pozza verso sinistra
            assertEquals(Arrays.asList(fila.get(3), fila.get(4)),
                    CalcolatoreCombattimento.viciniInvestiti(fila, fila.get(2), TipoInterazioneConEffettiDiStato.ELETTROCUZIONE));
        }
    }

    @Test
    void lEsplosioneDiGasBruciaIVicini() {
        try (PartitaDiTest partita = partita()) {
            List<Personaggio> fila = goblin(4);
            fila.forEach(GruppoAvversario.getIstanza()::aggiungiPersonaggio);
            Personaggio bersaglio = fila.get(1);
            bersaglio.addEffettoDiStato(TipoEffettoDiStato.BRUCIATO, 3, 0);
            Personaggio arsenio = partita.gruppo().getCapo();
            int[] saluteDiPrima = fila.stream().mapToInt(Personaggio::getSalute).toArray();

            DannoRisultante colpo = CalcolatoreCombattimento.calcolaDannoRisultante(arsenio, bersaglio, arma(TipoDanno.VELENO));
            assertTrue(colpo.getInterazioni().contains(TipoInterazioneConEffettiDiStato.ESPLOSIONE_DI_GAS));
            assertEquals(1, colpo.getDanniAdArea().size());
            assertEquals(TipoDanno.FUOCO, colpo.getDanniAdArea().get(0).getTipoDanno());
            bersaglio.applicaRisultatoCombattimento(colpo);

            assertTrue(fila.get(0).getSalute() < saluteDiPrima[0], "il vicino di sinistra");
            assertTrue(fila.get(2).getSalute() < saluteDiPrima[2], "il vicino di destra");
            assertEquals(saluteDiPrima[3], fila.get(3).getSalute(), "chi non è vicino non è investito");
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith("L'esplosione investe anche")), String.valueOf(partita.testi()));
            // L'avversario scelto resta il bersaglio, non l'ultimo vicino investito
            assertSame(bersaglio, StatoDelCombattimento.getIstanza().getUltimoAvversario(arsenio));
        }
    }

    @Test
    void ilColpoAiViciniPassaDalleLoroReazioniMaNonFaCatena() {
        try (PartitaDiTest partita = partita()) {
            Personaggio arsenio = partita.gruppo().getCapo();
            Personaggio vicino = FabbricaPersonaggi.crea(TipoPersonaggio.GOBLIN, 5);
            vicino.addEffettoDiStato(TipoEffettoDiStato.BAGNATO, 3, 0);
            DannoRisultante.DannoAdArea scarica = new DannoRisultante.DannoAdArea(TipoInterazioneConEffettiDiStato.ELETTROCUZIONE, 40.0d);

            DannoRisultante colpo = CalcolatoreCombattimento.calcolaDannoAdArea(arsenio, vicino, scarica);
            assertTrue(colpo.isColpoAdArea());
            assertEquals(TipoDanno.FULMINE, colpo.getTipoDanno());
            assertTrue(colpo.getInterazioni().contains(TipoInterazioneConEffettiDiStato.ELETTROCUZIONE), "il vicino bagnato è debole al fulmine");
            assertTrue(colpo.getDanniAdArea().isEmpty(), "il colpo ai vicini non porta altre reazioni ad area");
            double senzaReazioni = 40.0d * 100.0d / (100.0d + CalcolatoreCombattimento.difesaContro(vicino, TipoDanno.FULMINE));
            assertEquals(Math.floor(senzaReazioni * 1.5d), colpo.getDanno(), 1.0d);
        }
    }
}
