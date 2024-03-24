package com.threeamigos.foresta.tools;

/**
 * Una classe per ottenere numeri casuali
 */
public class Random {

	private Random() {
	}

	private static final java.util.Random instance = new java.util.Random(System.currentTimeMillis());

	public static int getInt(int maxValue) {
		if (maxValue == 0)
			return 0;
		int t = instance.nextInt();
		if (t < 0)
			t = -t;
		t %= maxValue;
		return t;
	}
}
