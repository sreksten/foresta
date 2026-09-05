package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class PersonaggioMD implements Serializzabile {

	/**
	 * Costante che indica che un dato attributo non ha limite.
	 * Per il tempo, indica che il personaggio è entrato definitivamente nel gruppo
	 * di un giocatore.
	 */
	public static final int SENZA_LIMITE = -1;

	private ClassePersonaggio classe;
	private String nome;

	// Impostati da PersonaggioBase uguali per tutti i personaggi
	private boolean vivo;
	private int livello;
	private int esperienza;
	private String causaTrapasso;
	private int tempo = SENZA_LIMITE;

	private final Map<TipoAttributo, Double> valoriMinimi = new HashMap<>();
	private final Map<TipoAttributo, Double> valoriMassimi = new HashMap<>();
	private final Map<TipoAttributo, Double> valoriAttributi = new HashMap<>();
	private Collection<ModificatoreAttributo> modificatori = new ArrayList<>();
	private Collection<EffettoDiStato> effettiDiStato = new ArrayList<>();
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

	public void setMinimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMinimi.remove(tipo);
		} else {
			this.valoriMinimi.put(tipo, valore);
		}
	}

	public double getMinimo(TipoAttributo tipo) {
		if (!valoriMinimi.containsKey(tipo)) {
			return 0.0d;
		}
		return valoriMinimi.get(tipo);
	}

	public void setMassimo(TipoAttributo tipo, Double valore) {
		if (valore == null) {
			valoriMassimi.remove(tipo);
		} else {
			this.valoriMassimi.put(tipo, valore);
		}
	}

	public double getMassimo(TipoAttributo tipo) {
		if (!valoriMassimi.containsKey(tipo)) {
			return 999_999.0d;
		}
		return valoriMassimi.get(tipo);
	}

	public void set(TipoAttributo tipo, double valore) {
		double valoreMinimo = getMinimo(tipo);
		if (valore < valoreMinimo) {
			valore = valoreMinimo;
		}
		double valoreMassimo = getMassimo(tipo);
		if (valore > valoreMassimo) {
			valore = valoreMassimo;
		}
		valoriAttributi.put(tipo, valore);
	}

	public double get(TipoAttributo tipo) {
		if (!valoriAttributi.containsKey(tipo)) {
			return getMinimo(tipo);
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

	public int getSaluteMassima() {
		return (int)getMassimo(TipoAttributo.SALUTE);
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

	public int getForzaMassima() {
		return (int)getMassimo(TipoAttributo.FORZA);
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

	public int getDestrezzaMassima() {
		return (int)getMassimo(TipoAttributo.DESTREZZA);
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

	public int getCostituzioneMassima() {
		return (int)getMassimo(TipoAttributo.COSTITUZIONE);
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

	public int getIntelligenzaMassima() {
		return (int)getMassimo(TipoAttributo.INTELLIGENZA);
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

	public int getSaggezzaMassima() {
		return (int)getMassimo(TipoAttributo.SAGGEZZA);
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

	public int getCarismaMassimo() {
		return (int)getMassimo(TipoAttributo.CARISMA);
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

	public int getFortunaMassima() {
		return (int)getMassimo(TipoAttributo.FORTUNA);
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

	public int getCriticoMassimo() {
		return (int)getMassimo(TipoAttributo.CRITICO);
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

	public int getPrecisioneMassima() {
		return (int)getMassimo(TipoAttributo.PRECISIONE);
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

	public int getVelocitaMassima() {
		return (int)getMassimo(TipoAttributo.VELOCITA);
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

	public int getFurtivitaMassima() {
		return (int)getMassimo(TipoAttributo.FURTIVITA);
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

	public int getParataMassima() {
		return (int)getMassimo(TipoAttributo.PARATA);
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

	public int getResistenzaMagicaMassima() {
		return (int)getMassimo(TipoAttributo.RESISTENZA_MAGICA);
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

	public int getPercezioneMassima() {
		return (int)getMassimo(TipoAttributo.PERCEZIONE);
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

	public int getSoggezioneMassima() {
		return (int)getMassimo(TipoAttributo.SOGGEZIONE);
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

	public int getFuriaMassima() {
		return (int)getMassimo(TipoAttributo.FURIA);
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

	public int getMagiaMassima() {
		return (int)getMassimo(TipoAttributo.MAGIA);
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

	public int getCoraggioMassimo() {
		return (int)getMassimo(TipoAttributo.CORAGGIO);
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

	public int getValoreMassimo() {
		return (int)getMassimo(TipoAttributo.VALORE);
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

	public int getStanchezzaMassima() {
		return (int)getMassimo(TipoAttributo.STANCHEZZA);
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

	public int getNumeroBersagliMassimo() {
		return (int)getMassimo(TipoAttributo.NUMERO_BERSAGLI);
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
		stream.print(tempo);
		stream.print(PIPE);
		stream.print(artefatti.size());
		stream.println();

		stream.println(valoriMinimi.entrySet().stream().map(e -> e.getKey().name() + ":" + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(valoriMassimi.entrySet().stream().map(e -> e.getKey().name() + ":" + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(valoriAttributi.entrySet().stream().map(e -> e.getKey().name() + ":" + e.getValue()).collect(Collectors.joining(PIPE)));
		stream.println(modificatori.stream().map(m -> m.getTipoAttributo().name() + ":" + m.getTipoModificatoreAttributo().name() + ":" + m.getQuantita() + ":" + m.getNote()).collect(Collectors.joining(PIPE)));
		stream.println(effettiDiStato.stream().map(e -> e.getTipoModificatoreAttributo().name() + ":" + e.getValore()).collect(Collectors.joining(PIPE)));

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
		tempo = Integer.parseInt(st.nextToken());
		int numeroArtefatti = Integer.parseInt(st.nextToken());

		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		valoriMinimi.clear();
		while (st.hasMoreTokens()) {
			String[] attributoValore = st.nextToken().split(":");
			valoriMinimi.put(TipoAttributo.valueOf(attributoValore[0]), Double.parseDouble(attributoValore[1]));
		}
		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		valoriMassimi.clear();
		while (st.hasMoreTokens()) {
			String[] attributoValore = st.nextToken().split(":");
			valoriMassimi.put(TipoAttributo.valueOf(attributoValore[0]), Double.parseDouble(attributoValore[1]));
		}
		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		valoriAttributi.clear();
		while (st.hasMoreTokens()) {
			String[] attributoValore = st.nextToken().split(":");
			valoriAttributi.put(TipoAttributo.valueOf(attributoValore[0]), Double.parseDouble(attributoValore[1]));
		}
		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		modificatori.clear();
		while (st.hasMoreTokens()) {
			String[] attributoValore = st.nextToken().split(":");
			ModificatoreAttributo modificatore = new ModificatoreAttributo(TipoAttributo.valueOf(attributoValore[0]),
					TipoModificatore.valueOf(attributoValore[1]), Double.parseDouble(attributoValore[2]),
					attributoValore[3]);
			modificatori.add(modificatore);
		}
		line = stream.readLine();
		st = new StringTokenizer(line, PIPE);
		effettiDiStato.clear();
		while (st.hasMoreTokens()) {
			String[] attributoValore = st.nextToken().split(":");
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
}
