package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.oggetti.Artefatto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Il banco di lavoro dell'incantatore: un artefatto incantabile e le pergamene da fondere.
 * Vive solo mentre si è nella bottega e non si salva: uscendo, quel che resta torna nel gruppo.
 */
public class BancoDiLavoro implements ScambiatoreArtefatti {

	private final List<Artefatto> artefatti = new ArrayList<>();

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
