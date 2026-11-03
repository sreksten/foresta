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
import com.threeamigos.foresta.testi.RegoleDellaLingua;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.testi.Genere;

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
		return Lingue.corrente().elenco(java.util.Arrays.asList(Lingue.corrente().inizialeMaiuscola(MANDANTE.getNome()), arma == null ? ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_LA_FORGIA") : arma.getNomeBreve()));
	}

	@Override
	public String getDescrizione() {
		return descrizioneDalModello();
	}

	@Override
	protected String produzioneDellaDescrizione(String passo) {
		if (armaSeScelta() == null) {
			return "LA_FORGIA_DESCRIZIONE_ATTESA";
		}
		switch (passo) {
			case RACCOLTA_TROFEO:
				return "LA_FORGIA_DESCRIZIONE_TROFEO";
			case FORGIATURA:
				return "LA_FORGIA_DESCRIZIONE_FORGIATURA";
			case SALUTO:
			case RAFFREDDAMENTO:
				return "LA_FORGIA_DESCRIZIONE_RAFFREDDAMENTO";
			case RITORNO:
				return "LA_FORGIA_DESCRIZIONE_RITORNO";
			default:
				return "LA_FORGIA_DESCRIZIONE_METALLO";
		}
	}

	@Override
	protected Object[] valoriStabiliDellaDescrizione() {
		return valori();
	}

	@Override
	protected Object[] valoriDellaDescrizione() {
		return armaSeScelta() == null ? new Object[0] : new Object[] {"PARAM_RACCOLTI_METALLO", getContatore(METALLO), "PARAM_RACCOLTI_TROFEO", getContatore(TROFEO)};
	}

	/**
	 * Tutti i segnaposto dei testi della missione che non cambiano mentre dura: l'armaiolo nelle sue forme, l'arma, il metallo e il
	 * trofeo, il costo e le ore di lavoro. Finché l'arma non è scelta c'è solo l'armaiolo (scegliere l'arma, e il resto, è compito di allIncarico).
	 */
	private Object[] valori() {
		RegoleDellaLingua lingua = Lingue.corrente();
		String armaiolo = MANDANTE.getNome();
		Object[] soloArmaiolo = {"PARAM_ARMAIOLO_MAIUSC", lingua.inizialeMaiuscola(mandante(armaiolo))};
		ForgiaRichiesta arma = armaSeScelta();
		if (arma == null) {
			return soloArmaiolo;
		}
		MaterialeRichiesto metallo = getMetallo();
		MaterialeRichiesto trofeo = getTrofeo();
		return new Object[] {
				"PARAM_ARMAIOLO_MAIUSC", lingua.inizialeMaiuscola(mandante(armaiolo)),
				"PARAM_ARMAIOLO_DI_CITTA", mandanteDiCitta(armaiolo),
				"PARAM_ARMAIOLO_DI_CITTA_MAIUSC", lingua.inizialeMaiuscola(mandanteDiCitta(armaiolo)),
				"PARAM_PRESSO_ARMAIOLO_DI_CITTA", lingua.conPreposizione(Preposizione.PRESSO, mandanteDiCitta(armaiolo)),
				"PARAM_A_ARMAIOLO_DI_CITTA_SENZA_VIRGOLA", lingua.conPreposizione(Preposizione.A, mandanteDiCittaSenzaVirgolaFinale(armaiolo)),
				"PARAM_DA_ARMAIOLO_DI_CITTA_SENZA_VIRGOLA", lingua.conPreposizione(Preposizione.DA, mandanteDiCittaSenzaVirgolaFinale(armaiolo)),
				"PARAM_ARMA", arma.getNomeBreve(),
				"PARAM_ARMA_MAIUSC", lingua.inizialeMaiuscola(arma.getNomeBreve()),
				"PARAM_QUANTI_METALLO", metallo.quanti(getQuantitaDelMetallo()),
				"PARAM_DA_DOVE_METALLO", metallo.getDaDoveViene(),
				"PARAM_METALLI", metallo.getPluraleConArticolo(),
				"PARAM_METALLI_MAIUSC", lingua.inizialeMaiuscola(metallo.getPluraleConArticolo()),
				"PARAM_TUTTI_METALLO", metallo.getTutti(),
				"PARAM_QUANTI_TROFEO", trofeo.quanti(getQuantitaDelTrofeo()),
				"PARAM_DA_DOVE_TROFEO", trofeo.getDaDoveViene(),
				"PARAM_TROFEI", trofeo.getPluraleConArticolo(),
				"PARAM_COSTO", getCosto(),
				"PARAM_ORE", lingua.cardinale(CostantiMissioni.FORGIA_ORE_DI_LAVORO, Genere.MASCHILE)};
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
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_METALLO_PRONTO", valori()))))
						.poi(RACCOLTA_TROFEO);
			case RACCOLTA_TROFEO:
				return raccogli(MomentoControllo.POST_LOCAZIONE, trofei)
						.esegui(() -> BusEventi.pubblica(new NotificaTestoParagrafo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_TUTTO_PRONTO", valori()))))
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
								ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_RAFFREDDATA", valori()))))
						.poi(RITORNO);
			default:
				throw new IllegalArgumentException("Passo sconosciuto per " + getNome() + ": " + id);
		}
	}

	private String testoForgiatura() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_FORGIATURA", valori());
	}

	private String testoSaluto() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_SALUTO", valori());
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
				.parlaIlMandante(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_INCARICO_1", valori()))
				.parlaIlCapo(arma.getBattutaDelCapo())
				.parlaIlMandante(arma.getRispostaDelMandante())
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_INCARICO_2", valori()));
	}

	@Override
	protected ScenaInCitta scenaRingraziamento() {
		return MANDANTE.nuovaScena()
				.parlaIlMandante(getArma().getConsegna())
				.parlaIlCapo(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_RINGRAZIAMENTO_2", valori()));
	}

	@Override
	protected String testoAccettazione() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_ACCETTAZIONE", valori());
	}

	@Override
	protected String testoRicompensa() {
		return ProduttoreDiMissioniCasuale.testoDellaMissione("LA_FORGIA_RICOMPENSA", valori());
	}

	/**
	 * Nessuna paga in monete: la ricompensa è l'arma (vedi {@link #getRicompensaFinale()}), e le monete le mette il gruppo.
	 */
	@Override
	protected int getRicompensaBase() {
		return 0;
	}
}
