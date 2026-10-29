package com.threeamigos.foresta.economia;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaMonete;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneDisponibilitaPreziosi;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaCassa;
import com.threeamigos.foresta.modellodati.CassaMD;
import com.threeamigos.foresta.tipi.CostantiEconomia;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import java.util.function.Supplier;

/**
 * La cassa del gruppo del giocatore: le monete e le pietre preziose. Il gruppo non sa di averla: la governa questa classe, con i dati
 * che le dà chi li custodisce (vedi {@link #setFornitoreDiDati}).
 *
 * @author Stefano Reksten
 */
public final class Cassa {

	/**
	 * La cassa vista dalla UI.
	 */
	public static final VistaCassa VISTA = new VistaCassa() {
		@Override
		public int getMonete() {
			return Cassa.getMonete();
		}

		@Override
		public int getPreziosi() {
			return Cassa.getPreziosi();
		}
	};

	// Da dove prendere i dati della partita in corso: la cassa non sa chi li custodisce
	private static volatile Supplier<CassaMD> fornitoreDiDati;

	private Cassa() {
	}

	/**
	 * Dice da dove prendere i dati della partita in corso (lo fa chi li custodisce: ogni volta si prendono da quelli in corso).
	 */
	public static void setFornitoreDiDati(Supplier<CassaMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static CassaMD dati() {
		Supplier<CassaMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto dove sono i dati della Cassa (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	/**
	 * La cassa di una partita nuova: le monete e i preziosi con cui si parte.
	 */
	public static void reimposta() {
		CassaMD md = dati();
		md.reimposta();
		md.setMonete(CostantiEconomia.MONETE_INIZIALI);
		md.setPreziosi(CostantiEconomia.PREZIOSI_INIZIALI);
	}

	public static int getMonete() {
		return dati().getMonete();
	}

	/**
	 * Aggiunge monete e lo annuncia alla UI.
	 */
	public static void addMonete(int quantita) {
		CassaMD md = dati();
		int valorePrecedente = md.getMonete();
		int valoreAttuale = valorePrecedente + quantita;
		md.setMonete(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaMonete(valorePrecedente, valoreAttuale));
	}

	/**
	 * Toglie monete.
	 */
	public static void subMonete(int quantita) {
		addMonete(-quantita);
	}

	public static int getPreziosi() {
		return dati().getPreziosi();
	}

	/**
	 * Aggiunge pietre preziose e lo annuncia alla UI.
	 */
	public static void addPreziosi(int quantita) {
		CassaMD md = dati();
		int valorePrecedente = md.getPreziosi();
		int valoreAttuale = valorePrecedente + quantita;
		md.setPreziosi(valoreAttuale);
		BusEventi.pubblica(new NotificaVariazioneDisponibilitaPreziosi(valorePrecedente, valoreAttuale));
	}

	/**
	 * Toglie pietre preziose.
	 */
	public static void subPreziosi(int quantita) {
		addPreziosi(-quantita);
	}

	/**
	 * Vende tutti i preziosi per quelle monete, già calcolate da chi chiama (a che prezzo si vendano lo sa il mondo).
	 */
	public static void vendiPreziosiPer(int monete) {
		addMonete(monete);
		subPreziosi(getPreziosi());
	}

	/**
	 * Compra l'artefatto a quel prezzo, già calcolato da chi chiama: lo mette nell'inventario del gruppo e toglie le monete, se ci sono.
	 *
	 * @return true se le monete bastavano
	 */
	public static boolean compra(Artefatto artefatto, int costo) {
		if (getMonete() < costo) {
			return false;
		}
		GruppoGiocatore.getIstanza().addArtefatto(artefatto);
		subMonete(costo);
		return true;
	}

	/**
	 * Il gruppo è fuggito: perde il {@value CostantiPersonaggi#FUGA_PERDITA_CONSUMABILI_PERCENTUALE}% delle monete e dei preziosi
	 * (arrotondato per difetto), come delle altre scorte (vedi GruppoGiocatore.fugge).
	 */
	public static void perdeNellaFuga() {
		subMonete(getMonete() * CostantiPersonaggi.FUGA_PERDITA_CONSUMABILI_PERCENTUALE / 100);
		subPreziosi(getPreziosi() * CostantiPersonaggi.FUGA_PERDITA_CONSUMABILI_PERCENTUALE / 100);
	}
}
