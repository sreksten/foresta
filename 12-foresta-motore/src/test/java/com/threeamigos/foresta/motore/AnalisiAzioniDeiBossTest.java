package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.TatticheBase;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Che cosa fa un boss turno per turno contro un gruppo di partenza (ognuno con la sua prima dotazione tipica e la tattica predefinita della
 * sua classe): per ogni livello, quante volte sceglie ogni azione e, del Silenzio, quante volte va a vuoto (il bersaglio resta non
 * silenziato). Serve a vedere se un boss spende i turni in azioni che non fanno danno: il Drago, che è nella fascia di astuzia più alta,
 * lancia il Silenzio nella gran parte dei turni (vedi todo.md, «Ritarare la difficoltà dei boss dopo il Silenzio corretto»). Usa il round a
 * coppie del gioco (RoundASimulato) e le tattiche vere (EsecutoreTattiche).
 */
class AnalisiAzioniDeiBossTest {

    private static final int SCONTRI = 30;
    private static final int ROUND_MASSIMI = 12;

    @Disabled("Da eseguire manualmente per vedere che cosa fanno i boss")
    @Test
    void stampaLeAzioniDeiBoss() {
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        try {
            for (TipoPersonaggio boss : new TipoPersonaggio[] {TipoPersonaggio.DRAGO, TipoPersonaggio.LICH, TipoPersonaggio.STREGA, TipoPersonaggio.IDRA,
                    TipoPersonaggio.MINOTAURO_GIGANTE}) {
                for (int livello : new int[] {3, 6, 10}) {
                    stampa(out, boss, livello);
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    private static void stampa(PrintStream out, TipoPersonaggio classeBoss, int livello) {
        List<TipoPersonaggio> classi = Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE);
        Map<String, Integer> azioni = new LinkedHashMap<>();
        int[] silenzi = new int[2];
        int totale = 0;
        for (int scontro = 0; scontro < SCONTRI; scontro++) {
            ModelloDati.installa();
            ModelloDati.setIstanza(new ModelloDati());
            List<Personaggio> miei = new ArrayList<>();
            for (TipoPersonaggio classe : classi) {
                Personaggio personaggio = FabbricaPersonaggi.crea(classe, livello);
                Equipaggiamento.tipiciPer(classe).get(0).equipaggia(personaggio);
                miei.add(personaggio);
            }
            RoundASimulato round = new RoundASimulato(miei, new ArrayList<>(Arrays.asList(FabbricaPersonaggi.crea(classeBoss, livello))));
            round.osservatoreDegliAvversari = (chi, decisione) -> {
                String azione = decisione == null ? "NIENTE" : decisione.getTipoEseguito() + (decisione.isRipiego() ? " (ripiego)" : "");
                azioni.merge(azione, 1, Integer::sum);
                if (decisione != null && decisione.getTipoEseguito() == TipoAzione.SILENZIO) {
                    silenzi[0]++;
                    if (!decisione.getBersaglio().hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO)) {
                        silenzi[1]++;
                    }
                }
            };
            RoundASimulato.Decisore decisore = (chi, r) -> r.agisciConLaTattica(chi, TatticheBase.deiGiocabili(chi.getClasse()));
            for (int turno = 0; turno < ROUND_MASSIMI && round.esito() == RoundASimulato.Esito.IN_CORSO; turno++) {
                round.eseguiRound(decisore);
            }
        }
        for (int n : azioni.values()) {
            totale += n;
        }
        out.printf("%n== %s liv %d contro %s (%d scontri, %d azioni)%n", classeBoss, livello, classi, SCONTRI, totale);
        for (Map.Entry<String, Integer> azione : azioni.entrySet()) {
            out.printf("   %-30s %4d  (%2d%%)%n", azione.getKey(), azione.getValue(), 100 * azione.getValue() / Math.max(1, totale));
        }
        if (silenzi[0] > 0) {
            out.printf("   Silenzi lanciati %d, andati a vuoto %d%n", silenzi[0], silenzi[1]);
        }
    }
}
