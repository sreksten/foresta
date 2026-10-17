package com.threeamigos.foresta.tipi;

import java.util.EnumMap;
import java.util.Map;

/**
 * La categoria di creatura di un Personaggio, per scegliere chi colpire o a chi rivolgere un'azione "per categoria" (un
 * non morto, un volante...) durante l'esecuzione di una Tattica. Un {@link TipoPersonaggio} ne ha una o più:
 * per esempio, lo Spirito è sia NON_MORTO, sia VOLANTE.
 * <p>
 * I testi di ogni costante (NOME) non stanno qui ma nel file {@code categoria_personaggio_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum CategoriaPersonaggio {

	/**
	 * Ha forma umana: i giocanti, i goblin, la Strega...
	 */
	UMANOIDE,
	/**
	 * Un animale o una bestia, senza un corpo da umanoide
	 */
	BESTIA,
	/**
	 * Un drago o un suo parente stretto: la Viverna, la Chimera-Drago
	 */
	DRACONICO,
	/**
	 * Morto che continua ad agire: scheletri, spettri, spiriti, Lich
	 */
	NON_MORTO,
	/**
	 * Si muove in volo, fuori dalla portata di chi combatte a terra
	 */
	VOLANTE,
	/**
	 * Di taglia esagerata, che sovrasta chi le sta davanti: il Gigante, il Titano, il Troll, l'Idra, il Drago...
	 */
	GIGANTESCO,
	/**
	 * Un corpo fatto di più creature: arpie, centauri, minotauri, chimere, chimere-drago
	 */
	IBRIDO,
	/**
	 * Una creatura fabbricata o animata, non nata: il gargoyle di pietra, un giorno i golem
	 */
	COSTRUTTO;

	// Quanto danno subisce, per tipo di danno, chi è di questa categoria. Tipi mancanti restituiscono 1
	// (danno non modificato)
	private final Map<TipoDanno, Double> moltiplicatoriDannoSubito = new EnumMap<>(TipoDanno.class);

	// Le debolezze e le resistenze comuni a una categoria (quelle di una sola classe stanno in TipoPersonaggio).
	// Vedi anche debolezze_e_resistenze.md
	static {
		// I non morti: la luce sacra li brucia, il veleno non li tocca (non hanno un corpo che vive) e
		// l'energia necrotica li rafforza più che ferirli
		NON_MORTO.modifica(TipoDanno.SACRO, 2.0d);
		NON_MORTO.modifica(TipoDanno.VELENO, 0.0d);
		NON_MORTO.modifica(TipoDanno.NECROTICO, 0.5d);
		// Chi vola è fuori dalla portata di ciò che scuote il terreno
		VOLANTE.modifica(TipoDanno.TERRA, 0.5d);
		// Ciò che è fabbricato non respira, non ha una mente e non ha sangue
		COSTRUTTO.modifica(TipoDanno.VELENO, 0.0d);
		COSTRUTTO.modifica(TipoDanno.PSICHICO, 0.0d);
	}

	CategoriaPersonaggio() {
	}

	private void modifica(TipoDanno tipoDanno, double moltiplicatore) {
		moltiplicatoriDannoSubito.put(tipoDanno, moltiplicatore);
	}

	/**
	 * Per quanto si moltiplica il danno di quel tipo contro chi è di questa categoria: 1 se non cambia, 0 se è immune,
	 * tra 0 e 1 se resiste, più di 1 se è debole (i non morti subiscono il doppio dal SACRO e sono immuni al VELENO).
	 */
	public double getMoltiplicatoreDannoSubito(TipoDanno tipoDanno) {
		return moltiplicatoriDannoSubito.getOrDefault(tipoDanno, 1.0d);
	}

	/**
	 * Il nome della categoria, per i testi
	 */
	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}
}
