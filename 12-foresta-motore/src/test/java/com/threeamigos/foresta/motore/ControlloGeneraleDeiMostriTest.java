package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.EquipaggiamentoIniziale;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.TatticheBase;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import java.io.*;
import java.util.*;

/**
 * Controllo generale di quanto fanno soffrire i mostri comuni (non i boss), con la dotazione di base del gioco (EquipaggiamentoIniziale), le
 * tattiche predefinite dei personaggi (senza pozioni) e il round a coppie (RoundASimulato):
 * <ul>
 * <li>A: quanta salute e magia recupera ogni classe a fine locazione, in percentuale del massimo;</li>
 * <li>B: ogni classe giocabile da sola contro ogni mostro comune, a livello 3, 6 e 9: vittorie e salute persa;</li>
 * <li>C: un gruppo da quattro e uno da cinque contro il massimo di mostri di ogni classe: vittorie, round, salute persa dal gruppo, caduti.</li>
 * </ul>
 * Si stampa tutto con il prefisso «OUT » (il resto dell'uscita è zittito).
 */
class ControlloGeneraleDeiMostriTest {

    static class Esito { boolean vittoria; int round; double persa; int caduti; }

    static final TipoPersonaggio[] CLASSI = {TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.ELFO,
            TipoPersonaggio.SACERDOTE, TipoPersonaggio.BARDO, TipoPersonaggio.CHIERICO, TipoPersonaggio.DRUIDO};

    /**
     * Il livello del mondo da cui nascono i pezzi della dotazione: quello della prova, o (variabile d'ambiente DOTAZIONE) uno fisso, per
     * vedere quanto conta un equipaggiamento mai rinnovato
     */
    static int livelloDelleDotazioni(int livello) {
        String fisso = System.getenv("DOTAZIONE");
        return fisso == null ? livello : Math.min(livello, Integer.parseInt(fisso));
    }

    static Esito combatti(List<TipoPersonaggio> mie, List<TipoPersonaggio> loro, int livello) {
        ModelloDati.installa();
        ModelloDati.setIstanza(new ModelloDati());
        List<Personaggio> miei = new ArrayList<>();
        for (TipoPersonaggio c : mie) {
            Personaggio p = FabbricaPersonaggi.crea(c, livello);
            EquipaggiamentoIniziale.equipaggia(p, livelloDelleDotazioni(livello));
            miei.add(p);
        }
        List<Personaggio> avversari = new ArrayList<>();
        for (TipoPersonaggio c : loro) {
            avversari.add(FabbricaPersonaggi.crea(c, livello));
        }
        double massima = 0;
        for (Personaggio p : miei) massima += p.getSaluteMassima();
        RoundASimulato r = new RoundASimulato(miei, avversari);
        RoundASimulato.Decisore d = (chi, rr) -> rr.agisciConLaTattica(chi, TatticheBase.deiGiocabili(chi.getClasse()));
        Esito e = new Esito();
        int turno = 0;
        while (r.esito() == RoundASimulato.Esito.IN_CORSO && turno < 40) {
            r.eseguiRound(d);
            turno++;
        }
        e.vittoria = r.esito() == RoundASimulato.Esito.VITTORIA;
        e.round = turno;
        double salute = 0;
        for (Personaggio p : miei) {
            if (p.isVivo()) salute += p.getSalute(); else e.caduti++;
        }
        e.persa = 100.0 * (massima - salute) / massima;
        return e;
    }

    static List<TipoPersonaggio> comuni() {
        List<TipoPersonaggio> l = new ArrayList<>();
        for (TipoPersonaggio m : new TreeSet<>(TatticheBase.classiDeiMostri())) {
            if (!FabbricaPersonaggi.crea(m, 1).isParteConValoriMassimi()) l.add(m);
        }
        return l;
    }

