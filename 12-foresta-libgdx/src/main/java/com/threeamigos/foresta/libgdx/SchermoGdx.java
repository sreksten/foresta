package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.artefatti.VistaArtefatto;
import com.threeamigos.foresta.artefatti.VistaBancoDiLavoro;
import com.threeamigos.foresta.artefatti.VistaScambio;
import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.StatoDelloSchermo;
import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.tipi.TipoNegozio;
import com.threeamigos.foresta.tipi.TipoBottega;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.DisposizioneDelloSchermo;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Punto;
import com.threeamigos.foresta.ui.SchermoDelGioco;
import com.threeamigos.foresta.ui.barra.BarraIcone;
import com.threeamigos.foresta.ui.riquadri.*;
import com.threeamigos.foresta.ui.schermate.*;
import com.threeamigos.foresta.ui.sprite.DescrizioneAnnuncioGlobale;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteATempo;
import com.threeamigos.foresta.ui.sprite.DescrizioneSpriteEffetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.util.List;

/**
 * Lo schermo del gioco in libGDX: i riquadri, le schermate e gli sprite di libGDX dietro lo schermo comune ({@link SchermoDelGioco},
 * lo stesso di Swing), e il disegno di quel che mostra, a ogni fotogramma ({@link #disegna}). È il gemello del
 * <code>DisplayableCanvas</code> di Swing.

 */
public final class SchermoGdx extends SchermoDelGioco<SpriteGdx, SpriteAnnuncioGlobaleGdx, SpriteFumettoGdx, RivelazioneDellArtefattoGdx> {

	private static final Color NERO = new Color(0f, 0f, 0f, 1f);

	private final ImmaginiGdx immagini;
	private final TestoGdx piccolo;
	private final TestoGdx medio;
	private final TestoGrandeGdx testoGrande;
	private final NuvoleGdx nuvole;
	private final CartiglioGdx cartiglio;
	private final Camera camera;
	private final Viewport viewport;

	private final SchermataIntroOutroGdx introOutro;
	private final SchermataIntermezzoGdx intermezzo;
	private final RiquadroMappaGdx riquadroMappa;
	private final RiquadroLocazioneGdx riquadroLocazione;
	private final RiquadroStatisticheGdx riquadroStatistiche;
	private final RiquadroCombattimentoGdx riquadroCombattimento;
	private final RiquadroSfidaGdx riquadroSfida;
	private final RiquadroTestoGdx riquadroTesto;
	private final RiquadroGruppoGdx riquadroGruppo;
	private final RiquadroIncantesimiEPozioniGdx riquadroIncantesimi;
	private final RiquadroMissioniGdx riquadroMissioni;
	private final SchermataMappaGdx mappaATuttoSchermo;
	private final SchermataDiScambioArtefattiGdx inventario;
	private final SchermataDiScambioArtefattiGdx commerciante;
	private final SchermataDiScambioArtefattiGdx incantatore;
	private final SchermataDiScambioConsumabiliGdx alchimista;
	private final SchermataTrofeiGdx trofei;
	private final SchermataBestiarioGdx bestiario;
	private final SchermataTatticheGdx tattiche;
	private final BarraIconeGdx barraIcone;

