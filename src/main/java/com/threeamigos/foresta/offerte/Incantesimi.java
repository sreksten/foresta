package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Misc;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;
import com.threeamigos.foresta.ui.InterfacciaUtente;

public class Incantesimi implements Offerta {

	private ClassiIncantesimo classeIncantesimo;
	private int quantita;
	private int costo;

	public Incantesimi() {
		classeIncantesimo = ClassiIncantesimo.casuale();
		quantita = Random.getInt(3) + 1;
		costo = quantita * classeIncantesimo.getIstanza().getCostoAcquisto() / 2;
	}

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return gruppo.getMonete() >= costo;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return false;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio capoAvversario = gruppoAvversario.getCapo();
		int numero = gruppoAvversario.getPersonaggiVivi().size();
		Personaggio.Sesso sesso = capoAvversario.getSesso();
		StringBuilder sb = new StringBuilder();
		if (numero > 1) {
			if (sesso == Personaggio.Sesso.MASCHIO) {
				sb.append("Uno");
			} else {
				sb.append("Una");
			}
			sb.append(" di loro per ");
			if (costo == 1) {
				sb.append("una moneta");
			} else {
				sb.append(costo).append(" monete");
			}
		} else {
			sb.append("Per ");
			if (costo == 1) {
				sb.append("una moneta ");
			} else {
				sb.append(costo).append(" monete ");
			}
			sb.append(capoAvversario.getADS()).append(capoAvversario.getNomeSingolare());
		}
		sb.append(" e' dispost").append(sesso == Personaggio.Sesso.MASCHIO ? 'o' : 'a').append(" a vendere ");
		if (quantita == 1) {
			sb.append("un ");
		} else {
			sb.append(Misc.getCardinaleM(quantita)).append(' ');
		}
		Incantesimo i = classeIncantesimo.getIstanza();
		if (quantita == 1) {
			sb.append(i.getNomeSingolare());
		} else {
			sb.append(i.getNomePlurale());
		}
		sb.append('.');
		return sb.toString();
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		gruppo.subMonete(costo);
		gruppo.addIncantesimi(classeIncantesimo, quantita);
		UI.primoPiano(InterfacciaUtente.Finestra.INCANTESIMI);
		UI.primoPiano(InterfacciaUtente.Finestra.MAPPA);
		UI.rinfresca();
	}
}
