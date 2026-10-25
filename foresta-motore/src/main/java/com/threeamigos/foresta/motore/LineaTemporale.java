package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Il tempo della partita: giorno e ora, le città distrutte e l'ultimo evento da mostrare. I dati stanno nel {@code LineaTemporaleMD}.
 * <p>
 * Il tempo ha una scadenza: dal ventesimo giorno il drago distrugge una città ogni cinque giorni, e al quarantunesimo non resta più
 * nulla da difendere e il gioco è perduto.
 */
public class LineaTemporale {
	
	private LineaTemporale() {
	}

	private static LineaTemporaleMD getLineaTemporaleMD() {
		return ModelloDati.getIstanza().getLineaTemporaleMD();
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

	private static final String[] ore = {
			"E' la mezzanotte",
			"E' l'una del mattino",
			"Sono le due del mattino",
			"Soo le tre del mattino",
			"E' il canto del gallo",
			"Sorge l'aurora",
			"E' l'alba",
			"Sono le sette del mattino",
			"Sono le otto del mattino",
			"Sono le nove del mattino",
			"E' la mezza mattinata",
			"E' la tarda mattina",
			"E' mezzogiorno",
			"E' il tocco",
			"Sono le due",
			"E' il primo pomeriggio",
			"E' il pomeriggio",
			"E' il tardo pomeriggio",
			"E' il tramonto",
			"E' l'imbrunire",
			"E' la sera",
			"E' la tarda sera",
			"E' la notte",
			"Sono le undici di notte"
	};

	public static String getDescrizioneOraDelGiorno() {
		StringBuilder sb = new StringBuilder(ore[getOra()]).append(" del");
		int giorno = getGiorno();
		if (giorno == 8 || giorno == 11) {
			sb.append("l'");
		} else {
			sb.append(' ');
		}
		sb.append(Numeri.getOrdinaleM(giorno, false)).append(" giorno.");
		return sb.toString();
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

	private static final String COLONNA = " vede levarsi una colonna di fumo ";

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
			setEvento("Sventura! " + nome + " ha invano tentato di fermare il Drago, che col tempo ha abbattuto l'ultimo baluardo della resistenza... tutto e' perduto!");
			setGiocoFinito(true);
		} else {
			if (giorno >= 35 && !isCittaDistrutta(TipoLocazione.CITTA_MALGAARD)) {
				distruggiCitta(TipoLocazione.CITTA_MALGAARD, gruppo, nome, dormiva);
			} else if (giorno >= 30 && !isCittaDistrutta(TipoLocazione.CITTA_FLEENA)) {
				distruggiCitta(TipoLocazione.CITTA_FLEENA, gruppo, nome, dormiva);
			} else if (giorno >= 25 && !isCittaDistrutta(TipoLocazione.CITTA_NYENA)) {
				distruggiCitta(TipoLocazione.CITTA_NYENA, gruppo, nome, dormiva);
			} else if (giorno >= 20 && !isCittaDistrutta(TipoLocazione.CITTA_RUUNA)) {
				distruggiCitta(TipoLocazione.CITTA_RUUNA, gruppo, nome, dormiva);
			}
		}
	}

	/**
	 * Il drago distrugge una citta': al suo posto restano delle rovine. La direzione del fumo si calcola prima, finche'
	 * la citta' e' ancora sulla mappa. Le missioni che andavano concluse li' falliscono al turno successivo (vedi
	 * isCittaDistrutta). Se il gruppo è proprio in quella città non vede una colonna di fumo da lontano: se ci dormiva, scampa per un pelo,
	 * e se ci è appena arrivato trova solo le rovine.
	 */
	private static void distruggiCitta(TipoLocazione citta, GruppoGiocatore gruppo, String nome, boolean dormiva) {
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(citta);
		if (coordinate != null) {
			String nomeCitta = citta.getNomeDellaCitta();
			if (!coordinate.equals(gruppo.getCoordinate())) {
				setEvento(nome + COLONNA + Misc.getDirezione(gruppo, coordinate));
			} else if (dormiva) {
				setEvento("Un boato scuote " + nomeCitta + " prima dell'alba: il Drago! " + gruppo.chiMaiuscolo()
						+ " riesce a fuggire per un pelo tra le fiamme, e di " + nomeCitta + " non restano che rovine fumanti.");
			} else {
				setEvento("Dove sorgeva " + nomeCitta + " " + gruppo.chi() + " trova solo le rovine: il Drago è passato di qui.");
			}
		}
		setCittaDistrutta(citta);
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

	public static boolean isCittaDistrutta(TipoLocazione citta) {
		if (citta.getCategoriaLocazione() != CategoriaLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		return getLineaTemporaleMD().isCittaDistrutta(citta);
	}

	public static void setCittaDistrutta(TipoLocazione citta) {
		if (citta.getCategoriaLocazione() != CategoriaLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		getLineaTemporaleMD().addCittaDistrutta(citta);
	}

	public static void setDragoSconfitto(boolean dragoSconfitto) {
		getLineaTemporaleMD().setGiocoFinito(dragoSconfitto);
	}
}
