package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tutto ciò che le rose, i filtri, i predicati e gli ordinamenti di una tattica devono poter <b>leggere</b> per decidere (vedi
 * tattiche_di_combattimento_automatiche.md §11.4). Si crea per ogni decisione di un personaggio e si butta dopo; non modifica niente.
 * <p>
 * Sa di che lato è l'attore, quindi chi sono i suoi alleati e chi i suoi avversari: le rose restano enumerati senza stato, e lo stesso
 * vale per un mio personaggio e per un mostro. Le liste sono tutti i membri dei gruppi (anche caduti o in panchina): sta alle rose scegliere.
 */
public final class ContestoTattico {

	private final Personaggio attore;
	private final boolean attoreDelGiocatore;
	private final List<Personaggio> alleati;
	private final List<Personaggio> avversari;
	private final List<Personaggio> ospitiVulnerabili;

	/**
	 * @param alleati           tutti i membri del gruppo dell'attore, nell'ordine del gruppo
	 * @param avversari         tutti i membri dell'altro gruppo
	 * @param ospitiVulnerabili gli ospiti vulnerabili vivi del gruppo del giocatore
	 */
	public ContestoTattico(Personaggio attore, boolean attoreDelGiocatore, List<Personaggio> alleati, List<Personaggio> avversari,
						   List<Personaggio> ospitiVulnerabili) {
		this.attore = attore;
		this.attoreDelGiocatore = attoreDelGiocatore;
		this.alleati = Collections.unmodifiableList(new ArrayList<>(alleati));
		this.avversari = Collections.unmodifiableList(new ArrayList<>(avversari));
		this.ospitiVulnerabili = Collections.unmodifiableList(new ArrayList<>(ospitiVulnerabili));
	}

	/**
	 * Il contesto di un personaggio in combattimento, ricavato dai gruppi della partita: se sta nel gruppo del giocatore i suoi alleati sono i miei
	 * personaggi, altrimenti sono i mostri.
	 */
	public static ContestoTattico per(Personaggio attore) {
		GruppoGiocatore giocatore = GruppoGiocatore.getIstanza();
		GruppoAvversario mostri = GruppoAvversario.getIstanza();
		boolean delGiocatore = giocatore.contiene(attore);
		return new ContestoTattico(attore, delGiocatore,
				(delGiocatore ? giocatore : mostri).getPersonaggi(),
				(delGiocatore ? mostri : giocatore).getPersonaggi(),
				giocatore.getOspitiBersaglio());
	}

	public Personaggio getAttore() {
		return attore;
	}

	/**
	 * Se chi decide è un personaggio del giocatore (e non un mostro)
	 */
	public boolean isAttoreDelGiocatore() {
		return attoreDelGiocatore;
	}

	/**
	 * Tutti i membri del gruppo dell'attore, anche caduti o in panchina
	 */
	public List<Personaggio> getAlleati() {
		return alleati;
	}

	/**
	 * Tutti i membri dell'altro gruppo, anche caduti o in panchina
	 */
	public List<Personaggio> getAvversari() {
		return avversari;
	}

	/**
	 * Gli ospiti vulnerabili vivi del gruppo del giocatore
	 */
	public List<Personaggio> getOspitiVulnerabili() {
		return ospitiVulnerabili;
	}
}
