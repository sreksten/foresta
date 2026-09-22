package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaNotizia;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.Notizia;
import com.threeamigos.foresta.motore.modellodati.NotizieMD;

import java.util.List;

/**
 * Facciata su {@link NotizieMD}: tiene sincronizzati gli ultimi messaggi mostrati
 * al giocatore (per poter ripopolare il pannello di testo dopo un ricaricamento)
 * e le ultime notizie destinate alla schermata della mappa, iscrivendosi al bus
 * eventi. Il modello dati resta un bean di soli dati; la logica di rotazione
 * (nuovo elemento in testa, scarto dei più vecchi oltre il limite) vive qui.
 *
 * @author Stefano Reksten
 */
public class Notizie {

	private Notizie() {
	}

	private static NotizieMD getNotizieMD() {
		return ModelloDati.getIstanza().getNotizieMD();
	}

	/**
	 * Si iscrive al bus eventi. Va chiamato una sola volta, prima che il gioco inizi
	 * a pubblicare notifiche (vedi Main).
	 */
	public static void registrati() {
		BusEventi.iscriviti(NotificaTestoFrase.class, evento -> aggiungiMessaggio(evento.getMessaggio()));
		BusEventi.iscriviti(NotificaTestoParagrafo.class, evento -> aggiungiMessaggio(evento.getMessaggio()));
		BusEventi.iscriviti(NotificaNotizia.class, evento -> aggiungiNotizia(evento.getNotizia()));
	}

	private static void aggiungiMessaggio(String messaggio) {
		List<String> ultimiMessaggi = getNotizieMD().getUltimiMessaggi();
		ultimiMessaggi.add(0, messaggio);
		while (ultimiMessaggi.size() > Costanti.MASSIMO_MESSAGGI_RICORDATI) {
			ultimiMessaggi.remove(ultimiMessaggi.size() - 1);
		}
	}

	private static void aggiungiNotizia(Notizia notizia) {
		List<Notizia> ultimeNotizie = getNotizieMD().getUltimeNotizie();
		ultimeNotizie.add(0, notizia);
		while (ultimeNotizie.size() > Costanti.MASSIMO_NOTIZIE_RICORDATE) {
			ultimeNotizie.remove(ultimeNotizie.size() - 1);
		}
	}

	/**
	 * Gli ultimi messaggi mostrati al giocatore, dal più recente al più vecchio:
	 * serve a ripopolare il pannello di testo dopo un ricaricamento.
	 */
	public static List<String> getUltimiMessaggi() {
		return getNotizieMD().getUltimiMessaggi();
	}

	/**
	 * Le ultime notizie destinate alla schermata della mappa, dalla più recente alla più vecchia.
	 */
	public static List<Notizia> getUltimeNotizie() {
		return getNotizieMD().getUltimeNotizie();
	}
}
