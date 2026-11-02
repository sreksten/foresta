package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.tipi.CostantiMissioni;
import com.threeamigos.foresta.tipi.Mandante;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.intermezzi.ScenaInCitta;
import com.threeamigos.foresta.missioni.*;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.richieste.ForgiaRichiesta;
import com.threeamigos.foresta.missioni.richieste.MaterialeRichiesto;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Testi;

/**
 * L'armaiolo di una città vuole forgiare un'arma molto migliore delle altre, e chiede al gruppo il necessario: un metallo da
 * raccogliere in certi luoghi, un trofeo da prendere a certi mostri (vedi {@link MaterialeRichiesto}) e le monete per il lavoro. Le
 * pesca da missioni.txt quando l'incarico si offre (FORGIA_METALLO, FORGIA_TROFEO, FORGIA), e l'arma è una di quelle che almeno
 * un personaggio del gruppo può impugnare (vedi {@link ForgiaRichiesta}).
 * <ol>
 * <li>RACCOLTA_METALLO e RACCOLTA_TROFEO, dopo ogni locazione: si raccolgono i due materiali;</li>
 * <li>FORGIATURA, in locazione, nella città: consuma i materiali, le monete e le ore di lavoro (vedi {@link Costruzione}); se
 * mancano le monete aspetta che ci siano;</li>
 * <li>SALUTO, in locazione: le ultime parole dell'armaiolo, che chiede di tornare più tardi;</li>
 * <li>RAFFREDDAMENTO, dopo ogni locazione: devono passare delle ore di gioco perché la lama si raffreddi;</li>
 * <li>poi i passi di ogni incarico in città (vedi {@link IncaricoInCitta}): RITORNO nella città e RICOMPENSA, che qui non sono monete
 * ma l'arma forgiata.</li>
 * </ol>
 */
public class LaForgia extends IncaricoInCitta {

	private static final String RACCOLTA_METALLO = "RACCOLTA_METALLO";
	private static final String RACCOLTA_TROFEO = "RACCOLTA_TROFEO";
	private static final String FORGIATURA = "FORGIATURA";
	private static final String SALUTO = "SALUTO";
	private static final String RAFFREDDAMENTO = "RAFFREDDAMENTO";

	/**
	 * Le chiavi sotto cui la missione conta i materiali raccolti (e con cui li fissa come parametri).
	 */
	public static final String METALLO = "METALLO";
	public static final String TROFEO = "TROFEO";
	private static final String QUANTITA_METALLO = "QUANTITA_METALLO";
	private static final String QUANTITA_TROFEO = "QUANTITA_TROFEO";
	private static final String ARMA = "ARMA";
	private static final int TENTATIVI_PER_L_ARMA = 12;

	private static final Mandante MANDANTE = Mandante.ARMAIOLO;

	public LaForgia() {
		super(MissionePredefinita.LA_FORGIA);
	}

	public MaterialeRichiesto getMetallo() {
		return MaterialeRichiesto.da(parametro(METALLO, () -> ProduttoreDiMissioniCasuale.materialeRichiesto("FORGIA_METALLO")));
	}

	public MaterialeRichiesto getTrofeo() {
		return MaterialeRichiesto.da(parametro(TROFEO, () -> ProduttoreDiMissioniCasuale.materialeRichiesto("FORGIA_TROFEO")));
	}

	public int getQuantitaDelMetallo() {
		MaterialeRichiesto metallo = getMetallo();
		return Integer.parseInt(parametro(QUANTITA_METALLO,
				() -> String.valueOf(Dado.tira(metallo.getQuantitaMinima(), metallo.getQuantitaMassima()))));
	}

	public int getQuantitaDelTrofeo() {
		MaterialeRichiesto trofeo = getTrofeo();
		return Integer.parseInt(parametro(QUANTITA_TROFEO,
				() -> String.valueOf(Dado.tira(trofeo.getQuantitaMinima(), trofeo.getQuantitaMassima()))));
	}

	/**
	 * L'arma che si forgia, scelta quando l'incarico si offre e poi sempre la stessa.
	 */
	public ForgiaRichiesta getArma() {
		return ForgiaRichiesta.da(parametro(ARMA, this::scegliLaRigaDellArma));
	}

