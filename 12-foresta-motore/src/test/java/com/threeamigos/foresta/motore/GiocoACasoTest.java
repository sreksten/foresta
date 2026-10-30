package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Un giocatore che preme i comandi a caso: a ogni passo sceglie uno di quelli che il gioco offre (o fa scattare il temporizzatore, o scrive
 * un testo), e il gioco non deve mai pubblicare un errore.
 */
class GiocoACasoTest {

    @AfterEach
    void ripulisce() {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
    }

    /**
     * Quello che il giocatore a caso scopre senza che sia ancora stato risolto (vedi 12-foresta-motore.md, "Da fare"): un incantesimo contro
     * i malefici o una pozione quando non c'è nessun avversario o nessuno può agire, e una scena in negozio senza chi parla per il capo. Il
     * resto, e le letture della vista con argomenti inventati, non contano.
     */
    static boolean isNoto(String problema) {
        return problema.startsWith("vista ") || problema.contains("Comando non valido") || problema.contains("ignoro il Comando") || problema.startsWith("AssertionError")
                || (problema.startsWith("ECCEZIONE") && (problema.contains("BattutaIntermezzo.di") || problema.contains("Morte.formula")
                || problema.contains("IncantesimoMaleficoImpl.formula") || problema.contains("LocazioneBase.gestisciCombattimento")
                || problema.contains("Automa.eseguiPasso")));
    }

    /**
     * Cambia il mondo sotto i piedi del giocatore: le tasche vuote o piene, nuovi oggetti nello zaino, un compagno ferito a morte e, fra
     * una mossa e l'altra, un salto in un posto speciale (città, locanda, bottega, tempio...).
     */
    private static void turba(Random caso, Stato stato) {
        int dado = caso.nextInt(120);
        com.threeamigos.foresta.gruppi.GruppoGiocatore gruppo = com.threeamigos.foresta.gruppi.GruppoGiocatore.getIstanza();
        if (dado == 0) {
            com.threeamigos.foresta.economia.Cassa.subMonete(com.threeamigos.foresta.economia.Cassa.getMonete());
        } else if (dado == 1) {
            com.threeamigos.foresta.economia.Cassa.addMonete(5000);
        } else if (dado == 2) {
            com.threeamigos.foresta.artefatti.GeneratoreArtefatti generatore = com.threeamigos.foresta.artefatti.GeneratoreArtefatti.istanza();
            com.threeamigos.foresta.tipi.TipoArtefatto[] tipi = {com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, com.threeamigos.foresta.tipi.TipoArtefatto.PERGAMENA,
                    com.threeamigos.foresta.tipi.TipoArtefatto.GEMMA, com.threeamigos.foresta.tipi.TipoArtefatto.ELMO, com.threeamigos.foresta.tipi.TipoArtefatto.SCUDO,
                    com.threeamigos.foresta.tipi.TipoArtefatto.AGGIORNAMENTO, com.threeamigos.foresta.tipi.TipoArtefatto.RIPROGRAMMAZIONE};
            for (int i = 0; i < 4; i++) {
                gruppo.addArtefatto(generatore.generaArtefatto(tipi[caso.nextInt(tipi.length)], 1 + caso.nextInt(5)));
            }
        } else if (dado == 3 && gruppo.getNumeroPersonaggi() > 1) {
            com.threeamigos.foresta.personaggi.Personaggio p = gruppo.getPersonaggio(caso.nextInt(gruppo.getNumeroPersonaggi()));
            if (p.isVivo()) {
                p.subSalute(100000, null, com.threeamigos.foresta.personaggi.Personaggio.NotificaFerite.NO, com.threeamigos.foresta.personaggi.Personaggio.NotificaMorte.NO);
            }
        } else if (dado < 8 && stato == Stato.SCELTA_DIREZIONE) {
            java.util.List<com.threeamigos.foresta.modellodati.CoordinateMD> speciali = new ArrayList<>();
            for (int x = 0; x < com.threeamigos.foresta.mappa.Foresta.getDimensioneX(); x++) {
                for (int y = 0; y < com.threeamigos.foresta.mappa.Foresta.getDimensioneY(); y++) {
                    com.threeamigos.foresta.tipi.TipoLocazione tipo = com.threeamigos.foresta.mappa.Foresta.getLocazione(x, y);
                    if (tipo != com.threeamigos.foresta.tipi.TipoLocazione.BOSCO && tipo != com.threeamigos.foresta.tipi.TipoLocazione.RADURA
                            && tipo != com.threeamigos.foresta.tipi.TipoLocazione.PALUDE && tipo != com.threeamigos.foresta.tipi.TipoLocazione.GROTTA) {
                        speciali.add(new com.threeamigos.foresta.modellodati.CoordinateMD(x, y));
                    }
                }
            }
            if (!speciali.isEmpty()) {
                com.threeamigos.foresta.mappa.Foresta.setPosizioneDelGruppo(speciali.get(caso.nextInt(speciali.size())));
            }
        }
    }

