package com.threeamigos.foresta.tipi;

/**
 * Gli oggetti che si trovano nelle locazioni, con quanti se ne trovano al massimo e il loro valore. L'oggetto vero lo
 * costruisce oggetti.FabbricaOggetti, ma solo per i tipi generabili: gli artefatti e gli oggetti delle missioni
 * nascono altrove.
 */
public enum TipoOggetto {

	ANELLO(1, 100),
	COFANO(1, 150),
	CORONA(2, 100),
	PIETRA_PREZIOSA(2, 75),
	MONETA(2, 50),
	SCUDO(1, 100),
	SPADA(1, 100),
	SPADONE(1, 100),
	ELMO(1, 100),
	MASCHERA(1, 100),
	ARMATURA(1, 100),
	SCHINIERI(1, 100),
	// Gli artefatti non vanno mai restituiti tra gli oggetti che una locazione può nascondere!
	ARTEFATTO(1, 100, false),
	// Neanche gli oggetti delle missioni: li mettono nelle locazioni le missioni (vedi OggettoMissione)
	OGGETTO_MISSIONE(1, 50, false);

	private final int quantitaMassima;
	private final int valore;
	private final boolean generabile;

	TipoOggetto(int quantitaMassima, int valore) {
		this(quantitaMassima, valore, true);
	}

	TipoOggetto(int quantitaMassima, int valore, boolean generabile) {
		this.quantitaMassima = quantitaMassima;
		this.valore = valore;
		this.generabile = generabile;
	}

	/**
	 * Se se ne può creare un'istanza qualsiasi con FabbricaOggetti: non per gli artefatti e gli oggetti delle
	 * missioni, che nascono altrove.
	 */
	public boolean isGenerabile() {
		return generabile;
	}

	public final int getQuantitaMassima() {
		return quantitaMassima;
	}

	public final int getValore() {
		return valore;
	}
}
