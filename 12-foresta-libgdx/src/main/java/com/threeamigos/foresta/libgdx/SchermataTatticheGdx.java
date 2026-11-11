package com.threeamigos.foresta.libgdx;

import com.threeamigos.foresta.ui.schermate.SchermataTattiche;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.tattiche.modifica.BozzaTattica;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.LivelloDiZoom;
import com.threeamigos.foresta.ui.componenti.ElencoScorrevole;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche.Quadro;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche.Riga;
import com.threeamigos.foresta.ui.schermate.MenuADiscesa;
import com.threeamigos.foresta.ui.schermate.SchermataDiScambio;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * La schermata delle tattiche di combattimento in libGDX: a sinistra l'elenco delle voci della tattica del personaggio, al centro il personaggio con i
 * pulsanti, a destra la spiegazione. Che cosa c'è, il suo posto, la bozza, i menu e i click sono di {@link EditorDelleTattiche} e {@link MenuADiscesa}, nel
 * modulo comune, gli stessi di Swing; qui c'è il disegno: le righe sono contenuti (testi e icone con il loro posto) che {@link ElencoScorrevoleGdx} mostra,
 * il menu a discesa un riquadro con la sua lista, e il cartiglio dell'aiuto.
 */
public final class SchermataTatticheGdx extends SchermataDiScambio implements SchermataTattiche {

	private static final String CORNICE = "fondi/CorniceInventario.png";

	private final ImmaginiGdx immagini;
	private final TestoGdx medio;
	private final ElencoScorrevoleGdx elencoGdx;
	private final DisegnoDiContenutoGdx disegno;
	private final CartiglioGdx cartiglio;
	private final EditorDelleTattiche editor;
	private final float zoom = (float) LivelloDiZoom.valore();
	private final int larghezzaCornice;

	public SchermataTatticheGdx(int width, int height, VistaPartita vistaPartita, ImmaginiGdx immagini, TestoGdx piccolo, TestoGdx medio) {
		super(width, height, vistaPartita, immagini.larghezza(CORNICE, (float) LivelloDiZoom.valore()), immagini.altezza(CORNICE, (float) LivelloDiZoom.valore()),
				SchermataDiScambioArtefattiGdx.misure(immagini));
		this.immagini = immagini;
		this.medio = medio;
		this.larghezzaCornice = immagini.larghezza(CORNICE, zoom);
		this.disegno = new DisegnoDiContenutoGdx(immagini, piccolo, medio);
		this.cartiglio = new CartiglioGdx(immagini, medio);
		this.elencoGdx = new ElencoScorrevoleGdx(immagini, piccolo, medio);
		this.editor = new EditorDelleTattiche(this, vistaPartita, SchermataDiScambioArtefattiGdx.misure(immagini));
	}

	public EditorDelleTattiche getEditor() {
		return editor;
	}

	/** Chi chiede un testo al giocatore (il nome di una voce) e chi chiude la richiesta: senza, una voce non si rinomina. */
	public void setRichiestaTesto(BiConsumer<String, Consumer<String>> richiestaTesto, Consumer<Consumer<String>> annullaRichiesta) {
		editor.setRichiestaTesto(richiestaTesto, annullaRichiesta);
	}

	/** Mostra la tattica del personaggio (vedi {@link EditorDelleTattiche#impostaTattica}). */
	public void impostaTattica(VistaPersonaggio personaggio, TatticaMD tattica, TatticaMD tatticaPredefinita, Set<TipoAzione> azioniPossibili) {
		editor.impostaTattica(personaggio, tattica, tatticaPredefinita, azioniPossibili);
	}

	public BozzaTattica getBozza() {
		return editor.getBozza();
	}

	public void disegna(Batch batch, Camera camera, Viewport viewport) {
		int xCorniceDestra = width - SPACING - larghezzaCornice;
		immagini.disegnaAFasce(batch, CORNICE, SPACING, SPACING, zoom, getFasceDellaCornice());
		immagini.disegnaAFasce(batch, CORNICE, xCorniceDestra, SPACING, zoom, getFasceDellaCornice());
		intestazione(batch, TestiDellaUI.get("TATTICHE_TATTICA"), SPACING + larghezzaCornice / 2);
		intestazione(batch, TestiDellaUI.get("TATTICHE_SPIEGAZIONE"), width - SPACING - larghezzaCornice / 2);
		if (editor.getPersonaggio() == null) {
			return;
		}
		disegno.disegna(batch, editor.colonnaCentrale(), 0, 0, 1f);
		Quadro quadro = editor.quadro();
		disegnaLElenco(batch, camera, viewport, quadro.elenco);
		disegno.disegna(batch, quadro.spiegazione, 0, 0, 1f);
		if (quadro.menu != null) {
			disegnaIlMenu(batch, camera, viewport, quadro.menu);
		} else if (quadro.aiuto != null && vistaPartita.isAiutoAbilitato() && mouseX >= 0) {
			cartiglio.disegnaAccantoAlMouse(batch, Collections.singletonList(quadro.aiuto), mouseX, mouseY, width, height);
		}
	}

