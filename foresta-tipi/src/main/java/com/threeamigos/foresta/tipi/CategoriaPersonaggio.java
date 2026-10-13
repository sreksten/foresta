package com.threeamigos.foresta.tipi;

import java.util.EnumMap;
import java.util.Map;

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
	// Quanto danno subisce, per tipo di danno, chi è di questa categoria: i tipi che mancano valgono 1
	private final Map<TipoDanno, Double> dannoSubito = new EnumMap<>(TipoDanno.class);

	// Le debolezze e le resistenze comuni a una categoria (quelle di una sola classe stanno in TipoPersonaggio). Tabella
	// provvisoria, con le sue ragioni in debolezze_e_resistenze.md
	static {
		// I non morti: la luce sacra li brucia, il veleno non li tocca (non hanno un corpo che vive) e l'energia necrotica li
		// rafforza più che ferirli
		NON_MORTO.moltiplicaIlDannoSubito(TipoDanno.SACRO, 2.0d);
		NON_MORTO.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.0d);
		NON_MORTO.moltiplicaIlDannoSubito(TipoDanno.NECROTICO, 0.5d);
		// Chi vola è fuori dalla portata di ciò che scuote il terreno
		VOLANTE.moltiplicaIlDannoSubito(TipoDanno.TERRA, 0.5d);
		// Ciò che è fabbricato non respira, non ha una mente e non ha sangue
		COSTRUTTO.moltiplicaIlDannoSubito(TipoDanno.VELENO, 0.0d);
		COSTRUTTO.moltiplicaIlDannoSubito(TipoDanno.PSICHICO, 0.0d);
	}

	CategoriaPersonaggio(String nome) {
		this.nome = nome;
	}

	private void moltiplicaIlDannoSubito(TipoDanno tipoDanno, double moltiplicatore) {
		dannoSubito.put(tipoDanno, moltiplicatore);
	}

	/**
	 * Per quanto si moltiplica il danno di quel tipo contro chi è di questa categoria: 1 se non cambia, 0 se è immune, tra 0
	 * e 1 se resiste, più di 1 se è debole (i non morti subiscono il doppio dal SACRO e sono immuni al VELENO).
	 */
	public double getMoltiplicatoreDannoSubito(TipoDanno tipoDanno) {
		return dannoSubito.getOrDefault(tipoDanno, 1.0d);
	}

	/**
	 * Il nome della categoria, per i testi
	 */
	public String getNome() {
		return nome;
	}
}
