package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.ClasseMissione;
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
		super(ClasseMissione.IL_RAPIMENTO);
	}

	/**
	 * Il nome dell'ostaggio, da missioni.txt.
	 */
	public String getOstaggio() {
		return parametro(OSTAGGIO, ProduttoreDiTestiCasuale::nomeOstaggio);
	}

	/**
	 * Il nome del capo dei rapitori, da missioni.txt.
	 */
	public String getCapobanda() {
		return parametro(CAPOBANDA, ProduttoreDiTestiCasuale::nomeCapobanda);
	}

	/**
	 * La banda di goblin dei rapitori, con il loro capo.
	 */
	public IncontroDiMissione getRapitori() {
		return IncontroDiMissione.di(TipoPersonaggio.GOBLIN, RAPITORI).conCapo(getCapobanda());
	}

	@Override
	protected void allIncarico() {
		getOstaggio();
		getCapobanda();
	}

	@Override
	public String getNome() {
		return "Il rapimento di " + getOstaggio();
	}

	@Override
	public String getDescrizione() {
		String passo = getPassoCorrente();
		if (VIAGGIO.equals(passo)) {
			return getOstaggio() + " è libero ma debole: riportalo vivo a " + getNomeCitta() + ", e proteggilo per strada.";
		}
		if (RITORNO.equals(passo)) {
			return getOstaggio() + " è a casa: sua moglie ti aspetta a " + getNomeCitta() + ".";
		}
		if (LUTTO.equals(passo) || FALLIMENTO.equals(passo)) {
			return getOstaggio() + " è morto: devi dare la notizia a sua moglie, a " + getNomeCitta() + ".";
		}
		return "La banda di goblin di " + getCapobanda() + " tiene prigioniero " + getOstaggio()
				+ " in una grotta segnata sulla mappa. Liberalo e riportalo a " + getNomeCitta() + ".";
	}

	@Override
	public String getRicordoDellaLocazione() {
		return "In questa grotta i goblin di " + getCapobanda() + " tenevano prigioniero " + getOstaggio() + ".";
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
		return "La grotta dove i goblin tengono prigioniero " + getOstaggio() + " è segnata sulla mappa.";
	}

	@Override
	protected String testoLiberato() {
		return getOstaggio() + " è libero, ma è debole e non sa difendersi: riportatelo vivo a " + getNomeCitta() + ".";
	}

	@Override
	protected String testoMorto() {
		return getOstaggio() + " non ce l'ha fatta: i suoi rapitori avevano degli amici. Bisogna dirlo a sua moglie, a "
				+ getNomeCitta() + ".";
	}

	@Override
	protected String testoPortaChiusa() {
		return "La moglie di " + getOstaggio() + " chiude la porta senza dire una parola.";
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Vi prego, aiutatemi! Una banda di goblin ha rapito mio marito " + getOstaggio() + ".")
				.parlaIlMandante("Lo tengono in una grotta qui vicino, e il loro capo, " + getCapobanda()
						+ ", chiede un riscatto che non posso pagare.")
				.parlaIlCapo("Il riscatto glielo portiamo noi, a modo nostro.")
				.parlaIlMandante("Riportatemelo vivo e avrete " + RICOMPENSA + " monete.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante(getOstaggio() + "! Sei tornato!")
				.parlaIlMandante("Non so come ringraziarvi. Ecco le " + RICOMPENSA + " monete.")
				.parlaIlCapo("Tenetelo d'occhio, la prossima volta.");
	}

	@Override
	protected ScenaInCitta scenaDelLutto() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Siete tornati! Ma... dov'è " + getOstaggio() + "?")
				.parlaIlCapo("Mi dispiace. Lo avevamo liberato, ma per strada ci hanno attaccati.")
				.parlaIlMandante("No... Non voglio sentire altro. Andate via.");
	}

	@Override
	protected String testoAccettazione() {
		return "La moglie di " + getOstaggio() + " pagherà " + RICOMPENSA + " monete per riaverlo a casa vivo.";
	}

	@Override
	protected String testoRicompensa() {
		return "La moglie di " + getOstaggio() + " paga le " + RICOMPENSA + " monete promesse.";
	}

	@Override
	protected int getRicompensa() {
		return RICOMPENSA;
	}
}
