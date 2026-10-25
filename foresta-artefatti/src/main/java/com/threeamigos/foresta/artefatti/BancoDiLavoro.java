package com.threeamigos.foresta.artefatti;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Il banco di lavoro dell'incantatore: un artefatto incantabile e gli ingredienti magici da fondere (pergamene, gemme, monili, gingilli, sigilli, aggiornamenti).
 * Vive solo mentre si è nella bottega e non si salva: uscendo, quel che resta torna nel gruppo.
 */
public class BancoDiLavoro implements ScambiatoreArtefatti {

	private final List<Artefatto> artefatti = new ArrayList<>();

	/**
	 * Un banco vuoto.
	 */
	public BancoDiLavoro() {
	}

	@Override
	public Collection<Artefatto> getInventario() {
		return new ArrayList<>(artefatti);
	}

	@Override
	public void addArtefatto(Artefatto artefatto) {
		artefatti.add(artefatto);
	}

	@Override
	public void removeArtefatto(Artefatto artefatto) {
		// Gli Artefatto si ricreano dal loro modello dati: si confronta quello
		artefatti.removeIf(a -> a.getModelloDati() == artefatto.getModelloDati());
	}
}
