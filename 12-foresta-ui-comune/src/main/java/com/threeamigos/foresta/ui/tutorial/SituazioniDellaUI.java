package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Situazione;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Traduce quello che la UI sa (le icone nella barra, il combattimento aperto, un oggetto appena trovato...) nell'insieme di {@link Situazione} che
 * il tutorial capisce. La UI aggiorna i fatti che non si leggono dalle icone con i metodi {@code è…/segna…} man mano che arrivano gli eventi, e a
 * ogni cambiamento chiede {@link #correnti}. Non conosce il bus né il toolkit: si prova da sola.
 *
 * @author Stefano Reksten
 */
public final class SituazioniDellaUI {

	/** Dopo quanti scontri visti compare l'icona delle tattiche, e quindi il suggerimento */
	static final int SCONTRI_PRIMA_DELLE_TATTICHE = 2;

	private static final Set<Comando> MOVIMENTO = EnumSet.of(Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST);
	private static final Set<Comando> BOTTEGHE = EnumSet.of(Comando.NEGOZIO_ALCHIMISTA, Comando.NEGOZIO_ARMAIOLO,
			Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, Comando.NEGOZIO_INCANTATORE);

	private boolean schermataDiGioco;
	private boolean primaSchermataDaDare;
	private boolean combattimentoAperto;
	private int scontriVisti;
	private boolean oggettoTrovato;

	/**
	 * La schermata di gioco è comparsa: la prima volta è una situazione a sé (i riquadri).
	 */
	public synchronized void segnaSchermataDiGioco() {
		if (!schermataDiGioco) {
			primaSchermataDaDare = true;
		}
		schermataDiGioco = true;
	}

	/**
	 * Si lascia la schermata di gioco (intro, scelta della classe, partita finita).
	 */
	public synchronized void segnaFuoriDalGioco() {
		schermataDiGioco = false;
		primaSchermataDaDare = false;
		combattimentoAperto = false;
	}

	public synchronized void segnaCombattimentoAperto() {
		if (!combattimentoAperto) {
			scontriVisti++;
		}
		combattimentoAperto = true;
	}

	public synchronized void segnaCombattimentoChiuso() {
		combattimentoAperto = false;
	}

	/**
	 * Il gruppo ha trovato un oggetto: vale finché non si prepara un'altra locazione.
	 */
	public synchronized void segnaOggettoTrovato() {
		oggettoTrovato = true;
	}

	public synchronized void segnaNuovaLocazione() {
		oggettoTrovato = false;
		combattimentoAperto = false;
	}

	/**
	 * Quanti scontri si sono visti da quando si gioca (serve alle tattiche).
	 */
	public synchronized int getScontriVisti() {
		return scontriVisti;
	}

	/**
	 * Le situazioni di adesso.
	 *
	 * @param comandi le icone che ci sono nella barra
	 * @param missioneAttiva se c'è una missione da mostrare nel riquadro delle missioni
	 * @param dito se si gioca con il dito
	 */
	public synchronized Set<Situazione> correnti(Collection<Comando> comandi, boolean missioneAttiva, boolean dito) {
		Set<Situazione> situazioni = EnumSet.noneOf(Situazione.class);
		if (!schermataDiGioco) {
			return situazioni;
		}
		if (primaSchermataDaDare) {
			situazioni.add(Situazione.PRIMA_SCHERMATA_DI_GIOCO);
		}
		if (!comandi.isEmpty()) {
			situazioni.add(Situazione.BARRA_DELLE_ICONE_VISIBILE);
		}
		if (comandi.containsAll(MOVIMENTO)) {
			situazioni.add(Situazione.ICONE_DI_MOVIMENTO_PRESENTI);
		}
		if (combattimentoAperto) {
			situazioni.add(Situazione.COMBATTIMENTO_APERTO);
		}
		if (oggettoTrovato) {
			situazioni.add(Situazione.OGGETTO_TROVATO);
		}
		if (comandi.contains(Comando.POZIONE_SALUTE) || comandi.contains(Comando.POZIONE_SALUTE_GRANDE)) {
			situazioni.add(Situazione.FERITO_CON_UNA_POZIONE);
		}
		if (comandi.contains(Comando.COMBATTIMENTO) && (comandi.contains(Comando.FUGA) || comandi.contains(Comando.PASSA_INOSSERVATO))) {
			situazioni.add(Situazione.SCELTE_IN_COMBATTIMENTO);
		}
		if (missioneAttiva) {
			situazioni.add(Situazione.MISSIONE_ATTIVA);
		}
		if (comandi.contains(Comando.MOSTRA_TATTICHE) && scontriVisti >= SCONTRI_PRIMA_DELLE_TATTICHE) {
			situazioni.add(Situazione.TATTICHE_DISPONIBILI);
		}
		if (comandi.contains(Comando.CORRUZIONE)) {
			situazioni.add(Situazione.AVVERSARIO_CORROMPIBILE);
		}
		if (comandi.contains(Comando.AMICIZIA)) {
			situazioni.add(Situazione.AVVERSARIO_AMICHEVOLE);
		}
		if (comandi.contains(Comando.INCANTESIMO)) {
			situazioni.add(Situazione.INCANTESIMO_LANCIABILE);
		}
		for (Comando bottega : BOTTEGHE) {
			if (comandi.contains(bottega)) {
				situazioni.add(Situazione.INGRESSO_IN_BOTTEGA);
				break;
			}
		}
		// Provvisorio: l'icona dell'accampamento compare quando cala la sera; un evento della notte la sostituirà
		if (comandi.contains(Comando.ACCAMPAMENTO)) {
			situazioni.add(Situazione.NOTTE);
		}
		if (dito) {
			situazioni.add(Situazione.DITO_SULLO_SCHERMO);
		}
		return situazioni;
	}
}
