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
import com.threeamigos.foresta.testi.Testi;

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
	public String getNome() {
		return Testi.formatta("missioni", "CacciatoreDiTaglie.nome", getRicercato());
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return Testi.formatta("missioni", "CacciatoreDiTaglie.t1", getRicercato(), getNomeCitta());
		}
		String direzione = ottieniProprieta(DIREZIONE);
		return direzione == null ? Testi.formatta("missioni", "CacciatoreDiTaglie.descrizione", getRicercato())
				: Testi.formatta("missioni", "CacciatoreDiTaglie.descrizioneConDirezione", getRicercato(), direzione.replaceAll("\\.$", ""), getNomeCitta());
	}

	@Override
	public String getRicordoDellaLocazione() {
		return Testi.formatta("missioni", "CacciatoreDiTaglie.t2", getRicercato());
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
							BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "CacciatoreDiTaglie.s1", getRicercato(), direzione.replaceAll("\\.$", ""))));
						})
						.poi(CACCIA);
			case CACCIA:
				return combattiIlCapo(this::getCovo, getBanda())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "CacciatoreDiTaglie.s2", getRicercato(), getNomeCitta()))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(Testi.formatta("missioni", "CacciatoreDiTaglie.1", getRicercato()))
				.parlaIlMandante(Testi.formatta("missioni", "CacciatoreDiTaglie.2"))
				.parlaIlCapo(Testi.formatta("missioni", "CacciatoreDiTaglie.3"))
				.parlaIlMandante(Testi.formatta("missioni", "CacciatoreDiTaglie.4", getRicompensa()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conCapitano()
				.parlaIlMandante(Testi.formatta("missioni", "CacciatoreDiTaglie.5", getRicercato()))
				.parlaIlMandante(Testi.formatta("missioni", "CacciatoreDiTaglie.6"))
				.parlaIlCapo(Testi.formatta("missioni", "CacciatoreDiTaglie.7"));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "CacciatoreDiTaglie.t3", getRicercato(), getRicompensa());
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "CacciatoreDiTaglie.t4", getRicompensa(), getRicercato());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
