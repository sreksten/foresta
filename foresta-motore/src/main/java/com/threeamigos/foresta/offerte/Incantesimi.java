package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraIncantesimiEPozioni;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

/**
 * L'offerta di alcune pergamene di un incantesimo scelto a caso, a metà del prezzo abituale per ciascuna. Occorrono le monete per pagarle.
 */
public class Incantesimi implements Offerta {

	private final TipoIncantesimo tipoIncantesimo;
	private final int quantita;
	private final int costo;

	public Incantesimi() {
		tipoIncantesimo = FabbricaIncantesimi.casuale();
		quantita = Dado.tira(3);
		costo = quantita * FabbricaIncantesimi.costoAcquisto(tipoIncantesimo) / 2;
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
		sb.append(" è dispost").append(capoAvversario.getLetteraFinaleAttributo()).append(" a vendere ");
		if (quantita == 1) {
			sb.append("un ");
		} else {
			sb.append(Numeri.getCardinaleM(quantita)).append(' ');
		}
		if (quantita == 1) {
			sb.append(tipoIncantesimo.getNomeSingolare());
		} else {
			sb.append(tipoIncantesimo.getNomePlurale());
		}
		sb.append('.');
		return sb.toString();
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		gruppo.subMonete(costo);
		gruppo.addIncantesimi(tipoIncantesimo, quantita);
		BusEventi.pubblica(new InternoMostraFinestraIncantesimiEPozioni());
	}
}
