package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.RegistroArtefattiMD;
import com.threeamigos.foresta.oggetti.Artefatto;

public class RegistroArtefatti {

	private RegistroArtefatti() {
	}

	private static RegistroArtefattiMD registroMD = ModelloDati.getIstanza().getRegistroArtefattiMD();

	private static final String IMPUGNA = "impugna";
	private static final String BRANDISCE = "brandisce";
	private static final String INDOSSA = "indossa";
	private static final String PORTA = "porta al braccio";
	private static final String HA_CON_SE = "ha con se";
	private static final String POSSIEDE = "possiede";

	private static final String COMBATTIMENTO = "il cui potere e' nel combattimento";
	private static final String PROTEZIONE = "che protegge dagli attacchi avversari";
	private static final String PERSUASIONE = "il cui potere e' nella persuasione";
	private static final String MAGIA = "che aumenta il potere magico";

	static final void reimposta() {
		registroMD.reimposta();

		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("il Pugnale di Worr")
				.setDescrizione(COMBATTIMENTO)
				.setUtilizzo(IMPUGNA)
				.setCostoAcquisto(15)
				.setForza(10)
				.setValore(5)
				.setCoraggio(5)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("l'Ascia di Thrann")
				.setDescrizione(COMBATTIMENTO)
				.setUtilizzo(IMPUGNA)
				.setCostoAcquisto(15)
				.setForza(15)
				.setValore(5)
				.setCoraggio(5)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("la Daga di Yltrim")
				.setDescrizione(COMBATTIMENTO)
				.setUtilizzo(BRANDISCE)
				.setCostoAcquisto(20)
				.setForza(20)
				.setValore(10)
				.setCoraggio(10)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("la Spada di Kartham")
				.setDescrizione(COMBATTIMENTO)
				.setUtilizzo(BRANDISCE)
				.setCostoAcquisto(25)
				.setForza(25)
				.setValore(10)
				.setCoraggio(10)
				.getArtefatto());

		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("il Manto di Grakk")
				.setDescrizione(PROTEZIONE)
				.setUtilizzo(INDOSSA)
				.setCostoAcquisto(5)
				.setStanchezza(1)
				.setProtezione(5)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("l'Elmo di Mithrr")
				.setDescrizione(PROTEZIONE)
				.setUtilizzo(INDOSSA)
				.setCostoAcquisto(10)
				.setCarisma(1)
				.setProtezione(10)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("lo Scudo di Kalar")
				.setDescrizione(PROTEZIONE)
				.setUtilizzo(PORTA)
				.setCostoAcquisto(15)
				.setCarisma(1)
				.setProtezione(15)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("la Corazza di Yar")
				.setDescrizione(PROTEZIONE)
				.setUtilizzo(INDOSSA)
				.setCostoAcquisto(20)
				.setProtezione(20)
				.getArtefatto());

		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("il Talismano di Beltram")
				.setDescrizione(PERSUASIONE)
				.setUtilizzo(HA_CON_SE)
				.setCostoAcquisto(10)
				.setCarisma(2)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("il Sigillo di Yshtalar")
				.setDescrizione(PERSUASIONE)
				.setUtilizzo(POSSIEDE)
				.setCostoAcquisto(15)
				.setCarisma(3)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("la Serpe di Yalar")
				.setDescrizione(PERSUASIONE)
				.setUtilizzo(HA_CON_SE)
				.setCostoAcquisto(20)
				.setCarisma(4)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("il Flagello di Mutr")
				.setDescrizione(PERSUASIONE)
				.setUtilizzo(POSSIEDE)
				.setCostoAcquisto(25)
				.setCarisma(5)
				.getArtefatto());

		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("la Bacchetta di Yuw")
				.setDescrizione(MAGIA)
				.setUtilizzo(IMPUGNA)
				.setCostoAcquisto(10)
				.setMagia(5)
				.setBersagli(1)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("la Verga di Pannk")
				.setDescrizione(MAGIA)
				.setUtilizzo(IMPUGNA)
				.setCostoAcquisto(15)
				.setMagia(10)
				.setBersagli(2)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("il Libro di Menk")
				.setDescrizione(MAGIA)
				.setUtilizzo(POSSIEDE)
				.setCostoAcquisto(20)
				.setMagia(15)
				.setBersagli(3)
				.getArtefatto());
		aggiungiArtefatto(Artefatto.getCostruttore()
				.setNome("il Bastone di Plarr")
				.setDescrizione(MAGIA)
				.setUtilizzo(IMPUGNA)
				.setCostoAcquisto(25)
				.setMagia(20)
				.setBersagli(4)
				.getArtefatto());
	}

	static Artefatto getArtefattoDisponibile() {
		return costruisciArtefatto(registroMD.getArtefattoDisponibile());
	}

	public static RegistroArtefattiMD.ArtefattoESuaUbicazione getArtefattoCasuale() {
		return registroMD.getArtefattoCasuale();
	}

	public static final void addArtefattoInLocazione(Artefatto artefatto, CoordinateMD coordinate) {
		registroMD.addArtefattoInLocazione(artefatto.getModelloDati(), coordinate);
	}

	public static final Artefatto getArtefattoInLocazione(CoordinateMD coordinate) {
		return costruisciArtefatto(registroMD.getArtefattoInLocazione(coordinate));
	}

	public static final void rimuoviArtefattoInLocazione(CoordinateMD coordinate) {
		registroMD.rimuoviArtefattoInLocazione(coordinate);
	}

	private static final void aggiungiArtefatto(Artefatto artefatto) {
		registroMD.aggiungiArtefatto(artefatto.getModelloDati());
	}

	private static final Artefatto costruisciArtefatto(ArtefattoMD modelloDati) {
		if (modelloDati == null) {
			return null;
		}
		return new Artefatto(modelloDati);
	}
}
