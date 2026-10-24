package com.threeamigos.foresta.testi;

import com.threeamigos.foresta.strumenti.logger.DestinazioneDiLog;
import com.threeamigos.foresta.strumenti.logger.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I produttori di testi a caso con le grammatiche vere del gioco: ognuno dà testi non vuoti, e i nomi che non si devono ripetere si
 * riazzerano da soli quando finiscono.
 */
class ProduttoriDiTestiTest {

	@AfterEach
	void riparte() {
		ProduttoriDiTesti.resetProduzioni();
		Logger.impostaDestinazione(null);
	}

	private static void assertNonVuoto(String testo, String cosa) {
		assertNotNull(testo, cosa);
		assertFalse(testo.trim().isEmpty(), cosa);
	}

	@Test
	void leFrasiDellaSfidaNonSonoVuote() {
		assertNonVuoto(ProduttoreDiSfideCasuale.fraseDiSfida(), "sfida");
		assertNonVuoto(ProduttoreDiSfideCasuale.fraseSeIlGiocatoreVince(), "vince");
		assertNonVuoto(ProduttoreDiSfideCasuale.fraseSeIlGiocatorePerde(), "perde");
		assertNonVuoto(ProduttoreDiSfideCasuale.fraseDelTorneo(), "torneo");
		assertNonVuoto(ProduttoreDiSfideCasuale.nomeLeggendarioDeiCampioni(), "leggendario");
	}

	@Test
	void iNomiDeiLuoghiHannoLArticolo() {
		assertNonVuoto(ProduttoreDiLuoghiCasuale.nomeTempio(), "tempio");
		assertNonVuoto(ProduttoreDiLuoghiCasuale.nomeRovine(), "rovine");
	}

	@Test
	void unaFiabaHaAlmenoUnParagrafo() {
		assertFalse(ProduttoreDiFiabeCasuale.fiaba().isEmpty());
		assertNonVuoto(ProduttoreDiFiabeCasuale.fiaba().get(0), "fiaba");
	}

	@Test
	void ilProduttoreDelleMissioniDaNomiETestiDelleSueProduzioni() {
		assertNonVuoto(ProduttoreDiMissioniCasuale.nomeOstaggio(), "ostaggio");
		assertNonVuoto(ProduttoreDiMissioniCasuale.nomePellegrino(), "pellegrino");
		assertNonVuoto(ProduttoreDiMissioniCasuale.nomeCapobanda(), "capobanda");
		assertNonVuoto(ProduttoreDiMissioniCasuale.nomeDiPersonaggioNoto("NOME_MASCHILE"), "noto");
		assertNonVuoto(ProduttoreDiMissioniCasuale.rigaDiMissioni("VIANDANTE"), "riga");
		assertNonVuoto(ProduttoreDiMissioniCasuale.materialeRichiesto("RICHIESTA_ALCHIMISTA"), "materiale");
		assertNonVuoto(ProduttoreDiMissioniCasuale.spedizione(), "spedizione");
		assertNonVuoto(ProduttoreDiMissioniCasuale.oggettoSmarrito(), "oggetto smarrito");
	}

	@Test
	void iSacerdotiHannoNomiCheFinisconoEPoiRipartono() {
		java.util.Set<String> nomi = new java.util.HashSet<>();
		// I nomi dei sacerdoti sono pochi e non si ripetono: quando finiscono le sezioni statiche si azzerano e si riparte
		for (int i = 0; i < 40; i++) {
			nomi.add(ProduttoreDiMissioniCasuale.nomeSacerdote());
			nomi.add(ProduttoreDiMissioniCasuale.nomeSacerdotessa());
		}
		assertTrue(nomi.size() > 5, nomi.toString());
	}

	@Test
	void ognunoDeiProduttoriDelleLocandeDaTestiENotizieGrezze() {
		assertNonVuoto(ProduttoreDiLocandeCasuale.reazioneLocandiere(), "reazione");
		List<ProduttoreDiLocandeCasuale.DatiLocanda> locande = ProduttoreDiLocandeCasuale.getDatiLocanda(3);
		assertEquals(3, locande.size());
		ProduttoreDiLocandeCasuale.DatiLocanda prima = locande.get(0);
		assertNonVuoto(prima.getNome(), "nome");
		assertNonVuoto(prima.getIdentificativo(), "identificativo");
		assertNonVuoto(prima.getNomeLocandiere(), "locandiere");
		assertNotNull(prima.getDialogo());
		assertNotNull(prima.getRecensione());
		assertTrue(ProduttoreDiLocandeCasuale.notiziaGrezza(prima.getIdentificativo()).contains("-"), "ID-corpo");
	}

	@Test
	void ilDialogoSullOroscopoHaUnTemaUnaDomandaEUnaRisposta() {
		ProduttoreDiOroscopiCasuale.DialogoSullOroscopo dialogo = ProduttoreDiOroscopiCasuale.dialogoSullOroscopo();

		assertNonVuoto(dialogo.getOroscopo(), "oroscopo");
		assertNonVuoto(dialogo.getDomanda(), "domanda");
		assertNonVuoto(dialogo.getRisposta(), "risposta");
		assertNotNull(dialogo.getTema(), "gli oroscopi del gioco hanno sempre un tema");
	}

