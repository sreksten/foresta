package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.strumenti.Misc;

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
		sb.append(Misc.getOrdinaleM(giorno, false)).append(" giorno.");
		return sb.toString();
	}

	public static int oreFinoAlMattino() {
		int ora = getOra();
		if (ora >= 8) {
			return 32 - ora; // 24 ore - ora corrente + 8 ore
		} else {
			return 8 - ora; // dopo mezzanotte: fino alle 8 dello stesso giorno
		}
	}

	public static void mattinoSeguente() {
		LineaTemporaleMD md = getLineaTemporaleMD();
		if (getOra() >= LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO) {
			md.setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
			md.setGiorno(getLineaTemporaleMD().getGiorno() + 1);
		} else {
			md.setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
		}
	}

	public static void aggiungiOre(int quantita) {
		LineaTemporaleMD md = getLineaTemporaleMD();
		int ora = getOra() + quantita;
		// Anche più di un giorno in una volta sola
		md.setOra(ora % 24);
		md.setGiorno(md.getGiorno() + ora / 24);
	}

	private static final String COLONNA = " vede levarsi una colonna di fumo ";

	//TODO le città potrebbero essere distrutte a caso
	public static void eventi(GruppoGiocatore gruppo) {
		int giorno = getGiorno();
		String nome = gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		if (giorno > 40) {
			setEvento("Sventura! " + nome + " ha invano tentato di fermare il Drago, che col tempo ha abbattuto l'ultimo baluardo della resistenza... tutto e' perduto!");
			setGiocoFinito(true);
		} else {
			if (giorno >= 35 && !isCittaDistrutta(TipoLocazione.CITTA_MALGAARD)) {
				distruggiCitta(TipoLocazione.CITTA_MALGAARD, gruppo, nome);
			} else if (giorno >= 30 && !isCittaDistrutta(TipoLocazione.CITTA_FLEENA)) {
				distruggiCitta(TipoLocazione.CITTA_FLEENA, gruppo, nome);
			} else if (giorno >= 25 && !isCittaDistrutta(TipoLocazione.CITTA_NYENA)) {
				distruggiCitta(TipoLocazione.CITTA_NYENA, gruppo, nome);
			} else if (giorno >= 20 && !isCittaDistrutta(TipoLocazione.CITTA_RUUNA)) {
				distruggiCitta(TipoLocazione.CITTA_RUUNA, gruppo, nome);
			}
		}
	}

	/**
	 * Il drago distrugge una citta': al suo posto restano delle rovine. La direzione del fumo si calcola prima, finche'
	 * la citta' e' ancora sulla mappa. Le missioni che andavano concluse li' falliscono al turno successivo (vedi
	 * isCittaDistrutta).
	 */
	private static void distruggiCitta(TipoLocazione citta, GruppoGiocatore gruppo, String nome) {
		CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(citta);
		if (coordinate != null) {
			setEvento(nome + COLONNA + Misc.getDirezione(gruppo, coordinate));
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
		if (citta.getCategoria() != CategoriaLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		return getLineaTemporaleMD().isCittaDistrutta(citta);
	}

	public static void setCittaDistrutta(TipoLocazione citta) {
		if (citta.getCategoria() != CategoriaLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		getLineaTemporaleMD().addCittaDistrutta(citta);
	}

	public static void setDragoSconfitto(boolean dragoSconfitto) {
		getLineaTemporaleMD().setGiocoFinito(dragoSconfitto);
	}
}
