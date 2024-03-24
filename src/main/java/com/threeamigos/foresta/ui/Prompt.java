package com.threeamigos.foresta.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Prompt extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private class MyFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			e.getComponent().requestFocus();
		}
	}

	private TextField tf;

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
		UI.riceviTesto(tf.getText());
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ImageCache.cornicePiccola, 0, 0, null);
	}
}