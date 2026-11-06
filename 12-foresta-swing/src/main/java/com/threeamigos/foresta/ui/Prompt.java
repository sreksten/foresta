package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.ui.immagini.ImageCache;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Il campo in cui il giocatore scrive un testo, in Swing: un <code>JTextField</code> dentro la cornice del combattimento. Chi aspetta
 * la risposta, l'invio, la rinuncia e i caratteri ammessi sono del {@link CampoDiTesto} comune ({@link #getCampo()}), che lo
 * comanda.
 */
public class Prompt extends JPanel implements CampoDiTesto.Campo {

	private static final long serialVersionUID = 1L;

	private class MyFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			// Il fuoco si riprende solo se è passato a un altro componente della stessa finestra (un click sul canvas):
			// non se è passato a un'altra applicazione o a un'altra finestra, come quella di prova
			Component altro = e.getOppositeComponent();
			if (isShowing() && altro != null && SwingUtilities.getWindowAncestor(altro) == SwingUtilities.getWindowAncestor(e.getComponent())) {
				e.getComponent().requestFocus();
			}
		}
	}

	// Un JTextField e non un java.awt.TextField: un componente AWT (nativo) ha una visibilità sua e, aggiunto a
	// finestra già aperta (a fine logo, vedi ForestaSwing), comparirebbe anche con il Prompt nascosto e senza cornice
	private final JTextField tf;
	private final transient CampoDiTesto campo = new CampoDiTesto();

	public Prompt() {
		super();
		campo.setCampo(this);
		BufferedImage cornice = ImageCache.corniceCombattimento;
		setSize(new Dimension(cornice.getWidth(), cornice.getHeight()));
		setLayout(null);
		tf = new JTextField();
		tf.addFocusListener(new MyFocusListener());
		tf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (!CampoDiTesto.ammesso(e.getKeyChar())) {
					e.consume();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					campo.esc();
				}
			}
		});
		tf.setSize(cornice.getWidth() - 20, cornice.getHeight() - 10);
		tf.setLocation(10, (cornice.getHeight() - tf.getSize().height) / 2);
		tf.setBackground(Color.white);
		Font f = new Font("DialogInput", Font.PLAIN, tf.getSize().height - 6);
		tf.setFont(f);
		add(tf);
		tf.addActionListener(e -> campo.invio());
		setVisible(false);
	}

	/**
	 * Il campo comune che comanda questo
	 */
	public CampoDiTesto getCampo() {
		return campo;
	}

	@Override
	public void mostra(String testo) {
		setVisible(true);
		tf.setText(testo);
		tf.setCaretPosition(testo.length());
		tf.requestFocus();
		repaint();
	}

	@Override
	public void nascondi() {
		setVisible(false);
		repaint();
	}

	@Override
	public String getTestoScritto() {
		return tf.getText();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ImageCache.corniceCombattimento, 0, 0, null);
	}
}
