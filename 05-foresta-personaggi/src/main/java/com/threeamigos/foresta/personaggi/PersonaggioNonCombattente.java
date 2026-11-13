package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Chi nelle scene non combatte: il locandiere, l'armaiolo, l'alchimista, il venditore di pergamene, l'incantatore, la
 * moglie del bardo, il borgomastro e i mandanti qualunque (Uomo, Donna). Ha le sue caratteristiche (la riga comune dei non combattenti in CostantiPersonaggi, un poco più basse di quelle di un bardo),
 * nessuna capacità propria (niente dardo arcano né alba sacra né canti), non si incontra, non si recluta e non si
 * corrompe: compare nelle scene e viaggia con il gruppo come ospite quando una missione lo deve scortare (vedi
 * Viandante). Non è un Bardo.
 * <p>
 * Il sesso lo dice la tabella delle classi (la moglie del bardo e la donna sono femmine).
 */
public abstract class PersonaggioNonCombattente extends PersonaggioBase implements Personaggio {

	protected PersonaggioNonCombattente(String nome, TipoPersonaggio classe, int livello) {
		super(nome, classe, livello);
	}

}
