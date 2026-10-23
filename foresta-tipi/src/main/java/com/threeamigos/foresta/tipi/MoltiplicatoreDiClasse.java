package com.threeamigos.foresta.tipi;

import java.util.Locale;

/**
 * Il moltiplicatore che una classe di personaggi ha per un attributo (il carico massimo, la furtività...) o per i danni (magici o fisici), con la
 * nota che spiega perché ha quel valore (vedi {@link NoteDeiPersonaggi}). Il bestiario li mostra a fianco del carattere di ogni classe: 1 è la media, di più
 * è un punto di forza, di meno un punto debole.
 *
 * @author Stefano Reksten
 */
public final class MoltiplicatoreDiClasse {

	private final String chiave;
	private final String nome;
	private final double moltiplicatore;
	private final String nota;

	/**
	 * @param chiave         il nome di un {@link TipoAttributo}, o {@link NoteDeiPersonaggi#DANNI_MAGICI} o {@link NoteDeiPersonaggi#DANNI_FISICI}
	 * @param classe         la classe a cui appartiene il valore (per la nota)
	 * @param moltiplicatore il valore che la classe ha per quella chiave
	 */
	public MoltiplicatoreDiClasse(String chiave, TipoPersonaggio classe, double moltiplicatore) {
		this.chiave = chiave;
		this.nome = nomeDi(chiave);
		this.moltiplicatore = moltiplicatore;
		this.nota = NoteDeiPersonaggi.nota(classe, chiave).orElse("");
	}

	private static String nomeDi(String chiave) {
		if (NoteDeiPersonaggi.DANNI_MAGICI.equals(chiave) || NoteDeiPersonaggi.DANNI_FISICI.equals(chiave)) {
			return Testi.get("caratteristica_di_classe", "NOME." + chiave).orElse(chiave);
		}
		return TipoAttributo.valueOf(chiave).getNome();
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Il nome dell'attributo (o dei danni), per i testi: "Carico massimo"
	 */
	public String getNome() {
		return nome;
	}

	public double getMoltiplicatore() {
		return moltiplicatore;
	}

	/**
	 * Il valore a parole, senza zeri inutili e con la virgola: "1,2", "0,5", "1"
	 */
	public String getMoltiplicatoreInTesto() {
		return CaratteristicaDiClasse.numeroInTesto(moltiplicatore);
	}

	/**
	 * Se la classe sta sopra la media (più di 1), sotto (meno di 1) o nella media
	 */
	public int getSegno() {
		return Double.compare(Math.round(moltiplicatore * 100.0d), 100L);
	}

	/**
	 * Perché la classe ha quel valore, in una frase; vuota se non c'è la nota
	 */
	public String getNota() {
		return nota;
	}

	@Override
	public String toString() {
		return nome + " x" + getMoltiplicatoreInTesto();
	}
}
