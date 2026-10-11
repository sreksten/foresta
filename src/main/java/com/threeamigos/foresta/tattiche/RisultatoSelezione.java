package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Collections;
import java.util.List;

/**
 * L'esito di un selettore: il bersaglio scelto (o nessuno), i candidati rimasti dopo i filtri e il percorso, per il registro delle decisioni
 * (tattiche_di_combattimento_automatiche.md §7.7).
 */
public final class RisultatoSelezione {

	private final Personaggio scelto;
	private final List<Personaggio> rimasti;
	private final List<String> passi;

	RisultatoSelezione(Personaggio scelto, List<Personaggio> rimasti, List<String> passi) {
		this.scelto = scelto;
		this.rimasti = Collections.unmodifiableList(rimasti);
		this.passi = Collections.unmodifiableList(passi);
	}

	/**
	 * Il bersaglio, o null se non è rimasto nessun candidato (la voce non è eseguibile)
	 */
	public Personaggio getScelto() {
		return scelto;
	}

	/**
	 * I candidati che hanno superato tutti i filtri, nell'ordine della rosa
	 */
	public List<Personaggio> getRimasti() {
		return rimasti;
	}

	public boolean isVuoto() {
		return rimasti.isEmpty();
	}

	/**
	 * Il percorso, un passo per riga: la rosa e quanti candidati aveva, ogni filtro e quanti ne sono rimasti, l'ordinamento e chi ha scelto
	 */
	public List<String> getPassi() {
		return passi;
	}
}
