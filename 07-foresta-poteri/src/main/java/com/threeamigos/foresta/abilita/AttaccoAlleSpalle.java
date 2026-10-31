package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.combattimento.AnnuncioDellAzione;
import com.threeamigos.foresta.combattimento.AttaccoConLeArmi;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoAbilita;

import java.util.List;

/**
 * L'abilità innata del Ladro: mentre due compagni distraggono gli avversari, colpisce alle spalle con le sue armi, e il colpo va sempre a segno ed è sempre un
 * critico (vedi {@link AttaccoConLeArmi#alleSpalle}). Non costa Magia; servono almeno tre personaggi vivi nel gruppo ({@link #isGruppoAbbastanzaGrande}).
 * Chi la conosce lo dice {@link AbilitaInnate}.
 */
public class AttaccoAlleSpalle implements Abilita {

	/**
	 * @param utilizzatore il Ladro (l'abilità non dipende dal suo livello: lo fa il calcolo del colpo)
	 */
	public AttaccoAlleSpalle(Personaggio utilizzatore) {
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.ATTACCO_ALLE_SPALLE;
	}

	/**
	 * Se nel gruppo ci sono abbastanza personaggi vivi perché due distraggano gli avversari e il Ladro colpisca alle spalle.
	 */
	public static boolean isGruppoAbbastanzaGrande(Gruppo gruppo) {
		return gruppo.getPersonaggiVivi().size() >= CostantiAbilitaInnate.ABILITA_ATTACCO_ALLE_SPALLE_PERSONAGGI_MINIMI;
	}

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		return usaSu(utilizzatore, null, gruppo, gruppoAvversario);
	}

	/**
	 * Colpisce il bersaglio scelto (da una tattica), se è ancora in piedi; altrimenti il primo avversario vivo.
	 */
	@Override
	public boolean usaSu(Personaggio utilizzatore, Personaggio scelto, Gruppo gruppo, Gruppo gruppoAvversario) {
		List<Personaggio> avversari = gruppoAvversario.getPersonaggiVivi();
		if (avversari.isEmpty() || !isGruppoAbbastanzaGrande(gruppo)) {
			return false;
		}
		Personaggio bersaglio = scelto != null && scelto.isVivo() && avversari.contains(scelto) ? scelto : avversari.get(0);
		AnnuncioDellAzione.azione(utilizzatore, bersaglio, getTipo().getTipoAzione());
		BusEventi.pubblica(new NotificaTestoFrase(utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " colpisce alle spalle "
				+ bersaglio.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + ", distratto dai compagni."));
		AttaccoConLeArmi.alleSpalle(utilizzatore, bersaglio);
		utilizzatore.subMagia(getCostoLancio());
		return true;
	}
}
