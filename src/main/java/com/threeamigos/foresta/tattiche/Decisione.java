package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

import java.util.Collections;
import java.util.List;

/**
 * Che cosa un personaggio ha deciso di fare scorrendo la sua tattica: l'azione, il bersaglio (nessuno per le azioni di gruppo e il cambio
 * d'arma) e il perché, per il registro delle decisioni (tattiche_di_combattimento_automatiche.md §7.7). Non cambia niente nel gioco: chi la esegue
 * è l'automa, che deve anche registrare l'uso della voce e l'esito dell'azione (StatoDelCombattimento).
 */
public final class Decisione {

	private final int indiceVoce;
	private final Azione azione;
	private final TipoAzione tipoEseguito;
	private final ClasseIncantesimo incantesimo;
	private final Personaggio bersaglio;
	private final List<String> spiegazione;

	Decisione(int indiceVoce, Azione azione, TipoAzione tipoEseguito, ClasseIncantesimo incantesimo, Personaggio bersaglio, List<String> spiegazione) {
		this.indiceVoce = indiceVoce;
		this.azione = azione;
		this.tipoEseguito = tipoEseguito;
		this.incantesimo = incantesimo;
		this.bersaglio = bersaglio;
		this.spiegazione = Collections.unmodifiableList(spiegazione);
	}

	/**
	 * La posizione della voce scelta nella tattica (da 0), o -1 se nessuna era eseguibile e si è ripiegato sull'attacco fisico
	 */
	public int getIndiceVoce() {
		return indiceVoce;
	}

	public boolean isRipiego() {
		return indiceVoce < 0;
	}

	/**
	 * L'identificativo della voce con cui lo StatoDelCombattimento ricorda gli usi (come lo vedono i predicati), o null per il ripiego
	 */
	public String getIdentificativoVoce() {
		return indiceVoce < 0 ? null : String.valueOf(indiceVoce);
	}

	/**
	 * L'azione scritta nella voce
	 */
	public Azione getAzione() {
		return azione;
	}

	/**
	 * L'azione da eseguire: quella della voce, tranne per INCANTESIMO_DI_DANNO, che diventa l'incantesimo scelto (FULMINE, GELO...)
	 */
	public TipoAzione getTipoEseguito() {
		return tipoEseguito;
	}

	/**
	 * L'incantesimo scelto per INCANTESIMO_DI_DANNO, o quello dell'azione se è un incantesimo; null altrimenti
	 */
	public ClasseIncantesimo getIncantesimo() {
		return incantesimo;
	}

	/**
	 * Il bersaglio; null per le azioni su un gruppo intero e per il cambio d'arma
	 */
	public Personaggio getBersaglio() {
		return bersaglio;
	}

	/**
	 * Il percorso nella lista: le voci saltate con il motivo, il selettore, l'azione scelta e quelle scartate
	 */
	public List<String> getSpiegazione() {
		return spiegazione;
	}

	@Override
	public String toString() {
		return tipoEseguito + (bersaglio == null ? "" : " su " + bersaglio.getNome()) + (isRipiego() ? " (ripiego)" : " (voce " + (indiceVoce + 1) + ")");
	}
}
