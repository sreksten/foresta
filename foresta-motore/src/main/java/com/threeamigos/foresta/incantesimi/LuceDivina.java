package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Gruppo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.*;

/**
 * La luce sacra che brucia i non morti (il SACRO fa il doppio dei danni a chi è di categoria NON_MORTO): danni come il
 * Fulmine, su più avversari. È innata per Sacerdote, Sacerdotessa, Chierico e Chierica, che la lanciano con la sola MAGIA, e per gli altri
 * una pergamena come ogni incantesimo.
 * <p>
 * Non fa distinzioni: chi la invoca ne investe anche un non morto del proprio gruppo (lo Scheletro della Mietitura), che
 * le cure non guariscono (vedi Personaggio.isCurabile) ma la luce sì.
 */
public class LuceDivina extends IncantesimoMaleficoImpl implements Incantesimo {

	public LuceDivina(int livello) {
		super(livello);
	}

	/**
	 * @return true se la classe sa lanciare la luce divina senza pergamene (Sacerdote, Sacerdotessa, Chierico e Chierica)
	 */
	public static boolean conosciutaDa(TipoPersonaggio classe) {
		return classe == TipoPersonaggio.SACERDOTE || classe == TipoPersonaggio.SACERDOTESSA
				|| classe == TipoPersonaggio.CHIERICO || classe == TipoPersonaggio.CHIERICA;
	}

	/**
	 * @return true se il personaggio è vivo, la conosce per natura e ha abbastanza MAGIA per lanciarla: in tal caso non
	 * le servono pergamene, e se ne ha il lancio non ne consuma
	 */
	public static boolean puoLanciarlaInnata(Personaggio personaggio) {
		return personaggio.isVivo() && conosciutaDa(personaggio.getClasse())
				&& personaggio.getMagia() >= Costanti.INCANTESIMO_LUCE_DIVINA_COSTO_LANCIO
				&& !personaggio.hasEffettoDiStato(TipoEffettoDiStato.SILENZIATO);
	}

	public TipoIncantesimo getClasse() {
		return TipoIncantesimo.LUCE_DIVINA;
	}

	public int getCostoLancio() {
		return Costanti.INCANTESIMO_LUCE_DIVINA_COSTO_LANCIO;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.SACRO;
	}

	public int getDanni() {
		return Costanti.INCANTESIMO_LUCE_DIVINA_DANNI;
	}

	/**
	 * Come ogni incantesimo di danno sui bersagli, e in più sui non morti vivi del gruppo del giocatore se a lanciarla è
	 * uno dei suoi personaggi. Il tiro per colpire e il danno sono quelli di ogni bersaglio.
	 */
	@Override
	public void formula(Personaggio formulante, Personaggio personaggioBersaglio, Gruppo gruppoBersaglio) {
		super.formula(formulante, personaggioBersaglio, gruppoBersaglio);
		GruppoGiocatore gruppoGiocatore = GruppoGiocatore.getIstanza();
		if (!gruppoGiocatore.contiene(formulante)) {
			return;
		}
		for (Personaggio alleato : gruppoGiocatore.getPersonaggiVivi()) {
			if (!alleato.isCurabile()) {
				BusEventi.pubblica(new NotificaTestoFrase("La luce divina investe anche "
						+ alleato.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE) + "."));
				colpisci(formulante, alleato);
			}
		}
	}
}
