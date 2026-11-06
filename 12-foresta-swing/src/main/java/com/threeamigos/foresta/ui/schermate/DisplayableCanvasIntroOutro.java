package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.interfacce.GestorePunteggi;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.strumenti.TestataSalvataggio;
import com.threeamigos.foresta.tipi.MappaPersonaggioImmagine;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.Finestra;
import com.threeamigos.foresta.ui.Misura;
import com.threeamigos.foresta.ui.componenti.ColonnaScorrevole;
import com.threeamigos.foresta.ui.componenti.ContenutoDiColonna.Elemento;
import com.threeamigos.foresta.ui.componenti.ScorrimentoVerticale;
import com.threeamigos.foresta.ui.immagini.DimensioniRisorsa;
import com.threeamigos.foresta.ui.immagini.ImageCache;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 * La finestra delle schermate senza partita in corso, disegnata con Swing: l'introduzione (storia che scorre, loghi, classifica, trofei), i messaggi su
 * sfondo con l'ombra del drago, le statistiche di fine partita e la scelta dello slot di salvataggio da caricare. Che cosa mostrano lo dicono
 * {@link SchermateSenzaPartita} (le schermate ferme) e {@link IntroDelGioco} (l'intro, che dipende dal tempo), nel modulo comune; qui si disegna.
 */
public class DisplayableCanvasIntroOutro implements Finestra {

	private final int width;
	private final int height;
	private final SchermateSenzaPartita schermate;
	private final IntroDelGioco intro;
	private final GestorePunteggi gestorePunteggi;
	private final DisegnoDiContenutoAwt disegno = new DisegnoDiContenutoAwt();

	// La colonna che scorre adesso nell'intro e il suo disegno (si rifà quando la colonna cambia)
	private ColonnaScorrevole colonnaDisegnata;
	private ScorrimentoVerticale scorrimento;

	/**
	 * @param gestorePunteggi la classifica, che l'intro mostra fra le sue pagine
	 * @param vistaPartita la partita appena finita, per le statistiche e i trofei
	 */
	public DisplayableCanvasIntroOutro(int width, int height, GestorePunteggi gestorePunteggi, VistaPartita vistaPartita) {
		this.gestorePunteggi = gestorePunteggi;
		this.width = width;
		this.height = height;
		MisureDelleImmagini misure = new MisureDelleImmagini() {
			@Override
			public Misura personaggio(TipoPersonaggio classe) {
				BufferedImage immagine = MappaPersonaggioImmagine.getImmagine(classe);
				return new Misura(immagine.getWidth(), immagine.getHeight());
			}

			@Override
			public Misura immagine(String risorsa) {
				Dimension d = DimensioniRisorsa.di(risorsa);
				return new Misura(d.width, d.height);
			}
		};
		this.schermate = new SchermateSenzaPartita(width, height, vistaPartita, misure);
		this.intro = new IntroDelGioco(width, height, gestorePunteggi, vistaPartita, misure);
	}

	/**
	 * Fa ripartire l'intro dalla storia (e le schermate di fine partita dal primo messaggio).
	 */
	public void resettaSequenza() {
		schermate.resettaSequenza();
		intro.resettaSequenza();
	}

	/**
	 * Porta l'intro alla classifica: da lì si passa ai trofei, poi si riparte dalla storia.
	 */
	public void posizionaSuPunteggi() {
		intro.posizionaSuPunteggi();
	}

	/**
	 * Passa al passo successivo della sequenza, ripartendo da zero oltre la lunghezza massima.
	 */
	public void incrementaSequenza(int lunghezzaMassima) {
		schermate.incrementaSequenza(lunghezzaMassima);
	}

	/**
	 * Imposta il messaggio che {@link #scrivi} mostra.
	 */
	public void impostaMessaggio(String messaggio) {
		schermate.impostaMessaggio(messaggio);
	}

	public void setSalvataggiDisponibili(Collection<TestataSalvataggio> salvataggiDisponibili) {
		schermate.setSalvataggiDisponibili(salvataggiDisponibili);
	}

	private void disegnaSchermata(Graphics2D graphics, DisposizioneDiSchermata schermata) {
		switch (schermata.getSfondo()) {
			case OMBRA_DEL_DRAGO:
				disegnaOmbraDelDrago(graphics);
				break;
			case TRIONFO:
				Image d = ImageCache.trionfo;
				graphics.drawImage(d, (width - d.getWidth(null)) / 2, (height - d.getHeight(null)) / 2, null);
				break;
			default:
				break;
		}
		disegno.disegna(graphics, schermata.getContenuto(), 0, 0);
	}

	/**
	 * Disegna il messaggio corrente, centrato e con a capo automatico, sopra l'ombra del drago se richiesta.
	 */
	public void scrivi(Graphics2D graphics, boolean disegnaOmbraDelDrago) {
		disegnaSchermata(graphics, schermate.scrivi(disegnaOmbraDelDrago));
	}

	/**
	 * Testo centrato in alto, con a capo automatico, nell'alfabeto grande dei messaggi:
	 * per chi compone una schermata attorno al testo (vedi DisplayableCanvasIntermezzo).
	 * Non tocca il messaggio corrente di questo riquadro.
	 */
	void scriviTestoCentrato(Graphics2D graphics, String testo) {
		disegnaSchermata(graphics, schermate.testo(testo));
	}

	/**
	 * Disegna i mostri uccisi per classe, uno per riga.
	 */
	public void statistiche(Graphics2D graphics) {
		disegnaSchermata(graphics, schermate.statistiche());
	}

	/**
	 * Disegna l'elenco dei salvataggi disponibili, con il gruppo di ciascuno.
	 */
	public void selezioneSlotDaCaricare(Graphics2D graphics) {
		disegnaSchermata(graphics, schermate.selezioneSlotDaCaricare());
	}

	/**
	 * Disegna gli slot di salvataggio fra cui scegliere dove salvare, con il gruppo di quelli occupati.
	 */
	public void selezioneSlotDaSalvare(Graphics2D graphics) {
		disegnaSchermata(graphics, schermate.selezioneSlotDaSalvare());
	}

	/**
	 * Disegna la richiesta di conferma per uscire dal gioco.
	 */
	public void confermaUscita(Graphics2D graphics) {
		disegnaSchermata(graphics, schermate.confermaUscita());
	}

	/**
	 * Disegna la schermata della sconfitta.
	 */
	public void perso(Graphics2D graphics) {
		disegnaSchermata(graphics, schermate.perso());
	}

	/**
	 * Disegna la schermata della vittoria.
	 */
	public void vinto(Graphics2D graphics) {
		disegnaSchermata(graphics, schermate.vinto());
	}

	/**
	 * La classifica ferma, a fine partita.
	 */
	public void hiscore(Graphics2D graphics) {
		disegnaSchermata(graphics, schermate.hiscore(gestorePunteggi));
	}

	/**
	 * L'intro, un fotogramma: la storia sale sullo sfondo della storia, i loghi compaiono appena la storia è salita
	 * sopra di loro, poi loghi e sfondo spariscono insieme; la classifica sale e si ferma, poi sparisce; i trofei
	 * scorrono tutti. Poi si ricomincia.
	 */
	public void intro(Graphics2D graphics) {
		IntroDelGioco.Quadro quadro = intro.avanza();
		if (quadro.isOmbraDelDrago()) {
			disegnaOmbraDelDrago(graphics);
		}
		if (quadro.getOpacitaDelloSfondoDellaStoria() > 0) {
			disegnaConOpacita(graphics, ImageCache.sfondoStoria, (width - ImageCache.sfondoStoria.getWidth()) >> 1,
					(height - ImageCache.sfondoStoria.getHeight()) >> 1, quadro.getOpacitaDelloSfondoDellaStoria());
		}
		if (quadro.getColonna() != colonnaDisegnata) {
			colonnaDisegnata = quadro.getColonna();
			scorrimento = new ScorrimentoVerticale(disegno.comeImmagine(quadro.getContenuto()), colonnaDisegnata, IntroDelGioco.FASCIA);
		}
		scorrimento.disegna(graphics, quadro.getOpacitaDellaColonna());
		for (Elemento logo : quadro.getLoghi()) {
			disegno.disegnaConOpacita(graphics, logo, quadro.getOpacitaDeiLoghi());
		}
	}

	private static void disegnaConOpacita(Graphics2D graphics, Image immagine, int x, int y, float opacita) {
		if (opacita <= 0) {
			return;
		}
		Composite composito = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, opacita)));
		graphics.drawImage(immagine, x, y, null);
		graphics.setComposite(composito);
	}

	void disegnaOmbraDelDrago(Graphics2D graphics) {
		Image d = ImageCache.ombraDelDrago;
		graphics.drawImage(d, (width - d.getWidth(null)) >> 1, (height - d.getHeight(null)) >> 1, null);
	}
}
