package com.threeamigos.foresta.oggetti;

import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import com.threeamigos.foresta.ui.BufferedImageBuilder;

public enum ClassiOggetto {

	ANELLO(Anello::new, builder()
			.setImmagine("oggetti/Anello.gif")
			.setQuantitaMassima(1)
			.setValore(100)
			),
	COFANO(Cofano::new, builder()
			.setImmagine("oggetti/Cofano.gif")
			.setQuantitaMassima(1)
			.setValore(150)
			),
	CORONA(Corona::new, builder()
			.setImmagine("oggetti/Corona.gif")
			.setQuantitaMassima(2)
			.setValore(100)
			),
	GEMMA(Gemma::new, builder()
			.setImmagine("oggetti/Gemma.gif")
			.setQuantitaMassima(2)
			.setValore(75)
			),
	MONETA(Moneta::new, builder()
			.setImmagine("oggetti/Moneta.gif")
			.setQuantitaMassima(2)
			.setValore(50)
			),
	SCUDO(Scudo::new, builder()
			.setImmagine("oggetti/Scudo.gif")
			.setQuantitaMassima(1)
			.setValore(100)
			),
	SPADA(Spada::new, builder()
			.setImmagine("oggetti/Spada.gif")
			.setQuantitaMassima(1)
			.setValore(100)
			),
	// Gli artefatti non vanno mai restituiti tra gli oggetti che una locazione può nascondere!
	ARTEFATTO(null, builder()
			.setImmagine(null)
			.setQuantitaMassima(1)
			.setValore(100)
			);

	private Supplier<Oggetto> supplier;
	private BufferedImage immagine;
	private int quantitaMassima;
	private int valore;
	
	private ClassiOggetto(Supplier<Oggetto> supplier, Builder builder) {
		this.supplier = supplier;
		this.immagine = BufferedImageBuilder.buildBufferedImage(builder.getImmagine());
		this.quantitaMassima = builder.getQuantitaMassima();
		this.valore = builder.getValore();
	}
	
	public Oggetto getIstanza() {
		return supplier.get();
	}
	
	public BufferedImage getImmagine() {
		return immagine;
	}
	
	public final int getQuantitaMassima() {
		return quantitaMassima;
	}
	
	public final int getValore() {
		return valore;
	}

	private static BuilderStep0 builder() {
		return Builder.istanza();
	}
	
	static interface BuilderStep0 {
		BuilderStep1 setImmagine(String immagine);
	}
	
	static interface BuilderStep1 {
		BuilderStep2 setQuantitaMassima(int quantitaMassima);
	}
		
	static interface BuilderStep2 {
		Builder setValore(int valore);
	}
	
	static class Builder implements BuilderStep0, BuilderStep1, BuilderStep2 {
		private String immagine;
		private int quantitaMassima;
		private int valore;
		private Builder() {}
		public static BuilderStep0 istanza() {
			return new Builder();
		}
		public BuilderStep1 setImmagine(String immagine) {
			this.immagine = immagine;
			return this;
		}
		public BuilderStep2 setQuantitaMassima(int quantitaMassima) {
			this.quantitaMassima = quantitaMassima;
			return this;
		}
		public Builder setValore(int valore) {
			this.valore = valore;
			return this;
		}
		public String getImmagine() {
			return immagine;
		}
		public int getQuantitaMassima() {
			return quantitaMassima;
		}
		public int getValore() {
			return valore;
		}
	}
}
