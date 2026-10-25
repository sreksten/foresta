package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.GruppoMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Un insieme di personaggi, con il suo capo e le sue scorte di pergamene e pozioni. Le due specializzazioni sono {@link GruppoGiocatore} e
 * {@link GruppoAvversario}. Le scorte stanno nel modello dati del gruppo: le variazioni di quelle del giocatore si annunciano alla UI con le
 * notifiche, quelle degli avversari no (sono scorte che i mostri hanno con sé e che a scontro finito possono diventare bottino).
 */
public abstract class Gruppo implements VistaGruppo {
	
	// Caratteristiche del gruppo
	protected List<Personaggio> personaggi = new ArrayList<>();
	protected Personaggio capo;

	/**
	 * Svuota il gruppo: nessun personaggio e nessun capo.
	 */
	protected void reimposta() {
		personaggi.clear();
		capo = null;
	}

	// Il modello dati di un gruppo che non è quello del giocatore (il gruppo avversario): ha solo le scorte, e si perdono con la locazione
	private final GruppoMD gruppoMD = new GruppoMD();

	/**
	 * Il modello dati del gruppo: le scorte di incantesimi e di pozioni stanno lì (nel gruppo del giocatore anche tutto il
	 * resto, e si salvano)
	 */
	protected GruppoMD getGruppoMD() {
		return gruppoMD;
	}

	// LE SCORTE DEL GRUPPO: le pergamene degli incantesimi e le pozioni. Le variazioni del gruppo del giocatore si
	// annunciano alla UI; quelle del gruppo avversario no (sono scorte che i mostri hanno con sé, e che a scontro finito
	// possono diventare bottino)

	public final int getIncantesimi(TipoIncantesimo tipoIncantesimo) {
		return getGruppoMD().getIncantesimi(tipoIncantesimo);
	}

