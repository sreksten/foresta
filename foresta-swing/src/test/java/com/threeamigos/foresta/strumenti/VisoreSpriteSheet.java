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
 * Uno strumento per chi disegna: apre uno sprite sheet e ne mostra l'animazione, con i fotogrammi letti da sinistra a destra
 * e dall'alto in basso. L'immagine si trascina sulla finestra (o si passa come argomento). In basso si scelgono quanti
 * fotogrammi ci sono in ogni riga e quante righe ci sono (da 1 a 7), quanti fotogrammi ha l'animazione (da 1 a 25, i primi:
 * l'ultima riga può essere incompleta, e se la griglia ne ha meno si usano tutti i suoi), il primo e l'ultimo del giro (si
 * ripetono solo i fotogrammi fra i due; se uno supera l'altro se lo porta dietro) e quanti millisecondi far passare fra un
 * fotogramma e l'altro; un tasto ferma l'animazione e la fa ripartire, e ad animazione ferma un ultimo cursore mostra il
 * fotogramma che si vuole (mentre gira, il cursore è spento e segue l'animazione). L'animazione è ingrandita quanto sta nella finestra,
 * a multipli interi per non sfocare i pixel, su una scacchiera che mostra la trasparenza.
 * <p>
 * Non fa parte del gioco: si lancia a mano, dal modulo foresta-swing.
 */
public class VisoreSpriteSheet {

	private static final int LATO_MASSIMO = 7;
	private static final int FOTOGRAMMI_MASSIMI = 25;
	private static final int LATO_INIZIALE = 5;
	private static final int ATTESA_MINIMA = 20;
	private static final int ATTESA_MASSIMA = 1000;
	private static final int ATTESA_INIZIALE = 120;

	private final JFrame finestra = new JFrame("Sprite sheet: trascina qui un'immagine");
	private final Animazione animazione = new Animazione();
	private final JSlider cursoreColonne = new JSlider(1, LATO_MASSIMO, LATO_INIZIALE);
	private final JSlider cursoreRighe = new JSlider(1, LATO_MASSIMO, LATO_INIZIALE);
	private final JSlider cursoreFotogrammi = new JSlider(1, FOTOGRAMMI_MASSIMI, LATO_INIZIALE * LATO_INIZIALE);
	private final JSlider cursorePrimo = new JSlider(1, FOTOGRAMMI_MASSIMI, 1);
	private final JSlider cursoreUltimo = new JSlider(1, FOTOGRAMMI_MASSIMI, FOTOGRAMMI_MASSIMI);
	private final JSlider cursoreAttesa = new JSlider(ATTESA_MINIMA, ATTESA_MASSIMA, ATTESA_INIZIALE);
	private final JLabel etichettaColonne = new JLabel();
	private final JLabel etichettaRighe = new JLabel();
	private final JLabel etichettaFotogrammi = new JLabel();
	private final JLabel etichettaPrimo = new JLabel();
	private final JLabel etichettaUltimo = new JLabel();
	private final JLabel etichettaAttesa = new JLabel();
	private final JSlider cursoreMostrato = new JSlider(1, FOTOGRAMMI_MASSIMI, 1);
	private final JLabel etichettaMostrato = new JLabel();
	private final Timer timer = new Timer(ATTESA_INIZIALE, e -> {
		animazione.avanti();
		sincronizzaMostrato();
	});
	private final JButton avviaFerma = new JButton("Ferma");
	private File file;
	private BufferedImage foglio;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			VisoreSpriteSheet visore = new VisoreSpriteSheet();
			if (args.length > 0) {
				visore.apri(new File(args[0]));
			}
		});
	}

	private VisoreSpriteSheet() {
		for (JSlider cursore : new JSlider[] {cursoreColonne, cursoreRighe}) {
			cursore.setMajorTickSpacing(1);
			cursore.setPaintTicks(true);
			cursore.setPaintLabels(true);
			cursore.setSnapToTicks(true);
			cursore.addChangeListener(e -> aggiornaGriglia());
		}
		for (JSlider cursore : new JSlider[] {cursoreFotogrammi, cursorePrimo, cursoreUltimo, cursoreMostrato}) {
			cursore.setMajorTickSpacing(1);
			cursore.setPaintTicks(true);
			cursore.setPaintLabels(true);
			cursore.setSnapToTicks(true);
		}
		cursoreFotogrammi.addChangeListener(e -> aggiornaFotogrammi());
		// Il primo del giro non può superare l'ultimo: chi si muove si porta dietro l'altro
		cursorePrimo.addChangeListener(e -> {
			if (cursoreUltimo.getValue() < cursorePrimo.getValue()) {
				cursoreUltimo.setValue(cursorePrimo.getValue());
			}
			aggiornaGiro();
		});
		cursoreUltimo.addChangeListener(e -> {
			if (cursorePrimo.getValue() > cursoreUltimo.getValue()) {
				cursorePrimo.setValue(cursoreUltimo.getValue());
			}
			aggiornaGiro();
		});
		cursoreAttesa.setMajorTickSpacing(200);
		cursoreAttesa.setMinorTickSpacing(20);
		cursoreAttesa.setPaintTicks(true);
		cursoreAttesa.addChangeListener(e -> aggiornaAttesa());

		JPanel comandi = new JPanel(new GridBagLayout());
		comandi.setBorder(new EmptyBorder(6, 10, 8, 10));
		aggiungiRiga(comandi, 0, etichettaColonne, cursoreColonne);
		aggiungiRiga(comandi, 1, etichettaRighe, cursoreRighe);
		aggiungiRiga(comandi, 2, etichettaFotogrammi, cursoreFotogrammi);
		aggiungiRiga(comandi, 3, etichettaPrimo, cursorePrimo);
		aggiungiRiga(comandi, 4, etichettaUltimo, cursoreUltimo);
		aggiungiRiga(comandi, 5, etichettaAttesa, cursoreAttesa);
		aggiungiRiga(comandi, 6, etichettaMostrato, cursoreMostrato);
		// Il cursore comanda solo ad animazione ferma; mentre gira lo muove il timer
		cursoreMostrato.setEnabled(false);
		cursoreMostrato.addChangeListener(e -> {
			if (!timer.isRunning()) {
				animazione.mostra(cursoreMostrato.getValue());
			}
			etichettaMostrato.setText("Fotogramma mostrato: " + animazione.getMostrato());
		});
		avviaFerma.addActionListener(e -> avviaOFerma());
		GridBagConstraints posizioneTasto = new GridBagConstraints();
		posizioneTasto.gridy = 7;
		posizioneTasto.gridwidth = 2;
		posizioneTasto.insets = new Insets(6, 0, 0, 0);
		comandi.add(avviaFerma, posizioneTasto);

		finestra.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		finestra.setLayout(new BorderLayout());
		finestra.add(animazione, BorderLayout.CENTER);
		finestra.add(comandi, BorderLayout.SOUTH);
		finestra.setTransferHandler(new GestoreTrascinamento());
		animazione.setTransferHandler(finestra.getTransferHandler());
		finestra.setSize(760, 920);
		finestra.setLocationRelativeTo(null);
		aggiornaGriglia();
		aggiornaGiro();
		aggiornaAttesa();
		finestra.setVisible(true);
		timer.start();
	}

	private static void aggiungiRiga(JPanel pannello, int riga, JLabel etichetta, JSlider cursore) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = riga;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 0, 2, 8);
		// Larga abbastanza per il testo più lungo, così i cursori restano allineati; l'altezza si prende dal font, perché
		// l'etichetta qui è ancora vuota
		etichetta.setPreferredSize(new Dimension(250, etichetta.getFontMetrics(etichetta.getFont()).getHeight()));
		pannello.add(etichetta, c);
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		pannello.add(cursore, c);
	}

	/**
	 * Cambiate le colonne o le righe, si ritaglia di nuovo il foglio.
	 */
	private void aggiornaGriglia() {
		int colonne = cursoreColonne.getValue();
		int righe = cursoreRighe.getValue();
		etichettaColonne.setText("Fotogrammi per riga: " + colonne);
		etichettaRighe.setText("Righe: " + righe);
		if (foglio != null) {
			animazione.setFoglio(foglio, colonne, righe);
			finestra.setTitle(file.getName() + " (" + foglio.getWidth() + "x" + foglio.getHeight() + ", fotogrammi di "
					+ foglio.getWidth() / colonne + "x" + foglio.getHeight() / righe + ")");
		}
		aggiornaFotogrammi();
	}

	private void aggiornaFotogrammi() {
		int voluti = cursoreFotogrammi.getValue();
		int griglia = cursoreColonne.getValue() * cursoreRighe.getValue();
		etichettaFotogrammi.setText("Fotogrammi in tutto: " + Math.min(voluti, griglia) + (voluti > griglia ? " (griglia piena)" : ""));
		animazione.setNumeroFotogrammi(voluti);
		sincronizzaMostrato();
	}

	/**
	 * Porta il cursore del fotogramma mostrato su quello che l'animazione mostra davvero.
	 */
	private void sincronizzaMostrato() {
		cursoreMostrato.setValue(animazione.getMostrato());
		etichettaMostrato.setText("Fotogramma mostrato: " + animazione.getMostrato());
	}

	private void aggiornaGiro() {
		etichettaPrimo.setText("Primo del giro: " + cursorePrimo.getValue());
		etichettaUltimo.setText("Ultimo del giro: " + cursoreUltimo.getValue());
		animazione.setGiro(cursorePrimo.getValue(), cursoreUltimo.getValue());
		sincronizzaMostrato();
	}

	/**
	 * Ferma l'animazione sul fotogramma corrente, o la fa ripartire da lì.
	 */
	private void avviaOFerma() {
		if (timer.isRunning()) {
			timer.stop();
			avviaFerma.setText("Avvia");
			cursoreMostrato.setEnabled(true);
		} else {
			cursoreMostrato.setEnabled(false);
			timer.start();
			avviaFerma.setText("Ferma");
		}
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
			this.file = file;
			foglio = immagine;
			aggiornaGriglia();
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
		private int numeroFotogrammi = 1;
		// Il primo e l'ultimo fotogramma del giro, da 1: si ripetono solo quelli in mezzo
		private int primo = 1;
		private int ultimo = 1;
		// Da 0
		private int corrente;

		Animazione() {
			setBackground(Color.DARK_GRAY);
		}

		void setFoglio(BufferedImage foglio, int colonne, int righe) {
			int larghezza = Math.max(1, foglio.getWidth() / colonne);
			int altezza = Math.max(1, foglio.getHeight() / righe);
			BufferedImage[] nuovi = new BufferedImage[colonne * righe];
			for (int i = 0; i < nuovi.length; i++) {
				nuovi[i] = foglio.getSubimage(i % colonne * larghezza, i / colonne * altezza, larghezza, altezza);
			}
			fotogrammi = nuovi;
			corrente = primoVero() - 1;
			repaint();
		}

		void setNumeroFotogrammi(int numero) {
			numeroFotogrammi = fotogrammi == null ? numero : Math.min(numero, fotogrammi.length);
			rientraNelGiro();
		}

		void setGiro(int primo, int ultimo) {
			this.primo = primo;
			this.ultimo = ultimo;
			rientraNelGiro();
		}

		/**
		 * L'ultimo del giro, che non va oltre i fotogrammi dell'animazione.
		 */
		private int ultimoVero() {
			return Math.min(ultimo, numeroFotogrammi);
		}

		/**
		 * Il primo del giro, che non va oltre l'ultimo.
		 */
		private int primoVero() {
			return Math.min(primo, ultimoVero());
		}

		private void rientraNelGiro() {
			if (corrente < primoVero() - 1 || corrente > ultimoVero() - 1) {
				corrente = primoVero() - 1;
			}
			repaint();
		}

		/**
		 * Mostra quel fotogramma (da 1), uno qualunque dell'animazione, anche fuori dal giro.
		 */
		void mostra(int fotogramma) {
			corrente = Math.max(1, Math.min(fotogramma, numeroFotogrammi)) - 1;
			repaint();
		}

		/**
		 * Il fotogramma mostrato, da 1.
		 */
		int getMostrato() {
			return corrente + 1;
		}

		void avanti() {
			corrente = corrente + 1 > ultimoVero() - 1 ? primoVero() - 1 : corrente + 1;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (fotogrammi == null) {
				g.setColor(Color.LIGHT_GRAY);
				String testo = "Trascina qui uno sprite sheet";
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
			g.drawString("Fotogramma " + (corrente + 1) + " di " + numeroFotogrammi + ", giro da " + primoVero() + " a " + ultimoVero()
					+ ", ingrandito " + scala + "x", 10, getHeight() - 10);
		}
	}
}
