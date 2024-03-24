package com.threeamigos.foresta.ui;

class DoomdarkFontMedium implements DoomdarkFont {

	private DoomdarkFontMedium() {
	}

	private static final DoomdarkFontMedium instance = new DoomdarkFontMedium();

	public static final DoomdarkFontMedium getInstance() {
		return instance;
	}

	public final int getHeight() {
		return 16;
	}

	public final int getSpacing() {
		return 1;
	}

	public final int getPadding() {
		return 1;
	}

	public final int getDataWidthInBytes() {
		return 2;
	}

	public final int getGlyphWidth(char c) throws UnsupportedCharacterException {
		if (c >= 'a' && c <= 'z') {
			return minuscole[(c - 'a')][0];
		} else if (c >= 'A' && c <= 'Z') {
			return maiuscole[(c - 'A')][0];
		} else if (c >= '0' && c <= '9') {
			return cifre[(c - '0')][0];
		} else if (c == '\'') {
			return apostrofo[0];
		} else if (c == '"') {
			return virgolette[0];
		} else if (c == ',') {
			return virgola[0];
		} else if (c == '.') {
			return punto[0];
		} else if (c == ':') {
			return duepunti[0];
		} else if (c == '-') {
			return meno[0];
		} else if (c == '+') {
			return piu[0];
		} else if (c == '/') {
			return barra[0];
		} else if (c == '?') {
			return punto_interrogativo[0];
		} else if (c == '!') {
			return punto_esclamativo[0];
		} else if (c == ' ') {
			return spazio[0];
		}
		throw new UnsupportedCharacterException(c);
	}

	public final byte[] getGlyphData(char c) throws UnsupportedCharacterException {
		if (c >= 'a' && c <= 'z') {
			return minuscole[c - 'a'];
		} else if (c >= 'A' && c <= 'Z') {
			return maiuscole[c - 'A'];
		} else if (c >= '0' && c <= '9') {
			return cifre[c - '0'];
		} else if (c == '\'') {
			return apostrofo;
		} else if (c == '"') {
			return virgolette;
		} else if (c == ',') {
			return virgola;
		} else if (c == '.') {
			return punto;
		} else if (c == ':') {
			return duepunti;
		} else if (c == '-') {
			return meno;
		} else if (c == '+') {
			return piu;
		} else if (c == '/') {
			return barra;
		} else if (c == '?') {
			return punto_interrogativo;
		} else if (c == '!') {
			return punto_esclamativo;
		} else if (c == ' ') {
			return spazio;
		}
		throw new UnsupportedCharacterException(c);
	}

