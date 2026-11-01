package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.IncontroDiMissione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.testi.Testi;

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
	public String getNome() {
		return Testi.formatta("missioni", "IlRapimento.t1", getOstaggio());
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (VIAGGIO.equals(passo)) {
			return Testi.formatta("missioni", "IlRapimento.t2", getOstaggio(), getNomeCitta());
		}
		if (RITORNO.equals(passo)) {
			return Testi.formatta("missioni", "IlRapimento.t3", getOstaggio(), getNomeCitta());
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return Testi.formatta("missioni", "IlRapimento.t4", getOstaggio(), getNomeCitta());
		}
		return Testi.formatta("missioni", "IlRapimento.t5", getCapobanda(), getOstaggio(), getNomeCitta());
	}

	@Override
	public String getRicordoDellaLocazione() {
		return Testi.formatta("missioni", "IlRapimento.t6", getCapobanda(), getOstaggio());
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
		return Testi.formatta("missioni", "IlRapimento.t7", getOstaggio());
	}

	@Override
	protected String testoLiberato() {
		return Testi.formatta("missioni", "IlRapimento.t8", getOstaggio(), getNomeCitta());
	}

	@Override
	protected String testoMorto() {
		return Testi.formatta("missioni", "IlRapimento.t9", getOstaggio(), getNomeCitta());
	}

	@Override
	protected String testoPortaChiusa() {
		return Testi.formatta("missioni", "IlRapimento.t10", getOstaggio());
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "IlRapimento.1", getOstaggio()))
				.parlaIlMandante(Testi.formatta("missioni", "IlRapimento.2", getCapobanda()))
				.parlaIlCapo(Testi.formatta("missioni", "IlRapimento.3"))
				.parlaIlMandante(Testi.formatta("missioni", "IlRapimento.4", getRicompensa()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "IlRapimento.5", getOstaggio()))
				.parlaIlMandante(Testi.formatta("missioni", "IlRapimento.6", getRicompensa()))
				.parlaIlCapo(Testi.formatta("missioni", "IlRapimento.7"));
	}

	@Override
	protected ScenaInCitta scenaDelLutto() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(Testi.formatta("missioni", "IlRapimento.8", getOstaggio()))
				.parlaIlCapo(Testi.formatta("missioni", "IlRapimento.9"))
				.parlaIlMandante(Testi.formatta("missioni", "IlRapimento.10"));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "IlRapimento.t11", getOstaggio(), getRicompensa());
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "IlRapimento.t12", getOstaggio(), getRicompensa());
	}

	@Override
	protected int getRicompensaBase() {
		return RICOMPENSA;
	}
}
