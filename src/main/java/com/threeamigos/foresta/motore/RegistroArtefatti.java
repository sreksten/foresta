package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.*;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.tools.CostruttoreArtefatto;

public class RegistroArtefatti {

	private static RegistroArtefattiMD getRegistroArtefatti() {
		return ModelloDati.getIstanza().getRegistroArtefattiMD();
	}

	private RegistroArtefatti() {
	}

	private static final String COMBATTIMENTO = "il cui potere è nel combattimento";
	private static final String PROTEZIONE = "che protegge dagli attacchi avversari";
	private static final String PERSUASIONE = "il cui potere è nella persuasione";
	private static final String MAGIA = "che aumenta il potere magico";

	static void reimposta() {
		getRegistroArtefatti().reimposta();

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("il pugnale di Worr")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(2)
				.setDanniBase(8)
				.setCostoAcquisto(15)
				.setPeso(1)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 25)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ASCIA)
				.setNome("l'Ascia di Thrann")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(3)
				.setDanniBase(12)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 50)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("la Daga di Yltrim")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(4)
				.setDanniBase(10)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 75)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SPADA)
				.setNome("la Spada di Kartham")
				.setDescrizione(COMBATTIMENTO)
				.setLivello(5)
				.setDanniBase(12)
				.setCostoAcquisto(25)
				.setPeso(2)
				.setModificatore(TipoAttributo.FORZA, TipoModificatore.AUMENTO_PERCENTUALE, 100)
				.setModificatore(TipoAttributo.VALORE, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.setModificatore(TipoAttributo.CORAGGIO, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.VESTE)
				.setNome("il Manto di Grakk")
				.setDescrizione(PROTEZIONE)
				.setLivello(1)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.STANCHEZZA, TipoModificatore.AUMENTO_PERCENTUALE, -10)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.setModificatore(TipoAttributo.RESISTENZA_MAGICA, TipoModificatore.AUMENTO_PERCENTUALE, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ELMO)
				.setNome("l'Elmo di Mithrr")
				.setDescrizione(PROTEZIONE)
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.SCUDO)
				.setNome("lo Scudo di Kalar")
				.setDescrizione(PROTEZIONE)
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 1)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.ARMATURA)
				.setNome("la Corazza di Yar")
				.setDescrizione(PROTEZIONE)
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(3)
				.setModificatore(TipoAttributo.PARATA, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Talismano di Beltram")
				.setDescrizione(PERSUASIONE)
				.setLivello(2)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Sigillo di Yshtalar")
				.setDescrizione(PERSUASIONE)
				.setLivello(3)
				.setCostoAcquisto(15)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("la Serpe di Yalar")
				.setDescrizione(PERSUASIONE)
				.setLivello(4)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 4)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.TALISMANO)
				.setNome("il Flagello di Mutr")
				.setDescrizione(PERSUASIONE)
				.setLivello(1)
				.setCostoAcquisto(25)
				.setPeso(1)
				.setModificatore(TipoAttributo.CARISMA, TipoModificatore.AUMENTO_FISSO, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("la Bacchetta di Yuw")
				.setDescrizione(MAGIA)
				.setLivello(1)
				.setCostoAcquisto(10)
				.setPeso(1)
				.setModificatore(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_FISSO, 5)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("la Verga di Pannk")
				.setDescrizione(MAGIA)
				.setLivello(2)
				.setCostoAcquisto(15)
				.setPeso(2)
				.setModificatore(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_PERCENTUALE, 10)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.AUMENTO_FISSO, 2)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.LIBRO_MAGICO)
				.setNome("il Libro di Menk")
				.setDescrizione(MAGIA)
				.setLivello(3)
				.setCostoAcquisto(20)
				.setPeso(1)
				.setModificatore(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_PERCENTUALE, 15)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.AUMENTO_FISSO, 3)
				.costruisci());

		aggiungiArtefatto(CostruttoreArtefatto.istanza()
				.setTipo(TipoArtefatto.BASTONE_MAGICO)
				.setNome("il Bastone di Plarr")
				.setDescrizione(MAGIA)
				.setLivello(4)
				.setCostoAcquisto(25)
				.setPeso(1)
				.setModificatore(TipoAttributo.MAGIA, TipoModificatore.AUMENTO_PERCENTUALE, 20)
				.setModificatore(TipoAttributo.NUMERO_BERSAGLI, TipoModificatore.AUMENTO_FISSO, 4)
				.costruisci());

	}

	static Artefatto getArtefattoDisponibile() {
		return costruisciArtefatto(getRegistroArtefatti().getArtefattoDisponibile());
	}

	public static RegistroArtefattiMD.ArtefattoESuaUbicazione getArtefattoCasuale() {
		return getRegistroArtefatti().getArtefattoCasuale();
	}

	public static void addArtefattoInLocazione(Artefatto artefatto, CoordinateMD coordinate) {
		getRegistroArtefatti().addArtefattoInLocazione(artefatto.getModelloDati(), coordinate);
	}

	public static Artefatto getArtefattoInLocazione(CoordinateMD coordinate) {
		return costruisciArtefatto(getRegistroArtefatti().getArtefattoInLocazione(coordinate));
	}

	public static void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		getRegistroArtefatti().rimuoviArtefattoInLocazione(coordinate);
	}

	private static void aggiungiArtefatto(Artefatto artefatto) {
		getRegistroArtefatti().aggiungiArtefatto(artefatto.getModelloDati());
	}

	private static Artefatto costruisciArtefatto(ArtefattoMD modelloDati) {
		if (modelloDati == null) {
			return null;
		}
		return new Artefatto(modelloDati);
	}

	public static ScambiatoreArtefatti getScambiatorePerLocazione(CoordinateMD coordinate) {
		return getRegistroArtefatti().getScambiatorePerLocazione(coordinate);
	}
}
