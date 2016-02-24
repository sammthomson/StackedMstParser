/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
    @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
*/

package mst;

import gnu.trove.TObjectIntProcedure;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Alphabet implements Serializable {
    gnu.trove.TObjectIntHashMap map;
    int numEntries;
    boolean growthStopped = false;

    public Alphabet (int capacity) {
        this.map = new gnu.trove.TObjectIntHashMap (capacity);
        //this.map.setDefaultValue(-1);

        numEntries = 0;
    }

    public Alphabet ()
    {
	this (10000);
    }

	
    /** Return -1 if entry isn't present. */
    public int lookupIndex (Object entry)
    {
        if (entry == null) {
            throw new IllegalArgumentException ("Can't lookup \"null\" in an Alphabet.");
        }

        int ret = map.get(entry);

        if (ret == -1 && !growthStopped) {
            ret = numEntries;
            map.put (entry, ret);
            numEntries++;
        }

        return ret;
    }

    public Object[] toArray () {
	return map.keys();
    }

    public boolean contains (Object entry)
    {
	return map.contains (entry);
    }

    public int size ()
    {
	return numEntries;
    }

    public void stopGrowth ()
    {
	growthStopped = true;
	map.compact();
    }

    public void allowGrowth ()
    {
	growthStopped = false;
    }

    public boolean growthStopped ()
    {
	return growthStopped;
    }

    public static Pair<Alphabet, double[]> removeZeros(Alphabet alphabet, final double[] params) {
        // count nonZeros
        int numNonZeros = 0;
        for (double param : params) {
            if (param != 0.0) numNonZeros++;
        }
        if (numNonZeros == alphabet.numEntries) {
            // no zero entries, nothing to do
            return Pair.of(alphabet, params);
        } else {
            // make a new Alphabet and array with only the non-zero entries
            final Alphabet newAlphabet = new Alphabet(numNonZeros);
            final double[] newParams = new double[numNonZeros];
            alphabet.map.forEachEntry(new TObjectIntProcedure() {
                public boolean execute(Object paramName, int paramIdx) {
                    final double newParamVal = params[paramIdx];
                    if (newParamVal != 0.0) {
                        newAlphabet.map.put(paramName, newAlphabet.numEntries);
                        newParams[newAlphabet.numEntries] = newParamVal;
                        newAlphabet.numEntries++;
                    }
                    return true;
                }
            });
            newAlphabet.growthStopped = alphabet.growthStopped;
            return Pair.of(newAlphabet, newParams);
        }
    }

    // Serialization 
		
    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject (ObjectOutputStream out) throws IOException {
        out.writeInt (CURRENT_SERIAL_VERSION);
        out.writeInt (numEntries);
        out.writeObject(map);
        out.writeBoolean (growthStopped);
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt ();
        numEntries = in.readInt();
        map = (gnu.trove.TObjectIntHashMap)in.readObject();
        growthStopped = in.readBoolean();
    }
}
