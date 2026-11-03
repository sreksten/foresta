package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.mappa.Direzioni;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il cacciatore di taglie: in città c'è un manifesto con una taglia sul capo di una banda di goblin, un hobgoblin. La
 * banda si nasconde fra delle rovine che la missione rivendica, ma che non segna sulla mappa: il mandante sa dire
 * solo in che direzione si trovano, e il gruppo le deve cercare. Lì c'è la banda con il suo capo (vedi
 * {@link MissioneAPassi#combattiIlCapo}): basta abbattere lui, poi si torna in città a riscuotere (vedi
 * IncaricoInCitta). Il nome del capo viene da missioni.txt: la missione si ripete con altri ricercati.
 */
public class CacciatoreDiTaglie extends IncaricoInCitta {

	private static final String RICERCATO = "RICERCATO";
	private static final String DIREZIONE = "DIREZIONE";
	public static final int GOBLIN = 3;
	private static final int RICOMPENSA = 30;
	private static final String COVO = "COVO";
	private static final String CACCIA = "CACCIA";

	public CacciatoreDiTaglie() {
		super(MissionePredefinita.CACCIATORE_DI_TAGLIE);
	}

	/**
	 * Il nome del ricercato, da missioni.txt.
	 */
	public String getRicercato() {
		return parametro(RICERCATO, ProduttoreDiMissioniCasuale::nomeCapobanda);
	}

	/**
	 * La banda di goblin, con il ricercato, un hobgoblin, a capo.
	 */
	public IncontroDiMissione getBanda() {
		return IncontroDiMissione.di(TipoPersonaggio.GOBLIN, GOBLIN).conCapo(getRicercato(), TipoPersonaggio.HOBGOBLIN);
	}

	/**
	 * Le rovine dove si nasconde la banda, o null finché la missione non le ha trovate.
	 */
	public CoordinateMD getCovo() {
		return RegistroMissioni.getLocazioneOccupata(this);
	}

	@Override
	protected void allIncarico() {
		getRicercato();
	}

	@Override
	protected String creaIlNome() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_TITOLO", valori());
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		if (RITORNO.equals(passo)) {
			return "CACCIATORE_DI_TAGLIE_DESCRIZIONE_RITORNO";
		}
		return ottieniProprieta(DIREZIONE) == null ? "CACCIATORE_DI_TAGLIE_DESCRIZIONE_COVO" : "CACCIATORE_DI_TAGLIE_DESCRIZIONE_CACCIA";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Tutti i segnaposto dei testi della missione: il ricercato, la città, le monete e, dopo aver trovato le rovine, la loro direzione.
	 */
	private Object[] valori() {
		String direzione = ottieniProprieta(DIREZIONE);
		return new Object[] {"PARAM_RICERCATO", getRicercato(), "PARAM_CITTA", getNomeCitta(), "PARAM_MONETE", getRicompensa(),
				"PARAM_DIREZIONE", direzione == null ? "" : direzione.replaceAll("\\.$", "")};
	}

	@Override
	public String getRicordoDellaLocazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_RICORDO", valori());
	}

	@Override
	protected String primoPassoDelCompito() {
		return COVO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		switch (id) {
			case COVO:
				// Le rovine non si segnano sulla mappa: il gruppo sa solo da che parte cercarle
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, TipoLocazione.ROVINE)
						.esegui(() -> {
							String direzione = Direzioni.getDirezione(Foresta.getPosizioneDelGruppo(), getCovo());
							aggiungiProprieta(DIREZIONE, direzione);
							BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_COVO_TROVATO", "PARAM_RICERCATO", getRicercato(), "PARAM_DIREZIONE", direzione.replaceAll("\\.$", ""))));
						})
						.poi(CACCIA);
			case CACCIA:
				return combattiIlCapo(this::getCovo, getBanda())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_ABBATTUTO", valori()))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_INCARICO_1", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_INCARICO_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_INCARICO_3", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_INCARICO_4", valori()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_RINGRAZIAMENTO_1", valori()))
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_RINGRAZIAMENTO_2", valori()))
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_RINGRAZIAMENTO_3", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("CACCIATORE_DI_TAGLIE_RICOMPENSA", valori());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
