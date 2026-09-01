package com.threeamigos.foresta.motore.modellodati;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class PersonaggioMD implements Serializzabile {

	/**
	 * Costante che indica che un PNG è entrato definitivamente nel gruppo di un giocatore
	 */
	public static final int NO_TEMPO = -1;

	private ClassePersonaggio classe;
	private String nome;
	private boolean vivo;
	private int livello;
	private int esperienza;

	private int salute;
	private int saluteMassima;
	private int magia;
	private int magiaMassima;
	private int carico;
	private int caricoMassimo;

	private int forza;
	private int forzaMassima;
	private int destrezza;
	private int destrezzaMassima;
	private int costituzione;
	private int costituzioneMassima;
	private int intelligenza;
	private int intelligenzaMassima;
	private int saggezza;
	private int saggezzaMassima;
	private int carisma;
	private int carismaMassimo;
	private int fortuna;
	private int fortunaMassima;
	private int critico;
	private int criticoMassimo;
	private int precisione;
	private int precisioneMassima;
	private int velocita;
	private int velocitaMassima;
	private int furtivita;
	private int furtivitaMassima;
	private int parata;
	private int parataMassima;
	private int resistenzaMagica;
	private int resistenzaMagicaMassima;
	private int percezione;
	private int percezioneMassima;
	private int soggezione;
	private int soggezioneMassima;
	private int furia;
	private int furiaMassima;

	private int coraggio;
	private int valore;
	private int stanchezza;

	private String causaTrapasso;
	private int tempo = NO_TEMPO;

	private List<EffettoDiStato> effettiDiStato = new ArrayList<>();

	private List<ArtefattoMD> artefatti = new ArrayList<>();

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

	public int getSalute() {
		return salute;
	}

	public void setSalute(int forza) {
		this.salute = forza;
	}

	public int getSaluteMassima() {
		return saluteMassima;
	}

	public void setSaluteMassima(int saluteMassima) {
		this.saluteMassima = saluteMassima;
	}

	public int getForza() {
		return forza;
	}

	public void setForza(int forza) {
		this.forza = forza;
	}

	public int getForzaMassima() {
		return forzaMassima;
	}

	public void setForzaMassima(int forzaMassima) {
		this.forzaMassima = forzaMassima;
	}

	public int getDestrezza() {
		return destrezza;
	}

	public void setDestrezza(int destrezza) {
		this.destrezza = destrezza;
	}

	public int getDestrezzaMassima() {
		return destrezzaMassima;
	}

	public void setDestrezzaMassima(int destrezzaMassima) {
		this.destrezzaMassima = destrezzaMassima;
	}

	public int getCostituzione() {
		return costituzione;
	}

	public void setCostituzione(int costituzione) {
		this.costituzione = costituzione;
	}

	public int getCostituzioneMassima() {
		return costituzioneMassima;
	}

	public void setCostituzioneMassima(int costituzioneMassima) {
		this.costituzioneMassima = costituzioneMassima;
	}

	public int getIntelligenza() {
		return intelligenza;
	}

	public void setIntelligenza(int intelligenza) {
		this.intelligenza = intelligenza;
	}

	public int getIntelligenzaMassima() {
		return intelligenzaMassima;
	}

	public void setIntelligenzaMassima(int intelligenzaMassima) {
		this.intelligenzaMassima = intelligenzaMassima;
	}

	public int getSaggezza() {
		return saggezza;
	}

	public void setSaggezza(int saggezza) {
		this.saggezza = saggezza;
	}

	public int getSaggezzaMassima() {
		return saggezzaMassima;
	}

	public void setSaggezzaMassima(int saggezzaMassima) {
		this.saggezzaMassima = saggezzaMassima;
	}

	public int getCarisma() {
		return carisma;
	}

	public void setCarisma(int carisma) {
		this.carisma = carisma;
	}

	public int getCarismaMassimo() {
		return carismaMassimo;
	}

	public void setCarismaMassimo(int carismaMassimo) {
		this.carismaMassimo = carismaMassimo;
	}

	public int getFortuna() {
		return fortuna;
	}

	public void setFortuna(int fortuna) {
		this.fortuna = fortuna;
	}

	public int getFortunaMassima() {
		return fortunaMassima;
	}

	public void setFortunaMassima(int fortunaMassima) {
		this.fortunaMassima = fortunaMassima;
	}

	public int getCritico() {
		return critico;
	}

	public void setCritico(int critico) {
		this.critico = critico;
	}

	public int getCriticoMassimo() {
		return criticoMassimo;
	}

	public void setCriticoMassimo(int criticoMassimo) {
		this.criticoMassimo = criticoMassimo;
	}

	public int getPrecisione() {
		return precisione;
	}

	public void setPrecisione(int precisione) {
		this.precisione = precisione;
	}

	public int getPrecisioneMassima() {
		return precisioneMassima;
	}

	public void setPrecisioneMassima(int precisioneMassima) {
		this.precisioneMassima = precisioneMassima;
	}

	public int getVelocita() {
		return velocita;
	}

	public void setVelocita(int velocita) {
		this.velocita = velocita;
	}

	public int getVelocitaMassima() {
		return velocitaMassima;
	}

	public void setVelocitaMassima(int velocitaMassima) {
		this.velocitaMassima = velocitaMassima;
	}

	public int getFurtivita() {
		return furtivita;
	}

	public void setFurtivita(int furtivita) {
		this.furtivita = furtivita;
	}

	public int getFurtivitaMassima() {
		return furtivitaMassima;
	}

	public void setFurtivitaMassima(int furtivitaMassima) {
		this.furtivitaMassima = furtivitaMassima;
	}

	public int getParata() {
		return parata;
	}

	public void setParata(int parata) {
		this.parata = parata;
	}

	public int getParataMassima() {
		return parataMassima;
	}

	public void setParataMassima(int parataMassima) {
		this.parataMassima = parataMassima;
	}

	public int getResistenzaMagica() {
		return resistenzaMagica;
	}

	public void setResistenzaMagica(int resistenzaMagica) {
		this.resistenzaMagica = resistenzaMagica;
	}

	public int getResistenzaMagicaMassima() {
		return resistenzaMagicaMassima;
	}

	public void setResistenzaMagicaMassima(int resistenzaMagicaMassima) {
		this.resistenzaMagicaMassima = resistenzaMagicaMassima;
	}

	public int getPercezione() {
		return percezione;
	}

	public void setPercezione(int percezione) {
		this.percezione = percezione;
	}

	public int getPercezioneMassima() {
		return percezioneMassima;
	}

	public void setPercezioneMassima(int percezioneMassima) {
		this.percezioneMassima = percezioneMassima;
	}

	public int getSoggezione() {
		return soggezione;
	}

	public void setSoggezione(int soggezione) {
		this.soggezione = soggezione;
	}

	public int getSoggezioneMassima() {
		return soggezioneMassima;
	}

	public void setSoggezioneMassima(int soggezioneMassima) {
		this.soggezioneMassima = soggezioneMassima;
	}

	public int getFuria() {
		return furia;
	}

	public void setFuria(int furia) {
		this.furia = furia;
	}

	public int getFuriaMassima() {
		return furiaMassima;
	}

	public void setFuriaMassima(int furiaMassima) {
		this.furiaMassima = furiaMassima;
	}

	public int getCarico() {
		return carico;
	}

	public void setCarico(int carico) {
		this.carico = carico;
	}

	public int getCaricoMassimo() {
		return caricoMassimo;
	}

	public void setCaricoMassimo(int caricoMassimo) {
		this.caricoMassimo = caricoMassimo;
	}

	public int getMagia() {
		return magia;
	}

	public void setMagia(int magia) {
		this.magia = magia;
	}

	public int getMagiaMassima() {
		return magiaMassima;
	}

	public void setMagiaMassima(int magiaMassima) {
		this.magiaMassima = magiaMassima;
	}

	public int getCoraggio() {
		return coraggio;
	}

	public void setCoraggio(int coraggio) {
		this.coraggio = coraggio;
	}

	public int getValore() {
		return valore;
	}

	public void setValore(int valore) {
		this.valore = valore;
	}

	public int getStanchezza() {
		return stanchezza;
	}

	public void setStanchezza(int stanchezza) {
		this.stanchezza = stanchezza;
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

	public List<EffettoDiStato> getEffettiDiStato() {
		return effettiDiStato;
	}

	public void setEffettiDiStato(List<EffettoDiStato> effettiDiStato) {
		this.effettiDiStato = effettiDiStato;
	}

	public List<ArtefattoMD> getArtefatti() {
		return artefatti;
	}

	public void setArtefatti(List<ArtefattoMD> artefatti) {
		this.artefatti = artefatti;
	}

	@Override
	public void salva(PrintWriter stream) throws IOException {
		stream.print(classe.ordinal());
		stream.print(PIPE);
		stream.print(nome);
		stream.print(PIPE);
		stream.print(vivo ? "vivo" : causaTrapasso);
		stream.print(PIPE);
		stream.print(livello);
		stream.print(PIPE);
		stream.print(esperienza);
		stream.print(PIPE);
		stream.print(salute);
		stream.print(PIPE);
		stream.print(saluteMassima);
		stream.print(PIPE);
		stream.print(magia);
		stream.print(PIPE);
		stream.print(magiaMassima);
		stream.print(PIPE);
		stream.print(carico);
		stream.print(PIPE);
		stream.print(caricoMassimo);
		stream.print(PIPE);
		stream.print(forza);
		stream.print(PIPE);
		stream.print(forzaMassima);
		stream.print(PIPE);
		stream.print(destrezza);
		stream.print(PIPE);
		stream.print(destrezzaMassima);
		stream.print(PIPE);
		stream.print(costituzione);
		stream.print(PIPE);
		stream.print(costituzioneMassima);
		stream.print(PIPE);
		stream.print(intelligenza);
		stream.print(PIPE);
		stream.print(intelligenzaMassima);
		stream.print(PIPE);
		stream.print(saggezza);
		stream.print(PIPE);
		stream.print(saggezzaMassima);
		stream.print(PIPE);
		stream.print(carisma);
		stream.print(PIPE);
		stream.print(carismaMassimo);
		stream.print(PIPE);
		stream.print(fortuna);
		stream.print(PIPE);
		stream.print(fortunaMassima);
		stream.print(PIPE);
		stream.print(critico);
		stream.print(PIPE);
		stream.print(criticoMassimo);
		stream.print(PIPE);
		stream.print(precisione);
		stream.print(PIPE);
		stream.print(precisioneMassima);
		stream.print(PIPE);
		stream.print(velocita);
		stream.print(PIPE);
		stream.print(velocitaMassima);
		stream.print(PIPE);
		stream.print(furtivita);
		stream.print(PIPE);
		stream.print(furtivitaMassima);
		stream.print(PIPE);
		stream.print(parata);
		stream.print(PIPE);
		stream.print(parataMassima);
		stream.print(PIPE);
		stream.print(resistenzaMagica);
		stream.print(PIPE);
		stream.print(resistenzaMagicaMassima);
		stream.print(PIPE);
		stream.print(percezione);
		stream.print(PIPE);
		stream.print(percezioneMassima);
		stream.print(PIPE);
		stream.print(soggezione);
		stream.print(PIPE);
		stream.print(soggezioneMassima);
		stream.print(PIPE);
		stream.print(furia);
		stream.print(PIPE);
		stream.print(furiaMassima);
		stream.print(PIPE);

		stream.print(coraggio);
		stream.print(PIPE);
		stream.print(valore);
		stream.print(PIPE);
		stream.print(stanchezza);
		stream.print(PIPE);
		stream.print(tempo);
		stream.print(PIPE);
		stream.print(artefatti.size());
		stream.print(PIPE);

		Iterator<EffettoDiStato> iterator = effettiDiStato.iterator();
		while (iterator.hasNext()) {
			EffettoDiStato effettoDiStato = iterator.next();
			stream.print(effettoDiStato.getTipoModificatoreAttributo().name());
			stream.print(PIPE);
			stream.print(effettoDiStato.getValore());
			if (iterator.hasNext()) {
				stream.print(PIPE);
			}
		}

		stream.println("");

		for (ArtefattoMD artefatto : artefatti) {
			artefatto.salva(stream);
		}
	}

	@Override
	public void leggi(BufferedReader stream) throws IOException{
		String line = stream.readLine();
		StringTokenizer st = new StringTokenizer(line, PIPE);
		classe = ClassePersonaggio.values()[Integer.parseInt(st.nextToken())];
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
		salute = Integer.parseInt(st.nextToken());
		saluteMassima = Integer.parseInt(st.nextToken());
		magia = Integer.parseInt(st.nextToken());
		magiaMassima = Integer.parseInt(st.nextToken());
		carico = Integer.parseInt(st.nextToken());
		caricoMassimo = Integer.parseInt(st.nextToken());
		forza = Integer.parseInt(st.nextToken());
		forzaMassima = Integer.parseInt(st.nextToken());
		destrezza = Integer.parseInt(st.nextToken());
		destrezzaMassima = Integer.parseInt(st.nextToken());
		costituzione = Integer.parseInt(st.nextToken());
		costituzioneMassima = Integer.parseInt(st.nextToken());
		intelligenza = Integer.parseInt(st.nextToken());
		intelligenzaMassima = Integer.parseInt(st.nextToken());
		saggezza = Integer.parseInt(st.nextToken());
		saggezzaMassima = Integer.parseInt(st.nextToken());
		carisma = Integer.parseInt(st.nextToken());
		carismaMassimo = Integer.parseInt(st.nextToken());
		fortuna = Integer.parseInt(st.nextToken());
		fortunaMassima = Integer.parseInt(st.nextToken());
		critico = Integer.parseInt(st.nextToken());
		criticoMassimo = Integer.parseInt(st.nextToken());
		precisione = Integer.parseInt(st.nextToken());
		precisioneMassima = Integer.parseInt(st.nextToken());
		velocita = Integer.parseInt(st.nextToken());
		velocitaMassima = Integer.parseInt(st.nextToken());
		furtivita = Integer.parseInt(st.nextToken());
		furtivitaMassima = Integer.parseInt(st.nextToken());
		parata = Integer.parseInt(st.nextToken());
		parataMassima = Integer.parseInt(st.nextToken());
		resistenzaMagica = Integer.parseInt(st.nextToken());
		resistenzaMagicaMassima = Integer.parseInt(st.nextToken());
		percezione = Integer.parseInt(st.nextToken());
		percezioneMassima = Integer.parseInt(st.nextToken());
		soggezione = Integer.parseInt(st.nextToken());
		soggezioneMassima = Integer.parseInt(st.nextToken());
		furia = Integer.parseInt(st.nextToken());
		furiaMassima = Integer.parseInt(st.nextToken());
		coraggio = Integer.parseInt(st.nextToken());
		valore = Integer.parseInt(st.nextToken());
		stanchezza = Integer.parseInt(st.nextToken());
		tempo = Integer.parseInt(st.nextToken());
		int numeroArtefatti = Integer.parseInt(st.nextToken());

		while (st.hasMoreTokens()) {
			EffettoDiStato effettoDiStato = new EffettoDiStato(TipoEffettoDiStato.valueOf(st.nextToken()), Integer.parseInt(st.nextToken()));
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
