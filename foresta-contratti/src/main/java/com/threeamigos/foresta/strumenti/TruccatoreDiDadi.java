package com.threeamigos.foresta.strumenti;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Chi può decidere al posto del caso il risultato di un lancio con contesto (vedi {@link Dado#impostaTruccatore}). Lo usa
 * la finestra di prova: se non c'è niente da decidere restituisce un risultato vuoto e il dado tira normalmente.
 * Può essere chiamato da qualunque thread.
 *
 * @author Stefano Reksten
 */
public interface TruccatoreDiDadi {

    /**
     * Il risultato voluto per un lancio fra min e max compresi, o niente per lasciarlo al caso. Un risultato fuori
     * dall'intervallo viene ignorato da {@link Dado}.
     */
    OptionalInt lancio(ContestoLancio contesto, int min, int max);

    /**
     * L'elemento voluto fra quelli ammessi, o niente per lasciare la scelta al caso. Un elemento non ammesso viene
     * ignorato da {@link Dado}.
     */
    <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi);

}
