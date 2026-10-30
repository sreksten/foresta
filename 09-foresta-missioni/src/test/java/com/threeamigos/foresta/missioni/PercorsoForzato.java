package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Percorre il grafo dei passi di una missione a passi senza aspettare che le condizioni si avverino: per ogni passo raggiungibile ne
 * valuta la condizione, ne esegue le azioni (con ogni risposta possibile se pone una domanda), ne costruisce l'intermezzo e segue il
 * passo successivo. Serve a far passare dal codice di ogni passo anche quando il gioco non ci arriverebbe.
 */
final class PercorsoForzato {

    private PercorsoForzato() {
    }

    static void percorri(MissioneAPassi m, BiConsumer<String, Throwable> segnalaErrore) {
        try {
            Foresta.setPosizioneDelGruppo(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA));
            if (!m.isAttiva() && !m.isCompleta() && !m.isFallita()) {
                m.attivaMissione();
            }
        } catch (Throwable t) {
            segnalaErrore.accept("percorso " + m.getClass().getSimpleName() + " attiva", t);
        }
        Set<String> visti = new HashSet<>();
        Deque<String> coda = new ArrayDeque<>();
        coda.add(m.getPassoCorrente());
        coda.add(m.passoIniziale());
        int limite = 80;
        while (!coda.isEmpty() && limite-- > 0) {
            String id = coda.pop();
            if (Passo.FINE.equals(id) || !visti.add(id)) {
                continue;
            }
            List<String> risposte = new ArrayList<>();
            risposte.add(null);
            try {
                m.aggiungiProprieta("PASSO_CORRENTE", id);
                Passo prova = m.costruisciPasso(id);
                if (prova.isDomanda()) {
                    risposte.clear();
                    risposte.addAll(prova.getRispostePossibili());
                }
            } catch (Throwable t) {
                segnalaErrore.accept("percorso " + m.getClass().getSimpleName() + " costruisci " + id, t);
                continue;
            }
            for (String risposta : risposte) {
                try {
                    m.aggiungiProprieta("PASSO_CORRENTE", id);
                    if (risposta != null) {
                        m.rispondi(risposta);
                    }
                    Passo p = m.costruisciPasso(id);
                    p.isConcluso();
                    if (p.isFallito()) {
                        p.getTestoFallimento();
                    }
                    p.eseguiAzioniAOgniControllo();
                    p.getLuogoDaSegnalare();
                    p.getOggettiDaSeminare();
                    for (CoordinateMD c : new CoordinateMD[] {Foresta.getPosizioneDelGruppo(), Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_NYENA),
                            new CoordinateMD(0, 0)}) {
                        p.getIncontroIn(c);
                    }
                    p.getRispostePossibili();
                    p.getOpzioni();
                    p.getDomanda();
                    p.isConferma();
                    p.eseguiAzione();
                    if (p.haIntermezzo()) {
                        p.getPagine();
                    }
                    coda.add(p.getProssimoPasso());
                } catch (Throwable t) {
                    segnalaErrore.accept("percorso " + m.getClass().getSimpleName() + " passo " + id + (risposta == null ? "" : " risposta " + risposta), t);
                }
            }
        }
    }
}
