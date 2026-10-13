package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaPersonaggio;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Le pergamene e le pozioni che il gruppo avversario porta con sé e che, se non le usa, diventano bottino.
 * <ul>
 * <li>Quando il gruppo si popola (all'inizio della locazione, o quando arriva un'ondata), per <b>ogni avversario</b> c'è una probabilità
 * (Costanti.PROBABILITA_PERGAMENA_PER_AVVERSARIO, il 20%) di aggiungere una pergamena: se è un <b>umanoide</b> un incantesimo qualsiasi;
 * altrimenti, se è <b>magico</b> (sa lanciare incantesimi per natura), una di uno dei suoi incantesimi (il suo repertorio: uno spirito
 * una di Gelo).</li>
 * <li>Per ogni avversario <b>umanoide</b> c'è anche la stessa probabilità (Costanti.PROBABILITA_POZIONE_SALUTE_PER_AVVERSARIO_UMANOIDE)
 * di una pozione della Salute.</li>
 * <li>Vinto lo scontro, tutto ciò che resta delle scorte degli avversari passa al giocatore e lo si dice con un messaggio.</li>
 * </ul>
 */
public final class BottinoDegliAvversari {

	private BottinoDegliAvversari() {
	}

	/**
	 * Aggiunge alla scorta del gruppo avversario le pergamene portate da questi avversari, appena entrati.
	 */
	public static void fornisciLeScorte(GruppoAvversario gruppo, Collection<Personaggio> avversari) {
		for (Personaggio avversario : avversari) {
			boolean umanoide = avversario.getClasse().isDellaCategoria(CategoriaPersonaggio.UMANOIDE);
			if (Dado.tira(100) <= Costanti.PROBABILITA_PERGAMENA_PER_AVVERSARIO) {
				ClasseIncantesimo pergamena = scegliLaPergamena(avversario, umanoide);
				if (pergamena != null) {
					gruppo.addIncantesimi(pergamena, 1);
				}
			}
			if (umanoide && Dado.tira(100) <= Costanti.PROBABILITA_POZIONE_SALUTE_PER_AVVERSARIO_UMANOIDE) {
				gruppo.addPozioniSalute(1);
			}
		}
	}

	private static ClasseIncantesimo scegliLaPergamena(Personaggio avversario, boolean umanoide) {
		if (umanoide) {
			ClasseIncantesimo[] tutti = ClasseIncantesimo.values();
			return tutti[Dado.tiraAncheAUnaFaccia(tutti.length) - 1];
		}
		if (avversario.isMagico() && !avversario.getRepertorioIncantesimi().isEmpty()) {
			List<ClasseIncantesimo> innati = new ArrayList<>(avversario.getRepertorioIncantesimi());
			innati.sort(null);
			return innati.get(Dado.tiraAncheAUnaFaccia(innati.size()) - 1);
		}
		return null;
	}

	/**
	 * Passa al giocatore ciò che resta delle scorte del gruppo avversario (pergamene e pozioni), e lo dice. Non fa niente se non
	 * resta niente.
	 */
	public static void raccogli(GruppoGiocatore giocatore, GruppoAvversario avversari) {
		List<String> trovati = new ArrayList<>();
		for (ClasseIncantesimo classe : ClasseIncantesimo.values()) {
			int quantita = avversari.getIncantesimi(classe);
			if (quantita > 0) {
				avversari.subIncantesimi(classe, quantita);
				giocatore.addIncantesimi(classe, quantita);
				trovati.add(quantita + " " + (quantita == 1 ? classe.getNomeSingolare() : classe.getNomePlurale()));
			}
		}
		raccogliPozioni(avversari.getPozioniSalute(), "pozione della Salute", "pozioni della Salute", trovati,
				q -> { avversari.subPozioniSalute(q); giocatore.addPozioniSalute(q); });
		raccogliPozioni(avversari.getPozioniSaluteGrande(), "pozione grande della Salute", "pozioni grandi della Salute", trovati,
				q -> { avversari.subPozioniSaluteGrande(q); giocatore.addPozioniSaluteGrande(q); });
		raccogliPozioni(avversari.getPozioniMagia(), "pozione della Magia", "pozioni della Magia", trovati,
				q -> { avversari.subPozioniMagia(q); giocatore.addPozioniMagia(q); });
		raccogliPozioni(avversari.getPozioniMagiaGrande(), "pozione grande della Magia", "pozioni grandi della Magia", trovati,
				q -> { avversari.subPozioniMagiaGrande(q); giocatore.addPozioniMagiaGrande(q); });
		if (!trovati.isEmpty()) {
			BusEventi.pubblica(new NotificaTestoParagrafo("Fra i resti degli avversari si trovano: "
					+ String.join(", ", trovati) + "."));
		}
	}

	private static void raccogliPozioni(int quantita, String singolare, String plurale, List<String> trovati,
										java.util.function.IntConsumer sposta) {
		if (quantita > 0) {
			sposta.accept(quantita);
			trovati.add(quantita + " " + (quantita == 1 ? singolare : plurale));
		}
	}
}
