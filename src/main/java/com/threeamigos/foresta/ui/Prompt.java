package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoInvioTesto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;

public class Prompt extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private class MyFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			e.getComponent().requestFocus();
		}
	}

	private final TextField tf;

	public String getText() {
		return tf.getText();
	}

	public Prompt() {
		super();
		BufferedImage cornice = ImageCache.cornicePiccola;
		setSize(new Dimension(cornice.getWidth(), cornice.getHeight()));
		setLayout(null);
		tf = new TextField();
		tf.addFocusListener(new MyFocusListener());
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
	 * ActionListener interface
	 */
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		BusEventi.pubblica(new ComandoInvioTesto(tf.getText()));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ImageCache.cornicePiccola, 0, 0, null);
	}
}