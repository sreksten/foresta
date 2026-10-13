package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.TipoConsumabile;

/**
 * Una voce del listino dell'alchimista, come la vede la UI: il motore la costruisce a ogni apertura della bottega
 * (vedi RichiestaAperturaInventarioFornitore) e decide anche il prezzo dell'acquisto, partendo da {@link #getCosto()}.
 */
public interface VistaOffertaConsumabile {

	TipoConsumabile getTipo();

	/**
	 * @return la classe dell'incantesimo in vendita, o null se l'offerta non è un incantesimo
	 */
	ClasseIncantesimo getClasseIncantesimo();

	/**
	 * @return il personaggio a cui va l'aumento di magia, o null se l'offerta non è per un personaggio solo
	 */
	VistaPersonaggio getPersonaggio();

	String getNome();

	String getDescrizione();

	/**
	 * @return il costo di listino: il gruppo paga quello scontato o maggiorato dalla contrattazione
	 * (vedi VistaGruppoGiocatore.prezzoAcquisto)
	 */
	int getCosto();

}
