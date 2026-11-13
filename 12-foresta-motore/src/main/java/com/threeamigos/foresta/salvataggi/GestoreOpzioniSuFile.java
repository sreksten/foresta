package com.threeamigos.foresta.salvataggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.interfacce.GestoreOpzioni;
import com.threeamigos.foresta.tipi.PosizioneDellaBarra;
import com.threeamigos.foresta.tipi.StileDellaBarra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Le opzioni del gioco nel file {@code opzioni} della cartella {@code .foresta}: una per riga, {@code NOME=valore} ({@code LINGUA=en-GB},
 * {@code BARRA=SINISTRA}, {@code STILE=DOCK}). Un file che non c'è vuol dire
 * le opzioni predefinite; una riga che non si conosce si ignora.
 */
public final class GestoreOpzioniSuFile extends GestoreSuFile implements GestoreOpzioni {

	private static final String LINGUA = "LINGUA";
	private static final String BARRA = "BARRA";
	private static final String STILE = "STILE";

	private String nomeFile;
	private String lingua;
	private PosizioneDellaBarra posizioneDellaBarra;
	private StileDellaBarra stileDellaBarra;

	/**
	 * Scrive nella cartella {@code .foresta} della cartella dell'utente.
	 */
	public GestoreOpzioniSuFile() {
		super();
	}

	/**
	 * Scrive nella cartella indicata (vedi {@link GestoreSuFile}).
	 */
	public GestoreOpzioniSuFile(File directory) {
		super(directory);
	}

	private String nomeFile() {
		if (nomeFile == null) {
			nomeFile = recuperaDirectory().getPath() + File.separatorChar + "opzioni";
		}
		return nomeFile;
	}

	@Override
	public boolean carica() {
		File file = new File(nomeFile());
		if (!file.exists()) {
			return true;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String riga;
			while ((riga = reader.readLine()) != null) {
				int uguale = riga.indexOf('=');
				if (uguale <= 0) {
					continue;
				}
				String nome = riga.substring(0, uguale).trim();
				String valore = riga.substring(uguale + 1).trim();
				if (LINGUA.equals(nome)) {
					lingua = valore.isEmpty() ? null : valore;
				} else if (BARRA.equals(nome)) {
					posizioneDellaBarra = posizione(valore);
				} else if (STILE.equals(nome)) {
					stileDellaBarra = stile(valore);
				}
			}
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			mettiDaParteIlFileRovinato(file);
			return false;
		}
		return true;
	}

	@Override
	public String getLingua() {
		return lingua;
	}

	@Override
	public void setLingua(String codice) {
		lingua = codice;
	}

	private static PosizioneDellaBarra posizione(String valore) {
		for (PosizioneDellaBarra posizione : PosizioneDellaBarra.values()) {
			if (posizione.name().equals(valore)) {
				return posizione;
			}
		}
		return null;
	}

	private static StileDellaBarra stile(String valore) {
		for (StileDellaBarra stile : StileDellaBarra.values()) {
			if (stile.name().equals(valore)) {
				return stile;
			}
		}
		return null;
	}

	@Override
	public StileDellaBarra getStileDellaBarra() {
		return stileDellaBarra;
	}

	@Override
	public void setStileDellaBarra(StileDellaBarra stile) {
		stileDellaBarra = stile;
	}

	@Override
	public PosizioneDellaBarra getPosizioneDellaBarra() {
		return posizioneDellaBarra;
	}

	@Override
	public void setPosizioneDellaBarra(PosizioneDellaBarra posizione) {
		posizioneDellaBarra = posizione;
	}

	@Override
	public boolean salva() {
		try {
			scriviSostituendo(new File(nomeFile()), writer -> {
				if (lingua != null) {
					writer.println(LINGUA + "=" + lingua);
				}
				if (posizioneDellaBarra != null) {
					writer.println(BARRA + "=" + posizioneDellaBarra.name());
				}
				if (stileDellaBarra != null) {
					writer.println(STILE + "=" + stileDellaBarra.name());
				}
			});
		} catch (Exception e) {
			BusEventi.pubblica(new InternoException(e));
			return false;
		}
		return true;
	}
}
