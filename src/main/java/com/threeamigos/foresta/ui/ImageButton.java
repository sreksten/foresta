package com.threeamigos.foresta.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImageButton extends JPanel implements MouseListener {

	private static final long serialVersionUID = 1L;
	
	private static final int NO_BORDER = 0;
	private static final int HI_BORDER = 1;
	private static final int CLICK_BORDER = 2;
	private static final int WIDTH = 62;
	private static final int HEIGHT = 64;

	private transient BufferedImage img;
	private int borderstatus = NO_BORDER;
	private transient ActionListener listener;
	private int action;

	public ImageButton(BufferedImage d, ActionListener listener, int action) {
		super();
		this.img = d;
		this.listener = listener;
		this.action = action;
		addMouseListener(this);
		setSize(WIDTH, HEIGHT);
	}

	public int getAction() {
		return action;
	}

	@Override
	public void paintComponent(Graphics g) {
		int sx = WIDTH - 1;
		int sy = HEIGHT - 1;
		g.drawImage(img, 0, 0, null);
		if (borderstatus == HI_BORDER) {
			g.setColor(Color.lightGray);
			g.drawLine(0,0,sx,0);
			g.drawLine(0,0,0,sy);
			g.drawLine(0,sy,sx,sy);
			g.drawLine(sx,sy,sx,0);
		} else if (borderstatus == CLICK_BORDER) {
			g.setColor(Color.white);
			g.drawLine(0,0,sx,0);
			g.drawLine(0,0,0,sy);
			g.drawLine(0,sy,sx,sy);
			g.drawLine(sx,sy,sx,0);
		}
	}

	private void notifyListeners() {
		ActionEvent e = new ActionEvent(this, action, null);
		if (listener != null)
			listener.actionPerformed(e);
	}

	/* MouseListener interface */
	public void mouseClicked(MouseEvent e) {
		borderstatus = NO_BORDER;
		repaint();
		notifyListeners();
	}

	public void mousePressed(MouseEvent e) {
		borderstatus = CLICK_BORDER;
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		borderstatus = NO_BORDER;
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
		borderstatus = HI_BORDER;
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		borderstatus = NO_BORDER;
		repaint();
	}
}