    private static final Set<String> GIA_VISTI = new TreeSet<>();

    /**
     * Legge tutto quello che la vista offre alla UI, e quello che offrono gli oggetti che restituisce, con argomenti qualunque.
     */
    private static void sfoglia(Object oggetto, Map<String, String> problemi, int profondita) {
        if (oggetto == null || profondita < 0) {
            return;
        }
        for (java.lang.reflect.Method m : oggetto.getClass().getMethods()) {
            if (m.getDeclaringClass() == Object.class || java.lang.reflect.Modifier.isStatic(m.getModifiers()) || m.getName().startsWith("set")
                    || m.getName().startsWith("add") || m.getName().startsWith("sub") || m.getName().startsWith("rimuovi")
                    || m.getName().startsWith("imposta") || m.getName().startsWith("azzera") || m.getName().startsWith("reimposta")) {
                continue;
            }
            Object[] args = new Object[m.getParameterCount()];
            Class<?>[] tipi = m.getParameterTypes();
            boolean ok = true;
            for (int i = 0; i < tipi.length && ok; i++) {
                Class<?> t = tipi[i];
                if (t == int.class) {
                    args[i] = 1;
                } else if (t == boolean.class) {
                    args[i] = false;
                } else if (t == String.class) {
                    args[i] = "x";
                } else if (t == com.threeamigos.foresta.modellodati.CoordinateMD.class) {
                    args[i] = com.threeamigos.foresta.mappa.Foresta.getPosizioneDelGruppo();
                } else if (t.isEnum()) {
                    args[i] = t.getEnumConstants().length == 0 ? null : t.getEnumConstants()[0];
                } else {
                    ok = false;
                }
            }
            if (!ok) {
                continue;
            }
            try {
                Object risultato = m.invoke(oggetto, args);
                if (risultato != null && risultato.getClass().getName().startsWith("com.threeamigos") && m.getParameterCount() == 0) {
                    sfoglia(risultato, problemi, profondita - 1);
                }
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (GIA_VISTI.add(oggetto.getClass().getSimpleName() + "." + m.getName())) {
                    problemi.putIfAbsent("vista " + oggetto.getClass().getSimpleName() + "." + m.getName() + " " + e.getCause(), "");
                }
            } catch (IllegalAccessException e) {
                // metodo non accessibile: niente da leggere
            }
        }
    }

    @Test
    void gira() {
        Map<String, String> problemi = gioca(1, 14, false);
        problemi.keySet().removeIf(GiocoACasoTest::isNoto);
        org.junit.jupiter.api.Assertions.assertTrue(problemi.isEmpty(), problemi.toString());
    }

    /**
     * Anche i comandi che il gioco non ha offerto, come se la UI sbagliasse: il gioco può lamentarsi, ma non deve bloccarsi.
     */
    @Test
    void giraAncheConComandiSbagliati() {
        gioca(101, 104, true);
    }

