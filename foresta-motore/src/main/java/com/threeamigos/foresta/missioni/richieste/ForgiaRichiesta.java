package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;
import com.threeamigos.foresta.oggetti.leggendari.OggettoLeggendario;
import com.threeamigos.foresta.tipi.CategoriaArtefatto;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.attributi.tipi.TipoAttributo;
import com.threeamigos.foresta.danni.tipi.TipoDanno;

import java.util.Locale;

/**
 * L'arma che l'armaiolo forgia (vedi LaForgia), letta da una riga di FORGIA in missioni.txt, che ne descrive i campi:
 * il tipo di arma, l'elemento del suo incantamento, l'attributo che fa crescere, il nome e i testi.
 * <p>
 * I valori non stanno nella riga: li calcola {@link #leggendario(int)} dal livello. L'arma forgiata è leggendaria (vedi
 * OggettoLeggendario) ma non è del catalogo e non fa parte di nessun set: si costruisce al volo, come l'oggetto di
 * La sfida dei campioni quando il catalogo non ne ha più.
 */
public final class ForgiaRichiesta {

	// Il bastone magico, come negli altri bastoni (vedi GeneratoreArtefattiTabelle): danno dimezzato e POTERE_MAGICO per livello
	private static final int POTERE_MAGICO_DEL_BASTONE_PER_LIVELLO = 5;
	private static final int COSTO_PER_LIVELLO = 40;
	private static final int PERCENTUALE_PER_LIVELLO = 3;
	private static final int INCANTAMENTO_FISSO_BASE = 4;
	private static final int INCANTAMENTO_FISSO_PER_LIVELLO = 2;
	private static final double INCANTAMENTO_COEFFICIENTE_BASE = 0.10d;
	private static final double INCANTAMENTO_COEFFICIENTE_PER_LIVELLO = 0.01d;

	private final String riga;
	private final String chiave;
	private final TipoArtefatto tipo;
	private final TipoDanno elemento;
	private final TipoAttributo attributo;
	private final String nome;
	private final String nomeBreve;
	private final String descrizione;
	private final double peso;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String rispostaDelMandante;
	private final String consegna;

	private ForgiaRichiesta(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "TIPO", "ELEMENTO", "ATTRIBUTO", "NOME", "BREVE",
				"DESCRIZIONE", "PESO", "RICHIESTA", "BATTUTA", "RISPOSTA", "CONSEGNA");
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoArtefatto.class);
		if (tipo.getCategoria() != CategoriaArtefatto.ARMA) {
			throw new IllegalArgumentException("Si forgia un'arma, non " + tipo + ": " + riga);
		}
		elemento = campi.enumerato("ELEMENTO", TipoDanno.class);
		attributo = campi.enumerato("ATTRIBUTO", TipoAttributo.class);
		if (tipo == TipoArtefatto.BASTONE_MAGICO && attributo != TipoAttributo.POTERE_MAGICO) {
			throw new IllegalArgumentException("Un bastone magico fa crescere il POTERE_MAGICO: " + riga);
		}
		nome = campi.obbligatorio("NOME");
		nomeBreve = campi.obbligatorio("BREVE");
		descrizione = campi.obbligatorio("DESCRIZIONE");
		peso = Double.parseDouble(campi.obbligatorio("PESO"));
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		rispostaDelMandante = campi.obbligatorio("RISPOSTA");
		consegna = campi.obbligatorio("CONSEGNA");
		// Si costruisce una volta qui, così un campo sbagliato si scopre subito
		leggendario(1);
	}

	/**
	 * Legge la riga della grammatica che descrive l'arma.
	 *
	 * @throws IllegalArgumentException se la riga è sbagliata o manca un campo obbligatorio
	 */
	public static ForgiaRichiesta da(String riga) {
		return new ForgiaRichiesta(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	public TipoArtefatto getTipo() {
		return tipo;
	}

	/**
	 * Il nome dell'arma con l'articolo: "la Spada dell'Alba Rossa".
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Come si parla dell'arma, con l'articolo.
	 */
	public String getNomeBreve() {
		return nomeBreve;
	}

	/**
	 * Che cosa vuole forgiare l'armaiolo: la sua prima battuta.
	 */
	public String getRichiesta() {
		return richiesta;
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRispostaDelMandante() {
		return rispostaDelMandante;
	}

	/**
	 * Quello che dice l'armaiolo quando consegna l'arma.
	 */
	public String getConsegna() {
		return consegna;
	}

	/**
	 * Il danno dell'arma forgiata a quel livello: quello medio di un'arma comune del suo tipo (lo spadone, a due mani, più
	 * alto, il bastone magico dimezzato), aumentato.
	 */
	public int getDanni(int livello) {
		double danni = GeneratoreArtefatti.danniMediArma(livello);
		if (tipo == TipoArtefatto.SPADONE) {
			danni *= Costanti.ARTEFATTO_MOLTIPLICATORE_DUE_MANI;
		} else if (tipo == TipoArtefatto.BASTONE_MAGICO) {
			danni /= 2;
		}
		return (int) Math.round(danni * Costanti.FORGIA_MOLTIPLICATORE_DANNI);
	}

	/**
	 * L'arma forgiata a quel livello, come leggendario costruito al volo: il danno aumentato, un modificatore
	 * percentuale all'attributo della riga, il critico e un incantamento dell'elemento della riga. Quando il gruppo
	 * la riceve ha almeno il livello del mondo di allora (vedi OggettoLeggendario.costruisci).
	 */
	public OggettoLeggendario leggendario(int livello) {
		String chiaveDellOggetto = "FORGIA_" + chiave + "_" + livello;
		// Un bastone forgiato tiene il POTERE_MAGICO di un bastone normale e ci aggiunge il suo
		int percentuale = (PERCENTUALE_PER_LIVELLO + (tipo == TipoArtefatto.BASTONE_MAGICO ? POTERE_MAGICO_DEL_BASTONE_PER_LIVELLO : 0)) * livello;
		int critico = 1 + livello / 3;
		int fisso = INCANTAMENTO_FISSO_BASE + INCANTAMENTO_FISSO_PER_LIVELLO * livello;
		double coefficiente = INCANTAMENTO_COEFFICIENTE_BASE + INCANTAMENTO_COEFFICIENTE_PER_LIVELLO * livello;
		String nomeDellIncantamento = "Tempra di " + elemento.getNome().toLowerCase(Locale.ROOT);
		String rigaDellOggetto = "TIPO=" + tipo.name() + ";NOME=" + nome + ";CHIAVE=" + chiaveDellOggetto + ";BREVE=" + nomeBreve
				+ ";DESCRIZIONE=" + descrizione + ";LIVELLO=" + livello + ";DANNI=" + getDanni(livello)
				+ ";COSTO=" + (COSTO_PER_LIVELLO * livello) + ";PESO=" + peso
				+ ";MOD=" + attributo.name() + " +" + percentuale + "%;MOD=CRITICO +" + critico
				+ ";INCANTAMENTO=" + elemento.name() + " " + fisso + " " + String.format(Locale.ROOT, "%.2f", coefficiente)
				+ " " + nomeDellIncantamento
				+ ";LEGGENDA=Avete mai sentito parlare di " + nome + "?;"
				+ "LEGGENDA=L'ha forgiata un armaiolo con quello che i suoi clienti gli hanno portato, e un pizzico di cattivo carattere.";
		return OggettoLeggendario.da(rigaDellOggetto);
	}
}
