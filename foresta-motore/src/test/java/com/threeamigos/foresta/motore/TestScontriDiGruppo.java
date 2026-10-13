package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.modellodati.ModelloDati;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.incantesimi.DardoArcano;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.incantesimi.IncantesimoMalefico;
import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.locazioni.LocazioneBase;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.PortataIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.combattimento.DannoRisultante;
import com.threeamigos.foresta.combattimento.FaseDiAttacco;

/**
 * Scontri di un gruppo di personaggi giocanti (ognuno con la sua prima dotazione tipica, del suo livello) contro boss
 * e mostri comuni, con il round a coppie del gioco (vedi {@link RoundASimulato} e tattiche_di_combattimento_automatiche.md §3):
 * <ul>
 * <li>i due gruppi agiscono a turno secondo il piano del round (max(n, m) coppie: un mio personaggio e poi un avversario), e a
 * ogni posto agisce il prossimo vivo di quel gruppo, in round-robin: <b>tutti i miei agiscono a ogni round</b>, uno solo ciascuno;</li>
 * <li>non c'è una "risposta": l'avversario che tocca attacca il gruppo scegliendo il bersaglio da sé (prima il Mago, poi il
 * Sacerdote, l'Elfo, il Bardo, altrimenti a caso), con le armi o, se sa la magia, con un incantesimo (la tattica base dei mostri, RegistroTattiche);
 * un compagno solo contro più avversari li subisce tutti;</li>
 * <li>a fine round passa un turno di effetti di stato per tutti; se muore il capo (il primo del gruppo) è sconfitta.</li>
 * </ul>
 * Il giocatore simulato fa scegliere a ogni personaggio, quando tocca a lui, l'azione con il danno atteso più alto (probabilità di
 * colpire per danno): un attacco in mischia, un dardo arcano o una pergamena della scorta del gruppo; beve una pozione se è sotto il
 * 30% della salute e ce ne sono (bere è l'azione del personaggio). Come le tattiche (che oggi non ci sono ancora): nessuno resta fuori
 * dal turno, e il capo ferito combatte come gli altri.
 * <p>
 * I numeri di "t" sono <b>round</b> (n azioni dei miei e m degli avversari ciascuno), non più scambi di colpi: non si confrontano con quelli
 * delle versioni precedenti, che simulavano la risposta e il disimpegno.
 */
public class TestScontriDiGruppo {

    private static final int TURNI_MASSIMI = 200;

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    static class Esito {
        boolean vittoria;
        int turni;
        int caduti;
        double saluteResidua;
    }

