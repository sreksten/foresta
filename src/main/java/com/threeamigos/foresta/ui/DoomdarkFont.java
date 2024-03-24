package com.threeamigos.foresta.ui;

public interface DoomdarkFont {

	public int getHeight();

	public int getSpacing();

	public int getPadding();
	
	public int getDataWidthInBytes();
	
	public int getGlyphWidth(char c);

	public byte[] getGlyphData(char c);

	public class UnsupportedCharacterException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;

		public UnsupportedCharacterException(char c) {
			super("Unsupported character: " + c + " [" + (int)c + "]");
		}
		
	}
	
}
