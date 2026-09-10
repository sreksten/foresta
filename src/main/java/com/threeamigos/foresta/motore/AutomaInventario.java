package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.List;
import java.util.function.Consumer;

/**
 * Gestisce lo scambio di artefatti tra l'inventario di un personaggio e un pool generico
 * di artefatti disponibili (tipicamente quelli del gruppo, in futuro anche quelli di un PNG).
 * Le due callback si occupano di rendere persistente ogni variazione del pool nella sua
 * sorgente reale, dato che artefattiDisponibili ne è solo una copia usata per il disegno.
 */
public class AutomaInventario {

	private final Personaggio personaggio;
	private final List<Artefatto> artefattiDisponibili;
	private final Consumer<Artefatto> aggiungiAlPool;
	private final Consumer<Artefatto> rimuoviDalPool;

	public AutomaInventario(Personaggio personaggio, List<Artefatto> artefattiDisponibili,
							 Consumer<Artefatto> aggiungiAlPool, Consumer<Artefatto> rimuoviDalPool) {
		this.personaggio = personaggio;
		this.artefattiDisponibili = artefattiDisponibili;
		this.aggiungiAlPool = aggiungiAlPool;
		this.rimuoviDalPool = rimuoviDalPool;
	}

	public Personaggio getPersonaggio() {
		return personaggio;
	}

	public List<Artefatto> getArtefattiDisponibili() {
		return artefattiDisponibili;
	}

	public void spostaNelPersonaggio(Artefatto artefatto) {
		artefattiDisponibili.remove(artefatto);
		rimuoviDalPool.accept(artefatto);
		personaggio.addArtefatto(artefatto);
	}

	public void spostaNelPool(Artefatto artefatto) {
		personaggio.removeArtefatto(artefatto);
		aggiungiAlPool.accept(artefatto);
		artefattiDisponibili.add(artefatto);
	}
}
