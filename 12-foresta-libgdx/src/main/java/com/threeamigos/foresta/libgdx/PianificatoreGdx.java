package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.Pianificatore;

import java.util.ArrayList;
import java.util.List;

/**
 * Il {@link Pianificatore} di libGDX: le azioni scattano a ogni fotogramma, sul thread di rendering, quando è passato il loro tempo. Chi
 * ospita l'interfaccia chiama {@link #aggiorna()} una volta per fotogramma (da <code>render</code>); così nessuna azione gira su un thread a parte.
 */
public final class PianificatoreGdx implements Pianificatore {

	private static final class Compito implements Pianificata {
		long scadenza;
		final long periodoInNanosecondi;
		final Runnable azione;
		boolean attivo = true;

		Compito(long scadenza, long periodoInNanosecondi, Runnable azione) {
			this.scadenza = scadenza;
			this.periodoInNanosecondi = periodoInNanosecondi;
			this.azione = azione;
		}

		@Override
		public void annulla() {
			attivo = false;
		}

		@Override
		public boolean isAttiva() {
			return attivo;
		}
	}

	private final List<Compito> compiti = new ArrayList<>();

	@Override
	public Pianificata dopo(int millisecondi, Runnable azione) {
		return aggiungi(new Compito(System.nanoTime() + millisecondi * 1_000_000L, 0, azione));
	}

	@Override
	public Pianificata ogni(int millisecondi, Runnable azione) {
		return aggiungi(new Compito(System.nanoTime() + millisecondi * 1_000_000L, millisecondi * 1_000_000L, azione));
	}

	private Pianificata aggiungi(Compito compito) {
		compiti.add(compito);
		return compito;
	}

	/**
	 * Fa scattare le azioni il cui tempo è passato; da chiamare una volta per fotogramma
	 */
	public void aggiorna() {
		long ora = System.nanoTime();
		for (Compito compito : new ArrayList<>(compiti)) {
			if (!compito.attivo) {
				compiti.remove(compito);
				continue;
			}
			if (ora >= compito.scadenza) {
				if (compito.periodoInNanosecondi > 0) {
					compito.scadenza = ora + compito.periodoInNanosecondi;
				} else {
					compito.attivo = false;
				}
				compito.azione.run();
			}
		}
	}
}
