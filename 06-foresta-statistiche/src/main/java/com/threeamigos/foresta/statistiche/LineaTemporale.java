package com.threeamigos.foresta.statistiche;

import com.threeamigos.foresta.mappa.Direzioni;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoLocazione;

import java.util.function.Supplier;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

/**
 * Il tempo della partita: giorno e ora, le città distrutte e l'ultimo evento da mostrare. I dati stanno nel {@code LineaTemporaleMD}.
 * <p>
 * Il tempo ha una scadenza: dal ventesimo giorno il drago distrugge una città ogni cinque giorni, e al quarantunesimo non resta più
 * nulla da difendere e il gioco è perduto.
 */
public class LineaTemporale {

	// Le frasi stanno in linea_temporale_<lingua>.properties
	private static final String FILE_DEI_TESTI = "linea_temporale";
	
	private LineaTemporale() {
	}

	// Da dove prendere i dati della partita in corso: la linea temporale non sa chi li custodisce
	private static volatile Supplier<LineaTemporaleMD> fornitoreDiDati;

	/**
	 * Dice da dove prendere i dati della partita in corso (lo fa chi li custodisce: ogni volta si prendono da quelli in corso).
	 */
	public static void setFornitoreDiDati(Supplier<LineaTemporaleMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static LineaTemporaleMD getLineaTemporaleMD() {
		Supplier<LineaTemporaleMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto dove sono i dati di LineaTemporale (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	public static int getOra() {
		return getLineaTemporaleMD().getOra();
	}

	public static int getGiorno() {
		return getLineaTemporaleMD().getGiorno();
	}

	/**
	 * Riporta il tempo all'inizio della partita.
	 */
	public static void reimposta() {
		getLineaTemporaleMD().reimposta();
	}

	public static String getDescrizioneOraDelGiorno() {
		int giorno = getGiorno();
		// L'ordinale con l'articolo elisio ("dell'ottavo", "dell'undicesimo") ha una frase sua
		boolean elisione = Lingue.corrente().ordinale(giorno, Genere.MASCHILE, true).startsWith("l'");
		return Testi.formatta(FILE_DEI_TESTI, elisione ? "FRASE_ELISA" : "FRASE", Testi.get(FILE_DEI_TESTI, "ORA." + getOra()).orElse(""),
				Lingue.corrente().ordinale(giorno, Genere.MASCHILE, false));
	}

	/**
	 * Quante ore mancano al mattino (vedi {@link LineaTemporaleMD#PRIMA_ORA_DEL_MATTINO}), che è quello seguente se è già passata quell'ora oggi.
	 */
	public static int oreFinoAlMattino() {
		int ora = getOra();
		if (ora >= LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO) {
			return 24 + LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO - ora; // 24 ore - ora corrente + l'ora del mattino
		} else {
			return LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO - ora; // dopo mezzanotte: fino al mattino dello stesso giorno
		}
	}

	/**
	 * Porta l'orologio alle otto del mattino: dello stesso giorno se è notte fonda, del giorno dopo altrimenti.
	 */
	public static void mattinoSeguente() {
		LineaTemporaleMD md = getLineaTemporaleMD();
		if (getOra() >= LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO) {
			md.setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
			md.setGiorno(getLineaTemporaleMD().getGiorno() + 1);
		} else {
			md.setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
		}
	}

	/**
	 * Fa passare quelle ore, anche più di un giorno in una volta sola.
	 */
	public static void aggiungiOre(int quantita) {
		LineaTemporaleMD md = getLineaTemporaleMD();
		int ora = getOra() + quantita;
		// Anche più di un giorno in una volta sola
		md.setOra(ora % 24);
		md.setGiorno(md.getGiorno() + ora / 24);
	}


	//TODO le città potrebbero essere distrutte a caso
	/**
	 * Gli eventi che dipendono dal giorno: dal giorno 20 il drago distrugge una città ogni cinque giorni (Ruuna, Nyena, Fleena, Malgaard) e
	 * dopo il giorno 40 il gioco è perduto. La distruzione di una città viene annunciata con una colonna di fumo nella sua direzione.
	 */
	public static void eventi(GruppoGiocatore gruppo) {
		eventi(gruppo, false);
	}

	/**
	 * Come {@link #eventi}, per l'alba dopo una notte passata dal gruppo dov'è: se la città che cade è proprio quella in cui dormiva, scampa
	 * per un pelo (invece di arrivarci a cosa fatta, come chi ci cammina).
	 */
	public static void eventiDiNotte(GruppoGiocatore gruppo) {
		eventi(gruppo, true);
	}

	private static void eventi(GruppoGiocatore gruppo, boolean dormiva) {
		int giorno = getGiorno();
		String nome = gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		if (giorno > 40) {
			setEvento(Testi.formatta(FILE_DEI_TESTI, "EVENTO_FINE", nome));
			setGiocoFinito(true);
		} else {
			if (giorno >= 35 && !Foresta.isCittaDistrutta(TipoLocazione.CITTA_MALGAARD)) {
				distruggiCitta(TipoLocazione.CITTA_MALGAARD, gruppo, nome, dormiva);
			} else if (giorno >= 30 && !Foresta.isCittaDistrutta(TipoLocazione.CITTA_FLEENA)) {
				distruggiCitta(TipoLocazione.CITTA_FLEENA, gruppo, nome, dormiva);
			} else if (giorno >= 25 && !Foresta.isCittaDistrutta(TipoLocazione.CITTA_NYENA)) {
				distruggiCitta(TipoLocazione.CITTA_NYENA, gruppo, nome, dormiva);
			} else if (giorno >= 20 && !Foresta.isCittaDistrutta(TipoLocazione.CITTA_RUUNA)) {
				distruggiCitta(TipoLocazione.CITTA_RUUNA, gruppo, nome, dormiva);
			}
		}
	}

	/**
	 * Il drago distrugge una citta': al suo posto restano delle rovine. La direzione del fumo si calcola prima, finche'
	 * la citta' e' ancora sulla mappa. Le missioni che andavano concluse li' falliscono al turno successivo (vedi
	 * Foresta.isCittaDistrutta). Se il gruppo è proprio in quella città non vede una colonna di fumo da lontano: se ci dormiva, scampa per un pelo,
	 * e se ci è appena arrivato trova solo le rovine.
	 */
	private static void distruggiCitta(TipoLocazione citta, GruppoGiocatore gruppo, String nome, boolean dormiva) {
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(citta);
		if (coordinate != null) {
			String nomeCitta = citta.getNomeDellaCitta();
			if (!coordinate.equals(Foresta.getPosizioneDelGruppo())) {
				setEvento(Testi.formatta(FILE_DEI_TESTI, "EVENTO_FUMO", nome, Direzioni.getDirezione(Foresta.getPosizioneDelGruppo(), coordinate)));
			} else if (dormiva) {
				setEvento(Testi.formatta(FILE_DEI_TESTI, "EVENTO_SCAMPATO", nomeCitta, gruppo.chiMaiuscolo()));
			} else {
				setEvento(Testi.formatta(FILE_DEI_TESTI, "EVENTO_ROVINE", nomeCitta, gruppo.chi()));
			}
		}
		Foresta.setCittaDistrutta(citta);
		Foresta.distruggiLocazioneUnica(citta, TipoLocazione.ROVINE);
	}

	/**
	 * L'ultimo evento non ancora mostrato, che viene consumato. Sta nel modello dati così un
	 * salvataggio fatto prima di mostrarlo non lo perde.
	 */
	public static String getEvento() {
		String e = getLineaTemporaleMD().getEvento();
		getLineaTemporaleMD().setEvento(null);
		return e;
	}

	private static void setEvento(String evento) {
		getLineaTemporaleMD().setEvento(evento);
	}

	public static boolean isGiocoFinito() {
		return getLineaTemporaleMD().isGiocoFinito();
	}

	public static void setGiocoFinito(boolean finito) {
		getLineaTemporaleMD().setGiocoFinito(finito);
	}

	public static void setDragoSconfitto(boolean dragoSconfitto) {
		getLineaTemporaleMD().setGiocoFinito(dragoSconfitto);
	}
}
