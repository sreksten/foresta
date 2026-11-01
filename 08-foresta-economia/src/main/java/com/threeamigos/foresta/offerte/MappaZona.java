package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneConoscenzaMappa;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'offerta gratuita della mappa della zona intorno al gruppo, un quadrato di 15 caselle per lato (meno vicino ai bordi della Foresta).
 */
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
		Personaggio capo = gruppoAvversario.getCapo();
		String sesso = capo.getSesso() == Personaggio.Sesso.MASCHIO ? "M" : "F";
		boolean inPiu = gruppoAvversario.getNumeroPersonaggiVivi() > 1;
		String a = gruppo.getCapo().getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE);
		return inPiu ? Testi.formatta("offerte", "MAPPA_ZONA.GRUPPO." + sesso, a)
				: Testi.formatta("offerte", "MAPPA_ZONA.SINGOLO." + sesso, capo.getADS() + capo.getNomeSingolare(), a);
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		int x = Foresta.getPosizioneDelGruppo().getX();
		int y = Foresta.getPosizioneDelGruppo().getY();
		int dm1x = Foresta.getDimensioneX() - 1;
		Logger.log(() -> "MappaZona::accetta(), x=" + x + ",y=" + y);
		int fx = x - 7;
		int tx = x + 7;
		if (Logger.isAttivo()) {
			Logger.log("MappaZona::accetta(), fx=" + fx + ",tx=" + tx);
		}
		if (fx < 0) {
			fx = 0;
			tx = Math.min(dm1x, 14);
		} else if (tx >= Foresta.getDimensioneX()) {
			fx = Foresta.getDimensioneX() - (Math.min(Foresta.getDimensioneX(), 15));
			tx = dm1x;
		}
		int dm1y = Foresta.getDimensioneY() - 1;
		int fy = y - 7;
		int ty = y + 7;
		if (Logger.isAttivo()) {
			Logger.log("MappaZona::accetta(), fy=" + fy + ",ty=" + ty);
		}
		if (fy < 0) {
			fy = 0;
			ty = Math.min(dm1y, 14);
		} else if (ty >= Foresta.getDimensioneY()) {
			fy = Foresta.getDimensioneY() - (Math.min(Foresta.getDimensioneY(), 15));
			ty = dm1y;
		}
		if (Logger.isAttivo()) {
			Logger.log("MappaZona::accetta(), fx=" + fx + ",fy=" + fy + ",tx=" + tx + ",ty=" + ty);
		}
		Foresta.ottieniMappaZona(fx, fy, tx, ty);
		BusEventi.pubblica(new NotificaVariazioneConoscenzaMappa(fx, fy, tx, ty));
	}
}
