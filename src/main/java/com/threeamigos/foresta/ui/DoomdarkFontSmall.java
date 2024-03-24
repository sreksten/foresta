package com.threeamigos.foresta.ui;

class DoomdarkFontSmall implements DoomdarkFont {

	private DoomdarkFontSmall() {
	}

	private static final DoomdarkFontSmall instance = new DoomdarkFontSmall();

	public static final DoomdarkFontSmall getInstance() {
		return instance;
	}

	public final int getHeight() {
		return 8;
	}

	public final int getSpacing() {
		return 1;
	}

	public final int getPadding() {
		return 1;
	}
	
	public final int getDataWidthInBytes() {
		return 1;
	}

	public final int getGlyphWidth(char c) {
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
		throw new IllegalArgumentException();
	}

	public final byte[] getGlyphData(char c) {
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
		throw new IllegalArgumentException();
	}

	private static final byte maiuscole[][] = {

			new byte[] {
					7, // A
					(byte)0b00111100,
					(byte)0b01100110,
					(byte)0b11000110,
					(byte)0b11111110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11001100,
					(byte)0b00000000
			},

			new byte[] {
					7, // B
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11000110,
					(byte)0b11111100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // C
					(byte)0b00111100,
					(byte)0b01100110,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b01100110,
					(byte)0b00111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // D
					(byte)0b11011000,
					(byte)0b11101100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11101100,
					(byte)0b11011000,
					(byte)0b00000000
			},

			new byte[] {
					7, // E
					(byte)0b00111110,
					(byte)0b01100110,
					(byte)0b01100000,
					(byte)0b11111000,
					(byte)0b01100000,
					(byte)0b01100110,
					(byte)0b00111110,
					(byte)0b00000000
			},

			new byte[] {
					7, // F
					(byte)0b11011110,
					(byte)0b11100110,
					(byte)0b11000000,
					(byte)0b11111100,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b01000000,
					(byte)0b00000000
			},

			new byte[] {
					7, // G
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11000000,
					(byte)0b11000110,
					(byte)0b11001110,
					(byte)0b11011110,
					(byte)0b01110110,
					(byte)0b00000000
			},

			new byte[] {
					7, // H
					(byte)0b01100110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11111110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11001100,
					(byte)0b00000000
			},

			new byte[] {
					4, // I
					(byte)0b11110000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b11110000,
					(byte)0b00000000
			},

			new byte[] {
					7, // J
					(byte)0b11111110,
					(byte)0b10000110,
					(byte)0b00000110,
					(byte)0b00000110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111110,
					(byte)0b00000000
			},

			new byte[] {
					7, // K
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11001100,
					(byte)0b11111000,
					(byte)0b11001100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b00000000
			},

			new byte[] {
					7, // L
					(byte)0b11100000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b11000110,
					(byte)0b11111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // M
					(byte)0b10000010,
					(byte)0b11000110,
					(byte)0b11101110,
					(byte)0b11010110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b00001100
			},

			new byte[] {
					7, // N
					(byte)0b11000110,
					(byte)0b11100110,
					(byte)0b11110110,
					(byte)0b11011110,
					(byte)0b11001110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b00000110
			},

			new byte[] {
					7, // O
					(byte)0b00111000,
					(byte)0b01101100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01101100,
					(byte)0b00111000,
					(byte)0b00000000
			},

			new byte[] {
					7, // P
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11000110,
					(byte)0b11001100,
					(byte)0b11011000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b00000000
			},

			new byte[] {
					7, // Q
					(byte)0b00111000,
					(byte)0b01101100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01101100,
					(byte)0b00111100,
					(byte)0b00000110
			},

			new byte[] {
					7, // R
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11111100,
					(byte)0b11001100,
					(byte)0b11000110,
					(byte)0b00000000
			},

			new byte[] {
					7, // S
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11000000,
					(byte)0b01111100,
					(byte)0b00000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					8, // T
					(byte)0b11111111,
					(byte)0b11011011,
					(byte)0b00011000,
					(byte)0b00011000,
					(byte)0b00011000,
					(byte)0b00011000,
					(byte)0b00011000,
					(byte)0b00000000
			},

			new byte[] {
					7, // U
					(byte)0b11110110,
					(byte)0b01100110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11001110,
					(byte)0b01110110,
					(byte)0b00000000
			},

			new byte[] {
					7, // V
					(byte)0b11110110,
					(byte)0b01100110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01101100,
					(byte)0b00111000,
					(byte)0b00010000,
					(byte)0b00000000
			},

			new byte[] {
					7, // W
					(byte)0b11110110,
					(byte)0b01100110,
					(byte)0b11000110,
					(byte)0b11010110,
					(byte)0b11010110,
					(byte)0b11111110,
					(byte)0b01101100,
					(byte)0b00000000
			},

			new byte[] {
					7, // X
					(byte)0b11101110,
					(byte)0b11000110,
					(byte)0b01101100,
					(byte)0b00111000,
					(byte)0b01101100,
					(byte)0b11000110,
					(byte)0b11101110,
					(byte)0b00000000
			},

			new byte[] {
					8, // Y
					(byte)0b11000011,
					(byte)0b11000011,
					(byte)0b01100110,
					(byte)0b00111100,
					(byte)0b00011000,
					(byte)0b00011000,
					(byte)0b00111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // Z
					(byte)0b11111110,
					(byte)0b00000110,
					(byte)0b00001100,
					(byte)0b00011000,
					(byte)0b00110000,
					(byte)0b01100110,
					(byte)0b11111110,
					(byte)0b00000000
			}
	};