    @Disabled("Da eseguire manualmente: gruppi contro boss e mostri comuni")
    @Test
    void gruppiControBoss() {
        PrintStream out = System.out;
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        System.setOut(NULL_STREAM);
        try {
            List<List<TipoPersonaggio>> gruppi = Arrays.asList(
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.BARDO, TipoPersonaggio.ELFO),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.ELFO,
                            TipoPersonaggio.MAGO),
                    Arrays.asList(TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.ELFO,
                            TipoPersonaggio.BARDO, TipoPersonaggio.MAGO));
            List<TipoPersonaggio> boss = Arrays.asList(TipoPersonaggio.IDRA, TipoPersonaggio.MINOTAURO_GIGANTE,
                    TipoPersonaggio.STREGA, TipoPersonaggio.LICH, TipoPersonaggio.DRAGO);
            int[][] scorte = {{0, 0}, {3, 2}};  // {pergamene di fuoco, pozioni di salute}
            int iterazioni = 300;
            for (int livello : new int[]{3, 5, 8}) {
                for (int[] scorta : scorte) {
                    out.printf("%n== livello %d, %d pergamene di fuoco e %d pozioni: vittorie / round / caduti / salute residua del gruppo%n",
                            livello, scorta[0], scorta[1]);
                    StringBuilder testata = new StringBuilder(String.format("%-28s", "avversari"));
                    for (List<TipoPersonaggio> gruppo : gruppi) {
                        testata.append(String.format(" | %-26s", sigla(gruppo)));
                    }
                    out.println(testata);
                    List<Object[]> avversari = new ArrayList<>();
                    for (TipoPersonaggio b : boss) {
                        avversari.add(new Object[]{b, 1});
                    }
                    avversari.add(new Object[]{TipoPersonaggio.TROLL, -1});
                    avversari.add(new Object[]{TipoPersonaggio.GOBLIN, -1});
                    for (Object[] avversario : avversari) {
                        TipoPersonaggio classe = (TipoPersonaggio) avversario[0];
                        int quantitaFissa = (Integer) avversario[1];
                        StringBuilder riga = new StringBuilder(String.format("%-28s",
                                classe + (quantitaFissa > 0 ? "" : " (massimo per il gruppo)")));
                        for (List<TipoPersonaggio> gruppo : gruppi) {
                            int quantita = quantitaFissa > 0 ? quantitaFissa
                                    : Math.min(LocazioneBase.numeroMassimoDiMostri(livello, gruppo.size()), quantitaMassima(classe));
                            int vittorie = 0;
                            double turni = 0;
                            double caduti = 0;
                            double salute = 0;
                            for (int i = 0; i < iterazioni; i++) {
                                Esito esito = scontro(gruppo, classe, quantita, livello, scorta[0], scorta[1]);
                                if (esito.vittoria) {
                                    vittorie++;
                                    salute += esito.saluteResidua;
                                }
                                turni += esito.turni;
                                caduti += esito.caduti;
                            }
                            riga.append(String.format(" | %5.1f%% %5.1fr %3.1fc %4.0f%%%s", 100.0d * vittorie / iterazioni,
                                    turni / iterazioni, caduti / iterazioni, vittorie == 0 ? 0 : 100.0d * salute / vittorie,
                                    quantitaFissa > 0 ? "   " : String.format(" x%d", quantita)));
                        }
                        out.println(riga);
                    }
                }
            }
        } finally {
            System.setOut(out);
            BusEventi.azzera();
        }
    }

    private static String sigla(List<TipoPersonaggio> gruppo) {
        StringBuilder sb = new StringBuilder();
        for (TipoPersonaggio classe : gruppo) {
            if (sb.length() > 0) {
                sb.append('+');
            }
            sb.append(classe.name(), 0, 3);
        }
        return sb.toString();
    }

    Esito scontro(List<TipoPersonaggio> classi, TipoPersonaggio classeAvversari, int quantita, int livello,
                  int pergamene, int pozioni) {
        ModelloDati.setIstanza(new ModelloDati());
        List<Personaggio> gruppo = new ArrayList<>();
        for (TipoPersonaggio classe : classi) {
            Personaggio pg = FabbricaPersonaggi.crea(classe, livello);
            Equipaggiamento.tipiciPer(classe).get(0).equipaggia(pg);
            gruppo.add(pg);
        }
        List<Personaggio> avversari = new ArrayList<>();
        for (int i = 0; i < quantita; i++) {
            avversari.add(FabbricaPersonaggi.crea(classeAvversari, livello));
        }
        int[] scorta = {pergamene, pozioni};
        Esito esito = new Esito();
        RoundASimulato round = new RoundASimulato(gruppo, avversari);
        RoundASimulato.Decisore giocatore = (chi, r) -> agisce(chi, r.primoAvversarioVivo(), avversari, scorta);
        for (int turno = 1; turno <= TURNI_MASSIMI; turno++) {
            esito.turni = turno;
            RoundASimulato.Esito fine = round.eseguiRound(giocatore);
            if (fine == RoundASimulato.Esito.VITTORIA) {
                esito.vittoria = true;
                break;
            }
            if (fine == RoundASimulato.Esito.SCONFITTA) {
                break;
            }
        }
        double salute = 0;
        double massima = 0;
        for (Personaggio p : gruppo) {
            if (!p.isVivo()) {
                esito.caduti++;
            } else {
                salute += p.getSalute();
            }
            massima += p.getSaluteMassima();
        }
        esito.saluteResidua = salute / massima;
        return esito;
    }

    /**
     * L'azione di un mio personaggio quando il piano tocca a lui: beve se è sotto il 30% della salute e ci sono pozioni, altrimenti
     * sceglie fra mischia, dardo arcano e pergamena (della scorta del gruppo, se ha la magia per lanciarla) quella che rende di più
     */
    private void agisce(Personaggio chi, Personaggio bersaglio, List<Personaggio> avversari, int[] scorta) {
        if (bersaglio == null) {
            return;
        }
        if (scorta[1] > 0 && chi.getSalute() * 100 < chi.getSaluteMassima() * 30) {
            scorta[1]--;
            chi.addSalute(Costanti.RECUPERO_DA_POZIONE_SALUTE);
            return;
        }
        double conMischia = atteseMischia(chi, bersaglio);
        double conDardo = DardoArcano.puoLanciarlo(chi) ? atteseDardo(chi, bersaglio) : 0;
        boolean puoPergamena = scorta[0] > 0 && chi.getMagia() >= pergamena(chi).getCostoLancio();
        double conPergamena = puoPergamena ? attesePergamena(chi, avversari) : 0;
        if (conPergamena > conMischia && conPergamena >= conDardo) {
            scorta[0]--;
            lanciaPergamena(chi, avversari);
        } else if (conDardo > conMischia) {
            DardoArcano dardo = new DardoArcano(chi);
            colpisci(chi, bersaglio, dardo, 1.0d);
            chi.subMagia(dardo.getCostoLancio());
        } else {
            mischia(chi, avversari);
        }
    }

    /**
     * attaccaConLeArmi: il personaggio attacca il primo avversario vivo (la seconda arma passa al prossimo se la prima l'ha ucciso)
     */
    private void mischia(Personaggio combattente, List<Personaggio> avversari) {
        Personaggio bersaglio = primoVivo(avversari);
        for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(combattente)) {
            if (bersaglio == null) {
                return;
            }
            colpisci(combattente, bersaglio, fase.getArma(), fase.getFattore());
            if (bersaglio.isFuoriCombattimento()) {
                bersaglio = primoVivo(avversari);
            }
        }
    }

    private static void colpisci(Personaggio attaccante, Personaggio difensore, Arma arma, double fattore) {
        if (CalcolatoreCombattimento.colpisce(attaccante, difensore, arma.getTipoDanno().getSuperTipo())) {
            DannoRisultante risultato = CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma, fattore);
            difensore.applicaRisultatoCombattimento(risultato);
        }
    }

    private static void lanciaPergamena(Personaggio formulante, List<Personaggio> avversari) {
        IncantesimoMalefico incantesimo = pergamena(formulante);
        for (Personaggio bersaglio : bersagliDellaPergamena(formulante, incantesimo, avversari)) {
            if (!bersaglio.isImmuneAIncantesimo(incantesimo.getClasse())) {
                colpisci(formulante, bersaglio, incantesimo, 1.0d);
            }
        }
        formulante.subMagia(incantesimo.getCostoLancio());
    }

    private static IncantesimoMalefico pergamena(Personaggio formulante) {
        return (IncantesimoMalefico) FabbricaIncantesimi.crea(ClasseIncantesimo.FUOCO, formulante.getLivello());
    }

    private static List<Personaggio> bersagliDellaPergamena(Personaggio formulante, IncantesimoMalefico incantesimo,
                                                           List<Personaggio> avversari) {
        List<Personaggio> bersagli = new ArrayList<>(vivi(avversari));
        PortataIncantesimo portata = incantesimo.getClasse().getPortata();
        if (portata == PortataIncantesimo.MULTIPLO && bersagli.size() > formulante.getBersagli()) {
            return bersagli.subList(0, formulante.getBersagli());
        }
        if (portata != PortataIncantesimo.MULTIPLO && portata != PortataIncantesimo.GRUPPO && !bersagli.isEmpty()) {
            return bersagli.subList(0, 1);
        }
        return bersagli;
    }

    private static double attesa(Personaggio attaccante, Personaggio difensore, Arma arma, double fattore) {
        return CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore, arma.getTipoDanno().getSuperTipo())
                / 100.0d * CalcolatoreCombattimento.calcolaDannoRisultante(attaccante, difensore, arma, fattore).getDanno();
    }

    private static double atteseMischia(Personaggio combattente, Personaggio bersaglio) {
        double totale = 0;
        for (FaseDiAttacco fase : CalcolatoreCombattimento.fasiDiAttacco(combattente)) {
            totale += attesa(combattente, bersaglio, fase.getArma(), fase.getFattore());
        }
        return totale;
    }

    private static double atteseDardo(Personaggio formulante, Personaggio bersaglio) {
        return attesa(formulante, bersaglio, new DardoArcano(formulante), 1.0d);
    }

    private static double attesePergamena(Personaggio formulante, List<Personaggio> avversari) {
        IncantesimoMalefico incantesimo = pergamena(formulante);
        double totale = 0;
        for (Personaggio bersaglio : bersagliDellaPergamena(formulante, incantesimo, avversari)) {
            if (!bersaglio.isImmuneAIncantesimo(incantesimo.getClasse())) {
                totale += attesa(formulante, bersaglio, incantesimo, 1.0d);
            }
        }
        return totale;
    }

    private static List<Personaggio> vivi(List<Personaggio> personaggi) {
        List<Personaggio> vivi = new ArrayList<>();
        for (Personaggio p : personaggi) {
            if (!p.isFuoriCombattimento()) {
                vivi.add(p);
            }
        }
        return vivi;
    }

    private static Personaggio primoVivo(List<Personaggio> personaggi) {
        for (Personaggio p : personaggi) {
            if (!p.isFuoriCombattimento()) {
                return p;
            }
        }
        return null;
    }

    private static int quantitaMassima(TipoPersonaggio classe) {
        FabbricaPersonaggi.crea(classe, 1);
        return FabbricaPersonaggi.quantitaMassima(classe);
    }
}
