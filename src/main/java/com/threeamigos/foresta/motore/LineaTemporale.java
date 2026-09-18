package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

public class LineaTemporale {
	
	private LineaTemporale() {
	}

	private static LineaTemporaleMD getLineaTemporaleMD() {
		return ModelloDati.getIstanza().getLineaTemporaleMD();
	}

	private static String evento;

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
			return ora;
		}
	}

	public static void mattinoSeguente() {
		if (getOra() >= LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO) {
			getLineaTemporaleMD().setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
			getLineaTemporaleMD().setGiorno(getLineaTemporaleMD().getGiorno() + 1);
		} else {
			getLineaTemporaleMD().setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
		}
	}

	public static void aggiungiOre(int quantita) {
		int ora = getOra() + quantita;
		if (ora >= 24) {
			getLineaTemporaleMD().setOra(ora % 24);
			getLineaTemporaleMD().setGiorno(getLineaTemporaleMD().getGiorno() + 1);
		} else {
			getLineaTemporaleMD().setOra(ora);
		}
	}

	private static final String COLONNA = " vede levarsi una colonna di fumo ";

	//TODO le città potrebbero essere distrutte a caso
	public static void eventi(GruppoGiocatore gruppo) {
		int giorno = getGiorno();
		if (giorno > 40) {
			evento = "Sventura! " + gruppo.getPersonaggi().get(0).getNomeProprio() + " ha invano tentato di fermare il Drago, che col tempo ha abbattuto l'ultimo baluardo della resistenza... tutto e' perduto!";
			setGiocoFinito(true);
		} else {
			String nome = gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
			if (giorno >= 35 && !isCittaDistrutta(ClassiLocazione.CITTA_MALGAARD)) {
				CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_MALGAARD);
				evento = nome + COLONNA + Misc.getDirezione(gruppo, coordinate);
				setCittaDistrutta(ClassiLocazione.CITTA_MALGAARD);
			} else if (giorno >= 30 && !isCittaDistrutta(ClassiLocazione.CITTA_FLEENA)) {
				CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_FLEENA);
				evento = nome + COLONNA + Misc.getDirezione(gruppo, coordinate);
				setCittaDistrutta(ClassiLocazione.CITTA_FLEENA);
			} else if (giorno >= 25 && !isCittaDistrutta(ClassiLocazione.CITTA_NYENA)) {
				CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA);
				evento = nome + COLONNA + Misc.getDirezione(gruppo, coordinate);
				setCittaDistrutta(ClassiLocazione.CITTA_NYENA);
			} else if (giorno >= 20 && !isCittaDistrutta(ClassiLocazione.CITTA_RUUNA)) {
				CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_RUUNA);
				evento = nome + COLONNA + Misc.getDirezione(gruppo, coordinate);
				setCittaDistrutta(ClassiLocazione.CITTA_RUUNA);
			}
		}
	}

	public static String getEvento() {
		String e = evento;
		evento = null;
		return e;
	}

	public static boolean isGiocoFinito() {
		return getLineaTemporaleMD().isGiocoFinito();
	}

	public static void setGiocoFinito(boolean finito) {
		getLineaTemporaleMD().setGiocoFinito(finito);
	}

	public static boolean isCittaDistrutta(ClassiLocazione citta) {
		if (citta.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		return getLineaTemporaleMD().isCittaDistrutta(citta);
	}

	public static void setCittaDistrutta(ClassiLocazione citta) {
		if (citta.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		getLineaTemporaleMD().addCittaDistrutta(citta);
	}

	public static void setDragoSconfitto(boolean dragoSconfitto) {
		getLineaTemporaleMD().setGiocoFinito(dragoSconfitto);
	}
}
