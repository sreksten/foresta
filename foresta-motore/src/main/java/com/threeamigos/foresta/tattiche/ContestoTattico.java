package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.combattimento.StatoDelCombattimento;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.tipi.tattiche.TipoAzione;

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

	// Il contesto è immutabile: le liste sono copie non modificabili e conAzione, conVoce, conStato e conGruppoDegliAlleati restituiscono un contesto
	// nuovo con quel solo campo cambiato. I campi azioneInValutazione e identificativoVoce valgono null finché non si valuta una voce.
	private final Personaggio attore;
	private final boolean attoreDelGiocatore;
	private final List<Personaggio> alleati;
	private final List<Personaggio> avversari;
	private final List<Personaggio> ospitiVulnerabili;
	private final Personaggio capoDegliAlleati;
	private final Personaggio capoDegliAvversari;
	// La memoria dello scontro è un singleton (StatoDelCombattimento.getIstanza): il contesto la porta con sé per poterla sostituire nei test (conStato)
	private final StatoDelCombattimento stato;
	private final TipoAzione azioneInValutazione;
	private final String identificativoVoce;
	private final Gruppo gruppoDegliAlleati;

	/**
	 * @param alleati           tutti i membri del gruppo dell'attore, nell'ordine del gruppo; il primo è il capo
	 * @param avversari         tutti i membri dell'altro gruppo; il primo è il capo
	 * @param ospitiVulnerabili gli ospiti vulnerabili vivi del gruppo del giocatore
	 */
	public ContestoTattico(Personaggio attore, boolean attoreDelGiocatore, List<Personaggio> alleati, List<Personaggio> avversari,
						   List<Personaggio> ospitiVulnerabili) {
		this(attore, attoreDelGiocatore, alleati, avversari, ospitiVulnerabili, primo(alleati), primo(avversari),
				StatoDelCombattimento.getIstanza(), null, null, null);
	}

	/**
	 * Il costruttore completo, usato da tutti i "con...": ricopia le liste, quindi un contesto non vede cambiare i gruppi dopo la sua creazione
	 */
	private ContestoTattico(Personaggio attore, boolean attoreDelGiocatore, List<Personaggio> alleati, List<Personaggio> avversari,
							List<Personaggio> ospitiVulnerabili, Personaggio capoDegliAlleati, Personaggio capoDegliAvversari,
							StatoDelCombattimento stato, TipoAzione azioneInValutazione, String identificativoVoce, Gruppo gruppoDegliAlleati) {
		this.attore = attore;
		this.attoreDelGiocatore = attoreDelGiocatore;
		this.alleati = Collections.unmodifiableList(new ArrayList<>(alleati));
		this.avversari = Collections.unmodifiableList(new ArrayList<>(avversari));
		this.ospitiVulnerabili = Collections.unmodifiableList(new ArrayList<>(ospitiVulnerabili));
		this.capoDegliAlleati = capoDegliAlleati;
		this.capoDegliAvversari = capoDegliAvversari;
		this.stato = stato;
		this.azioneInValutazione = azioneInValutazione;
		this.identificativoVoce = identificativoVoce;
		this.gruppoDegliAlleati = gruppoDegliAlleati;
	}

	/**
	 * Il primo del gruppo è il suo capo (il gruppo tiene il capo in testa); null per un gruppo vuoto
	 */
	private static Personaggio primo(List<Personaggio> personaggi) {
		return personaggi.isEmpty() ? null : personaggi.get(0);
	}

	/**
	 * Il contesto di un personaggio in combattimento, ricavato dai gruppi della partita: se sta nel gruppo del giocatore i suoi alleati sono i miei
	 * personaggi, altrimenti sono i mostri.
	 */
	public static ContestoTattico per(Personaggio attore) {
		return per(attore, GruppoGiocatore.getIstanza(), GruppoAvversario.getIstanza());
	}

	/**
	 * Come {@link #per(Personaggio)}, ma con i gruppi dati (le simulazioni non usano quelli della partita)
	 */
	public static ContestoTattico per(Personaggio attore, Gruppo giocatore, Gruppo mostri) {
		// Il lato dell'attore si decide cercandolo nel gruppo del giocatore: chi non c'è è un mostro. Da qui in poi "alleati" e "avversari" sono relativi
		// all'attore, e le rose e i filtri non devono più chiedersi di che lato sia
		boolean delGiocatore = giocatore.contiene(attore);
		return new ContestoTattico(attore, delGiocatore,
				(delGiocatore ? giocatore : mostri).getPersonaggi(),
				(delGiocatore ? mostri : giocatore).getPersonaggi(),
				// Gli ospiti vulnerabili stanno sempre nel gruppo del giocatore, anche per un mostro che li può scegliere come bersagli
				giocatore.getOspitiBersaglio(),
				(delGiocatore ? giocatore : mostri).getCapo(),
				(delGiocatore ? mostri : giocatore).getCapo(),
				StatoDelCombattimento.getIstanza(), null, null, delGiocatore ? giocatore : mostri);
	}

	/**
	 * Lo stesso contesto mentre si valuta una voce con quest'azione (per i filtri che ne dipendono, come DEBOLE_ALL_AZIONE)
	 */
	public ContestoTattico conAzione(TipoAzione azione) {
		return new ContestoTattico(attore, attoreDelGiocatore, alleati, avversari, ospitiVulnerabili, capoDegliAlleati, capoDegliAvversari,
				stato, azione, identificativoVoce, gruppoDegliAlleati);
	}

	/**
	 * Lo stesso contesto mentre si valuta la voce con quell'identificativo (per i predicati che ne guardano gli usi)
	 */
	public ContestoTattico conVoce(String identificativo) {
		return new ContestoTattico(attore, attoreDelGiocatore, alleati, avversari, ospitiVulnerabili, capoDegliAlleati, capoDegliAvversari,
				stato, azioneInValutazione, identificativo, gruppoDegliAlleati);
	}

	/**
	 * Lo stesso contesto che legge le schede di un altro stato del combattimento (per i test)
	 */
	public ContestoTattico conStato(StatoDelCombattimento altroStato) {
		return new ContestoTattico(attore, attoreDelGiocatore, alleati, avversari, ospitiVulnerabili, capoDegliAlleati, capoDegliAvversari,
				altroStato, azioneInValutazione, identificativoVoce, gruppoDegliAlleati);
	}

	/**
	 * Lo stesso contesto con il gruppo dell'attore, da cui si leggono le scorte (pergamene e pozioni); senza, il gruppo non ha scorte.
	 * Con {@link #per(Personaggio)} c'è già.
	 */
	public ContestoTattico conGruppoDegliAlleati(Gruppo gruppo) {
		return new ContestoTattico(attore, attoreDelGiocatore, alleati, avversari, ospitiVulnerabili, capoDegliAlleati, capoDegliAvversari,
				stato, azioneInValutazione, identificativoVoce, gruppo);
	}

	/**
	 * Il gruppo dell'attore (dei miei o dei mostri), o null se il contesto non lo conosce
	 */
	public Gruppo getGruppoDegliAlleati() {
		return gruppoDegliAlleati;
	}

	/**
	 * Le pergamene di quell'incantesimo che il gruppo dell'attore ha con sé
	 */
	public int getPergamene(ClasseIncantesimo classe) {
		return gruppoDegliAlleati == null ? 0 : gruppoDegliAlleati.getIncantesimi(classe);
	}

	/** Le pozioni della Salute normali (le grandi sono un'altra azione) */
	public int getPozioniSalute() {
		return gruppoDegliAlleati == null ? 0 : gruppoDegliAlleati.getPozioniSalute();
	}

	public int getPozioniSaluteGrandi() {
		return gruppoDegliAlleati == null ? 0 : gruppoDegliAlleati.getPozioniSaluteGrande();
	}

	/** Le pozioni della Magia normali (le grandi sono un'altra azione) */
	public int getPozioniMagia() {
		return gruppoDegliAlleati == null ? 0 : gruppoDegliAlleati.getPozioniMagia();
	}

	public int getPozioniMagiaGrandi() {
		return gruppoDegliAlleati == null ? 0 : gruppoDegliAlleati.getPozioniMagiaGrande();
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

	public StatoDelCombattimento getStato() {
		return stato;
	}

	/**
	 * L'azione della voce che si sta valutando, o null fuori da una voce
	 */
	public TipoAzione getAzioneInValutazione() {
		return azioneInValutazione;
	}

	/**
	 * L'identificativo della voce che si sta valutando (con cui lo stato del combattimento ricorda gli usi), o null fuori da una voce
	 */
	public String getIdentificativoVoce() {
		return identificativoVoce;
	}

	/**
	 * Se il personaggio è il capo del suo gruppo (dei miei o dei mostri)
	 */
	public boolean isCapo(Personaggio personaggio) {
		return personaggio == capoDegliAlleati || personaggio == capoDegliAvversari;
	}

	public boolean isOspite(Personaggio personaggio) {
		return ospitiVulnerabili.contains(personaggio);
	}

	/**
	 * Se il personaggio è un mostro (e non uno dei miei): sta nel gruppo dei mostri, che per l'attore è il suo o quello avversario
	 */
	public boolean isMostro(Personaggio personaggio) {
		return attoreDelGiocatore ? avversari.contains(personaggio) : alleati.contains(personaggio);
	}
}
