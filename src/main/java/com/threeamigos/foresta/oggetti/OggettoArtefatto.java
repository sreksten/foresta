package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;

import java.util.Optional;

/**
 * Un oggetto del loot (spada, scudo, elmo, armatura) che diventa un artefatto vero. L'artefatto nasce
 * con l'oggetto, al livello di riferimento, così è già noto quando si sceglie chi lo prende
 * (vedi {@link Artefatto#raccogli}).
 */
public abstract class OggettoArtefatto extends OggettoBase implements Oggetto {

	private final Artefatto artefatto;

	OggettoArtefatto(TipoArtefatto tipo) {
		super();
		artefatto = GeneratoreArtefatti.istanza().generaArtefatto(tipo, Statistiche.getLivello());
	}

	@Override
	public Optional<Artefatto> getArtefatto() {
		return Optional.of(artefatto);
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		if (!Artefatto.raccogli(gruppo, azione, artefatto)) {
			return false;
		}
		return super.prendi(gruppo, azione);
	}
}
