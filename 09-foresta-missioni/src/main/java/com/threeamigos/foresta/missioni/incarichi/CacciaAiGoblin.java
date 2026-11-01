package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.testi.Testi;

/**
 * In città un mercante chiede di liberare le strade dai goblin che assaltano le carovane: sconfitti
 * {@link #GOBLIN_DA_SCONFIGGERE} goblin, dovunque, si torna a riscuotere (vedi IncaricoInCitta).
 */
public class CacciaAiGoblin extends IncaricoInCitta {

	public static final int GOBLIN_DA_SCONFIGGERE = 3;
	private static final int RICOMPENSA = 15;
	private static final String CACCIA = "CACCIA";

	public CacciaAiGoblin() {
		super(MissionePredefinita.CACCIA_AI_GOBLIN);
	}

	@Override
	public String getNome() {
		return Testi.formatta("missioni", "CacciaAiGoblin.t1");
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return Testi.formatta("missioni", "CacciaAiGoblin.t2", getNomeCitta());
		}
		return Testi.formatta("missioni", "CacciaAiGoblin.t3", getNomeCitta(), GOBLIN_DA_SCONFIGGERE, getConteggioNelPassoCorrente(eventoSconfitto(TipoPersonaggio.GOBLIN)));
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
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "CacciaAiGoblin.s1", getNomeCitta()))))
				.poi(RITORNO);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "CacciaAiGoblin.1"))
				.parlaIlMandante(Testi.formatta("missioni", "CacciaAiGoblin.2", GOBLIN_DA_SCONFIGGERE, getRicompensa()))
				.parlaIlCapo(Testi.formatta("missioni", "CacciaAiGoblin.3"));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "CacciaAiGoblin.4"))
				.parlaIlCapo(Testi.formatta("missioni", "CacciaAiGoblin.5"));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "CacciaAiGoblin.t4", getRicompensa(), GOBLIN_DA_SCONFIGGERE);
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "CacciaAiGoblin.t5", getRicompensa());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
