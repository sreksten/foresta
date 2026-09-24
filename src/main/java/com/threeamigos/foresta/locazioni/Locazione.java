package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.Oggetto;

/**
 * Una locazione è un posto all'interno della Foresta dove il gruppo si trova.
 */

public interface Locazione {

	ClassiLocazione getClasseLocazione();

	/**
	 * Il modello dati della casella su cui si trova questa locazione: vi vive
	 * lo stato durevole, che sopravvive alla visita e al salvataggio.
	 */
    LocazioneMD getModelloDati();

    void setModelloDati(LocazioneMD modelloDati);

	/**
	 * Il nome proprio della locazione. Le locazioni notevoli lo hanno cablato,
	 * le altre lo prendono dalle proprietà del modello dati e possono non averlo.
	 */
    String getNome();
	
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
	 * Ripubblica i comandi disponibili nello stato corrente della locazione, senza farla
	 * avanzare: a differenza di impostaAzioni(..., null) non trascorre alcun turno (niente
	 * danni da effetti di stato, niente cambi di stato). Serve all'automa quando il gruppo
	 * torna alla locazione da una schermata che l'ha solo interrotta, come la mappa o l'inventario.
	 */
    void ripresentaComandi();

	/**
	 * Il testo scritto dal giocatore nel Prompt mentre il gruppo è nella locazione (es. il nome proprio
	 * di un artefatto dall'incantatore). Di norma la locazione non ne chiede e lo ignora.
	 */
	default Stato riceviTesto(GruppoGiocatore g, String testo) {
		return Stato.IN_LOCAZIONE;
	}

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
	 * Una volta preso, l'oggetto viene tolto dalla locazione (per evitare che venga preso due volte);
	 * se è l'artefatto del registro, viene tolto anche dal registro.
	 */
    void rimuoviOggetto();

	TipoRiposo getTipoRiposo();
}
