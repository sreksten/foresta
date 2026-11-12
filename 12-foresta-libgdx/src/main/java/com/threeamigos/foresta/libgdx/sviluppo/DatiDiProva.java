package com.threeamigos.foresta.libgdx.sviluppo;

import com.threeamigos.foresta.interfacce.VistaCassa;
import com.threeamigos.foresta.interfacce.VistaGruppo;
import com.threeamigos.foresta.interfacce.VistaGruppoGiocatore;
import com.threeamigos.foresta.interfacce.VistaMissione;
import com.threeamigos.foresta.tipi.MissionePredefinita;
import com.threeamigos.foresta.mappa.VistaMappa;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.interfacce.VistaPartita;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Una partita finta per provare i riquadri senza il motore: personaggi veri (della fabbrica) in un gruppo che risponde solo a quel che i riquadri
 * chiedono. Non è parte del gioco: serve a chi sviluppa.
 */
public final class DatiDiProva {

	private DatiDiProva() {
	}

	/**
	 * Un oggetto che implementa l'interfaccia rispondendo con le funzioni date (per nome di metodo) e, per il resto, con un valore neutro (0,
	 * false, elenco vuoto, null); se c'è un <code>reale</code> gli passa le chiamate che non sono sostituite.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T finto(Class<T> tipo, Object reale, Map<String, Function<Object[], Object>> risposte) {
		InvocationHandler gestore = (proxy, metodo, argomenti) -> {
			Function<Object[], Object> risposta = risposte.get(metodo.getName());
			if (risposta != null) {
				return risposta.apply(argomenti == null ? new Object[0] : argomenti);
			}
			if (reale != null) {
				return metodo.invoke(reale, argomenti);
			}
			Class<?> ritorno = metodo.getReturnType();
			if (ritorno == boolean.class) {
				return false;
			} else if (ritorno == int.class) {
				return 0;
			} else if (ritorno == double.class) {
				return 0d;
			} else if (ritorno == long.class) {
				return 0L;
			} else if (ritorno == List.class) {
				return Collections.emptyList();
			} else if (ritorno == Optional.class) {
				return Optional.empty();
			}
			return null;
		};
		return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, gestore);
	}

	/**
	 * Un personaggio vero, con i dati che servono a mostrare un caduto
	 */
	private static VistaPersonaggio caduto(Personaggio reale, String causa) {
		return finto(VistaPersonaggio.class, reale, new Risposte().con("isVivo", a -> false).con("getCausaTrapasso", a -> causa).mappa);
	}

	/**
	 * Le risposte di un oggetto finto, per nome di metodo
	 */
	private static final class Risposte {
		private final Map<String, Function<Object[], Object>> mappa = new java.util.HashMap<>();

		Risposte con(String metodo, Function<Object[], Object> risposta) {
			mappa.put(metodo, risposta);
			return this;
		}
	}

	/**
	 * Una missione finta: nome, descrizione, classe, se la descrizione è aperta e le sotto-missioni
	 */
	private static VistaMissione missione(String id, MissionePredefinita classe, String nome, String descrizione, boolean aperta, VistaMissione... secondarie) {
		return finto(VistaMissione.class, null, new Risposte()
				.con("getId", a -> id).con("getClasse", a -> classe).con("getNome", a -> nome).con("getDescrizione", a -> descrizione)
				.con("isDescrizioneVisibile", a -> aperta).con("isAttiva", a -> true)
				.con("getMissioniSecondarie", a -> java.util.Arrays.asList(secondarie)).mappa);
	}

