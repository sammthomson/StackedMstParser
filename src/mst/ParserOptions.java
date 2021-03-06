///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008 Carnegie Mellon University and 
// (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
// 
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////

package mst;

import java.io.File;

/**
 * Hold all the options for the parser so they can be passed around easily.
 *
 * <p>
 * Created: Sat Nov 10 15:25:10 2001
 * </p>
 *
 * @author Jason Baldridge
 * @version $Id: CONLLReader.java 103 2007-01-21 20:26:39Z jasonbaldridge $
 * @see mstparser.io.DependencyReader
 * <p>
 * Modified: Thu Sep 25 17:45:10 2008
 * @author Andre Martins
 * </p>
 * 
 */



public final class ParserOptions {
    
    public String trainfile = null;
    public String testfile = null;
    public File trainforest = null;
    public File testforest = null;
    public boolean train = false;
    public boolean eval = false;
    public boolean test = false;
    public String modelName = "dep.model";
    public String lossType = "punc";
    public boolean createForest = true;
    public String decodeType = "proj";
    public String format = "CONLL";
    public int numIters = 10;
    public String outfile = "out.txt";
    public String goldfile = null;
    public int trainK = 1;
    public int testK = 1;
    public int augmentNumParts = 2; // afm 03-03-08 --- Number of partitions
    public boolean stackedLevel0 = false; // afm 03-10-08 --- true for training/testing the level-0 classifier (if train, augment training data with output predictions)
    public boolean stackedLevel1 = false; // afm 03-10-08 --- true for training/testing the level-1 classifier
    public boolean secondOrder = false;
    public boolean useRelationalFeatures = false;
    public boolean discourseMode = false;
    public boolean separateLab = false; // afm 06-03-08 --- Perform labeling in a separate stage (using MALLET)
    public StackedFeaturesOptions stackedFeats = null;
    public int separateLabCutOff = 0;
    public boolean composeFeaturesWithPOS = false; // afm 09-25-08 --- If true, compose features just with POS tags instead of composing them with lemmas and words. 
    public boolean useStemmingIfLemmasAbsent = false; // afm 09-25-08 --- If lemmas are not available, use the first three characters of the words instead
    public ParserOptions (String[] args) {

    stackedFeats = new StackedFeaturesOptions();
    	
	for(int i = 0; i < args.length; i++) {
	    String[] pair = args[i].split(":");
	    if (pair[0].equals("train")) {
		train = true;
	    }
	    if (pair[0].equals("eval")) {
		eval = true;
	    }
	    if (pair[0].equals("test")) {
		test = true;
	    }
	    if (pair[0].equals("iters")) {
		numIters = Integer.parseInt(pair[1]);
	    }
	    if (pair[0].equals("output-file")) {
		outfile = pair[1];
	    }
	    if (pair[0].equals("gold-file")) {
		goldfile = pair[1];
	    }
	    if (pair[0].equals("train-file")) {
		trainfile = pair[1];
	    }
	    if (pair[0].equals("test-file")) {
		testfile = pair[1];
	    }
	    if (pair[0].equals("model-name")) {
			modelName = pair[1];
	    }
		if (pair[0].equals("training-k")) {
		trainK = Integer.parseInt(pair[1]);
	    }
	    if (pair[0].equals("augment-nparts")) {
	    augmentNumParts  = Integer.parseInt(pair[1]);
	    }
	    if (pair[0].equals("stacked-level0")) {
		stackedLevel0 = true;
		}
	    if (pair[0].equals("stacked-level1"))
	    {
	    	stackedLevel1 = true;
		}
	    if (pair[0].equals("separate-lab")) {
	    	separateLab = true;
	    }
	    if (pair[0].equals("separate-lab-cutoff"))
	    {
	    	separateLabCutOff = Integer.parseInt(pair[1]);
		}
	    if (pair[0].equals("compose-features-with-pos")) {
			composeFeaturesWithPOS = true;
	    }			
	    if (pair[0].equals("use-stemming-if-lemmas-absent")) {
			useStemmingIfLemmasAbsent = true;
	    }			
	    if (pair[0].equals("loss-type")) {
		lossType = pair[1];
	    }			
	    if (pair[0].equals("order") && pair[1].equals("2")) {
		secondOrder = true;
	    }			
	    if (pair[0].equals("create-forest")) {
		createForest = pair[1].equals("true") ? true : false;
	    }			
	    if (pair[0].equals("decode-type")) {
		decodeType = pair[1];
	    }			
	    if (pair[0].equals("format")) {
		format = pair[1];
	    }			
	    if (pair[0].equals("relational-features")) {
		useRelationalFeatures = pair[1].equals("true") ? true : false;
	    }			
	    if (pair[0].equals("discourse-mode")) {
			discourseMode = pair[1].equals("true") ? true : false;
		}			
	    if (pair[0].equals("stackedfeat-pred-edge")) {
			stackedFeats.usePredEdge = pair[1].equals("1") ? true : false;
		}			
	    if (pair[0].equals("stackedfeat-prev-sibl")) {
	    	stackedFeats.usePrevSibl = pair[1].equals("1") ? true : false;
		    }			
	    if (pair[0].equals("stackedfeat-next-sibl")) {
	    	stackedFeats.useNextSibl = pair[1].equals("1") ? true : false;
		    }			
	    if (pair[0].equals("stackedfeat-labels")) {
	    	stackedFeats.useLabels = pair[1].equals("1") ? true : false;
		    }			
	    if (pair[0].equals("stackedfeat-grandparents")) {
	    	stackedFeats.useGrandparents = pair[1].equals("1") ? true : false;
		    }			
	    if (pair[0].equals("stackedfeat-valency")) {
	    	stackedFeats.useValency = pair[1].equals("1") ? true : false;
		    }			
	    if (pair[0].equals("stackedfeat-allchildren")) {
	    	stackedFeats.useAllChildren = pair[1].equals("1") ? true : false;
		    }
	    if (pair[0].equals("stackedfeat-pred-head")) {
	    	stackedFeats.usePredHead = pair[1].equals("1") ? true : false;
		    }
	}

	if (stackedLevel1)
		stackedFeats.display();
	
	try {
	    File tmpDir = new File("/tmp");
	    if (null != trainfile) {
		trainforest = File.createTempFile("train", ".forest");
		trainforest.deleteOnExit();
	    }

	    if (null != testfile) {
		testforest = File.createTempFile("test", ".forest");
		testforest.deleteOnExit();
	    }

	} catch (java.io.IOException e) {
	    System.out.println("Unable to create tmp files for feature forests!");
	    System.out.println(e);
	    System.exit(0);
	}
    }


