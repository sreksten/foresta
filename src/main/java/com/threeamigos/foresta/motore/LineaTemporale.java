package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.LineaTemporaleMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.tools.Misc;

public class LineaTemporale {
	
	private LineaTemporale() {
	}

	private static LineaTemporaleMD lineaTemporaleMD = ModelloDati.getIstanza().getLineaTemporaleMD();

	private static String evento;

	public static final int getOra() {
		return lineaTemporaleMD.getOra();
	}

	public static final int getGiorno() {
		return lineaTemporaleMD.getGiorno();
	}

	public static final void reimposta() {
		lineaTemporaleMD.reimposta();
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

	public static final String getDescrizioneOraDelGiorno() {
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

	public static final int oreFinoAlMattino() {
		int ora = getOra();
		if (ora >= 8) {
			return 32 - ora; // 24 ore - ora corrente + 8 ore
		} else {
			return ora;
		}
	}

	public static final void mattinoSeguente() {
		if (getOra() >= LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO) {
			lineaTemporaleMD.setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
			lineaTemporaleMD.setGiorno(lineaTemporaleMD.getGiorno() + 1);
		} else {
			lineaTemporaleMD.setOra(LineaTemporaleMD.PRIMA_ORA_DEL_MATTINO);
		}
	}

	public static final void aggiungiOre(int quantita) {
		int ora = getOra() + quantita;
		if (ora >= 24) {
			lineaTemporaleMD.setOra(ora % 24);
			lineaTemporaleMD.setGiorno(lineaTemporaleMD.getGiorno() + 1);
		} else {
			lineaTemporaleMD.setOra(ora);
		}
	}

	private static final String COLONNA = " vede levarsi una colonna di fumo ";

	//TODO le città potrebbero essere distrutte a caso
	public static final void eventi(GruppoGiocatore gruppo) {
		int giorno = getGiorno();
		if (giorno > 40) {
			evento = new StringBuilder("Sventura! ").append(gruppo.getCapo().getNome()).append(" ha invano tentato di fermare il Drago, che col tempo ha abbattuto l'ultimo baluardo della resistenza... tutto e' perduto!").toString();
			setGiocoFinito(true);
		} else if (giorno >= 35 && !isCittaDistrutta(ClassiLocazione.CITTA_MALGAARD)) {
			CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_MALGAARD);
			evento = new StringBuilder(gruppo.getCapo().getNome()).append(COLONNA).append(Misc.getDirezione(gruppo, coordinate)).toString();
			setCittaDistrutta(ClassiLocazione.CITTA_MALGAARD);
		} else if (giorno >= 30 && !isCittaDistrutta(ClassiLocazione.CITTA_FLEENA)) {
			CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_FLEENA);
			evento = new StringBuilder(gruppo.getCapo().getNome()).append(COLONNA).append(Misc.getDirezione(gruppo, coordinate)).toString();
			setCittaDistrutta(ClassiLocazione.CITTA_FLEENA);
		} else if (giorno >= 25 && !isCittaDistrutta(ClassiLocazione.CITTA_NYENA)) {
			CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_NYENA);
			evento = new StringBuilder(gruppo.getCapo().getNome()).append(COLONNA).append(Misc.getDirezione(gruppo, coordinate)).toString();
			setCittaDistrutta(ClassiLocazione.CITTA_NYENA);
		} else if (giorno >= 20 && !isCittaDistrutta(ClassiLocazione.CITTA_RUUNA)) {
			CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_RUUNA);
			evento = new StringBuilder(gruppo.getCapo().getNome()).append(COLONNA).append(Misc.getDirezione(gruppo, coordinate)).toString();
			setCittaDistrutta(ClassiLocazione.CITTA_RUUNA);
		}
	}

	public static final String getEvento() {
		String e = evento;
		evento = null;
		return e;
	}

	public static final boolean isGiocoFinito() {
		return lineaTemporaleMD.isGiocoFinito();
	}

	public static final void setGiocoFinito(boolean finito) {
		lineaTemporaleMD.setGiocoFinito(finito);
	}

	public static final boolean isCittaDistrutta(ClassiLocazione citta) {
		if (citta.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		return lineaTemporaleMD.isCittaDistrutta(citta);
	}

	public static final void setCittaDistrutta(ClassiLocazione citta) {
		if (citta.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			throw new IllegalArgumentException();
		}
		lineaTemporaleMD.addCittaDistrutta(citta);
	}

	public static final void setDragoSconfitto(boolean dragoSconfitto) {
		lineaTemporaleMD.setGiocoFinito(dragoSconfitto);
	}
}
