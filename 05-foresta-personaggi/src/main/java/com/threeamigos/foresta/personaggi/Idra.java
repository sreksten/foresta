package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.tipi.TipoIncantesimo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.EnumSet;
import java.util.Set;

/**
 * L'idra: bestia gigantesca, boss del suo castello. Parte con salute e magia piene; è immune agli incantesimi di Terra, Acqua e
 * Aria e agli stati confuso e stordito. Combatte soprattutto con le sue teste, cioè con le armi: di magie ne sa due, il Veleno e l'Acqua.
 */
public class Idra extends PersonaggioBase implements Personaggio {

	public TipoPersonaggio getClasse() { return TipoPersonaggio.IDRA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Idra(int livello) {
		super(TipoPersonaggio.IDRA, livello);
	}

	@Override
	protected void impostaValoriDiPartenza() {

		setQuantitaMassima(CostantiPersonaggi.IDRA_MAX_NUMERO);
	}

	@Override
	public Set<TipoIncantesimo> getRepertorioIncantesimi() {
		return EnumSet.of(TipoIncantesimo.VELENO, TipoIncantesimo.ACQUA);
	}

	@Override
	public int getPuntiEsperienza() {
		return CostantiPersonaggi.IDRA_PUNTI_ESPERIENZA;
	}

	@Override
	public double getSaluteBase() {
		return CostantiPersonaggi.IDRA_SALUTE_BASE;
	}

	@Override
	public double getMagiaBase() {
		return CostantiPersonaggi.IDRA_MAGIA_BASE;
	}

	@Override
	public double getLivellamentoMagia() {
		return CostantiPersonaggi.IDRA_LIVELLAMENTO_MAGIA;
	}

	@Override
	public double getMoltiplicatoreCarico() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_CARICO;
	}

	@Override
	public double getMoltiplicatoreCritico() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_CRITICO;
	}

	@Override
	public double getMoltiplicatorePrecisione() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_PRECISIONE;
	}

	@Override
	public double getMoltiplicatoreVelocita() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_VELOCITA;
	}

	@Override
	public double getMoltiplicatoreFurtivita() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_FURTIVITA;
	}

	@Override
	public double getMoltiplicatoreParata() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_PARATA;
	}

	@Override
	public double getMoltiplicatoreResistenzaMagica() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_RESISTENZA_MAGICA;
	}

	@Override
	public double getMoltiplicatorePercezione() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_PERCEZIONE;
	}

	@Override
	public double getMoltiplicatoreSoggezione() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_SOGGEZIONE;
	}

	@Override
	public double getMoltiplicatoreFuria() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_FURIA;
	}

	@Override
	public double getMoltiplicatoreCoraggio() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_CORAGGIO;
	}

	@Override
	public double getMoltiplicatoreValore() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_VALORE;
	}

	@Override
	public double getMoltiplicatoreNumeroBersagli() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_NUMERO_BERSAGLI;
	}

	@Override
	public double getMoltiplicatoreStanchezza() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_STANCHEZZA;
	}

	@Override
	public double getMoltiplicatoreDanniMagici() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_DANNI_MAGICI;
	}

	@Override
	public double getMoltiplicatoreDanniFisici() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_DANNI_FISICI;
	}

	@Override
	public double getMoltiplicatoreRecuperoMagico() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_RECUPERO_MAGICO;
	}

	@Override
	public double getMoltiplicatoreRecuperoFisico() {
		return CostantiPersonaggi.IDRA_MOLTIPLICATORE_RECUPERO_FISICO;
	}

	@Override
	public double getMaxIntelligenza() {
		return CostantiPersonaggi.IDRA_MAX_INTELLIGENZA;
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}
}
