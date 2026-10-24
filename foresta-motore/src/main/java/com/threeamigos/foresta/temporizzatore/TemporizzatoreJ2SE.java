package com.threeamigos.foresta.temporizzatore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Il temporizzatore basato sull'esecutore pianificato di Java: gli impulsi si pianificano su un thread a parte, ma ognuno viene eseguito sul thread principale
 * (vedi {@link ThreadPrincipale}). Si può sospendere e riprendere con il ciclo di vita dell'app ({@link #sospendi()}). Ogni impulso ricorda la pianificazione da cui viene: se nel frattempo è cambiata, l'impulso già accodato non fa niente, così
 * nessun impulso arriva dopo un cambio di stato.
 */
public class TemporizzatoreJ2SE implements Temporizzatore {

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	});

	private ScheduledFuture<?> timerTask;
	// Cambia a ogni inizia/iniziaDopo/termina. cancel() ferma i prossimi impulsi ma non quelli gia' accodati
	// sul thread principale con accoda: ognuno ricorda la pianificazione da cui viene e, se non e' piu' quella corrente,
	// non fa nulla. Cosi' nessun TIMER arriva all'automa dopo un cambio di stato.
	private final AtomicInteger pianificazione = new AtomicInteger();
	private Temporizzabile temporizzabile;
	// Gli impulsi falliti di fila (sul thread principale): una riuscita li azzera
	private static final int MAX_IMPULSI_FALLITI_DI_FILA = 3;
	private int impulsiFalliti;

	// Il ritmo dell'ultimo inizia/iniziaDopo, per riprenderlo dopo una sospensione; 0 se non ci sono impulsi da fare
	private int periodoAttivo;
	private boolean sospeso;

	public synchronized void setTemporizzabile(Temporizzabile temporizzabile) {
		this.temporizzabile = temporizzabile;
	}

	/**
	 * Avvia gli impulsi: il primo subito, poi uno ogni quei millisecondi.
	 */
	public synchronized void inizia(int millisecondi) {
		pianifica(0, millisecondi);
	}

	/**
	 * Avvia gli impulsi: il primo dopo quei millisecondi, poi uno ogni quei millisecondi.
	 */
	public synchronized void iniziaDopo(int millisecondi) {
		pianifica(millisecondi, millisecondi);
	}

	private void pianifica(int ritardoIniziale, int millisecondi) {
		fermaLaPianificazioneInCorso();
		impulsiFalliti = 0;
		periodoAttivo = millisecondi;
		if (sospeso) {
			// Il ritmo e' ricordato: gli impulsi partono alla ripresa
			return;
		}
		int questaPianificazione = pianificazione.get();
		timerTask = executor.scheduleWithFixedDelay(
			// Il tick va eseguito sul thread principale: processaComando() pubblica eventi via BusEventi, che
			// se chiamato da un thread diverso li accoda. Questo apre una
			// finestra in cui lo stato di gioco è già cambiato (es. un personaggio è morto) ma la
			// UI non l'ha ancora saputo, causando artefatti visivi (es. flicker alla morte di un mostro).
			() -> ThreadPrincipale.accoda(() -> {
				if (pianificazione.get() != questaPianificazione) {
					return;
				}
				try {
					temporizzabile.tick();
					impulsiFalliti = 0;
				} catch (RuntimeException e) {
					BusEventi.pubblica(new InternoException(e));
					// Un impulso che fallisce ogni secondo riempirebbe il log e infastidirebbe il giocatore con lo stesso errore: dopo qualche
					// fallimento di fila gli impulsi si fermano (i comandi del giocatore continuano ad arrivare)
					if (++impulsiFalliti >= MAX_IMPULSI_FALLITI_DI_FILA) {
						BusEventi.pubblica(new InternoException("Impulsi fermati dopo " + impulsiFalliti + " errori di fila", e));
						termina();
					}
				}
			}),
                ritardoIniziale,
                millisecondi,
			TimeUnit.MILLISECONDS
		);
	}

	// Cancella i prossimi impulsi e rende non piu' validi quelli gia' accodati
	private void fermaLaPianificazioneInCorso() {
		pianificazione.incrementAndGet();
		if (timerTask != null) {
			timerTask.cancel(false);
			timerTask = null;
		}
	}

	/**
	 * Ferma gli impulsi, anche quelli già accodati.
	 */
	public synchronized void termina() {
		fermaLaPianificazioneInCorso();
		periodoAttivo = 0;
	}

	/**
	 * Ferma gli impulsi, anche quelli già accodati, ricordando il ritmo per {@link #riprendi()}.
	 */
	public synchronized void sospendi() {
		sospeso = true;
		fermaLaPianificazioneInCorso();
	}

	/**
	 * Rimette in marcia gli impulsi sospesi, con lo stesso ritmo e il primo dopo un periodo intero.
	 */
	public synchronized void riprendi() {
		if (!sospeso) {
			return;
		}
		sospeso = false;
		if (periodoAttivo > 0) {
			pianifica(periodoAttivo, periodoAttivo);
		}
	}
}