	/**
	 * Una partita con un gruppo di sette personaggi (uno caduto) e senza ospiti
	 */
	public static VistaPartita partita() {
		ModelloDati.installa();
		ModelloDati.setIstanza(new ModelloDati());
		TipoPersonaggio[] classi = {TipoPersonaggio.GUERRIERO, TipoPersonaggio.LADRO, TipoPersonaggio.MAGO, TipoPersonaggio.ELFA,
				TipoPersonaggio.SACERDOTE, TipoPersonaggio.BARDO, TipoPersonaggio.CHIERICO};
		int[] livelli = {5, 4, 6, 3, 5, 2, 4};
		List<VistaPersonaggio> personaggi = new ArrayList<>();
		for (int i = 0; i < classi.length; i++) {
			Personaggio pg = FabbricaPersonaggi.crea(classi[i], livelli[i]);
			personaggi.add(i == 3 ? caduto(pg, "Ucciso da un goblin") : pg);
		}
		List<VistaPersonaggio> vivi = new ArrayList<>();
		for (VistaPersonaggio personaggio : personaggi) {
			if (personaggio.isVivo()) {
				vivi.add(personaggio);
			}
		}
		VistaGruppoGiocatore gruppo = finto(VistaGruppoGiocatore.class, null, new Risposte()
				.con("getNumeroPersonaggi", a -> personaggi.size())
				.con("getPersonaggi", a -> personaggi)
				.con("getPersonaggiVivi", a -> vivi)
				.con("getPersonaggio", a -> personaggi.get((Integer) a[0]))
				.con("contiene", a -> personaggi.contains(a[0]))
				.con("getIncantesimi", a -> a[0] == TipoIncantesimo.FUOCO ? 3 : a[0] == TipoIncantesimo.GELO ? 12 : 0)
				.con("getPozioniSalute", a -> 5)
				.con("getPozioniMagia", a -> 2).mappa);
		List<VistaPersonaggio> mostri = new ArrayList<>();
		for (TipoPersonaggio classe : new TipoPersonaggio[]{TipoPersonaggio.GOBLIN, TipoPersonaggio.TROLL, TipoPersonaggio.SCHELETRO}) {
			mostri.add(FabbricaPersonaggi.crea(classe, 3));
		}
		VistaGruppo avversari = finto(VistaGruppo.class, null, new Risposte()
				.con("getNumeroPersonaggi", a -> mostri.size())
				.con("getPersonaggi", a -> mostri)
				.con("getPersonaggiVivi", a -> mostri)
				.con("getPersonaggio", a -> mostri.get((Integer) a[0]))
				.con("contiene", a -> mostri.contains(a[0])).mappa);
		VistaCassa cassa = finto(VistaCassa.class, null, new Risposte().con("getMonete", a -> 1234).con("getPreziosi", a -> 7).mappa);
		List<VistaPersonaggio> inCombattimento = new ArrayList<>();
		for (int i = 0; i < Math.min(3, vivi.size()); i++) {
			inCombattimento.add(vivi.get(i));
			inCombattimento.add(mostri.get(i));
		}
		TipoLocazione[] tipi = {TipoLocazione.BOSCO, TipoLocazione.BOSCO, TipoLocazione.RADURA, TipoLocazione.BOSCO, TipoLocazione.PALUDE,
				TipoLocazione.BOSCO, TipoLocazione.TEMPIO, TipoLocazione.BOSCO, TipoLocazione.LOCANDA, TipoLocazione.GROTTA};
		VistaMappa mappa = finto(VistaMappa.class, null, new Risposte()
				.con("getLocazioneDelGruppo", a -> TipoLocazione.BOSCO)
				.con("getDimensioneX", a -> 20)
				.con("getDimensioneY", a -> 20)
				.con("getPosizioneDelGruppo", a -> new CoordinateMD(10, 10))
				.con("getLocazione", a -> tipi[(((CoordinateMD) a[0]).getX() * 7 + ((CoordinateMD) a[0]).getY() * 3) % tipi.length])
				.con("getVarianteBosco", a -> 1 + (((CoordinateMD) a[0]).getX() + ((CoordinateMD) a[0]).getY()) % 4)
				.con("isLocazioneConosciuta", a -> Math.abs(((CoordinateMD) a[0]).getX() - 10) + Math.abs(((CoordinateMD) a[0]).getY() - 10) < 6)
				.con("isLocazioneVisitata", a -> Math.abs(((CoordinateMD) a[0]).getX() - 10) + Math.abs(((CoordinateMD) a[0]).getY() - 10) < 3)
				.con("getCoordinateDaSegnalare", a -> Collections.singletonList(new CoordinateMD(12, 9))).mappa);
		List<VistaMissione> attive = java.util.Arrays.asList(
				missione("m1", MissionePredefinita.NESSUN_BOCCALE_LASCIATO_INDIETRO, "Nessun boccale lasciato indietro",
						"Il locandiere ha perso la sua scorta di birra: ritrovala prima che scenda la sera, e non farti vedere dai goblin.", true,
						missione("m1a", MissionePredefinita.CACCIA_AI_GOBLIN, "Il magazzino dei goblin", "Trova la grotta dove sono state nascoste le botti.", true)),
				missione("m2", MissionePredefinita.DISTURBATORE_DELLA_QUIETE_PUBBLICA, "Il gallo del mattino", "Fai tacere il gallo del contadino.", false),
				missione("m3", MissionePredefinita.CACCIA_AI_GOBLIN, "Caccia ai goblin", "Uccidi sette goblin nel bosco a nord della città, per una taglia generosa e un po' di gloria.", true),
				missione("m4", MissionePredefinita.CACCIA_AI_GOBLIN, "Una missione molto lunga per provare lo scorrimento", "Descrizione.", false),
				missione("m5", MissionePredefinita.CACCIA_AI_GOBLIN, "Un'altra missione", "Descrizione.", false));
		List<VistaMissione> completate = java.util.Arrays.asList(missione("c1", MissionePredefinita.CACCIA_AI_GOBLIN, "Il ponte rotto", "Fatto.", false));
		List<VistaMissione> fallite = java.util.Arrays.asList(missione("f1", MissionePredefinita.CACCIA_AI_GOBLIN, "La scorta del mercante", "Fallita.", false));
		return finto(VistaPartita.class, null, new Risposte()
				.con("getMissioniAttive", a -> attive)
				.con("getMissioniCompletate", a -> completate)
				.con("getMissioniFallite", a -> fallite)
				.con("getGruppoGiocatore", a -> gruppo)
				.con("getCassa", a -> cassa)
				.con("getPuntiEsperienza", a -> 340)
				.con("getPuntiEsperienzaPerProssimoLivello", a -> 500)
				.con("getOrdineDiCombattimento", a -> inCombattimento)
				.con("getGruppoAvversario", a -> avversari)
				.con("getMappa", a -> mappa)
				.con("getUltimeNotizie", a -> java.util.Arrays.asList(
						new com.threeamigos.foresta.modellodati.NotiziaMD("n1", "Il Drago - ha distrutto la città di Nyena e nessuno è sopravvissuto"),
						new com.threeamigos.foresta.modellodati.NotiziaMD("n2", "Locanda del Cinghiale - il locandiere offre birra gratis a chi porta notizie del Drago")))
				.con("getTipoBottinoInLocazione", a -> TipoBottino.COFANO)
				.con("isTrofeoVinto", a -> ((com.threeamigos.foresta.tipi.TipoTrofeo) a[0]).ordinal() % 3 == 0)
				.con("getObiettivoTrofeo", a -> 10)
				.con("getProgressoTrofeo", a -> ((com.threeamigos.foresta.tipi.TipoTrofeo) a[0]).ordinal() % 10)
				.con("isVoceDelBestiarioSbloccata", a -> ((TipoPersonaggio) a[0]).ordinal() % 2 == 0)
				.con("getCaratteristicheScoperte", a -> Collections.emptySet())
				.con("getStoria", a -> java.util.Arrays.asList(
						"Un tempo la Foresta era un luogo tranquillo, dove i viandanti potevano riposare nelle locande e i mercanti commerciare in pace.",
						"Poi è arrivato il Drago, che ha cominciato a distruggere le città una dopo l'altra, e nessuno ha saputo fermarlo.",
						"Solo un gruppo di avventurieri coraggiosi potrà sconfiggerlo prima che non resti più niente: eccoli, comincia il viaggio.",
						"Attenti ai goblin nel bosco, ai troll nelle grotte e a tutto ciò che si nasconde fra gli alberi dopo il tramonto.",
						"E ricordate: una buona birra e un buon compagno valgono più di mille monete d'oro, ma le monete servono sempre."))
				.con("getGiorno", a -> 12)
				.con("getMostriUccisi", a -> ((TipoPersonaggio) a[0]).ordinal() % 7 == 0 ? 3 : 0)
				.con("getNomeSingolare", a -> ((TipoPersonaggio) a[0]).name().toLowerCase(java.util.Locale.ROOT))
				.con("getNomePlurale", a -> ((TipoPersonaggio) a[0]).name().toLowerCase(java.util.Locale.ROOT) + "i")
				.con("getFinaleDellaSconfitta", a -> java.util.Arrays.asList("Il gruppo è caduto.", "La Foresta resta senza eroi."))
				.con("getFinaleDellaVittoria", a -> java.util.Arrays.asList("Il Drago è stato sconfitto!", "La Foresta è salva."))
				.con("prezzoVendita", a -> (Integer) a[0] / 2)
				.con("prezzoAcquisto", a -> (Integer) a[0])
				.con("isAiutoAbilitato", a -> true).mappa);
	}

