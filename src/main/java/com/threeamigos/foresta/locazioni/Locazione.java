package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.oggetti.Oggetto;

/**
 * Una locazione e' un posto all'interno della Foresta dove il gruppo si trova.
 */

public interface Locazione {

	public ClassiLocazione getClasseLocazione();
	
	/**
	 * Alcune locazioni possono aver bisogno di tenere traccia di uno stato;
	 * occorre che tali locazioni che estendono LocazioneBase chiamino
	 * super.reimposta().
	 */
	public void reimposta();

	/**
	 * All'interno di una specifica locazione possono essere creati determinati
	 * tipi di mostri e di oggetti
	 */
	public void crea(GruppoGiocatore g, GruppoAvversario gng);

	/**
	 * Descrive il luogo ed eventualmente mostri ed oggetti.
	 */
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng);

	/**
	 * All'interno di una specifica locazione possono essere eseguiti determinati tipi di azioni:
	 * ad esempio nella locanda possono essere trovate nuove persone e dentro la citta' si
	 * puo' cercare un alchimista. Normalmente nella foresta si combatte eccetera.
	 * La funzione torna lo stato in cui si trova il gruppo; solitamente riporta
	 * IN_LOCAZIONE (c'e' sempre qualcosa da fare) o LOCAZIONE_COMPLETA.
	 * L'array di interi verra' riempito
	 * a partire dal primo elemento con tutte le azioni che e' possibile intraprendere; l'ultima
	 * sara' null. Se lo stato tornato non e' Automa.NUOVA_LOCAZIONE allora
	 * l'automa che controlla il gruppo richiamera' questa funzione.
	 * Se l'azione passata alla funzione non e' Azione.INVALIDA allora la locazione eseguira'
	 * l'azione riportando un nuovo stato e reimpostando le azioni.
	 */
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione);

	/**
	 * Il giocatore ha portato in fondo la locazione o e' fuggito?
	 */
	public boolean isCompleta();
	
	/**
	 * Il gruppo è riuscito a stringere amicizia?
	 */
	public boolean isHaStrettoAmicizia();

	/**
	 * Alla fine di un turno un gruppo rade al suolo una locazione e si può verificare
	 * qualcosa; per adesso al momento in cui il giocatore rade al suolo quattro castelli
	 * appare quello del drago
	 */
	public void azzeraLocazione(GruppoGiocatore g);
	
	/**
	 * Un oggetto custodito nella locazione
	 */
	public Oggetto getOggetto();
	
	/**
	 * Una volta preso l'oggetto viene tolto dalla locazione (per evitare che venga preso due volte)
	 */
	public void rimuoviOggetto();
}