	private static final byte maiuscole[][] = {

			new byte[] {
					14, // A
					(byte)0b00001111, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11111000,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b11111000,
					(byte)0b11110000, (byte)0b11110000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // B
					(byte)0b11110001, (byte)0b11100000,
					(byte)0b11110111, (byte)0b11111100,
					(byte)0b11111110, (byte)0b01111100,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b11110011, (byte)0b11100000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // C
					(byte)0b00000111, (byte)0b11100000,
					(byte)0b00011111, (byte)0b11111000,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00011100,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b01111000, (byte)0b00011100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b00011111, (byte)0b11111000,
					(byte)0b00000111, (byte)0b11100000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // D
					(byte)0b11110011, (byte)0b11000000,
					(byte)0b11110111, (byte)0b11110000,
					(byte)0b11111100, (byte)0b01111000,
					(byte)0b11111000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11110011, (byte)0b11000000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // E
					(byte)0b00000111, (byte)0b11111100,
					(byte)0b00011111, (byte)0b11111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b11111111, (byte)0b11000000,
					(byte)0b11111111, (byte)0b11000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b00011111, (byte)0b11111100,
					(byte)0b00000111, (byte)0b11111100,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // F
					(byte)0b11110001, (byte)0b11111100,
					(byte)0b11110111, (byte)0b11111100,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b01110000, (byte)0b00000000,
					(byte)0b01110000, (byte)0b00000000,
					(byte)0b00110000, (byte)0b00000000,
					(byte)0b00110000, (byte)0b00000000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // G
					(byte)0b00001111, (byte)0b11000000,
					(byte)0b00011111, (byte)0b11111000,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00011100,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00011100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b11111100,
					(byte)0b11110001, (byte)0b11111100,
					(byte)0b01111011, (byte)0b11111100,
					(byte)0b00111111, (byte)0b10111100,
					(byte)0b00001111, (byte)0b00111100,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // H
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b11110000,
					0, 0,
					0, 0
			},

			new byte[] {
					8, // I
					(byte)0b11111111, (byte)0b00000000,
					(byte)0b11111111, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b11111111, (byte)0b00000000,
					(byte)0b11111111, (byte)0b00000000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // J
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11000000, (byte)0b00111100,
					(byte)0b11000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00011111, (byte)0b11100000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // K
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b11110000,
					(byte)0b11110001, (byte)0b11100000,
					(byte)0b11111111, (byte)0b11000000,
					(byte)0b11111111, (byte)0b11100000,
					(byte)0b11110000, (byte)0b11110000,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // L
					(byte)0b11111100, (byte)0b00000000,
					(byte)0b11111100, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00011100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11110000,
					0, 0,
					0, 0
			},

			new byte[] {
					15, // M
					(byte)0b11000000, (byte)0b00000110,
					(byte)0b11100000, (byte)0b00001110,
					(byte)0b11110000, (byte)0b00011110,
					(byte)0b11111000, (byte)0b00111110,
					(byte)0b11111100, (byte)0b01111110,
					(byte)0b11110111, (byte)0b11111110,
					(byte)0b11110011, (byte)0b10011110,
					(byte)0b11110001, (byte)0b00011110,
					(byte)0b11110000, (byte)0b00011110,
					(byte)0b11110000, (byte)0b00011110,
					(byte)0b11110000, (byte)0b00011110,
					(byte)0b11110000, (byte)0b00011110,
					(byte)0b11110000, (byte)0b00011110,
					(byte)0b11110000, (byte)0b00011110,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b01111000,
			},

			new byte[] {
					14, // N
					(byte)0b11100000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11111110, (byte)0b00111100,
					(byte)0b11111111, (byte)0b00111100,
					(byte)0b11110111, (byte)0b10111100,
					(byte)0b11110011, (byte)0b11111100,
					(byte)0b11110001, (byte)0b11111100,
					(byte)0b11110000, (byte)0b11111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b01111000,
					(byte)0b00000000, (byte)0b11110000,
			},

			new byte[] {
					14, // O
					(byte)0b00000111, (byte)0b10000000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00111100, (byte)0b11110000,
					(byte)0b01111000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b01111000, (byte)0b01111000,
					(byte)0b00111100, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00000111, (byte)0b10000000,
					0, 0,
					0, 0
			},

			new byte[] {
					13, // P
					(byte)0b11110001, (byte)0b11100000,
					(byte)0b11110111, (byte)0b11111000,
					(byte)0b11111110, (byte)0b00111100,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b11110000,
					(byte)0b11110011, (byte)0b11100000,
					(byte)0b11110011, (byte)0b10000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // Q
					(byte)0b00000111, (byte)0b10000000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00111100, (byte)0b11110000,
					(byte)0b01111000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b01111000, (byte)0b01111000,
					(byte)0b00111100, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00000111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000000, (byte)0b00111100,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // R
					(byte)0b11110001, (byte)0b11100000,
					(byte)0b11110111, (byte)0b11111000,
					(byte)0b11111110, (byte)0b00111100,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b11111000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11111111, (byte)0b11100000,
					(byte)0b11110000, (byte)0b11100000,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00011100,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // S
					(byte)0b00011111, (byte)0b11000000,
					(byte)0b01111111, (byte)0b11110000,
					(byte)0b11111000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11111000, (byte)0b00000000,
					(byte)0b01111111, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11111000,
					(byte)0b00000000, (byte)0b01111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b11100000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00011111, (byte)0b11100000,
					0, 0,
					0, 0
			},

			new byte[] {
					16, // T
					(byte)0b11111111, (byte)0b11111111,
					(byte)0b11111111, (byte)0b11111111,
					(byte)0b11110011, (byte)0b11001111,
					(byte)0b11110011, (byte)0b11001111,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // U
					(byte)0b11111111, (byte)0b00111100,
					(byte)0b11111111, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b11111100,
					(byte)0b01111111, (byte)0b10111100,
					(byte)0b00011110, (byte)0b00111100,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // V
					(byte)0b11111111, (byte)0b00111100,
					(byte)0b11111111, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b01111000, (byte)0b01111000,
					(byte)0b00111100, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00001111, (byte)0b11000000,
					(byte)0b00000111, (byte)0b10000000,
					(byte)0b00000011, (byte)0b00000000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // W
					(byte)0b11111111, (byte)0b00111100,
					(byte)0b11111111, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00111100, (byte)0b11110000,
					(byte)0b00011000, (byte)0b01100000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // X
					(byte)0b11111100, (byte)0b11111100,
					(byte)0b11111100, (byte)0b11111100,
					(byte)0b01110000, (byte)0b00111000,
					(byte)0b00111000, (byte)0b01110000,
					(byte)0b00011100, (byte)0b11100000,
					(byte)0b00001111, (byte)0b11000000,
					(byte)0b00000111, (byte)0b10000000,
					(byte)0b00000111, (byte)0b10000000,
					(byte)0b00001111, (byte)0b11000000,
					(byte)0b00011100, (byte)0b11100000,
					(byte)0b00111000, (byte)0b01110000,
					(byte)0b01110000, (byte)0b00111000,
					(byte)0b11111100, (byte)0b11111100,
					(byte)0b11111100, (byte)0b11111100,
					0, 0,
					0, 0
			},

			new byte[] {
					16, // Y
					(byte)0b11111000, (byte)0b00011111,
					(byte)0b11111000, (byte)0b00011111,
					(byte)0b01110000, (byte)0b00001110,
					(byte)0b01110000, (byte)0b00001110,
					(byte)0b00111000, (byte)0b00011100,
					(byte)0b00011100, (byte)0b00111000,
					(byte)0b00001111, (byte)0b11110000,
					(byte)0b00000111, (byte)0b11100000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00001111, (byte)0b11110000,
					(byte)0b00001111, (byte)0b11110000,
					0, 0,
					0, 0
			},

			new byte[] {
					14, // Z
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b01111000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000001, (byte)0b11100000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000111, (byte)0b10000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00011110, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					0, 0,
					0, 0
			}
	};

