package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.eventi.notifiche.NotificaAumentoLivelloMondo;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePunteggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazionePuntiEsperienzaPersonaggio;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.modellodati.StatisticheMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Le statistiche della partita: punti, esperienza, livello del gioco e turni giocati. I dati stanno nello {@code StatisticheMD}; qui si aggiornano,
 * e le variazioni si annunciano alla UI con le notifiche.
 */
public class Statistiche {

	private static StatisticheMD getStatisticheMD() {
		return ModelloDati.getIstanza().getStatisticheMD();
	}

	private Statistiche() {
	}

	/**
	 * Si iscrive agli eventi che le statistiche tengono in conto, come gli avversari sconfitti.
	 */
	public static void registrati() {
		BusEventi.iscriviti(InternoAvversarioSconfitto.class, evento -> addMostroUcciso(evento.getClasse()));
	}

	/**
	 * Aggiunge punti al punteggio e lo annuncia alla UI.
	 */
	public static void addPunti(int quantita) {
		StatisticheMD md = getStatisticheMD();
		int valorePrecedente = md.getPunti();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPunti(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazionePunteggio(valorePrecedente, valoreAttuale));
	}

	// Chiamato ogni volta che il personaggio completa una missione o uccide un mostro
	/**
	 * Aggiunge punti esperienza e lo annuncia alla UI.
	 */
	public static void addPuntiEsperienza(int quantita) {
		StatisticheMD md = getStatisticheMD();
		int valorePrecedente = md.getPuntiEsperienza();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPuntiEsperienza(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazionePuntiEsperienzaPersonaggio(valorePrecedente, valoreAttuale));

		// Verifichiamo se i nuovi XP accumulati determinano un salto di livello
		int livelloAttuale = md.getLivello();
		int nuovoLivello = GestoreProgressione.calcolaLivelloDaXp(md.getPuntiEsperienza());

		if (nuovoLivello > livelloAttuale) {
			md.setLivello(nuovoLivello);
			BusEventi.pubblica(new NotificaAumentoLivelloMondo(nuovoLivello));
		}
	}

	public static int getPunti() {
		return getStatisticheMD().getPunti();
	}

	public static int getPuntiEsperienza() {
		return getStatisticheMD().getPuntiEsperienza();
	}

	public static int getLivello() {
		return getStatisticheMD().getLivello();
	}

	/**
	 * Per la modalità di prova: porta il livello del mondo a quello dato e i punti esperienza a quelli che servono per arrivarci
	 * (altrimenti il livello tornerebbe a salire al primo punto guadagnato). Salendo lo annuncia come un aumento vero; scendendo no,
	 * perché chi ascolta (gli artefatti, la UI) conosce solo gli aumenti.
	 */
	public static void impostaLivelloDelMondo(int livello) {
		StatisticheMD md = getStatisticheMD();
		int precedente = md.getLivello();
		int esperienzaPrecedente = md.getPuntiEsperienza();
		int nuovaEsperienza = GestoreProgressione.getXpNecessariPerLivello(livello);
		md.setLivello(livello);
		md.setPuntiEsperienza(nuovaEsperienza);
		BusEventi.pubblica(new NotificaVariazionePuntiEsperienzaPersonaggio(esperienzaPrecedente, nuovaEsperienza));
		if (livello > precedente) {
			BusEventi.pubblica(new NotificaAumentoLivelloMondo(livello));
		}
	}

	public static int getPuntiEsperienzaPerProssimoLivello() {
		return GestoreProgressione.getXpPerProssimoLivello(getLivello());
	}

	private static void addMostroUcciso(TipoPersonaggio classe) {
		getStatisticheMD().addMostroUcciso(classe);
	}

	public static int getMostriUccisi(TipoPersonaggio classe) {
		return getStatisticheMD().getMostriUccisi(classe);
	}

	/**
	 * Conta un turno giocato in più.
	 */
	public static void incrementaTurniGiocati() {
		StatisticheMD md = getStatisticheMD();
		md.setTurniGiocati(md.getTurniGiocati() + 1);
	}

	public static int getTurniGiocati() {
		return getStatisticheMD().getTurniGiocati();
	}

	/**
	 * L'identificativo della partita, che resta lo stesso dopo un salvataggio e un caricamento.
	 */
	public static String getIdPartita() {
		return getStatisticheMD().getIdPartita();
	}

	/**
	 * Azzera le statistiche per una partita nuova.
	 */
	public static void reimposta() {
		getStatisticheMD().reimposta();
	}
}