	/**
	 * Una classifica finta con otto punteggi
	 */
	public static com.threeamigos.foresta.interfacce.GestorePunteggi punteggi() {
		return new com.threeamigos.foresta.interfacce.GestorePunteggi() {
			@Override
			public boolean carica() {
				return true;
			}

			@Override
			public int getConteggio() {
				return 8;
			}

			@Override
			public com.threeamigos.foresta.strumenti.Punteggio getPunteggio(int posizione) {
				return new com.threeamigos.foresta.strumenti.Punteggio() {
					@Override
					public String getNome() {
						return "Giocatore " + (posizione + 1);
					}

					@Override
					public int getPunteggio() {
						return 90000 - posizione * 7000;
					}

					@Override
					public String getIdPartita() {
						return null;
					}
				};
			}

			@Override
			public boolean isPunteggioInClassifica(int punteggio, String idPartita) {
				return true;
			}

			@Override
			public void addPunteggio(String nome, int punteggio, String idPartita) {
			}

			@Override
			public void ripristina() {
			}

			@Override
			public boolean salva() {
				return true;
			}
		};
	}

	/**
	 * Uno scambio finto con due elenchi di artefatti veri: a sinistra quelli del gruppo, a destra quelli di un armaiolo
	 */
	public static com.threeamigos.foresta.artefatti.VistaScambio scambio() {
		com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle generatore =
				new com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle(new java.util.Random(7));
		com.threeamigos.foresta.tipi.TipoArtefatto[] tipi = {com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, com.threeamigos.foresta.tipi.TipoArtefatto.ELMO,
				com.threeamigos.foresta.tipi.TipoArtefatto.SCUDO, com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, com.threeamigos.foresta.tipi.TipoArtefatto.ARMATURA,
				com.threeamigos.foresta.tipi.TipoArtefatto.MAZZA, com.threeamigos.foresta.tipi.TipoArtefatto.ELMO, com.threeamigos.foresta.tipi.TipoArtefatto.SPADA};
		List<com.threeamigos.foresta.artefatti.VistaArtefatto> sinistra = new ArrayList<>();
		List<com.threeamigos.foresta.artefatti.VistaArtefatto> destra = new ArrayList<>();
		for (int i = 0; i < tipi.length; i++) {
			sinistra.add(generatore.generaArtefatto(tipi[i], 2 + i));
			destra.add(generatore.generaArtefatto(tipi[(i + 3) % tipi.length], 3 + i));
		}
		return finto(com.threeamigos.foresta.artefatti.VistaScambio.class, null, risposteDelloScambio(sinistra, destra).mappa);
	}

