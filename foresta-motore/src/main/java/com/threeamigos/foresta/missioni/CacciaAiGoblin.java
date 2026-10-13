package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.tipi.ClasseMissione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * In città un mercante chiede di liberare le strade dai goblin che assaltano le carovane: sconfitti
 * {@link #GOBLIN_DA_SCONFIGGERE} goblin, dovunque, si torna a riscuotere (vedi IncaricoInCitta).
 */
public class CacciaAiGoblin extends IncaricoInCitta {

	public static final int GOBLIN_DA_SCONFIGGERE = 3;
	private static final int RICOMPENSA = 15;
	private static final String CACCIA = "CACCIA";

	public CacciaAiGoblin() {
		super(ClasseMissione.CACCIA_AI_GOBLIN);
	}

	@Override
	public String getNome() {
		return "Caccia ai goblin";
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return "Le strade sono libere: torna dal mercante di " + getNomeCitta() + " a riscuotere.";
		}
		return "Un mercante di " + getNomeCitta() + " ti ha chiesto di sconfiggere " + GOBLIN_DA_SCONFIGGERE
				+ " goblin. Finora: " + getConteggioNelPassoCorrente(eventoSconfitto(TipoPersonaggio.GOBLIN)) + ".";
	}

	@Override
	protected String primoPassoDelCompito() {
		return CACCIA;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		if (!CACCIA.equals(id)) {
			throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
		return sconfiggi(MomentoControllo.POST_LOCAZIONE, TipoPersonaggio.GOBLIN, GOBLIN_DA_SCONFIGGERE)
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("Di goblin, per un po', non se ne vedranno: è ora di tornare dal mercante di "
						+ getNomeCitta() + ".")))
				.poi(RITORNO);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Ehi, voi! Avete l'aria di gente che sa menare le mani.")
				.parlaIlMandante("I goblin assaltano le mie carovane appena escono dalla città. Ne sconfiggete "
						+ GOBLIN_DA_SCONFIGGERE + " e vi do " + RICOMPENSA + " monete.")
				.parlaIlCapo("Goblin? Affare fatto.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Le carovane passano di nuovo! Siete stati di parola.")
				.parlaIlCapo("E adesso tocca a te.");
	}

	@Override
	protected String testoAccettazione() {
		return "Il mercante pagherà " + RICOMPENSA + " monete per " + GOBLIN_DA_SCONFIGGERE + " goblin sconfitti.";
	}

	@Override
	protected String testoRicompensa() {
		return "Il mercante paga le " + RICOMPENSA + " monete promesse.";
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
