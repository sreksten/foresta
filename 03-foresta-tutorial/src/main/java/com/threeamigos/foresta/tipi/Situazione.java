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
	/** Non scatta mai da sola: è l'innesco dei messaggi che il motore chiede alla UI di dire con la finestra del tutorial (vedi NotificaMessaggioDelTutorial). */
	MESSAGGIO_DEL_MOTORE,
	/** Il motore dice che fra una locazione e l'altra il gruppo ha recuperato salute e magia (vedi NotificaMessaggioDelTutorial): la UI la tiene finché il suggerimento non è visto. */
	RECUPERO_FRA_LOCAZIONI,
	/** Il giocatore ha già letto come ci si muove e ci sono le frecce di movimento: è l'ultimo consiglio della prima locazione, su dove andare. */
	MOVIMENTO_SPIEGATO,
	/** Si è aperta la schermata delle tattiche di un personaggio. */
	TATTICHE_APERTE,
	/** Sulla schermata di gioco qualcuno del gruppo ha punti abilità da spendere (è salito di livello). */
	PUNTI_ABILITA_DISPONIBILI,
	/** Si è aperto l'inventario e qualcuno del gruppo ha punti abilità da spendere: prima di dire come si spendono si dice che cosa sono gli attributi. */
	ATTRIBUTI_DA_INTRODURRE,
	/** Si è aperto l'inventario e qualcuno del gruppo ha punti abilità da spendere. */
	PUNTI_ABILITA_DA_SPENDERE,
	/** Ci sono le quattro icone di movimento (nord, est, sud, ovest). */
	ICONE_DI_MOVIMENTO_PRESENTI,
	/** Si apre il combattimento. */
	COMBATTIMENTO_APERTO,
	/** Il gruppo ha trovato un oggetto. */
	OGGETTO_TROVATO,
	/** Qualcuno del gruppo ha meno di metà della salute e c'è una pozione di salute da dargli. */
	FERITO_CON_UNA_POZIONE,
	/** In combattimento ci sono più comandi del solo attacco semplice (la fuga, il combattimento a round...). */
	SCELTE_IN_COMBATTIMENTO,
	/** C'è l'icona del combattimento automatico (le tattiche che agiscono da sole, un turno dopo l'altro, finché lo scontro non è finito). */
	COMBATTIMENTO_AUTOMATICO_DISPONIBILE,
	/** Un effetto di stato è stato applicato (a chiunque) in questa locazione. */
	EFFETTO_DI_STATO_APPLICATO,
	/** Si è scatenata una reazione elementale in questa locazione. */
	REAZIONE_ELEMENTALE_SCATENATA,
	/** Un personaggio del gruppo è caduto e il combattimento è finito. */
	PERSONAGGIO_CADUTO,
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