	/**
	 * @param barraDock vero per la barra delle icone che si ingrandisce sotto il cursore (solo in orizzontale)
	 */
	public SchermoGdx(DisposizioneDelloSchermo disposizione, boolean barraDock, GestorePunteggi gestorePunteggi, VistaPartita vistaPartita,
					  ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio, Camera camera, Viewport viewport) {
		super(disposizione, vistaPartita);
		this.camera = camera;
		this.viewport = viewport;
		this.cartiglio = new CartiglioGdx(immagini, medio);
		this.immagini = immagini;
		this.piccolo = piccolo;
		this.medio = medio;
		this.testoGrande = new TestoGrandeGdx(immagini);
		this.nuvole = new NuvoleGdx(immagini);
		int larghezza = disposizione.getLarghezzaContenuto();
		int altezza = disposizione.getAltezzaContenuto();

		introOutro = new SchermataIntroOutroGdx(larghezza, altezza, gestorePunteggi, vistaPartita, immagini, piccolo, medio);
		intermezzo = new SchermataIntermezzoGdx(larghezza, altezza, immagini, medio, introOutro);
		riquadroMappa = new RiquadroMappaGdx(disposizione.getMappa().getX(), disposizione.getMappa().getY(), vistaPartita, immagini, nuvole);
		Area locazione = disposizione.getLocazione();
		riquadroLocazione = new RiquadroLocazioneGdx(locazione.getX(), locazione.getY(), locazione.getLarghezza(), vistaPartita, immagini);
		riquadroStatistiche = new RiquadroStatisticheGdx(disposizione.getStatistiche().getX(), disposizione.getStatistiche().getY(), vistaPartita,
				immagini, medio);
		// Centrato sul testo sotto la locazione, non sull'intero schermo
		riquadroCombattimento = new RiquadroCombattimentoGdx(disposizione.getColonnaDestraX(), altezza, disposizione.getYPreferitaDelCombattimento(),
				vistaPartita, immagini, medio);
		riquadroSfida = new RiquadroSfidaGdx(larghezza, altezza, immagini, testoGrande);
		Area testo = disposizione.getTesto();
		riquadroTesto = new RiquadroTestoGdx(testo.getX(), testo.getY(), testo.getLarghezza(), testo.getAltezza(), immagini, medio);
		riquadroGruppo = new RiquadroGruppoGdx(disposizione.getGruppo().getX(), disposizione.getGruppo().getY(), vistaPartita, immagini, medio);
		riquadroIncantesimi = new RiquadroIncantesimiEPozioniGdx(disposizione.getIncantesimi().getX(), disposizione.getIncantesimi().getY(),
				vistaPartita, immagini, medio);
		riquadroMissioni = new RiquadroMissioniGdx(disposizione.getMissioni().getX(), disposizione.getMissioni().getY(), vistaPartita, immagini,
				piccolo, medio);
		mappaATuttoSchermo = new SchermataMappaGdx(larghezza, altezza, vistaPartita, immagini, medio, nuvole);
		inventario = new SchermataDiScambioArtefattiGdx(larghezza, altezza, vistaPartita, immagini, piccolo, medio, TipoBottega.INVENTARIO);
		commerciante = new SchermataDiScambioArtefattiGdx(larghezza, altezza, vistaPartita, immagini, piccolo, medio, TipoBottega.ARMAIOLO);
		incantatore = new SchermataDiScambioArtefattiGdx(larghezza, altezza, vistaPartita, immagini, piccolo, medio, TipoBottega.INCANTATORE);
		alchimista = new SchermataDiScambioConsumabiliGdx(larghezza, altezza, vistaPartita, immagini, piccolo, medio);
		trofei = new SchermataTrofeiGdx(larghezza, altezza, vistaPartita, immagini, medio, testoGrande);
		bestiario = new SchermataBestiarioGdx(larghezza, altezza, vistaPartita, immagini, piccolo, medio);
		tattiche = new SchermataTatticheGdx(larghezza, altezza, vistaPartita, immagini, piccolo, medio);
		Area barra = disposizione.getBarra();
		barraIcone = new BarraIconeGdx(disposizione.getOrientamento(), barraDock, barra.getX(), barra.getY(), barra.getLarghezza(),
				barra.getAltezza(), vistaPartita, immagini, piccolo, medio);
		prepara();
	}