	private static final byte[][] minuscole = {

			new byte[] {
					7, // a
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b01110110,
					(byte)0b11001110,
					(byte)0b11000110,
					(byte)0b11001110,
					(byte)0b01110110,
					(byte)0b00000000
			},

			new byte[] {
					7, // b
					(byte)0b00111000,
					(byte)0b01100000,
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // c
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11000000,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // d
					(byte)0b00111000,
					(byte)0b00001100,
					(byte)0b01110110,
					(byte)0b11001110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // e
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11111110,
					(byte)0b11000000,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // f
					(byte)0b00101100,
					(byte)0b00110010,
					(byte)0b00110000,
					(byte)0b01111000,
					(byte)0b00110000,
					(byte)0b00110000,
					(byte)0b00110000,
					(byte)0b01100000
			},

			new byte[] {
					7, // g
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b00111110,
					(byte)0b01100000,
					(byte)0b11000110,
					(byte)0b11001110,
					(byte)0b01110110,
					(byte)0b00000000
			},

			new byte[] {
					7, // h
					(byte)0b00111000,
					(byte)0b01100000,
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11000110,
					(byte)0b11001100,
					(byte)0b11011110,
					(byte)0b00000000
			},

			new byte[] {
					2, // i
					(byte)0b11000000,
					(byte)0b00000000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b11000000,
					(byte)0b00000000
			},

			new byte[] {
					7, // j
					(byte)0b00000110,
					(byte)0b00000000,
					(byte)0b00011110,
					(byte)0b00001100,
					(byte)0b00000110,
					(byte)0b00000110,
					(byte)0b11000110,
					(byte)0b01111100
			},

			new byte[] {
					7, // k
					(byte)0b00111000,
					(byte)0b01100000,
					(byte)0b11000110,
					(byte)0b11001100,
					(byte)0b11111000,
					(byte)0b11001100,
					(byte)0b11000110,
					(byte)0b00000000
			},

			new byte[] {
					4, // l
					(byte)0b11000000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b00110000,
					(byte)0b00000000
			},

			new byte[] {
					7, // m
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11101100,
					(byte)0b11010110,
					(byte)0b11010110,
					(byte)0b11001100,
					(byte)0b11011110,
					(byte)0b00000000
			},

			new byte[] {
					7, // n
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11001100,
					(byte)0b11001100,
					(byte)0b11011110,
					(byte)0b00000000
			},

			new byte[] {
					7, // o
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // p
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11000110,
					(byte)0b11100110,
					(byte)0b11011100,
					(byte)0b11000000
			},

			new byte[] {
					7, // q
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b01110110,
					(byte)0b11001110,
					(byte)0b11000110,
					(byte)0b11001110,
					(byte)0b01110110,
					(byte)0b00000110
			},

			new byte[] {
					7, // r
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11011100,
					(byte)0b11100110,
					(byte)0b11000110,
					(byte)0b11111100,
					(byte)0b11000110,
					(byte)0b00000000
			},

			new byte[] {
					7, // s
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b01111100,
					(byte)0b11000000,
					(byte)0b01111100,
					(byte)0b00000110,
					(byte)0b11111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // t
					(byte)0b00011100,
					(byte)0b00110000,
					(byte)0b11111110,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100010,
					(byte)0b00111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // u
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11110110,
					(byte)0b01100110,
					(byte)0b11000110,
					(byte)0b11001110,
					(byte)0b01110110,
					(byte)0b00000000
			},

			new byte[] {
					7, // v
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11001100,
					(byte)0b01100110,
					(byte)0b11000110,
					(byte)0b11001100,
					(byte)0b01111000,
					(byte)0b00000000
			},

			new byte[] {
					7, // w
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11011110,
					(byte)0b11001100,
					(byte)0b11010110,
					(byte)0b11010110,
					(byte)0b01101100,
					(byte)0b00000000
			},

			new byte[] {
					7, // x
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11000110,
					(byte)0b01101100,
					(byte)0b00111000,
					(byte)0b01101100,
					(byte)0b11000110,
					(byte)0b00000000
			},

			new byte[] {
					7, // y
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11110110,
					(byte)0b01100110,
					(byte)0b11001100,
					(byte)0b01111110,
					(byte)0b00000110,
					(byte)0b11111100
			},

			new byte[] {
					7, // z
					(byte)0b00000000,
					(byte)0b00000000,
					(byte)0b11111110,
					(byte)0b10001110,
					(byte)0b00111000,
					(byte)0b11100010,
					(byte)0b11111110,
					(byte)0b00000000
			}
	};

