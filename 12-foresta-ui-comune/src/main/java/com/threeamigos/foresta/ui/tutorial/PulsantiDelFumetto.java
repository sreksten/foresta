package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.SchermoDelGioco;

/**
 * Il pollice (avanti, «ho capito») e l'annulla (spegne il tutorial) del fumetto, senza toolkit: dove stavano nell'ultimo fotogramma disegnato e che
 * cosa succede se li si preme. Chi disegna li mette a posto a ogni fotogramma ({@link #posiziona}) o li toglie se il fumetto non c'è
 * ({@link #nascondi}); chi riceve i clic o i tocchi chiede {@link #suiPulsanti} e {@link #premi}.
 *
 * @author Stefano Reksten
 */
public final class PulsantiDelFumetto {

	private final SchermoDelGioco<?, ?, ?, ?> schermo;
	// Il disegno e l'input possono stare su thread diversi
	private volatile Area pollice;
	private volatile Area annulla;

	public PulsantiDelFumetto(SchermoDelGioco<?, ?, ?, ?> schermo) {
		this.schermo = schermo;
	}

	/**
	 * Mette i pulsanti al loro posto nel fumetto (vedi {@link DisposizioneDelFumetto#pollice} e {@link DisposizioneDelFumetto#annulla}).
	 */
	public void posiziona(Punto fumetto, int larghezza, int altezza, int lato) {
		pollice = DisposizioneDelFumetto.pollice(fumetto, larghezza, altezza, lato);
		annulla = DisposizioneDelFumetto.annulla(fumetto, larghezza, altezza, lato);
	}

	/**
	 * Il fumetto non c'è: i pulsanti non si possono premere.
	 */
	public void nascondi() {
		pollice = null;
		annulla = null;
	}

	/**
	 * Dove sta il pollice, o null se non c'è.
	 */
	public Area getPollice() {
		return pollice;
	}

	/**
	 * Dove sta l'annulla, o null se non c'è.
	 */
	public Area getAnnulla() {
		return annulla;
	}

	/**
	 * Se il punto sta su uno dei due pulsanti (anche solo premuto o rilasciato: il clic e quel che lo precede non devono arrivare al resto).
	 */
	public boolean suiPulsanti(int x, int y) {
		Area p = pollice;
		Area a = annulla;
		return (p != null && p.contiene(x, y)) || (a != null && a.contiene(x, y));
	}

	/**
	 * Un clic o un tocco finito lì: se è sul pollice o sull'annulla lo esegue.
	 *
	 * @return vero se era su un pulsante (e quindi non va oltre)
	 */
	public boolean premi(int x, int y) {
		Area p = pollice;
		Area a = annulla;
		if (p != null && p.contiene(x, y)) {
			schermo.pollicePremuto();
			return true;
		}
		if (a != null && a.contiene(x, y)) {
			schermo.annullaPremuto();
			return true;
		}
		return false;
	}
}
