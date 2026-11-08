package com.threeamigos.foresta.tipi;

/**
 * Che cosa vede la UI in un certo momento, in termini che al tutorial interessano: è l'unico modo in cui il registro dei suggerimenti sa che
 * cosa sta succedendo ({@code RegistroSuggerimenti.situazione}). Il registro non ascolta niente e non conosce la UI: chi sa se il gruppo è in
 * combattimento, quali icone ci sono o che è calata la notte glielo dice con un insieme di queste costanti.
 *
 * @author Stefano Reksten
 */
public enum Situazione {

	/** Si sta scegliendo la classe del personaggio (le classi sono le icone della barra). */
	SCELTA_DELLA_CLASSE,
	/** La prima volta che si vede la schermata di gioco. */
	PRIMA_SCHERMATA_DI_GIOCO,
	/** La barra delle icone è visibile. */
	BARRA_DELLE_ICONE_VISIBILE,
	/** C'è l'icona della mappa. */
	ICONA_DELLA_MAPPA_PRESENTE,
	/** Si è aperta la mappa a tutto schermo e ha il joystick virtuale (per chi non ha un mouse). */
	JOYSTICK_SULLA_MAPPA,
	/** Si è aperta la mappa a tutto schermo ed è più grande dello spazio che ha sullo schermo: per vederla tutta bisogna spostarla. */
	MAPPA_PIU_GRANDE_DELLO_SCHERMO,
	/** Si è aperto l'inventario del gruppo. */
	INVENTARIO_APERTO,
	/** Ci sono le quattro icone di movimento (nord, est, sud, ovest). */
	ICONE_DI_MOVIMENTO_PRESENTI,
	/** Si apre il combattimento. */
	COMBATTIMENTO_APERTO,
	/** Il gruppo ha trovato un oggetto. */
	OGGETTO_TROVATO,
	/** Qualcuno del gruppo è ferito e c'è una pozione di salute da dargli. */
	FERITO_CON_UNA_POZIONE,
	/** In combattimento ci sono più comandi del solo attacco semplice (la fuga, il combattimento a round...). */
	SCELTE_IN_COMBATTIMENTO,
	/** C'è una missione da mostrare nel riquadro delle missioni. */
	MISSIONE_ATTIVA,
	/** Si sono visti due scontri con la tattica che agisce da sola: ora si può dire che si cambia (dall'inventario). */
	TATTICHE_DISPONIBILI,
	/** L'avversario si può corrompere. */
	AVVERSARIO_CORROMPIBILE,
	/** Con l'avversario si può fare amicizia. */
	AVVERSARIO_AMICHEVOLE,
	/** Si può lanciare un incantesimo. */
	INCANTESIMO_LANCIABILE,
	/** Il gruppo entra in una bottega. */
	INGRESSO_IN_BOTTEGA,
	/** Cala la notte e ci si può accampare (l'icona dell'accampamento è nella barra). */
	NOTTE,
	/** Si gioca con il dito (tocco) invece che con il mouse. */
	DITO_SULLO_SCHERMO
}
