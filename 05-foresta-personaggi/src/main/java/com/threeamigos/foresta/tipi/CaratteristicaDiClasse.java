package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;
import java.util.Locale;
import java.util.Objects;

/**
 * Una caratteristica generale di una classe di personaggi, quella che il bestiario scopre a poco a poco: come reagisce a un tipo di danno
 * (debole, resistente, immune), se è immune a un incantesimo o a un effetto di stato. Sono dati di {@link TipoPersonaggio}, uguali per tutti i
 * personaggi della classe: le immunità di un singolo personaggio (un goblin particolare di una missione) non ne fanno parte.
 * <p>
 * La {@link #getChiave() chiave} ("DANNO:FUOCO", "INCANTESIMO:MORTE", "EFFETTO:SANGUINAMENTO") è quel che il bestiario salva per dire che la
 * caratteristica è stata scoperta, e non cambia con la lingua. I testi sono in {@code caratteristica_di_classe_<lingua>.properties}.
 *
 * @author Stefano Reksten
 */
public final class CaratteristicaDiClasse {

	/**
	 * Di che cosa è una caratteristica
	 */
	public enum Genere {
		/** Il moltiplicatore del danno di un tipo: 0 immune, meno di 1 resiste, più di 1 è debole */
		DANNO,
		/** L'immunità a un incantesimo, o a tutti */
		INCANTESIMO,
		/** L'immunità a un effetto di stato */
		EFFETTO
	}

	/**
	 * Il valore della chiave di chi è immune a tutti gli incantesimi (il gargoyle)
	 */
	public static final String TUTTI = "TUTTI";

	private static final String SEPARATORE = ":";

	private final Genere genere;
	// Il TipoDanno, il TipoIncantesimo o il TipoEffettoDiStato; null per "tutti gli incantesimi"
	private final Enum<?> valore;
	private final double moltiplicatore;

	private CaratteristicaDiClasse(Genere genere, Enum<?> valore, double moltiplicatore) {
		this.genere = genere;
		this.valore = valore;
		this.moltiplicatore = moltiplicatore;
	}

	public static CaratteristicaDiClasse danno(TipoDanno tipoDanno, double moltiplicatore) {
		return new CaratteristicaDiClasse(Genere.DANNO, Objects.requireNonNull(tipoDanno), moltiplicatore);
	}

	public static CaratteristicaDiClasse immuneAIncantesimo(TipoIncantesimo tipoIncantesimo) {
		return new CaratteristicaDiClasse(Genere.INCANTESIMO, Objects.requireNonNull(tipoIncantesimo), 0.0d);
	}

	public static CaratteristicaDiClasse immuneATuttiGliIncantesimi() {
		return new CaratteristicaDiClasse(Genere.INCANTESIMO, null, 0.0d);
	}

	public static CaratteristicaDiClasse immuneAEffetto(TipoEffettoDiStato tipoEffettoDiStato) {
		return new CaratteristicaDiClasse(Genere.EFFETTO, Objects.requireNonNull(tipoEffettoDiStato), 0.0d);
	}

	/**
	 * La chiave con cui si ricorda che è stata scoperta, per esempio "DANNO:FUOCO"
	 */
	public static String chiave(Genere genere, Enum<?> valore) {
		return genere.name() + SEPARATORE + (valore == null ? TUTTI : valore.name());
	}

	public String getChiave() {
		return chiave(genere, valore);
	}

	public Genere getGenere() {
		return genere;
	}

	/**
	 * Il tipo di danno, l'incantesimo o l'effetto di stato; null per l'immunità a tutti gli incantesimi
	 */
	public Enum<?> getValore() {
		return valore;
	}

	/**
	 * Il moltiplicatore del danno subito, per le caratteristiche di genere DANNO; 0 per le immunità
	 */
	public double getMoltiplicatore() {
		return moltiplicatore;
	}

	/**
	 * Se la classe non subisce niente da quel danno, incantesimo o effetto
	 */
	public boolean isImmunita() {
		return moltiplicatore == 0.0d;
	}

	/**
	 * La caratteristica a parole, per esempio "Debole ai danni di tipo Fuoco (x1,5)" o "Immune all'incantesimo di Morte"
	 */
	public String getDescrizione() {
		switch (genere) {
			case DANNO:
				String nome = ((TipoDanno) valore).getNome();
				if (isImmunita()) {
					return testo("DANNO.IMMUNE", nome, "");
				}
				return testo(moltiplicatore < 1.0d ? "DANNO.RESISTE" : "DANNO.DEBOLE", nome, moltiplicatoreInTesto());
			case INCANTESIMO:
				return valore == null ? testo("INCANTESIMO.TUTTI", "", "") : testo("INCANTESIMO.IMMUNE", ((TipoIncantesimo) valore).getNomeSingolare(), "");
			default:
				return testo("EFFETTO.IMMUNE", ((TipoEffettoDiStato) valore).getDescrizione(), "");
		}
	}

	private String moltiplicatoreInTesto() {
		return numeroInTesto(moltiplicatore);
	}

	/**
	 * Un moltiplicatore a parole, senza gli zeri inutili ("1,50" è "1,5") e con la virgola come in italiano
	 */
	static String numeroInTesto(double numero) {
		// Il formato ha sempre il punto decimale: gli zeri in fondo e il punto rimasto da solo si tolgono
		return String.format(Locale.ROOT, "%.2f", numero).replaceAll("0+$", "").replaceAll("[.]$", "").replace('.', ',');
	}

	private static String testo(String chiave, String valore, String moltiplicatore) {
		return Testi.get("caratteristica_di_classe", chiave).orElse(chiave).replace("{0}", valore).replace("{1}", moltiplicatore);
	}

	@Override
	public boolean equals(Object altro) {
		return altro instanceof CaratteristicaDiClasse && ((CaratteristicaDiClasse) altro).getChiave().equals(getChiave())
				&& ((CaratteristicaDiClasse) altro).moltiplicatore == moltiplicatore;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getChiave(), moltiplicatore);
	}

	@Override
	public String toString() {
		return getChiave() + (genere == Genere.DANNO ? " x" + moltiplicatore : "");
	}
}
