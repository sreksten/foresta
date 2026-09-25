package com.threeamigos.foresta.oggetti;

import java.util.function.Supplier;

public enum ClassiOggetto {

	ANELLO(Anello::new, builder()
			.setQuantitaMassima(1)
			.setValore(100)
			),
	COFANO(Cofano::new, builder()
			.setQuantitaMassima(1)
			.setValore(150)
			),
	CORONA(Corona::new, builder()
			.setQuantitaMassima(2)
			.setValore(100)
			),
	GEMMA(Gemma::new, builder()
			.setQuantitaMassima(2)
			.setValore(75)
			),
	MONETA(Moneta::new, builder()
			.setQuantitaMassima(2)
			.setValore(50)
			),
	SCUDO(Scudo::new, builder()
			.setQuantitaMassima(1)
			.setValore(100)
			),
	SPADA(Spada::new, builder()
			.setQuantitaMassima(1)
			.setValore(100)
			),
	// Senza immagine (vedi ClassiOggettoImmagine): l'elmo non va tra gli oggetti delle locazioni
	ELMO(Elmo::new, builder()
			.setQuantitaMassima(1)
			.setValore(100)
			),
	// Senza immagine (vedi ClassiOggettoImmagine): l'armatura non va tra gli oggetti delle locazioni
	ARMATURA(Armatura::new, builder()
			.setQuantitaMassima(1)
			.setValore(100)
			),
	// Gli artefatti non vanno mai restituiti tra gli oggetti che una locazione può nascondere!
	ARTEFATTO(null, builder()
			.setQuantitaMassima(1)
			.setValore(100)
			);

	private final Supplier<Oggetto> supplier;
	private final int quantitaMassima;
	private final int valore;
	
	ClassiOggetto(Supplier<Oggetto> supplier, Builder builder) {
		this.supplier = supplier;
		this.quantitaMassima = builder.getQuantitaMassima();
		this.valore = builder.getValore();
	}
	
	public Oggetto getIstanza() {
		return supplier.get();
	}
	
	public final int getQuantitaMassima() {
		return quantitaMassima;
	}
	
	public final int getValore() {
		return valore;
	}

	private static BuilderStep1 builder() {
		return Builder.istanza();
	}
	
	interface BuilderStep1 {
		BuilderStep2 setQuantitaMassima(int quantitaMassima);
	}
		
	interface BuilderStep2 {
		Builder setValore(int valore);
	}
	
	static class Builder implements BuilderStep1, BuilderStep2 {
		private int quantitaMassima;
		private int valore;
		private Builder() {}
		public static BuilderStep1 istanza() {
			return new Builder();
		}
		public BuilderStep2 setQuantitaMassima(int quantitaMassima) {
			this.quantitaMassima = quantitaMassima;
			return this;
		}
		public Builder setValore(int valore) {
			this.valore = valore;
			return this;
		}
		public int getQuantitaMassima() {
			return quantitaMassima;
		}
		public int getValore() {
			return valore;
		}
	}
}