	/**
	 * Aggiunge pergamene di quell'incantesimo alle scorte; il gruppo del giocatore lo annuncia alla UI.
	 */
	public final void addIncantesimi(TipoIncantesimo tipoIncantesimo, int quantita) {
		int valorePrecedente = getGruppoMD().getIncantesimi(tipoIncantesimo);
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setIncantesimi(tipoIncantesimo, valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaIncantesimi(tipoIncantesimo, valorePrecedente, valoreAttuale));
		}
	}

	/**
	 * Toglie pergamene di quell'incantesimo dalle scorte.
	 */
	public final void subIncantesimi(TipoIncantesimo tipoIncantesimo, int quantita) {
		addIncantesimi(tipoIncantesimo, -quantita);
	}
	
	public final int getPozioniSalute() {
		return getGruppoMD().getPozioniSalute();
	}

	/**
	 * Aggiunge pozioni della salute alle scorte; il gruppo del giocatore lo annuncia alla UI.
	 */
	public final void addPozioniSalute(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniSalute();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniSalute(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniSalute(valorePrecedente, valoreAttuale));
		}
	}

	/**
	 * Toglie pozioni della salute dalle scorte.
	 */
	public final void subPozioniSalute(int quantita) {
		addPozioniSalute(-quantita);
	}

	/**
	 * Fa bere a quel personaggio una pozione della salute: ne toglie una dalle scorte e gli restituisce un po' di salute.
	 */
	public final void consumaPozioneSalute(Personaggio personaggio) {
		subPozioniSalute(1);
		personaggio.addSalute(CostantiPersonaggi.RECUPERO_DA_POZIONE_SALUTE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che fa riacquistare salute."));
	}

	public final int getPozioniSaluteGrande() {
		return getGruppoMD().getPozioniSaluteGrande();
	}
	
	/**
	 * Aggiunge pozioni della salute grandi alle scorte; il gruppo del giocatore lo annuncia alla UI.
	 */
	public final void addPozioniSaluteGrande(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniSaluteGrande();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniSaluteGrande(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniSaluteGrandi(valorePrecedente, valoreAttuale));
		}
	}

	/**
	 * Toglie pozioni della salute grandi dalle scorte.
	 */
	public final void subPozioniSaluteGrande(int quantita) {
		addPozioniSaluteGrande(-quantita);
	}

	/**
	 * Fa bere a quel personaggio una pozione della salute grande: ne toglie una dalle scorte, gli alza la salute massima e lo cura.
	 */
	public final void consumaPozioneSaluteGrande(Personaggio personaggio) {
		subPozioniSaluteGrande(1);
		personaggio.addSaluteMassima(CostantiPersonaggi.AUMENTO_SALUTE_DA_POZIONE_SALUTE_GRANDE, "POZIONE_SALUTE_GRANDE");
		personaggio.addSalute(CostantiPersonaggi.RECUPERO_DA_POZIONE_SALUTE_GRANDE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che recupera e fa aumentare la salute massima!"));
	}

	public final int getPozioniMagia() {
		return getGruppoMD().getPozioniMagia();
	}

	/**
	 * Aggiunge pozioni della magia alle scorte; il gruppo del giocatore lo annuncia alla UI.
	 */
	public final void addPozioniMagia(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniMagia();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniMagia(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniMagia(valorePrecedente, valoreAttuale));
		}
	}

	/**
	 * Toglie pozioni della magia dalle scorte.
	 */
	public final void subPozioniMagia(int quantita) {
		addPozioniMagia(-quantita);
	}

	/**
	 * Fa bere a quel personaggio una pozione della magia: ne toglie una dalle scorte e gli restituisce un po' di magia.
	 */
	public final void consumaPozioneMagia(Personaggio personaggio) {
		subPozioniMagia(1);
		personaggio.addMagia(CostantiPersonaggi.RECUPERO_DA_POZIONE_MAGIA);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che fa riacquistare magia."));
	}

	public final int getPozioniMagiaGrande() {
		return getGruppoMD().getPozioniMagiaGrande();
	}

	/**
	 * Aggiunge pozioni della magia grandi alle scorte; il gruppo del giocatore lo annuncia alla UI.
	 */
	public final void addPozioniMagiaGrande(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniMagiaGrande();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniMagiaGrande(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniMagiaGrandi(valorePrecedente, valoreAttuale));
		}
	}

	/**
	 * Toglie pozioni della magia grandi dalle scorte.
	 */
	public final void subPozioniMagiaGrande(int quantita) {
		addPozioniMagiaGrande(-quantita);
	}

	/**
	 * Fa bere a quel personaggio una pozione della magia grande: ne toglie una dalle scorte, gli alza la magia massima e gli restituisce magia.
	 */
	public final void consumaPozioneMagiaGrande(Personaggio personaggio) {
		subPozioniMagiaGrande(1);
		personaggio.addMagiaMassima(CostantiPersonaggi.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "POZIONE_MAGIA_GRANDE");
		personaggio.addMagia(CostantiPersonaggi.RECUPERO_DA_POZIONE_MAGIA_GRANDE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che recupera e fa aumentare la magia massima!"));
	}

	public final int getPozioniAumentoLivello() {
		return getGruppoMD().getPozioniAumentoLivello();
	}

	/**
	 * Aggiunge pozioni dell'aumento di livello alle scorte; il gruppo del giocatore lo annuncia alla UI.
	 */
	public final void addPozioniAumentoLivello(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniAumentoLivello();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniAumentoLivello(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniAumentoLivello(valorePrecedente, valoreAttuale));
		}
	}

	/**
	 * Toglie pozioni dell'aumento di livello dalle scorte.
	 */
	public final void subPozioniAumentoLivello(int quantita) {
		addPozioniAumentoLivello(-quantita);
	}

	/**
	 * Se c'è una pozione dell'aumento di livello da bere e almeno un personaggio vivo che non sia già al livello massimo.
	 */
	public final boolean isPozioneAumentoLivelloUtilizzabile() {
		return getPozioniAumentoLivello() > 0
				&& getPersonaggiVivi().stream().anyMatch(p -> !p.isAlLivelloMassimo());
	}

	/**
	 * Fa bere a quel personaggio una pozione dell'aumento di livello: ne toglie una dalle scorte e gli dà l'esperienza che
	 * serve per salire di un livello, tenendo la stessa percentuale che gli mancava per quello dopo (vedi
	 * {@link GestoreProgressione#getXpPerAumentoDiLivello}). Se è già al livello massimo non fa nulla e la pozione resta.
	 */
	public final void consumaPozioneAumentoLivello(Personaggio personaggio) {
		String chi = personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		if (personaggio.isAlLivelloMassimo()) {
			BusEventi.pubblica(new NotificaTestoFrase(chi + " è già al livello massimo: la pozione non avrebbe effetto."));
			return;
		}
		subPozioniAumentoLivello(1);
		personaggio.addPuntiEsperienza(GestoreProgressione.getXpPerAumentoDiLivello(personaggio.getLivello(), personaggio.getEsperienza()));
		BusEventi.pubblica(new NotificaTestoFrase(chi + " ha bevuto una pozione che fa salire di livello!"));
	}

	/**
	 * Riporta se il gruppo sia quello del giocatore o il gruppo avversario
	 */
	public abstract boolean isGruppoGiocatore();

	/**
	 * Riporta il numero di tutti i personaggi, compresi eventualmente
	 * quelli passati a miglior vita
	 */
	public final int getNumeroPersonaggi() {
		return personaggi.size();
	}

	public final int getNumeroPersonaggiVivi() {
		return getPersonaggiVivi().size();
	}

	/**
	 * Riporta il numero di personaggi permanenti (esclusi quelli a tempo).
	 */
	public final int getNumeroPersonaggiPermanenti() {
		return (int) personaggi.stream()
				.filter(p -> !p.isATempo())
				.count();
	}

	public final List<Personaggio> getPersonaggi() {
		return personaggi;
	}
	
	/**
	 * I personaggi vivi e in campo: chi è in panchina non conta (vedi Personaggio.isInPanchina).
	 */
	public final List<Personaggio> getPersonaggiVivi() {
		return personaggi.stream().filter(p -> !p.isFuoriCombattimento()).collect(Collectors.toList());
	}

	/**
	 * Il personaggio vivo con la quota di salute più bassa, se qualcuno è ferito (a parità, il primo del gruppo). Chi non
	 * si può curare (un non morto nel gruppo) non conta
	 */
	public final Optional<Personaggio> piuFerito() {
		Personaggio piuFerito = null;
		double quotaMinima = 1.0d;
		for (Personaggio personaggio : getPersonaggiVivi()) {
			if (!personaggio.isCurabile()) {
				continue;
			}
			double quota = (double) personaggio.getSalute() / personaggio.getSaluteMassima();
			if (quota < quotaMinima) {
				quotaMinima = quota;
				piuFerito = personaggio;
			}
		}
		return Optional.ofNullable(piuFerito);
	}

	/**
	 * Rimette in campo chi era in panchina: a ogni nuova locazione.
	 */
	public final void svuotaPanchina() {
		personaggi.forEach(p -> p.setInPanchina(false));
	}

	/**
	 * Se qualcuno del gruppo va affrontato per forza (vedi Personaggio.isDaAffrontare).
	 */
	public final boolean isDaAffrontare() {
		return personaggi.stream().anyMatch(Personaggio::isDaAffrontare);
	}

	/**
	 * Se qualcuno del gruppo sfida a duello (vedi Personaggio.isSfidante).
	 */
	public final boolean isDuello() {
		return personaggi.stream().anyMatch(Personaggio::isSfidante);
	}

	/**
	 * Se qualcuno del gruppo sfida a carta, forbici e sasso (vedi Personaggio.isSfidanteACartaForbiciSasso).
	 */
	public final boolean isSfidaACartaForbiciSasso() {
		return personaggi.stream().anyMatch(Personaggio::isSfidanteACartaForbiciSasso);
	}

	/**
	 * Se qualcuno del gruppo combatte fino alla resa (vedi Personaggio.isFinoAllaResa).
	 */
	public final boolean isFinoAllaResa() {
		return personaggi.stream().anyMatch(Personaggio::isFinoAllaResa);
	}

	/**
	 * Aggiunge un personaggio al gruppo; il primo che entra ne è il capo.
	 */
	public void aggiungiPersonaggio(Personaggio p) {
		if (capo == null) {
			capo = p;
		}
		personaggi.add(p);
	}

	public final Personaggio getCapo() {
		return capo;
	}
	
	public final Personaggio getPersonaggio(int numero) {
		return personaggi.get(numero);
	}

	/**
	 * Gli ospiti che un avversario può attaccare, come i personaggi del gruppo (vedi GruppoGiocatore): per gli altri
	 * gruppi nessuno.
	 */
	public List<Personaggio> getOspitiBersaglio() {
		return Collections.emptyList();
	}

	/**
	 * Sposta un personaggio permanente di un posto a sinistra (-1) o a destra (+1), scambiandolo con il permanente
	 * vicino (chi è a tempo non conta). Il capo resta quello che era.
	 *
	 * @return false se non si può: il personaggio non c'è, è a tempo, o è già al margine
	 */
	public boolean spostaPersonaggio(Personaggio p, int direzione) {
		int da = personaggi.indexOf(p);
		if (da < 0 || p.isATempo() || (direzione != -1 && direzione != 1)) {
			return false;
		}
		int a = da + direzione;
		while (a >= 0 && a < personaggi.size() && personaggi.get(a).isATempo()) {
			a += direzione;
		}
		if (a < 0 || a >= personaggi.size()) {
			return false;
		}
		Personaggio altro = personaggi.get(a);
		personaggi.set(a, p);
		personaggi.set(da, altro);
		return true;
	}

	/**
	 * Toglie tutti i personaggi e il capo.
	 */
	public void rimuoviPersonaggi() {
		personaggi.clear();
		capo = null;
	}

	/**
	 * Toglie il personaggio dal gruppo; se era il capo, il nuovo capo è il primo dei personaggi rimasti, altrimenti il capo resta quello che era.
	 */
	public void rimuoviPersonaggio(Personaggio p) {
		personaggi.remove(p);
		if (p.equals(capo)) {
			capo = personaggi.isEmpty() ? null : personaggi.get(0);
		}
	}

	/**
	 * Chi parla, per le frasi dei testi: "il gruppo" se sono più di uno, altrimenti il capo con l'articolo.
	 */
	public String chi() {
		return personaggi.size() > 1 ? "il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
	}

	/**
	 * Come {@link #chi()}, con l'iniziale maiuscola.
	 */
	public String chiMaiuscolo() {
		return personaggi.size() > 1 ? "Il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
	}
	
	@Override
	public boolean contiene(VistaPersonaggio personaggio) {
		return personaggi.contains(personaggio);
	}
}
