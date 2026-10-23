package com.threeamigos.foresta.tipi;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

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
		// I non morti non hanno una forza vitale da prosciugare, né sangue da perdere o un corpo che si infetta
		NON_MORTO.immuneAIncantesimi.addAll(EnumSet.of(TipoIncantesimo.MORTE, TipoIncantesimo.VELENO));
		NON_MORTO.immuneAEffetti.addAll(EnumSet.of(TipoEffettoDiStato.SANGUINAMENTO, TipoEffettoDiStato.INFETTATO));
	}

	// Quanto danno subisce, per tipo di danno, chi è di questa categoria. Tipi mancanti restituiscono 1
	// (danno non modificato)
	private final Map<TipoDanno, Double> moltiplicatoriDannoSubito = new EnumMap<>(TipoDanno.class);

	// Gli incantesimi e gli effetti di stato a cui è immune chi è di questa categoria
	private final Set<TipoIncantesimo> immuneAIncantesimi = EnumSet.noneOf(TipoIncantesimo.class);
	private final Set<TipoEffettoDiStato> immuneAEffetti = EnumSet.noneOf(TipoEffettoDiStato.class);

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
	 * Se chi è di questa categoria è immune a quell'incantesimo (i non morti lo sono a Morte e Veleno)
	 */
	public boolean isImmuneAIncantesimo(TipoIncantesimo tipoIncantesimo) {
		return immuneAIncantesimi.contains(tipoIncantesimo);
	}

	/**
	 * Se chi è di questa categoria è immune a quell'effetto di stato (i non morti non sanguinano e non si infettano)
	 */
	public boolean isImmuneAEffetto(TipoEffettoDiStato tipoEffettoDiStato) {
		return immuneAEffetti.contains(tipoEffettoDiStato);
	}

	/**
	 * Il nome della categoria, per i testi
	 */
	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

}
