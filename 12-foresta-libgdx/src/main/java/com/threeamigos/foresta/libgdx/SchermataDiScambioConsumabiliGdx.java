package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.tipi.TipoBottega;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.componenti.ScorrimentoSuUnAsse;
import com.threeamigos.foresta.ui.schermate.ScambioDeiConsumabili;
import com.threeamigos.foresta.ui.schermate.ScambioDeiConsumabili.Voce;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.util.ArrayList;
import java.util.List;

/**
 * La schermata dell'alchimista in libGDX: a sinistra i consumabili del gruppo, a destra le offerte, in mezzo l'alchimista con le monete. Le due liste, le
 * notifiche di acquisto, l'aiuto e i click sono del modulo comune ({@link ScambioDeiConsumabili}, lo stesso di Swing); la colonna centrale è
 * {@link SchermataDiScambio#colonnaDelNegoziante}; qui c'è il disegno, gli sprite (le monete spese, l'incantesimo comprato) e i cartigli dell'aiuto.
 */
public final class SchermataDiScambioConsumabiliGdx extends SchermataDiScambio {

	private static final String CORNICE = "fondi/CorniceInventario.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final ElencoScorrevoleGdx elencoGdx;
	private final ScambioDeiConsumabili logica;
	private final DisegnoDiContenutoGdx disegno;
	private final CartiglioGdx cartiglio;
	private final float zoom = (float) LivelloDiZoom.valore();
	private final int larghezzaCornice;
	// Gli sprite che restano confinati nella schermata (le monete spese, l'incantesimo comprato)
	private final List<SpriteATempoGdx> spriteLocali = new ArrayList<>();

	public SchermataDiScambioConsumabiliGdx(int width, int height, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		super(width, height, vistaPartita, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()), immagini.altezza(CORNICE, (float) LivelloDiZoom.valore()),
				SchermataDiScambioArtefattiGdx.misure(immagini));
		this.immagini = immagini;
		this.medio = medio;
		this.larghezzaCornice = immagini.larghezza(CORNICE, zoom);
		this.disegno = new DisegnoDiContenutoGdx(immagini, piccolo, medio);
		this.cartiglio = new CartiglioGdx(immagini, medio);
		this.elencoGdx = new ElencoScorrevoleGdx(immagini, piccolo, medio);
		this.logica = new ScambioDeiConsumabili(this, vistaPartita, SchermataDiScambioArtefattiGdx.misure(immagini),
				descrizione -> spriteLocali.add(new SpriteATempoGdx(descrizione, immagini, medio)));
	}

	@Override
	public TipoPersonaggio getMestiereAlCentro() {
		return TipoBottega.ALCHIMISTA.getMestiere();
	}

	@Override
	public String getEtichettaMestiere() {
		return TipoBottega.ALCHIMISTA.getEtichettaDelMestiere();
	}

	/** Imposta le offerte da mostrare. */
	public void impostaOfferte(List<? extends VistaOffertaConsumabile> offerte) {
		logica.impostaOfferte(offerte);
	}

	/** Quando la schermata si apre gli sprite della volta scorsa, anche se non finiti, non ricompaiono. */
	@Override
	public void apri() {
		super.apri();
		spriteLocali.clear();
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		logica.processaClick(x, y, tasto);
	}

	@Override
	public void processaDoppioClick(int x, int y, Tasto tasto) {
		logica.processaDoppioClick(x, y, tasto);
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		int xCorniceDestra = width - SPACING - larghezzaCornice;
		immagini.disegnaAFasce(batch, CORNICE, SPACING, SPACING, zoom, getFasceDellaCornice());
		immagini.disegnaAFasce(batch, CORNICE, xCorniceDestra, SPACING, zoom, getFasceDellaCornice());
		disegnaElenco(batch, camera, viewport, logica.getElencoGruppo(), xMinimaZonaSinistra, scorrimentoSinistra, false);
		disegnaElenco(batch, camera, viewport, logica.getElencoVenditore(), xMinimaZonaDestra, scorrimentoDestra, true);
		intestazione(batch, TipoBottega.ALCHIMISTA.getIntestazioneSinistra(), SPACING + larghezzaCornice / 2);
		intestazione(batch, TipoBottega.ALCHIMISTA.getIntestazioneDestra(), width - SPACING - larghezzaCornice / 2);
		disegno.disegna(batch, colonnaDelNegoziante(getMestiereAlCentro()), 0, 0, 1f);
		for (SpriteATempoGdx sprite : new ArrayList<>(spriteLocali)) {
			sprite.anima(batch, camera, viewport);
			if (!sprite.isAttivo()) {
				spriteLocali.remove(sprite);
			}
		}
		// Per ultimo, sopra il resto
		if (vistaPartita.isAiutoAbilitato() && mouseX >= 0) {
			cartiglio.disegnaAccantoAlMouse(batch, logica.righeAiuto(), mouseX, mouseY, width, height);
		}
	}

	private void intestazione(Batch batch, String testo, int xCentro) {
		int x = xCentro - medio.larghezza(testo) / 2;
		medio.disegna(batch, testo, x + 2, SPACING + 2, DoomdarkColorModel.Color.BLACK);
		medio.disegna(batch, testo, x, SPACING, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	private void disegnaElenco(Batch batch, Camera camera, Viewport viewport, List<Voce> voci, int x, ScorrimentoSuUnAsse scorrimento, boolean mostraCosto) {
		int altezza = altezzaDisponibileInRiquadroInventario();
		// L'elenco può essere cambiato dall'ultimo scorrimento: il limite si aggiorna e lo scorrimento resta dentro
		Voce evidenziata = logica.trovaVoce(voci, x, (int) Math.round(scorrimento.getScorrimento()), mouseX, mouseY, mostraCosto);
		ElencoScorrevole<Voce> elenco = logica.costruisciElenco(voci, evidenziata, mostraCosto);
		int massimo = elenco.getMassimoScorrimento(altezza);
		scorrimento.imposta(massimo > 0, massimo);
		scorrimento.aggiorna();
		int offset = elenco.limitaOffset(altezza, (int) Math.round(scorrimento.getScorrimento()));
		elencoGdx.disegna(batch, camera, viewport, elenco, new Area(x + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, larghezzaDisponibileInRiquadroInventario(), altezza),
				offset);
	}
}
