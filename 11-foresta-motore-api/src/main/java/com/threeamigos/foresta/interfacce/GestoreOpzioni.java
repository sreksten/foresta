package com.threeamigos.foresta.interfacce;

/**
 * Le opzioni del gioco che valgono da una partita all'altra e si cambiano dalla schermata delle opzioni (dall'intro): la lingua. Le crea chi avvia
 * il gioco (GestoreOpzioniSuFile) e le danno all'Automa, che le cambia, e all'interfaccia. Gli argomenti di avvio (per esempio {@code LINGUA=})
 * vincono su quel che c'è scritto qui.
 */
public interface GestoreOpzioni {

	/**
	 * Le rilegge da dove stanno; dice se ci è riuscito (se non ci sono ancora, sì: valgono quelle predefinite)
	 */
	boolean carica();

	/**
	 * Il codice della lingua scelta ({@code it}, {@code en}, {@code en-GB}), o null se non la si è mai scelta
	 */
	String getLingua();

	void setLingua(String codice);

	/**
	 * Le scrive dove stanno; dice se ci è riuscito
	 */
	boolean salva();

	/**
	 * Opzioni che non si scrivono da nessuna parte: per i test, e per chi non ha dove scriverle
	 */
	final class InMemoria implements GestoreOpzioni {
		private String lingua;

		@Override
		public boolean carica() {
			return true;
		}

		@Override
		public String getLingua() {
			return lingua;
		}

		@Override
		public void setLingua(String codice) {
			lingua = codice;
		}

		@Override
		public boolean salva() {
			return true;
		}
	}
}
