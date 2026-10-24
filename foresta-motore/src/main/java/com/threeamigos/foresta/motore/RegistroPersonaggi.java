package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.locazioni.Bottega;
import com.threeamigos.foresta.modellodati.*;
import com.threeamigos.foresta.personaggi.*;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.tipi.PersonaggioNoto;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * I personaggi del mondo che non stanno nel gruppo del giocatore: quelli da distribuire nelle locazioni, quelli già posati nelle caselle e i residenti,
 * cioè i personaggi con un nome proprio nel mondo (il bardo, gli artigiani delle città, i campioni delle sfide: vedi nomi.md). I dati stanno nel
 * {@code RegistroPersonaggiMD}.
 */
public class RegistroPersonaggi {

	private RegistroPersonaggi() {
	}

	private static RegistroPersonaggiMD getRegistroMD() {
		return ModelloDati.getIstanza().getRegistroPersonaggiMD();
	}

	static void reimposta() {
		getRegistroMD().reimposta();
		
		aggiungiPersonaggio(new Guerriero("Reginald", 1));
		aggiungiPersonaggio(new Guerriera("Elleran", 1));
		aggiungiPersonaggio(new Guerriero("Roderick", 1));
		aggiungiPersonaggio(new Guerriera("Wendy", 1));

		aggiungiPersonaggio(new Ladro("Gabriel", 1));
		aggiungiPersonaggio(new Ladra("Filean", 1));
		aggiungiPersonaggio(new Ladro("Raven", 1));
		aggiungiPersonaggio(new Ladra("Wyan", 1));

		aggiungiPersonaggio(new Bardo("Samuel", 1));
		aggiungiPersonaggio(new Cantastorie("Gwendolyn", 1));

		aggiungiPersonaggio(new Elfo("Lamiel", 1));
		aggiungiPersonaggio(new Elfa("Yluviel", 1));

		aggiungiPersonaggio(new Mago("Merlin", 1));
		aggiungiPersonaggio(new Maga("LeFey", 1));

		aggiungiPersonaggio(new Sacerdote(ProduttoreDiMissioniCasuale.nomeSacerdote(), 1));
		aggiungiPersonaggio(new Sacerdotessa(ProduttoreDiMissioniCasuale.nomeSacerdotessa(), 1));

		aggiungiPersonaggio(new Chierico("Frederick", 1));
		aggiungiPersonaggio(new Chierica("Elowen", 1));

		aggiungiPersonaggio(new Druido("Alder", 1));
		aggiungiPersonaggio(new Druida("Morla", 1));
	}

	/**
	 * Uno a caso fra quelli ancora da sistemare nella foresta, tolto dall'elenco; null se sono finiti.
	 */
	static Personaggio getPersonaggioDisponibile() {
		int disponibili = getRegistroMD().getNumeroDisponibili();
		if (disponibili == 0) {
			return null;
		}
		return costruisciPersonaggio(getRegistroMD().rimuoviDisponibile(Dado.tiraAncheAUnaFaccia(disponibili) - 1));
	}

	static int getNumeroPersonaggiDisponibili() {
		return getRegistroMD().getNumeroDisponibili();
	}

	/**
	 * Uno a caso fra quelli che aspettano in una locazione, tolto dalla sua locazione; null se non ce ne sono.
	 */
	static Personaggio getPersonaggioCasuale() {
		List<CoordinateMD> ubicazioni = new ArrayList<>(getRegistroMD().getUbicazioniPersonaggi());
		if (ubicazioni.isEmpty()) {
			return null;
		}
		CoordinateMD coordinate = ubicazioni.get(Dado.tiraAncheAUnaFaccia(ubicazioni.size()) - 1);
		PersonaggioMD personaggio = getRegistroMD().getPersonaggioInLocazione(coordinate);
		getRegistroMD().rimuoviPersonaggioInLocazione(coordinate);
		return costruisciPersonaggio(personaggio);
	}

