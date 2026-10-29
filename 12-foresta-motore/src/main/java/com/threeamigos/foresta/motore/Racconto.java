package com.threeamigos.foresta.motore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * I pezzi della storia che non dipendono dalla partita: l'introduzione e i finali. Sono contenuto del gioco, non della UI: la UI li
 * riceve dal motore attraverso VistaPartita, così ogni piattaforma racconta la stessa storia.
 *
 * @author Stefano Reksten
 */
public final class Racconto {

	private static final String[] STORIA = {
			"molto, molto tempo fa, quando il mondo era da poco stato creato,",
			"un gruppo di eroi venne riunito per liberarlo dalla minaccia dei draghi.",
			"questi, invece di comportarsi da bravi ragazzi, presto cominciarono a degenerare",
			"andando per locande a sbevazzare e a cercare compagne di sbornie e avventure",
			"finche' un giorno il piu' scoppiato di tutti sbaglio' strada e si ritrovo' in un postaccio conosciuto come"
	};

	private static final String[] PERSO = {
			"con la morte del cacciatore, il drago ha finalmente via libera... la foresta e' perduta!",
	};

	private static final String[] VINTO = {
			"congratulazioni!",
			"la foresta e' stata liberata",
			"i tuoi amici sono venuti per offrirti doni come loro salvatore",
			"possa la pace oggi conquistata durare a lungo!",
			""
	};

	private Racconto() {
	}

	/**
	 * L'introduzione, un paragrafo per elemento.
	 */
	public static List<String> getStoria() {
		return Collections.unmodifiableList(Arrays.asList(STORIA));
	}

	/**
	 * Il finale della partita persa, una pagina per elemento.
	 */
	public static List<String> getFinaleDellaSconfitta() {
		return Collections.unmodifiableList(Arrays.asList(PERSO));
	}

	/**
	 * Il finale della partita vinta, una pagina per elemento.
	 */
	public static List<String> getFinaleDellaVittoria() {
		return Collections.unmodifiableList(Arrays.asList(VINTO));
	}
}
