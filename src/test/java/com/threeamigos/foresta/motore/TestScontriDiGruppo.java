package com.threeamigos.foresta.motore;

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
import java.util.Random;

/**
 * Scontri di un gruppo di personaggi giocanti (ognuno con la sua prima dotazione tipica, del suo livello) contro boss
 * e mostri comuni, con le regole di LocazioneBase:
 * <ul>
 * <li>a ogni round il gruppo fa una sola azione: un attacco in mischia del combattente scelto (eseguiSingoloAttacco),
 * un dardo arcano o una pergamena della scorta del gruppo (lanciaDardoArcano, CHI_FORMULA), o una pozione;</li>
 * <li>in mischia risponde il primo avversario vivo, sul combattente (Personaggio.rispondiInMischia: con le armi o, se
 * sa la magia, con un incantesimo); se gli avversari vivi
 * sono più dei personaggi vivi, uno "si disimpegna e attacca" il gruppo (Personaggio.attacca(Gruppo): prima il Mago,
 * poi l'Elfo, altrimenti uno a caso);</li>
 * <li>dopo un incantesimo, un dardo o una pozione risponde il prossimo avversario, su chi ha agito
 * (rispostaAvversaria, con Personaggio.attacca: i mostri che sanno la magia possono lanciare incantesimi);</li>
 * <li>a fine round passa un turno di effetti di stato per tutti; se muore il capo (il primo del gruppo) è sconfitta.</li>
 * </ul>
 * Il giocatore simulato sceglie a ogni round l'azione con il danno atteso più alto (probabilità di colpire per
 * danno); combatte in mischia chi rende di più, ma non il capo se è sotto il 40% della salute e c'è qualcun altro;
 * beve una pozione chi è sotto il 30%, se ce ne sono.
 */
public class TestScontriDiGruppo {

