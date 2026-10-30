package com.threeamigos.foresta.missioni;

/**
 * Per i test di altri pacchetti: il passo di una missione è un dettaglio del pacchetto delle missioni, e qui lo si può guardare da fuori.
 */
public final class AccessoAiPassi {

    private AccessoAiPassi() {
    }

    /** Se il passo di quell'id, per quella missione, è concluso. */
    public static boolean isConcluso(MissioneAPassi missione, String id) {
        return missione.costruisciPasso(id).isConcluso();
    }

    /** Le azioni che il passo fa a ogni controllo, anche prima di concludersi. */
    public static void eseguiAOgniControllo(MissioneAPassi missione, String id) {
        missione.costruisciPasso(id).eseguiAzioniAOgniControllo();
    }
}
