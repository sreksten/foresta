package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoPassaggioInosservato;
import com.threeamigos.foresta.eventi.interni.InternoPastoConsumatoInLocanda;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.intermezzi.Locande;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoOggettoRaccolto;
import com.threeamigos.foresta.eventi.interni.InternoRichiestaAperturaFinestraCombattimento;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.oggetti.OggettoMissione;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.oggetti.Bottino;
import com.threeamigos.foresta.intermezzi.RegistroIntermezzi;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;

/**
 * Un giocatore che non capisce niente: gira per la mappa a caso, fa passare le ore, accetta e risponde a caso, sconfigge avversari a caso e
 * a ogni passo chiede alle missioni tutto quello che la locazione chiederebbe loro. Serve a far passare le missioni da tutti i loro passi.
 */
public final class GiocatoreAutomatico {

    private final Random caso;
    private final Map<String, String> errori = new TreeMap<>();
    private boolean completa;

    public GiocatoreAutomatico(long seme) {
        this.caso = new Random(seme);
        RegistroMissioni.setFornitoreLocazioneCompleta(() -> completa);
        BusEventi.iscriviti(com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo.class, e -> testi.merge(e.getMessaggio(), 1, Integer::sum));
    }

    private final Map<String, Integer> testi = new TreeMap<>();

    public Map<String, Integer> getTesti() {
        return testi;
    }

    private final Map<String, Integer> esiti = new TreeMap<>();

    public Map<String, Integer> getEsiti() {
        return esiti;
    }

    private void registraEsiti() {
        for (Missione m : RegistroMissioni.getTutteLeMissioni()) {
            List<Missione> albero = new ArrayList<>();
            tutte(albero, m);
            for (Missione x : albero) {
                String stato = x.isCompleta() ? "completa" : x.isFallita() ? "fallita" : x.isAttiva() ? "attiva" : "ferma";
                String passo = x instanceof MissioneAPassi && stato.equals("attiva") ? "@" + ((MissioneAPassi) x).getPassoCorrente() : "";
                esiti.merge(x.getClass().getSimpleName() + " " + stato + passo, 1, Integer::sum);
            }
        }
    }

    public Map<String, String> getErrori() {
        return errori;
    }

    private void provaPer(Missione m, String fase, Runnable azione) {
        boolean prima = m.isFallita();
        prova(m.getClass().getSimpleName() + " " + fase, azione);
        if (!prima && m.isFallita()) {
            esiti.merge("FALLITA da " + m.getClass().getSimpleName() + " " + fase + (m instanceof MissioneAPassi ? " @" + ((MissioneAPassi) m).getPassoCorrente() : ""), 1, Integer::sum);
        }
    }

    private final java.util.Set<String> giaFallite = new java.util.HashSet<>();

    private void controllaFallite(String dove) {
        for (Missione m : RegistroMissioni.getTutteLeMissioni()) {
            List<Missione> albero = new ArrayList<>();
            tutte(albero, m);
            for (Missione x : albero) {
                if (x.isFallita() && giaFallite.add(x.getId())) {
                    esiti.merge("FALLITA durante " + dove.replaceAll("^\\w+ ", x.getClass().getSimpleName() + " "), 1, Integer::sum);
                }
            }
        }
    }

    private void prova(String dove, Runnable azione) {
        try {
            azione.run();
            controllaFallite(dove);
        } catch (Throwable t) {
            errori.putIfAbsent(dove + ": " + descrivi(t), String.valueOf(t.getMessage()));
        }
    }

    /** L'eccezione e i primi punti del nostro codice da cui è passata. */
    static String descrivi(Throwable t) {
        StringBuilder sb = new StringBuilder(t.getClass().getSimpleName());
        int n = 0;
        for (StackTraceElement e : t.getStackTrace()) {
            if (e.getClassName().startsWith("com.threeamigos") && !e.getClassName().contains("GiocatoreAutomatico") && n++ < 3) {
                sb.append(" < ").append(e.getClassName().substring(e.getClassName().lastIndexOf('.') + 1)).append('.').append(e.getMethodName())
                        .append(':').append(e.getLineNumber());
            }
        }
        return sb.toString();
    }

    private CoordinateMD posizioneACaso() {
        if (caso.nextInt(10) < 6) {
            List<CoordinateMD> mete = new ArrayList<>(RegistroMissioni.getLocazioniDaSegnalare());
            for (Missione m : RegistroMissioni.getTutteLeMissioni()) {
                CoordinateMD occupata = RegistroMissioni.getLocazioneOccupata(m);
                if (occupata != null) {
                    mete.add(occupata);
                }
            }
            if (!mete.isEmpty()) {
                return mete.get(caso.nextInt(mete.size()));
            }
        }
        int scelta = caso.nextInt(10);
        if (scelta < 2) {
            TipoLocazione[] citta = {TipoLocazione.CITTA_NYENA, TipoLocazione.CITTA_MALGAARD, TipoLocazione.CITTA_RUUNA, TipoLocazione.CITTA_FLEENA};
            CoordinateMD c = Foresta.getCoordinateLocazioneUnica(citta[caso.nextInt(citta.length)]);
            if (c != null) {
                return c;
            }
        }
        return new CoordinateMD(caso.nextInt(MondoDiProva.LATO), caso.nextInt(MondoDiProva.LATO));
    }

