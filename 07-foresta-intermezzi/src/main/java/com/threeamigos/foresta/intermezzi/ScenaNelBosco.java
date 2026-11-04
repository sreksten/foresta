package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.List;

/**
 * La pagina di un intermezzo in un bosco: chi il gruppo incontra è già lì, sulla destra, e il gruppo arriva in fila come
 * quando entra in un negozio (vedi {@link ScenaNegozio}) e, appena arrivato, cominciano le battute. Come {@link ScenaInCitta}, ma
 * all'aperto, con lo sfondo del bosco.
 * <pre>
 * ScenaNelBosco.con(TipoPersonaggio.VICHINGO)
 *     .parlaIlCapo("E... tu chi saresti?")
 *     .parlaChiIncontrato("Io sono Olaf.")
 *     .getPagine();
 * </pre>
 * Gli ospiti vivi del gruppo entrano subito dopo il capo, come nelle altre scene.
 */
public final class ScenaNelBosco {

	private static final String ID_INCONTRATO = "incontrato";
	private static final double X_INCONTRATO = 0.62;
	private static final double Y_PERSONAGGI = 0.61;
	private static final double X_ARRIVO_CAPO = 0.45;
	private static final double RITARDO_FRA_PARTENZE = 0.6;

	private final TipoPersonaggio incontrato;
	// Si costruisce alla prima battuta (o a getPagine), con il gruppo di quel momento
	private ScenaNegozio scena;

	private ScenaNelBosco(TipoPersonaggio incontrato) {
		this.incontrato = incontrato;
	}

	/**
	 * Chi si incontra è un personaggio di quella classe.
	 */
	public static ScenaNelBosco con(TipoPersonaggio classe) {
		return new ScenaNelBosco(classe);
	}

	private ScenaNegozio scena() {
		if (scena == null) {
			scena = new ScenaNegozio(ImmagineIntermezzo.locazione(TipoLocazione.BOSCO), false, null, ID_INCONTRATO,
					ElementoIntermezzo.personaggio(ID_INCONTRATO, incontrato, X_INCONTRATO, Y_PERSONAGGI),
					Y_PERSONAGGI, X_ARRIVO_CAPO, RITARDO_FRA_PARTENZE);
		}
		return scena;
	}

	/**
	 * Una battuta di chi il gruppo ha incontrato.
	 */
	public ScenaNelBosco parlaChiIncontrato(String testo) {
		scena().parlaIlNegoziante(testo);
		return this;
	}

	/**
	 * Una battuta del capo del gruppo.
	 */
	public ScenaNelBosco parlaIlCapo(String testo) {
		scena().parlaIlCapo(testo);
		return this;
	}

	/**
	 * Una battuta di un compagno del capo; se il capo è solo, la dice lui.
	 */
	public ScenaNelBosco parlaUnCompagno(String testo) {
		scena().parlaUnCompagno(testo);
		return this;
	}

	public List<PaginaIntermezzo> getPagine() {
		return scena().getPagine();
	}
}
