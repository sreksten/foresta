package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Chi viaggia con il gruppo come ospite perché una missione lo deve scortare (vedi MissioneAPassi.prendiInScorta):
 * un mercante, un pellegrino... Non è un mostro né una classe giocante, non si incontra nelle locazioni e non si
 * recluta. È un personaggio a sé, con le sue caratteristiche (la sua riga in CostantiPersonaggi.valoriDi) e per ora la stessa immagine del
 * bardo; non è un Bardo, e nessuna capacità del Bardo gli arriva per eredità.
 */
public class Viandante extends PersonaggioBase {

	public Viandante(int livello) {
		this(null, livello);
	}

	public Viandante(String nome, int livello) {
		super(nome, TipoPersonaggio.VIANDANTE, livello);
	}

}
