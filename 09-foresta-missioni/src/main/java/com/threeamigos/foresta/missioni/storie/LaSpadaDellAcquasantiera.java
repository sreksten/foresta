package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La Spada dell'Acquasantiera Traboccante (vedi {@link RecuperoDelSacerdote}). Don Arcangelo, per fare prima, ha benedetto la spada del
 * campanaro immergendola nell'acquasantiera, e da allora la spada non smette di sgocciolare: l'ha data a un ambulante perché la portasse
 * lontano, ma una banda di goblin gliel'ha rubata scambiandola per una fontana, e ora in una grotta ci fa il bucato. Il gruppo
 * affronta i goblin e il loro capo, un hobgoblin; la spada resta al gruppo.
 * <ol>
 * <li>INCARICO e ACCETTAZIONE (vedi RecuperoDelSacerdote): la grotta sulla mappa;</li>
 * <li>SCONTRO, a fine locazione, nella grotta, sconfitto il capo: la spada passa al gruppo.</li>
 * </ol>
 */
public class LaSpadaDellAcquasantiera extends RecuperoDelSacerdote {

	/**
	 * La chiave della spada in leggendari.txt
	 */
	public static final String CHIAVE_DELLA_SPADA = "SPADA_DELL_ACQUASANTIERA";
	/**
	 * I goblin della lavanderia, intorno al loro capo
	 */
	public static final int GOBLIN = 4;
	private static final String SCONTRO = "SCONTRO";
	private static final String CAPO = "CAPO";

	public LaSpadaDellAcquasantiera() {
		super(MissionePredefinita.LA_SPADA_DELL_ACQUASANTIERA);
	}

	@Override
	protected String prefisso() {
		return "LA_SPADA_DELL_ACQUASANTIERA";
	}

	@Override
	protected String chiaveDelLeggendario() {
		return CHIAVE_DELLA_SPADA;
	}

	@Override
	protected String chiParlaNellaScena() {
		return "SSCSSC";
	}

	@Override
	protected TipoLocazione tipoDelPosto() {
		return TipoLocazione.GROTTA;
	}

	@Override
	protected String passoDopoLAccettazione() {
		return SCONTRO;
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo("_RICORDO", "PARAM_CAPO", getNomeDelCapo());
	}

	/**
	 * Il capo dei goblin lavandai, con il nome che gli ha dato la missione.
	 */
	public String getNomeDelCapo() {
		return parametro(CAPO, () -> ProduttoreDiMissioniCasuale.testoDellaMissione("LA_SPADA_DELL_ACQUASANTIERA_NOME_DEL_CAPO"));
	}

	/**
	 * I goblin e il loro capo, un hobgoblin, al centro.
	 */
	public IncontroDiMissione getIncontro() {
		return IncontroDiMissione.di(TipoPersonaggio.GOBLIN, GOBLIN).conCapoAlCentro(getNomeDelCapo(), TipoPersonaggio.HOBGOBLIN);
	}

	@Override
	protected Passo costruisciGliAltriPassi(String id) {
		if (SCONTRO.equals(id)) {
			return combattiIlCapo(this::getPostoDellaStoria, getIncontro())
					.esegui(this::consegnaIlLeggendario)
					.poi(Passo.FINE);
		}
		throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
	}
}