	private static final byte[][] cifre = {

			new byte[] {
					7, // 0
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11010110,
					(byte)0b11010110,
					(byte)0b11010110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					4, // 1
					(byte)0b01100000,
					(byte)0b11100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b01100000,
					(byte)0b11110000,
					(byte)0b00000000
			},

			new byte[] {
					7, // 2
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b00001100,
					(byte)0b00110000,
					(byte)0b01100000,
					(byte)0b11000110,
					(byte)0b11111110,
					(byte)0b00000000
			},

			new byte[] {
					7, // 3
					(byte)0b11111110,
					(byte)0b11000110,
					(byte)0b00001100,
					(byte)0b00011100,
					(byte)0b00000110,
					(byte)0b11000110,
					(byte)0b11111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // 4
					(byte)0b00101100,
					(byte)0b01101100,
					(byte)0b01101100,
					(byte)0b11001100,
					(byte)0b11111110,
					(byte)0b00001100,
					(byte)0b00011110,
					(byte)0b00000000
			},

			new byte[] {
					7, // 5
					(byte)0b11111110,
					(byte)0b11000000,
					(byte)0b11111100,
					(byte)0b00000110,
					(byte)0b00000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // 6
					(byte)0b00111100,
					(byte)0b01100000,
					(byte)0b11000000,
					(byte)0b11111100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // 7
					(byte)0b11111110,
					(byte)0b11000110,
					(byte)0b00001100,
					(byte)0b00011000,
					(byte)0b00110000,
					(byte)0b00110000,
					(byte)0b00110000,
					(byte)0b00000000
			},

			new byte[] {
					7, // 8
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111100,
					(byte)0b00000000
			},

			new byte[] {
					7, // 9
					(byte)0b01111100,
					(byte)0b11000110,
					(byte)0b11000110,
					(byte)0b01111110,
					(byte)0b00000110,
					(byte)0b00001100,
					(byte)0b01111000,
					(byte)0b00000000
			}
	};

	private static final byte[] apostrofo = {
			3,
			(byte)0b01100000,
			(byte)0b01100000,
			(byte)0b11000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000
	};

	private static final byte[] virgolette = {
			7,
			(byte)0b01100110,
			(byte)0b01100110,
			(byte)0b11001100,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000
	};

	private static final byte[] virgola = {
			3,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b01100000,
			(byte)0b01100000,
			(byte)0b11000000
	};

	private static final byte[] punto = {
			2,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b11000000,
			(byte)0b11000000,
			(byte)0b00000000
	};

	private static final byte[] duepunti = {
			2,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b11000000,
			(byte)0b11000000,
			(byte)0b00000000,
			(byte)0b11000000,
			(byte)0b11000000,
			(byte)0b00000000
	};

	private static final byte[] punto_interrogativo = {
			7,
			(byte)0b01111100,
			(byte)0b11000110,
			(byte)0b01000110,
			(byte)0b00001100,
			(byte)0b00110000,
			(byte)0b00000000,
			(byte)0b00110000,
			(byte)0b00000000
	};

	private static final byte[] punto_esclamativo = {
			4,
			(byte)0b00110000,
			(byte)0b00110000,
			(byte)0b01100000,
			(byte)0b01100000,
			(byte)0b01100000,
			(byte)0b00000000,
			(byte)0b11000000,
			(byte)0b00000000
	};

	private static final byte[] spazio = {
			4,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000
	};

	private static final byte[] meno = {
			8,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00111110,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00000000
	};

	private static final byte[] piu = {
			8,
			(byte)0b00000000,
			(byte)0b00000000,
			(byte)0b00001000,
			(byte)0b00001000,
			(byte)0b00111110,
			(byte)0b00001000,
			(byte)0b00001000,
			(byte)0b00000000
	};

	private static final byte[] barra = {
			8,
			(byte)0b00000000,
			(byte)0b00000011,
			(byte)0b00000110,
			(byte)0b00001100,
			(byte)0b00011000,
			(byte)0b00110000,
			(byte)0b01100000,
			(byte)0b00000000
	};
}
