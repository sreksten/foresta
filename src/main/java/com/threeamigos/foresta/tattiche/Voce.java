package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.tipi.tattiche.PortataAzione;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Una voce di tattica: il selettore (chi), le condizioni (quando, in AND) e una sequenza di azioni (che cosa: la prima applicabile al bersaglio
 * vince). Senza selettore vale quello di base della portata delle azioni. Si può disattivare senza toglierla. Immutabile.
 * Vedi tattiche_di_combattimento_automatiche.md §11.2.
 */
public final class Voce {

	private final Selettore selettore;
	private final List<Predicato> condizioni;
	private final List<Azione> azioni;
	private final boolean attiva;

	/**
	 * @param selettore  chi, o null per quello di base (vedi {@link #selettoreEffettivo()})
	 * @param condizioni quando (in AND); vuota vale sempre
	 * @param azioni     almeno una; la prima applicabile vince
	 */
	public Voce(Selettore selettore, List<Predicato> condizioni, List<Azione> azioni, boolean attiva) {
		if (azioni.isEmpty()) {
			throw new IllegalArgumentException("Una voce ha almeno un'azione");
		}
		this.selettore = selettore;
		this.condizioni = Collections.unmodifiableList(new ArrayList<>(condizioni));
		this.azioni = Collections.unmodifiableList(new ArrayList<>(azioni));
		this.attiva = attiva;
	}

	public Voce(Selettore selettore, List<Predicato> condizioni, List<Azione> azioni) {
		this(selettore, condizioni, azioni, true);
	}

	/**
	 * Una voce attiva, senza condizioni, con le azioni indicate
	 */
	public static Voce su(Selettore selettore, Azione... azioni) {
		return new Voce(selettore, Collections.emptyList(), java.util.Arrays.asList(azioni));
	}

	public Voce disattivata() {
		return new Voce(selettore, condizioni, azioni, false);
	}

	/**
	 * Il selettore di base per la portata delle azioni: avversari (il primo) per un attacco, se stesso per una cura o una pozione, i caduti per la
	 * Resurrezione, gli alleati o gli avversari tutti per un'azione di gruppo, se stesso per il cambio d'arma
	 */
	static Selettore selettoreDiBase(PortataAzione portata) {
		TipoRosa rosa;
		switch (portata) {
			case AVVERSARIO:
			case GRUPPO_AVVERSARI:
				rosa = TipoRosa.AVVERSARI;
				break;
			case ALLEATO_CADUTO:
				rosa = TipoRosa.ALLEATI_CADUTI;
				break;
			case GRUPPO_ALLEATI:
				rosa = TipoRosa.ALLEATI;
				break;
			default:
				rosa = TipoRosa.SE_STESSO;
				break;
		}
		return new Selettore(rosa, Collections.emptyList(),
				Ordinamento.di(com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.PRIMO));
	}

	/**
	 * Su che cosa agiscono le azioni (sono tutte della stessa portata)
	 */
	public PortataAzione getPortata() {
		return azioni.get(0).getTipo().getPortata();
	}

	public Selettore getSelettore() {
		return selettore;
	}

	public Selettore selettoreEffettivo() {
		return selettore != null ? selettore : selettoreDiBase(getPortata());
	}

	public List<Predicato> getCondizioni() {
		return condizioni;
	}

	public List<Azione> getAzioni() {
		return azioni;
	}

	public boolean isAttiva() {
		return attiva;
	}

	/**
	 * Gli errori della voce per quel lato (un mio personaggio o un mostro): azioni di portate diverse, una rosa che nessuna azione accetta
	 * o che non vale per il lato. Vuota se la voce è a posto: la lettura rifiuta una tattica con problemi.
	 */
	public List<String> problemi(boolean attoreDelGiocatore) {
		List<String> problemi = new ArrayList<>();
		PortataAzione portata = getPortata();
		for (Azione azione : azioni) {
			if (azione.getTipo().getPortata() != portata) {
				problemi.add("le azioni di una voce devono avere la stessa portata: " + azione.getTipo() + " non è " + portata);
			}
		}
		Selettore effettivo = selettoreEffettivo();
		if (!effettivo.getRosa().valePer(attoreDelGiocatore)) {
			problemi.add("la rosa " + effettivo.getRosa() + " non vale per " + (attoreDelGiocatore ? "i personaggi del giocatore" : "i mostri"));
		}
		for (Azione azione : azioni) {
			if (!azione.getTipo().roseAmmesse(attoreDelGiocatore).contains(effettivo.getRosa())) {
				problemi.add(azione.getTipo() + " non accetta la rosa " + effettivo.getRosa());
			}
		}
		return problemi;
	}

	@Override
	public String toString() {
		return (selettore == null ? "" : selettore + " ") + (condizioni.isEmpty() ? "" : "SE " + condizioni + " ") + "→ " + azioni
				+ (attiva ? "" : " (disattivata)");
	}
}
