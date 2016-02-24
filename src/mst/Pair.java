package mst;


import java.util.Arrays;

import static mst.Util.objectsEqual;


/**
 * Stores two objects.
 * @param <A> the type of the first object
 * @param <B> the type of the second object
 */
public class Pair<A, B> implements java.io.Serializable {
	private static final long serialVersionUID = 5036185774790301596L;
	
	public final A fst;
	public final B snd;
	
	
	public Pair(A o1, B o2) {
		fst = o1;
		snd = o2;
	}

	public static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<A, B>(a, b);
	}

	@Override
	public boolean equals(Object that) {
		if (that == null || !(that instanceof Pair)) return false;
		Pair thatPair = (Pair) that;
		return objectsEqual(fst, thatPair.fst) && objectsEqual(snd, thatPair.snd);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[]{fst, snd});
	}
	
	@Override
	public Pair<A, B> clone() throws CloneNotSupportedException {
		return new Pair<A, B>(fst, snd);
	}
	
	@Override
	public String toString() {
		return "Pair(" + fst + ", " + snd + ")";
	}
}