	private static Risposte risposteDelloScambio(List<com.threeamigos.foresta.artefatti.VistaArtefatto> sinistra,
												 List<com.threeamigos.foresta.artefatti.VistaArtefatto> destra) {
		return new Risposte()
				.con("getId", a -> "prova")
				.con("getInventarioParteAttiva", a -> sinistra)
				.con("getInventarioParteRemota", a -> destra)
				.con("mostraCostoSuParteAttiva", a -> true)
				.con("mostraCostoSuParteRemota", a -> true);
	}

	/**
	 * Un banco di lavoro finto: l'inventario del gruppo a sinistra, sul banco una spada e un ingrediente, e una fusione che porta la spada al livello
	 * 7 con due effetti su quattro.
	 */
	public static com.threeamigos.foresta.artefatti.VistaBancoDiLavoro banco() {
		com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle generatore =
				new com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle(new java.util.Random(11));
		List<com.threeamigos.foresta.artefatti.VistaArtefatto> sinistra = new ArrayList<>();
		for (com.threeamigos.foresta.tipi.TipoArtefatto tipo : new com.threeamigos.foresta.tipi.TipoArtefatto[]{com.threeamigos.foresta.tipi.TipoArtefatto.SPADA,
				com.threeamigos.foresta.tipi.TipoArtefatto.ELMO, com.threeamigos.foresta.tipi.TipoArtefatto.SCUDO, com.threeamigos.foresta.tipi.TipoArtefatto.ARMATURA}) {
			sinistra.add(generatore.generaArtefatto(tipo, 4));
		}
		com.threeamigos.foresta.artefatti.VistaArtefatto sulBanco = generatore.generaArtefatto(com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, 5);
		List<com.threeamigos.foresta.artefatti.VistaArtefatto> destra = new ArrayList<>();
		destra.add(sulBanco);
		destra.add(generatore.generaArtefatto(com.threeamigos.foresta.tipi.TipoArtefatto.AGGIORNAMENTO, 3));
		return finto(com.threeamigos.foresta.artefatti.VistaBancoDiLavoro.class, null, risposteDelloScambio(sinistra, destra)
				.con("getArtefattoSulBanco", a -> Optional.of(sulBanco))
				.con("getLivelloDopoLaFusione", a -> 7)
				.con("getEffettiDopoLaFusione", a -> 2)
				.con("getEffettiMassimiDopoLaFusione", a -> 4)
				.con("getCostoFusione", a -> 120)
				// Per vedere i colori: il primo effetto della spada sul banco esce, il secondo entra
				.con("isEffettoInUscita", a -> a[0] == effettoNumero(sulBanco, 0))
				.con("isEffettoInEntrata", a -> a[0] == effettoNumero(sulBanco, 1)).mappa);
	}

