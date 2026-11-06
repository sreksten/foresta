package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.Gdx;
import com.threeamigos.foresta.strumenti.temporizzatore.ThreadPrincipale;

import java.util.ArrayList;
import java.util.List;

/**
 * Il thread principale in libGDX: quello di rendering, su cui girano <code>create</code>, <code>render</code> e i
 * <code>Runnable</code> passati a <code>Gdx.app.postRunnable</code> (al posto dell'Event Dispatch Thread di Swing, vedi
 * <code>EsecutoreSwing</code>).
 * <p>
 * Il thread di rendering esiste solo quando l'applicazione è partita: fino ad allora (per esempio gli eventi pubblicati dal
 * punto d'ingresso mentre collega il motore) il lavoro resta in attesa, e parte, nell'ordine, quando l'applicazione chiama
 * {@link #avviato()} dal suo <code>create</code>.
 */
public final class EsecutoreGdx implements ThreadPrincipale.Esecutore {

	private final List<Runnable> inAttesa = new ArrayList<>();
	private volatile Thread threadDiRendering;

	@Override
	public boolean eIlThreadCorrente() {
		return Thread.currentThread() == threadDiRendering;
	}

	@Override
	public void accoda(Runnable operazione) {
		synchronized (inAttesa) {
			if (threadDiRendering == null) {
				inAttesa.add(operazione);
				return;
			}
		}
		Gdx.app.postRunnable(operazione);
	}

	/**
	 * Da chiamare nel <code>create</code> dell'applicazione: il thread corrente diventa quello principale, e il lavoro rimasto in
	 * attesa si esegue subito, nell'ordine in cui era stato accodato.
	 */
	public void avviato() {
		List<Runnable> daEseguire;
		synchronized (inAttesa) {
			threadDiRendering = Thread.currentThread();
			daEseguire = new ArrayList<>(inAttesa);
			inAttesa.clear();
		}
		for (Runnable operazione : daEseguire) {
			operazione.run();
		}
	}
}
