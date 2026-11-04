package com.threeamigos.foresta.testi;

import java.util.List;

/**
 * La grammatica di una lingua: gli articoli, le preposizioni articolate, i numeri in lettere, gli elenchi. È la sola parte
 * del gioco che ha una classe per lingua: tutto il resto sono testi nei file {@code .properties} (vedi {@link Testi}).
 * Il codice chiama le regole della lingua in uso ({@link Lingue#corrente()}) e non sa quale sia.
 * <p>
 * Gli articoli si danno già col loro separatore: con lo spazio ("il ", "the ") o senza se la lingua elide ("l'"), così si
 * mettono davanti al nome con una concatenazione e basta.
 */
public interface RegoleDellaLingua {

	/**
	 * L'articolo determinativo di un nome, singolare o plurale ("il ", "gli ", "the "), col suo separatore.
	 */
	String articoloDeterminativo(Nome nome, boolean plurale);

	/**
	 * L'articolo indeterminativo ("un ", "uno ", "a ", "an "); al plurale quello partitivo ("alcuni ", "some ").
	 */
	String articoloIndeterminativo(Nome nome, boolean plurale);

	/**
	 * La preposizione con l'articolo determinativo del nome, fusi se la lingua lo fa: "del ", "dello ", "of the ".
	 */
	String preposizioneArticolata(Preposizione preposizione, Nome nome, boolean plurale);

	/**
	 * Chi compie un'azione ("ucciso da un goblin", "ucciso dal Drago"): con l'indeterminativo di un nome qualunque ("da un
	 * ", "by a "), con il determinativo di uno unico o noto ({@link Nome#isAgenteDeterminato()}: "dal ", "by the ").
	 */
	String daAgente(Nome nome);

	/**
	 * Come {@link #preposizioneArticolata} ma su un nome già composto: «in» + «il Tempio del Sole» dà «nel Tempio del Sole»,
	 * «a» + «l'Eremo» dà «all'Eremo»; se il nome non comincia con un articolo la preposizione resta staccata.
	 */
	String conPreposizione(Preposizione preposizione, String nomeConArticolo);

	/**
	 * Il nome senza l'articolo con cui comincia, se ce l'ha ("lo Scudo Fiscale" dà "Scudo Fiscale", "the Fiscal Shield" dà "Fiscal Shield",
	 * "l'Anello" dà "Anello"); un nome che non comincia con un articolo resta com'è.
	 */
	String senzaArticolo(String nomeConArticolo);

	/**
	 * Il testo con la prima lettera maiuscola.
	 */
	String inizialeMaiuscola(String testo);

	/**
	 * Il numero in lettere (fino a dieci; oltre resta in cifre), concordato nel genere dove la lingua lo chiede.
	 */
	String cardinale(int numero, Genere genere);

	/**
	 * L'ordinale in lettere ("primo", "third"), con l'articolo davanti se richiesto; vale da 1 a 41.
	 */
	String ordinale(int numero, Genere genere, boolean conArticolo);

	/**
	 * Gli elementi di un elenco come si scrive nella lingua: "A, B e C", "A, B, and C".
	 */
	String elenco(List<String> elementi);

	/**
	 * Il suono con cui comincia il testo, per scegliere l'articolo quando il nome non dichiara il suo.
	 */
	Suono suonoIniziale(String testo);
}
