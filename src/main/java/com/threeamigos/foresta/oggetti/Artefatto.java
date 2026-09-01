package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.TipoAttributo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.ui.UI;

public class Artefatto implements Oggetto {

	private final ArtefattoMD md;

	public Artefatto(ArtefattoMD artefattoMD) {
		this.md = artefattoMD;
	}

	public final String getNome() {
		return md.getNome();
	}

	public final String getDescrizione() {
		return md.getDescrizione();
	}

	public final int getCostoAcquisto() {
		return md.getCostoAcquisto();
	}

	public final int getSalute() {
		return md.getSalute();
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
		UI.notifica(p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " raccoglie " + md.getNome() + '.');
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

	public int getModificatoreAttributo(TipoAttributo tipoAttributo) {
		return md.getModificatoreAttributo(tipoAttributo);
	}

	public int getLivello() {
		return md.getLivello();
	}

	public int getDanniBase() {
		return md.getDanniBase();
	}

	public int getPeso() {
		return md.getPeso();
	}

}
