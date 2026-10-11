package com.threeamigos.foresta.modellodati.tattiche;

import java.util.Objects;
import com.threeamigos.foresta.modellodati.Serializzabile;
import com.threeamigos.foresta.modellodati.LettoreCampi;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
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
public final class VoceMD {

	private final SelettoreMD selettore;
	private final List<PredicatoMD> condizioni;
	private final List<AzioneMD> azioni;
	private final boolean attiva;

	/**
	 * @param selettore  chi, o null per quello di base (vedi {@link #selettoreEffettivo()})
	 * @param condizioni quando (in AND); vuota vale sempre
	 * @param azioni     almeno una; la prima applicabile vince
	 */
	public VoceMD(SelettoreMD selettore, List<PredicatoMD> condizioni, List<AzioneMD> azioni, boolean attiva) {
		if (azioni.isEmpty()) {
			throw new IllegalArgumentException("Una voce ha almeno un'azione");
		}
		this.selettore = selettore;
		this.condizioni = Collections.unmodifiableList(new ArrayList<>(condizioni));
		this.azioni = Collections.unmodifiableList(new ArrayList<>(azioni));
		this.attiva = attiva;
	}

	public VoceMD(SelettoreMD selettore, List<PredicatoMD> condizioni, List<AzioneMD> azioni) {
		this(selettore, condizioni, azioni, true);
	}

	/**
	 * Una voce attiva, senza condizioni, con le azioni indicate
	 */
	public static VoceMD su(SelettoreMD selettore, AzioneMD... azioni) {
		return new VoceMD(selettore, Collections.emptyList(), java.util.Arrays.asList(azioni));
	}

	public VoceMD disattivata() {
		return new VoceMD(selettore, condizioni, azioni, false);
	}

	/**
	 * Il selettore di base per la portata delle azioni: avversari (il primo) per un attacco, se stesso per una cura o una pozione, i caduti per la
	 * Resurrezione, gli alleati o gli avversari tutti per un'azione di gruppo, se stesso per il cambio d'arma
	 */
	static SelettoreMD selettoreDiBase(PortataAzione portata) {
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
		return new SelettoreMD(rosa, Collections.emptyList(),
				OrdinamentoMD.di(com.threeamigos.foresta.tipi.tattiche.TipoOrdinamento.PRIMO));
	}

	/**
	 * Su che cosa agiscono le azioni (sono tutte della stessa portata)
	 */
	public PortataAzione getPortata() {
		return azioni.get(0).getTipo().getPortata();
	}

	public SelettoreMD getSelettore() {
		return selettore;
	}

	public SelettoreMD selettoreEffettivo() {
		return selettore != null ? selettore : selettoreDiBase(getPortata());
	}

	public List<PredicatoMD> getCondizioni() {
		return condizioni;
	}

	public List<AzioneMD> getAzioni() {
		return azioni;
	}

	public boolean isAttiva() {
		return attiva;
	}

	/**
	 * Gli errori della voce per quel lato (un mio personaggio o un mostro): azioni di portate diverse, una rosa che nessuna azione accetta
	 * o che non vale per il lato. Vuota se la voce è a posto. La rilettura non rifiuta una tattica con problemi: la disabilita (TatticaMD.problemiPerQualsiasiLato).
	 */
	public List<String> problemi(boolean attoreDelGiocatore) {
		List<String> problemi = new ArrayList<>();
		PortataAzione portata = getPortata();
		for (AzioneMD azione : azioni) {
			if (azione.getTipo().getPortata() != portata) {
				problemi.add("le azioni di una voce devono avere la stessa portata: " + azione.getTipo() + " non è " + portata);
			}
		}
		SelettoreMD effettivo = selettoreEffettivo();
		if (!effettivo.getRosa().valePer(attoreDelGiocatore)) {
			problemi.add("la rosa " + effettivo.getRosa() + " non vale per " + (attoreDelGiocatore ? "i personaggi del giocatore" : "i mostri"));
		}
		for (AzioneMD azione : azioni) {
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

	/**
	 * Scrive la voce, il suo selettore (se c'è), le condizioni e le azioni (vedi FormatoTattiche)
	 */
	public void salva(PrintWriter stream) {
		stream.println(String.join(Serializzabile.PIPE, "VOCE", String.valueOf(attiva), String.valueOf(selettore != null),
				String.valueOf(condizioni.size()), String.valueOf(azioni.size())));
		if (selettore != null) {
			selettore.salva(stream);
		}
		for (PredicatoMD condizione : condizioni) {
			condizione.salva(stream);
		}
		for (AzioneMD azione : azioni) {
			azione.salva(stream);
		}
	}

	public static VoceMD leggi(BufferedReader stream) throws IOException {
		LettoreCampi campi = FormatoTattiche.riga(stream, "VOCE");
		boolean attiva = campi.booleano();
		boolean haSelettore = campi.booleano();
		int numeroCondizioni = campi.intero();
		int numeroAzioni = campi.intero();
		SelettoreMD selettore = haSelettore ? SelettoreMD.leggi(stream) : null;
		List<PredicatoMD> condizioni = new ArrayList<>();
		for (int i = 0; i < numeroCondizioni; i++) {
			condizioni.add(PredicatoMD.leggi(stream));
		}
		List<AzioneMD> azioni = new ArrayList<>();
		for (int i = 0; i < numeroAzioni; i++) {
			azioni.add(AzioneMD.leggi(stream));
		}
		if (azioni.isEmpty()) {
			throw new IOException("Una voce senza azioni nel salvataggio");
		}
		return new VoceMD(selettore, condizioni, azioni, attiva);
	}

	@Override
	public boolean equals(Object altro) {
		if (!(altro instanceof VoceMD)) {
			return false;
		}
		VoceMD v = (VoceMD) altro;
		return attiva == v.attiva && Objects.equals(selettore, v.selettore) && condizioni.equals(v.condizioni) && azioni.equals(v.azioni);
	}

	@Override
	public int hashCode() {
		return Objects.hash(selettore, condizioni, azioni, attiva);
	}
}
