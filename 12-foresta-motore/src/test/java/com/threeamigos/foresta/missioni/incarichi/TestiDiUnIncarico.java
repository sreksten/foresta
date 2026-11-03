package com.threeamigos.foresta.missioni.incarichi;

import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;

import java.util.ArrayList;
import java.util.List;

/**
 * Per i test: i testi di un incarico in città che le sottoclassi tengono protetti (le scene e le frasi che il motore
 * mostra nei vari momenti), così si possono controllare senza guidare l'automa.
 */
public final class TestiDiUnIncarico {

	private TestiDiUnIncarico() {
	}

	/** Le battute della scena in cui il mandante offre l'incarico, in ordine. */
	public static List<String> sceneDellIncarico(IncaricoInCitta incarico) {
		return battute(incarico.scenaIncarico().getPagine());
	}

	/** Le battute della scena in cui il mandante ringrazia, in ordine. */
	public static List<String> sceneDelRingraziamento(IncaricoInCitta incarico) {
		return battute(incarico.scenaRingraziamento().getPagine());
	}

	/** Le monete dell'accordo, quelle che i testi dicono. */
	public static int monete(IncaricoInCitta incarico) {
		return incarico.getRicompensa();
	}

	public static String accettazione(IncaricoInCitta incarico) {
		return incarico.testoAccettazione();
	}

	public static String consegna(IncaricoInCitta incarico) {
		return incarico.testoConsegna();
	}

	public static String ricompensa(IncaricoInCitta incarico) {
		return incarico.testoRicompensa();
	}

