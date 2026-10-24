package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.combattimento.Ondata;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.*;

/**
 * Il gruppo dei mostri che il gruppo del giocatore affronta in una locazione. Esiste una sola istanza, che si svuota a ogni locazione; tiene anche
 * le ondate di rinforzi che arrivano, una dopo l'altra, quando gli avversari in campo sono stati sconfitti tutti (vedi {@link Ondata}).
 */
public class GruppoAvversario extends Gruppo {

	// Le ondate che arrivano, una dopo l'altra, quando gli avversari in campo sono sconfitti tutti (vedi Ondata). Non
	// si salvano: si salva solo fra una locazione e l'altra, mai a metà combattimento
	private final Deque<Ondata> ondateSuccessive = new ArrayDeque<>();

	// Chi è caduto ed è stato tolto dal gruppo prima che lo scontro lo contasse (il minion della Mietitura, che muore e viene sostituito da uno
	// scheletro del giocatore): chi conta gli sconfitti lo trova qui (vedi getCadutiRimossi)
	private final List<Personaggio> cadutiRimossi = new ArrayList<>();

	private GruppoAvversario() {
		super();
	}
	
	private static GruppoAvversario gruppoAvversario;
	
	public static GruppoAvversario getIstanza() {
		if (gruppoAvversario == null) {
			gruppoAvversario = new GruppoAvversario();
		}
		return gruppoAvversario;
	}

	/**
	 * Dimentica il gruppo avversario corrente. Serve ai test (vedi GruppoGiocatore.azzeraIstanza).
	 */
	public static void azzeraIstanza() {
		gruppoAvversario = null;
	}

	@Override
	protected void reimposta() {
		super.reimposta();
		// Le scorte che i mostri avevano con sé (vedi Gruppo.getGruppoMD) se ne vanno con loro
		getGruppoMD().reimposta();
		ondateSuccessive.clear();
		cadutiRimossi.clear();
	}

	/**
	 * Toglie il personaggio dal gruppo; se era già fuori combattimento lo ricorda fra {@link #getCadutiRimossi()}, così non sfugge al conteggio
	 * degli sconfitti.
	 */
	@Override
	public void rimuoviPersonaggio(Personaggio p) {
		if (p.isFuoriCombattimento() && personaggi.contains(p) && !cadutiRimossi.contains(p)) {
			cadutiRimossi.add(p);
		}
		super.rimuoviPersonaggio(p);
	}

	@Override
	public void rimuoviPersonaggi() {
		super.rimuoviPersonaggi();
		cadutiRimossi.clear();
	}

	/**
	 * Chi è caduto ed è stato tolto dal gruppo (un minion mietuto) da quando il gruppo si è formato.
	 */
	public List<Personaggio> getCadutiRimossi() {
		return Collections.unmodifiableList(cadutiRimossi);
	}

	/**
	 * Le ondate che arriveranno dopo gli avversari in campo, nell'ordine.
	 */
	public void setOndateSuccessive(List<Ondata> ondate) {
		ondateSuccessive.clear();
		ondateSuccessive.addAll(ondate);
	}

	/**
	 * Se arriverà ancora un'ondata, sconfitti gli avversari in campo.
	 */
	public boolean hasOndateSuccessive() {
		return !ondateSuccessive.isEmpty();
	}

	/**
	 * Mette in campo la prossima ondata, al posto degli avversari sconfitti, e la restituisce.
	 */
	public Ondata prossimaOndata() {
		Ondata ondata = ondateSuccessive.removeFirst();
		rimuoviPersonaggi();
		ondata.getAvversari().forEach(this::aggiungiPersonaggio);
		return ondata;
	}

	@Override
	public boolean isGruppoGiocatore() {
		return false;
	}

	/**
	 * L'avversario prende come ordinale la sua posizione fra i mostri della sua specie nel gruppo (il primo scheletro, il secondo
	 * scheletro, il primo goblin, il terzo scheletro...), che serve a distinguerli (vedi PersonaggioBase.getNome): così il gruppo può
	 * essere eterogeneo, ogni specie conta per sé.
	 */
	@Override
	public void aggiungiPersonaggio(Personaggio p) {
		p.setOrdinale(contaDellaStessaSpecie(p) + 1);
		super.aggiungiPersonaggio(p);
	}

	/**
	 * Quanti personaggi del gruppo (anche caduti) hanno la stessa specie di {@code p}, lui compreso se è già nel gruppo.
	 */
	public int contaDellaStessaSpecie(Personaggio p) {
		String specie = p.getNomeSingolare();
		int quanti = 0;
		for (Personaggio altro : personaggi) {
			if (specie.equals(altro.getNomeSingolare())) {
				quanti++;
			}
		}
		return quanti;
	}

	public Personaggio getPersonaggioVivo() {
		for (Personaggio personaggio : personaggi) {
			if (!personaggio.isFuoriCombattimento()) {
				return personaggio;
			}
		}
		return null;
	}
}
