package com.threeamigos.foresta.ui.testo;

/**
 * I caratteri delle immagini dei font Doomdark (vedi {@link ImmagineDelFont}), nell'ordine in cui compaiono: tre
 * righe, le maiuscole, le minuscole e tutti gli altri. Sono quelli dei font più le lettere delle lingue europee con
 * alfabeto latino: italiano, francese, tedesco, norvegese, islandese; spagnolo, portoghese, polacco; ceco, slovacco,
 * croato, sloveno, ungherese, rumeno, turco; lettone, lituano; maltese, gallese, sami. Le immagini non dicono quale
 * carattere sta in quale riquadro: lo dice la posizione, quindi chi le disegna e chi le legge devono usare questo elenco.
 */
public final class CaratteriDelFont {

	public static final String MAIUSCOLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "ÀÁÂÄÅÆÇÐÈÉÊËÌÍÎÏÒÓÔÖØŒÙÚÛÜÝÞŸ"
			+ "ÑÃÕĄĆĘŁŃŚŹŻ"
			+ "ČĎĚŇŘŠŤŮŽĹĽŔŐŰĂȘȚĞİĐ"
			+ "ĀĒĢĪĶĻŅŪĖĮŲĊĠĦŴŶŊŦ";
	public static final String MINUSCOLE = "abcdefghijklmnopqrstuvwxyz"
			+ "àáâäåæçðèéêëìíîïòóôöøœùúûüýþÿ"
			+ "ñãõąćęłńśźż"
			+ "čďěňřšťůžĺľŕőűășțğıđ"
			+ "āēģīķļņūėįųċġħŵŷŋŧß";
	// Cifre, spazio e la punteggiatura che i font hanno, poi quella che manca (virgolette dei testi francesi, tedeschi,
	// norvegesi, polacchi e cechi, punti esclamativo e interrogativo rovesciati, ordinali, trattini, puntini, grado)
	public static final String ALTRI = "0123456789 '\"“,.:;-+/?!()%" + "«»„”‘’‚‹›" + "¿¡ªº–—…°·";

	/**
	 * Le righe dell'immagine, dall'alto in basso
	 */
	public static final String[] RIGHE = {MAIUSCOLE, MINUSCOLE, ALTRI};

	private CaratteriDelFont() {
	}

	/**
	 * Quanti riquadri ha la riga più lunga: tanti quante sono le colonne dell'immagine
	 */
	public static int colonne() {
		int colonne = 0;
		for (String riga : RIGHE) {
			colonne = Math.max(colonne, riga.length());
		}
		return colonne;
	}
}
