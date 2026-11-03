package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;

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
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		return RITORNO.equals(passo) ? "CACCIA_AI_GOBLIN_DESCRIZIONE_RITORNO" : "CACCIA_AI_GOBLIN_DESCRIZIONE_CACCIA";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return new Object[] {"PARAM_CITTA", getNomeCitta(), "PARAM_GOBLIN", GOBLIN_DA_SCONFIGGERE};
	}

	@Override
	protected Object[] valoriDellaDescrizione() {
		return new Object[] {"PARAM_SCONFITTI", getConteggioNelPassoCorrente(eventoSconfitto(TipoPersonaggio.GOBLIN))};
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
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_FINE_CACCIA", "PARAM_CITTA", getNomeCitta()))))
				.poi(RITORNO);
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_INCARICO_1"))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_INCARICO_2", "PARAM_GOBLIN", GOBLIN_DA_SCONFIGGERE, "PARAM_MONETE", getRicompensa()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_INCARICO_3"));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_RINGRAZIAMENTO_1"))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_RINGRAZIAMENTO_2"));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_ACCETTAZIONE", "PARAM_MONETE", getRicompensa(), "PARAM_GOBLIN", GOBLIN_DA_SCONFIGGERE);
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIA_AI_GOBLIN_RICOMPENSA", "PARAM_MONETE", getRicompensa());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
