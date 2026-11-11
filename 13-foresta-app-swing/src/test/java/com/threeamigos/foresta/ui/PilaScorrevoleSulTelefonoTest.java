package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.motore.PartitaDiAnteprima;
import com.threeamigos.foresta.motore.StatoDelMotore;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.BersaglioDelSuggerimento;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.Situazione;
import com.threeamigos.foresta.tipi.TipoDiLayout;
import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tutorial.RegistroSuggerimenti;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.riquadri.RiquadroLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Sul telefono la pila dei riquadri scorre a destra e lascia il suo spazio alla locazione: i riquadri, la freccia e il mouse si spostano con lei, la
 * locazione si allarga e i personaggi si allargano con lei, il testo resta com'è; un suggerimento del tutorial che indica la pila la riapre.
 */
class PilaScorrevoleSulTelefonoTest {

	private static final int LARGHEZZA = 842;
	private static final int ALTEZZA = 505;

	private DisplayableCanvas canvas;
	private SchermoDelGioco<?, ?, ?, ?> schermo;

	@BeforeEach
	void creaIlTelefono() {
		// Le immagini si caricano solo con uno schermo
		assumeFalse(GraphicsEnvironment.isHeadless());
		BusEventi.azzera();
		BusEventi.impostaConsegna(Runnable::run);
		RegistroSuggerimenti.ripristina();
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		StatoDelMotore.registraComponenti();
		ImageCache.init();
		PartitaDiAnteprima.prepara(TipoPersonaggio.GUERRIERO, "Aldric", TipoPersonaggio.BARDO, TipoPersonaggio.MAGO);
		canvas = new DisplayableCanvas(TipoDiLayout.TELEFONO, Orientamento.VERTICALE, LARGHEZZA, ALTEZZA, true, new GestorePunteggiInMemoria(),
				new VistaPartitaMotore());
		canvas.setSize(LARGHEZZA, ALTEZZA);
		schermo = canvas.getSchermo();
		schermo.iniziaGioco();
	}

	@AfterEach
	void ripristina() {
		RegistroSuggerimenti.ripristina();
		RegistroSuggerimenti.setMostrabile(false);
		BusEventi.azzera();
		BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
	}

	private void disegna() {
		BufferedImage immagine = new BufferedImage(LARGHEZZA, ALTEZZA, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		canvas.paint(g);
		g.dispose();
	}

	/** La pila arriva in fondo, e la locazione lo sa al fotogramma dopo */
	private void finisceDiScorrere() {
		schermo.getPilaScorrevole().avanza(1);
		disegna();
	}

	private int xDelPrimoDelGruppo() {
		RiquadroLocazione locazione = (RiquadroLocazione) schermo.finestra(TipoFinestra.GRAFICA);
		VistaPersonaggio primo = new VistaPartitaMotore().getGruppoGiocatore().getPersonaggiVivi().get(0);
		return Math.round(locazione.spriteDellaSalute(primo, 5).getXIniziale());
	}

	@Test
	void compattataLaPilaSiSpostaELaLocazioneSiAllargaMaIlTestoNo() {
		disegna();
		DisposizioneDelloSchermo disposizione = schermo.getDisposizione();
		PilaScorrevole pila = schermo.getPilaScorrevole();
		assertNotNull(pila);
		Area testo = schermo.getArea(schermo.finestra(TipoFinestra.TESTO));
		int larghezzaDellaLocazione = schermo.getArea(schermo.finestra(TipoFinestra.GRAFICA)).getLarghezza();
		assertTrue(schermo.isFrecciaDellaPilaVersoDestra());
		Area frecciaEspansa = schermo.getAreaDellaFrecciaDellaPila();

		pila.commuta();
		finisceDiScorrere();

		int scostamento = pila.getScostamentoMassimo();
		assertEquals(disposizione.getGruppo().getLarghezza() - scostamento,
				(int) Math.round(disposizione.getGruppo().getLarghezza() * PilaScorrevole.PARTE_VISIBILE), "se ne vede il 20%");
		for (TipoFinestra tipo : schermo.riquadriImpilati()) {
			assertEquals(disposizione.getColonnaDestraX() + scostamento, schermo.getArea(schermo.finestra(tipo)).getX(), tipo.name());
		}
		assertEquals(scostamento, schermo.getAreaDellaFrecciaDellaPila().getX() - frecciaEspansa.getX(), "la freccia va con la pila");
		assertFalse(schermo.isFrecciaDellaPilaVersoDestra(), "e adesso dice che la si può riaprire");
		assertEquals(larghezzaDellaLocazione + scostamento, schermo.getArea(schermo.finestra(TipoFinestra.GRAFICA)).getLarghezza());
		Area testoDopo = schermo.getArea(schermo.finestra(TipoFinestra.TESTO));
		assertEquals(testo.getX(), testoDopo.getX());
		assertEquals(testo.getLarghezza(), testoDopo.getLarghezza());
	}

	@Test
	void iPersonaggiDelGruppoSiAllarganoVersoDestraEScivolanoConLaPila() {
		schermo.assegnaCoordinateAPersonaggi();
		disegna();
		int espansa = xDelPrimoDelGruppo();
		PilaScorrevole pila = schermo.getPilaScorrevole();
		pila.commuta();
		pila.avanza(PilaScorrevole.SECONDI_DELLO_SCORRIMENTO / 2);
		disegna();
		int aMeta = xDelPrimoDelGruppo();
		finisceDiScorrere();
		int compattata = xDelPrimoDelGruppo();
		assertTrue(compattata > espansa, espansa + " → " + compattata);
		assertTrue(aMeta > espansa && aMeta < compattata, "a metà strada sta in mezzo: " + espansa + " " + aMeta + " " + compattata);
	}

	@Test
	void unSuggerimentoCheIndicaUnRiquadroDellaPilaLaRiapre() {
		PilaScorrevole pila = schermo.getPilaScorrevole();
		pila.commuta();
		finisceDiScorrere();
		// Il suggerimento del movimento, alla pagina che indica la mappa
		schermo.getTutor().aggiorna(EnumSet.of(Situazione.ICONE_DI_MOVIMENTO_PRESENTI), s -> true);
		schermo.pollicePremuto();
		assertEquals(BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA, schermo.getTutor().getFumetto().get().getBersaglio());
		disegna();
		assertTrue(pila.isVersoEspansa());
		finisceDiScorrere();
		assertEquals(0, pila.getScostamento());
		assertEquals(schermo.getDisposizione().getColonnaDestraX(),
				schermo.getAreaDelBersaglio(BersaglioDelSuggerimento.RIQUADRO_DELLA_MAPPA).getX(), "il bordo è attorno alla mappa riaperta");
	}

	@Test
	void sulTabletLaPilaNonScorre() {
		DisplayableCanvas tablet = new DisplayableCanvas(Orientamento.ORIZZONTALE, 1024, 768, false, new GestorePunteggiInMemoria(),
				new VistaPartitaMotore());
		assertNull(tablet.getSchermo().getPilaScorrevole());
		assertEquals(0, tablet.getSchermo().getScostamentoDellaPila());
		tablet.getSchermo().iniziaGioco();
		assertNull(tablet.getSchermo().getAreaDellaFrecciaDellaPila());
	}
}