	private static final byte[][] minuscole = {
			new byte[] {
					14, // a
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00011111, (byte)0b00111100,
					(byte)0b01111111, (byte)0b10111100,
					(byte)0b11111000, (byte)0b11111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11111000, (byte)0b11111100,
					(byte)0b01111111, (byte)0b10111100,
					(byte)0b00011111, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // b
					(byte)0b00001111, (byte)0b11000000,
					(byte)0b00011111, (byte)0b11000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b01111000, (byte)0b00000000,
					(byte)0b11110001, (byte)0b11000000,
					(byte)0b11111100, (byte)0b01111000,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b01111111, (byte)0b11110000,
					(byte)0b00111111, (byte)0b11000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // c
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00111111, (byte)0b11110000,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00111111, (byte)0b11110000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // d
					(byte)0b00000111, (byte)0b11000000,
					(byte)0b00000111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000000, (byte)0b01111000,
					(byte)0b00001110, (byte)0b00111100,
					(byte)0b00111111, (byte)0b10111100,
					(byte)0b11110001, (byte)0b11111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b00111111, (byte)0b11111000,
					(byte)0b00001111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // e
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00011111, (byte)0b11000000,
					(byte)0b01111111, (byte)0b11110000,
					(byte)0b11110000, (byte)0b00111000,
					(byte)0b11110000, (byte)0b00111000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b01111111, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					12, // f
					(byte)0b00110011, (byte)0b11000000,
					(byte)0b00110111, (byte)0b11100000,
					(byte)0b00111100, (byte)0b00110000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b11111111, (byte)0b00000000,
					(byte)0b11111111, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000
			},

			new byte[] {
					14, // g
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00001111, (byte)0b11111100,
					(byte)0b00011111, (byte)0b11111100,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b01111000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b11111100,
					(byte)0b11110001, (byte)0b11111100,
					(byte)0b01111111, (byte)0b10111100,
					(byte)0b00111111, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // h
					(byte)0b00000111, (byte)0b00000000,
					(byte)0b00011111, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b01111000, (byte)0b00000000,
					(byte)0b11110001, (byte)0b11110000,
					(byte)0b11110011, (byte)0b11111000,
					(byte)0b11111110, (byte)0b00111100,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111000,
					(byte)0b11110000, (byte)0b01110000,
					(byte)0b11110000, (byte)0b11100000,
					(byte)0b11110001, (byte)0b11000000,
					(byte)0b11110011, (byte)0b11111000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					4, // i
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // j
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000011, (byte)0b11111000,
					(byte)0b00000000, (byte)0b11100000,
					(byte)0b00000000, (byte)0b01110000,
					(byte)0b00000000, (byte)0b00111000,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b11100000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00011111, (byte)0b11100000
			},

			new byte[] {
					14, // k
					(byte)0b00000111, (byte)0b00000000,
					(byte)0b00011110, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b01111000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00011100,
					(byte)0b11110000, (byte)0b00111000,
					(byte)0b11110000, (byte)0b01110000,
					(byte)0b11110000, (byte)0b11100000,
					(byte)0b11111111, (byte)0b11000000,
					(byte)0b11111111, (byte)0b11000000,
					(byte)0b11110001, (byte)0b11100000,
					(byte)0b11110000, (byte)0b11110000,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					8, // l
					(byte)0b11100000, (byte)0b00000000,
					(byte)0b01110000, (byte)0b00000000,
					(byte)0b00111000, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00011111, (byte)0b00000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // m
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11111100, (byte)0b11100000,
					(byte)0b11111100, (byte)0b11111000,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01110000,
					(byte)0b11110000, (byte)0b11100000,
					(byte)0b11110001, (byte)0b11000000,
					(byte)0b11110011, (byte)0b11111000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // n
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11110011, (byte)0b11100000,
					(byte)0b11110111, (byte)0b11111000,
					(byte)0b11111110, (byte)0b00111100,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111000,
					(byte)0b11110000, (byte)0b01110000,
					(byte)0b11110000, (byte)0b11100000,
					(byte)0b11110001, (byte)0b11000000,
					(byte)0b11110011, (byte)0b11111000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // o
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // p
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11110001, (byte)0b11100000,
					(byte)0b11110111, (byte)0b11111000,
					(byte)0b11111100, (byte)0b01111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11111100, (byte)0b01111100,
					(byte)0b11110111, (byte)0b11111000,
					(byte)0b11110001, (byte)0b11100000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000
			},

			new byte[] {
					14, // q
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00011110, (byte)0b00111100,
					(byte)0b01111111, (byte)0b10111100,
					(byte)0b11111000, (byte)0b11111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11111000, (byte)0b11111100,
					(byte)0b01111111, (byte)0b10111100,
					(byte)0b00011110, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100
			},

			new byte[] {
					14, // r
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11110011, (byte)0b11100000,
					(byte)0b11110111, (byte)0b11111000,
					(byte)0b11111100, (byte)0b00111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11111111, (byte)0b11100000,
					(byte)0b11111111, (byte)0b11100000,
					(byte)0b11110000, (byte)0b01110000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // s
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00011111, (byte)0b11110000,
					(byte)0b01111111, (byte)0b11110000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b01111111, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11111000,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b11111111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // t
					(byte)0b00000001, (byte)0b11000000,
					(byte)0b00000111, (byte)0b00000000,
					(byte)0b00001110, (byte)0b00000000,
					(byte)0b00011100, (byte)0b00000000,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00001100,
					(byte)0b00011111, (byte)0b11111000,
					(byte)0b00000111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // u
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b01111111, (byte)0b00111100,
					(byte)0b00011111, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11111000, (byte)0b11111100,
					(byte)0b01111111, (byte)0b10111100,
					(byte)0b00011111, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // v
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b11110000,
					(byte)0b11110000, (byte)0b11111000,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b01110000, (byte)0b11110000,
					(byte)0b00111111, (byte)0b11100000,
					(byte)0b00011111, (byte)0b11000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // w
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11110011, (byte)0b11111000,
					(byte)0b11110011, (byte)0b11000000,
					(byte)0b11110000, (byte)0b11100000,
					(byte)0b11110000, (byte)0b01110000,
					(byte)0b11110011, (byte)0b00111000,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b01111100, (byte)0b11111000,
					(byte)0b00111100, (byte)0b11100000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // x
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11100000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b00111000, (byte)0b11110000,
					(byte)0b00011101, (byte)0b11100000,
					(byte)0b00001111, (byte)0b11000000,
					(byte)0b00011111, (byte)0b11000000,
					(byte)0b00111101, (byte)0b11100000,
					(byte)0b01111000, (byte)0b11110000,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11100000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // y
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b01111111, (byte)0b00111100,
					(byte)0b00011110, (byte)0b00111100,
					(byte)0b00111100, (byte)0b00111100,
					(byte)0b01111000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b11111000,
					(byte)0b01111111, (byte)0b11111100,
					(byte)0b00111111, (byte)0b11111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b11111111, (byte)0b11110000
			},

			new byte[] {
					14, // z
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11000000, (byte)0b01111100,
					(byte)0b00000000, (byte)0b11111100,
					(byte)0b00001111, (byte)0b11110000,
					(byte)0b00111111, (byte)0b11000000,
					(byte)0b11111100, (byte)0b00001100,
					(byte)0b11111100, (byte)0b00001100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			}
	};

