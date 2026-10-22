package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoAbilita;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;

/**
 * L'abilità del Druido che rafforza la difesa di tutto il gruppo: a ogni personaggio vivo dà due modificatori temporanei, parata e resistenza magica
 * in percentuale (ABILITA_PELLE_DI_CORTECCIA_*), per qualche turno di combattimento. L'Alba sacra non li toglie. Usarla di nuovo non raddoppia
 * l'effetto: rinfresca la durata (vedi Personaggio.addModificatore).
 */
public class PelleDiCorteccia implements Abilita {

	/**
	 * La nota dei due modificatori: li distingue dagli altri
	 */
	public static final String NOTA = "Pelle di corteccia";

	public PelleDiCorteccia(Personaggio utilizzatore) {
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.PELLE_DI_CORTECCIA;
	}

	@Override
	public int getCostoLancio() {
		return Costanti.ABILITA_PELLE_DI_CORTECCIA_COSTO_LANCIO;
	}

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		utilizzatore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " ricopre il gruppo di una pelle di corteccia."));
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			personaggio.addModificatore(modificatore(TipoAttributo.PARATA, Costanti.ABILITA_PELLE_DI_CORTECCIA_PERCENTUALE_PARATA));
			personaggio.addModificatore(modificatore(TipoAttributo.RESISTENZA_MAGICA,
					Costanti.ABILITA_PELLE_DI_CORTECCIA_PERCENTUALE_RESISTENZA_MAGICA));
		}
		return true;
	}

	private static ModificatoreAttributoMD modificatore(TipoAttributo attributo, double percentuale) {
		return new ModificatoreAttributoMD(attributo, TipoModificatore.VARIAZIONE_PERCENTUALE, percentuale, NOTA,
				UnitaDurataModificatore.TURNI, Costanti.ABILITA_PELLE_DI_CORTECCIA_DURATA, BersaglioModificatore.PORTATORE, false);
	}
}
