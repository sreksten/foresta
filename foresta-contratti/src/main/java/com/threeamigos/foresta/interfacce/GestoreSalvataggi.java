package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;

import java.util.List;

/**
 * Dove stanno le partite salvate: l'Automa lo riceve nel costruttore (nel gioco GestoreSalvataggiSuFile, nei test un
 * gestore in memoria). Dopo una lettura riuscita il modello dati letto è installato (ModelloDati.setIstanza), ma lo
 * stato derivato va ricostruito: lo fa chi legge, con RiletturaPartita.ricostruisci.
 * Gli slot da {@code NUMERO_1} a {@code NUMERO_5} sono quelli che sceglie il giocatore e che si elencano; {@code SALVATAGGIO_AUTOMATICO}
 * è lo slot a parte che l'Automa scrive quando l'app va in background, e non compare nell'elenco.
 */
public interface GestoreSalvataggi {

	List<TestataSalvataggio> getSalvataggiDisponibili();

	boolean leggi(Comando id);

	/**
	 * @return true se il salvataggio è stato scritto per intero
	 */
	boolean salva(Comando id);

	/**
	 * Cancella il salvataggio di quello slot, se c'è.
	 */
	void elimina(Comando id);

}
