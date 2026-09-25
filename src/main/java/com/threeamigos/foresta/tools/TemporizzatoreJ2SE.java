package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.motore.Temporizzabile;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TemporizzatoreJ2SE implements Temporizzatore {

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	});

	private ScheduledFuture<?> timerTask;
	// Cambia a ogni inizia/iniziaDopo/termina. cancel() ferma i prossimi impulsi ma non quelli gia' accodati
	// sull'EDT con invokeLater: ognuno ricorda la pianificazione da cui viene e, se non e' piu' quella corrente,
	// non fa nulla. Cosi' nessun TIMER arriva all'automa dopo un cambio di stato.
	private final AtomicInteger pianificazione = new AtomicInteger();
	private Temporizzabile temporizzabile;

	public void setTemporizzabile(Temporizzabile temporizzabile) {
		this.temporizzabile = temporizzabile;
	}

	public void inizia(int millisecondi) {
		pianifica(0, millisecondi);
	}

	public void iniziaDopo(int millisecondi) {
		pianifica(millisecondi, millisecondi);
	}

	private void pianifica(int ritardoIniziale, int millisecondi) {
		if (timerTask != null) {
			timerTask.cancel(false);
		}
		int questaPianificazione = pianificazione.incrementAndGet();
		timerTask = executor.scheduleWithFixedDelay(
			// Il tick va eseguito sull'EDT: processaComando() pubblica eventi via BusEventi, che
			// se chiamato da un thread diverso dall'EDT li accoda con invokeLater. Questo apre una
			// finestra in cui lo stato di gioco è già cambiato (es. un personaggio è morto) ma la
			// UI non l'ha ancora saputo, causando artefatti visivi (es. flicker alla morte di un mostro).
			() -> SwingUtilities.invokeLater(() -> {
				if (pianificazione.get() != questaPianificazione) {
					return;
				}
				try {
					temporizzabile.tick();
				} catch (RuntimeException e) {
					BusEventi.pubblica(new InternoException(e));
				}
			}),
                ritardoIniziale,
                millisecondi,
			TimeUnit.MILLISECONDS
		);
	}

	public void termina() {
		pianificazione.incrementAndGet();
		if (timerTask != null) {
			timerTask.cancel(false);
		}
	}
}
