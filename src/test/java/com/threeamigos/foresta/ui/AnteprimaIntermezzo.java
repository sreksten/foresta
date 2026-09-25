package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.intermezzi.ClasseIntermezzo;
import com.threeamigos.foresta.intermezzi.Intermezzo;
import com.threeamigos.foresta.intermezzi.PaginaIntermezzo;
import com.threeamigos.foresta.motore.PartitaDiAnteprima;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Strumento di sviluppo: mostra un intermezzo senza dover giocare fino al punto in cui
 * scatta. Prepara una partita minima (vedi PartitaDiAnteprima) con il protagonista
 * indicato, chiede le pagine all'intermezzo e le disegna con la stessa finestra e alla
 * stessa dimensione del gioco. L'innesco (deveScattare) non viene interrogato.
 * <p>
 * Uso, dalla radice del progetto dopo {@code mvn test-compile}:
 * <pre>
 *     java -cp target/test-classes:target/classes com.threeamigos.foresta.ui.AnteprimaIntermezzo [INTERMEZZO] [opzioni]
 *
 *     INTERMEZZO            un valore di ClasseIntermezzo (default: il primo)
 *     --classe CLASSE       classe del protagonista, un valore di ClassePersonaggio (default GUERRIERO)
 *     --nome NOME           nome del protagonista (default Aldric)
 *     --pagina N            pagina da cui partire, da 1 (default 1)
 *     --png FILE            invece della finestra, salva in FILE una griglia: una riga per
 *                           pagina (o solo la pagina indicata con --pagina), una colonna per istante
 *     --istanti A,B,...     secondi da disegnare con --png (default 0,1,2,4)
 * </pre>
 * Nella finestra la pagina avanza da sola con le stesse regole del gioco, oppure con i
 * tasti: SPAZIO, freccia destra o click = pagina successiva; freccia sinistra = precedente;
 * R = ricomincia la pagina; P = pausa; G = rigenera le pagine (per contenuti casuali);
 * ESC = chiudi. Dopo l'ultima pagina si ricomincia dalla prima. Gli errori (per esempio
 * un'immagine mancante) vengono stampati sulla console.
 */
public final class AnteprimaIntermezzo {

	private static final int FRAME_AL_SECONDO = 30;

	private final Intermezzo intermezzo;
	private final DisplayableCanvasIntermezzo schermata;
	private final int larghezza;
	private final int altezza;

	private List<PaginaIntermezzo> pagine;
	private int indicePagina;
	private long inizioPaginaNanosecondi;
	// Secondi già trascorsi nella pagina prima dell'ultima pausa
	private double secondiAccumulati;
	private boolean inPausa;

	private AnteprimaIntermezzo(Intermezzo intermezzo, int indicePagina) {
		this.intermezzo = intermezzo;
		Dimension finestra = ForestaUI.calcolaDimensioniFinestra(Orientamento.ORIZZONTALE, false);
		Dimension contenuto = DisplayableCanvas.calcolaAreaDiContenuto(finestra.width, finestra.height,
				ForestaUI.orientamentoCanvas(Orientamento.ORIZZONTALE), ForestaUI.SPESSORE_BARRA_ICONE);
		larghezza = contenuto.width;
		altezza = contenuto.height;
		schermata = new DisplayableCanvasIntermezzo(larghezza, altezza, new DisplayableCanvasIntroOutro(larghezza, altezza));
		pagine = generaPagine();
		this.indicePagina = Math.max(0, Math.min(indicePagina, pagine.size() - 1));
	}

