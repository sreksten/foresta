/**
 * I tipi di base del gioco: gli enumerati che nominano le cose (personaggi, locazioni, oggetti, incantesimi,
 * missioni...) e i comandi del giocatore. Non dipende da nessun altro modulo, e tutti gli altri dipendono da lui.
 * <p>
 * Gli enumerati sono <b>identificativi puri</b>: dicono "che cosa" e portano al più dati fissi (un nome da mostrare,
 * una descrizione, un moltiplicatore), ma non costruiscono niente. La costruzione sta nelle fabbriche del motore
 * (per esempio le fabbriche di personaggi, di locazioni e di incantesimi), e i dati che cambiano durante la partita
 * stanno nel modello dati.
 * <p>
 * Qualche punto di partenza:
 * <ul>
 * <li>{@link com.threeamigos.foresta.tipi.TipoPersonaggio} e {@link com.threeamigos.foresta.tipi.CategoriaPersonaggio}:
 * le classi di personaggio e i generi cui appartengono, con le loro debolezze e resistenze;</li>
 * <li>{@link com.threeamigos.foresta.danni.tipi.TipoDanno} e {@link com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato}: i danni e
 * gli stati alterati che possono causare;</li>
 * <li>{@link com.threeamigos.foresta.attributi.tipi.TipoAttributo}: gli attributi di un personaggio;</li>
 * <li>{@link com.threeamigos.foresta.artefatti.tipi.TipoArtefatto}: gli artefatti e gli slot in cui si portano;</li>
 * <li>{@link com.threeamigos.foresta.tipi.TipoLocazione}: le caselle della mappa;</li>
 * <li>{@link com.threeamigos.foresta.tipi.Comando}: tutto ciò che il giocatore può comandare.</li>
 * </ul>
 */
package com.threeamigos.foresta.tipi;