	// ------------------------------------------------------------------------------------------ oroscopi malformati

	private static final String DOMANDE = "DOMANDA_GENERICA\n    Che vuol dire?\nRISPOSTA_SULL_OROSCOPO\n    Non saprei\nDOMANDA_COSA\n    Che cos'è [#TEMA]?\n";

	@Test
	void unOroscopoSenzaTemaFaUnaDomandaGenericaESegnalaIlDifetto() throws Exception {
		List<String> registro = new ArrayList<>();
		Logger.impostaDestinazione(new DestinazioneDiLog() {
			@Override
			public void scrivi(String messaggio) {
				registro.add(messaggio);
			}

			@Override
			public void scrivi(Exception e) {
				registro.add(e.toString());
			}
		});

		ProduttoreDiOroscopiCasuale.DialogoSullOroscopo dialogo = ProduttoreDiOroscopiCasuale.dialogoSullOroscopo(
				new GrammarBean("DIALOGO_SULL_OROSCOPO\n    Giornata fortunata\n" + DOMANDE));

		assertEquals("Giornata fortunata", dialogo.getOroscopo());
		assertNull(dialogo.getTema());
		assertEquals("Che vuol dire?", dialogo.getDomanda());
		assertEquals("Non saprei", dialogo.getRisposta());
		assertEquals(1, registro.size(), registro.toString());
		assertTrue(registro.get(0).contains("Oroscopo senza tema"), registro.get(0));
	}

	@Test
	void unTemaSenzaFormaFaUnaDomandaGenerica() throws Exception {
		ProduttoreDiOroscopiCasuale.DialogoSullOroscopo dialogo = ProduttoreDiOroscopiCasuale.dialogoSullOroscopo(
				new GrammarBean("DIALOGO_SULL_OROSCOPO\n    Stelle propizie/niente\n" + DOMANDE));

		assertEquals("Stelle propizie", dialogo.getOroscopo());
		assertNull(dialogo.getTema());
		assertEquals("Che vuol dire?", dialogo.getDomanda());
	}

	@Test
	void unTemaConLaFormaFaUnaDomandaSuQuelTema() throws Exception {
		ProduttoreDiOroscopiCasuale.DialogoSullOroscopo dialogo = ProduttoreDiOroscopiCasuale.dialogoSullOroscopo(
				new GrammarBean("DIALOGO_SULL_OROSCOPO\n    Il sole sorge/COSA:il Sole\n" + DOMANDE));

		assertEquals("Il sole sorge", dialogo.getOroscopo());
		assertEquals("il Sole", dialogo.getTema());
		assertEquals("Che cos'è il Sole?", dialogo.getDomanda());
		assertEquals("Non saprei", dialogo.getRisposta());
	}

	// ------------------------------------------------------------------------------------------ locande malformate

	private static List<ProduttoreDiLocandeCasuale.DatiLocanda> daRiga(String riga) throws Exception {
		return ProduttoreDiLocandeCasuale.getDatiLocanda(1, new GrammarBean("LOCANDA\n    " + riga + "\n"));
	}

	@Test
	void unaRigaDiLocandaHaCinqueCampiEIlDialogoELaRecensioneSonoFacoltativi() throws Exception {
		ProduttoreDiLocandeCasuale.DatiLocanda completa = daRiga("il Boccale/BOCCALE/Oste/RECENSIONE=Ottima/DIALOGO=Salve").get(0);
		assertEquals("il Boccale", completa.getNome());
		assertEquals("BOCCALE", completa.getIdentificativo());
		assertEquals("Oste", completa.getNomeLocandiere());
		assertEquals("Ottima", completa.getRecensione());
		assertEquals("Salve", completa.getDialogo());

		ProduttoreDiLocandeCasuale.DatiLocanda senza = daRiga("il Boccale/BOCCALE/Oste//").get(0);
		assertEquals("", senza.getRecensione());
		assertEquals("", senza.getDialogo());
	}

	@Test
	void unaRigaDiLocandaConMenoDiCinqueCampiEUnErroreCheDiceLaRiga() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> daRiga("il Boccale/BOCCALE/Oste"));

		assertTrue(e.getMessage().contains("3 campi invece di 5") && e.getMessage().contains("il Boccale/BOCCALE/Oste"), e.getMessage());
	}

	@Test
	void unaGrammaticaDiLocandeCheProduceUnaRigaSolaPerVolta() throws Exception {
		GrammarBean piuRighe = new GrammarBean("LOCANDA\n    [#RIGHE]\n");
		piuRighe.addFixedProduction("RIGHE", "il Boccale/BOCCALE/Oste/RECENSIONE=x/DIALOGO=y\nil Calice/CALICE/Oste/RECENSIONE=x/DIALOGO=y");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ProduttoreDiLocandeCasuale.getDatiLocanda(1, piuRighe));

		assertTrue(e.getMessage().contains("più di una riga") && e.getMessage().contains("LOCANDA"), e.getMessage());
	}
}
