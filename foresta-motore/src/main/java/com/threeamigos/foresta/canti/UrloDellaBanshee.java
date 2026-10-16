package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.threeamigos.foresta.tipi.TipoCanto;

/**
 * Il canto ipnotico potenziato (vedi {@link CantoIpnotico}), che il bardo della locanda insegna al gruppo dopo il viaggio
 * della moglie: lo stesso numero di bersagli scelti a caso fra gli avversari vivi, ma la SOGGEZIONE del cantore pesa di più
 * contro il CORAGGIO di chi ascolta (Costanti.CANTO_URLO_DELLA_BANSHEE_MOLTIPLICATORE_SOGGEZIONE) e chi cede, con un solo
 * tiro, prende <b>sia</b> {@code SPAVENTATO} <b>sia</b> {@code CONFUSO}. Costa più del canto ipnotico.
 */
public class UrloDellaBanshee implements Canto {

	private final int numeroBersagli;

	public UrloDellaBanshee(Personaggio cantore) {
		this.numeroBersagli = cantore.getBersagli();
	}

	@Override
	public TipoCanto getTipo() {
		return TipoCanto.URLO_DELLA_BANSHEE;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.CANTO_URLO_DELLA_BANSHEE_COSTO_LANCIO;
	}

	/**
	 * La probabilità, su 100, che l'avversario ceda: quella del canto ipnotico con la soggezione del cantore moltiplicata.
	 */
	public static double probabilitaDiCedere(Personaggio cantore, Personaggio avversario) {
		return CantoIpnotico.probabilitaDiCedere(cantore, avversario, Costanti.CANTO_URLO_DELLA_BANSHEE_MOLTIPLICATORE_SOGGEZIONE);
	}

	@Override
	public boolean canta(Personaggio cantore, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<Personaggio> ascoltatori = new ArrayList<>(gruppoAvversario.getPersonaggiVivi());
		if (ascoltatori.isEmpty()) {
			return false;
		}
		Collections.shuffle(ascoltatori, Dado.sorgente());
		List<Personaggio> bersagli = ascoltatori.subList(0, Math.min(numeroBersagli, ascoltatori.size()));

		cantore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(cantore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " lancia l'Urlo della Banshee."));
		for (Personaggio avversario : bersagli) {
			if (Dado.tira(100) <= probabilitaDiCedere(cantore, avversario)) {
				for (TipoEffettoDiStato effetto : new TipoEffettoDiStato[]{TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO}) {
					avversario.addEffettoDiStato(effetto, CalcolatoreCombattimento.calcolaDurataStato(avversario, effetto), 0);
				}
			} else {
				BusEventi.pubblica(new NotificaTestoFrase(avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
						Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " resiste all'urlo."));
			}
		}
		return true;
	}
}