	private void intestazione(Batch batch, String testo, int xCentro) {
		int x = xCentro - medio.larghezza(testo) / 2;
		medio.disegna(batch, testo, x + 2, SPACING + 2, DoomdarkColorModel.Color.BLACK);
		medio.disegna(batch, testo, x, SPACING, DoomdarkColorModel.Color.LIGHT_GRAY);
	}

	private void disegnaLElenco(Batch batch, Camera camera, Viewport viewport, ElencoScorrevole<Riga> elenco) {
		int altezza = altezzaDisponibileInRiquadroInventario();
		int massimo = elenco.getMassimoScorrimento(altezza);
		scorrimentoSinistra.imposta(massimo > 0, massimo);
		int offset = elenco.limitaOffset(altezza, (int) Math.round(scorrimentoSinistra.getScorrimento()));
		elencoGdx.disegna(batch, camera, viewport, elenco, new Area(xMinimaZonaSinistra + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING,
				larghezzaDisponibileInRiquadroInventario(), altezza), offset);
	}

	/** Il riquadro scuro con il bordo chiaro e dentro la lista delle scelte, con quella sotto il mouse in bianco. */
	private <T> void disegnaIlMenu(Batch batch, Camera camera, Viewport viewport, MenuADiscesa<T> menu) {
		immagini.riempi(batch, menu.getX(), menu.getY(), menu.getLarghezza(), menu.getAltezza(), colore(MenuADiscesa.SFONDO));
		Color bordo = colore(MenuADiscesa.BORDO);
		immagini.riempi(batch, menu.getX(), menu.getY(), menu.getLarghezza(), 1, bordo);
		immagini.riempi(batch, menu.getX(), menu.getY() + menu.getAltezza() - 1, menu.getLarghezza(), 1, bordo);
		immagini.riempi(batch, menu.getX(), menu.getY(), 1, menu.getAltezza(), bordo);
		immagini.riempi(batch, menu.getX() + menu.getLarghezza() - 1, menu.getY(), 1, menu.getAltezza(), bordo);
		ElencoScorrevole<T> elenco = menu.elenco(menu.sceltaAlla(mouseX, mouseY));
		menu.aggiorna();
		int offset = elenco.limitaOffset(menu.getAltezzaInterna(), (int) Math.round(menu.getScorrimento().getScorrimento()));
		elencoGdx.disegna(batch, camera, viewport, elenco, new Area(menu.getX() + MenuADiscesa.MARGINE, menu.getY() + MenuADiscesa.MARGINE,
				menu.getLarghezzaInterna(), menu.getAltezzaInterna()), offset);
	}

	private static Color colore(int rgb) {
		return new Color(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, 1f);
	}

	// ============================================================== il mouse

	@Override
	public boolean gestisceDoppioClick() {
		return false;
	}

	@Override
	public int passiPerScattoDellaRotella() {
		// il menu a discesa ha già un passo suo
		return editor.isMenuAperto() ? 1 : super.passiPerScattoDellaRotella();
	}

	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (!editor.processaRotella(x, y, numeroRotazioni, movimentoRotella)) {
			super.processaRotella(x, y, numeroRotazioni, movimentoRotella);
		}
	}

	/** Con il menu a discesa aperto la pressione è per il menu: la lista sotto non si trascina. */
	@Override
	protected boolean puoScorrere() {
		return !editor.isMenuAperto();
	}

	@Override
	public void processaPressione(int x, int y, Tasto tasto) {
		if (!editor.processaPressione(x, y, tasto)) {
			super.processaPressione(x, y, tasto);
		}
	}

	@Override
	public void processaTrascinamento(int x, int y) {
		if (!editor.processaTrascinamento(x, y)) {
			super.processaTrascinamento(x, y);
		}
	}

	@Override
	public void processaRilascio(int x, int y, Tasto tasto) {
		if (!editor.processaRilascio(x, y, tasto)) {
			super.processaRilascio(x, y, tasto);
		}
	}

	@Override
	public void processaClick(int x, int y, Tasto tasto) {
		editor.processaClick(x, y, tasto);
	}
}
