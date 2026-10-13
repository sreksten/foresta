package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.modellodati.tattiche.TatticaMD;
import com.threeamigos.foresta.tipi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * I dati di un personaggio: la classe (mago, arpia...), il nome proprio facoltativo, se è vivo (o la causa della morte),
 * livello, esperienza e punti abilità non ancora assegnati, se è un PNG e perché sta nel gruppo, gli artefatti che
 * porta, i modificatori e gli effetti di stato in corso e la sua tattica di combattimento.
 * <p>
 * Gli <b>attributi</b> (salute, forza, destrezza...) stanno in una mappa indicizzata da {@link TipoAttributo}, con un
 * valore minimo e uno massimo facoltativi per ciascuno: {@link #set(TipoAttributo, double)} li rispetta, quindi un
 * valore fuori dai limiti viene portato al limite. I metodi con il nome dell'attributo ({@code getForza},
 * {@code setSaluteMassima}...) sono scorciatoie su {@link #get}, {@link #set}, {@link #getMassimo} e
 * {@link #setMassimo}, e i loro valori letti sono troncati a numeri interi.
 * <p>
 * Le regole del gioco (come cambiano gli attributi, come si combatte) stanno nel motore; qui ci sono solo i dati.
 */
public class PersonaggioMD implements Serializzabile {

	/**
	 * Costante che indica che un dato attributo non ha limite.
	 * Per il tempo, indica che il personaggio è entrato definitivamente nel gruppo
	 * di un giocatore.
	 */
	public static final int SENZA_LIMITE = -1;

	/**
	 * Un identificativo unico per il personaggio
	 */
	private String uuid = UUID.randomUUID().toString();

	/**
	 * Classe del personaggio (Mago, Arpia, etc.)
	 */
	private TipoPersonaggio classe;
	/**
	 * Nome proprio del personaggio (quando presente)
	 */
	private String nome;

	// Impostati da PersonaggioBase uguali per tutti i personaggi
	/**
	 * Se il personaggio è vivo
	 */
	private boolean vivo;
	/**
	 * Livello del personaggio
	 */
	private int livello;
	/**
	 * Punti esperienza accumulati dal personaggio
	 */
	private int esperienza;
	/**
	 * Punti abilità guadagnati da passaggio a livello successivo e non ancora assegnati
	 */
	private int puntiAbilitaDisponibili;
	/**
	 * La causa della morte del personaggio
	 */
	private String causaTrapasso;
	/**
	 * Se è un PNG: non l'ha creato il giocatore, per cui non ha esperienza e può non avere un nome proprio
	 */
	private boolean png;
	/**
	 * Perché si trova nel gruppo del giocatore (se vi si trova)
	 */
	private TipoMotivoNelGruppo motivoNelGruppo = TipoMotivoNelGruppo.PERMANENTE;

	/**
	 * Valori minimi per un dato attributo - solitamente non presenti
	 */
	private final Map<TipoAttributo, Double> valoriMinimi = new HashMap<>();
	/**
	 * Valori massimi per un dato attributo - solitamente non presenti
	 */
	private final Map<TipoAttributo, Double> valoriMassimi = new HashMap<>();
	/**
	 * Valori per i vari attributi dei personaggi
	 */
	private final Map<TipoAttributo, Double> valoriAttributi = new HashMap<>();
	/**
	 * Modificatori permanenti agli attributi dei personaggi
	 */
	private Collection<ModificatoreAttributoMD> modificatori = new ArrayList<>();
	private TatticaMD tattica;
	/**
	 * Effetti di stato applicati al personaggio
	 */
	private Collection<EffettoDiStatoMD> effettiDiStato = new ArrayList<>();
	/**
	 * Artefatti posseduti dal personaggio
	 */
	private Collection<ArtefattoMD> artefatti = new ArrayList<>();

	/**
	 * L'identificativo unico del personaggio, che il salvataggio conserva.
	 */
	public String getUuid() {
		return uuid;
	}

	public TipoPersonaggio getClasse() {
		return classe;
	}

	public void setClasse(TipoPersonaggio classe) {
		this.classe = classe;
	}

	/**
	 * Il nome proprio del personaggio, o null se non ne ha: allora si chiama con la sua classe.
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Il "|" si toglie; un nome vuoto vale come nessun nome (null): il personaggio si chiama allora con la sua classe.
	 */
	public void setNome(String nome) {
		String pulito = Serializzabile.senzaPipe(nome);
		this.nome = pulito == null || pulito.trim().isEmpty() ? null : pulito;
	}

	public boolean isPng() {
		return png;
	}

	public void setPng(boolean png) {
		this.png = png;
	}

	public TipoMotivoNelGruppo getMotivoNelGruppo() {
		return motivoNelGruppo;
	}

	public void setMotivoNelGruppo(TipoMotivoNelGruppo motivoNelGruppo) {
		this.motivoNelGruppo = motivoNelGruppo;
	}

	public boolean isVivo() {
		return vivo;
	}

	public void setVivo(boolean vivo) {
		this.vivo = vivo;
	}

	public int getLivello() {
		return livello;
	}

	public void setLivello(int livello) {
		this.livello = livello;
	}

	public int getEsperienza() {
		return esperienza;
	}

	public void setEsperienza(int esperienza) {
		this.esperienza = esperienza;
	}

	public int getPuntiAbilitaDisponibili() {
		return puntiAbilitaDisponibili;
	}

	public void setPuntiAbilitaDisponibili(int puntiAbilitaDisponibili) {
		this.puntiAbilitaDisponibili = puntiAbilitaDisponibili;
	}

	/**
	 * Imposta il valore minimo di un attributo; con null lo toglie.
	 */
	public void setMinimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMinimi.remove(tipo);
		} else {
			this.valoriMinimi.put(tipo, valore);
		}
	}

	/**
	 * Il valore minimo dell'attributo, se ne ha uno.
	 */
	public Optional<Double> getMinimo(TipoAttributo tipo) {
		if (!valoriMinimi.containsKey(tipo)) {
			return Optional.empty();
		}
		return Optional.of(valoriMinimi.get(tipo));
	}

	/**
	 * Imposta il valore massimo di un attributo; con null lo toglie.
	 */
	public void setMassimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMassimi.remove(tipo);
		} else {
			this.valoriMassimi.put(tipo, valore);
		}
	}

	/**
	 * Il valore massimo dell'attributo, se ne ha uno.
	 */
	public Optional<Double> getMassimo(TipoAttributo tipo) {
		if (!valoriMassimi.containsKey(tipo)) {
			return Optional.empty();
		}
		return Optional.of(valoriMassimi.get(tipo));
	}

	/**
	 * Imposta un determinato attributo con un certo valore, limitandolo però ai valori minimo e massimo,
	 * quando presenti.
	 */
	public void set(TipoAttributo tipo, double valore) {
		Optional<Double> valoreMinimoOpt = getMinimo(tipo);
		if (valoreMinimoOpt.isPresent()) {
			double valoreMinimo = valoreMinimoOpt.get();
			if (valore < valoreMinimo) {
				valore = valoreMinimo;
			}
		}
		Optional<Double> valoreMassimoOpt = getMassimo(tipo);
		if (valoreMassimoOpt.isPresent()) {
			double valoreMassimo = valoreMassimoOpt.get();
			if (valore > valoreMassimo) {
				valore = valoreMassimo;
			}
		}
		valoriAttributi.put(tipo, valore);
	}

	/**
	 * Il valore di un attributo.
	 *
	 * @throws IllegalStateException se il personaggio non ha quell'attributo
	 */
	public double get(TipoAttributo tipo) {
		if (!valoriAttributi.containsKey(tipo)) {
			throw new IllegalStateException("TipoAttributo " + tipo + " non presente");
		}
		return valoriAttributi.get(tipo);
	}

	// Piccola eccezione, TEMPO non è detto che ci sia.
	/**
	 * Come {@link #get(TipoAttributo)}, ma vuoto invece di lanciare un'eccezione: serve per l'attributo del tempo, che non tutti hanno.
	 */
	public Optional<Double> getOptional(TipoAttributo tipo) {
		return Optional.ofNullable(valoriAttributi.get(tipo));
	}

	/**
	 * Il carico massimo che il personaggio può portare, 0 se non è limitato da un massimo.
	 */
	public int getCaricoMassimo() {
		return getMassimo(TipoAttributo.CARICO_MASSIMO).orElse(0.0d).intValue();
	}

	public void setSalute(double salute) {
		set(TipoAttributo.SALUTE, salute);
	}

	public int getSalute() {
		return (int)get(TipoAttributo.SALUTE);
	}

	public void setSaluteMassima(double saluteMassima) {
		setMassimo(TipoAttributo.SALUTE, saluteMassima);
	}

	public Optional<Double> getSaluteMassima() {
		return getMassimo(TipoAttributo.SALUTE);
	}

	public void setForza(double forza) {
		set(TipoAttributo.FORZA, forza);
	}

	public int getForza() {
		return (int)get(TipoAttributo.FORZA);
	}

	public void setForzaMassima(double forzaMassima) {
		setMassimo(TipoAttributo.FORZA, forzaMassima);
	}

	public Optional<Double> getForzaMassima() {
		return getMassimo(TipoAttributo.FORZA);
	}

	public void setDestrezza(int destrezza) {
		set(TipoAttributo.DESTREZZA, destrezza);
	}

	public int getDestrezza() {
		return (int)get(TipoAttributo.DESTREZZA);
	}

	public void setDestrezzaMassima(double destrezzaMassima) {
		setMassimo(TipoAttributo.DESTREZZA, destrezzaMassima);
	}

	public Optional<Double> getDestrezzaMassima() {
		return getMassimo(TipoAttributo.DESTREZZA);
	}

	public void setCostituzione(int costituzione) {
		set(TipoAttributo.COSTITUZIONE, costituzione);
	}

	public int getCostituzione() {
		return (int)get(TipoAttributo.COSTITUZIONE);
	}

	public void setCostituzioneMassima(double costituzioneMassima) {
		setMassimo(TipoAttributo.COSTITUZIONE, costituzioneMassima);
	}

	public Optional<Double> getCostituzioneMassima() {
		return getMassimo(TipoAttributo.COSTITUZIONE);
	}

	public void setIntelligenza(double intelligenza) {
		set(TipoAttributo.INTELLIGENZA, intelligenza);
	}

	public int getIntelligenza() {
		return (int)get(TipoAttributo.INTELLIGENZA);
	}

	public void setIntelligenzaMassima(double intelligenzaMassima) {
		setMassimo(TipoAttributo.INTELLIGENZA, intelligenzaMassima);
	}

	public Optional<Double> getIntelligenzaMassima() {
		return getMassimo(TipoAttributo.INTELLIGENZA);
	}

	public void setSaggezza(double saggezza) {
		set(TipoAttributo.SAGGEZZA, saggezza);
	}

	public int getSaggezza() {
		return (int)get(TipoAttributo.SAGGEZZA);
	}

	public void setSaggezzaMassima(double saggezzaMassima) {
		setMassimo(TipoAttributo.SAGGEZZA, saggezzaMassima);
	}

	public Optional<Double> getSaggezzaMassima() {
		return getMassimo(TipoAttributo.SAGGEZZA);
	}

	public void setCarisma(double carisma) {
		set(TipoAttributo.CARISMA, carisma);
	}

	public int getCarisma() {
		return (int)get(TipoAttributo.CARISMA);
	}

	public void setCarismaMassimo(double carismaMassimo) {
		setMassimo(TipoAttributo.CARISMA, carismaMassimo);
	}

	public Optional<Double> getCarismaMassimo() {
		return getMassimo(TipoAttributo.CARISMA);
	}

	public void setFortuna(double fortuna) {
		set(TipoAttributo.FORTUNA, fortuna);
	}

	public int getFortuna() {
		return (int)get(TipoAttributo.FORTUNA);
	}

	public void setFortunaMassima(double fortunaMassima) {
		setMassimo(TipoAttributo.FORTUNA, fortunaMassima);
	}

	public Optional<Double> getFortunaMassima() {
		return getMassimo(TipoAttributo.FORTUNA);
	}

	public void setCritico(double  critico) {
		set(TipoAttributo.CRITICO, critico);
	}

	public int getCritico() {
		return (int)get(TipoAttributo.CRITICO);
	}

	public void setCriticoMassimo(double criticoMassimo) {
		setMassimo(TipoAttributo.CRITICO, criticoMassimo);
	}

	public Optional<Double> getCriticoMassimo() {
		return getMassimo(TipoAttributo.CRITICO);
	}

	public void setPrecisione(double precisione) {
		set(TipoAttributo.PRECISIONE, precisione);
	}

	public int getPrecisione() {
		return (int)get(TipoAttributo.PRECISIONE);
	}

	public void setPrecisioneMassima(double precisioneMassima) {
		setMassimo(TipoAttributo.PRECISIONE, precisioneMassima);
	}

	public Optional<Double> getPrecisioneMassima() {
		return getMassimo(TipoAttributo.PRECISIONE);
	}

	public void setVelocita(double velocita) {
		set(TipoAttributo.VELOCITA, velocita);
	}

	public int getVelocita() {
		return (int)get(TipoAttributo.VELOCITA);
	}

	public void setVelocitaMassima(double velocitaMassima) {
		setMassimo(TipoAttributo.VELOCITA, velocitaMassima);
	}

	public Optional<Double> getVelocitaMassima() {
		return getMassimo(TipoAttributo.VELOCITA);
	}

	public void setFurtivita(double furtivita) {
		set(TipoAttributo.FURTIVITA, furtivita);
	}

	public int getFurtivita() {
		return (int)get(TipoAttributo.FURTIVITA);
	}

	public void setFurtivitaMassima(double furtivitaMassima) {
		setMassimo(TipoAttributo.FURTIVITA, furtivitaMassima);
	}

	public Optional<Double> getFurtivitaMassima() {
		return getMassimo(TipoAttributo.FURTIVITA);
	}

	public void setParata(double parata) {
		set(TipoAttributo.PARATA, parata);
	}

	public int getParata() {
		return (int)get(TipoAttributo.PARATA);
	}

	public void setParataMassima(double parataMassima) {
		setMassimo(TipoAttributo.PARATA, parataMassima);
	}

	public Optional<Double> getParataMassima() {
		return getMassimo(TipoAttributo.PARATA);
	}

	public void setResistenzaMagica(double resistenzaMagica) {
		set(TipoAttributo.RESISTENZA_MAGICA, resistenzaMagica);
	}

	public int getResistenzaMagica() {
		return (int)get(TipoAttributo.RESISTENZA_MAGICA);
	}

	public void setResistenzaMagicaMassima(double resistenzaMagicaMassima) {
		setMassimo(TipoAttributo.RESISTENZA_MAGICA, resistenzaMagicaMassima);
	}

	public Optional<Double> getResistenzaMagicaMassima() {
		return getMassimo(TipoAttributo.RESISTENZA_MAGICA);
	}

	public void setPercezione(double percezione) {
		set(TipoAttributo.PERCEZIONE, percezione);
	}

	public int getPercezione() {
		return (int)get(TipoAttributo.PERCEZIONE);
	}

	public void setPercezioneMassima(double percezioneMassima) {
		setMassimo(TipoAttributo.PERCEZIONE, percezioneMassima);
	}

	public Optional<Double> getPercezioneMassima() {
		return getMassimo(TipoAttributo.PERCEZIONE);
	}

	public void setSoggezione(double soggezione) {
		set(TipoAttributo.SOGGEZIONE, soggezione);
	}

	public int getSoggezione() {
		return (int)get(TipoAttributo.SOGGEZIONE);
	}

	public void setSoggezioneMassima(double soggezioneMassima) {
		setMassimo(TipoAttributo.SOGGEZIONE, soggezioneMassima);
	}

	public Optional<Double> getSoggezioneMassima() {
		return getMassimo(TipoAttributo.SOGGEZIONE);
	}

	public void setContrattazione(double contrattazione) {
		set(TipoAttributo.CONTRATTAZIONE, contrattazione);
	}

	public int getContrattazione() {
		return (int)get(TipoAttributo.CONTRATTAZIONE);
	}

	public void setContrattazioneMassima(double contrattazioneMassima) {
		setMassimo(TipoAttributo.CONTRATTAZIONE, contrattazioneMassima);
	}

	public Optional<Double> getContrattazioneMassima() {
		return getMassimo(TipoAttributo.CONTRATTAZIONE);
	}

	public void setFuria(double furia) {
		set(TipoAttributo.FURIA, furia);
	}

	public int getFuria() {
		return (int)get(TipoAttributo.FURIA);
	}

	public void setFuriaMassima(double furiaMassima) {
		setMassimo(TipoAttributo.FURIA, furiaMassima);
	}

	public Optional<Double> getFuriaMassima() {
		return getMassimo(TipoAttributo.FURIA);
	}

	public void setMagia(int magia) {
		set(TipoAttributo.MAGIA, magia);
	}

	public int getMagia() {
		return (int)get(TipoAttributo.MAGIA);
	}

	public void setMagiaMassima(double magiaMassima) {
		setMassimo(TipoAttributo.MAGIA, magiaMassima);
	}

	public Optional<Double> getMagiaMassima() {
		return getMassimo(TipoAttributo.MAGIA);
	}

	public void setCoraggio(double coraggio) {
		set(TipoAttributo.CORAGGIO, coraggio);
	}

	public int getCoraggio() {
		return (int)get(TipoAttributo.CORAGGIO);
	}

	public void setCoraggioMassimo(double coraggioMassimo) {
		setMassimo(TipoAttributo.CORAGGIO, coraggioMassimo);
	}

	public Optional<Double> getCoraggioMassimo() {
		return getMassimo(TipoAttributo.CORAGGIO);
	}

	public void setValore(double valore) {
		set(TipoAttributo.VALORE, valore);
	}

	public int getValore() {
		return (int)get(TipoAttributo.VALORE);
	}

	public void setValoreMassimo(double valoreMassimo) {
		setMassimo(TipoAttributo.VALORE, valoreMassimo);
	}

	public Optional<Double> getValoreMassimo() {
		return getMassimo(TipoAttributo.VALORE);
	}

	public void setStanchezza(double stanchezza) {
		set(TipoAttributo.STANCHEZZA, stanchezza);
	}

	public int getStanchezza() {
		return (int)get(TipoAttributo.STANCHEZZA);
	}

	public void setStanchezzaMassima(double stanchezzaMassima) {
		setMassimo(TipoAttributo.STANCHEZZA, stanchezzaMassima);
	}

	public Optional<Double> getStanchezzaMassima() {
		return getMassimo(TipoAttributo.STANCHEZZA);
	}

	public void setNumeroBersagli(int numeroBersagli) {
		set(TipoAttributo.NUMERO_BERSAGLI, numeroBersagli);
	}

	public int getNumeroBersagli() {
		return (int)get(TipoAttributo.NUMERO_BERSAGLI);
	}

	public void setNumeroBersagliMassimo(double numeroBersagliMassimo) {
		setMassimo(TipoAttributo.NUMERO_BERSAGLI, numeroBersagliMassimo);
	}

	public Optional<Double> getNumeroBersagliMassimo() {
		return getMassimo(TipoAttributo.NUMERO_BERSAGLI);
	}

	public String getCausaTrapasso() {
		return causaTrapasso;
	}

	public void setCausaTrapasso(String causaTrapasso) {
		this.causaTrapasso = Serializzabile.senzaPipe(causaTrapasso);
	}

	public Collection<EffettoDiStatoMD> getEffettiDiStato() {
		return effettiDiStato;
	}

	public void setEffettiDiStato(Collection<EffettoDiStatoMD> effettiDiStato) {
		this.effettiDiStato = effettiDiStato;
	}

	public Collection<ArtefattoMD> getArtefatti() {
		return artefatti;
	}

	public void setArtefatti(Collection<ArtefattoMD> artefatti) {
		this.artefatti = artefatti;
	}

	/**
	 * La tattica del personaggio (tattiche_di_combattimento_automatiche.md §11), o null se non ne ha una (oggi i mostri); si salva con lui
	 */
	public TatticaMD getTattica() {
		return tattica;
	}

	public void setTattica(TatticaMD tattica) {
		this.tattica = tattica;
	}

	public Collection<ModificatoreAttributoMD> getModificatori() {
		return modificatori;
	}

	public void setModificatori(Collection<ModificatoreAttributoMD> modificatori) {
		this.modificatori = modificatori;
	}

	/**
	 * Scrive una riga con i dati del personaggio (compreso il numero di artefatti), poi cinque righe con i valori minimi,
	 * i valori massimi, gli attributi, i modificatori e gli effetti di stato, poi la tattica (una riga vuota se non ne ha)
	 * e infine gli artefatti.
	 */
	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(uuid);
		stream.print(PIPE);
		stream.print(classe.name());
		stream.print(PIPE);
		stream.print(Serializzabile.facoltativo(nome));
		stream.print(PIPE);
		stream.print(vivo ? "vivo" : Serializzabile.facoltativo(causaTrapasso));
		stream.print(PIPE);
		stream.print(livello);
		stream.print(PIPE);
		stream.print(esperienza);
		stream.print(PIPE);
		stream.print(puntiAbilitaDisponibili);
		stream.print(PIPE);
		stream.print(artefatti.size());
		stream.print(PIPE);
		stream.print(png);
		stream.print(PIPE);
		stream.print(motivoNelGruppo.name());
		stream.println();

		stream.println(valoriMinimi.entrySet().stream().map(e -> e.getKey().name() + MappaProprieta.SEPARATORE + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(valoriMassimi.entrySet().stream().map(e -> e.getKey().name() + MappaProprieta.SEPARATORE + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(valoriAttributi.entrySet().stream().map(e -> e.getKey().name() + MappaProprieta.SEPARATORE + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(modificatori.stream().map(m -> m.getTipoAttributo().name() + MappaProprieta.SEPARATORE + m.getTipoModificatoreAttributo().name() + MappaProprieta.SEPARATORE + m.getQuantita() + MappaProprieta.SEPARATORE + m.getUnitaDurata().name() + MappaProprieta.SEPARATORE + m.getDurata() + MappaProprieta.SEPARATORE + m.getBersaglio().name() + MappaProprieta.SEPARATORE + m.getNote()).collect(Collectors.joining(PIPE)));
		stream.println(effettiDiStato.stream().map(e -> e.getTipoEffettoDiStato().name() + MappaProprieta.SEPARATORE + e.getDurata() + MappaProprieta.SEPARATORE + e.getDanniNelTempo() + MappaProprieta.SEPARATORE + e.getIntensita()).collect(Collectors.joining(PIPE)));

		// La tattica: una riga vuota se non ce n'è una, altrimenti la tattica con le sue voci
		if (tattica == null) {
			stream.println();
		} else {
			tattica.salva(stream);
		}

		for (ArtefattoMD artefatto : artefatti) {
			artefatto.salva(stream);
		}
	}

	/**
	 * Rilegge ciò che scrive {@link #salva(PrintWriter)}, sostituendo i dati attuali del personaggio.
	 */
	@Override
	public void leggi(BufferedReader stream) throws IOException{
		LettoreCampi campi = new LettoreCampi(stream.readLine());

		uuid = campi.testo();
		classe = campi.enumerato(TipoPersonaggio.class);
		nome = campi.testoFacoltativo();
		String vivoOMorto = campi.testo();
		if ("vivo".equals(vivoOMorto)) {
			vivo = true;
			causaTrapasso = null;
		} else {
			vivo = false;
			causaTrapasso = vivoOMorto.isEmpty() ? null : vivoOMorto;
		}
		livello = campi.intero();
		esperienza = campi.intero();
		puntiAbilitaDisponibili = campi.intero();
		int numeroArtefatti = campi.intero();
		png = campi.booleano();
		motivoNelGruppo = campi.enumerato(TipoMotivoNelGruppo.class);

		impostaValori(valoriMinimi, stream.readLine());
		impostaValori(valoriMassimi, stream.readLine());
		impostaValori(valoriAttributi, stream.readLine());

		campi = new LettoreCampi(stream.readLine());
		modificatori.clear();
		while (campi.haAltriCampi()) {
			// Limite 7: la nota, l'ultimo campo, può contenere il separatore
			String[] attributoValore = campi.testo().split(MappaProprieta.SEPARATORE, 7);
			ModificatoreAttributoMD modificatore = new ModificatoreAttributoMD(TipoAttributo.valueOf(attributoValore[0]),
					TipoModificatore.valueOf(attributoValore[1]), Double.parseDouble(attributoValore[2]),
					attributoValore.length > 6 ? attributoValore[6] : "",
					UnitaDurataModificatore.valueOf(attributoValore[3]), Integer.parseInt(attributoValore[4]),
					BersaglioModificatore.valueOf(attributoValore[5]));
			modificatori.add(modificatore);
		}
		campi = new LettoreCampi(stream.readLine());
		effettiDiStato.clear();
		while (campi.haAltriCampi()) {
			String[] attributoValore = campi.testo().split(MappaProprieta.SEPARATORE);
			EffettoDiStatoMD effettoDiStato = new EffettoDiStatoMD(TipoEffettoDiStato.valueOf(attributoValore[0]),
					Integer.parseInt(attributoValore[1]), Integer.parseInt(attributoValore[2]),
					Double.parseDouble(attributoValore[3]));
			effettiDiStato.add(effettoDiStato);
		}

		String intestazioneTattica = stream.readLine();
		if (intestazioneTattica == null) {
			throw new IOException("Fine del salvataggio inattesa");
		}
		tattica = intestazioneTattica.isEmpty() ? null : TatticaMD.leggi(intestazioneTattica, stream);

		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
	}

	private void impostaValori(Map<TipoAttributo, Double> mappa, String linea) throws IOException {
		LettoreCampi campi = new LettoreCampi(linea);
		mappa.clear();
		while (campi.haAltriCampi()) {
			String[] attributoValore = campi.testo().split(MappaProprieta.SEPARATORE);
			mappa.put(TipoAttributo.valueOf(attributoValore[0]), Double.parseDouble(attributoValore[1]));
		}
	}
}
