package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoMessaggio;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.motore.modellodati.SupertipoArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;

import java.util.Collection;

public class Artefatto implements Oggetto, OggettoConCosto, OggettoConPeso {

	protected final ArtefattoMD md;

	public Artefatto(ArtefattoMD artefattoMD) {
		this.md = artefattoMD;
	}

	/**
	 * Ricostruisce la sottoclasse corretta (es. ArmaFisica) in base al supertipo,
	 * così da non perdere il tipo concreto quando un Artefatto viene rimaterializzato dal suo ArtefattoMD.
	 */
	public static Artefatto di(ArtefattoMD md) {
		if (md.getTipo().getSupertipo() == SupertipoArtefatto.ARMA) {
			return new ArmaFisica(md);
		}
		return new Artefatto(md);
	}

	public final TipoArtefatto getTipo() {
		return md.getTipo();
	}

	public final String getNome() {
		return md.getNome();
	}

	public final String getDescrizione() {
		return md.getDescrizione();
	}

	public final int getLivello() {
		return md.getLivello();
	}

	public final int getCostoAcquisto() {
		return md.getCostoAcquisto();
	}

	public double getPeso() {
		return md.getPeso();
	}

	/**
	 * Modificatori permanenti agli attributi dei personaggi
	 */
	public Collection<ModificatoreAttributo> getModificatori() {
		return md.getModificatori();
	}

	/**
	 * Incantamenti fatti sull'artefatto
	 */
	public Collection<Incantamento> getIncantamenti() {
		return md.getIncantamenti();
	}

	public boolean isFigliVisibili() {
		return md.isFigliVisibili();
	}

	public void mostraFigli() {
		md.setFigliVisibili(true);
	}

	public void nascondiFigli() {
		md.setFigliVisibili(false);
	}

	/**
	 * Interfaccia Oggetto
	 */
	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando comando) {
		if (comando == null) {
			if (gruppo.getNumeroPersonaggiVivi() > 1) {
				BusEventi.pubblica(new EventoMessaggio("Chi raccoglie " + getNome() + "?"));
				return false;
			} else {
				comando = Comando.PERSONAGGIO_1;
			}
		}
		Personaggio p = gruppo.getPersonaggio(comando);
		p.addArtefatto(this);
		BusEventi.pubblica(new EventoMessaggio(p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " raccoglie " + md.getNome() + '.'));
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

}
