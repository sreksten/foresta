package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;

public class Cofano extends OggettoBase implements Oggetto {

	public Cofano() {
		super();
	}
	
	public Cofano(int q) {
		quantita = q;
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.IL;
	}

	public String getADP() {
		return Misc.I;
	}

	public String getNomeSingolare() {
		return "cofano";
	}

	public String getNomePlurale() {
		return "cofani";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.COFANO;
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		ClassiLocazione classeLocazione = gruppo.getClasseLocazione();
		int min;
		int span;
		int tipo;
		if (classeLocazione.getTipoLocazione() == ClassiLocazione.TipoLocazione.CASTELLO) {
			min = 1;
			span = 7;
		} else {
			min = 0;
			span = 8;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < quantita; i++) {
			tipo = Random.getInt(span) + min;
			sb.append("Aprendo il");
			if (quantita > 1) {
				sb.append(' ').append(Misc.getOrdinaleM(i + 1, false));
			}
			if (Random.getInt(2) == 0) {
				sb.append(" cofano, ");
			} else {
				sb.append(" forziere, ");
			}
			if (i == 0) {
				sb.append(gruppo.chi()).append(' ');
			}
			if (tipo == 0) {
				sb.append("non trova nulla.");
			} else if (tipo == 1) {
				sb.append("trova una pergamena con un ");
				ClassiIncantesimo classeIncantesimo = ClassiIncantesimo.casuale();
				Incantesimo inc = classeIncantesimo.getIstanza();
				sb.append(inc.getNomeSingolare()).append('.');
				gruppo.addIncantesimi(classeIncantesimo, 1);
			} else if (tipo == 2) {
				sb.append("trova una pergamena con tre ");
				ClassiIncantesimo classeIncantesimo = ClassiIncantesimo.casuale();
				Incantesimo inc = classeIncantesimo.getIstanza();
				sb.append(inc.getNomePlurale()).append('.');
				gruppo.addIncantesimi(classeIncantesimo, 3);
			} else if (tipo == 3) {
				sb.append("trova una pozione della forza.");
				gruppo.addPozioniForza(1);
			} else if (tipo == 4) {
				sb.append("trova una pozione della grande forza.");
				gruppo.addPozioniGrandeForza(1);
			} else if (tipo == 5) {
				sb.append("trova una pozione della magia.");
				gruppo.addPozioniMagia(1);
			} else if (tipo == 6) {
				sb.append("trova alcune monete d'oro.");
				gruppo.addMonete(5 + Random.getInt(5));
			} else if (tipo == 7) {
				sb.append("trova alcune gemme.");
				gruppo.addPreziosi(5 + Random.getInt(5));
			}
			if (i < quantita - 1) {
				sb.append(' ');
			}
		}
		UI.notifica(sb.toString());
		return super.prendi(gruppo, azione);
	}
}
