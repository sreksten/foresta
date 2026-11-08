package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoSuggerimento;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;

import com.threeamigos.foresta.strumenti.logger.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Chi decide, a ogni cambiamento della UI, se mostrare un suggerimento del tutorial: la UI gli dice che cosa vede ({@link #aggiorna}), lui
 * chiede al {@link RegistroSuggerimenti} se c'è qualcosa da spiegare e tiene aperto il fumetto finché il giocatore non preme il pollice
 * ({@link #pollice}) o lo spegne ({@link #annulla}). Mai due fumetti insieme: gli altri aspettano, uno dopo l'altro, in un ordine fisso.
 * <p>
 * Non disegna e non pubblica eventi. Chi disegna legge {@link #getFumetto} e {@link #isBordoAcceso}; lo schermo conta il fumetto aperto fra le
 * cose che tengono occupata la UI (finché è aperto il motore aspetta: vedi {@code SchermoDelGioco.fineFotogramma}).
 *
 * @author Stefano Reksten
 */
public final class TutorDelGioco {

	/** Il bordo del bersaglio lampeggia: acceso per questo tempo, poi spento per altrettanto */
	static final long MEZZO_PERIODO_DEL_LAMPEGGIO_MS = 400L;

	private FumettoDelTutorial fumetto;
	// A che pagina si era arrivati in un suggerimento lasciato a metà (si è cambiata schermata): non si tiene su disco, ma finché si gioca si riparte da lì
	private final Map<TipoSuggerimento, Integer> pagineRaggiunte = new EnumMap<>(TipoSuggerimento.class);
	private boolean dito;
	private boolean abilitato;

	/**
	 * Il toolkit dice che sa disegnare il fumetto e ascoltare il pollice e l'annulla: finché non lo dice non si apre niente (un fumetto che non
	 * si può chiudere terrebbe ferma la UI per sempre).
	 */
	public synchronized void setAbilitato(boolean abilitato) {
		this.abilitato = abilitato;
		// Anche il motore lo deve sapere: la partita si pilota solo se i suggerimenti si possono leggere
		RegistroSuggerimenti.setMostrabile(abilitato);
		if (!abilitato) {
			fumetto = null;
		}
	}

	/**
	 * Se si gioca con il dito: i testi cambiano (trascinare invece del mouse, tenere premuto per la descrizione).
	 */
	public synchronized void setDito(boolean dito) {
		this.dito = dito;
	}

	public synchronized boolean isDito() {
		return dito;
	}

	/**
	 * Il fumetto aperto, o niente.
	 */
	public synchronized Optional<FumettoDelTutorial> getFumetto() {
		return Optional.ofNullable(fumetto);
	}

	public synchronized boolean isAperto() {
		return fumetto != null;
	}

	/**
	 * La UI ha cambiato qualcosa: se non c'è già un fumetto aperto e il momento lo consente, apre il primo suggerimento ancora da vedere che scatta
	 * in una di queste situazioni.
	 *
	 * @param situazioni che cosa vede la UI adesso
	 * @param apribile dice se un suggerimento si può aprire adesso: la schermata è quella giusta e ciò a cui punta è già a schermo (un suggerimento che
	 *                 punta a un'icona che non c'è aspetta che compaia); falso per tutti durante un intermezzo o nel mezzo di un'azione che chiede attenzione
	 * @return il fumetto appena aperto, se ce n'è uno
	 */
	public synchronized Optional<FumettoDelTutorial> aggiorna(Set<Situazione> situazioni, Predicate<TipoSuggerimento> apribile) {
		if (!abilitato || fumetto != null) {
			return Optional.empty();
		}
		for (TipoSuggerimento suggerimento : RegistroSuggerimenti.candidati(situazioni)) {
			if (suggerimento.getNumeroPagine() > 0 && apribile.test(suggerimento)) {
				fumetto = new FumettoDelTutorial(suggerimento, dito, pagineRaggiunte.getOrDefault(suggerimento, 1));
				Logger.log(() -> "Tutorial: apre " + suggerimento + ", pagina " + fumetto.getPagina() + " di " + fumetto.getNumeroPagine());
				return Optional.of(fumetto);
			}
		}
		return Optional.empty();
	}

	/**
	 * Dice subito una cosa con la finestra del tutorial (vedi NotificaMessaggioDelTutorial), qualunque cosa sia già stata vista: apre il messaggio, che ha una
	 * pagina sola, con i valori dei suoi segnaposto. Non fa niente se il toolkit non sa mostrare i suggerimenti o il tutorial è spento.
	 *
	 * @return il fumetto aperto, se si è aperto
	 */
	public synchronized Optional<FumettoDelTutorial> mostra(TipoSuggerimento suggerimento, java.util.List<String> parametri) {
		if (!abilitato || !RegistroSuggerimenti.isAttivo()) {
			return Optional.empty();
		}
		fumetto = new FumettoDelTutorial(suggerimento, dito, parametri);
		Logger.log(() -> "Tutorial: dice " + suggerimento + " " + parametri);
		return Optional.of(fumetto);
	}

	/**
	 * Il giocatore ha premuto il pollice: avanti di una pagina, e sull'ultima «ho capito». Il suggerimento è visto e non si ripresenta.
	 *
	 * @return vero se il fumetto si è chiuso
	 */
	public synchronized boolean pollice() {
		if (fumetto == null) {
			return false;
		}
		if (fumetto.prossimaPagina()) {
			Logger.log(() -> "Tutorial: " + fumetto.getSuggerimento() + ", pagina " + fumetto.getPagina());
			return false;
		}
		RegistroSuggerimenti.visto(fumetto.getSuggerimento());
		pagineRaggiunte.remove(fumetto.getSuggerimento());
		Logger.log(() -> "Tutorial: letto " + fumetto.getSuggerimento());
		fumetto = null;
		return true;
	}

	/**
	 * Il giocatore ha premuto l'annulla: il tutorial si spegne, tutto, e il fumetto si chiude. I suggerimenti già visti restano visti.
	 */
	public synchronized void annulla() {
		RegistroSuggerimenti.setAttivo(false);
		pagineRaggiunte.clear();
		fumetto = null;
	}

	/**
	 * Se in questo istante il bordo del bersaglio è acceso (lampeggia mentre il fumetto è aperto, e parte acceso).
	 */
	public synchronized boolean isBordoAcceso(long millisecondiDallApertura) {
		return fumetto != null && (Math.max(0L, millisecondiDallApertura) / MEZZO_PERIODO_DEL_LAMPEGGIO_MS) % 2 == 0;
	}

	/**
	 * Se adesso il bordo del bersaglio è acceso, contando dall'apertura del fumetto aperto.
	 */
	public synchronized boolean isBordoAcceso() {
		return fumetto != null && isBordoAcceso(fumetto.getMillisDallApertura());
	}

	/**
	 * Chiude il fumetto perché si è lasciata la sua schermata. Se ha più pagine ed era arrivato all'ultima è letto (l'ultima pagina è quella che manda a
	 * fare la cosa che ha spiegato, per esempio ad aprire la mappa: chi ci va l'ha capita; un suggerimento di una pagina sola, invece, non si sa se sia letto); altrimenti non si segna letto e si riproporrà tornando, dalla
	 * pagina a cui era arrivato.
	 */
	public synchronized void chiudi() {
		if (fumetto != null) {
			Logger.log(() -> "Tutorial: chiude " + fumetto.getSuggerimento() + " alla pagina " + fumetto.getPagina() + " (si è lasciata la sua schermata)");
			if (fumetto.getNumeroPagine() > 1 && fumetto.isUltimaPagina()) {
				RegistroSuggerimenti.visto(fumetto.getSuggerimento());
				pagineRaggiunte.remove(fumetto.getSuggerimento());
			} else {
				pagineRaggiunte.put(fumetto.getSuggerimento(), fumetto.getPagina());
			}
		}
		fumetto = null;
	}

	/**
	 * Chiude il fumetto e dimentica a che pagina erano i suggerimenti lasciati a metà (si cambia partita, o la UI si reinizializza): ripartiranno
	 * dall'inizio.
	 */
	public synchronized void chiudiEDimentica() {
		pagineRaggiunte.clear();
		fumetto = null;
	}
}
