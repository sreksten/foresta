package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.personaggi.Sacerdote;
import com.threeamigos.foresta.personaggi.Sacerdotessa;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il sacerdote o la sacerdotessa che ha commissionato una missione (la benedizione, il giro delle sette chiese) si offre a
 * fine missione di unirsi al gruppo. L'offerta è una sola per partita: se un sacerdote è già entrato, le altre missioni
 * non la ripetono; un rifiuto, invece, non la preclude. Entra solo chi ha posto fra i personaggi fissi (non contano gli
 * ospiti e chi è a tempo).
 */
public final class OffertaDelSacerdote {

	private OffertaDelSacerdote() {
	}

	/**
	 * Se si può fare la proposta: nessun sacerdote è ancora entrato e il gruppo ha meno del massimo di personaggi fissi.
	 */
	public static boolean puoProporsi() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		return !gruppo.isSacerdoteAccolto() && gruppo.getNumeroPersonaggiPermanenti() < Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE;
	}

	/**
	 * Se il sacerdote non è ancora entrato ma il gruppo è già al completo, lo dice con garbo: la proposta non si fa.
	 *
	 * @param mandante chi si sarebbe offerto, con l'articolo ("il sacerdote", "la sacerdotessa")
	 */
	public static void avvisaSeIlGruppoEPieno(String mandante) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		if (!gruppo.isSacerdoteAccolto() && !puoProporsi()) {
			BusEventi.pubblica(new NotificaTestoParagrafo("Siete già in " + Numeri.getCardinaleM(Costanti.MAX_PERSONAGGI_GRUPPO_GIOCATORE)
					+ ": non penso abbiate bisogno anche dell'aiuto " + Articoli.conPreposizione("di", mandante) + "."));
		}
	}

	/**
	 * Il sacerdote (o la sacerdotessa, secondo la classe) entra nel gruppo, e la proposta non si ripete più.
	 *
	 * @param classe SACERDOTE o SACERDOTESSA
	 */
	public static void accogli(TipoPersonaggio classe) {
		Personaggio sacerdote = classe == TipoPersonaggio.SACERDOTESSA
				? new Sacerdotessa(ProduttoreDiTestiCasuale.nomeSacerdotessa(), 1)
				: new Sacerdote(ProduttoreDiTestiCasuale.nomeSacerdote(), 1);
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		gruppo.aggiungiPersonaggio(RegistroPersonaggi.preparaCompagno(sacerdote));
		gruppo.setSacerdoteAccolto(true);
		BusEventi.pubblica(new InternoMostraFinestraStato());
	}
}
