package com.threeamigos.foresta.strumenti;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Uno strumento per chi disegna: apre uno sprite sheet di 3x3 fotogrammi e ne mostra l'animazione, letti da sinistra a
 * destra e dall'alto in basso. L'immagine si trascina sulla finestra (o si passa come argomento); in basso si scelgono quanti
 * fotogrammi usare (da 1 a 9, i primi) e quanti millisecondi far passare fra uno e l'altro. L'animazione è ingrandita
 * quanto sta nella finestra, a multipli interi per non sfocare i pixel, su una scacchiera che mostra la trasparenza.
 * <p>
 * Non fa parte del gioco: si lancia a mano, dal modulo foresta-swing.
 */
public class VisoreSpriteSheet {

	private static final int LATO = 3;
	private static final int FOTOGRAMMI_MASSIMI = LATO * LATO;
	private static final int ATTESA_MINIMA = 20;
	private static final int ATTESA_MASSIMA = 1000;
	private static final int ATTESA_INIZIALE = 120;

	private final JFrame finestra = new JFrame("Sprite sheet 3x3: trascina qui un'immagine");
	private final Animazione animazione = new Animazione();
	private final JSlider cursoreFotogrammi = new JSlider(1, FOTOGRAMMI_MASSIMI, FOTOGRAMMI_MASSIMI);
	private final JSlider cursoreAttesa = new JSlider(ATTESA_MINIMA, ATTESA_MASSIMA, ATTESA_INIZIALE);
	private final JLabel etichettaFotogrammi = new JLabel();
	private final JLabel etichettaAttesa = new JLabel();
	private final Timer timer = new Timer(ATTESA_INIZIALE, e -> animazione.avanti());

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			VisoreSpriteSheet visore = new VisoreSpriteSheet();
			if (args.length > 0) {
				visore.apri(new File(args[0]));
			}
		});
	}

	private VisoreSpriteSheet() {
		cursoreFotogrammi.setMajorTickSpacing(1);
		cursoreFotogrammi.setPaintTicks(true);
		cursoreFotogrammi.setPaintLabels(true);
		cursoreFotogrammi.setSnapToTicks(true);
		cursoreFotogrammi.addChangeListener(e -> aggiornaFotogrammi());
		cursoreAttesa.setMajorTickSpacing(200);
		cursoreAttesa.setMinorTickSpacing(20);
		cursoreAttesa.setPaintTicks(true);
		cursoreAttesa.addChangeListener(e -> aggiornaAttesa());

		JPanel comandi = new JPanel(new GridBagLayout());
		comandi.setBorder(new EmptyBorder(6, 10, 8, 10));
		aggiungiRiga(comandi, 0, etichettaFotogrammi, cursoreFotogrammi);
		aggiungiRiga(comandi, 1, etichettaAttesa, cursoreAttesa);

		finestra.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		finestra.setLayout(new BorderLayout());
		finestra.add(animazione, BorderLayout.CENTER);
		finestra.add(comandi, BorderLayout.SOUTH);
		finestra.setTransferHandler(new GestoreTrascinamento());
		animazione.setTransferHandler(finestra.getTransferHandler());
		finestra.setSize(600, 640);
		finestra.setLocationRelativeTo(null);
		aggiornaFotogrammi();
		aggiornaAttesa();
		finestra.setVisible(true);
		timer.start();
	}

	private static void aggiungiRiga(JPanel pannello, int riga, JLabel etichetta, JSlider cursore) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = riga;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 0, 2, 8);
		etichetta.setPreferredSize(new Dimension(150, etichetta.getPreferredSize().height));
		pannello.add(etichetta, c);
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		pannello.add(cursore, c);
	}

	private void aggiornaFotogrammi() {
		etichettaFotogrammi.setText("Fotogrammi: " + cursoreFotogrammi.getValue());
		animazione.setNumeroFotogrammi(cursoreFotogrammi.getValue());
	}

	private void aggiornaAttesa() {
		etichettaAttesa.setText("Attesa: " + cursoreAttesa.getValue() + " ms");
		timer.setDelay(cursoreAttesa.getValue());
	}

	private void apri(File file) {
		try {
			BufferedImage immagine = ImageIO.read(file);
			if (immagine == null) {
				throw new IOException("non è un'immagine che Java sa leggere");
			}
			animazione.setFoglio(immagine);
			finestra.setTitle(file.getName() + " (" + immagine.getWidth() + "x" + immagine.getHeight() + ", fotogrammi di "
					+ immagine.getWidth() / LATO + "x" + immagine.getHeight() / LATO + ")");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(finestra, "Non riesco ad aprire " + file.getName() + ": " + e.getMessage(),
					"Sprite sheet", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Apre il primo file trascinato sulla finestra.
	 */
	private class GestoreTrascinamento extends TransferHandler {

		@Override
		public boolean canImport(TransferSupport supporto) {
			return supporto.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}

		@Override
		public boolean importData(TransferSupport supporto) {
			if (!canImport(supporto)) {
				return false;
			}
			try {
				@SuppressWarnings("unchecked")
				List<File> file = (List<File>) supporto.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				if (file.isEmpty()) {
					return false;
				}
				apri(file.get(0));
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	/**
	 * Il pannello che disegna il fotogramma corrente, centrato e ingrandito.
	 */
	private static class Animazione extends JPanel {

		private static final int QUADRETTO = 8;
		private static final Color CHIARO = new Color(200, 200, 200);
		private static final Color SCURO = new Color(160, 160, 160);

		private BufferedImage[] fotogrammi;
		private int numeroFotogrammi = FOTOGRAMMI_MASSIMI;
		private int corrente;

		Animazione() {
			setBackground(Color.DARK_GRAY);
		}

		void setFoglio(BufferedImage foglio) {
			int larghezza = foglio.getWidth() / LATO;
			int altezza = foglio.getHeight() / LATO;
			BufferedImage[] nuovi = new BufferedImage[FOTOGRAMMI_MASSIMI];
			for (int i = 0; i < FOTOGRAMMI_MASSIMI; i++) {
				nuovi[i] = foglio.getSubimage(i % LATO * larghezza, i / LATO * altezza, larghezza, altezza);
			}
			fotogrammi = nuovi;
			corrente = 0;
			repaint();
		}

		void setNumeroFotogrammi(int numero) {
			numeroFotogrammi = numero;
			corrente %= numero;
			repaint();
		}

		void avanti() {
			corrente = (corrente + 1) % numeroFotogrammi;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (fotogrammi == null) {
				g.setColor(Color.LIGHT_GRAY);
				String testo = "Trascina qui uno sprite sheet di 3x3 fotogrammi";
				FontMetrics metriche = g.getFontMetrics();
				g.drawString(testo, (getWidth() - metriche.stringWidth(testo)) / 2, getHeight() / 2);
				return;
			}
			BufferedImage fotogramma = fotogrammi[corrente];
			int scala = Math.max(1, Math.min((getWidth() - 20) / fotogramma.getWidth(), (getHeight() - 40) / fotogramma.getHeight()));
			int w = fotogramma.getWidth() * scala;
			int h = fotogramma.getHeight() * scala;
			int x = (getWidth() - w) / 2;
			int y = (getHeight() - h) / 2;
			for (int qy = 0; qy < h; qy += QUADRETTO) {
				for (int qx = 0; qx < w; qx += QUADRETTO) {
					g.setColor((qx / QUADRETTO + qy / QUADRETTO) % 2 == 0 ? CHIARO : SCURO);
					g.fillRect(x + qx, y + qy, Math.min(QUADRETTO, w - qx), Math.min(QUADRETTO, h - qy));
				}
			}
			g.drawImage(fotogramma, x, y, w, h, null);
			g.setColor(Color.WHITE);
			g.drawString("Fotogramma " + (corrente + 1) + " di " + numeroFotogrammi + ", ingrandito " + scala + "x", 10, getHeight() - 10);
		}
	}
}