    public String toString () {
	StringBuilder sb = new StringBuilder();
	sb.append("FLAGS [");
	sb.append("train-file: " + trainfile);
	sb.append(" | ");
	sb.append("test-file: " + testfile);
	sb.append(" | ");
	sb.append("gold-file: " + goldfile);
	sb.append(" | ");
	sb.append("output-file: " + outfile);
	sb.append(" | ");
	sb.append("model-name: " + modelName);
	sb.append(" | ");
	sb.append("train: " + train);
	sb.append(" | ");
	sb.append("test: " + test);
	sb.append(" | ");
	sb.append("eval: " + eval);
	sb.append(" | ");
	sb.append("loss-type: " + lossType);
	sb.append(" | ");
	sb.append("second-order: " + secondOrder);
	sb.append(" | ");
	sb.append("training-iterations: " + numIters);
	sb.append(" | ");
	sb.append("training-k: " + trainK);
	sb.append(" | ");
	sb.append("decode-type: " + decodeType);
	sb.append(" | ");
	sb.append("create-forest: " + createForest);
	sb.append(" | ");
	sb.append("format: " + format);
	sb.append(" | ");
	sb.append("relational-features: " + useRelationalFeatures);
	sb.append(" | ");
	sb.append("discourse-mode: " + discourseMode);
	sb.append("]\n");
	return sb.toString();
    }
}
