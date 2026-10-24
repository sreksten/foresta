package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCaricamentoCompletato;
import com.threeamigos.foresta.eventi.interni.InternoFineLocazione;
import com.threeamigos.foresta.eventi.interni.InternoTrofeoAcquisito;
import com.threeamigos.foresta.interfacce.GestoreTrofei;
import com.threeamigos.foresta.modellodati.TrofeiMD;
import com.threeamigos.foresta.tipi.TipoTrofeo;

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
 * Se il gruppo muore la fine della locazione non arriva, e il sospeso si scarta
 * ({@link #scartaIlProgressoInSospeso}) quando la partita finisce, ne comincia una nuova o se ne carica
 * una. Non quando si prepara una locazione: le missioni completate prima (a inizio locazione, o
 * all'accampamento) hanno già contato per i trofei, e il loro progresso deve restare.
 */
public class RegistroTrofei {

	private static TrofeiMD trofeiMD = new TrofeiMD();
	private static GestoreTrofei gestoreTrofei;
	private static final Map<TipoTrofeo, Integer> progressiInSospeso = new EnumMap<>(TipoTrofeo.class);

	private RegistroTrofei() {
	}

	/**
	 * Imposta dove leggere e salvare i trofei, e li rilegge da lì.
	 */
	public static void impostaGestoreTrofei(GestoreTrofei gestore) {
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
		BusEventi.iscriviti(InternoCaricamentoCompletato.class, evento -> scartaIlProgressoInSospeso());
		BusEventi.iscriviti(InternoFineLocazione.class, evento -> fineLocazione());
		for (ClasseTrofeo classeTrofeo : ClasseTrofeo.values()) {
			classeTrofeo.getIstanza().registrati();
		}
	}

	/**
	 * Dimentica quanto si è guadagnato dall'ultima fine di una locazione: serve quando quella partita non prosegue
	 * (il gruppo è morto, se ne comincia o se ne carica un'altra), perché non conti per la prossima.
	 */
	public static void scartaIlProgressoInSospeso() {
		progressiInSospeso.clear();
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
		// Si ripete finché si vince qualcosa: un trofeo come il Perdigiorno dipende dagli altri,
		// qualunque sia il loro ordine
		boolean vintoQualcosa;
		do {
			vintoQualcosa = false;
			for (ClasseTrofeo classeTrofeo : ClasseTrofeo.values()) {
				Trofeo trofeo = classeTrofeo.getIstanza();
				if (!trofeiMD.isVinto(trofeo.getTipo()) && trofeo.isMeritato()) {
					trofeiMD.aggiungiVinto(trofeo.getTipo());
					vintoQualcosa = true;
					modificato = true;
					BusEventi.pubblica(new InternoTrofeoAcquisito(trofeo.getTipo()));
				}
			}
		} while (vintoQualcosa);
		// Senza un gestore impostato (una partita di prova) i trofei restano in memoria
		if (modificato && gestoreTrofei != null) {
			gestoreTrofei.salva(trofeiMD);
		}
	}
}
