package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraIncantesimiEPozioni;
import com.threeamigos.foresta.incantesimi.FabbricaIncantesimi;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

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
		costo = quantita * tipoIncantesimo.getCostoAcquisto() / 2;
	}

	@Override
	public boolean isFattibile(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return Cassa.getMonete() >= costo;
	}

	@Override
	public boolean isGratuita(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		return false;
	}

	@Override
	public String getDescrizione(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Personaggio capo = gruppoAvversario.getCapo();
		String sesso = capo.getSesso() == Personaggio.Sesso.MASCHIO ? "M" : "F";
		boolean inPiu = gruppoAvversario.getNumeroPersonaggiVivi() > 1;
		String prezzo = costo == 1 ? Testi.formatta("offerte", "MONETE_UNA") : Testi.formatta("offerte", "MONETE_N", costo);
		String venduto = quantita == 1 ? Testi.formatta("offerte", "INCANTESIMI.UNO", tipoIncantesimo.getNomeSingolare())
				: Testi.formatta("offerte", "INCANTESIMI.N", Lingue.corrente().cardinale(quantita, Genere.MASCHILE), tipoIncantesimo.getNomePlurale());
		return inPiu ? Testi.formatta("offerte", "INCANTESIMI.GRUPPO." + sesso, prezzo, venduto)
				: Testi.formatta("offerte", "INCANTESIMI.SINGOLO." + sesso, prezzo, capo.getADS() + capo.getNomeSingolare(), venduto);
	}

	@Override
	public void accetta(GruppoGiocatore gruppo, GruppoAvversario gruppoAvversario) {
		Cassa.subMonete(costo);
		gruppo.addIncantesimi(tipoIncantesimo, quantita);
		BusEventi.pubblica(new InternoMostraFinestraIncantesimiEPozioni());
	}
}
