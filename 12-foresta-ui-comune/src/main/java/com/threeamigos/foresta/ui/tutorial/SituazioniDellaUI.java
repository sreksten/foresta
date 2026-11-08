package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.StatoDelloSchermo;

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

	private boolean sceltaDellaClasse;
	private boolean schermataDiGioco;
	private boolean primaSchermataDaDare;
	private boolean combattimentoAperto;
	private int scontriVisti;
	private boolean oggettoTrovato;
	private StatoDelloSchermo schermata = StatoDelloSchermo.IN_GIOCO;
	private boolean puntiAbilitaDaSpendere;
	private boolean joystickSullaMappa;
	private boolean mappaPiuGrandeDelloSchermo;

	/**
	 * Si sta scegliendo la classe del personaggio (a inizio partita, prima della schermata di gioco), o si ha finito.
	 */
	public synchronized void segnaSceltaDellaClasse(boolean inCorso) {
		sceltaDellaClasse = inCorso;
	}

	/**
	 * La schermata di gioco è comparsa: la prima volta è una situazione a sé (i riquadri).
	 */
	public synchronized void segnaSchermataDiGioco() {
		if (!schermataDiGioco) {
			primaSchermataDaDare = true;
		}
		schermataDiGioco = true;
		sceltaDellaClasse = false;
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

	/**
	 * Quale schermata si vede. Se è la mappa a tutto schermo, dice anche se ha il joystick virtuale e se è più grande dello spazio che ha sullo
	 * schermo (negli altri casi i due valori non contano).
	 */
	public synchronized void segnaLaSchermata(StatoDelloSchermo nuova, boolean mappaConJoystick, boolean mappaPiuGrande) {
		schermata = nuova;
		joystickSullaMappa = mappaConJoystick;
		mappaPiuGrandeDelloSchermo = mappaPiuGrande;
	}

	/**
	 * Se qualcuno del gruppo ha punti abilità da spendere (conta solo con l'inventario aperto).
	 */
	public synchronized void segnaPuntiAbilita(boolean daSpendere) {
		puntiAbilitaDaSpendere = daSpendere;
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
	 * @param feritoConUnaPozione se qualcuno del gruppo è ferito e c'è una pozione di salute da dargli (le icone delle pozioni non bastano: in
	 *                            combattimento ci sono anche a chi sta bene)
	 */
	public synchronized Set<Situazione> correnti(Collection<Comando> comandi, boolean missioneAttiva, boolean dito, boolean feritoConUnaPozione) {
		Set<Situazione> situazioni = EnumSet.noneOf(Situazione.class);
		if (sceltaDellaClasse) {
			situazioni.add(Situazione.SCELTA_DELLA_CLASSE);
		}
		if (!schermataDiGioco) {
			return situazioni;
		}
		if (primaSchermataDaDare) {
			situazioni.add(Situazione.PRIMA_SCHERMATA_DI_GIOCO);
		}
		if (!comandi.isEmpty()) {
			situazioni.add(Situazione.BARRA_DELLE_ICONE_VISIBILE);
		}
		if (comandi.contains(Comando.MAPPA)) {
			situazioni.add(Situazione.ICONA_DELLA_MAPPA_PRESENTE);
		}
		if (schermata == StatoDelloSchermo.INVENTARIO) {
			situazioni.add(Situazione.INVENTARIO_APERTO);
			if (puntiAbilitaDaSpendere) {
				situazioni.add(Situazione.PUNTI_ABILITA_DA_SPENDERE);
			}
		}
		if (schermata == StatoDelloSchermo.MAPPA) {
			if (joystickSullaMappa) {
				situazioni.add(Situazione.JOYSTICK_SULLA_MAPPA);
			}
			if (mappaPiuGrandeDelloSchermo) {
				situazioni.add(Situazione.MAPPA_PIU_GRANDE_DELLO_SCHERMO);
			}
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
		if (feritoConUnaPozione) {
			situazioni.add(Situazione.FERITO_CON_UNA_POZIONE);
		}
		if (comandi.contains(Comando.COMBATTIMENTO) && (comandi.contains(Comando.FUGA) || comandi.contains(Comando.PASSA_INOSSERVATO))) {
			situazioni.add(Situazione.SCELTE_IN_COMBATTIMENTO);
		}
		if (missioneAttiva) {
			situazioni.add(Situazione.MISSIONE_ATTIVA);
		}
		// Le tattiche si aprono dall'inventario, la cui icona è già nella barra: il suggerimento aspetta solo i due scontri (e che l'icona si veda)
		if (scontriVisti >= SCONTRI_PRIMA_DELLE_TATTICHE) {
			situazioni.add(Situazione.TATTICHE_DISPONIBILI);
		}
		// Le icone della corruzione e dell'amicizia compaiono solo se fra gli avversari c'è chi si può corrompere (e il gruppo ha monete) o chi è
		// amichevole: anche qui la barra basta
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
		// L'icona dell'accampamento compare esattamente quando ci si può accampare: di notte, con più di un personaggio, fuori da città e castelli.
		// Non serve un evento: la barra lo dice già
		if (comandi.contains(Comando.ACCAMPAMENTO)) {
			situazioni.add(Situazione.NOTTE);
		}
		if (dito) {
			situazioni.add(Situazione.DITO_SULLO_SCHERMO);
		}
		return situazioni;
	}
}
