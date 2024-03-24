package com.threeamigos.foresta.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.threeamigos.foresta.motore.Logger;

public class GestoreSalvataggiSuFile extends GestoreSalvataggiBase {

	private static final String NOME_DIRECTORY_SALVATAGGI = ".foresta";
	private static final String POSTFISSO_FILE = ".TXT";

	@Override
	public List<InterfacciaTestataSalvataggio> getSalvataggiDisponibili() {
		File directorySalvataggi = recuperaDirectorySalvataggi();
		List<InterfacciaTestataSalvataggio> salvataggi = new ArrayList<>();
		for (int i = 1; i <= NUMERO_MASSIMO; i++) {
			File salvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + i + POSTFISSO_FILE);
			if (salvataggio.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(salvataggio))) {
					String line = reader.readLine();
					Salvataggio testataSalvataggio = new Salvataggio();
					testataSalvataggio.setId(String.valueOf(i));
					testataSalvataggio.setNome(line);
					salvataggi.add(testataSalvataggio);
				} catch (Exception e) {
					Logger.log(e);
				}
			}
		}
		return salvataggi;
	}

	@Override
	public InterfacciaGestoreSalvataggi.InterfacciaSalvataggio recuperaSalvataggio(String id) {
		File directorySalvataggi = recuperaDirectorySalvataggi();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + id + POSTFISSO_FILE);
		if (fileSalvataggio.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(fileSalvataggio))) {
				String line = reader.readLine();
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					sb.append('\n');
				}
				Salvataggio salvataggio = new Salvataggio();
				salvataggio.setId(fileSalvataggio.getPath());
				salvataggio.setNome(line);
				salvataggio.setContenuto(sb.toString());
				return salvataggio;
			} catch (Exception e) {
				Logger.log(e);
				return null;
			}
		} else {
			Logger.log("Tentativo di lettura di file non esistente: " + id);
			return null;
		}
	}

	@Override
	public void salva(InterfacciaGestoreSalvataggi.InterfacciaSalvataggio salvataggio) {
		File directorySalvataggi = recuperaDirectorySalvataggi();
		File fileSalvataggio = new File(directorySalvataggi.getPath() + File.separatorChar + salvataggio.getId() + POSTFISSO_FILE);
		try (PrintWriter writer = new PrintWriter(new FileWriter(fileSalvataggio))) {
			writer.println(salvataggio.getDescrizione());
			writer.println(salvataggio.getContenuto());
			writer.flush();
		} catch (IOException e) {
			Logger.log(e);
		}
	}

	private final File recuperaDirectorySalvataggi() {
		String homeName = System.getProperty("user.home");
		File homeFile = new File(homeName);
		if (homeFile.isDirectory()) {
			File directorySalvataggi = new File(homeFile.getPath() + File.separatorChar + NOME_DIRECTORY_SALVATAGGI);
			Logger.log("Directory salvataggi: " + directorySalvataggi.getPath());
			if (!directorySalvataggi.exists()) {
				directorySalvataggi.mkdirs();
			}
			return directorySalvataggi;
		} else {
			throw new IllegalArgumentException("user.home non e' una directory");
		}
	}
}
