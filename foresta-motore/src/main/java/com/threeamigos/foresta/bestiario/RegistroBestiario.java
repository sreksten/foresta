package com.threeamigos.foresta.bestiario;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAmiciziaStretta;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.interni.InternoCaratteristicaOsservata;
import com.threeamigos.foresta.eventi.interni.InternoCorruzioneRiuscita;
import com.threeamigos.foresta.eventi.interni.InternoPersonaggioEntratoNelGruppo;
import com.threeamigos.foresta.eventi.interni.InternoSfidaVinta;
import com.threeamigos.foresta.eventi.interni.InternoVoceDelBestiarioSbloccata;
import com.threeamigos.foresta.interfacce.GestoreBestiario;
import com.threeamigos.foresta.modellodati.BestiarioMD;
import com.threeamigos.foresta.strumenti.Logger;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Facciata su {@link BestiarioMD}: ricorda quali voci del bestiario si sono sbloccate e quali caratteristiche si sono scoperte, e ascolta gli eventi
 * che le fanno avanzare. Passa da una partita all'altra come i trofei: il modello si legge una volta all'avvio (vedi
 * {@link #impostaGestoreBestiario}) e non viene mai reimpostato. Si salva subito a ogni novità, che è rara (una voce o una caratteristica per
 * volta, e ogni scoperta una volta sola in tutte le partite).
 * <p>
 * Una voce è un ruolo (vedi {@link TipoPersonaggio#getRuolo()}) e si sblocca in due modi: si sconfigge un avversario di quella classe (anche
 * facendoci amicizia, corrompendolo o battendolo a carta, forbici e sasso) oppure un personaggio di quella classe entra nel gruppo. Solo le
 * classi che hanno una voce ({@link TipoPersonaggio#hasVoceNelBestiario()}) contano: un locandiere o un viandante no.
 * <p>
 * Le caratteristiche si scoprono indipendentemente dalla voce: un colpo di fuoco su una classe dice come reagisce al fuoco anche se non l'hai ancora
 * sconfitta, e il bestiario lo ricorda. La voce però le mostra solo da sbloccata.
 */
public final class RegistroBestiario {

	private static BestiarioMD bestiarioMD = new BestiarioMD();
	private static GestoreBestiario gestoreBestiario;

	private RegistroBestiario() {
	}

	/**
	 * Imposta dove leggere e salvare il bestiario, e lo rilegge da lì.
	 */
	public static synchronized void impostaGestoreBestiario(GestoreBestiario gestore) {
		gestoreBestiario = gestore;
		bestiarioMD = new BestiarioMD();
		gestoreBestiario.carica(bestiarioMD);
	}

	/**
	 * Si iscrive agli eventi che sbloccano le voci e a quello che fa scoprire una caratteristica.
	 */
	public static void registrati() {
		BusEventi.iscriviti(InternoAvversarioSconfitto.class, evento -> sblocca(evento.getClasse()));
		BusEventi.iscriviti(InternoAmiciziaStretta.class, evento -> evento.getAvversari().forEach(RegistroBestiario::sblocca));
		BusEventi.iscriviti(InternoCorruzioneRiuscita.class, evento -> evento.getAvversari().forEach(RegistroBestiario::sblocca));
		BusEventi.iscriviti(InternoSfidaVinta.class, evento -> sblocca(evento.getAvversario()));
		BusEventi.iscriviti(InternoPersonaggioEntratoNelGruppo.class, evento -> sblocca(evento.getClasse()));
		BusEventi.iscriviti(InternoCaratteristicaOsservata.class,
				evento -> osserva(evento.getClasse(), evento.getGenere(), evento.getValore()));
	}

	/**
	 * Se la voce di quella classe (o di quella che ne condivide il ruolo) è sbloccata
	 */
	public static synchronized boolean isSbloccata(TipoPersonaggio classe) {
		return bestiarioMD.isSbloccata(classe);
	}

	/**
	 * Quante voci sono sbloccate
	 */
	public static synchronized int getNumeroVociSbloccate() {
		return bestiarioMD.getSbloccate().size();
	}

	/**
	 * Quante voci ha il bestiario in tutto: tutte quelle che si possono sbloccare
	 */
	public static int getNumeroVoci() {
		return TipoPersonaggio.getVociDelBestiario().size();
	}

	/**
	 * Le chiavi delle caratteristiche già scoperte per la voce di quella classe
	 */
	public static synchronized Set<String> getCaratteristicheScoperte(TipoPersonaggio classe) {
		return Collections.unmodifiableSet(new LinkedHashSet<>(bestiarioMD.getScoperte(classe)));
	}

	/**
	 * Sblocca la voce di quella classe; non fa niente se la classe non ha una voce (un non combattente) o se era già sbloccata.
	 */
	static void sblocca(TipoPersonaggio classe) {
		Logger.log(() -> "Bestiario: richiesto lo sblocco di " + classe + (classe.hasVoceNelBestiario() ? "" : " (non ha una voce)"));
		if (!classe.hasVoceNelBestiario()) {
			return;
		}
		if (sbloccaESalva(classe)) {
			// Fuori dal blocco: chi ascolta può richiamare il registro
			BusEventi.pubblica(new InternoVoceDelBestiarioSbloccata(classe.getRuolo()));
		}
	}

	private static synchronized boolean sbloccaESalva(TipoPersonaggio classe) {
		if (!bestiarioMD.sblocca(classe)) {
			return false;
		}
		salva();
		return true;
	}

	/**
	 * Ricorda che si è visto come la classe reagisce a quel tipo di danno, incantesimo o effetto di stato, se è davvero una sua caratteristica
	 * (una classe che subisce il danno normale non ha niente da scoprire). L'immunità a tutti gli incantesimi (il gargoyle) si scopre con uno
	 * qualunque.
	 */
	static synchronized void osserva(TipoPersonaggio classe, CaratteristicaDiClasse.Genere genere, Enum<?> valore) {
		List<CaratteristicaDiClasse> caratteristiche = classe.getCaratteristiche();
		for (CaratteristicaDiClasse caratteristica : caratteristiche) {
			boolean quella = caratteristica.getGenere() == genere
					&& (caratteristica.getValore() == valore || (genere == CaratteristicaDiClasse.Genere.INCANTESIMO && caratteristica.getValore() == null));
			if (quella && bestiarioMD.scopri(classe, caratteristica.getChiave())) {
				salva();
			}
		}
	}

	// Senza un gestore impostato (una partita di prova) il bestiario resta in memoria
	private static void salva() {
		if (gestoreBestiario != null) {
			gestoreBestiario.salva(bestiarioMD);
		}
	}
}
