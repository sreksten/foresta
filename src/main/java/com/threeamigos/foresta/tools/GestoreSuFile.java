package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Logger;

import java.io.File;

public abstract class GestoreSuFile {

	protected static final String NOME_DIRECTORY = ".foresta";

	protected File recuperaDirectory() {
		String homeName = System.getProperty("user.home");
		File homeFile = new File(homeName);
		if (!homeFile.isDirectory()) {
			throw new IllegalArgumentException("user.home non e' una directory");
		}
		File directory = new File(homeFile.getPath() + File.separatorChar + NOME_DIRECTORY);
		Logger.log("Directory: " + directory.getPath());
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}
}
