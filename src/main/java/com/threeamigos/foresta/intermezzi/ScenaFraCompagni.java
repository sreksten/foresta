package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.ArrayList;
import java.util.List;

/**
 * La pagina di un intermezzo in cui il gruppo parla fra sé, fuori dalle città: intorno al fuoco di un accampamento
 * ({@link #allAccampamento}), o in una locazione ({@link #in}). Il primo personaggio è a sinistra e guarda verso
 * destra, il secondo è a destra e guarda verso sinistra, e parlano loro due; un eventuale terzo e quarto personaggio
 * stanno più in alto, fra il primo/secondo e il centro, e un eventuale quinto ancora più in alto, al centro.
 */
public final class ScenaFraCompagni {

	private static final String PRIMO = "personaggio0";
	private static final String SECONDO = "personaggio1";

	private static final double X_FUOCO = 0.5;
	private static final double Y_FUOCO = 0.68;

	private static final double X_PRIMO = 0.38;
	private static final double Y_PRIMO = 0.61;
	private static final double X_SECONDO = 0.62;
	private static final double Y_SECONDO = 0.61;
	private static final double X_TERZO = 0.44;
	private static final double Y_TERZO = 0.56;
	private static final double X_QUARTO = 0.56;
	private static final double Y_QUARTO = 0.56;
	private static final double X_QUINTO = 0.5;
	private static final double Y_QUINTO = 0.5;

	// La luna sorge da dietro gli alberi, sulla destra, e sale nel cielo
	private static final double X_LUNA_INIZIO = 0.40;
	private static final double Y_LUNA_INIZIO = 0.20;
	private static final double X_LUNA_FINE = 0.88;
	private static final double Y_LUNA_FINE = 0.18;
	private static final double SECONDI_SALITA_LUNA = 6;

	private final String testo;
	private final TipoLocazione sfondo;
	private final boolean conFuoco;
	private final List<ClassePersonaggio> personaggi;
	private final List<BattutaIntermezzo> battute = new ArrayList<>();
	private String luna;

	private ScenaFraCompagni(String testo, TipoLocazione sfondo, boolean conFuoco, List<Personaggio> personaggi) {
		if (personaggi.size() < 2) {
			throw new IllegalArgumentException("Per parlare fra sé servono almeno due personaggi");
		}
		this.testo = testo;
		this.sfondo = sfondo;
		this.conFuoco = conFuoco;
		this.personaggi = new ArrayList<>();
		personaggi.forEach(p -> this.personaggi.add(p.getClasse()));
	}

	/**
	 * Intorno al fuoco, nel bosco, di notte: i primi due personaggi della lista parlano.
	 */
	public static ScenaFraCompagni allAccampamento(String testo, List<Personaggio> personaggi) {
		return new ScenaFraCompagni(testo, TipoLocazione.BOSCO, true, personaggi);
	}

	/**
	 * In quella locazione: i primi due personaggi della lista parlano.
	 */
	public static ScenaFraCompagni in(TipoLocazione locazione, String testo, List<Personaggio> personaggi) {
		return new ScenaFraCompagni(testo, locazione, false, personaggi);
	}

	/**
	 * La luna che sale nel cielo, da quella risorsa (per esempio "fondi/Luna.gif").
	 */
	public ScenaFraCompagni conLuna(String risorsa) {
		luna = risorsa;
		return this;
	}

	public ScenaFraCompagni parlaIlPrimo(String battuta) {
		battute.add(BattutaIntermezzo.di(PRIMO, battuta));
		return this;
	}

	public ScenaFraCompagni parlaIlSecondo(String battuta) {
		battute.add(BattutaIntermezzo.di(SECONDO, battuta));
		return this;
	}

	public List<PaginaIntermezzo> getPagine() {
		PaginaIntermezzo pagina = new PaginaIntermezzo(testo).conSfondo(ImmagineIntermezzo.locazione(sfondo));

		// Niente conRitaglioSuSfondo(): la luna deve salire liberamente nel cielo, ben oltre
		// il piccolo riquadro occupato dall'immagine di sfondo (a differenza degli altri
		// personaggi, statici e già dentro quel riquadro).
		if (luna != null) {
			// La luna è nel cielo, più lontana di tutto il resto della scena
			pagina.conElemento(ElementoIntermezzo.di("luna", ImmagineIntermezzo.risorsa(luna), X_LUNA_INIZIO, Y_LUNA_INIZIO)
					.poi(Tappa.inSecondi(SECONDI_SALITA_LUNA).verso(X_LUNA_FINE, Y_LUNA_FINE)));
		}

		// Disegnati dal più lontano al più vicino (quinto, quarto, terzo, secondo, primo
		// e infine il fuoco) così gli elementi davanti coprono quelli dietro.
		if (personaggi.size() > 4) {
			pagina.conElemento(versoDestra("personaggio4", personaggi.get(4), X_QUINTO, Y_QUINTO));
		}
		if (personaggi.size() > 3) {
			pagina.conElemento(versoSinistra("personaggio3", personaggi.get(3), X_QUARTO, Y_QUARTO));
		}
		if (personaggi.size() > 2) {
			pagina.conElemento(versoDestra("personaggio2", personaggi.get(2), X_TERZO, Y_TERZO));
		}
		pagina.conElemento(versoSinistra(SECONDO, personaggi.get(1), X_SECONDO, Y_SECONDO).conBocca(0.5, -0.15));
		pagina.conElemento(versoDestra(PRIMO, personaggi.get(0), X_PRIMO, Y_PRIMO).conBocca(0.5, -0.15));
		if (conFuoco) {
			pagina.conElemento(ElementoIntermezzo.di("fuoco", ImmagineIntermezzo.animazione(Animazione.FUOCO_DA_CAMPO), X_FUOCO, Y_FUOCO));
		}
		battute.forEach(pagina::conBattuta);
		List<PaginaIntermezzo> pagine = new ArrayList<>();
		pagine.add(pagina);
		return pagine;
	}

	private static ElementoIntermezzo versoDestra(String id, ClassePersonaggio classe, double x, double y) {
		ElementoIntermezzo elemento = ElementoIntermezzo.personaggio(id, classe, x, y);
		if (VersoDiDefault.serveSpecchiare(classe, Verso.DESTRA)) {
			elemento.specchiato();
		}
		return elemento;
	}

	private static ElementoIntermezzo versoSinistra(String id, ClassePersonaggio classe, double x, double y) {
		ElementoIntermezzo elemento = ElementoIntermezzo.personaggio(id, classe, x, y);
		if (VersoDiDefault.serveSpecchiare(classe, Verso.SINISTRA)) {
			elemento.specchiato();
		}
		return elemento;
	}
}