	/**
	 * I testi della sorveglianza che il giocatore legge durante la missione e che non passano dalle scene: la descrizione nel diario di
	 * ogni passo (con una visita già fatta e con nemici, se ce ne sono), il posto segnato, la veglia, l'attesa del mandante.
	 */
	public static List<String> testiDuranteLaSorveglianza(LaSorveglianza sorveglianza) {
		Object[] valori = sorveglianza.valori();
		List<String> testi = new ArrayList<>();
		for (String passo : new String[] {"POSTO", "AGGUATO", IncaricoInCitta.RITORNO}) {
			if ("AGGUATO".equals(passo) && sorveglianza.getNemici() == null) {
				continue;
			}
			testi.add(ProduttoreDiMissioniCasuale.riempi(ProduttoreDiMissioniCasuale.modelloDellaMissione(sorveglianza.produzioneDellaDescrizione(passo)), valori));
		}
		testi.add(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_SORVEGLIANZA_DESCRIZIONE_IN_CORSO", conMancanti(valori, "ancora due volte")));
		testi.add(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_SORVEGLIANZA_VEGLIA", conVeglia(valori)));
		testi.add(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_SORVEGLIANZA_POSTO_SEGNATO"));
		testi.add(ProduttoreDiMissioniCasuale.testoDellaMissione("LA_SORVEGLIANZA_ATTESA", valori));
		return testi;
	}

	/**
	 * I testi del soccorso che non passano dalle scene dell'incarico e del ringraziamento: la scena del lutto, il covo segnato, il
	 * liberato, il morto, la porta chiusa e la descrizione nel diario di ogni passo.
	 */
	public static List<String> testiDuranteIlSoccorso(IlSoccorso soccorso) {
		List<String> testi = new ArrayList<>(battute(soccorso.scenaDelLutto().getPagine()));
		testi.addAll(java.util.Arrays.asList(soccorso.testoCovoSegnato(), soccorso.testoLiberato(), soccorso.testoMorto(), soccorso.testoPortaChiusa()));
		for (String passo : new String[] {"COVO", "VIAGGIO", IncaricoInCitta.RITORNO, "LUTTO"}) {
			testi.add(ProduttoreDiMissioniCasuale.riempi(ProduttoreDiMissioniCasuale.modelloDellaMissione(soccorso.produzioneDellaDescrizione(passo)), soccorso.valori()));
		}
		return testi;
	}

	/**
	 * Come {@link #testiDuranteIlSoccorso}, per il rapimento.
	 */
	public static List<String> testiDuranteIlRapimento(IlRapimento rapimento) {
		List<String> testi = new ArrayList<>(battute(rapimento.scenaDelLutto().getPagine()));
		testi.addAll(java.util.Arrays.asList(rapimento.testoCovoSegnato(), rapimento.testoLiberato(), rapimento.testoMorto(), rapimento.testoPortaChiusa(), rapimento.getRicordoDellaLocazione()));
		for (String passo : new String[] {"COVO", "VIAGGIO", IncaricoInCitta.RITORNO, "LUTTO"}) {
			testi.add(ProduttoreDiMissioniCasuale.riempi(ProduttoreDiMissioniCasuale.modelloDellaMissione(rapimento.produzioneDellaDescrizione(passo)), rapimento.valori()));
		}
		return testi;
	}

	/**
	 * Per una missione portata alla grammatica, senza un aiuto per classe: la descrizione nel diario di ciascuno dei passi dati (il
	 * modello della produzione di quel passo riempito dei valori stabili e di quelli che cambiano) e il testo di ciascuno dei metodi
	 * senza argomenti dati (per esempio "testoCovoSegnato"), chiamati anche se protetti o privati.
	 */
	public static List<String> testiDuranteLaMissione(Object incarico, String[] passi, String... metodi) {
		List<String> testi = new ArrayList<>();
		try {
			for (String passo : passi) {
				String produzione = (String) chiama(incarico, "produzioneDellaDescrizione", new Class<?>[] {String.class}, passo);
				Object[] stabili = (Object[]) chiama(incarico, "valoriStabiliDellaDescrizione", new Class<?>[0]);
				Object[] mobili = (Object[]) chiama(incarico, "valoriDellaDescrizione", new Class<?>[0]);
				Object[] tutti = java.util.Arrays.copyOf(stabili, stabili.length + mobili.length);
				System.arraycopy(mobili, 0, tutti, stabili.length, mobili.length);
				testi.add(ProduttoreDiMissioniCasuale.riempi(ProduttoreDiMissioniCasuale.modelloDellaMissione(produzione), tutti));
			}
			for (String metodo : metodi) {
				Object risultato = chiama(incarico, metodo, new Class<?>[0]);
				if (risultato instanceof com.threeamigos.foresta.intermezzi.ScenaInCitta) {
					testi.addAll(battute(((com.threeamigos.foresta.intermezzi.ScenaInCitta) risultato).getPagine()));
				} else {
					testi.add((String) risultato);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
		return testi;
	}

	/**
	 * I testi della missione figlia "chiave del custode" (che non è un incarico in città): per un custode che combatte e per uno che
	 * vuole un oggetto, la descrizione di ogni passo e ogni frase fissa, riempite con i valori della missione.
	 */
	public static List<String> testiDellaChiaveDelCustode() {
		com.threeamigos.foresta.modellodati.CoordinateMD posto = new com.threeamigos.foresta.modellodati.CoordinateMD(3, 4);
		ChiaveDelCustode combatte = ChiaveDelCustode.chePerCombattere(posto, "la Torre del Sole", com.threeamigos.foresta.tipi.TipoPersonaggio.GOBLIN, 3,
				null, "la chiave d'argento", "Un custode ti sbarra la strada.");
		ChiaveDelCustode scambia = ChiaveDelCustode.cheVuoleUnOggetto(posto, "la Torre del Sole", null, "la chiave d'argento", "Un custode ti sbarra la strada.",
				"il calice rubato", "Il custode rivuole il suo calice.", new com.threeamigos.foresta.modellodati.CoordinateMD(5, 6), "la Grotta Nera",
				com.threeamigos.foresta.tipi.TipoPersonaggio.GOBLIN, 2);
		List<String> testi = new ArrayList<>();
		try {
			for (ChiaveDelCustode missione : new ChiaveDelCustode[] {combatte, scambia}) {
				testi.addAll(testiDuranteLaMissione(missione, missione == combatte ? new String[] {"CUSTODE"} : new String[] {"CHIEDE", "OGGETTO", "CONSEGNA"}));
				Object[] valori = (Object[]) chiama(missione, "valori", new Class<?>[0]);
				for (String produzione : missione == combatte ? new String[] {"VINTO"} : new String[] {"VINTO", "OGGETTO_CHIESTO", "OGGETTO_RECUPERATO", "SCAMBIO"}) {
					testi.add(ProduttoreDiMissioniCasuale.testoDellaMissione("CHIAVE_DEL_CUSTODE_" + produzione, valori));
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
		return testi;
	}

	private static Object chiama(Object oggetto, String nome, Class<?>[] tipi, Object... argomenti) throws ReflectiveOperationException {
		for (Class<?> classe = oggetto.getClass(); classe != null; classe = classe.getSuperclass()) {
			try {
				java.lang.reflect.Method metodo = classe.getDeclaredMethod(nome, tipi);
				metodo.setAccessible(true);
				return metodo.invoke(oggetto, argomenti);
			} catch (NoSuchMethodException e) {
				// si cerca nella classe sopra
			}
		}
		throw new NoSuchMethodException(oggetto.getClass().getSimpleName() + "." + nome);
	}

	private static Object[] conMancanti(Object[] valori, String mancanti) {
		Object[] tutti = java.util.Arrays.copyOf(valori, valori.length + 2);
		tutti[valori.length] = "PARAM_MANCANTI";
		tutti[valori.length + 1] = mancanti;
		return tutti;
	}

	private static Object[] conVeglia(Object[] valori) {
		Object[] tutti = java.util.Arrays.copyOf(conMancanti(valori, "ancora una volta"), valori.length + 4);
		tutti[valori.length + 2] = "PARAM_VEGLIA";
		tutti[valori.length + 3] = "Nessuno è passato.";
		return tutti;
	}

	private static List<String> battute(List<PaginaIntermezzo> pagine) {
		List<String> testi = new ArrayList<>();
		for (PaginaIntermezzo pagina : pagine) {
			if (pagina.getTesto() != null && !pagina.getTesto().isEmpty()) {
				testi.add(pagina.getTesto());
			}
			for (BattutaProgrammata programmata : pagina.getBattuteProgrammate()) {
				testi.add(programmata.getBattuta().getTesto());
			}
		}
		return testi;
	}
}
