package com.threeamigos.foresta.personaggi;

/**
 * Chi si unisce al gruppo per farsi scortare da una missione (vedi MissioneAPassi.prendiInScorta): un mercante, un
 * pellegrino... Non è un mostro né una classe giocante, non si incontra nelle locazioni e non si recluta. Ha le
 * caratteristiche del bardo, e per ora anche la sua immagine.
 */
public class Viandante extends Bardo {

	public Viandante(int livello) {
		this(null, livello);
	}

	public Viandante(String nome, int livello) {
		super(nome, ClassePersonaggio.VIANDANTE, livello);
	}

	@Override
	public String getNomeSingolare() { return "Viandante"; }

	@Override
	public String getNomePlurale() { return "Viandanti"; }
}
