package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tattiche.TatticheBase;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * Una marcia di un gruppo contro i mostri comuni: una serie di locazioni, in ognuna (con una certa probabilità) uno scontro con un mostro scelto a caso e
 * in numero a caso fino al massimo per il gruppo, e a fine di ogni locazione il recupero di salute e magia del gioco (Automa, FINE_LOCAZIONE_2: il
 * recupero a fine locazione e un turno di effetti di stato). Chi cade resta caduto (niente resurrezione né riposo). Serve a vedere se i mostri consumano
 * davvero le risorse del gruppo o se il recupero fra una locazione e l'altra le rimette tutte a posto: la salute media all'inizio di ogni scontro, quanti
 * gruppi arrivano in fondo e quanti personaggi cadono. Dotazione di base, tattiche predefinite, niente pozioni.
 */
class MarciaDelGruppoTest {

    private static final int LOCAZIONI = 12;
    private static final int MARCE = 80;

    @Disabled("Da eseguire manualmente per vedere quanto consumano i mostri comuni fra una locazione e l'altra")
    @Test
    void stampaLaMarcia() {
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        StringBuilder sb = new StringBuilder();
        try {
            List<List<TipoPersonaggio>> gruppi = Arrays.asList(
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.ELFO, TipoPersonaggio.BARDO, TipoPersonaggio.SACERDOTE));
            sb.append(String.format("MARCIA %d locazioni, %d marce per cella. Salute del gruppo all'inizio di uno scontro (media), gruppi che arrivano in fondo, caduti medi%n",
                    LOCAZIONI, MARCE));
            for (int percentuale : new int[]{100, 50}) {
                for (List<TipoPersonaggio> gruppo : gruppi) {
                    for (int livello : new int[]{3, 6, 9}) {
                        sb.append(String.format("MARCIA scontro in %3d%% delle locazioni, gruppo di %d, livello %d: %s%n", percentuale, gruppo.size(), livello,
                                marcia(gruppo, livello, percentuale)));
                    }
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
        out.print(sb);
    }

    private static String marcia(List<TipoPersonaggio> classi, int livello, int percentualeDiScontri) {
        List<TipoPersonaggio> mostri = new ArrayList<>();
        for (TipoPersonaggio m : new TreeSet<>(TatticheBase.classiDeiMostri())) {
            if (!FabbricaPersonaggi.crea(m, 1).isParteConValoriMassimi()) {
                mostri.add(m);
            }
        }
        double saluteInizio = 0;
        int scontri = 0;
        int inFondo = 0;
        double caduti = 0;
        for (int marcia = 0; marcia < MARCE; marcia++) {
            ModelloDati.installa();
            ModelloDati.setIstanza(new ModelloDati());
            List<Personaggio> miei = new ArrayList<>();
            double massima = 0;
            for (TipoPersonaggio c : classi) {
                Personaggio p = FabbricaPersonaggi.crea(c, livello);
                EquipaggiamentoIniziale.equipaggia(p, livello);
                miei.add(p);
                massima += p.getSaluteMassima();
            }
            boolean capoVivo = true;
            for (int locazione = 0; locazione < LOCAZIONI && capoVivo; locazione++) {
                if (Dado.tira(100) <= percentualeDiScontri) {
                    TipoPersonaggio mostro = mostri.get(Dado.tiraAncheAUnaFaccia(mostri.size()) - 1);
                    FabbricaPersonaggi.crea(mostro, 1);
                    int massimo = Math.min(LocazioneBase.numeroMassimoDiMostri(livello, classi.size()), FabbricaPersonaggi.quantitaMassima(mostro));
                    int quanti = Dado.tiraAncheAUnaFaccia(massimo);
                    double salute = 0;
                    for (Personaggio p : miei) {
                        salute += p.isVivo() ? p.getSalute() : 0;
                    }
                    saluteInizio += 100.0 * salute / massima;
                    scontri++;
                    List<Personaggio> avversari = new ArrayList<>();
                    for (int i = 0; i < quanti; i++) {
                        avversari.add(FabbricaPersonaggi.crea(mostro, livello));
                    }
                    List<Personaggio> vivi = new ArrayList<>();
                    for (Personaggio p : miei) {
                        if (p.isVivo()) {
                            vivi.add(p);
                        }
                    }
                    RoundASimulato round = new RoundASimulato(vivi, avversari);
                    RoundASimulato.Decisore decisore = (chi, r) -> r.agisciConLaTattica(chi, TatticheBase.deiGiocabili(chi.getClasse()));
                    for (int turno = 0; turno < 40 && round.esito() == RoundASimulato.Esito.IN_CORSO; turno++) {
                        round.eseguiRound(decisore);
                    }
                    capoVivo = miei.get(0).isVivo();
                    if (round.esito() == RoundASimulato.Esito.IN_CORSO) {
                        // Lo scontro non finisce: la marcia si ferma come un gruppo che fugge
                        capoVivo = false;
                    }
                }
                // Fine locazione: recupero di salute e magia e un turno di effetti di stato, come l'Automa
                for (Personaggio p : miei) {
                    if (p.isVivo()) {
                        p.addSalute(p.getRigenerazioneSalute());
                        p.addMagia(p.getRigenerazioneMagia());
                        p.trascorriUnTurno();
                    }
                }
            }
            if (capoVivo) {
                inFondo++;
            }
            for (Personaggio p : miei) {
                if (!p.isVivo()) {
                    caduti++;
                }
            }
        }
        return String.format("salute all'inizio dello scontro %3.0f%%, in fondo %3d%%, caduti %3.1f", scontri == 0 ? 0 : saluteInizio / scontri,
                100 * inFondo / MARCE, caduti / MARCE);
    }
}
