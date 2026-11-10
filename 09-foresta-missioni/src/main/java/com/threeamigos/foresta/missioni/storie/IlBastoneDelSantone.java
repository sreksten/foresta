package com.threeamigos.foresta.missioni.storie;

import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il Bastone del Santone Brontolone (vedi {@link RecuperoDelSacerdote}). Un santone eremita si lamentava di tutto, e un giorno il suo
 * bastone, che non ne poteva più, se n'è andato da solo; ora sta in certe rovine, dove un fantasma lo usa come bastone da passeggio, ma
 * anche lui brontola e non lo lascia infestare in pace. Il gruppo affronta il fantasma (uno solo: il premio serve a chi usa il bastone) e
 * torna dal sacerdote:
 * il santone ha mandato a dire che non lo rivuole, e il bastone resta al gruppo.
 * <ol>
 * <li>INCARICO e ACCETTAZIONE (vedi RecuperoDelSacerdote): le rovine sulla mappa;</li>
 * <li>SCONTRO, a fine locazione, nelle rovine, sconfitto il fantasma;</li>
 * <li>RITORNO, in locazione, dove il sacerdote ha raccontato la storia: il bastone passa al gruppo.</li>
 * </ol>
 */
public class IlBastoneDelSantone extends RecuperoDelSacerdote {

	/**
	 * La chiave del bastone in leggendari.txt
	 */
	public static final String CHIAVE_DEL_BASTONE = "BASTONE_DEL_SANTONE";
	// Un fantasma solo, con il suo nome
	private static final int FANTASMI = 1;
	private static final String SCONTRO = "SCONTRO";
	private static final String RITORNO = "RITORNO";
	private static final String CAPO = "CAPO";

	public IlBastoneDelSantone() {
		super(MissionePredefinita.IL_BASTONE_DEL_SANTONE);
	}

	@Override
	protected String prefisso() {
		return "IL_BASTONE_DEL_SANTONE";
	}

	@Override
	protected String chiaveDelLeggendario() {
		return CHIAVE_DEL_BASTONE;
	}

	@Override
	protected String chiParlaNellaScena() {
		return "SSCSSC";
	}

	@Override
	protected TipoLocazione tipoDelPosto() {
		return TipoLocazione.ROVINE;
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
	 * Il fantasma delle rovine, con il nome che gli ha dato la missione.
	 */
	public String getNomeDelCapo() {
		return parametro(CAPO, () -> ProduttoreDiMissioniCasuale.testoDellaMissione("IL_BASTONE_DEL_SANTONE_NOME_DEL_CAPO"));
	}

	/**
	 * Il fantasma che si è preso il bastone.
	 */
	public IncontroDiMissione getIncontro() {
		return IncontroDiMissione.di(TipoPersonaggio.FANTASMA, FANTASMI).conCapo(getNomeDelCapo());
	}

	@Override
	protected Passo costruisciGliAltriPassi(String id) {
		switch (id) {
			case SCONTRO:
				return combattiIlCapo(this::getPostoDellaStoria, getIncontro())
						.esegui(() -> pubblica(testo("_SCONFITTI", "PARAM_CAPO", getNomeDelCapo())))
						.poi(RITORNO);
			case RITORNO:
				return tornaAlPuntoDiPartenza(MomentoControllo.IN_LOCAZIONE)
						.esegui(this::consegnaIlLeggendario)
						.poi(Passo.FINE);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private static void pubblica(String testo) {
		com.threeamigos.foresta.eventi.BusEventi.pubblica(new com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo(testo));
	}
}
