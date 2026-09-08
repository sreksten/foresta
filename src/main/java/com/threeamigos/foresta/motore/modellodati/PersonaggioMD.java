package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Il modello dati per un personaggio.
 */
public class PersonaggioMD implements Serializzabile {

	/**
	 * Costante che indica che un dato attributo non ha limite.
	 * Per il tempo, indica che il personaggio è entrato definitivamente nel gruppo
	 * di un giocatore.
	 */
	public static final int SENZA_LIMITE = -1;

	/**
	 * Classe del personaggio (Mago, Arpia, etc.)
	 */
	private ClassePersonaggio classe;
	/**
	 * Nome proprio del personaggio (quando presente)
	 */
	private String nome;

	// Impostati da PersonaggioBase uguali per tutti i personaggi
	/**
	 * Se il personaggio sia sempre vivo
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
	 * Tempo rimanente prima che il personaggio lasci il gruppo (per i personaggi amichevoli che si offrono di
	 * accompagnare il gruppo)
	 */
	private int tempo = SENZA_LIMITE;

	/**
	 * Valori minimi per un dato attributo - solitamente non presenti
	 */
	private final Map<TipoAttributo, Double> valoriMinimi = new HashMap<>();
	/**
	 * Valori minimi per un dato attributo - solitamente non presenti
	 */
	private final Map<TipoAttributo, Double> valoriMassimi = new HashMap<>();
	/**
	 * Valori per i vari attributi dei personaggi
	 */
	private final Map<TipoAttributo, Double> valoriAttributi = new HashMap<>();
	/**
	 * Modificatori permanenti agli attributi dei personaggi
	 */
	private Collection<ModificatoreAttributo> modificatori = new ArrayList<>();
	/**
	 * Effetti di stato applicati al personaggio
	 */
	private Collection<EffettoDiStato> effettiDiStato = new ArrayList<>();
	/**
	 * Artefatti posseduti dal personaggio
	 */
	private Collection<ArtefattoMD> artefatti = new ArrayList<>();

	public ClassePersonaggio getClasse() {
		return classe;
	}