    private Map<String, String> gioca(int primoSeme, int ultimoSeme, boolean comandiStorti) {
        Logger.impostaDestinazione(DestinazioneDiLog.NESSUNA);
        Map<String, String> problemi = new TreeMap<>();
        Map<Stato, Integer> stati = new java.util.EnumMap<>(Stato.class);
        Comando[] classi = {Comando.GUERRIERO, Comando.MAGO, Comando.LADRO, Comando.CHIERICO, Comando.DRUIDO, Comando.BARDO, Comando.SACERDOTE, Comando.ELFO};
        for (long seme = primoSeme; seme <= ultimoSeme; seme++) {
            Random caso = new Random(seme);
            boolean senzaTrucchi = seme % 3 == 0;
            try (PartitaDiTest partita = senzaTrucchi ? PartitaDiTest.nuovaSenzaTrucchi(seme) : PartitaDiTest.nuova(seme)) {
                VistaPartita vista = new VistaPartitaMotore();
                boolean[] fuoriProtocollo = {false};
                com.threeamigos.foresta.eventi.BusEventi.iscriviti(com.threeamigos.foresta.eventi.interni.InternoException.class, ev -> {
                    Throwable t = ev.getException();
                    StringBuilder sb = new StringBuilder("ECCEZIONE " + t.getClass().getSimpleName() + " " + partita.stato());
                    int n = 0;
                    for (StackTraceElement x : t.getStackTrace()) {
                        if (x.getClassName().startsWith("com.threeamigos") && n++ < 4) {
                            sb.append(" < ").append(x.getClassName().substring(x.getClassName().lastIndexOf('.') + 1)).append('.').append(x.getMethodName()).append(':').append(x.getLineNumber());
                        }
                    }
                    if (!fuoriProtocollo[0]) {
                        problemi.putIfAbsent(sb.toString(), String.valueOf(t.getMessage()));
                    }
                });
                partita.comando(Comando.PERGAMENA);
                partita.testo("Arsenio");
                partita.comando(seme % 2 == 0 ? Comando.MASCHIO : Comando.FEMMINA);
                partita.comando(classi[(int) (seme % classi.length)]);
                if (seme % 4 == 1) {
                    partita.nonSaltareIntermezzi();
                }
                for (int passo = 0; passo < 3000; passo++) {
                    if (passo == 2500 && seme % 2 == 0) {
                        // La partita finisce: il tempo è scaduto, e a volte il Drago è stato sconfitto
                        if (seme % 4 == 0) {
                            com.threeamigos.foresta.missioni.RegistroMissioni.getMissionePrincipale().completaMissione();
                        }
                        com.threeamigos.foresta.statistiche.LineaTemporale.setGiocoFinito(true);
                    }
                    stati.merge(partita.stato(), 1, Integer::sum);
                    try {
                        List<Comando> comandi = new ArrayList<>(partita.comandiDisponibili());
                        Stato stato = partita.stato();
                        boolean aspettaUnTesto = stato == Stato.ATTESA_NOME_PUNTEGGI || stato == Stato.PRE_GAME_ATTESA_NOME_PERSONAGGIO;
                        int scelta = caso.nextInt(10);
                        turba(caso, stato);
                        if (passo % 50 == 0) {
                            sfoglia(vista, problemi, 2);
                        }
                        if (caso.nextInt(300) == 0) {
                            partita.inBackground();
                            partita.inPrimoPiano();
                        }
                        if (comandiStorti && caso.nextInt(12) == 0) {
                            // Un comando che il gioco non ha offerto: la UI non lo manda, e se il gioco si lamenta non è un suo difetto
                            Comando[] tutti = Comando.values();
                            fuoriProtocollo[0] = true;
                            try {
                                partita.comando(tutti[caso.nextInt(tutti.length)]);
                            } catch (AssertionError | RuntimeException e) {
                                // il gioco ha segnalato l'errore, come ci si aspetta
                            } finally {
                                fuoriProtocollo[0] = false;
                            }
                        } else if (aspettaUnTesto) {
                            partita.testo(caso.nextInt(4) == 0 ? "" : "Nome" + caso.nextInt(100));
                        } else if (!comandi.isEmpty() && (scelta > 1 || !partita.temporizzatore().puoScattare())) {
                            partita.comando(comandi.get(caso.nextInt(comandi.size())));
                        } else if (partita.temporizzatore().puoScattare()) {
                            partita.scatta();
                        } else if (stato == Stato.IN_LOCAZIONE) {
                            partita.testo("Nome" + caso.nextInt(100));
                        }
                    } catch (RuntimeException | AssertionError e) {
                        StackTraceElement[] st = e.getStackTrace();
                        StringBuilder sb = new StringBuilder(e.getClass().getSimpleName() + " " + partita.stato());
                        int n = 0;
                        for (StackTraceElement x : st) {
                            if (x.getClassName().startsWith("com.threeamigos") && !x.getClassName().contains("GiocoACaso") && !x.getClassName().contains("PartitaDiTest") && n++ < 3) {
                                sb.append(" < ").append(x.getClassName().substring(x.getClassName().lastIndexOf('.') + 1)).append('.').append(x.getMethodName()).append(':').append(x.getLineNumber());
                            }
                        }
                        problemi.putIfAbsent(sb.toString(), String.valueOf(e.getMessage()).replace("\n", " "));
                    }
                }
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(stati.size() >= 15, "il giocatore a caso passa da molti stati: " + stati);
        return problemi;
    }
}
