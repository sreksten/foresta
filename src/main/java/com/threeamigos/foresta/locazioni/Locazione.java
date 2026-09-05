package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.Oggetto;

/**
 * Una locazione è un posto all'interno della Foresta dove il gruppo si trova.
 */

public interface Locazione {

	ClassiLocazione getClasseLocazione();
	
	/**
	 * Alcune locazioni possono aver bisogno di tenere traccia di uno stato;
	 * occorre che tali locazioni che estendono LocazioneBase chiamino
	 * super.reimposta().
	 */
    void reimposta();

	/**
	 * All'interno di una specifica locazione possono essere creati determinati
	 * tipi di mostri e di oggetti
	 */
    void crea(GruppoGiocatore g, GruppoAvversario gng);

	/**
	 * Descrive il luogo ed eventualmente mostri e oggetti.
	 */
    void descrivi(GruppoGiocatore g, GruppoAvversario gng);

	/**
	 * All'interno di una specifica locazione possono essere eseguiti determinati tipi di azioni:
	 * ad esempio nella locanda possono essere trovate nuove persone e dentro la città si
	 * può cercare un alchimista. Normalmente nella foresta si combatte eccetera.
	 * La funzione torna lo stato in cui si trova il gruppo; solitamente riporta
	 * IN_LOCAZIONE (quando c'è sempre qualcosa rimasto da fare) o LOCAZIONE_COMPLETA.
	 * L'elenco di possibili azioni verrà riempito
	 * a partire dal primo elemento con tutte le azioni che è possibile intraprendere; l'ultima
	 * sarà null. Se lo stato tornato non è Automa.NUOVA_LOCAZIONE allora
	 * l'automa che controlla il gruppo richiamerà questa funzione.
	 * Se l'azione passata alla funzione non è Azione.INVALIDA allora la locazione eseguirà
	 * l'azione riportando un nuovo stato e reimpostando le azioni.
	 */
    Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione);

	/**
	 * Il giocatore ha portato in fondo la locazione o è fuggito?
	 */
    boolean isCompleta();
	
	/**
	 * Il gruppo è riuscito a stringere amicizia?
	 */
    boolean isHaStrettoAmicizia();

	/**
	 * Alla fine di un turno un gruppo rade al suolo una locazione e si può verificare
	 * qualcosa; per adesso al momento in cui il giocatore rade al suolo quattro castelli
	 * appare quello del drago
	 */
    void azzeraLocazione(GruppoGiocatore g);
	
	/**
	 * Un oggetto speciale custodito nella locazione
	 */
    Oggetto getOggetto();
	
	/**
	 * Una volta preso, l'oggetto viene tolto dalla locazione (per evitare che venga preso due volte)
	 */
    void rimuoviOggetto();

	TipoRiposo getTipoRiposo();
}
