package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * In città una donna chiede di liberare suo marito, rapito da una banda di goblin che lo tiene in una grotta (vedi
 * {@link LaLiberazione}): sconfitta la banda, l'ostaggio va riportato vivo in città. Se muore per strada, bisogna
 * tornare a dare la notizia a sua moglie, e la missione fallisce.
 * <p>
 * I nomi dell'ostaggio e del capobanda vengono da missioni.txt, pescati quando l'incarico si offre: la missione si
 * ripete con altri nomi.
 */
public class IlRapimento extends LaLiberazione {

	private static final String OSTAGGIO = "OSTAGGIO";
	private static final String CAPOBANDA = "CAPOBANDA";
	public static final int RAPITORI = 4;
	private static final int RICOMPENSA = 35;

	public IlRapimento() {
		super(MissionePredefinita.IL_RAPIMENTO);
	}

	/**
	 * Il nome dell'ostaggio, da missioni.txt.
	 */
	public String getOstaggio() {
		return parametro(OSTAGGIO, ProduttoreDiMissioniCasuale::nomeOstaggio);
	}

	/**
	 * Il nome del capo dei rapitori, da missioni.txt.
	 */
	public String getCapobanda() {
		return parametro(CAPOBANDA, ProduttoreDiMissioniCasuale::nomeCapobanda);
	}

	/**
	 * La banda di goblin dei rapitori, con un hobgoblin a capo.
	 */
	public IncontroDiMissione getRapitori() {
		return IncontroDiMissione.di(TipoPersonaggio.GOBLIN, RAPITORI).conCapo(getCapobanda(), TipoPersonaggio.HOBGOBLIN);
	}

	@Override
	protected void allIncarico() {
		getOstaggio();
		getCapobanda();
	}

	@Override
	protected String creaIlNome() {
		return testo("IL_RAPIMENTO_TITOLO");
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		if (VIAGGIO.equals(passo)) {
			return "IL_RAPIMENTO_DESCRIZIONE_VIAGGIO";
		}
		if (RITORNO.equals(passo)) {
			return "IL_RAPIMENTO_DESCRIZIONE_RITORNO";
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return "IL_RAPIMENTO_DESCRIZIONE_LUTTO";
		}
		return "IL_RAPIMENTO_DESCRIZIONE_LIBERAZIONE";
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	/**
	 * Il testo di quella produzione di missioni.txt, con l'ostaggio, il capo dei rapitori, la città e le monete.
	 */
	private String testo(String produzione) {
		return ProduttoreDiMissioniCasuale.testoDellaMissione(produzione, valori());
	}

	/**
	 * Tutti i segnaposto dei testi della missione. Senza "private" per i test.
	 */
	Object[] valori() {
		return new Object[] {"PARAM_OSTAGGIO", getOstaggio(), "PARAM_CAPOBANDA", getCapobanda(), "PARAM_CITTA", getNomeCitta(), "PARAM_MONETE", getRicompensa()};
	}

	@Override
	public String getRicordoDellaLocazione() {
		return testo("IL_RAPIMENTO_RICORDO");
	}

	@Override
	protected String getPersona() {
		return getOstaggio();
	}

	@Override
	protected TipoLocazione getLuogoDelCovo() {
		return TipoLocazione.GROTTA;
	}

	@Override
	protected IncontroDiMissione getNemiciDelCovo() {
		return getRapitori();
	}

	@Override
	protected String testoCovoSegnato() {
		return testo("IL_RAPIMENTO_COVO_SEGNATO");
	}

	@Override
	protected String testoLiberato() {
		return testo("IL_RAPIMENTO_LIBERATO");
	}

	@Override
	protected String testoMorto() {
		return testo("IL_RAPIMENTO_MORTO");
	}

	@Override
	protected String testoPortaChiusa() {
		return testo("IL_RAPIMENTO_PORTA_CHIUSA");
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(testo("IL_RAPIMENTO_INCARICO_1"))
				.parlaIlMandante(testo("IL_RAPIMENTO_INCARICO_2"))
				.parlaIlCapo(testo("IL_RAPIMENTO_INCARICO_3"))
				.parlaIlMandante(testo("IL_RAPIMENTO_INCARICO_4"));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(testo("IL_RAPIMENTO_RINGRAZIAMENTO_1"))
				.parlaIlMandante(testo("IL_RAPIMENTO_RINGRAZIAMENTO_2"))
				.parlaIlCapo(testo("IL_RAPIMENTO_RINGRAZIAMENTO_3"));
	}

	@Override
	protected ScenaInCitta scenaDelLutto() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(testo("IL_RAPIMENTO_LUTTO_1"))
				.parlaIlCapo(testo("IL_RAPIMENTO_LUTTO_2"))
				.parlaIlMandante(testo("IL_RAPIMENTO_LUTTO_3"));
	}

	@Override
	protected String testoAccettazione() {
		return testo("IL_RAPIMENTO_ACCETTAZIONE");
	}

	@Override
	protected String testoRicompensa() {
		return testo("IL_RAPIMENTO_RICOMPENSA");
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
