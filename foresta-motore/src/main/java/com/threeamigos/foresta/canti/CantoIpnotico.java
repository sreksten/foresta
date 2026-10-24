package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.tipi.tattiche.TipoAzione;
import com.threeamigos.foresta.combattimento.AnnuncioDellAzione;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoCanto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Il canto che scuote il morale degli avversari: a caso {@code SPAVENTATO} o {@code CONFUSO} (lo stesso per tutti
 * quelli del canto), su fino a NUMERO_BERSAGLI avversari vivi scelti a caso. Ognuno resiste con il suo CORAGGIO contro
 * la SOGGEZIONE del cantore; chi è immune all'effetto lo resiste da sé (vedi Personaggio.addEffettoDiStato).
 */
public class CantoIpnotico implements Canto {

	private final int numeroBersagli;

	public CantoIpnotico(Personaggio cantore) {
		this.numeroBersagli = cantore.getBersagli();
	}

	@Override
	public TipoCanto getTipo() {
		return TipoCanto.IPNOTICO;
	}

	/**
	 * La probabilità, su 100, che l'avversario ceda: la quota della SOGGEZIONE del cantore sul totale con il CORAGGIO di
	 * chi ascolta. Con un coraggio nullo cede sempre, con una soggezione nulla mai.
	 */
	public static double probabilitaDiCedere(Personaggio cantore, Personaggio avversario) {
		return probabilitaDiCedere(cantore, avversario, 1.0d);
	}

	/**
	 * Come {@link #probabilitaDiCedere(Personaggio, Personaggio)}, con la SOGGEZIONE del cantore moltiplicata (vedi
	 * UrloDellaBanshee).
	 */
	public static double probabilitaDiCedere(Personaggio cantore, Personaggio avversario, double moltiplicatoreSoggezione) {
		double soggezione = Math.max(0, cantore.getSoggezione()) * moltiplicatoreSoggezione;
		double coraggio = Math.max(0, avversario.getCoraggio());
		if (soggezione + coraggio <= 0) {
			return 0.0d;
		}
		return 100.0d * soggezione / (soggezione + coraggio);
	}

	@Override
	public boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<Personaggio> ascoltatori = new ArrayList<>(gruppoAvversario.getPersonaggiVivi());
		if (ascoltatori.isEmpty()) {
			return false;
		}
		Collections.shuffle(ascoltatori, Dado.sorgente());
		List<Personaggio> bersagli = ascoltatori.subList(0, Math.min(numeroBersagli, ascoltatori.size()));
		TipoEffettoDiStato effetto = Dado.tira(2) == 1 ? TipoEffettoDiStato.SPAVENTATO : TipoEffettoDiStato.CONFUSO;

		AnnuncioDellAzione.azione(cantore, bersagli.size() == 1 ? bersagli.get(0) : null, TipoAzione.CANTO_IPNOTICO);
		cantore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(cantore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " intona un canto ipnotico."));
		for (Personaggio avversario : bersagli) {
			if (Dado.tira(100) <= probabilitaDiCedere(cantore, avversario)) {
				avversario.addEffettoDiStato(effetto, CalcolatoreCombattimento.calcolaDurataStato(avversario, effetto), 0);
			} else {
				BusEventi.pubblica(new NotificaTestoFrase(avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
						Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " resiste al canto."));
			}
		}
		return true;
	}
}