	private static final byte[][] cifre = {

			new byte[] {
					14, // 0
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110011, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					8, // 1
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b11111100, (byte)0b00000000,
					(byte)0b11111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b11111111, (byte)0b00000000,
					(byte)0b11111111, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 2
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b11111000,
					(byte)0b00000011, (byte)0b11110000,
					(byte)0b00001111, (byte)0b11000000,
					(byte)0b00011111, (byte)0b00000000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b01111000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 3
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000001, (byte)0b11100000,
					(byte)0b00000011, (byte)0b11110000,
					(byte)0b00000111, (byte)0b11111000,
					(byte)0b00000000, (byte)0b01111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 4
					(byte)0b00001100, (byte)0b11110000,
					(byte)0b00001100, (byte)0b11110000,
					(byte)0b00011100, (byte)0b11110000,
					(byte)0b00011100, (byte)0b11110000,
					(byte)0b00111000, (byte)0b11110000,
					(byte)0b01111000, (byte)0b11110000,
					(byte)0b11110000, (byte)0b11110000,
					(byte)0b11110000, (byte)0b11110000,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000011, (byte)0b11111100,
					(byte)0b00000011, (byte)0b11111100,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 5
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b00000000, (byte)0b01111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b11100000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00111111, (byte)0b11110000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 6
					(byte)0b00000111, (byte)0b11110000,
					(byte)0b00011111, (byte)0b11110000,
					(byte)0b00111100, (byte)0b00000000,
					(byte)0b01111000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11110000, (byte)0b00000000,
					(byte)0b11111111, (byte)0b11110000,
					(byte)0b11111111, (byte)0b11111000,
					(byte)0b11110000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00111111, (byte)0b11110000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 7
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11111111, (byte)0b11111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b01111000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00000001, (byte)0b11100000,
					(byte)0b00000011, (byte)0b11000000,
					(byte)0b00000111, (byte)0b10000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00001111, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 8
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b01111000, (byte)0b01111000,
					(byte)0b00111111, (byte)0b11110000,
					(byte)0b00111111, (byte)0b11110000,
					(byte)0b01111000, (byte)0b01111000,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			},

			new byte[] {
					14, // 9
					(byte)0b00011111, (byte)0b11100000,
					(byte)0b01111111, (byte)0b11111000,
					(byte)0b11111000, (byte)0b01111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11110000, (byte)0b00111100,
					(byte)0b11111000, (byte)0b00111100,
					(byte)0b01111111, (byte)0b11111100,
					(byte)0b00011111, (byte)0b11111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b00111100,
					(byte)0b00000000, (byte)0b01111000,
					(byte)0b00000000, (byte)0b11110000,
					(byte)0b00111111, (byte)0b11100000,
					(byte)0b01111111, (byte)0b10000000,
					(byte)0b00000000, (byte)0b00000000,
					(byte)0b00000000, (byte)0b00000000
			}
	};