    @org.junit.jupiter.api.Disabled("Da eseguire manualmente per vedere quanto soffrono i personaggi contro i mostri comuni")
    @org.junit.jupiter.api.Test
    void controllo() {
        PrintStream out = System.out;
        System.setOut(new PrintStream(new OutputStream() { public void write(int b) { } }));
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        StringBuilder sb = new StringBuilder();
        try {
            List<TipoPersonaggio> mostri = comuni();
            sb.append("OUT mostri comuni: ").append(mostri).append('\n');
            // A. recupero a fine locazione
            sb.append("OUT == A. Recupero a fine locazione, in % della salute e della magia massima (come lo calcola il gioco)\n");
            for (int livello : new int[]{3, 6, 9}) {
                sb.append(String.format("OUT liv %d:", livello));
                for (TipoPersonaggio c : CLASSI) {
                    ModelloDati.installa();
                    ModelloDati.setIstanza(new ModelloDati());
                    double s = 0, m = 0;
                    int n = 40;
                    for (int i = 0; i < n; i++) {
                        Personaggio p = FabbricaPersonaggi.crea(c, livello);
                        EquipaggiamentoIniziale.equipaggia(p, livelloDelleDotazioni(livello));
                        s += 100.0 * p.getRigenerazioneSalute() / p.getSaluteMassima();
                        m += p.getMagiaMassima() == 0 ? 0 : 100.0 * p.getRigenerazioneMagia() / p.getMagiaMassima();
                    }
                    sb.append(String.format("  %s salute %2.0f%% magia %2.0f%% |", c.name().substring(0, 4), s / n, m / n));
                }
                sb.append('\n');
            }
            // B. 1 contro 1
            sb.append("OUT == B. Uno contro uno (dotazione di base, tattica predefinita, niente pozioni): vittorie% / salute persa% (media)\n");
            for (int livello : new int[]{3, 6, 9}) {
                sb.append(String.format("OUT liv %d %-12s", livello, ""));
                for (TipoPersonaggio c : CLASSI) sb.append(String.format(" %-9s", c.name().substring(0, Math.min(8, c.name().length()))));
                sb.append('\n');
                for (TipoPersonaggio m : mostri) {
                    sb.append(String.format("OUT %-17s", m));
                    for (TipoPersonaggio c : CLASSI) {
                        int v = 0; double persa = 0; int n = 30;
                        for (int i = 0; i < n; i++) {
                            Esito e = combatti(Arrays.asList(c), Arrays.asList(m), livello);
                            if (e.vittoria) v++;
                            persa += e.persa;
                        }
                        sb.append(String.format(" %3d/%-5.0f", 100 * v / n, persa / n));
                    }
                    sb.append('\n');
                }
            }
            // C. gruppi
            sb.append("OUT == C. Gruppi contro il massimo di mostri della stessa classe: vittorie% / round / salute persa del gruppo% / caduti\n");
            List<List<TipoPersonaggio>> gruppi = Arrays.asList(
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.SACERDOTE),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.ELFO, TipoPersonaggio.BARDO, TipoPersonaggio.SACERDOTE));
            for (int livello : new int[]{3, 6, 9}) {
                sb.append(String.format("OUT liv %d %-12s | G4 (GUE LAD MAG SAC)         | G5 (GUE LAD ELF BAR SAC)%n", livello, ""));
                for (TipoPersonaggio m : mostri) {
                    FabbricaPersonaggi.crea(m, 1);
                    int max = FabbricaPersonaggi.quantitaMassima(m);
                    sb.append(String.format("OUT %-17s", m));
                    for (List<TipoPersonaggio> g : gruppi) {
                        int q = Math.min(LocazioneBase.numeroMassimoDiMostri(livello, g.size()), max);
                        List<TipoPersonaggio> loro = new ArrayList<>();
                        for (int i = 0; i < q; i++) loro.add(m);
                        int v = 0, caduti = 0; double rounds = 0, persa = 0; int n = 40;
                        for (int i = 0; i < n; i++) {
                            Esito e = combatti(g, loro, livello);
                            if (e.vittoria) v++;
                            rounds += e.round; persa += e.persa; caduti += e.caduti;
                        }
                        sb.append(String.format(" | x%d %3d%% %4.1fr %3.0f%% %3.1fc", q, 100 * v / n, rounds / n, persa / n, caduti / (double) n));
                    }
                    sb.append('\n');
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
        out.print(sb);
    }
}
