package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoInvioTesto;
import com.threeamigos.foresta.motore.modellodati.Serializzabile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Prompt extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private class MyFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			e.getComponent().requestFocus();
		}
	}

	// Un JTextField e non un java.awt.TextField: un componente AWT (nativo) ha una visibilità sua e, aggiunto a
	// finestra già aperta (a fine logo, vedi ForestaUI), comparirebbe anche con il Prompt nascosto e senza cornice
	private final JTextField tf;

	public String getText() {
		return Serializzabile.senzaPipe(tf.getText());
	}

	public Prompt() {
		super();
		BufferedImage cornice = ImageCache.cornicePiccola;
		setSize(new Dimension(cornice.getWidth(), cornice.getHeight()));
		setLayout(null);
		tf = new JTextField();
		tf.addFocusListener(new MyFocusListener());
		// Il "|" separa i campi dei salvataggi: non si lascia nemmeno scrivere
		tf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '|') {
					e.consume();
				}
			}
		});
		tf.setSize(cornice.getWidth() - 20, cornice.getHeight() - 10);
		tf.setLocation(10, (cornice.getHeight() - tf.getSize().height) / 2);
		tf.setBackground(Color.white);
		Font f = new Font("DialogInput", Font.PLAIN, tf.getSize().height - 6);
		tf.setFont(f);
		add(tf);
		tf.addActionListener(this);
		setVisible(false);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			tf.setText("");
		}
		super.setVisible(visible);
		if (visible) {
			tf.requestFocus();
		}
		repaint();
	}

	/**
	 * Mostra il Prompt già compilato con un testo, con il cursore in fondo.
	 */
	public void mostra(String testoPredefinito) {
		setVisible(true);
		tf.setText(testoPredefinito);
		tf.setCaretPosition(testoPredefinito.length());
	}

	/**
	 * ActionListener interface
	 */
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		// Il testo incollato può ancora contenere un "|"
		BusEventi.pubblica(new ComandoInvioTesto(getText()));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ImageCache.cornicePiccola, 0, 0, null);
	}
}