	/**
	 * Quante monete costa il lavoro: tante per ogni livello del mondo di quando l'incarico si è offerto, così la cifra
	 * promessa è quella che si paga.
	 */
	public int getCosto() {
		return CostantiMissioni.FORGIA_MONETE_PER_LIVELLO * getLivelloDellIncarico();
	}

	/**
	 * Una riga di FORGIA a caso fra quelle che almeno un personaggio del gruppo può impugnare, se dopo qualche tentativo se ne
	 * trova una; altrimenti l'ultima pescata.
	 */
	private String scegliLaRigaDellArma() {
		String riga = null;
		for (int i = 0; i < TENTATIVI_PER_L_ARMA; i++) {
			riga = ProduttoreDiMissioniCasuale.rigaDiMissioni("FORGIA");
			Artefatto arma = ForgiaRichiesta.da(riga).leggendario(getLivelloDellIncarico()).costruisci(Statistiche.getLivello());
			if (!GruppoGiocatore.getIstanza().chiPuoEquipaggiare(arma).isEmpty()) {
				return riga;
			}
		}
		return riga;
	}

	private ForgiaRichiesta armaSeScelta() {
		String riga = getParametro(ARMA);
		return riga == null ? null : ForgiaRichiesta.da(riga);
	}

	@Override
	protected void allIncarico() {
		getMetallo();
		getQuantitaDelMetallo();
		getTrofeo();
		getQuantitaDelTrofeo();
		getArma();
	}

	@Override
	public String getNome() {
		ForgiaRichiesta arma = armaSeScelta();
		return Lingue.corrente().elenco(java.util.Arrays.asList(Lingue.corrente().inizialeMaiuscola(MANDANTE.getNome()), arma == null ? Testi.formatta("missioni", "LaForgia.laForgia") : arma.getNomeBreve()));
	}

	@Override
	public String getDescrizione() {
		ForgiaRichiesta arma = armaSeScelta();
		if (arma == null) {
			return Testi.formatta("missioni", "LaForgia.t1", Lingue.corrente().inizialeMaiuscola(MANDANTE.getNome()));
		}
		String armaiolo = Lingue.corrente().inizialeMaiuscola(mandanteDiCitta(MANDANTE.getNome()));
		MaterialeRichiesto metallo = getMetallo();
		MaterialeRichiesto trofeo = getTrofeo();
		switch (getPassoCorrente()) {
			case RACCOLTA_TROFEO:
				return Testi.formatta("missioni", "LaForgia.t2", armaiolo, arma.getNomeBreve(), trofeo.quanti(getQuantitaDelTrofeo()), trofeo.getDaDoveViene(), getContatore(TROFEO));
			case FORGIATURA:
				return Testi.formatta("missioni", "LaForgia.t3", metallo.getPluraleConArticolo(), trofeo.getPluraleConArticolo(), Lingue.corrente().conPreposizione(Preposizione.A, mandanteDiCittaSenzaVirgolaFinale(MANDANTE.getNome())), getCosto());
			case SALUTO:
			case RAFFREDDAMENTO:
				return Testi.formatta("missioni", "LaForgia.t4", arma.getNomeBreve(), Lingue.corrente().conPreposizione(Preposizione.PRESSO, mandanteDiCitta(MANDANTE.getNome())));
			case RITORNO:
				return Testi.formatta("missioni", "LaForgia.t5", arma.getNomeBreve(), Lingue.corrente().conPreposizione(Preposizione.DA, mandanteDiCittaSenzaVirgolaFinale(MANDANTE.getNome())));
			default:
				return Testi.formatta("missioni", "LaForgia.t6", armaiolo, arma.getNomeBreve(), metallo.quanti(getQuantitaDelMetallo()), metallo.getDaDoveViene(), getContatore(METALLO));
		}
	}

	@Override
	protected String primoPassoDelCompito() {
		return RACCOLTA_METALLO;
	}

