package com.threeamigos.foresta.tipi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * I trofei che il giocatore può vincere. A differenza delle missioni non scadono mai e
 * valgono da una partita all'altra: una volta vinto, un trofeo non si perde più.
 * <p>
 * I testi di ogni costante (NOME, DESCRIZIONE) non stanno qui ma nel file {@code tipo_trofeo_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 */
public enum TipoTrofeo {

	PERDIGIORNO(SupertipoTrofeo.SPECIALE),
	CACCIATORE_DI_TAGLIE(SupertipoTrofeo.MISSIONE),
	RE_DI_CARTA_FORBICI_E_SASSO(SupertipoTrofeo.MISSIONE),
	AMMAZZAGOBLIN(SupertipoTrofeo.UCCISIONE),
	UCCIDI_IL_DRAGO(SupertipoTrofeo.UCCISIONE),
	UCCIDI_LA_STREGA(SupertipoTrofeo.UCCISIONE),
	UCCIDI_IL_LICH(SupertipoTrofeo.UCCISIONE),
	UCCIDI_L_IDRA(SupertipoTrofeo.UCCISIONE),
	UCCIDI_IL_MINOTAURO_GIGANTE(SupertipoTrofeo.UCCISIONE),
	RAPINATORE(SupertipoTrofeo.BOTTINO),
	LADRO_DI_PREZIOSI(SupertipoTrofeo.BOTTINO),
	ARSENIO_LUPIN(SupertipoTrofeo.BOTTINO),
	ESPERTO_SCASSINATORE(SupertipoTrofeo.BOTTINO),
	CACCIATORE_DI_TESORI(SupertipoTrofeo.BOTTINO),
	ESPERTO_CACCIATORE_DI_TESORI(SupertipoTrofeo.BOTTINO),
	RIGATTIERE(SupertipoTrofeo.ACQUISTO),
	COLLEZIONISTA(SupertipoTrofeo.ACQUISTO),
	STUDIOSO(SupertipoTrofeo.ACQUISTO),
	BOMBAROLO(SupertipoTrofeo.ACQUISTO),
	CARTOGRAFO(SupertipoTrofeo.ACQUISTO),
	TRAFFICONE(SupertipoTrofeo.ACQUISTO),
	AMICO_DI_TUTTI(SupertipoTrofeo.DIPLOMAZIA),
	CORRUTTORE(SupertipoTrofeo.DIPLOMAZIA),
	NECROMANTE(SupertipoTrofeo.VARIE),
	SBEVAZZONE(SupertipoTrofeo.VARIE),
	TUTTI_I_SANTI(SupertipoTrofeo.VARIE),
	SPELEOLOGO(SupertipoTrofeo.VARIE),
	VERO_ROMPISCATOLE(SupertipoTrofeo.VARIE);

	private final SupertipoTrofeo supertipo;

	TipoTrofeo(SupertipoTrofeo supertipo) {
		this.supertipo = supertipo;
	}

	public String getNome() {
		return Testi.testoOCostante(this, "NOME");
	}

	public String getDescrizione() {
		return Testi.testoOCostante(this, "DESCRIZIONE");
	}

	public SupertipoTrofeo getSupertipo() {
		return supertipo;
	}

	/**
	 * Tutti i trofei raggruppati per tipologia, nell'ordine di {@link SupertipoTrofeo}.
	 */
	public static List<TipoTrofeo> perTipologia() {
		List<TipoTrofeo> trofei = new ArrayList<>(Arrays.asList(values()));
		trofei.sort(Comparator.comparing(TipoTrofeo::getSupertipo));
		return trofei;
	}
}
