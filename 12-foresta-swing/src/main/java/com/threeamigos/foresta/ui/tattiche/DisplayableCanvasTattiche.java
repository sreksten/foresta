package com.threeamigos.foresta.ui.tattiche;

import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.tattiche.modifica.BozzaTattica;
import com.threeamigos.foresta.tipi.TipoAzione;
import com.threeamigos.foresta.ui.componenti.ComponenteScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.schermate.DisegnoDiContenutoAwt;
import com.threeamigos.foresta.ui.schermate.DisplayableCanvasScambiatore;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche;
import com.threeamigos.foresta.ui.schermate.SchermataTattiche;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche.Quadro;
import com.threeamigos.foresta.ui.schermate.EditorDelleTattiche.Riga;
import com.threeamigos.foresta.ui.schermate.MenuADiscesa;
import com.threeamigos.foresta.ui.schermate.MisureDelleImmaginiAwt;
import com.threeamigos.foresta.ui.testo.TestiDellaUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * La schermata delle tattiche di combattimento in Swing, che si apre dall'inventario (comando MOSTRA_TATTICHE). Che cosa c'è, il suo posto, la bozza, i menu e i
 * click sono di {@link EditorDelleTattiche} e {@link MenuADiscesa}, nel modulo comune; qui c'è il disegno: l'elenco delle voci (un
 * {@link ComponenteScorrevole} con una riga per nodo), la colonna centrale, la spiegazione a destra, il menu a discesa e il cartiglio dell'aiuto.
 */
public class DisplayableCanvasTattiche extends DisplayableCanvasScambiatore implements SchermataTattiche {

	private final EditorDelleTattiche editor;
	private final DisegnoDiContenutoAwt disegnoDelContenuto = new DisegnoDiContenutoAwt();

	public DisplayableCanvasTattiche(int width, int height, VistaPartita vistaPartita) {
		super(width, height, vistaPartita);
		editor = new EditorDelleTattiche(this, vistaPartita, new MisureDelleImmaginiAwt());
	}

	/**
	 * @param richiestaTesto   chi chiede un testo al giocatore: il testo predefinito e chi riceve quello confermato
	 * @param annullaRichiesta chi chiude la richiesta di prima se non serve più, riconoscendola da chi doveva riceverla
	 */
	public void setRichiestaTesto(BiConsumer<String, Consumer<String>> richiestaTesto, Consumer<Consumer<String>> annullaRichiesta) {
		editor.setRichiestaTesto(richiestaTesto, annullaRichiesta);
	}

	/**
	 * Se si sta ancora aspettando il nome di una voce, lo si smette di aspettare: la bozza è cambiata, o si è lasciata la schermata.
	 */
	public void annullaLaRichiestaDiTesto() {
		editor.annullaLaRichiestaDiTesto();
	}

	/**
	 * Mostra la tattica del personaggio (vedi EditorDelleTattiche.impostaTattica).
	 */
	public void impostaTattica(VistaPersonaggio nuovo, TatticaMD tattica, TatticaMD tatticaPredefinita, Set<TipoAzione> azioniPossibili) {
		editor.impostaTattica(nuovo, tattica, tatticaPredefinita, azioniPossibili);
	}

	/**
	 * Un click sul nome ripiega o riapre la voce; il secondo click ravvicinato annulla il primo e chiede il nuovo nome.
	 */
	void clickSulNome(BozzaTattica.Voce voce, int indice) {
		editor.clickSulNome(voce, indice);
	}

	/**
	 * Per i test: come il click sul primo pezzo con quel testo dell'elenco, senza passare dalle coordinate.
	 */
	boolean attivaPezzo(String testo) {
		return editor.attivaPezzo(testo);
	}

	boolean isMenuAperto() {
		return editor.isMenuAperto();
	}

	/** Per i test: la logica della schermata */
	public EditorDelleTattiche getEditor() {
		return editor;
	}

	BozzaTattica getBozza() {
		return editor.getBozza();
	}

	// ============================================================== il disegno

	/**
	 * Disegna la schermata: la colonna del personaggio e l'elenco delle voci della sua tattica, con la casella a discesa aperta per ultima.
	 */
	public void disegna(Graphics2D graphics) {
		super.disegnaInventario(graphics);
		if (editor.getPersonaggio() == null) {
			return;
		}
		disegnaColonnaPersonaggio(graphics);
		Quadro quadro = editor.quadro();
		ComponenteScorrevole<Riga> elenco = new ComponenteScorrevole<>(quadro.elenco, larghezzaDisponibileInRiquadroInventario(), 0, 1, this::immagineDelNodo);
		graphics.drawImage(elenco.produci(altezzaDisponibileInRiquadroInventario(), scorrimentoSinistra),
				xMinimaZonaSinistra + SPACING, DIMENSIONE_BORDO_INTERNO + 2 * SPACING, null);
		disegnoDelContenuto.disegna(graphics, quadro.spiegazione, 0, 0);
		if (quadro.menu != null) {
			disegnaIlMenu(graphics, quadro.menu);
		} else if (quadro.aiuto != null) {
			disegnaAiuto(graphics, Collections.singletonList(quadro.aiuto));
		}
	}

	private <T> void disegnaIlMenu(Graphics2D graphics, MenuADiscesa<T> menu) {
		new CasellaADiscesa<>(menu).disegna(graphics, mouseX, mouseY);
	}

	@Override
	public void disegnaIntestazioniInventario(Graphics2D graphics) {
		disegnaIntestazioniInventarioImpl(graphics, TestiDellaUI.get("TATTICHE_TATTICA"), TestiDellaUI.get("TATTICHE_SPIEGAZIONE"));
	}

	@Override
	public void disegnaColonnaPersonaggio(Graphics2D graphics) {
		disegnoDelContenuto.disegna(graphics, editor.colonnaCentrale(), 0, 0);
	}

	/**
	 * L'immagine di una riga dell'elenco, dal suo contenuto (testi e icone con il loro posto)
	 */
	private Image immagineDelNodo(Object oggetto) {
		ContenutoDiColonna contenuto = (ContenutoDiColonna) oggetto;
		BufferedImage immagine = new BufferedImage(Math.max(1, contenuto.getLarghezza()), Math.max(1, contenuto.getAltezza()), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = immagine.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		for (Elemento e : contenuto.getElementi()) {
			if (e.getTipo() == Elemento.Tipo.TESTO) {
				g2d.drawImage(ImageCache.get(e.getRighe().get(0), e.getFont(), e.getColore()), e.getX(), e.getY(), null);
			} else {
				if (e.getOpacita() < 1f) {
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, e.getOpacita()));
				}
				g2d.drawImage(icona(e.getRisorsa()), e.getX(), e.getY(), e.getLarghezza(), e.getAltezza(), null);
				g2d.setComposite(AlphaComposite.SrcOver);
			}
		}
		g2d.dispose();
		return immagine;
	}

	private static Image icona(String risorsa) {
		return ImageCache.getComposta(Arrays.<Object>asList("iconaDelleTattiche", risorsa), () -> BufferedImageBuilder.buildBufferedImage(risorsa));
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

	/**
	 * Con il menu a discesa aperto la pressione è per il menu: la lista sotto non si trascina
	 */
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