	/** L'effetto di quel numero di un artefatto, o null se non ne ha tanti. */
	private static Object effettoNumero(com.threeamigos.foresta.artefatti.VistaArtefatto artefatto, int numero) {
		return new ArrayList<Object>(artefatto.getModificatori()).stream().skip(numero).findFirst().orElse(null);
	}

	/**
	 * Il listino di un alchimista finto: due incantesimi e le pozioni, con nome, descrizione e costo
	 */
	public static List<com.threeamigos.foresta.interfacce.VistaOffertaConsumabile> offerteDelAlchimista() {
		List<com.threeamigos.foresta.interfacce.VistaOffertaConsumabile> offerte = new ArrayList<>();
		offerte.add(offerta(com.threeamigos.foresta.tipi.TipoConsumabile.INCANTESIMO, TipoIncantesimo.FUOCO, "Fuoco",
				"Una palla di fuoco colpisce il nemico e lo brucia.", 150));
		offerte.add(offerta(com.threeamigos.foresta.tipi.TipoConsumabile.INCANTESIMO, TipoIncantesimo.GELO, "Gelo",
				"Il bersaglio si ghiaccia e rallenta.", 120));
		offerte.add(offerta(com.threeamigos.foresta.tipi.TipoConsumabile.POZIONE_SALUTE, null, "Pozione di salute", "Cura un personaggio.", 40));
		offerte.add(offerta(com.threeamigos.foresta.tipi.TipoConsumabile.POZIONE_MAGIA, null, "Pozione di magia", "Restituisce punti magia.", 55));
		offerte.add(offerta(com.threeamigos.foresta.tipi.TipoConsumabile.POZIONE_PANACEA, null, "Panacea", "Toglie gli effetti negativi.", 80));
		return offerte;
	}

