package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.tipi.PosizioneDellaBarra;

/**
 * Le opzioni del gioco che valgono da una partita all'altra e si cambiano dalla schermata delle opzioni (dall'intro): la lingua e la posizione
 * della barra dei comandi. Le crea chi avvia
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
	 * Dove sta la barra dei comandi, o null se non la si è mai scelta (la decide l'interfaccia)
	 */
	PosizioneDellaBarra getPosizioneDellaBarra();

	void setPosizioneDellaBarra(PosizioneDellaBarra posizione);

	/**
	 * Le scrive dove stanno; dice se ci è riuscito
	 */
	boolean salva();

	/**
	 * Opzioni che non si scrivono da nessuna parte: per i test, e per chi non ha dove scriverle
	 */
	final class InMemoria implements GestoreOpzioni {
		private String lingua;
		private PosizioneDellaBarra posizioneDellaBarra;

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
		public PosizioneDellaBarra getPosizioneDellaBarra() {
			return posizioneDellaBarra;
		}

		@Override
		public void setPosizioneDellaBarra(PosizioneDellaBarra posizione) {
			posizioneDellaBarra = posizione;
		}

		@Override
		public boolean salva() {
			return true;
		}
	}
}