	/**
	 * Abilita sulla mappa a tutto schermo il joystick virtuale, per i dispositivi senza mouse.
	 */
	public void setJoystickVirtuale(boolean abilitato) {
		if (abilitato) {
			mappaATuttoSchermo.getModello().abilitaJoystickVirtuale();
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// Il disegno

	/**
	 * Disegna lo schermo, nello stesso ordine di Swing: la schermata corrente (nella schermata di gioco la pila dei riquadri, poi il
	 * combattimento e la sfida, gli sprite, la rivelazione e il fumetto), l'annuncio a tutto schermo, la barra delle icone e, ad aiuto
	 * acceso, i cartigli (quelli dei riquadri del gruppo e delle missioni nella schermata di gioco, poi quello della barra).
	 */
	public void disegna(Batch batch) {
		StatoDelloSchermo stato = getStato();
		switch (stato) {
			case INTRO:
				introOutro.intro(batch);
				break;
			case SELEZIONE_SLOT_DA_CARICARE:
				introOutro.selezioneSlotDaCaricare(batch);
				break;
			case MESSAGGIO:
				introOutro.scrivi(batch, true);
				break;
			case IN_GIOCO:
				inGioco(batch, camera, viewport);
				disegnaLaRivelazione(rivelazione -> rivelazione.anima(batch, camera, viewport));
				disegnaIlFumetto(fumetto -> fumetto.anima(batch, camera, viewport));
				break;
			case MAPPA:
				mappaATuttoSchermo.disegna(batch, camera, viewport);
				disegnaIlFumetto(fumetto -> fumetto.anima(batch, camera, viewport));
				break;
			case SELEZIONE_SLOT_DA_SALVARE:
				introOutro.selezioneSlotDaSalvare(batch);
				break;
			case CONFERMA_USCITA:
				introOutro.confermaUscita(batch);
				break;
			case PERSO:
				introOutro.perso(batch);
				break;
			case VINTO:
				introOutro.vinto(batch);
				break;
			case STATISTICHE:
				introOutro.statistiche(batch);
				break;
			case PUNTEGGI:
				introOutro.hiscore(batch);
				break;
			case INVENTARIO:
				inventario.disegna(batch, camera, viewport);
				disegnaIlFumetto(fumetto -> fumetto.anima(batch, camera, viewport));
				break;
			case TROFEI:
				trofei.disegna(batch, camera, viewport);
				break;
			case BESTIARIO:
				bestiario.disegna(batch, camera, viewport, testoGrande);
				break;
			case TATTICHE:
				tattiche.disegna(batch, camera, viewport);
				break;
			case COMMERCIANTE:
				commerciante.disegna(batch, camera, viewport);
				disegnaIlFumetto(fumetto -> fumetto.anima(batch, camera, viewport));
				break;
			case ALCHIMISTA:
				alchimista.disegna(batch, camera, viewport);
				disegnaIlFumetto(fumetto -> fumetto.anima(batch, camera, viewport));
				break;
			case INCANTATORE:
				incantatore.disegna(batch, camera, viewport);
				disegnaIlFumetto(fumetto -> fumetto.anima(batch, camera, viewport));
				break;
			case INTERMEZZO:
				intermezzo.disegna(batch, camera, viewport);
				disegnaIlFumetto(fumetto -> fumetto.anima(batch, camera, viewport));
				break;
			default:
				break;
		}
		disegnaLAnnuncio(annuncio -> annuncio.anima(batch, camera, viewport));
		barraIcone.disegna(batch, camera, viewport);
		if (stato == StatoDelloSchermo.IN_GIOCO) {
			disegnaAiuto(batch, riquadroGruppo.righeDellAiuto(), riquadroGruppo.getTopLeftX() + riquadroGruppo.getMouseX(),
					riquadroGruppo.getTopLeftY() + riquadroGruppo.getMouseY());
			disegnaAiuto(batch, riquadroMissioni.righeDellAiuto(), riquadroMissioni.getTopLeftX() + riquadroMissioni.getMouseX(),
					riquadroMissioni.getTopLeftY() + riquadroMissioni.getMouseY());
		}
		barraIcone.disegnaAiuto(batch, disposizione.getLarghezzaTotale(), disposizione.getAltezzaTotale());
		batch.setColor(Color.WHITE);
		fineFotogramma();
	}

	private void disegnaAiuto(Batch batch, List<String> righe, int xMouse, int yMouse) {
		if (!righe.isEmpty()) {
			cartiglio.disegnaAccantoAlMouse(batch, righe, xMouse, yMouse, disposizione.getLarghezzaTotale(), disposizione.getAltezzaTotale());
		}
	}

	private void inGioco(Batch batch, Camera camera, Viewport viewport) {
		for (TipoFinestra finestra : getPila()) {
			switch (finestra) {
				case MAPPA:
					riquadroMappa.disegna(batch, camera, viewport, disposizione.getLarghezzaContenuto(), disposizione.getAltezzaContenuto());
					break;
				case STATISTICHE:
					riquadroStatistiche.disegna(batch);
					break;
				case GRAFICA:
					boolean inCombattimento = riquadroCombattimento.isVisibile();
					riquadroLocazione.disegna(batch, inCombattimento ? riquadroCombattimento.getCombattente() : null,
							inCombattimento ? riquadroCombattimento.getAvversario() : null);
					break;
				case STATO:
					riquadroGruppo.disegna(batch, camera, viewport);
					dissolviSeCoperto(batch, finestra);
					break;
				case INCANTESIMI_E_POZIONI:
					riquadroIncantesimi.disegna(batch, camera, viewport);
					dissolviSeCoperto(batch, finestra);
					break;
				case TESTO:
					riquadroTesto.disegna(batch, camera, viewport);
					break;
				case MISSIONI:
					riquadroMissioni.disegna(batch, camera, viewport);
					dissolviSeCoperto(batch, finestra);
					break;
				default:
					// Il combattimento e la sfida si disegnano dopo tutti gli altri, così nessuno li copre
					break;
			}
		}
		if (riquadroCombattimento.isVisibile()) {
			riquadroCombattimento.disegna(batch);
		}
		riquadroSfida.disegna(batch, camera, viewport);
		perOgniSprite(sprite -> sprite.anima(batch, camera, viewport));
	}

	/**
	 * Un riquadro impilato che non è l'ultimo sfuma nel nero nelle ultime righe prima del riquadro successivo
	 */
	private void dissolviSeCoperto(Batch batch, TipoFinestra finestra) {
		int quotaSuccessivo = quotaDelSuccessivo(finestra);
		if (quotaSuccessivo < 0) {
			return;
		}
		Area area = getArea(finestra(finestra));
		int altezzaDissolvenza = getAltezzaDissolvenzaPila();
		for (int riga = 0; riga < altezzaDissolvenza; riga++) {
			NERO.a = (riga + 1) / (float) altezzaDissolvenza;
			immagini.riempi(batch, area.getX(), quotaSuccessivo - altezzaDissolvenza + riga, area.getLarghezza(), 1, NERO);
		}
		batch.setColor(Color.WHITE);
	}

	// ---------------------------------------------------------------------------------------------------------------------------
	// Quel che lo schermo comune chiede

	@Override
	protected RiquadroTesto riquadroTesto() {
		return riquadroTesto;
	}

	@Override
	protected RiquadroGruppo riquadroGruppo() {
		return riquadroGruppo;
	}

	@Override
	protected RiquadroIncantesimiEPozioni riquadroIncantesimiEPozioni() {
		return riquadroIncantesimi;
	}

	@Override
	protected RiquadroMissioni riquadroMissioni() {
		return riquadroMissioni;
	}

	@Override
	protected RiquadroLocazione riquadroLocazione() {
		return riquadroLocazione;
	}

	@Override
	protected RiquadroCombattimento riquadroCombattimento() {
		return riquadroCombattimento;
	}

	@Override
	protected RiquadroSfida riquadroSfida() {
		return riquadroSfida;
	}

	@Override
	protected Finestra riquadroMappa() {
		return riquadroMappa;
	}

	@Override
	protected Finestra riquadroStatistiche() {
		return riquadroStatistiche;
	}

	@Override
	protected BarraIcone barraIcone() {
		return barraIcone.getModello();
	}

	@Override
	protected SchermateIntroOutro introOutro() {
		return introOutro;
	}

	@Override
	protected SchermataIntermezzo intermezzo() {
		return intermezzo;
	}

	@Override
	protected SchermataMappa mappaATuttoSchermo() {
		return mappaATuttoSchermo;
	}

	@Override
	protected SchermataDiScambio schermataInventario() {
		return inventario;
	}

	@Override
	protected SchermataDiScambio schermataCommerciante() {
		return commerciante;
	}

	@Override
	protected SchermataDiScambio schermataAlchimista() {
		return alchimista;
	}

	@Override
	protected SchermataDiScambio schermataIncantatore() {
		return incantatore;
	}

	@Override
	protected SchermataTattiche schermataTattiche() {
		return tattiche;
	}

	@Override
	protected SchermataTrofei schermataTrofei() {
		return trofei;
	}

	@Override
	protected SchermataBestiario schermataBestiario() {
		return bestiario;
	}

	@Override
	protected void scambioDellInventario(VistaScambio scambio, VistaPersonaggio personaggio) {
		inventario.impostaScambio(scambio);
		inventario.impostaPersonaggio(personaggio);
	}

	@Override
	protected void scambioDelCommerciante(TipoNegozio negozio, VistaScambio scambio) {
		commerciante.impostaNegozio(negozio);
		commerciante.impostaScambio(scambio);
	}

	@Override
	protected void bancoDellIncantatore(VistaBancoDiLavoro banco) {
		incantatore.impostaBanco(banco);
	}

	@Override
	protected void offerteDellAlchimista(List<? extends VistaOffertaConsumabile> offerte) {
		alchimista.impostaOfferte(offerte);
	}

	private TestoGdx testo(DoomdarkFont font) {
		return font == piccolo.getFont() ? piccolo : medio;
	}

	@Override
	protected SpriteGdx sprite(DescrizioneSpriteATempo descrizione) {
		return descrizione == null ? null : new SpriteATempoGdx(descrizione, immagini, testo(descrizione.getFont()));
	}

	@Override
	protected SpriteGdx sprite(DescrizioneSpriteEffetto descrizione) {
		return descrizione == null ? null : new SpriteEffettoGdx(descrizione, testo(descrizione.getFont()));
	}

	@Override
	protected SpriteGdx spriteDellaMorte(VistaPersonaggio personaggio) {
		return riquadroLocazione.spriteDellaMorte(personaggio);
	}

	@Override
	protected SpriteGdx spriteDellaSfida(float secondiDiAttesa, float secondiDiDissolvenza) {
		return riquadroSfida.spriteCheSiDissolve(camera, viewport, secondiDiAttesa, secondiDiDissolvenza);
	}

	@Override
	protected SpriteAnnuncioGlobaleGdx annuncio(DescrizioneAnnuncioGlobale descrizione) {
		return new SpriteAnnuncioGlobaleGdx(descrizione, immagini);
	}

	@Override
	protected SpriteFumettoGdx fumetto(String testo, int larghezzaMassima, int x, int y, int puntaVersoX, int puntaVersoY) {
		return new SpriteFumettoGdx(new DescrizioneFumetto(testo, larghezzaMassima, medio.getFont()), DoomdarkColorModel.Color.BLACK, x, y,
				puntaVersoX, puntaVersoY, immagini, medio);
	}

	@Override
	protected RivelazioneDellArtefattoGdx rivelazione(VistaArtefatto artefatto, int livelloMondo, Area area, Punto destinazione) {
		return new RivelazioneDellArtefattoGdx(artefatto, livelloMondo, area, destinazione, immagini, medio);
	}

	/**
	 * libGDX ridisegna a ogni fotogramma: non serve chiederlo.
	 */
	@Override
	public void ridisegna() {
		// niente
	}
}
