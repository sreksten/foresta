package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class GruppoAvversario extends Gruppo {

	// Le ondate che arrivano, una dopo l'altra, quando gli avversari in campo sono sconfitti tutti (vedi Ondata). Non
	// si salvano: si salva solo fra una locazione e l'altra, mai a metà combattimento
	private final Deque<Ondata> ondateSuccessive = new ArrayDeque<>();

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
	static void azzeraIstanza() {
		gruppoAvversario = null;
	}

	@Override
	protected void reimposta() {
		super.reimposta();
		ondateSuccessive.clear();
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

	public Personaggio getPersonaggioVivo() {
		for (Personaggio personaggio : personaggi) {
			if (!personaggio.isFuoriCombattimento()) {
				return personaggio;
			}
		}
		return null;
	}
}
