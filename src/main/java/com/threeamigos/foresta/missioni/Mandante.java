package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;

import java.util.function.Supplier;

/**
 * Chi chiede dei materiali in città (vedi RichiestaDiMateriali): da quale produzione di missioni.txt li pesca, come
 * compare nell'intermezzo e che cosa dice.
 */
public enum Mandante {

	ALCHIMISTA("RICHIESTA_ALCHIMISTA", "l'alchimista", ScenaInCitta::conAlchimista,
			"Buongiorno, viaggiatori. Non è che andate in giro per la foresta?",
			"che %s annusa soddisfatto"),
	ARMAIOLO("RICHIESTA_ARMAIOLO", "l'armaiolo", ScenaInCitta::conArmaiolo,
			"Ehi, voi! Mi serve del materiale per la forgia, e voi avete l'aria di chi non ha paura di sporcarsi.",
			"che %s soppesa con l'occhio del mestiere"),
	CAPITANO("RICHIESTA_CAPITANO", "il capitano delle guardie", ScenaInCitta::conCapitano,
			"Voi avete l'aria di chi sa usare una spada. Sono il capitano delle guardie, e ho bisogno di prove.",
			"che %s conta per bene");

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

	public ScenaInCitta nuovaScena() {
		return scena.get();
	}

	/**
	 * La prima battuta del mandante nell'intermezzo dell'incarico.
	 */
	public String getApertura() {
		return apertura;
	}

	/**
	 * Che cosa fa dei materiali che riceve, con il pronome: "che le annusa soddisfatto".
	 */
	public String allaConsegna(String pronome) {
		return String.format(allaConsegna, pronome);
	}
}
