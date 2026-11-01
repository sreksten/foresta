package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.interfacce.OggettoConArticoli;
import com.threeamigos.foresta.testi.Genere;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;

import java.util.Objects;

/**
 * Il nome di un oggetto che non ha una classe sua, come gli oggetti delle missioni (vedi {@link OggettoMissione}):
 * singolare, plurale e genere (gli articoli li sceglie la lingua in uso), così le descrizioni delle locazioni lo flettono come gli altri oggetti.
 */
public final class NomeOggetto implements OggettoConArticoli {

	private final Nome nome;
	private final Nome nomePlurale;

	/**
	 * @param genere il genere del nome: da lui e da come comincia dipendono gli articoli, che sceglie la lingua in uso
	 */
	public NomeOggetto(String singolare, String plurale, Genere genere) {
		this(singolare, plurale, genere, genere);
	}

	/**
	 * Per un nome che cambia genere al plurale: «l'orecchio» (maschile), «le orecchie» (femminile).
	 */
	public NomeOggetto(String singolare, String plurale, Genere genereSingolare, Genere generePlurale) {
		this.nome = new Nome(Objects.requireNonNull(singolare), Objects.requireNonNull(plurale), genereSingolare, null, null);
		this.nomePlurale = new Nome(singolare, plurale, generePlurale, null, null);
	}

	/**
	 * Un nome femminile: "una radice di mandragola", "le radici di mandragola" (l'articolo si sceglie da come comincia).
	 */
	public static NomeOggetto femminile(String singolare, String plurale) {
		return new NomeOggetto(singolare, plurale, Genere.FEMMINILE);
	}

	/**
	 * Un nome maschile: "un fungo", "i funghi" (l'articolo si sceglie da come comincia).
	 */
	public static NomeOggetto maschile(String singolare, String plurale) {
		return new NomeOggetto(singolare, plurale, Genere.MASCHILE);
	}

	public String getSingolare() {
		return nome.getSingolare();
	}

	public String getPlurale() {
		return nome.getPlurale();
	}

	@Override
	public String getAIS() {
		return Lingue.corrente().articoloIndeterminativo(nome, false);
	}

	@Override
	public String getAIP() {
		return Lingue.corrente().articoloIndeterminativo(nomePlurale, true);
	}

	@Override
	public String getADS() {
		return Lingue.corrente().articoloDeterminativo(nome, false);
	}

	@Override
	public String getADP() {
		return Lingue.corrente().articoloDeterminativo(nomePlurale, true);
	}
}
