package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.oggetti.NomeOggetto;
import com.threeamigos.foresta.strumenti.Misc;

import java.util.Objects;

/**
 * Una cosa da portare da una città all'altra (vedi IlCorriere), letta da una riga di missioni.txt: genere
 * dell'oggetto ("F" o "M"), oggetto, mittente, destinatario, "URGENTE" o "CON_CALMA", monete, la richiesta del
 * mittente, la battuta del capo, la risposta del mittente e il ringraziamento del destinatario, separati da ";".
 * <pre>
 * F;fiala di antidoto;l'erborista;il fabbro;URGENTE;20;Il fabbro è stato morso da una vipera.;E se arriviamo tardi?;Non arrivate tardi.;Mi sento già le dita dei piedi!
 * </pre>
 * L'oggetto è senza articolo, il mittente e il destinatario con l'articolo.
 */
public final class Spedizione {

	private static final String SEPARATORE = ";";
	private static final int CAMPI = 10;
	private static final String URGENTE = "URGENTE";
	private static final String CON_CALMA = "CON_CALMA";

	private final String riga;
	private final boolean femminile;
	private final String oggetto;
	private final String mittente;
	private final String destinatario;
	private final boolean urgente;
	private final int monete;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String rispostaDelMittente;
	private final String ringraziamento;

	private Spedizione(String riga) {
		this.riga = Objects.requireNonNull(riga);
		String[] campi = riga.split(SEPARATORE, -1);
		if (campi.length != CAMPI) {
			throw new IllegalArgumentException("Una spedizione ha " + CAMPI + " campi: " + riga);
		}
		String genere = campi[0].trim();
		if (!"F".equals(genere) && !"M".equals(genere)) {
			throw new IllegalArgumentException("Il genere è F o M: " + riga);
		}
		femminile = "F".equals(genere);
		oggetto = campi[1].trim();
		mittente = campi[2].trim();
		destinatario = campi[3].trim();
		String fretta = campi[4].trim();
		if (!URGENTE.equals(fretta) && !CON_CALMA.equals(fretta)) {
			throw new IllegalArgumentException("La fretta è " + URGENTE + " o " + CON_CALMA + ": " + riga);
		}
		urgente = URGENTE.equals(fretta);
		monete = Integer.parseInt(campi[5].trim());
		if (monete < 1) {
			throw new IllegalArgumentException("Monete sbagliate: " + riga);
		}
		richiesta = campi[6].trim();
		battutaDelCapo = campi[7].trim();
		rispostaDelMittente = campi[8].trim();
		ringraziamento = campi[9].trim();
	}

	/**
	 * La spedizione di una riga di missioni.txt (o di una riga salvata con {@link #getRiga()}).
	 */
	public static Spedizione da(String riga) {
		return new Spedizione(riga);
	}

	/**
	 * La riga da cui è stata letta, da salvare con la missione.
	 */
	public String getRiga() {
		return riga;
	}

	public boolean isFemminile() {
		return femminile;
	}

	/**
	 * L'oggetto, senza articolo: "fiala di antidoto".
	 */
	public String getOggetto() {
		return oggetto;
	}

	/**
	 * L'oggetto con l'articolo: "la fiala di antidoto".
	 */
	public String getOggettoConArticolo() {
		return (femminile ? Misc.LA : Misc.IL) + oggetto;
	}

	/**
	 * L'oggetto con il dimostrativo: "questa fiala di antidoto".
	 */
	public String getQuestoOggetto() {
		return (femminile ? "questa " : "questo ") + oggetto;
	}

	/**
	 * Il pronome per l'oggetto: "la" o "lo".
	 */
	public String getPronome() {
		return femminile ? "la" : "lo";
	}

	/**
	 * Il nome dell'oggetto di missione da consegnare (uno solo).
	 */
	public NomeOggetto getNome() {
		return femminile ? NomeOggetto.femminile(oggetto, oggetto) : NomeOggetto.maschile(oggetto, oggetto);
	}

	/**
	 * Chi manda l'oggetto, con l'articolo: "l'erborista".
	 */
	public String getMittente() {
		return mittente;
	}

	/**
	 * Chi lo riceve, con l'articolo: "il fabbro".
	 */
	public String getDestinatario() {
		return destinatario;
	}

	/**
	 * Se c'è fretta: un antidoto, una medicina. Allora la consegna ha una scadenza (vedi IlCorriere).
	 */
	public boolean isUrgente() {
		return urgente;
	}

	/**
	 * Le monete che paga il destinatario, prima di aggiungere la strada da fare.
	 */
	public int getMonete() {
		return monete;
	}

	/**
	 * Perché il mittente manda l'oggetto: la sua prima battuta nell'intermezzo.
	 */
	public String getRichiesta() {
		return richiesta;
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRispostaDelMittente() {
		return rispostaDelMittente;
	}

	/**
	 * Che cosa dice il destinatario quando riceve l'oggetto.
	 */
	public String getRingraziamento() {
		return ringraziamento;
	}
}
