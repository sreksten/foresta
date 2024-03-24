package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.UI;

public class Artefatto implements Oggetto {

	private ArtefattoMD md = new ArtefattoMD();

	public Artefatto(ArtefattoMD artefattoMD) {
		this.md = artefattoMD;
	}

	public Artefatto(String nome, String descrizione, String utilizzo,
			int costoAcquisto,
			int forza, int magia, int valore, int coraggio,
			int carisma, int stanchezza, int bersagli, int protezione) {
		md = new ArtefattoMD();
		md.setNome(nome);
		md.setDescrizione(descrizione);
		md.setUtilizzo(utilizzo);
		md.setCostoAcquisto(costoAcquisto);
		md.setForza(forza);
		md.setMagia(magia);
		md.setValore(valore);
		md.setCoraggio(coraggio);
		md.setCarisma(carisma);
		md.setStanchezza(stanchezza);
		md.setBersagli(bersagli);
		md.setProtezione(protezione);
	}

	public final String getNome() {
		return md.getNome();
	}

	public final String getDescrizione() {
		return md.getDescrizione();
	}

	public final String getUtilizzo() {
		return md.getUtilizzo();
	}

	public final int getCostoAcquisto() {
		return md.getCostoAcquisto();
	}

	public final int getForza() {
		return md.getForza();
	}

	public final int getMagia() {
		return md.getMagia();
	}

	public final int getValore() {
		return md.getValore();
	}

	public final int getCoraggio() {
		return md.getCoraggio();
	}

	public final int getCarisma() {
		return md.getCarisma();
	}

	public final int getStanchezza() {
		return md.getStanchezza();
	}

	public final int getBersagli() {
		return md.getBersagli();
	}

	public final int getProtezione() {
		return md.getProtezione();
	}

	/**
	 * Interfaccia Oggetto
	 */
	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		if (azione == null) {
			if (gruppo.getNumeroPersonaggi() > 1)
				UI.notifica(new StringBuilder("Chi raccoglie ").append(getNome()).append('?').toString());
			return false;
		}
		Logger.log("Artefatto::prendi() - azione " + azione);
		Personaggio p = gruppo.getPersonaggio(azione);
		p.addArtefatto(this);
		UI.notifica(new StringBuilder(p.getNome()).append(" raccoglie ").append(md.getNome()).append('.').toString());
		return true;
	}

	public int getQuantita() {
		return 1;
	}

	public String getNomePlurale() {
		return "artefatti";
	}

	public String getNomeSingolare() {
		return "artefatto";
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.L_APOSTROFO;
	}

	public String getADP() {
		return Misc.GLI;
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.ARTEFATTO;
	}
	
	public ArtefattoMD getModelloDati() {
		return md;
	}
	
	public static final Costruttore getCostruttore() {
		return new Costruttore();
	}
	
	public static class Costruttore {
		private ArtefattoMD artefattoMD = new ArtefattoMD();
		
		public Costruttore setNome(String nome) {
			artefattoMD.setNome(nome);
			return this;
		}
		
		public Costruttore setDescrizione(String descrizione) {
			artefattoMD.setDescrizione(descrizione);
			return this;
		}

		public Costruttore setUtilizzo(String utilizzo) {
			artefattoMD.setUtilizzo(utilizzo);
			return this;
		}
		
		public Costruttore setCostoAcquisto(int costoAcquisto) {
			artefattoMD.setCostoAcquisto(costoAcquisto);
			return this;
		}
		
		public Costruttore setForza(int forza) {
			artefattoMD.setForza(forza);
			return this;
		}
		
		public Costruttore setMagia(int magia) {
			artefattoMD.setMagia(magia);
			return this;
		}
		
		public Costruttore setValore(int valore) {
			artefattoMD.setValore(valore);
			return this;
		}
		
		public Costruttore setCoraggio(int coraggio) {
			artefattoMD.setCoraggio(coraggio);
			return this;
		}
		
		public Costruttore setCarisma(int carisma) {
			artefattoMD.setCarisma(carisma);
			return this;
		}
		
		public Costruttore setStanchezza(int stanchezza) {
			artefattoMD.setStanchezza(stanchezza);
			return this;
		}
		
		public Costruttore setBersagli(int bersagli) {
			artefattoMD.setBersagli(bersagli);
			return this;
		}
		
		public Costruttore setProtezione(int protezione) {
			artefattoMD.setProtezione(protezione);
			return this;
		}
		
		public Artefatto getArtefatto() {
			return new Artefatto(artefattoMD);
		}

	}
}
