package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tools.Misc;

/**
 * La caccia ai trofei: in città il capitano delle guardie vuole che il gruppo sfoltisca un tipo di mostro, e ne
 * chiede i trofei come prova (orecchie di goblin, corni di minotauro...). I trofei li portano i mostri stessi (vedi
 * {@link OggettiDaRaccogliere#daiNemici}): si prendono sconfiggendoli, dovunque. Al ritorno si consegnano e si
 * riscuote. Il trofeo e la quantità si pescano quando l'incarico si offre: la missione si ripete con altri mostri.
 */
public class CacciaAiTrofei extends IncaricoInCitta {

	/**
	 * La chiave sotto cui la missione conta i trofei raccolti.
	 */
	public static final String TROFEI = "TROFEI";
	private static final String TROFEO = "TROFEO";
	private static final String QUANTITA = "QUANTITA";
	public static final int QUANTITA_MINIMA = 3;
	public static final int QUANTITA_MASSIMA = 4;
	private static final String CACCIA = "CACCIA";

	public CacciaAiTrofei() {
		super(ClasseMissione.CACCIA_AI_TROFEI);
	}

	public TrofeoDiCaccia getTrofeo() {
		TrofeoDiCaccia[] trofei = TrofeoDiCaccia.values();
		return TrofeoDiCaccia.valueOf(parametro(TROFEO, () -> trofei[Dado.tiraAncheAUnaFaccia(trofei.length) - 1].name()));
	}

	public int getQuantita() {
		return Integer.parseInt(parametro(QUANTITA, () -> String.valueOf(Dado.tira(QUANTITA_MINIMA, QUANTITA_MASSIMA))));
	}

	/**
	 * I trofei da portare: quanti, e a quali mostri si prendono.
	 */
	public OggettiDaRaccogliere getTrofei() {
		TrofeoDiCaccia trofeo = getTrofeo();
		return OggettiDaRaccogliere.di(TROFEI, trofeo.getNome(), getQuantita())
				.daiNemici(trofeo.getNemico())
				.alPiuPerLocazione(QUANTITA_MASSIMA);
	}

	private String quanti() {
		TrofeoDiCaccia trofeo = getTrofeo();
		int quantita = getQuantita();
		return (trofeo.isFemminile() ? Misc.getCardinaleF(quantita) : Misc.getCardinaleM(quantita)) + " " + trofeo.getNome().getPlurale();
	}

	@Override
	protected void allIncarico() {
		getTrofeo();
		getQuantita();
	}

	@Override
	public String getNome() {
		return "Prove di caccia: " + getTrofeo().getNome().getPlurale();
	}

	@Override
	public String getDescrizione() {
		TrofeoDiCaccia trofeo = getTrofeo();
		if (RITORNO.equals(getPassoCorrente())) {
			return "Hai i trofei: porta " + trofeo.getNome().getADP() + trofeo.getNome().getPlurale() + " al capitano delle guardie di "
					+ getNomeCitta() + ".";
		}
		return "Il capitano delle guardie di " + getNomeCitta() + " vuole " + quanti() + " come prova che hai sfoltito "
				+ trofeo.getNemici() + ". Finora: " + getContatore(TROFEI) + ".";
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
		return raccogli(MomentoControllo.POST_LOCAZIONE, getTrofei())
				.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo("I trofei ci sono tutti: il capitano delle guardie di "
						+ getNomeCitta() + " vi aspetta.")))
				.poi(RITORNO);
	}

	@Override
	protected OggettiDaRaccogliere getOggettiDaConsegnare() {
		return getTrofei();
	}

	@Override
	protected String testoConsegna() {
		TrofeoDiCaccia trofeo = getTrofeo();
		return "Il capitano delle guardie conta " + trofeo.getNome().getADP() + quanti()
				+ (trofeo.isFemminile() ? ", una per una." : ", uno per uno.");
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		TrofeoDiCaccia trofeo = getTrofeo();
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Voi avete l'aria di chi sa usare una spada. Sono il capitano delle guardie.")
				.parlaIlMandante("Bisogna sfoltire " + trofeo.getNemici() + ". Portatemi " + quanti()
						+ " come prova, e avrete " + getRicompensa() + " monete.")
				.parlaIlCapo("Prove? Non vi fidate di noi?")
				.parlaIlMandante("Mi fido delle prove.");
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return ScenaInCitta.conMandante()
				.parlaIlMandante("Siete tornati. Fatemi vedere.")
				.parlaIlCapo("Ecco qui. Ve " + (getTrofeo().isFemminile() ? "le" : "li") + " incartiamo?");
	}

	@Override
	protected String testoAccettazione() {
		return "Il capitano delle guardie pagherà " + getRicompensa() + " monete per " + quanti() + ".";
	}

	@Override
	protected String testoRicompensa() {
		return "Il capitano delle guardie paga le " + getRicompensa() + " monete promesse.";
	}

	/**
	 * Sei monete per trofeo, più cinque.
	 */
	@Override
	protected int getRicompensa() {
		return 6 * getQuantita() + 5;
	}
}
