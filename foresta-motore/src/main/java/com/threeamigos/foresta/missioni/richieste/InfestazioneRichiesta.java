package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.testi.Numeri;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Chi infesta le rovine di una bottega nella Foresta (vedi IlNegozio), letto da una riga di NEGOZIO_NEMICI in missioni.txt:
 * di che classe sono e quanti.
 */
public final class InfestazioneRichiesta {

	private final TipoPersonaggio nemico;
	private final int numero;

	private InfestazioneRichiesta(String riga) {
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "NEMICO", "NUMERO");
		nemico = campi.enumerato("NEMICO", TipoPersonaggio.class);
		numero = campi.intero("NUMERO");
		if (numero < 1) {
			throw new IllegalArgumentException("Il numero è almeno 1: " + riga);
		}
	}

	/**
	 * L'infestazione descritta da una riga di NEGOZIO_NEMICI.
	 */
	public static InfestazioneRichiesta da(String riga) {
		return new InfestazioneRichiesta(riga);
	}

	public TipoPersonaggio getNemico() {
		return nemico;
	}

	public int getNumero() {
		return numero;
	}

	/**
	 * Chi infesta il posto, per i testi: "una Chimera-drago", "quattro Scheletri".
	 */
	public String getDescrizione() {
		Personaggio modello = FabbricaPersonaggi.modello(nemico);
		if (numero == 1) {
			return modello.getAIS() + modello.getNomeSingolare();
		}
		return Numeri.getCardinaleM(numero) + " " + modello.getNomePlurale();
	}
}
