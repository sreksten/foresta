package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Dove stanno le partite salvate: l'Automa lo riceve nel costruttore (nel gioco GestoreSalvataggiSuFile, nei test un
 * gestore in memoria). Dopo una lettura riuscita il modello dati letto è installato (ModelloDati.setIstanza), ma lo
 * stato derivato va ricostruito: lo fa chi legge, con RiletturaPartita.ricostruisci.
 * Gli slot da {@code NUMERO_1} a {@code NUMERO_4} ({@link #SLOT_MANUALI}) sono quelli che sceglie il giocatore e che si elencano;
 * {@code SALVATAGGIO_AUTOMATICO} è lo slot a parte che l'Automa scrive quando l'app va in background, e non compare nell'elenco.
 * Lo slot {@link #SLOT_IN_MEMORIA} ({@code NUMERO_5}) non è di questi gestori: è il salvataggio automatico in memoria che l'Automa
 * tiene da sé, e compare solo nell'elenco da cui si carica.
 */
public interface GestoreSalvataggi {

	/** Gli slot in cui il giocatore può salvare e da cui può caricare, nell'ordine in cui si mostrano */
	List<Comando> SLOT_MANUALI = Collections.unmodifiableList(Arrays.asList(Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3, Comando.NUMERO_4));

	/** Il salvataggio automatico in memoria, l'ultima riga dell'elenco da cui si carica: non si può scrivere a mano */
	Comando SLOT_IN_MEMORIA = Comando.NUMERO_5;

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
