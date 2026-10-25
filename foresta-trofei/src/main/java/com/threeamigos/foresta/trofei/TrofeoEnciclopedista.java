package com.threeamigos.foresta.trofei;

import com.threeamigos.foresta.bestiario.RegistroBestiario;
import com.threeamigos.foresta.tipi.TipoTrofeo;

/**
 * Si vince quando tutte le voci del bestiario sono sbloccate. Non è un trofeo a contatore: il progresso non è quello accumulato dal registro dei
 * trofei, che si perde se il gruppo muore nella locazione, ma le voci che il bestiario ha già in tutte le partite.
 */
public class TrofeoEnciclopedista implements Trofeo {

	@Override
	public TipoTrofeo getTipo() {
		return TipoTrofeo.ENCICLOPEDISTA;
	}

	@Override
	public void registrati() {
		// Niente da ascoltare: il bestiario sa già quante voci sono sbloccate, e il trofeo si controlla a fine locazione
	}

	@Override
	public boolean isMeritato() {
		return RegistroBestiario.getNumeroVociSbloccate() >= RegistroBestiario.getNumeroVoci();
	}

	@Override
	public int getObiettivo() {
		return RegistroBestiario.getNumeroVoci();
	}

	@Override
	public int getProgresso() {
		return Math.min(getObiettivo(), RegistroBestiario.getNumeroVociSbloccate());
	}
}
