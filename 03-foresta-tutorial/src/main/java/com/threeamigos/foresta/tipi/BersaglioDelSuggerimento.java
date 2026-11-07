package com.threeamigos.foresta.tipi;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * A che cosa punta la coda del fumetto di un suggerimento, e che cosa fa lampeggiare: un riquadro della schermata di gioco, la barra delle
 * icone o un gruppo di icone. È la UI a sapere dove sta ognuno (le posizioni dei riquadri, il rettangolo di un'icona); qui c'è solo il nome
 * e, per le icone, i comandi che le identificano.
 *
 * @author Stefano Reksten
 */
public enum BersaglioDelSuggerimento {

	/** Tutti i riquadri della schermata di gioco. */
	RIQUADRI,
	RIQUADRO_DEL_COMBATTIMENTO,
	RIQUADRO_DELLE_MISSIONI,
	RIQUADRO_DEGLI_INCANTESIMI_E_DELLE_POZIONI,
	/** La barra delle icone intera. */
	BARRA_DELLE_ICONE,
	ICONE_DI_MOVIMENTO(Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST),
	ICONA_DELL_INVENTARIO(Comando.INVENTARIO),
	ICONE_DELLE_POZIONI(Comando.POZIONE_SALUTE, Comando.POZIONE_SALUTE_GRANDE),
	ICONE_DI_COMBATTIMENTO(Comando.COMBATTIMENTO, Comando.SINGOLO_ATTACCO, Comando.FUGA, Comando.PASSA_INOSSERVATO),
	ICONA_DELLE_TATTICHE(Comando.MOSTRA_TATTICHE),
	ICONA_DELLA_CORRUZIONE(Comando.CORRUZIONE),
	ICONA_DELL_AMICIZIA(Comando.AMICIZIA),
	ICONA_DELL_ACCAMPAMENTO(Comando.ACCAMPAMENTO),
	ICONE_DELLE_BOTTEGHE(Comando.NEGOZIO_ALCHIMISTA, Comando.NEGOZIO_ARMAIOLO, Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, Comando.NEGOZIO_INCANTATORE);

	private final Set<Comando> icone;

	BersaglioDelSuggerimento(Comando... icone) {
		this.icone = icone.length == 0 ? Collections.<Comando>emptySet() : Collections.unmodifiableSet(EnumSet.of(icone[0], icone));
	}

	/**
	 * I comandi delle icone a cui punta, vuoto se il bersaglio è un riquadro o la barra intera.
	 */
	public Set<Comando> getIcone() {
		return icone;
	}
}
