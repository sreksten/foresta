package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.combattimento.AnnuncioDellAzione;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoAzione;

/**
 * L'alba sacra: l'abilità innata di Sacerdote e Sacerdotessa che toglie al gruppo gli stati alterati negativi e i modificatori temporanei
 * (lasciando l'ispirazione e il berserk). Costa Magia e non consuma pergamene. Si può invocare anche fuori dagli scontri, se qualcuno ha
 * uno stato da togliere (vedi {@link AbilitaInnate#serve}).
 *
 * @author Stefano Reksten
 */
public class AlbaSacra implements Abilita {

	public AlbaSacra(Personaggio utilizzatore) {
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.ALBA_SACRA;
	}

	/**
	 * Toglie a tutto il gruppo gli stati alterati negativi e i modificatori temporanei rimovibili (non l'ignominia della fuga), e fa pagare
	 * il lancio a chi la invoca.
	 */
	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		AnnuncioDellAzione.azione(utilizzatore, null, TipoAzione.ALBA_SACRA);
		BusEventi.pubblica(new NotificaTestoFrase(utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " invoca l'Alba Sacra."));
		for (Personaggio personaggio : gruppo.getPersonaggi()) {
			personaggio.rimuoviGliEffettiDiStatoNegativi();
			personaggio.rimuoviIModificatoriTemporaneiRimovibili();
		}
		utilizzatore.subMagia(getCostoLancio());
		return true;
	}
}
