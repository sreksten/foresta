package com.threeamigos.foresta.notizie;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.modellodati.MessaggioMD;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.modellodati.NotizieMD;

import java.util.List;
import java.util.function.Supplier;

/**
 * Facciata su {@link NotizieMD}: tiene gli ultimi messaggi mostrati al giocatore (per poter ripopolare il pannello di
 * testo dopo un ricaricamento) e le ultime notizie destinate alla schermata della mappa. Il modello dati resta un
 * bean di soli dati; la logica di rotazione (nuovo elemento in testa, scarto dei più vecchi oltre il limite) vive qui.
 * <p>
 * I messaggi li prende dal bus, ascoltando le notifiche di testo che il motore pubblica per la UI: sono centinaia i
 * punti che le pubblicano, e lo storico è proprio quel che il giocatore ha visto, quindi ascoltarle è la scelta
 * giusta. Le notizie, invece, le riceve da chi le produce (vedi {@link #aggiungiNotizia}): nessun altro le ascolta.
 * <p>
 * Il dato non lo possiede: lo chiede a ogni uso a chi lo custodisce (vedi {@link #setFornitoreDiDati}).
 *
 * @author Stefano Reksten
 */
public class Notizie {

	private Notizie() {
	}

	private static volatile Supplier<NotizieMD> fornitoreDiDati;

	/**
	 * Dice da dove prendere i dati della partita in corso (lo fa chi li custodisce: ogni volta si prendono da quelli in corso).
	 */
	public static void setFornitoreDiDati(Supplier<NotizieMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static NotizieMD getNotizieMD() {
		Supplier<NotizieMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto dove sono i dati delle Notizie (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	/**
	 * Si iscrive al bus eventi per i messaggi di testo. Va chiamato una sola volta, prima che il gioco inizi a
	 * pubblicare notifiche (vedi Main).
	 */
	public static void registrati() {
		BusEventi.iscriviti(NotificaTestoFrase.class, evento -> aggiungiMessaggio(new MessaggioMD(evento.getMessaggio(), false)));
		BusEventi.iscriviti(NotificaTestoParagrafo.class, evento -> aggiungiMessaggio(new MessaggioMD(evento.getMessaggio(), true)));
	}

	private static void aggiungiMessaggio(MessaggioMD messaggioMD) {
		getNotizieMD().aggiungiMessaggio(messaggioMD);
	}

	/**
	 * Una notizia nuova per la schermata della mappa (la produce la locanda, vedi Locanda.generaNotizia): va in
	 * testa, e le più vecchie oltre il limite si scartano. La UI la legge da VistaPartita.getUltimeNotizie.
	 */
	public static void aggiungiNotizia(NotiziaMD notizia) {
		getNotizieMD().aggiungiNotizia(notizia);
	}

	/**
	 * Gli ultimi messaggi mostrati al giocatore, dal più recente al più vecchio:
	 * serve a ripopolare il pannello di testo dopo un ricaricamento.
	 */
	public static List<MessaggioMD> getUltimiMessaggi() {
		return getNotizieMD().getUltimiMessaggi();
	}

	/**
	 * Le ultime notizie destinate alla schermata della mappa, dalla più recente alla più vecchia.
	 */
	public static List<NotiziaMD> getUltimeNotizie() {
		return getNotizieMD().getUltimeNotizie();
	}
}
