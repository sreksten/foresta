package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoPreparazioneLocazione;
import com.threeamigos.foresta.eventi.interni.InternoTrofeoAcquisito;
import com.threeamigos.foresta.motore.modellodati.TrofeiMD;
import com.threeamigos.foresta.motore.tipi.TipoTrofeo;
import com.threeamigos.foresta.tools.InterfacciaGestoreTrofei;
import com.threeamigos.foresta.trofei.ClasseTrofeo;
import com.threeamigos.foresta.trofei.Trofeo;

import java.util.EnumMap;
import java.util.Map;

/**
 * Facciata su {@link TrofeiMD}: ricorda i trofei vinti e il progresso verso gli altri, e
 * controlla i trigger di quelli ancora da vincere. I trofei passano da una partita
 * all'altra: il modello si legge una volta all'avvio (vedi {@link #impostaGestoreTrofei})
 * e non viene mai reimpostato.
 * <p>
 * Quanto si guadagna in una locazione resta in sospeso e conta solo se il gruppo ne esce
 * vivo: alla fine della locazione diventa progresso, si controllano i trofei e si salva.
 * Se il gruppo muore la fine della locazione non arriva, e il sospeso si scarta quando
 * se ne prepara una nuova.
 */
public class RegistroTrofei {

	private static TrofeiMD trofeiMD = new TrofeiMD();
	private static InterfacciaGestoreTrofei gestoreTrofei;
	private static final Map<TipoTrofeo, Integer> progressiInSospeso = new EnumMap<>(TipoTrofeo.class);

	private RegistroTrofei() {
	}

	/**
	 * Imposta dove leggere e salvare i trofei, e li rilegge da lì.
	 */
	public static void impostaGestoreTrofei(InterfacciaGestoreTrofei gestore) {
		gestoreTrofei = gestore;
		trofeiMD = new TrofeiMD();
		progressiInSospeso.clear();
		gestoreTrofei.carica(trofeiMD);
	}

	/**
	 * Si iscrive all'inizio e alla fine delle locazioni, e iscrive ogni trofeo agli eventi
	 * che lo fanno avanzare.
	 */
	public static void registrati() {
		BusEventi.iscriviti(InternoPreparazioneLocazione.class, evento -> progressiInSospeso.clear());
		BusEventi.iscriviti(InternoFineLocazione.class, evento -> fineLocazione());
		for (ClasseTrofeo classeTrofeo : ClasseTrofeo.values()) {
			classeTrofeo.getIstanza().registrati();
		}
	}

	public static boolean isVinto(TipoTrofeo trofeo) {
		return trofeiMD.isVinto(trofeo);
	}

	/**
	 * Il progresso acquisito, senza quanto è ancora in sospeso nella locazione corrente.
	 */
	public static int getProgresso(TipoTrofeo trofeo) {
		return trofeiMD.getProgresso(trofeo);
	}

	/**
	 * Fa avanzare un trofeo che si conquista partita dopo partita: l'avanzamento resta in
	 * sospeso fino alla fine della locazione. Se il trofeo è già stato vinto, o se non c'è
	 * niente da aggiungere, non serve contare.
	 */
	public static void incrementaProgresso(TipoTrofeo trofeo, int quantita) {
		if (trofeiMD.isVinto(trofeo) || quantita <= 0) {
			return;
		}
		progressiInSospeso.merge(trofeo, quantita, Integer::sum);
	}

	/**
	 * Il gruppo esce vivo dalla locazione: il sospeso diventa progresso, i trofei meritati
	 * vengono segnati e annunciati con {@link InternoTrofeoAcquisito}, e se è cambiato
	 * qualcosa si salva.
	 */
	private static void fineLocazione() {
		boolean modificato = !progressiInSospeso.isEmpty();
		progressiInSospeso.forEach(trofeiMD::incrementaProgresso);
		progressiInSospeso.clear();
		for (ClasseTrofeo classeTrofeo : ClasseTrofeo.values()) {
			Trofeo trofeo = classeTrofeo.getIstanza();
			if (!trofeiMD.isVinto(trofeo.getTipo()) && trofeo.isMeritato()) {
				trofeiMD.aggiungiVinto(trofeo.getTipo());
				modificato = true;
				BusEventi.pubblica(new InternoTrofeoAcquisito(trofeo.getTipo()));
			}
		}
		if (modificato) {
			gestoreTrofei.salva(trofeiMD);
		}
	}
}
