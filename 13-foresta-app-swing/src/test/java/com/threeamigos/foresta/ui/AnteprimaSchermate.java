package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.PartitaDiAnteprima;
import com.threeamigos.foresta.motore.StatoDelMotore;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.immagini.ImageCache;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Strumento di sviluppo: salva in PNG le schermate principali del gioco, disegnate fuori schermo con una partita di
 * anteprima (vedi PartitaDiAnteprima), per confrontarle prima e dopo una modifica dell'impaginazione. Come
 * AnteprimaIntermezzo non serve una finestra.
 * <p>
 * Uso, dalla radice del progetto dopo {@code mvn test-compile}:
 * <pre>
 *     java -cp ... com.threeamigos.foresta.ui.AnteprimaSchermate CARTELLA [ORIZZONTALE|VERTICALE]
 * </pre>
 * Scrive {@code gioco.png}, {@code inventario.png}, {@code mappa.png}, {@code trofei.png} e {@code bestiario.png}.
 */
public final class AnteprimaSchermate {

	private AnteprimaSchermate() {
	}

	public static void main(String[] args) throws IOException {
		File cartella = new File(args.length > 0 ? args[0] : ".");
		Orientamento orientamento = args.length > 1 && args[1].equalsIgnoreCase("VERTICALE") ? Orientamento.VERTICALE : Orientamento.ORIZZONTALE;
		cartella.mkdirs();

		BusEventi.iscriviti(InternoErrore.class, errore -> System.err.println("ERRORE: " + errore.getMessaggio()));
		// Come in Main: i dati della partita e i componenti che ricordano messaggi e notizie
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		StatoDelMotore.registraComponenti();
		ImageCache.init();
		PartitaDiAnteprima.prepara(TipoPersonaggio.GUERRIERO, "Aldric",
				TipoPersonaggio.BARDO, TipoPersonaggio.MAGO, TipoPersonaggio.LADRA, TipoPersonaggio.ELFA);

		Dimension finestra = ForestaSwing.calcolaDimensioniFinestra(orientamento, false);
		DisplayableCanvas canvas = new DisplayableCanvas(orientamento, finestra.width, finestra.height, false, new GestorePunteggiInMemoria(),
				new VistaPartitaMotore());
		canvas.setSize(finestra.width, finestra.height);

		canvas.getSchermo().iniziaGioco();
		// Qualche messaggio nel riquadro di testo, con le lettere accentate
		canvas.getSchermo().notifica("Arrivate in una radura. È già l'alba: càpita spesso, qui, di vedere un cavallo che parla.");
		canvas.getSchermo().notifica("Il Guerriero dice: «Più in là c'è un lago, perché non ci riposiamo?»");
		canvas.getSchermo().notifica("Trovate una pozione, un'antica spada e due monete d'oro. Che fortuna!");
		canvas.getSchermo().notifica("Un lupo vi guarda dal bosco, poi se ne va: può darsi che tornerà domani.");
		salva(canvas, new File(cartella, "gioco.png"));
		canvas.getSchermo().inventario();
		salva(canvas, new File(cartella, "inventario.png"));
		canvas.getSchermo().mappa();
		salva(canvas, new File(cartella, "mappa.png"));
		canvas.getSchermo().inventario();
		canvas.getSchermo().trofei();
		salva(canvas, new File(cartella, "trofei.png"));
		canvas.getSchermo().inventario();
		canvas.getSchermo().bestiario();
		salva(canvas, new File(cartella, "bestiario.png"));
		System.out.println("Salvate le schermate in " + cartella.getAbsolutePath());
	}

	private static void salva(DisplayableCanvas canvas, File file) throws IOException {
		BufferedImage immagine = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = immagine.createGraphics();
		canvas.paint(graphics);
		graphics.dispose();
		ImageIO.write(immagine, "png", file);
	}
}