	private static final byte[] apostrofo = {
			6,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b01111100, (byte)0b00000000,
			(byte)0b11111000, (byte)0b00000000,
			(byte)0b11100000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
	};

	private static final byte[] virgolette = {
			14,
			(byte)0b00111100, (byte)0b00111100,
			(byte)0b00111100, (byte)0b00111100,
			(byte)0b00111100, (byte)0b00111100,
			(byte)0b01111100, (byte)0b01111100,
			(byte)0b11111000, (byte)0b11111000,
			(byte)0b11100000, (byte)0b11100000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
	};
	private static final byte[] virgola = {
			6,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00011000, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b11111000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000
	};

	private static final byte[] punto = {
			4,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000
	};

	private static final byte[] duepunti = {
			4,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000
	};

	private static final byte[] punto_interrogativo = {
			14,
			(byte)0b00011111, (byte)0b11100000,
			(byte)0b01111111, (byte)0b11111000,
			(byte)0b11111000, (byte)0b01111100,
			(byte)0b11110000, (byte)0b00111100,
			(byte)0b01110000, (byte)0b00111100,
			(byte)0b00110000, (byte)0b01111100,
			(byte)0b00000011, (byte)0b11111000,
			(byte)0b00000111, (byte)0b11110000,
			(byte)0b00000111, (byte)0b11000000,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000110, (byte)0b00000000,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00000110, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000
	};

