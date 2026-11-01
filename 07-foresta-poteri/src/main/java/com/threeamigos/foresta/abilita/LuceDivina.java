package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.CostantiAbilitaInnate;
import com.threeamigos.foresta.tipi.TipoAbilita;

import java.util.List;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'abilità innata di Sacerdote e Sacerdotessa: una luce sacra che brucia i non morti (il SACRO fa il doppio dei danni a chi è di categoria
 * NON_MORTO), con danni propri (ABILITA_LUCE_DIVINA_DANNI) e un costo di lancio in proporzione ai danni del Fulmine (ABILITA_LUCE_DIVINA_COSTO_LANCIO), su più avversari. Chi la conosce lo dice {@link AbilitaInnate}.
 * <p>
 * Non fa distinzioni: chi la invoca ne investe anche un non morto del proprio gruppo (lo Scheletro della Mietitura), che le cure non guariscono
 * (vedi Personaggio.isCurabile) ma la luce sì. Il Gargoyle di pietra non la sente, come ogni danno che non sia fisico.
 */
public class LuceDivina extends AbilitaDiDanno {

	public LuceDivina(Personaggio utilizzatore) {
		super(utilizzatore);
	}

	@Override
	public TipoAbilita getTipo() {
		return TipoAbilita.LUCE_DIVINA;
	}

	@Override
	public TipoDanno getTipoDanno() {
		return TipoDanno.SACRO;
	}

	@Override
	public int getDanni() {
		return CostantiAbilitaInnate.ABILITA_LUCE_DIVINA_DANNI;
	}

	@Override
	protected String getAnnuncio(Personaggio utilizzatore, List<Personaggio> bersagli) {
		return bersagli.size() == 1
				? Testi.formatta("messaggi_poteri", "LuceDivina.annuncio", chi(utilizzatore),
						bersagli.get(0).getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE))
				: Testi.formatta("messaggi_poteri", "LuceDivina.annuncioTutti", chi(utilizzatore));
	}

	/**
	 * Oltre ai bersagli, se a invocarla è uno dei personaggi del giocatore, colpisce i suoi non morti vivi.
	 */
	@Override
	protected void dopoIColpi(Personaggio utilizzatore) {
		GruppoGiocatore gruppoGiocatore = GruppoGiocatore.getIstanza();
		if (!gruppoGiocatore.contiene(utilizzatore)) {
			return;
		}
		for (Personaggio alleato : gruppoGiocatore.getPersonaggiVivi()) {
			if (!alleato.isCurabile()) {
				BusEventi.pubblica(new NotificaTestoFrase(Testi.formatta("messaggi_poteri", "LuceDivina.1", alleato.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE))));
				colpisci(utilizzatore, alleato, 1.0d);
			}
		}
	}
}