	public void setClasse(ClassePersonaggio classe) {
		this.classe = classe;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
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

	public void setMinimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMinimi.remove(tipo);
		} else {
			this.valoriMinimi.put(tipo, valore);
		}
	}

	public Optional<Double> getMinimo(TipoAttributo tipo) {
		if (!valoriMinimi.containsKey(tipo)) {
			return Optional.empty();
		}
		return Optional.of(valoriMinimi.get(tipo));
	}

	public void setMassimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMassimi.remove(tipo);
		} else {
			this.valoriMassimi.put(tipo, valore);
		}
	}

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

	public double get(TipoAttributo tipo) {
		if (!valoriAttributi.containsKey(tipo)) {
			throw new IllegalStateException("TipoAttributo " + tipo + " non presente");
		}
		return valoriAttributi.get(tipo);
	}

	public void setCaricoMassimo(double caricoMassimo) {
		setMassimo(TipoAttributo.CARICO_MASSIMO, caricoMassimo);
	}

	public int getCaricoMassimo() {
		return (int)get(TipoAttributo.CARICO_MASSIMO);
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
		valoriMassimi.put(TipoAttributo.FORZA, forzaMassima);
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
		this.causaTrapasso = causaTrapasso;
	}

	public int getTempo() {
		return tempo;
	}

	public void setTempo(int tempo) {
		this.tempo = tempo;
	}

	public Collection<EffettoDiStato> getEffettiDiStato() {
		return effettiDiStato;
	}

	public void setEffettiDiStato(Collection<EffettoDiStato> effettiDiStato) {
		this.effettiDiStato = effettiDiStato;
	}

	public Collection<ArtefattoMD> getArtefatti() {
		return artefatti;
	}

	public void setArtefatti(Collection<ArtefattoMD> artefatti) {
		this.artefatti = artefatti;
	}

	public Collection<ModificatoreAttributo> getModificatori() {
		return modificatori;
	}

	public void setModificatori(Collection<ModificatoreAttributo> modificatori) {
		this.modificatori = modificatori;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(classe.name());
		stream.print(PIPE);
		stream.print(nome);
		stream.print(PIPE);
		stream.print(vivo ? "vivo" : causaTrapasso);
		stream.print(PIPE);
		stream.print(livello);
		stream.print(PIPE);
		stream.print(esperienza);
		stream.print(PIPE);
		stream.print(puntiAbilitaDisponibili);
		stream.print(PIPE);
		stream.print(tempo);
		stream.print(PIPE);
		stream.print(artefatti.size());
		stream.println();

		stream.println(valoriMinimi.entrySet().stream().map(e -> e.getKey().name() + MappaProprieta.SEPARATORE + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(valoriMassimi.entrySet().stream().map(e -> e.getKey().name() + MappaProprieta.SEPARATORE + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(valoriAttributi.entrySet().stream().map(e -> e.getKey().name() + MappaProprieta.SEPARATORE + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(modificatori.stream().map(m -> m.getTipoAttributo().name() + MappaProprieta.SEPARATORE + m.getTipoModificatoreAttributo().name() + MappaProprieta.SEPARATORE + m.getQuantita() + MappaProprieta.SEPARATORE + m.getNote()).collect(Collectors.joining(PIPE)));
		stream.println(effettiDiStato.stream().map(e -> e.getTipoModificatoreAttributo().name() + MappaProprieta.SEPARATORE + e.getValore()).collect(Collectors.joining(PIPE)));

		for (ArtefattoMD artefatto : artefatti) {
			artefatto.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException{
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		classe = ClassePersonaggio.valueOf(st.nextToken());
		nome = st.nextToken();
		if ("null".equals(nome)) {
			nome = null;
		}
		String vivoOMorto = st.nextToken();
		if ("vivo".equals(vivoOMorto)) {
			vivo = true;
			causaTrapasso = null;
		} else {
			vivo = false;
			causaTrapasso = vivoOMorto;
		}
		livello = Integer.parseInt(st.nextToken());
		esperienza = Integer.parseInt(st.nextToken());
		puntiAbilitaDisponibili = Integer.parseInt(st.nextToken());
		tempo = Integer.parseInt(st.nextToken());
		int numeroArtefatti = Integer.parseInt(st.nextToken());

		impostaValori(valoriMinimi, stream.readLine());
		impostaValori(valoriMassimi, stream.readLine());
		impostaValori(valoriAttributi, stream.readLine());

		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		modificatori.clear();
		while (st.hasMoreTokens()) {
			// Limite 4: la nota può contenere il separatore
			String[] attributoValore = st.nextToken().split(MappaProprieta.SEPARATORE, 4);
			ModificatoreAttributo modificatore = new ModificatoreAttributo(TipoAttributo.valueOf(attributoValore[0]),
					TipoModificatore.valueOf(attributoValore[1]), Double.parseDouble(attributoValore[2]),
					attributoValore.length > 3 ? attributoValore[3] : "");
			modificatori.add(modificatore);
		}
		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		effettiDiStato.clear();
		while (st.hasMoreTokens()) {
			String[] attributoValore = st.nextToken().split(MappaProprieta.SEPARATORE);
			EffettoDiStato effettoDiStato = new EffettoDiStato(TipoEffettoDiStato.valueOf(attributoValore[0]), Integer.parseInt(attributoValore[1]));
			effettiDiStato.add(effettoDiStato);
		}

		artefatti.clear();
		for (int i = 0; i < numeroArtefatti; i++) {
			ArtefattoMD artefatto = new ArtefattoMD();
			artefatto.leggi(stream);
			artefatti.add(artefatto);
		}
	}

	private void impostaValori(Map<TipoAttributo, Double> mappa, String linea) {
		StringTokenizer st = new StringTokenizer(linea, PIPE);
		mappa.clear();
		while (st.hasMoreTokens()) {
			String[] attributoValore = st.nextToken().split(MappaProprieta.SEPARATORE);
			mappa.put(TipoAttributo.valueOf(attributoValore[0]), Double.parseDouble(attributoValore[1]));
		}
	}
}
