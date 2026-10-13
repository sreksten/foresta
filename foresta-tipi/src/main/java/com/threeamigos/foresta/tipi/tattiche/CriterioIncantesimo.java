package com.threeamigos.foresta.tipi.tattiche;

/**
 * Come <code>INCANTESIMO_DI_DANNO</code> sceglie quale dei sette incantesimi elementali lanciare (tattiche_di_combattimento_automatiche.md §11.10).
 */
public enum CriterioIncantesimo {
	/** S1: uno a caso fra quelli lanciabili */
	A_CASO,
	/** S2: quello con più danno base, senza resistenze né debolezze (come oggi) */
	PIU_POTENTE,
	/** S3: quello con il danno atteso più alto su quel bersaglio (debolezze e resistenze di classe e stati); con più bersagli il danno totale */
	PIU_ADATTO,
	/** S4: come S3 con il fattore dell'equipaggiamento osservato; con più bersagli conta chi verrebbe portato vicino alla fine */
	PIU_ADATTO_ESPERTO,
	/** Fra quelli che bastano a uccidere il bersaglio il meno caro; se nessuno basta, il PIU_ADATTO_ESPERTO */
	PIU_ECONOMICO_CHE_BASTA
}