	public static void main(String[] args) throws IOException {
		String nomeIntermezzo = ClasseIntermezzo.values()[0].name();
		ClassePersonaggio classe = ClassePersonaggio.GUERRIERO;
		String nome = "Aldric";
		int pagina = 1;
		String png = null;
		double[] istanti = {0, 1, 2, 4};
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "--classe":
					classe = ClassePersonaggio.valueOf(args[++i].toUpperCase(Locale.ROOT));
					break;
				case "--nome":
					nome = args[++i];
					break;
				case "--pagina":
					pagina = Integer.parseInt(args[++i]);
					break;
				case "--png":
					png = args[++i];
					break;
				case "--istanti":
					istanti = leggiIstanti(args[++i]);
					break;
				default:
					if (args[i].startsWith("--")) {
						throw new IllegalArgumentException("Opzione sconosciuta: " + args[i]);
					}
					nomeIntermezzo = args[i].toUpperCase(Locale.ROOT);
			}
		}

		BusEventi.iscriviti(InternoErrore.class, errore -> System.err.println("ERRORE: " + errore.getMessaggio()));
		ImageCache.init();
		PartitaDiAnteprima.prepara(classe, nome);
		Intermezzo intermezzo = ClasseIntermezzo.valueOf(nomeIntermezzo).getIstanza();
		AnteprimaIntermezzo anteprima = new AnteprimaIntermezzo(intermezzo, pagina - 1);

		if (png != null) {
			anteprima.salvaPng(new File(png), istanti, args.length > 0 && contiene(args, "--pagina"));
			System.out.println("Salvato " + png);
		} else {
			SwingUtilities.invokeLater(anteprima::apriFinestra);
		}
	}

	private List<PaginaIntermezzo> generaPagine() {
		List<PaginaIntermezzo> generate = intermezzo.getPagine();
		if (generate == null || generate.isEmpty()) {
			throw new IllegalStateException("L'intermezzo " + intermezzo.getId() + " non ha pagine");
		}
		return new ArrayList<>(generate);
	}

	// ----- Griglia PNG

	private void salvaPng(File file, double[] istanti, boolean soloPaginaIndicata) throws IOException {
		int primaPagina = soloPaginaIndicata ? indicePagina : 0;
		int ultimaPagina = soloPaginaIndicata ? indicePagina : pagine.size() - 1;
		int righe = ultimaPagina - primaPagina + 1;
		int spazio = 8;
		BufferedImage griglia = new BufferedImage(istanti.length * (larghezza + spazio) - spazio,
				righe * (altezza + spazio) - spazio, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = griglia.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, griglia.getWidth(), griglia.getHeight());
		for (int riga = 0; riga < righe; riga++) {
			int numeroPagina = primaPagina + riga;
			schermata.mostra(pagine.get(numeroPagina));
			for (int colonna = 0; colonna < istanti.length; colonna++) {
				BufferedImage fotogramma = disegna(istanti[colonna]);
				Graphics2D gf = fotogramma.createGraphics();
				gf.setColor(Color.YELLOW);
				gf.drawString(String.format(Locale.ROOT, "pagina %d/%d  t = %.2f s", numeroPagina + 1, pagine.size(),
						istanti[colonna]), 8, altezza - 8);
				gf.dispose();
				g.drawImage(fotogramma, colonna * (larghezza + spazio), riga * (altezza + spazio), null);
			}
		}
		g.dispose();
		ImageIO.write(griglia, "png", file);
	}

	private BufferedImage disegna(double secondi) {
		BufferedImage immagine = new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, larghezza, altezza);
		schermata.disegnaAl(g, secondi);
		g.dispose();
		return immagine;
	}

	// ----- Finestra

	private void apriFinestra() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pannello = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics graphics) {
				super.paintComponent(graphics);
				schermata.disegnaAl((Graphics2D) graphics, secondiNellaPagina());
			}
		};
		pannello.setBackground(Color.BLACK);
		pannello.setPreferredSize(new Dimension(larghezza, altezza));
		pannello.setFocusable(true);
		pannello.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_SPACE:
					case KeyEvent.VK_RIGHT:
						vaiAllaPagina(indicePagina + 1);
						break;
					case KeyEvent.VK_LEFT:
						vaiAllaPagina(indicePagina - 1);
						break;
					case KeyEvent.VK_R:
						vaiAllaPagina(indicePagina);
						break;
					case KeyEvent.VK_P:
						cambiaPausa();
						break;
					case KeyEvent.VK_G:
						pagine = generaPagine();
						vaiAllaPagina(Math.min(indicePagina, pagine.size() - 1));
						break;
					case KeyEvent.VK_ESCAPE:
						frame.dispose();
						System.exit(0);
						break;
					default:
						break;
				}
			}
		});
		pannello.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				vaiAllaPagina(indicePagina + 1);
			}
		});
		frame.add(pannello);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		pannello.requestFocusInWindow();

		vaiAllaPagina(indicePagina);
		new Timer(1000 / FRAME_AL_SECONDO, e -> {
			avanzaSeScaduta();
			frame.setTitle(titolo());
			pannello.repaint();
		}).start();
	}

	private void vaiAllaPagina(int indice) {
		indicePagina = Math.floorMod(indice, pagine.size());
		secondiAccumulati = 0;
		inizioPaginaNanosecondi = System.nanoTime();
		schermata.mostra(pagine.get(indicePagina));
	}

	private void cambiaPausa() {
		if (inPausa) {
			inizioPaginaNanosecondi = System.nanoTime();
		} else {
			secondiAccumulati = secondiNellaPagina();
		}
		inPausa = !inPausa;
	}

	private double secondiNellaPagina() {
		if (inPausa) {
			return secondiAccumulati;
		}
		return secondiAccumulati + (System.nanoTime() - inizioPaginaNanosecondi) / 1_000_000_000.0;
	}

	/** Stessa regola del gioco: vedi PaginaIntermezzo.getSecondiPrimaDiAvanzare. */
	private void avanzaSeScaduta() {
		double secondiPrimaDiAvanzare = pagine.get(indicePagina).getSecondiPrimaDiAvanzare(intermezzo.getSecondiPerPagina());
		if (!inPausa && secondiPrimaDiAvanzare > 0 && secondiNellaPagina() >= secondiPrimaDiAvanzare) {
			vaiAllaPagina(indicePagina + 1);
		}
	}

	private String titolo() {
		double secondiPrimaDiAvanzare = pagine.get(indicePagina).getSecondiPrimaDiAvanzare(intermezzo.getSecondiPerPagina());
		String avanzamento = secondiPrimaDiAvanzare > 0
				? String.format(Locale.ROOT, "avanza a %.1f s", secondiPrimaDiAvanzare)
				: "avanza al click";
		return String.format(Locale.ROOT, "%s - pagina %d/%d - t = %.1f s (%s)%s",
				intermezzo.getId(), indicePagina + 1, pagine.size(), secondiNellaPagina(), avanzamento,
				inPausa ? " - PAUSA" : "");
	}

	// ----- Argomenti

	private static double[] leggiIstanti(String elenco) {
		String[] parti = elenco.split(",");
		double[] istanti = new double[parti.length];
		for (int i = 0; i < parti.length; i++) {
			istanti[i] = Double.parseDouble(parti[i].trim());
		}
		return istanti;
	}

	private static boolean contiene(String[] args, String opzione) {
		for (String arg : args) {
			if (arg.equals(opzione)) {
				return true;
			}
		}
		return false;
	}
}
