package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.modellodati.GruppoMD;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;
import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Un insieme di personaggi
 */

public abstract class Gruppo implements VistaGruppo {
	
	// Caratteristiche del gruppo
	protected List<Personaggio> personaggi = new ArrayList<>();
	protected Personaggio capo;

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

	public final int getIncantesimi(ClasseIncantesimo classeIncantesimo) {
		return getGruppoMD().getIncantesimi(classeIncantesimo);
	}

	public final void addIncantesimi(ClasseIncantesimo classeIncantesimo, int quantita) {
		int valorePrecedente = getGruppoMD().getIncantesimi(classeIncantesimo);
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setIncantesimi(classeIncantesimo, valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaIncantesimi(classeIncantesimo, valorePrecedente, valoreAttuale));
		}
	}

	public final void subIncantesimi(ClasseIncantesimo classeIncantesimo, int quantita) {
		addIncantesimi(classeIncantesimo, -quantita);
	}
	
	public final int getPozioniSalute() {
		return getGruppoMD().getPozioniSalute();
	}

	public final void addPozioniSalute(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniSalute();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniSalute(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniSalute(valorePrecedente, valoreAttuale));
		}
	}

	public final void subPozioniSalute(int quantita) {
		addPozioniSalute(-quantita);
	}

	public final void consumaPozioneSalute(Comando azione) {
		consumaPozioneSalute(getPersonaggio(azione));
	}

	public final void consumaPozioneSalute(Personaggio personaggio) {
		subPozioniSalute(1);
		personaggio.addSalute(Costanti.RECUPERO_DA_POZIONE_SALUTE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che fa riacquistare salute."));
	}

	public final int getPozioniSaluteGrande() {
		return getGruppoMD().getPozioniSaluteGrande();
	}
	
	public final void addPozioniSaluteGrande(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniSaluteGrande();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniSaluteGrande(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniSaluteGrandi(valorePrecedente, valoreAttuale));
		}
	}

	public final void subPozioniSaluteGrande(int quantita) {
		addPozioniSaluteGrande(-quantita);
	}

	public final void consumaPozioneSaluteGrande(Comando azione) {
		consumaPozioneSaluteGrande(getPersonaggio(azione));
	}

	public final void consumaPozioneSaluteGrande(Personaggio personaggio) {
		subPozioniSaluteGrande(1);
		personaggio.addSaluteMassima(Costanti.AUMENTO_SALUTE_DA_POZIONE_SALUTE_GRANDE, "POZIONE_SALUTE_GRANDE");
		personaggio.addSalute(Costanti.RECUPERO_DA_POZIONE_SALUTE_GRANDE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che recupera e fa aumentare la salute massima!"));
	}

	public final int getPozioniMagia() {
		return getGruppoMD().getPozioniMagia();
	}

	public final void addPozioniMagia(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniMagia();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniMagia(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniMagia(valorePrecedente, valoreAttuale));
		}
	}

	public final void subPozioniMagia(int quantita) {
		addPozioniMagia(-quantita);
	}

	public final void consumaPozioneMagia(Comando azione) {
		consumaPozioneMagia(getPersonaggio(azione));
	}

	public final void consumaPozioneMagia(Personaggio personaggio) {
		subPozioniMagia(1);
		personaggio.addMagia(Costanti.RECUPERO_DA_POZIONE_MAGIA);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che fa riacquistare magia."));
	}

	public final int getPozioniMagiaGrande() {
		return getGruppoMD().getPozioniMagiaGrande();
	}

	public final void addPozioniMagiaGrande(int quantita) {
		int valorePrecedente = getGruppoMD().getPozioniMagiaGrande();
		int valoreAttuale = valorePrecedente + quantita;
		getGruppoMD().setPozioniMagiaGrande(valoreAttuale);
		if (isGruppoGiocatore()) {
			BusEventi.pubblica(new NotificaVariazioneDisponibilitaPozioniMagiaGrandi(valorePrecedente, valoreAttuale));
		}
	}

	public final void subPozioniMagiaGrande(int quantita) {
		addPozioniMagiaGrande(-quantita);
	}

	public final void consumaPozioneMagiaGrande(Comando azione) {
		consumaPozioneMagiaGrande(getPersonaggio(azione));
	}

	public final void consumaPozioneMagiaGrande(Personaggio personaggio) {
		subPozioniMagiaGrande(1);
		personaggio.addMagiaMassima(Costanti.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "POZIONE_MAGIA_GRANDE");
		personaggio.addMagia(Costanti.RECUPERO_DA_POZIONE_MAGIA_GRANDE);
		BusEventi.pubblica(new NotificaTestoFrase(personaggio.getNome(
				Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
				" ha bevuto una pozione che recupera e fa aumentare la magia massima!"));
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

	public final Personaggio getPersonaggio(Comando azione) {
		return personaggi.get(azione.ordinal() - Comando.PERSONAGGIO_1.ordinal());
	}

	/**
	 * Gli ospiti che un avversario può attaccare, come i personaggi del gruppo (vedi GruppoGiocatore): per gli altri
	 * gruppi nessuno.
	 */
	public List<Personaggio> getOspitiBersaglio() {
		return Collections.emptyList();
	}

	public void rimuoviPersonaggi() {
		personaggi.clear();
		capo = null;
	}

	public void rimuoviPersonaggio(Personaggio p) {
		if (p.equals(capo)) {
			capo = null;
		}
		personaggi.remove(p);
		if (!personaggi.isEmpty()) {
			capo = personaggi.get(0);
		}
	}

	public String chi() {
		return personaggi.size() > 1 ? "il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
	}

	public String chiMaiuscolo() {
		return personaggi.size() > 1 ? "Il gruppo" : capo.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
	}
	
	@Override
	public boolean contiene(VistaPersonaggio personaggio) {
		return personaggi.contains(personaggio);
	}
}
