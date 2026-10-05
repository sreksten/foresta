package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.tools.TestataSalvataggio;

import java.util.List;

/**
 * Dove stanno le partite salvate: l'Automa lo riceve nel costruttore (nel gioco GestoreSalvataggiSuFile, nei test un
 * gestore in memoria). Dopo una lettura riuscita il modello dati letto è installato (ModelloDati.setIstanza), ma lo
 * stato derivato va ricostruito: lo fa chi legge, con RiletturaPartita.ricostruisci.
 */
public interface GestoreSalvataggi {

	List<TestataSalvataggio> getSalvataggiDisponibili();

	boolean leggi(Comando id);

	/**
	 * @return true se il salvataggio è stato scritto per intero
	 */
	boolean salva(Comando id);

}
