package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.StatoDelCombattimento;
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

	private final Personaggio attore;
	private final boolean attoreDelGiocatore;
	private final List<Personaggio> alleati;
	private final List<Personaggio> avversari;
	private final List<Personaggio> ospitiVulnerabili;
	private final Personaggio capoDegliAlleati;
	private final Personaggio capoDegliAvversari;
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

	private static Personaggio primo(List<Personaggio> personaggi) {
		return personaggi.isEmpty() ? null : personaggi.get(0);
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

	public int getPozioniSalute() {
		return gruppoDegliAlleati == null ? 0 : gruppoDegliAlleati.getPozioniSalute() + gruppoDegliAlleati.getPozioniSaluteGrande();
	}

	public int getPozioniMagia() {
		return gruppoDegliAlleati == null ? 0 : gruppoDegliAlleati.getPozioniMagia() + gruppoDegliAlleati.getPozioniMagiaGrande();
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
