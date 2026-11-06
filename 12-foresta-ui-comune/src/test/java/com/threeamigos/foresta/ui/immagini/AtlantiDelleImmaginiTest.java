package com.threeamigos.foresta.ui.immagini;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Gli atlanti sono aggiornati: ogni immagine originale (in <code>sorgenti-immagini/</code>, che non va nei jar) è nell'atlante con gli stessi
 * pixel, e nell'atlante non c'è altro. Se aggiungi o cambi un'immagine originale e questo test fallisce, rifai l'atlante con
 * <code>python3 risorse_e_documenti_vari/atlanti/costruisci_atlante.py &lt;cartella&gt;</code>.
 */
class AtlantiDelleImmaginiTest {

	private static final Path SORGENTI = Paths.get("sorgenti-immagini");

	@Test
	void ogniImmagineOriginaleStaNellAtlanteConGliStessiPixel() throws IOException {
		Set<Path> cartelle;
		try (Stream<Path> elenco = Files.list(SORGENTI)) {
			cartelle = elenco.filter(Files::isDirectory).collect(Collectors.toSet());
		}
		assertFalse(cartelle.isEmpty());
		for (Path cartella : cartelle) {
			String nomeCartella = cartella.getFileName().toString();
			AtlanteDiImmagini atlante = AtlanteDiImmagini.di(nomeCartella);
			Set<String> originali = new HashSet<>();
			try (Stream<Path> file = Files.list(cartella)) {
				for (Path png : file.filter(p -> p.toString().endsWith(".png")).collect(Collectors.toList())) {
					String nome = png.getFileName().toString().replace(".png", "");
					originali.add(nome);
					ImmagineDiPixel atteso;
					try (InputStream in = Files.newInputStream(png)) {
						atteso = LetturaPng.leggi(in);
					}
					ImmagineDiPixel dallAtlante = atlante.immagine(nome);
					assertNotNull(dallAtlante, nomeCartella + "/" + nome + " non sta nell'atlante");
					assertEquals(atteso.getLarghezza(), dallAtlante.getLarghezza(), nomeCartella + "/" + nome);
					assertEquals(atteso.getAltezza(), dallAtlante.getAltezza(), nomeCartella + "/" + nome);
					for (int y = 0; y < atteso.getAltezza(); y++) {
						for (int x = 0; x < atteso.getLarghezza(); x++) {
							int a = atteso.getArgb(x, y);
							int b = dallAtlante.getArgb(x, y);
							if ((a >>> 24) == 0 && (b >>> 24) == 0) {
								continue; // un pixel trasparente non ha un colore che conti
							}
							assertEquals(Integer.toHexString(a), Integer.toHexString(b), nomeCartella + "/" + nome + " in " + x + "," + y);
						}
					}
				}
			}
			Set<String> inPiu = new HashSet<>(atlante.getNomi());
			inPiu.removeAll(originali);
			assertEquals(new HashSet<String>(), inPiu, "Nell'atlante " + nomeCartella + " ci sono immagini senza originale");
		}
	}

	@Test
	void unaCartellaSenzaAtlanteNonHaNessunaImmagine() {
		assertEquals(0, AtlanteDiImmagini.di("cartella-inventata").getNomi().size());
		assertEquals(null, AtlanteDiImmagini.cerca("cartella-inventata/Niente.png"));
	}
}
