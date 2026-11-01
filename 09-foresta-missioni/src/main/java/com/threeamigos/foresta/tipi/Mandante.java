package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;

import java.util.Locale;
import java.util.function.Supplier;
import com.threeamigos.foresta.testi.Testi;

/**
 * Chi chiede dei materiali in città (vedi RichiestaDiMateriali): l'alchimista, l'armaiolo, il capitano delle guardie, il
 * locandiere (i pesci e le rane delle paludi): da quale produzione di missioni.txt li pesca, come
 * compare nell'intermezzo e che cosa dice.
 */
public enum Mandante {

	ALCHIMISTA("RICHIESTA_ALCHIMISTA", "l'alchimista", ScenaInCitta::conAlchimista,
			Testi.formatta("missioni", "Mandante.s1"),
			Testi.formatta("missioni", "Mandante.s2")),
	ARMAIOLO("RICHIESTA_ARMAIOLO", "l'armaiolo", ScenaInCitta::conArmaiolo,
			Testi.formatta("missioni", "Mandante.s3"),
			Testi.formatta("missioni", "Mandante.s4")),
	CAPITANO("RICHIESTA_CAPITANO", Testi.formatta("missioni", "Mandante.s5"), ScenaInCitta::conCapitano,
			Testi.formatta("missioni", "Mandante.s6"),
			Testi.formatta("missioni", "Mandante.s7")),
	LOCANDIERE("RICHIESTA_LOCANDIERE", "il locandiere", ScenaInCitta::conLocandiere,
			Testi.formatta("missioni", "Mandante.s8"),
			Testi.formatta("missioni", "Mandante.s9"));

	private final String produzione;
	private final String nome;
	private final Supplier<ScenaInCitta> scena;
	private final String apertura;
	private final String allaConsegna;

	Mandante(String produzione, String nome, Supplier<ScenaInCitta> scena, String apertura, String allaConsegna) {
		this.produzione = produzione;
		this.nome = nome;
		this.scena = scena;
		this.apertura = apertura;
		this.allaConsegna = allaConsegna;
	}

	/**
	 * La produzione di missioni.txt da cui il mandante pesca il materiale.
	 */
	public String getProduzione() {
		return produzione;
	}

	/**
	 * Il mandante, con l'articolo: "l'alchimista".
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Una scena nuova con questo mandante, per l'intermezzo dell'incarico.
	 */
	public ScenaInCitta nuovaScena() {
		return scena.get();
	}

	/**
	 * La prima battuta del mandante nell'intermezzo dell'incarico. Il capitano si presenta anche con il suo nome, se c'è
	 * ("Sono il capitano delle guardie, Rodolfo, e ho bisogno di prove").
	 */
	public String getApertura(String nomeProprio) {
		return String.format(Locale.ROOT, apertura, nomeProprio == null ? "," : ", " + nomeProprio + ",");
	}

	/**
	 * Che cosa fa dei materiali che riceve, con il pronome: "che le annusa soddisfatto".
	 */
	public String allaConsegna(String pronome) {
		return String.format(Locale.ROOT, allaConsegna, pronome);
	}
}
