package com.threeamigos.foresta.ui;

import java.awt.image.IndexColorModel;

public class DoomdarkColorModel {

	public enum Color {
		WHITE,
		LIGHT_GRAY,
		MEDIUM_GRAY,
		DARK_GRAY,
		VERY_DARK_GRAY,
		BLACK,
		RED,
		YELLOW,
		GREEN,
	}
	
	private static final byte[] whiteColorMap = new byte[] { 0, (byte)0xFF };
	private static final byte[] lightGrayColorMap = new byte[] { 0, (byte)0xAD };
	private static final byte[] mediumGrayColorMap = new byte[] { 0, (byte)0x73 };
	private static final byte[] darkGrayColorMap = new byte[] { 0, (byte)0x52 };
	private static final byte[] veryDarkGrayColorMap = new byte[] { 0, (byte)0x22 };
	private static final byte[] blackColorMap = new byte[] { 0, 0 };

	private static final IndexColorModel whiteIndexColorModel = new IndexColorModel(8, 2, whiteColorMap, whiteColorMap, whiteColorMap, 0);
	private static final IndexColorModel lightGrayIndexColorModel = new IndexColorModel(8, 2, lightGrayColorMap, lightGrayColorMap, lightGrayColorMap, 0);
	private static final IndexColorModel mediumGrayIndexColorModel = new IndexColorModel(8, 2, mediumGrayColorMap, mediumGrayColorMap, mediumGrayColorMap, 0);
	private static final IndexColorModel darkGrayIndexColorModel = new IndexColorModel(8, 2, darkGrayColorMap, darkGrayColorMap, darkGrayColorMap, 0);
	private static final IndexColorModel veryDarkGrayIndexColorModel = new IndexColorModel(8, 2, veryDarkGrayColorMap, veryDarkGrayColorMap, veryDarkGrayColorMap, 0);
	private static final IndexColorModel blackIndexColorModel = new IndexColorModel(8, 2, blackColorMap, blackColorMap, blackColorMap, 0);
	private static final IndexColorModel redIndexColorModel = new IndexColorModel(8, 2, whiteColorMap, blackColorMap, blackColorMap, 0);
	private static final IndexColorModel yellowIndexColorModel = new IndexColorModel(8, 2, whiteColorMap, whiteColorMap, blackColorMap, 0);
	private static final IndexColorModel greenIndexColorModel = new IndexColorModel(8, 2, blackColorMap, whiteColorMap, blackColorMap, 0);

	static final IndexColorModel getColorModel(Color color) {
		switch (color) {
		case WHITE:
			return whiteIndexColorModel;
		case LIGHT_GRAY:
			return lightGrayIndexColorModel;
		case MEDIUM_GRAY:
			return mediumGrayIndexColorModel;
		case DARK_GRAY:
			return darkGrayIndexColorModel;
		case VERY_DARK_GRAY:
			return veryDarkGrayIndexColorModel;
		case BLACK:
			return blackIndexColorModel;
		case RED:
			return redIndexColorModel;
		case YELLOW:
			return yellowIndexColorModel;
		case GREEN:
			return greenIndexColorModel;
		}
		return null;
	}
}
