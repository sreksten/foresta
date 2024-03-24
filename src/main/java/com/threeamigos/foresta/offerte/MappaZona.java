package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.ui.UI;

public class MappaZona implements Offerta {

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return true;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return true;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio capoAvversario = gruppoAvversario.getCapo();
		Personaggio.Sesso sesso = capoAvversario.getSesso();
		StringBuilder sb = new StringBuilder();
		if (gruppoAvversario.getNumeroPersonaggiVivi() > 1) {
			if (sesso == Personaggio.Sesso.MASCHIO) {
				sb.append("Una");
			} else {
				sb.append("Uno");
			}
			sb.append(" di loro");
		} else {
			sb.append("Durante la conversazione ").append(capoAvversario.getADS()).append(capoAvversario.getNomeSingolare());
		}
		sb.append(" fa vedere a ").append(gruppo.getCapo().getNome()).append(" una mappa della zona.");
		return sb.toString();
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		int x = gruppo.getX();
		int y = gruppo.getY();
		int dm1x = Foresta.getDimensioneX() - 1;
		Logger.log("MappaZona::accetta(), x=" + x + ",y=" + y);
		int fx = x - 7;
		int tx = x + 7;
		Logger.log("MappaZona::accetta(), fx=" + fx + ",tx=" + tx);
		if (fx < 0) {
			fx = 0;
			tx = dm1x < 14 ? dm1x : 14;
		} else if (tx >= Foresta.getDimensioneX()) {
			fx = Foresta.getDimensioneX() - (Foresta.getDimensioneX() < 15 ? Foresta.getDimensioneX() : 15);
			tx = dm1x;
		}
		int dm1y = Foresta.getDimensioneY() - 1;
		int fy = y - 7;
		int ty = y + 7;
		Logger.log("MappaZona::accetta(), fy=" + fy + ",ty=" + ty);
		if (fy < 0) {
			fy = 0;
			ty = dm1y < 14 ? dm1y : 14;
		} else if (ty >= Foresta.getDimensioneY()) {
			fy = Foresta.getDimensioneY() - (Foresta.getDimensioneY() < 15 ? Foresta.getDimensioneY() : 15);
			ty = dm1y;
		}
		Logger.log("MappaZona::accetta(), fx=" + fx + ",fy=" + fy + ",tx=" + tx + ",ty=" + ty);
		Foresta.ottieniMappaZona(fx, fy, tx, ty);
		UI.variaMappa();
	}
}
