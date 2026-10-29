/**
 * Le missioni: gli incarichi e le storie che il gruppo porta avanti durante la partita.
 * <p>
 * Questo pacchetto contiene l'<b>infrastruttura</b>: l'interfaccia {@link com.threeamigos.foresta.missioni.Missione}, la base
 * {@link com.threeamigos.foresta.missioni.MissioneBase} (per le missioni scritte a mano), la base
 * {@link com.threeamigos.foresta.missioni.MissioneAPassi} (per quelle fatte di {@link com.threeamigos.foresta.missioni.Passo passi},
 * un piccolo automa con controlli, intermezzi, domande e ricompense), il {@link com.threeamigos.foresta.missioni.RegistroMissioni}
 * che le tiene e le controlla e la {@link com.threeamigos.foresta.missioni.FabbricaMissioni} che le ricostruisce dopo un caricamento.
 * Vedi gestione_missioni.md.
 * <p>
 * Le missioni vere stanno nei sottopacchetti:
 * <ul>
 * <li>{@code principali}: il drago e i quattro boss alleati, e le missioni fisse a tema (le locande da visitare, gli eremiti da disturbare);</li>
 * <li>{@code incarichi}: gli incarichi che si prendono in città (il corriere, il rituale, l'indagine, la liberazione...), tutti figli di
 * {@code IncaricoInCitta};</li>
 * <li>{@code richieste}: i dati che ogni famiglia di incarichi legge da una riga della grammatica {@code missioni.txt}, e gli aiuti per leggerle;</li>
 * <li>{@code leggende}: le leggende degli oggetti leggendari e dei loro set;</li>
 * <li>{@code storie}: le missioni legate alla trama o ai compagni (la benedizione, il favore, la lealtà, il viaggio della moglie, i campioni).</li>
 * </ul>
 * Il catalogo completo è in passi_missioni.md.
 */
package com.threeamigos.foresta.missioni;