	private static final byte[] punto_esclamativo = {
			8,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00011110, (byte)0b00000000,
			(byte)0b00011110, (byte)0b00000000,
			(byte)0b00011110, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b00111000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b01100000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
	};

	private static final byte[] spazio = {
			4,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
	};

	private static final byte[] meno = {
			8,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b11111111, (byte)0b00000000,
			(byte)0b11111111, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000
	};

	private static final byte[] piu = {
			8,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00011000, (byte)0b00000000,
			(byte)0b00011000, (byte)0b00000000,
			(byte)0b00011000, (byte)0b00000000,
			(byte)0b11111111, (byte)0b00000000,
			(byte)0b11111111, (byte)0b00000000,
			(byte)0b00011000, (byte)0b00000000,
			(byte)0b00011000, (byte)0b00000000,
			(byte)0b00011000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000
	};

	private static final byte[] barra = {
			14,
			(byte)0b00000000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00001100,
			(byte)0b00000000, (byte)0b00011100,
			(byte)0b00000000, (byte)0b00111100,
			(byte)0b00000000, (byte)0b01111000,
			(byte)0b00000000, (byte)0b11110000,
			(byte)0b00000001, (byte)0b11100000,
			(byte)0b00000011, (byte)0b11000000,
			(byte)0b00000111, (byte)0b10000000,
			(byte)0b00001111, (byte)0b00000000,
			(byte)0b00011110, (byte)0b00000000,
			(byte)0b00111100, (byte)0b00000000,
			(byte)0b01111000, (byte)0b00000000,
			(byte)0b11110000, (byte)0b00000000,
			(byte)0b11100000, (byte)0b00000000,
			(byte)0b00000000, (byte)0b00000000,
	};
}
