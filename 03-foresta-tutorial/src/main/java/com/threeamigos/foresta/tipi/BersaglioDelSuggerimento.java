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
	RIQUADRO_DEL_GRUPPO,
	RIQUADRO_DELLE_MISSIONI,
	/** Il riquadro della mappa, quello piccolo (la mappa a tutto schermo si apre con l'icona). */
	RIQUADRO_DELLA_MAPPA,
	RIQUADRO_DEGLI_INCANTESIMI_E_DELLE_POZIONI,
	/** Tutto lo schermo, per le pagine che parlano della schermata intera e non di un punto. */
	SCHERMATA,
	/** Il disco del joystick virtuale sulla mappa a tutto schermo. */
	JOYSTICK_VIRTUALE,
	/** Nella schermata delle tattiche, la colonna a sinistra con le regole della tattica. */
	LISTA_DELLE_TATTICHE,
	/** Nella schermata delle tattiche, la colonna a destra che spiega l'elemento sotto il mouse. */
	SPIEGAZIONE_DELLE_TATTICHE,
	/** Nella schermata delle tattiche, i pulsanti Sì, No e Predefinita. */
	PULSANTI_DELLE_TATTICHE,
	/** La barra delle icone intera. */
	BARRA_DELLE_ICONE,
	ICONE_DI_MOVIMENTO(Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST),
	ICONA_DELL_INVENTARIO(Comando.INVENTARIO),
	/** Le icone che dall'inventario aprono le altre pagine: trofei, tattiche, bestiario e grimorio. */
	ICONE_DELLE_PAGINE(Comando.MOSTRA_TROFEI, Comando.MOSTRA_TATTICHE, Comando.MOSTRA_BESTIARIO, Comando.MOSTRA_GRIMORIO),
	ICONE_DELLE_POZIONI(Comando.POZIONE_SALUTE, Comando.POZIONE_SALUTE_GRANDE),
	ICONE_DI_COMBATTIMENTO(Comando.COMBATTIMENTO, Comando.SINGOLO_ATTACCO, Comando.FUGA, Comando.PASSA_INOSSERVATO),
	/** L'icona del combattimento automatico. */
	ICONA_DEL_COMBATTIMENTO_AUTOMATICO(Comando.COMBATTIMENTO),
	ICONA_DELLA_MAPPA(Comando.MAPPA),
	/** L'interruttore dell'aiuto (il punto interrogativo) */
	ICONA_DELL_AIUTO(Comando.AIUTO, Comando.NO_AIUTO),
	/** L'icona che apre l'elenco di quello che si può lanciare: incantesimi e abilità innate. */
	ICONA_DEGLI_INCANTESIMI(Comando.INCANTESIMO),
	ICONA_DELLA_CORRUZIONE(Comando.CORRUZIONE),
	ICONA_DELL_AMICIZIA(Comando.AMICIZIA),
	ICONA_DELL_ACCAMPAMENTO(Comando.ACCAMPAMENTO),
	ICONE_DELLE_BOTTEGHE(Comando.NEGOZIO_ALCHIMISTA, Comando.NEGOZIO_ARMAIOLO, Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, Comando.NEGOZIO_INCANTATORE),
	/** Le icone dei personaggi del gruppo, quando la barra chiede chi compie l'azione. */
	ICONE_DEI_PERSONAGGI(Comando.PERSONAGGIO_1, Comando.PERSONAGGIO_2, Comando.PERSONAGGIO_3, Comando.PERSONAGGIO_4, Comando.PERSONAGGIO_5,
			Comando.PERSONAGGIO_6, Comando.PERSONAGGIO_7, Comando.PERSONAGGIO_8),
	/** Il Sì e il No di una domanda. */
	ICONE_SI_NO(Comando.SI, Comando.NO);

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
