package com.threeamigos.foresta.tipi;

/**
 * Gli intermezzi fissi del gioco (quelli dei passi delle missioni non hanno un tipo).
 * <p>
 * Il nome è anche l'identificativo con cui IntermezziMD ricorda quelli già mostrati.
 * <p>
 * L'intermezzo vero lo costruisce intermezzi.FabbricaIntermezzi.
 *
 * @author Stefano Reksten
 */
public enum TipoIntermezzo {

	/** L'Eremita che a inizio partita chiede chi stia procedendo */
	INTERMEZZO_INTRODUTTIVO,
	/** L'Eremita che dopo la prima locazione augura buona fortuna al giocatore. */
	INTERMEZZO_FINE_PRIMA_LOCAZIONE,
	/** Il Bardo che racconta una storia, e il locandiere che lo brontola */
	INTERMEZZO_LOCANDA_PRIMA_VISITA,
	/** Il bardo della locanda insegna al gruppo l'Urlo della Banshee, dopo il viaggio della moglie */
	INTERMEZZO_URLO_DELLA_BANSHEE,
	/** L'alchimista spiega "ls -la" a chi ha trovato il Manuale di Unix System V e rivela la spada normalmente nascosta; sta prima dell'alchimista di tutti i giorni, a cui cede il posto */
	INTERMEZZO_SPADA_NASCOSTA,
	/** Gli intermezzi che si vedono alla prima visita nei negozi */
	INTERMEZZO_ARMAIOLO,
	INTERMEZZO_ALCHIMISTA,
	INTERMEZZO_VENDITORE_DI_PERGAMENE,
	/** L'incantatore spiega che The C Programming Language riprogramma gli artefatti, e che un errore finisce in un Core Dump; prima dell'incantatore di tutti i giorni, a cui cede il posto */
	INTERMEZZO_RIPROGRAMMAZIONE,
	INTERMEZZO_INCANTATORE,
	/** Uno degli intermezzi dell'accampamento */
	INTERMEZZO_ACCAMPAMENTO,
	/** L'ultimo eremita disturbato dice al gruppo quello che pensa, a missione DisturbatoreDellaQuietePubblica completata */
	INTERMEZZO_EREMITA_ROMPISCATOLE,
	/** Il capo legge il titolo di un libro tecnico appena trovato in un cofano; si ricorda uno per libro, con la sua chiave in coda al nome */
	INTERMEZZO_LIBRO_TECNICO,
	/** Di ripiego: la visita successiva alla locanda, che aspetta se all'ingresso scatta altro (per esempio l'incarico di una missione) */
	INTERMEZZO_LOCANDA_VISITA_SUCCESSIVA,
	/** Di ripiego: chi entra a ore impossibili, solo se all'ingresso non scatta nient'altro */
	INTERMEZZO_NOTTE_ARMAIOLO,
	INTERMEZZO_NOTTE_ALCHIMISTA,
	INTERMEZZO_NOTTE_VENDITORE_DI_PERGAMENE,
	INTERMEZZO_NOTTE_INCANTATORE,
	INTERMEZZO_NOTTE_LOCANDA
}
