package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;

/**
 * L'abilità del Druido che rafforza la difesa di tutto il gruppo: a ogni personaggio vivo dà due modificatori temporanei, parata e resistenza magica
 * di un numero fisso di punti (ABILITA_PELLE_DI_CORTECCIA_BASE_* per la radice quadrata del livello del druido), per qualche turno di combattimento.
 * Sono punti e non una percentuale: chi non è un guerriero ha pochissima parata e resistenza magica, e una percentuale non cambierebbe niente. L'Alba
 * sacra non li toglie. Usarla di nuovo non raddoppia l'effetto: rinfresca la durata (vedi Personaggio.addModificatore).
 */
public class PelleDiCorteccia implements Abilita {

	/**
	 * La nota dei due modificatori: li distingue dagli altri
	 */
	public static final String NOTA = "Pelle di corteccia";

	private final int livello;

	public PelleDiCorteccia(Personaggio utilizzatore) {
		this.livello = utilizzatore.getLivello();
	}

	/**
	 * Quanti punti di parata dà a ognuno del gruppo
	 */
	public int getPuntiParata() {
		return (int) Math.round(CostantiAbilitaInnate.ABILITA_PELLE_DI_CORTECCIA_BASE_PARATA * Math.sqrt(livello));
	}

	/**
	 * Quanti punti di resistenza magica dà a ognuno del gruppo
	 */
	public int getPuntiResistenzaMagica() {
		return (int) Math.round(CostantiAbilitaInnate.ABILITA_PELLE_DI_CORTECCIA_BASE_RESISTENZA_MAGICA * Math.sqrt(livello));
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.PELLE_DI_CORTECCIA;
	}

	@Override
	public boolean usa(Personaggio utilizzatore, Gruppo gruppo, Gruppo gruppoAvversario) {
		utilizzatore.subMagia(getCostoLancio());
		BusEventi.pubblica(new NotificaTestoFrase(utilizzatore.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE,
				Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) + " ricopre il gruppo di una pelle di corteccia."));
		for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
			personaggio.addModificatore(modificatore(TipoAttributo.PARATA, getPuntiParata()));
			personaggio.addModificatore(modificatore(TipoAttributo.RESISTENZA_MAGICA, getPuntiResistenzaMagica()));
		}
		return true;
	}

	private static ModificatoreAttributoMD modificatore(TipoAttributo attributo, int punti) {
		return new ModificatoreAttributoMD(attributo, TipoModificatore.VARIAZIONE_FISSA, punti, NOTA,
				UnitaDurataModificatore.TURNI, CostantiAbilitaInnate.ABILITA_PELLE_DI_CORTECCIA_DURATA, BersaglioModificatore.PORTATORE, false);
	}
}