    private static void tutte(List<Missione> dove, Missione m) {
        dove.add(m);
        for (Missione figlia : new ArrayList<>(m.getMissioniSecondarie())) {
            tutte(dove, figlia);
        }
    }

    public void gioca(int tick) {
        try {
            giocaDavvero(tick);
        } finally {
            registraEsiti();
        }
        percorriTutto();
    }

    /**
     * Alla fine della partita fa percorrere a ogni missione i suoi passi, anche quelli a cui non è arrivata.
     */
    public void percorriTutto() {
        for (Missione m : new ArrayList<>(RegistroMissioni.getTutteLeMissioni())) {
            List<Missione> albero = new ArrayList<>();
            tutte(albero, m);
            for (Missione x : albero) {
                if (x instanceof MissioneAPassi) {
                    PercorsoForzato.percorri((MissioneAPassi) x, (dove, t) -> errori.putIfAbsent(dove + ": " + descrivi(t), String.valueOf(t.getMessage())));
                }
            }
        }
    }

    private void giocaDavvero(int tick) {
        for (int t = 0; t < tick; t++) {
            CoordinateMD qui = posizioneACaso();
            prova("posizione", () -> Foresta.setPosizioneDelGruppo(qui));
            completa = caso.nextInt(10) < 6;
            prova("mondo", () -> {
                // Chi c'è nella locazione, che cosa si è fatto in locanda, quanto si ha in tasca
                GruppoAvversario nemici = GruppoAvversario.getIstanza();
                nemici.reimposta();
                int quanti = caso.nextInt(4);
                TipoPersonaggio[] classi = TipoPersonaggio.values();
                for (int i = 0; i < quanti; i++) {
                    TipoPersonaggio classe = caso.nextInt(3) == 0 ? TipoPersonaggio.EREMITA : classi[caso.nextInt(classi.length)];
                    try {
                        nemici.aggiungiPersonaggio(FabbricaPersonaggi.crea(classe, 1 + caso.nextInt(10)));
                    } catch (RuntimeException nonSiCrea) {
                        // alcune classi non sono mostri
                    }
                }
                TipoLocazione tipo = Foresta.getLocazione(qui);
                if ((tipo == TipoLocazione.LOCANDA || tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) && caso.nextInt(3) == 0) {
                    Foresta.getLocazioneMD(qui).aggiungiProprieta(Locande.LOCANDA_VISITATA, "SI");
                }
                if ((tipo == TipoLocazione.LOCANDA || tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA) && caso.nextBoolean()) {
                    String visite = Foresta.getLocazioneMD(qui).ottieniProprieta(Locande.LOCANDA_VISITE);
                    Foresta.getLocazioneMD(qui).aggiungiProprieta(Locande.LOCANDA_VISITE, String.valueOf(visite == null ? 1 : Integer.parseInt(visite) + 1));
                }
                if (caso.nextInt(6) == 0) {
                    BusEventi.pubblica(new InternoPastoConsumatoInLocanda());
                }
                if (caso.nextInt(6) == 0) {
                    BusEventi.pubblica(new InternoPassaggioInosservato());
                }
                MondoDiProva.cassa.setMonete(5000);
            });
            if (caso.nextInt(3) > 0) {
                Foresta.setLocazioneVisitata(qui, true);
            }
            if (caso.nextInt(3) == 0) {
                prova("tempo", () -> LineaTemporale.aggiungiOre(1 + caso.nextInt(3)));
            }
            List<Missione> tutteLeMissioni = new ArrayList<>(RegistroMissioni.getTutteLeMissioni());
            for (Missione m : tutteLeMissioni) {
                List<Missione> albero = new ArrayList<>();
                tutte(albero, m);
                for (Missione x : albero) {
                    muovi(x, qui);
                }
            }
            if (caso.nextInt(25) == 0) {
                prova("salvataggio", MondoDiProva::salvaERicarica);
            }
            if (caso.nextInt(8) == 0) {
                prova("avvia", () -> {
                    MissionePredefinita[] classi = MissionePredefinita.values();
                    MissionePredefinita classe = classi[caso.nextInt(classi.length)];
                    if (!classe.isPredefinita() && !RegistroMissioni.isPartita(classe)) {
                        RegistroMissioni.avvia(classe);
                    }
                });
            }
            prova("registro", () -> {
                RegistroMissioni.getMissioniAttive();
                RegistroMissioni.getMissioniCompletate();
                RegistroMissioni.getMissioniFallite();
                RegistroMissioni.getMissionePrincipale();
                for (Missione x : RegistroMissioni.getTutteLeMissioni()) {
                    RegistroMissioni.getMissione(x.getId());
                }
                RegistroMissioni.getOggettoMissione(qui, Foresta.getLocazione(qui), caso.nextBoolean());
                RegistroMissioni.getIncontroMissione(qui);
                RegistroMissioni.getOndateSuccessiveMissione(qui);
                RegistroMissioni.getRicordo(qui);
                RegistroMissioni.isLocazioneSenzaOggetti(qui);
                RegistroMissioni.isDaSopprimere(qui);
                RegistroMissioni.getLocazioniDaSegnalare();
                RegistroMissioni.getNomiMissioniDaSegnalare(qui);
            });
            prova("eventi", () -> {
                TipoPersonaggio[] classi = TipoPersonaggio.values();
                BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(null, null));
                BusEventi.pubblica(new InternoAvversarioSconfitto(classi[caso.nextInt(classi.length)]));
                TipoBottino[] bottini = TipoBottino.values();
                BusEventi.pubblica(new InternoOggettoRaccolto(bottini[caso.nextInt(bottini.length)], 1 + caso.nextInt(3), null, false));
            });
            for (MomentoIntermezzo momento : MomentoIntermezzo.values()) {
                prova("intermezzo " + momento, () -> {
                    RegistroIntermezzi.nuovoMomento();
                    Intermezzo i = RegistroIntermezzi.getProssimoIntermezzo(momento);
                    if (i != null) {
                        i.getPagine();
                        RegistroIntermezzi.segnaScattato(i);
                    }
                });
            }
        }
    }

    private void muovi(Missione m, CoordinateMD qui) {
        String nome = m.getClass().getSimpleName();
        if (m.isCompleta() || m.isFallita()) {
            prova(nome + " descrizione dopo la fine", () -> {
                if (m.isCompleta()) {
                    // La UI le elenca fra le completate, con la loro descrizione
                    m.getNome();
                    m.getDescrizione();
                }
            });
            prova(nome + " dopo la fine", () -> {
                m.getOggettoInLocazione(qui, Foresta.getLocazione(qui), false);
            });
            return;
        }
        if (!m.isAttiva() && !(m instanceof MissioneAPassi) && caso.nextInt(4) == 0) {
            prova(nome + " attiva", m::attivaMissione);
        }
        provaPer(m, "pre", m::controllaPreLocazione);
        provaPer(m, "in", m::controllaInLocazione);
        provaPer(m, "post", m::controllaPostLocazione);
        if (caso.nextInt(3) == 0) {
            provaPer(m, "accampamento", m::controllaAccampamento);
        }
        prova(nome + " query", () -> {
            Optional<Bottino> oggetto = m.getOggettoInLocazione(qui, Foresta.getLocazione(qui), caso.nextBoolean());
            if (oggetto.isPresent() && oggetto.get() instanceof OggettoMissione && m instanceof MissioneAPassi) {
                OggettoMissione om = (OggettoMissione) oggetto.get();
                ((MissioneAPassi) m).oggettoDiMissioneRaccolto(om.getChiave(), om.getQuantita());
            }
            Optional<List<Personaggio>> incontro = m.getIncontroInLocazione(qui);
            if (incontro.isPresent() && caso.nextInt(4) > 0) {
                BusEventi.pubblica(new InternoRichiestaAperturaFinestraCombattimento(null, null));
                for (Personaggio nemico : incontro.get()) {
                    BusEventi.pubblica(new InternoAvversarioSconfitto(nemico.getClasse()));
                }
                // Finito il combattimento, la locazione è conclusa e le missioni guardano com'è andata
                completa = true;
                m.controllaPostLocazione();
            }
            m.isLocazioneSenzaOggetti(qui);
            m.getOndateSuccessiveInLocazione(qui);
            if (m.isAttiva()) {
                m.getNome();
                try {
                    m.getDescrizione();
                } catch (RuntimeException e) {
                    esiti.merge("DESCRIZIONE rotta " + nome + " passo=" + (m instanceof MissioneAPassi ? ((MissioneAPassi) m).getPassoCorrente() : "-")
                            + " completa=" + m.isCompleta() + " fallita=" + m.isFallita(), 1, Integer::sum);
                    throw e;
                }
                m.getRicordoDellaLocazione();
            }
        });
        if (m instanceof MissioneAPassi) {
            MissioneAPassi p = (MissioneAPassi) m;
            if (caso.nextInt(12) == 0) {
                // Chi si scorta può cadere strada facendo
                prova(nome + " scortato muore", () -> p.getScortato().ifPresent(
                        s -> s.subSalute(100000, null, Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO)));
            }
            for (MomentoControllo momento : MomentoControllo.values()) {
                provaPer(m, "domanda", () -> {
                    Passo domanda = p.getDomandaDaPorre(momento);
                    if (domanda != null) {
                        List<String> risposte = domanda.getRispostePossibili();
                        p.rispondi(caso.nextInt(6) > 0 ? risposte.get(0) : risposte.get(caso.nextInt(risposte.size())));
                    }
                });
            }
            prova(nome + " intermezzo in attesa", () -> {
                for (String id : new ArrayList<>(p.getPassiConIntermezzoInAttesa())) {
                    p.segnaIntermezzoPassoMostrato(id);
                }
            });
        }
        if (m.isAttiva() && caso.nextInt(20000) == 0) {
            prova(nome + " fallisce", m::fallisciMissione);
        }
    }
}
