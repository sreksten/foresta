package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tools.Misc;

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
		super(ClasseMissione.CACCIATORE_DI_TAGLIE);
	}

	/**
	 * Il nome del ricercato, da missioni.txt.
	 */
	public String getRicercato() {
		return parametro(RICERCATO, ProduttoreDiTestiCasuale::nomeCapobanda);
	}

	/**
	 * La banda di goblin, con il ricercato, un hobgoblin, a capo.
	 */
	public IncontroDiMissione getBanda() {
		return IncontroDiMissione.di(ClassePersonaggio.GOBLIN, GOBLIN).conCapo(getRicercato(), ClassePersonaggio.HOBGOBLIN);
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
		return "Ricercato: " + getRicercato();
	}

	@Override
	public String getDescrizione() {
		if (RITORNO.equals(getPassoCorrente())) {
			return getRicercato() + " è stato abbattuto: torna a " + getNomeCitta() + " a riscuotere la taglia.";
		}
		String direzione = ottieniProprieta(DIREZIONE);
		return "C'è una taglia su " + getRicercato() + ", l'hobgoblin a capo di una banda di goblin. Si nasconde fra delle rovine"
				+ (direzione == null ? "" : ", " + direzione + " da " + getNomeCitta()) + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "Fra queste rovine si nascondeva la banda di " + getRicercato() + ".";
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
				return cercaLocazione(MomentoControllo.IN_LOCAZIONE, ClassiLocazione.ROVINE)
						.esegui(() -> {
							String direzione = Misc.getDirezione(GruppoGiocatore.getIstanza(), getCovo());
							aggiungiProprieta(DIREZIONE, direzione);
							BusEventi.pubblica(new NotificaTestoParagrafo("Dicono che la banda di " + getRicercato()
									+ " si nasconda fra delle rovine, " + direzione + "."));
						})
						.poi(CACCIA);
			case CACCIA:
				return combattiIlCapo(this::getCovo, getBanda())
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(getRicercato()
								+ " è stato abbattuto: la taglia aspetta a " + getNomeCitta() + ".")))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Cercate lavoro? Leggete qui: ricercato, vivo o morto, " + getRicercato() + ".")
				.parlaIlMandante("È un hobgoblin, il capo di una banda di goblin che assalta chiunque passi. Si nasconde fra certe rovine.")
				.parlaIlCapo("Dove, esattamente?")
				.parlaIlMandante("Se lo sapessi lo avrei già preso io. Portatemi la prova che è morto e avrete " + RICOMPENSA + " monete.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(getRicercato() + " non darà più fastidio a nessuno? Ottimo lavoro.")
				.parlaIlMandante("Ecco la taglia.")
				.parlaIlCapo("Se ne avete altre, sapete dove trovarci.");
	}

	@Override
	protected String testoAccettazione() {
		return "La taglia su " + getRicercato() + " vale " + RICOMPENSA + " monete.";
	}

	@Override
	protected String testoRicompensa() {
		return "La taglia di " + RICOMPENSA + " monete su " + getRicercato() + " è vostra.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
