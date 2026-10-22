package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.combattimento.CalcolatoreCombattimento;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.Dado;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * L'abilità del Chierico che spaventa i non morti: fino a NUMERO_BERSAGLI avversari non morti vivi (e non già spaventati) scelti a caso. Ognuno
 * resiste con il suo CORAGGIO contro la SOGGEZIONE del chierico, come per il canto ipnotico; chi è immune allo spavento lo resiste da sé.
 */
public class ScacciareINonMorti implements Abilita {

	private final int numeroBersagli;

	public ScacciareINonMorti(Personaggio utilizzatore) {
		this.numeroBersagli = utilizzatore.getBersagli();
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.SCACCIARE_I_NON_MORTI;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.ABILITA_SCACCIARE_I_NON_MORTI_COSTO_LANCIO;
	}

	/**
	 * La probabilità, su 100, che il non morto ceda: la quota della SOGGEZIONE di chi scaccia sul totale con il CORAGGIO del non morto
	 */
	public static double probabilitaDiCedere(Personaggio utilizzatore, Personaggio avversario) {
		double soggezione = Math.max(0, utilizzatore.getSoggezione());
		double coraggio = Math.max(0, avversario.getCoraggio());
		if (soggezione + coraggio <= 0) {
			return 0.0d;
		}
		return 100.0d * soggezione / (soggezione + coraggio);
	}

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<Personaggio> nonMorti = new ArrayList<>();
		for (Personaggio avversario : gruppoAvversario.getPersonaggiVivi()) {
			if (AbilitaInnate.isDaScacciare(avversario)) {
				nonMorti.add(avversario);
			}
		}
		if (nonMorti.isEmpty()) {
			return false;
		}
		Collections.shuffle(nonMorti, Dado.sorgente());
		List<Personaggio> bersagli = nonMorti.subList(0, Math.min(numeroBersagli, nonMorti.size()));

		utilizzatore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " leva il simbolo sacro contro i non morti."));
		for (Personaggio avversario : bersagli) {
			if (Dado.tira(100) <= probabilitaDiCedere(utilizzatore, avversario)) {
				avversario.addEffettoDiStato(TipoEffettoDiStato.SPAVENTATO,
						CalcolatoreCombattimento.calcolaDurataStato(avversario, TipoEffettoDiStato.SPAVENTATO), 0);
			} else {
				BusEventi.pubblica(new NotificaTestoFrase(avversario.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
						Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " resiste."));
			}
		}
		return true;
	}
}