	private static com.threeamigos.foresta.interfacce.VistaOffertaConsumabile offerta(com.threeamigos.foresta.tipi.TipoConsumabile tipo,
			TipoIncantesimo incantesimo, String nome, String descrizione, int costo) {
		return finto(com.threeamigos.foresta.interfacce.VistaOffertaConsumabile.class, null, new Risposte()
				.con("getTipo", a -> tipo)
				.con("getClasseIncantesimo", a -> incantesimo)
				.con("getNome", a -> nome)
				.con("getDescrizione", a -> descrizione)
				.con("getCosto", a -> costo).mappa);
	}

	/**
	 * Una pagina di intermezzo di prova: lo sfondo della locanda, un guerriero e un mago che si guardano, il fuoco da campo e due battute una dopo l'altra
	 */
	public static com.threeamigos.foresta.intermezzi.PaginaIntermezzo paginaDiIntermezzo() {
		return paginaDiIntermezzo(false);
	}

	/**
	 * La stessa pagina, e con <code>conRivelazione</code> all'inizio rivela un artefatto raro che poi vola verso il guerriero: le battute cominciano dopo
	 */
	public static com.threeamigos.foresta.intermezzi.PaginaIntermezzo paginaDiIntermezzo(boolean conRivelazione) {
		double ritardo = conRivelazione ? com.threeamigos.foresta.intermezzi.PaginaIntermezzo.SECONDI_RIVELAZIONE_ARTEFATTO : 0;
		com.threeamigos.foresta.intermezzi.ImmagineIntermezzo sfondo =
				com.threeamigos.foresta.intermezzi.ImmagineIntermezzo.risorsa("fondinon2x2/InternoLocanda.png");
		com.threeamigos.foresta.intermezzi.PaginaIntermezzo pagina = new com.threeamigos.foresta.intermezzi.PaginaIntermezzo("Una sera alla locanda")
				.conSfondo(sfondo, com.threeamigos.foresta.tipi.TipoStiramento.ORIZZONTALE_E_VERTICALE)
				.conElemento(com.threeamigos.foresta.intermezzi.ElementoIntermezzo.personaggio("guerriero", TipoPersonaggio.GUERRIERO, 0.3, 0.7)
						.guarda(com.threeamigos.foresta.tipi.Verso.DESTRA))
				.conElemento(com.threeamigos.foresta.intermezzi.ElementoIntermezzo.personaggio("mago", TipoPersonaggio.MAGO, 0.7, 0.7)
						.guarda(com.threeamigos.foresta.tipi.Verso.SINISTRA))
				.conElemento(com.threeamigos.foresta.intermezzi.ElementoIntermezzo.di("fuoco",
						com.threeamigos.foresta.intermezzi.ImmagineIntermezzo.animazione(com.threeamigos.foresta.tipi.Animazione.FUOCO_DA_CAMPO), 0.5, 0.8))
				.conBattuta(com.threeamigos.foresta.intermezzi.BattutaIntermezzo.di("guerriero", "Che serata, amico mio!").daSecondo(0.5 + ritardo).perSecondi(4))
				.conBattuta(com.threeamigos.foresta.intermezzi.BattutaIntermezzo.di("mago", "Il Drago non ci troverà qui, spero.").daSecondo(5 + ritardo).perSecondi(4));
		if (conRivelazione) {
			com.threeamigos.foresta.artefatti.VistaArtefatto spada = new com.threeamigos.foresta.artefatti.GeneratoreArtefattiTabelle(new java.util.Random(5))
					.generaArtefatto(com.threeamigos.foresta.tipi.TipoArtefatto.SPADA, 6);
			pagina.conRivelazioneDiArtefatto(spada, 3, "guerriero");
		}
		return pagina;
	}
}
