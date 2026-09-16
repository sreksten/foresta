package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoException;
import com.threeamigos.foresta.motore.Temporizzabile;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TemporizzatoreJ2SE implements Temporizzatore {

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	});

	private ScheduledFuture<?> timerTask;
	private Temporizzabile temporizzabile;

	public void setTemporizzabile(Temporizzabile temporizzabile) {
		this.temporizzabile = temporizzabile;
	}

	public void inizia(int millisecondi) {
		if (timerTask != null) {
			timerTask.cancel(false);
		}
		timerTask = executor.scheduleWithFixedDelay(
			() -> {
				try {
					temporizzabile.tick();
				} catch (RuntimeException e) {
					BusEventi.pubblica(new EventoException(e));
				}
			},
                millisecondi,
                millisecondi,
			TimeUnit.MILLISECONDS
		);
	}

	public void termina() {
		if (timerTask != null) {
			timerTask.cancel(false);
		}
	}
}
