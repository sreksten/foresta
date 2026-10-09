package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;

/**
 * La pagina di un intermezzo dentro una locanda: il locandiere aspetta dietro il bancone, il gruppo arriva in fila
 * come quando entra in un negozio (vedi {@link ScenaNegozio}) e, appena arrivato, cominciano le battute. Come
 * {@link ScenaInCitta}, ma per le missioni che nascono in una locanda. Al posto del locandiere può esserci un
 * avventore che chiede qualcosa al gruppo: un sacerdote o una sacerdotessa ({@link #conSacerdote}).
 */
public final class ScenaInLocanda {

	private static final String SFONDO = "fondinon2x2/InternoLocanda.gif";
	private static final String PRIMO_PIANO = "fondinon2x2/ForegroundLocanda.gif";
	private static final String ID_LOCANDIERE = "locandiere";
	// Come la scenetta d'ingresso nella locanda (vedi NegozioInScena.LOCANDA)
	private static final double X_LOCANDIERE = 0.65;
	private static final double Y_PERSONAGGI = 0.6;
	private static final double X_ARRIVO_CAPO = 0.5;
	private static final double RITARDO_FRA_PARTENZE = 0.6;

	private final ScenaNegozio scena;

	private ScenaInLocanda(ElementoIntermezzo avventore) {
		scena = new ScenaNegozio(SFONDO, PRIMO_PIANO, ID_LOCANDIERE, avventore,
				Y_PERSONAGGI, X_ARRIVO_CAPO, RITARDO_FRA_PARTENZE);
	}

	public static ScenaInLocanda conLocandiere() {
		return new ScenaInLocanda(ElementoIntermezzo.personaggio(ID_LOCANDIERE, TipoPersonaggio.LOCANDIERE, X_LOCANDIERE, Y_PERSONAGGI));
	}

	/**
	 * Un sacerdote, o una sacerdotessa, seduto nella locanda: parla con {@link #parlaIlLocandiere}, al suo posto.
	 * Guarda verso sinistra, da dove arriva il gruppo,.
	 *
	 * @param classe SACERDOTE o SACERDOTESSA
	 */
	public static ScenaInLocanda conSacerdote(TipoPersonaggio classe) {
		if (classe != TipoPersonaggio.SACERDOTE && classe != TipoPersonaggio.SACERDOTESSA) {
			throw new IllegalArgumentException("Un sacerdote o una sacerdotessa, non " + classe);
		}
		return new ScenaInLocanda(ElementoIntermezzo.personaggio(ID_LOCANDIERE, classe, X_LOCANDIERE, Y_PERSONAGGI)
				.guarda(Verso.SINISTRA));
	}

	public ScenaInLocanda parlaIlLocandiere(String testo) {
		scena.parlaIlNegoziante(testo);
		return this;
	}

	public ScenaInLocanda parlaIlCapo(String testo) {
		scena.parlaIlCapo(testo);
		return this;
	}

	public List<PaginaIntermezzo> getPagine() {
		return scena.getPagine();
	}
}