	/**
	 * Il compagno che entra nel gruppo. Quelli del registro nascono al livello 1 insieme al mondo: chi entra arriva
	 * al livello del mondo meno un numero a caso fra 0 e 2 (mai sotto 1), con l'esperienza di quel livello e i punti
	 * abilità da spendere (vedi Personaggio.impostaLivelloDopoReclutamento), e con l'equipaggiamento di base della sua
	 * classe. È lo stesso personaggio del registro, non uno nuovo: resta il suo modello dati, e con lui il nome e il
	 * fatto di essere un personaggio giocante che accumula esperienza.
	 */
	public static Personaggio preparaCompagno(Personaggio compagno) {
		// Prima il livello, poi la dotazione: i pezzi non possono superare il livello di chi li porta
		compagno.impostaLivelloDopoReclutamento(EquipaggiamentoIniziale.livelloCasualeDalMondo());
		EquipaggiamentoIniziale.equipaggia(compagno);
		return compagno;
	}

	/**
	 * Mette il personaggio nella casella indicata.
	 */
	public static void addPersonaggioInLocazione(Personaggio personaggio, CoordinateMD coordinate) {
		getRegistroMD().addPersonaggioInLocazione(personaggio.getModelloDati(), coordinate);
	}
	
	public static Personaggio getPersonaggioInLocazione(CoordinateMD coordinate) {
		return costruisciPersonaggio(getRegistroMD().getPersonaggioInLocazione(coordinate));
	}

	/**
	 * Toglie il personaggio dalla casella.
	 */
	public static void rimuoviPersonaggioInLocazione(CoordinateMD coordinate) {
		getRegistroMD().rimuoviPersonaggioInLocazione(coordinate);
	}
	
	// -- I personaggi del mondo con un nome proprio (vedi tipi.PersonaggioNoto e nomi.md) --

