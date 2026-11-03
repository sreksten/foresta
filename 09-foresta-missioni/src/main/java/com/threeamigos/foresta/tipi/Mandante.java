package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;

import java.util.Locale;
import java.util.function.Supplier;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.Testi;

/**
 * Chi chiede dei materiali in città (vedi RichiestaDiMateriali): l'alchimista, l'armaiolo, il capitano delle guardie, il
 * locandiere (i pesci e le rane delle paludi): da quale produzione di missioni.txt li pesca, come
 * compare nell'intermezzo e che cosa dice.
 */
public enum Mandante {

	ALCHIMISTA("RICHIESTA_ALCHIMISTA", "MANDANTE_NOME_ALCHIMISTA", ScenaInCitta::conAlchimista, "MANDANTE_APERTURA_ALCHIMISTA", "MANDANTE_ALLA_CONSEGNA_ALCHIMISTA"),
	ARMAIOLO("RICHIESTA_ARMAIOLO", "MANDANTE_NOME_ARMAIOLO", ScenaInCitta::conArmaiolo, "MANDANTE_APERTURA_ARMAIOLO", "MANDANTE_ALLA_CONSEGNA_ARMAIOLO"),
	CAPITANO("RICHIESTA_CAPITANO", "MANDANTE_NOME_CAPITANO", ScenaInCitta::conCapitano, "MANDANTE_APERTURA_CAPITANO", "MANDANTE_ALLA_CONSEGNA_CAPITANO"),
	LOCANDIERE("RICHIESTA_LOCANDIERE", "MANDANTE_NOME_LOCANDIERE", ScenaInCitta::conLocandiere, "MANDANTE_APERTURA_LOCANDIERE", "MANDANTE_ALLA_CONSEGNA_LOCANDIERE");

	private final String produzione;
	// Le chiavi dei testi in missioni_<lingua>.properties: si leggono quando servono, nella lingua in uso
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
		return ProduttoreDiMissioniCasuale.testoDellaMissione(nome);
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
		return ProduttoreDiMissioniCasuale.testoDellaMissione(apertura, "PARAM_NOME_PROPRIO", nomeProprio == null ? "," : ", " + nomeProprio + ",");
	}

	/**
	 * Che cosa fa dei materiali che riceve, con il pronome: "che le annusa soddisfatto".
	 */
	public String allaConsegna(String pronome) {
		return ProduttoreDiMissioniCasuale.testoDellaMissione(allaConsegna, "PARAM_PRONOME", pronome);
	}
}
