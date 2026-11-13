package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.interfacce.OggettoConArticoli;
import com.threeamigos.foresta.testi.Lingue;
import com.threeamigos.foresta.testi.Nome;
import com.threeamigos.foresta.testi.Preposizione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il nome di una classe di personaggi con quel che serve per articolarlo, nella lingua in uso: «goblin», «i goblin», «del goblin», «da un
 * goblin». Le parole vengono dai file dei testi della classe (NOME_SINGOLARE, NOME_PLURALE, GENERE, PRONOME... in
 * {@code tipo_personaggio_<lingua>.properties}), le regole (articoli, preposizioni) dalla lingua (vedi RegoleDellaLingua).
 * <p>
 * Lo usa ogni personaggio per il nome della sua classe (PersonaggioBase), e chi deve parlare di una classe senza avere un personaggio in
 * mano: le missioni («Il borgomastro vi chiede di cacciare i goblin»), la vista della partita. Non costruisce nessun personaggio.
 */
public final class NomeDellaClasse implements OggettoConArticoli {

	private final TipoPersonaggio classe;

	private NomeDellaClasse(TipoPersonaggio classe) {
		this.classe = classe;
	}

	/**
	 * Il nome di quella classe
	 */
	public static NomeDellaClasse di(TipoPersonaggio classe) {
		return new NomeDellaClasse(classe);
	}

	// Si rilegge a ogni uso, così segue la lingua in uso anche se cambia
	private Nome nome() {
		return Nome.di(classe);
	}

	/**
	 * Il nome al singolare, senza articolo: «goblin», «maga»
	 */
	public String getNomeSingolare() {
		return nome().getSingolare();
	}

	/**
	 * Il nome al plurale, senza articolo: «goblin», «idre»
	 */
	public String getNomePlurale() {
		return nome().getPlurale();
	}

	@Override
	public String getADS() {
		return Lingue.corrente().articoloDeterminativo(nome(), false);
	}

	@Override
	public String getADP() {
		return Lingue.corrente().articoloDeterminativo(nome(), true);
	}

	@Override
	public String getAIS() {
		return Lingue.corrente().articoloIndeterminativo(nome(), false);
	}

	@Override
	public String getAIP() {
		return Lingue.corrente().articoloIndeterminativo(nome(), true);
	}

	/**
	 * La preposizione «di» con l'articolo determinativo singolare: «del », «della »
	 */
	public String getDeS() {
		return Lingue.corrente().preposizioneArticolata(Preposizione.DI, nome(), false);
	}

	/**
	 * La preposizione «di» con l'articolo determinativo plurale: «dei », «delle »
	 */
	public String getDeP() {
		return Lingue.corrente().preposizioneArticolata(Preposizione.DI, nome(), true);
	}

	/**
	 * La preposizione dell'agente con l'articolo, per «ucciso da»: «da un », «da una », o «dal » per chi è unico (il Drago)
	 */
	public String getDa() {
		return Lingue.corrente().daAgente(nome());
	}

	/**
	 * Il pronome della classe (egli, ella, esso...); vuoto se i testi non ne danno uno
	 */
	public String getPronome() {
		return nome().getPronome().orElse("");
	}
}
