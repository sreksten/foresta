package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.GestorePunteggiInMemoria;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.PartitaDiAnteprima;
import com.threeamigos.foresta.motore.StatoDelMotore;
import com.threeamigos.foresta.motore.VistaPartitaMotore;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.TipoDiLayout;
import com.threeamigos.foresta.tipi.TipoFinestra;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;

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
 *     java -cp ... com.threeamigos.foresta.ui.AnteprimaSchermate CARTELLA [ORIZZONTALE|VERTICALE|SINISTRA|SCHERMO=&lt;profilo o misura&gt;]...
 * </pre>
 * Con {@code SCHERMO=} (come per {@code Main}: {@code SCHERMO=GALAXY_S24}, {@code SCHERMO=800x480}) le schermate hanno l'area virtuale e
 * il layout di quello schermo, senza scala, come la finestra di Swing.
 * Scrive {@code gioco.png}, {@code gioco_mappa.png} (con la mappa davanti), sul telefono {@code gioco_pila_compattata.png} (con la pila scorsa a
 * destra), {@code inventario.png}, {@code mappa.png}, {@code trofei.png}, {@code bestiario.png} e {@code opzioni.png}.
 */
public final class AnteprimaSchermate {

	private AnteprimaSchermate() {
	}

	public static void main(String[] args) throws IOException {
		File cartella = new File(args.length > 0 ? args[0] : ".");
		OpzioniDiAvvio opzioni = OpzioniDiAvvio.leggi(args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0]);
		Orientamento orientamento = opzioni.getOrientamento();
		SceltaDelLayout scelta = opzioni.getSchermoDiProva() == null ? null
				: SceltaDelLayout.per(opzioni.getSchermoDiProva(), new MisureDaiFile(), LivelloDiZoom.valore(),
				opzioni.getPosizioneDellaBarra());
		if (scelta != null) {
			orientamento = scelta.getOrientamento();
		}
		boolean barraASinistra = scelta != null ? scelta.isBarraASinistra() : opzioni.getPosizioneDellaBarra() == PosizioneDellaBarra.SINISTRA;
		cartella.mkdirs();

		BusEventi.iscriviti(InternoErrore.class, errore -> System.err.println("ERRORE: " + errore.getMessaggio()));
		// Come in Main: i dati della partita e i componenti che ricordano messaggi e notizie
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		StatoDelMotore.registraComponenti();
		ImageCache.init();
		PartitaDiAnteprima.prepara(TipoPersonaggio.GUERRIERO, "Aldric",
				TipoPersonaggio.BARDO, TipoPersonaggio.MAGO, TipoPersonaggio.LADRA, TipoPersonaggio.ELFA);

		Dimension finestra = scelta != null ? new Dimension(scelta.getAreaVirtuale().larghezza, scelta.getAreaVirtuale().altezza)
				: ForestaSwing.calcolaDimensioniFinestra(orientamento, false);
		DisplayableCanvas canvas = new DisplayableCanvas(scelta != null ? scelta.getTipo() : TipoDiLayout.TABLET, orientamento, barraASinistra, finestra.width,
				finestra.height, false, new GestorePunteggiInMemoria(), new VistaPartitaMotore());
		canvas.setSize(finestra.width, finestra.height);

		canvas.getSchermo().iniziaGioco();
		// Qualche messaggio nel riquadro di testo, con le lettere accentate
		canvas.getSchermo().notifica("Arrivate in una radura. È già l'alba: càpita spesso, qui, di vedere un cavallo che parla.");
		canvas.getSchermo().notifica("Il Guerriero dice: «Più in là c'è un lago, perché non ci riposiamo?»");
		canvas.getSchermo().notifica("Trovate una pozione, un'antica spada e due monete d'oro. Che fortuna!");
		canvas.getSchermo().notifica("Un lupo vi guarda dal bosco, poi se ne va: può darsi che tornerà domani.");
		salva(canvas, new File(cartella, "gioco.png"));
		// Con la mappa davanti: sul telefono è uno dei riquadri della pila
		canvas.getSchermo().primoPiano(TipoFinestra.MAPPA);
		salva(canvas, new File(cartella, "gioco_mappa.png"));
		// Sul telefono, con la pila scorsa a destra: la locazione ha più spazio per i personaggi
		PilaScorrevole pila = canvas.getSchermo().getPilaScorrevole();
		if (pila != null) {
			pila.commuta();
			pila.avanza(1);
			// Il primo disegno dà alla locazione lo spazio della pila, il secondo lo mostra
			salva(canvas, new File(cartella, "gioco_pila_compattata.png"));
			salva(canvas, new File(cartella, "gioco_pila_compattata.png"));
			// Le schermate dopo con la pila al suo posto
			pila.espandi();
			pila.avanza(1);
		}
		// L'inventario con lo scambio fra il capo e il gruppo, come lo apre l'automa
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		canvas.getSchermo().impostaScambioInventario(new AutomaInventario(gruppo.getCapo(), gruppo), gruppo.getCapo());
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
		canvas.getSchermo().opzioni(java.util.Collections.singletonList(com.threeamigos.foresta.tipi.Comando.ANNULLA));
		salva(canvas, new File(cartella, "opzioni.png"));
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