	@Override
	protected Passo costruisciPassoDelCompito(String id) {
		MaterialeRichiesto metallo = getMetallo();
		MaterialeRichiesto trofeo = getTrofeo();
		OggettiDaRaccogliere metalli = metallo.daRaccogliere(METALLO, getQuantitaDelMetallo());
		OggettiDaRaccogliere trofei = trofeo.daRaccogliere(TROFEO, getQuantitaDelTrofeo());
		switch (id) {
			case RACCOLTA_METALLO:
				return raccogli(MomentoControllo.POST_LOCAZIONE, metalli)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "LaForgia.pronti",
								Lingue.corrente().inizialeMaiuscola(metallo.getPluraleConArticolo()), metallo.getTutti(), trofeo.quanti(getQuantitaDelTrofeo())))))
						.poi(RACCOLTA_TROFEO);
			case RACCOLTA_TROFEO:
				return raccogli(MomentoControllo.POST_LOCAZIONE, trofei)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("missioni", "LaForgia.s1", mandanteDiCitta(MANDANTE.getNome())))))
						.poi(FORGIATURA);
			case FORGIATURA:
				return costruisci(MomentoControllo.IN_LOCAZIONE, this::nellaCitta,
						Costruzione.con(metalli, trofei).conMonete(getCosto()).inOre(CostantiMissioni.FORGIA_ORE_DI_LAVORO), this::testoForgiatura)
						.segnala(this::coordinateDellaCittaDelRitorno)
						.poi(SALUTO);
			case SALUTO:
				return dialogo(MomentoControllo.IN_LOCAZIONE, this::testoSaluto).poi(RAFFREDDAMENTO);
			case RAFFREDDAMENTO:
				return attendiOre(MomentoControllo.POST_LOCAZIONE, CostantiMissioni.FORGIA_ORE_DI_ATTESA)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(
								Testi.formatta("missioni", "LaForgia.s2", mandanteDiCitta(MANDANTE.getNome())))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private String testoForgiatura() {
		return Testi.formatta("missioni", "LaForgia.t7", Lingue.corrente().inizialeMaiuscola(mandante(MANDANTE.getNome())), Lingue.corrente().cardinale(CostantiMissioni.FORGIA_ORE_DI_LAVORO, Genere.MASCHILE), getCosto());
	}

	private String testoSaluto() {
		return Testi.formatta("missioni", "LaForgia.t8", Lingue.corrente().inizialeMaiuscola(mandante(MANDANTE.getNome())), Lingue.corrente().inizialeMaiuscola(getArma().getNomeBreve()));
	}

	@Override
	protected Ricompensa getRicompensaFinale() {
		return Ricompensa.nulla().conArtefatto(() -> getArma().leggendario(getLivelloDellIncarico()).costruisci(Statistiche.getLivello()));
	}

	@Override
	protected ScenaInCitta scenaIncarico() {
		ForgiaRichiesta arma = getArma();
		MaterialeRichiesto metallo = getMetallo();
		MaterialeRichiesto trofeo = getTrofeo();
		return MANDANTE.nuovaScena()
				.parlaIlMandante(MANDANTE.getApertura(nomeDelMandante(MANDANTE.getNome())))
				.parlaIlMandante(arma.getRichiesta())
				.parlaIlMandante(Testi.formatta("missioni", "LaForgia.1", metallo.quanti(getQuantitaDelMetallo()), metallo.getDaDoveViene(), trofeo.quanti(getQuantitaDelTrofeo()), trofeo.getDaDoveViene(), getCosto()))
				.parlaIlCapo(arma.getBattutaDelCapo())
				.parlaIlMandante(arma.getRispostaDelMandante())
				.parlaIlCapo(Testi.formatta("missioni", "LaForgia.2"));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return MANDANTE.nuovaScena()
				.parlaIlMandante(getArma().getConsegna())
				.parlaIlCapo(Testi.formatta("missioni", "LaForgia.3"));
	}

	@Override
	protected String testoAccettazione() {
		return Testi.formatta("missioni", "LaForgia.t9", Lingue.corrente().inizialeMaiuscola(mandante(MANDANTE.getNome())), getArma().getNomeBreve(), getMetallo().quanti(getQuantitaDelMetallo()), getTrofeo().quanti(getQuantitaDelTrofeo()), getCosto());
	}

	@Override
	protected String testoRicompensa() {
		return Testi.formatta("missioni", "LaForgia.t10", Lingue.corrente().inizialeMaiuscola(mandante(MANDANTE.getNome())), getArma().getNomeBreve());
	}

	/**
	 * Nessuna paga in monete: la ricompensa è l'arma (vedi {@link #getRicompensaFinale()}), e le monete le mette il gruppo.
	 */
	@Override
	protected int getRicompensaBase() {
		return 0;
	}
}
