package com.threeamigos.foresta.ui;

import javax.swing.Timer;

/**
 * Il {@link Pianificatore} di Swing: un <code>javax.swing.Timer</code>, che scatta sul thread di Swing come gli eventi del mouse.
 */
public final class PianificatoreSwing implements Pianificatore {

	@Override
	public Pianificata dopo(int millisecondi, Runnable azione) {
		Timer timer = new Timer(millisecondi, evento -> azione.run());
		timer.setRepeats(false);
		timer.start();
		return di(timer);
	}

	@Override
	public Pianificata ogni(int millisecondi, Runnable azione) {
		Timer timer = new Timer(millisecondi, evento -> azione.run());
		timer.start();
		return di(timer);
	}

	private static Pianificata di(Timer timer) {
		return new Pianificata() {
			@Override
			public void annulla() {
				timer.stop();
			}

			@Override
			public boolean isAttiva() {
				return timer.isRunning();
			}
		};
	}
}