	/**
	 * Alla creazione del mondo: il borgomastro, il capitano delle guardie e i quattro artigiani di quella città, ognuno con
	 * il suo nome (tutti diversi fra loro e da quelli già usciti) e la provenienza.
	 */
	static void creaIResidentiDellaCitta(TipoLocazione citta) {
		for (PersonaggioNoto noto : PersonaggioNoto.values()) {
			if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA) {
				getResidente(noto, citta);
			}
		}
	}

	/**
	 * Alla creazione del mondo: il bardo della locanda, che viaggia di locanda in locanda ma è sempre lui, e sua moglie.
	 */
	static void creaIlBardoESuaMoglie() {
		getResidente(PersonaggioNoto.BARDO_DELLE_LOCANDE);
		getResidente(PersonaggioNoto.MOGLIE_DEL_BARDO);
	}

	/**
	 * Chi è nel mondo in un solo esemplare o compare al volo (il bardo, sua moglie, la levatrice): se non c'è ancora gli
	 * si dà un nome.
	 */
	public static ResidenteMD getResidente(PersonaggioNoto noto) {
		if (noto.getAmbito() != PersonaggioNoto.Ambito.GLOBALE && noto.getAmbito() != PersonaggioNoto.Ambito.COMPARSA) {
			throw new IllegalArgumentException(noto + " non è unico: serve la città (vedi getResidente(PersonaggioNoto, TipoLocazione))");
		}
		return residente(noto, null, null);
	}

	/**
	 * Chi è di una città (il borgomastro, il capitano, l'alchimista, l'armaiolo, il venditore di pergamene, l'incantatore),
	 * o chi apre una bottega nella Foresta con la missione di quella città: si crea alla prima richiesta.
	 */
	public static ResidenteMD getResidente(PersonaggioNoto noto, TipoLocazione citta) {
		if (noto.getAmbito() != PersonaggioNoto.Ambito.PER_CITTA && noto.getAmbito() != PersonaggioNoto.Ambito.PER_NEGOZIO) {
			throw new IllegalArgumentException(noto + " non è di una città");
		}
		return residente(noto, citta.name(), citta.getNomeDellaCitta());
	}

	private static ResidenteMD residente(PersonaggioNoto noto, String dettaglio, String provenienza) {
		String chiave = noto.chiave(dettaglio);
		ResidenteMD residente = getRegistroMD().getResidente(chiave);
		if (residente == null) {
			if (noto.getProduzioneNomi() == null) {
				throw new IllegalStateException(noto + " non ha un nome da nomi.txt: lo decide chi lo crea");
			}
			residente = new ResidenteMD(ProduttoreDiMissioniCasuale.nomeDiPersonaggioNoto(noto.getProduzioneNomi()),
					noto.isConProvenienza() ? provenienza : null, noto.getTipo());
			getRegistroMD().aggiungiResidente(chiave, residente);
		}
		return residente;
	}

	/**
	 * Il nome come lo si legge nelle scene: "Mastro Ferruccio da Ruuna" per chi è di una città, il solo nome per gli altri.
	 */
	public static String getNomeCompleto(ResidenteMD residente) {
		if (residente.getProvenienza() == null) {
			return residente.getNome();
		}
		return residente.getNome() + " " + Articoli.conPreposizione("da", residente.getProvenienza());
	}

	/**
	 * Il nome completo del personaggio di quel mestiere (armaiolo, alchimista...) della città in cui è il gruppo, o il
	 * negoziante della bottega in cui è; null se il gruppo non è in una città né in una bottega o nessuna voce di
	 * PersonaggioNoto ha quel mestiere.
	 */
	public static String getNomeDelPersonaggioDiCitta(TipoPersonaggio mestiere) {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		TipoLocazione luogo = gruppo.getTipoLocazioneCorrente();
		if (luogo != null && luogo.isBottega()) {
			return Bottega.getNegoziante(Foresta.getLocazioneMD(gruppo.getCoordinate()));
		}
		if (luogo == null || luogo.getNomeDellaCitta() == null) {
			return null;
		}
		for (PersonaggioNoto noto : PersonaggioNoto.values()) {
			if (noto.getAmbito() == PersonaggioNoto.Ambito.PER_CITTA && noto.getTipo() == mestiere) {
				return getNomeCompleto(getResidente(noto, luogo));
			}
		}
		return null;
	}

	/**
	 * Il personaggio di chi ha una classe, con il suo nome proprio (non la provenienza), a livello 1 e senza equipaggiamento:
	 * per le scene e per le missioni che lo portano con il gruppo.
	 */
	public static Personaggio costruisciPersonaggio(ResidenteMD residente) {
		return costruisciPersonaggio(residente, 1);
	}

	/**
	 * Come {@link #costruisciPersonaggio(ResidenteMD)}, ma del livello indicato (per esempio quello del mondo, per chi
	 * viaggia con il gruppo come ospite).
	 */
	public static Personaggio costruisciPersonaggio(ResidenteMD residente, int livello) {
		if (residente.getClasse() == null) {
			throw new IllegalStateException(residente.getNome() + " non ha ancora una classe");
		}
		Personaggio personaggio = FabbricaPersonaggi.crea(residente.getClasse(), livello);
		personaggio.getModelloDati().setNome(residente.getNome());
		return personaggio;
	}

	/**
	 * Un campione di una sfida (vedi LaSfidaDeiCampioni), con il nome e l'epiteto che gli ha dato la missione. Resta nel
	 * registro anche dopo essere stato battuto.
	 */
	public static void registraCampione(String nome, TipoPersonaggio classe) {
		getRegistroMD().aggiungiResidente(PersonaggioNoto.CAMPIONE.chiave(nome), new ResidenteMD(nome, null, classe));
	}

	/**
	 * Segna come sconfitto il campione di quel nome.
	 */
	public static void segnaCampioneSconfitto(String nome) {
		ResidenteMD campione = getRegistroMD().getResidente(PersonaggioNoto.CAMPIONE.chiave(nome));
		if (campione != null) {
			campione.setSconfitto(true);
		}
	}

	/**
	 * I campioni incontrati finora, nell'ordine in cui sono stati registrati.
	 */
	public static List<ResidenteMD> getCampioni() {
		List<ResidenteMD> campioni = new ArrayList<>();
		for (Map.Entry<String, ResidenteMD> voce : getRegistroMD().getResidenti().entrySet()) {
			if (voce.getKey().startsWith(PersonaggioNoto.CAMPIONE.chiave(""))) {
				campioni.add(voce.getValue());
			}
		}
		return campioni;
	}

	private static void aggiungiPersonaggio(Personaggio personaggio) {
		getRegistroMD().aggiungiPersonaggio(personaggio.getModelloDati());
	}
	
	private static Personaggio costruisciPersonaggio(PersonaggioMD modelloDati) {
		if (modelloDati == null) {
			return null;
		}
		Personaggio personaggio = FabbricaPersonaggi.crea(modelloDati.getClasse(), 1);
		personaggio.setModelloDati(modelloDati);
		return personaggio;
	}
}
