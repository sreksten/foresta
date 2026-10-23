package com.threeamigos.foresta.tipi;

/**
 * Una capacità innata di una classe (l'imposizione delle mani del Chierico, i canti del Bardo, l'alba sacra del Sacerdote, il dardo arcano del Mago...)
 * con il suo nome e a che cosa serve, per il bestiario. I testi non sono scritti qui ma vengono da quelli di chi le definisce (TipoAbilita, TipoCanto,
 * TipoIncantesimo, TipoAzione).
 *
 * @author Stefano Reksten
 */
public final class AbilitaInnataDiClasse {

	private final String nome;
	private final String descrizione;

	public AbilitaInnataDiClasse(String nome, String descrizione) {
		this.nome = nome;
		this.descrizione = descrizione;
	}

	public String getNome() {
		return nome;
	}

	/**
	 * A che cosa serve, in una frase
	 */
	public String getDescrizione() {
		return descrizione;
	}

	@Override
	public String toString() {
		return nome;
	}
}
