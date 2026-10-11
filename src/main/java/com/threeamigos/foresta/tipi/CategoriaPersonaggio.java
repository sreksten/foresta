package com.threeamigos.foresta.tipi;

/**
 * Il genere di creatura di un personaggio, per scegliere chi colpire o a chi rivolgere un'azione "per categoria" (un
 * non morto, un volante...). Una classe (TipoPersonaggio) ne ha una o più: lo Spirito è sia NON_MORTO sia VOLANTE.
 */
public enum CategoriaPersonaggio {

	/** Ha forma umana: i giocanti, i goblin, la Strega... */
	UMANOIDE("Umanoide"),
	/** Un animale o una bestia, senza un corpo da umanoide */
	BESTIA("Bestia"),
	/** Un drago o un suo parente stretto: la Viverna, la Chimera Drago */
	DRACONICO("Draconico"),
	/** Morto che continua ad agire: scheletri, spettri, spiriti, lich */
	NON_MORTO("Non morto"),
	/** Si muove in volo, fuori dalla portata di chi combatte a terra */
	VOLANTE("Volante"),
	/** Di taglia esagerata, che sovrasta chi le sta davanti: il Gigante, il Titano, il Troll, l'Idra, il Drago... Non è la razza dei giganti */
	GIGANTESCO("Gigantesco"),
	/** Un corpo fatto di più creature: arpie, centauri, minotauri, chimere */
	IBRIDO("Ibrido"),
	/** Una creatura fabbricata o animata, non nata: il gargoyle di pietra, un giorno i golem */
	COSTRUTTO("Costrutto");

	private final String nome;

	CategoriaPersonaggio(String nome) {
		this.nome = nome;
	}

	/**
	 * Il nome della categoria, per i testi
	 */
	public String getNome() {
		return nome;
	}
}