    private static final int TURNI_MASSIMI = 200;

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    private final Random random = new Random(20261005L);

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
                    out.printf("%n== livello %d, %d pergamene di fuoco e %d pozioni: vittorie / turni / caduti / salute residua del gruppo%n",
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
                            riga.append(String.format(" | %5.1f%% %5.1ft %3.1fc %4.0f%%%s", 100.0d * vittorie / iterazioni,
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
        Personaggio capo = gruppo.get(0);
        List<Personaggio> avversari = new ArrayList<>();
        for (int i = 0; i < quantita; i++) {
            avversari.add(FabbricaPersonaggi.crea(classeAvversari, livello));
        }
        int[] scorta = {pergamene, pozioni};
        int prossimoAttaccante = 0;
        Personaggio combattente = null;
        Esito esito = new Esito();
        for (int turno = 1; turno <= TURNI_MASSIMI; turno++) {
            esito.turni = turno;
            List<Personaggio> vivi = vivi(gruppo);
            Personaggio primoAvversario = primoVivo(avversari);

            Personaggio chiBeve = scorta[1] > 0 ? vivi.stream()
                    .filter(p -> p.getSalute() * 100 < p.getSaluteMassima() * 30).findFirst().orElse(null) : null;
            Personaggio formulanteDardo = migliorDardo(vivi, primoAvversario);
            Personaggio formulantePergamena = scorta[0] > 0 ? migliorPergamena(vivi, avversari) : null;
            if (combattente == null || combattente.isFuoriCombattimento()
                    || (combattente == capo && capo.getSalute() * 100 < capo.getSaluteMassima() * 40 && vivi.size() > 1)) {
                combattente = migliorCombattente(vivi, capo, primoAvversario);
            }
            double conMischia = atteseMischia(combattente, primoAvversario);
            double conDardo = formulanteDardo == null ? 0 : atteseDardo(formulanteDardo, primoAvversario);
            double conPergamena = formulantePergamena == null ? 0 : attesePergamena(formulantePergamena, avversari);

            if (chiBeve != null) {
                scorta[1]--;
                chiBeve.addSalute(Costanti.RECUPERO_DA_POZIONE_SALUTE);
                prossimoAttaccante = risposta(avversari, prossimoAttaccante, chiBeve, gruppo);
            } else if (conPergamena > conMischia && conPergamena >= conDardo) {
                scorta[0]--;
                lanciaPergamena(formulantePergamena, avversari);
                prossimoAttaccante = risposta(avversari, prossimoAttaccante, formulantePergamena, gruppo);
            } else if (conDardo > conMischia) {
                DardoArcano dardo = new DardoArcano(formulanteDardo);
                colpisci(formulanteDardo, primoAvversario, dardo, 1.0d);
                formulanteDardo.subMagia(dardo.getCostoLancio());
                prossimoAttaccante = risposta(avversari, prossimoAttaccante, formulanteDardo, gruppo);
            } else {
                mischia(combattente, avversari, gruppo);
            }

            for (Personaggio p : vivi(gruppo)) {
                p.applicaDanniDaEffettiDiStato();
                p.riduciEffettiDiStato();
            }
            for (Personaggio a : vivi(avversari)) {
                a.applicaDanniDaEffettiDiStato();
                a.riduciEffettiDiStato();
            }
            if (!capo.isVivo() || vivi(gruppo).isEmpty()) {
                break;
            }
            if (vivi(avversari).isEmpty()) {
                esito.vittoria = true;
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
     * eseguiSingoloAttacco: il combattente attacca il primo avversario vivo (la seconda arma passa al prossimo se la
     * prima l'ha ucciso), poi quello risponde sul combattente; se gli avversari vivi sono più dei personaggi vivi,
     * uno si disimpegna e attacca il gruppo
     */
    private void mischia(Personaggio combattente, List<Personaggio> avversari, List<Personaggio> gruppo) {
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
        if (bersaglio == null) {
            return;
        }
        // Come nel gioco: con le armi o, se sa la magia, con un incantesimo
        bersaglio.rispondiInMischia(combattente);
        if (vivi(avversari).size() > vivi(gruppo).size()) {
            Personaggio chiSiDisimpegna = primoVivo(avversari);
            Personaggio preso = bersaglioDelGruppo(gruppo);
            if (chiSiDisimpegna != null && preso != null) {
                chiSiDisimpegna.attacca(preso);
            }
        }
    }

    /**
     * rispostaAvversaria: il prossimo avversario (a turno) attacca chi ha agito, o il gruppo se quello è caduto
     */
    private int risposta(List<Personaggio> avversari, int prossimo, Personaggio chiHaAgito, List<Personaggio> gruppo) {
        List<Personaggio> vivi = vivi(avversari);
        if (vivi.isEmpty()) {
            return prossimo;
        }
        Personaggio attaccante = vivi.get(prossimo % vivi.size());
        Personaggio bersaglio = chiHaAgito.isFuoriCombattimento() ? bersaglioDelGruppo(gruppo) : chiHaAgito;
        if (bersaglio != null) {
            attaccante.attacca(bersaglio);
        }
        return prossimo + 1;
    }

    /**
     * Personaggio.attacca(Gruppo): prima il Mago, poi l'Elfo, altrimenti uno a caso
     */
    private Personaggio bersaglioDelGruppo(List<Personaggio> gruppo) {
        List<Personaggio> vivi = vivi(gruppo);
        if (vivi.isEmpty()) {
            return null;
        }
        for (TipoPersonaggio preferito : Arrays.asList(TipoPersonaggio.MAGO, TipoPersonaggio.ELFO)) {
            for (Personaggio p : vivi) {
                if (p.getClasse() == preferito) {
                    return p;
                }
            }
        }
        return vivi.get(random.nextInt(vivi.size()));
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

    private static Personaggio migliorCombattente(List<Personaggio> vivi, Personaggio capo, Personaggio bersaglio) {
        Personaggio migliore = null;
        double rendimento = -1;
        for (Personaggio p : vivi) {
            boolean capoFerito = p == capo && vivi.size() > 1 && capo.getSalute() * 100 < capo.getSaluteMassima() * 40;
            double r = capoFerito ? -0.5 : atteseMischia(p, bersaglio);
            if (r > rendimento) {
                rendimento = r;
                migliore = p;
            }
        }
        return migliore;
    }

    private static Personaggio migliorDardo(List<Personaggio> vivi, Personaggio bersaglio) {
        Personaggio migliore = null;
        double rendimento = 0;
        for (Personaggio p : vivi) {
            if (DardoArcano.puoLanciarlo(p)) {
                double r = atteseDardo(p, bersaglio);
                if (r > rendimento) {
                    rendimento = r;
                    migliore = p;
                }
            }
        }
        return migliore;
    }

    private static Personaggio migliorPergamena(List<Personaggio> vivi, List<Personaggio> avversari) {
        Personaggio migliore = null;
        double rendimento = 0;
        for (Personaggio p : vivi) {
            if (p.getMagia() >= pergamena(p).getCostoLancio()) {
                double r = attesePergamena(p, avversari);
                if (r > rendimento) {
                    rendimento = r;
                    migliore = p;
                }
            }
        }
        return migliore;
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
