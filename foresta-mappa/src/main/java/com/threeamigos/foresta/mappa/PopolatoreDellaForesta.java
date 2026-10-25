package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.testi.ProduttoreDiLocandeCasuale;

/**
 * Chi popola la Foresta mentre {@link GeneratoreDellaForesta} la costruisce: la mappa costruisce le caselle e i loro nomi, e a ogni casella
 * importante dice a chi la abita che è nata, perché ci metta quello che le serve (i residenti di una città, il magazzino di un negozio,
 * i personaggi nelle locande, gli artefatti nei templi). Quello che mette non lo sa la mappa.
 */
public interface PopolatoreDellaForesta {

    /**
     * Scrive nella casella i dati di una locanda (nome, locandiere, recensione e dialogo): il suo contenuto non è della mappa.
     */
    void impostaDatiLocanda(LocazioneMD locazione, ProduttoreDiLocandeCasuale.DatiLocanda datiLocanda);

    /**
     * Una città è nata in quelle coordinate (con i dati della sua locanda già scritti).
     */
    void cittaCostruita(TipoLocazione citta, CoordinateMD coordinate);

    /**
     * Tutte le città sono nate: prima delle locande.
     */
    void dopoLeCitta();

    /**
     * Se c'è ancora un personaggio da mettere in una locanda; in tal caso lo prende da parte, e {@link #locandaCostruita} ce lo mette.
     */
    boolean prelevaPersonaggio();

    /**
     * La locanda del personaggio preso da parte con {@link #prelevaPersonaggio()} è nata in quelle coordinate.
     */
    void locandaCostruita(CoordinateMD coordinate);

    /**
     * Se c'è ancora un artefatto da mettere in un tempio; in tal caso lo prende da parte, e {@link #tempioCostruito} ce lo mette.
     */
    boolean prelevaArtefatto();

    /**
     * Il tempio dell'artefatto preso da parte con {@link #prelevaArtefatto()} è nato in quelle coordinate.
     */
    void tempioCostruito(CoordinateMD coordinate);
}
