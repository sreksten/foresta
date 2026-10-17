package com.threeamigos.foresta.tipi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * I trofei che il giocatore può vincere. A differenza delle missioni non scadono mai e
 * valgono da una partita all'altra. Una volta vinto, un trofeo non si perde più.
 * <p>
 * I testi di ogni costante (NOME, DESCRIZIONE) non stanno qui ma nel file {@code tipo_trofeo_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoTrofeo {

	PERDIGIORNO(CategoriaTrofeo.SPECIALE),
	CACCIATORE_DI_TAGLIE(CategoriaTrofeo.MISSIONE),
	RE_DI_CARTA_FORBICI_E_SASSO(CategoriaTrofeo.MISSIONE),
	AMMAZZAGOBLIN(CategoriaTrofeo.UCCISIONE),
	UCCIDI_IL_DRAGO(CategoriaTrofeo.UCCISIONE),
	UCCIDI_LA_STREGA(CategoriaTrofeo.UCCISIONE),
	UCCIDI_IL_LICH(CategoriaTrofeo.UCCISIONE),
	UCCIDI_L_IDRA(CategoriaTrofeo.UCCISIONE),
	UCCIDI_IL_MINOTAURO_GIGANTE(CategoriaTrofeo.UCCISIONE),
	RAPINATORE(CategoriaTrofeo.BOTTINO),
	LADRO_DI_PREZIOSI(CategoriaTrofeo.BOTTINO),
	ARSENIO_LUPIN(CategoriaTrofeo.BOTTINO),
	ESPERTO_SCASSINATORE(CategoriaTrofeo.BOTTINO),
	CACCIATORE_DI_TESORI(CategoriaTrofeo.BOTTINO),
	ESPERTO_CACCIATORE_DI_TESORI(CategoriaTrofeo.BOTTINO),
	RIGATTIERE(CategoriaTrofeo.ACQUISTO),
	COLLEZIONISTA(CategoriaTrofeo.ACQUISTO),
	STUDIOSO(CategoriaTrofeo.ACQUISTO),
	BOMBAROLO(CategoriaTrofeo.ACQUISTO),
	CARTOGRAFO(CategoriaTrofeo.ACQUISTO),
	TRAFFICONE(CategoriaTrofeo.ACQUISTO),
	AMICO_DI_TUTTI(CategoriaTrofeo.DIPLOMAZIA),
	CORRUTTORE(CategoriaTrofeo.DIPLOMAZIA),
	NECROMANTE(CategoriaTrofeo.VARIE),
	SBEVAZZONE(CategoriaTrofeo.VARIE),
	TUTTI_I_SANTI(CategoriaTrofeo.VARIE),
	SPELEOLOGO(CategoriaTrofeo.VARIE),
	VERO_ROMPISCATOLE(CategoriaTrofeo.VARIE);

	private final CategoriaTrofeo supertipo;

	TipoTrofeo(CategoriaTrofeo supertipo) {
		this.supertipo = supertipo;
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	public CategoriaTrofeo getSupertipo() {
		return supertipo;
	}

	/**
	 * Tutti i trofei raggruppati per tipologia, nell'ordine di {@link CategoriaTrofeo}.
	 */
	public static List<TipoTrofeo> perTipologia() {
		List<TipoTrofeo> trofei = new ArrayList<>(Arrays.asList(values()));
		trofei.sort(Comparator.comparing(TipoTrofeo::getSupertipo));
		return trofei;
	}
}
