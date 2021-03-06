package mst;


import edu.umass.cs.mallet.base.classify.Classifier;
import mst.mallet.LabelClassifier;

import java.io.*;

import static mst.Util.getCoNLLFormat;


public class DependencyParser {

	public ParserOptions options;

	private DependencyPipe pipe;
	private DependencyDecoder decoder;
	protected Parameters params;
	private Classifier classifier;

	public DependencyParser(DependencyPipe pipe, ParserOptions options) {
		this.pipe = pipe;
		this.options = options;

		// Set up arrays
		params = new Parameters(pipe.dataAlphabet.size());
		decoder = options.secondOrder ? 
				new DependencyDecoder2O(pipe) : new DependencyDecoder(pipe);
	}
	

	// afm 03-06-08 --- Count the real number of instances to be considered
	public int countActualInstances(int ignore[]) 
	{
		int i;
		int numInstances = ignore.length;
		int numActualInstances = 0;
		for (i = 0; i < numInstances; i++) {
			if (ignore[i] == 0) // This sentence is not to be ignored
				numActualInstances++;
		}
		return numActualInstances;
	}

	public void augment(int[] instanceLengths,
	                    String trainfile,
	                    File train_forest,
	                    int numParts) throws IOException {

		int i, j;
		int[] ignore = new int[instanceLengths.length];

		int numInstances = instanceLengths.length;
		int numInstancesPerPart = numInstances / numParts; // The last partition becomes bigger
		pipe.initOutputFile(options.outfile); // Initialize the output file once

		for(j = 0; j < numParts; j++) {
			System.out.println("Training classifier for partition " + j);

			// Make partition
			for (i = 0; i < numInstances; i++) {
				if (i >= j * numInstancesPerPart &&
						i < (j+1) * numInstancesPerPart)
					ignore[i] = 1; // Mark to ignore this instance in training
				else
					ignore[i] = 0;
			}

			// Train on one split
			params = new Parameters(pipe.dataAlphabet.size());
			train(instanceLengths, ignore,trainfile, train_forest);		
			
			// Test on the other split
			System.out.println("Making predictions for partition " + j);

			for (i = 0; i < numInstances; i++)
				ignore[i] = 1 - ignore[i]; // Toggle ignore
			parseFile(ignore);
		}

		pipe.close(); // Close the output file once
	}



	public void train(int[] instanceLengths,
	                  int[] ignore,
	                  String trainfile,
	                  File train_forest) throws IOException {
		int i;
		for(i = 0; i < options.numIters; i++) {
			System.out.print(" Iteration "+i);
			System.out.print("[");

			long start = System.currentTimeMillis();
			
			trainingIter(instanceLengths,ignore, train_forest,i+1);

			long end = System.currentTimeMillis();
			//System.out.println("Training iter took: " + (end-start));
			System.out.println("|Time:"+(end-start)+"]");
		}
		params.averageParams(i * countActualInstances(ignore));
		//	 afm 06-04-08
		if (options.separateLab) {
			LabelClassifier oc = new LabelClassifier(options, instanceLengths,ignore,trainfile,train_forest,this, pipe);
			try {
				classifier = oc.trainClassifier(100);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Note: Change this to pass -1 for indices in instanceLengths[] that you
	// don't want to use on training (need to be careful because i is being used
	// in the for loop; need new index)
	private void trainingIter(int[] instanceLengths,
	                          int ignore[],
	                          File train_forest,
	                          int iter) throws IOException {

		final ObjectInputStream in = new ObjectInputStream(new FileInputStream(train_forest));

		int numInstances = instanceLengths.length;

		// afm -- Count the real number of instances to be considered
		int numActualInstances = countActualInstances(ignore);

		int j = 0;
		for(int i = 0; i < numInstances; i++) {
			if((i+1) % 500 == 0) {
				System.out.print((i+1)+",");
				//System.out.println("  "+(i+1)+" instances");
			}
			
			int length = instanceLengths[i];


			// Get production crap.
			FeatureVector[][][] fvs = new FeatureVector[length][length][2];
			double[][][] probs = new double[length][length][2];
			FeatureVector[][][][] nt_fvs = new FeatureVector[length][pipe.types.length][2][2];
			double[][][][] nt_probs = new double[length][pipe.types.length][2][2];
			FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
			double[][][] probs_trips = new double[length][length][length];
			FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
			double[][][] probs_sibs = new double[length][length][2];

			DependencyInstance inst;

			if(options.secondOrder) {
				inst = ((DependencyPipe2O)pipe).readInstance(in,length,fvs,probs,
						fvs_trips,probs_trips,
						fvs_sibs,probs_sibs,
						nt_fvs,nt_probs,params);
			}

			else
				inst = pipe.readInstance(in,length,fvs,probs,nt_fvs,nt_probs,params);

			// afm 03-06-08
			if (ignore[i] != 0) // This sentence is to be ignored
				continue;

			double upd = (double)(options.numIters*numActualInstances - (numActualInstances*(iter-1)+(j+1)) + 1);
			int K = options.trainK;
			Object[][] d = null;
			if(options.decodeType.equals("proj")) {
				if(options.secondOrder)
					d = ((DependencyDecoder2O)decoder).decodeProjective(inst,fvs,probs,
							fvs_trips,probs_trips,
							fvs_sibs,probs_sibs,
							nt_fvs,nt_probs,K);
				else
					d = decoder.decodeProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
			}
			if(options.decodeType.equals("non-proj")) {
				if(options.secondOrder)
					d = ((DependencyDecoder2O)decoder).decodeNonProjective(inst,fvs,probs,
							fvs_trips,probs_trips,
							fvs_sibs,probs_sibs,
							nt_fvs,nt_probs,K);
				else
					d = decoder.decodeNonProjective(inst,fvs,probs,nt_fvs,nt_probs,K);
			}
			params.updateParamsMIRA(inst,d,upd);
			j++;
		}

		System.out.print(numActualInstances);

		in.close();

	}

	///////////////////////////////////////////////////////
	// Saving and loading models
	///////////////////////////////////////////////////////
	public void saveModel(String file) throws IOException {
		final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		// Only save features with non-zero weights
		final Pair<Alphabet, double[]> nonZeros = Alphabet.removeZeros(pipe.dataAlphabet, params.parameters);
		out.writeObject(nonZeros.snd);
		out.writeObject(nonZeros.fst);
		out.writeObject(pipe.typeAlphabet);

		// afm 06-04-08
		if (options.separateLab) {
			out.writeObject(classifier);
		}

		out.close();
	}

	public void loadModel(String file) throws Exception {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		params.parameters = (double[])in.readObject();
		pipe.dataAlphabet = (Alphabet)in.readObject();
		pipe.typeAlphabet = (Alphabet)in.readObject();

		// afm 06-04-08
		if (options.separateLab) {
			classifier = (Classifier)in.readObject();
		}

		in.close();
		pipe.closeAlphabets();
	}


	//////////////////////////////////////////////////////
	// Get Best Parse ///////////////////////////////////
	//////////////////////////////////////////////////////

	public DependencyInstance getBestParse(DependencyInstance unparsedSentence) {
		final LabelClassifier labelClassifier = new LabelClassifier(options);

		final String[] forms = unparsedSentence.forms;
		int length = forms.length;

		FeatureVector[][][] fvs = new FeatureVector[forms.length][forms.length][2];
		double[][][] probs = new double[forms.length][forms.length][2];
		FeatureVector[][][][] nt_fvs = new FeatureVector[forms.length][pipe.types.length][2][2];
		double[][][][] nt_probs = new double[forms.length][pipe.types.length][2][2];
		FeatureVector[][][] fvs_trips = new FeatureVector[length][length][length];
		double[][][] probs_trips = new double[length][length][length];
		FeatureVector[][][] fvs_sibs = new FeatureVector[length][length][2];
		double[][][] probs_sibs = new double[length][length][2];
		if(options.secondOrder) {
			((DependencyPipe2O) pipe).fillFeatureVectors(
					unparsedSentence,
					fvs,
					probs,
					fvs_trips,
					probs_trips,
					fvs_sibs,
					probs_sibs,
					nt_fvs,
					nt_probs,
					params);
		} else {
			pipe.fillFeatureVectors(
					unparsedSentence,
					fvs,
					probs,
					nt_fvs,
					nt_probs,
					params);
		}

		int K = options.testK;
		Object[][] decoded;
		if(options.decodeType.equals("proj")) {
			if(options.secondOrder) {
				decoded = ((DependencyDecoder2O) decoder).decodeProjective(
						unparsedSentence,
						fvs,
						probs,
						fvs_trips,
						probs_trips,
						fvs_sibs,
						probs_sibs,
						nt_fvs,
						nt_probs,
						K);
			} else {
				decoded = decoder.decodeProjective(
						unparsedSentence,
						fvs,
						probs,
						nt_fvs,
						nt_probs,
						K);
			}
		} else {
			assert(options.decodeType.equals("non-proj"));
			if(options.secondOrder) {
				decoded = ((DependencyDecoder2O)decoder).decodeNonProjective(
						unparsedSentence,
						fvs,
						probs,
						fvs_trips,
						probs_trips,
						fvs_sibs,
						probs_sibs,
						nt_fvs,
						nt_probs,
						K);
			} else {
				decoded = decoder.decodeNonProjective(
						unparsedSentence,
						fvs,
						probs,
						nt_fvs,
						nt_probs,
						K);
			}
		}

		String[] res = ((String) decoded[0][1]).split(" ");
		String[] pos = unparsedSentence.cpostags;

		String[] formsNoRoot = new String[forms.length-1];
		String[] posNoRoot = new String[formsNoRoot.length];
		String[] labels = new String[formsNoRoot.length];
		int[] heads = new int[formsNoRoot.length];

		for(int j = 0; j < formsNoRoot.length; j++) {
			formsNoRoot[j] = forms[j+1];
			posNoRoot[j] = pos[j+1];

			final String[] trip = res[j].split("[\\|:]");
			labels[j] = pipe.types[Integer.parseInt(trip[2])];
			heads[j] = Integer.parseInt(trip[0]);
		}

		// afm 06-04-08
		if (options.separateLab) {
			// ask whether instance contains level0 information
			//Note, forms and pos have the root. labels and heads do not
			final String[] deprelsPred = options.stackedLevel1 ? unparsedSentence.deprels_pred : null;
			final int[] headsPred = options.stackedLevel1 ? unparsedSentence.heads_pred : null;
			labels = labelClassifier.outputLabels(
					classifier,
					unparsedSentence.forms,
					unparsedSentence.postags,
					labels,
					heads,
					deprelsPred,
					headsPred,
					unparsedSentence);
		}

		// afm 03-07-08
		//if (ignore == null)
		DependencyInstance out_inst;
		if (!options.stackedLevel0) {
			out_inst = new DependencyInstance(formsNoRoot, posNoRoot, labels, heads);
		} else {
			int[] headsNoRoot = new int[unparsedSentence.heads.length-1];
			String[] labelsNoRoot = new String[unparsedSentence.heads.length-1];
			for (int j = 0; j < headsNoRoot.length; j++) {
				headsNoRoot[j] = unparsedSentence.heads[j+1];
				labelsNoRoot[j] = unparsedSentence.deprels[j+1];
			}
			out_inst = new DependencyInstance(formsNoRoot, posNoRoot, labelsNoRoot, headsNoRoot);
			out_inst.stacked = true;
			out_inst.heads_pred = heads;
			out_inst.deprels_pred = labels;
		}
		return out_inst;
	}

	/**
	 * Go straight from POS-tagged input to dep-parsed CoNLL output.
	 * @param line one line in the form "form_pos form_pos..."
	 * @return the parsed sentence in CoNLL format
	 */
	public String parsePosTaggedLine(String line) {
		pipe.depReader.startReadingString(getCoNLLFormat(line));
		DependencyInstance unparsed = null;
		try {
			unparsed = pipe.nextInstance();
		} catch (IOException e) { /* ain't never gonna happen */ }
		final DependencyInstance parsed = getBestParse(unparsed);
		return pipe.depWriter.encode(parsed);
	}

	public void parseFile(int[] ignore) throws IOException {
		final String tFile = options.testfile;
		final String file = options.outfile;

		final long start = System.currentTimeMillis();

		pipe.initInputFile(tFile);
		//if (ignore == null) // afm 03-07-2008 --- If this is called for each partition, must have initialized output file before
		if (!options.train || !options.stackedLevel0) // afm 03-07-2008 --- If this is called for each partition, must have initialized output file before
			pipe.initOutputFile(file);

		System.out.print("Processing Sentence: ");
		DependencyInstance instance = pipe.nextInstance();
		int cnt = 0;
		int i = 0;
		while(instance != null) {
			cnt++;
			System.out.print(cnt+" ");

			// afm 03-07-08 --- If this instance is to be ignored, just go for the next one
			if (ignore != null && ignore[i] != 0) {
				instance = pipe.nextInstance();
				i++;
				continue;
			}

			final DependencyInstance parsedInstance = getBestParse(instance);
			pipe.outputInstance(parsedInstance);

			instance = pipe.nextInstance();
			i++;
		}
		//if (ignore == null) // afm 03-07-2008 --- If this is called for each partition (ignore != null), must close pipe outside the loop
		if (!options.train || !options.stackedLevel0) // afm 03-07-2008 --- If this is called for each partition (ignore != null), must close pipe outside the loop
			pipe.close();

		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end-start));
	}

	/////////////////////////////////////////////////////
	// RUNNING THE PARSER
	////////////////////////////////////////////////////
	public static void main (String[] args) throws Exception {
		System.setProperty("java.io.tmpdir", "./tmp/");
		ParserOptions options = new ParserOptions(args);
		System.out.println("Default temp directory:" + System.getProperty("java.io.tmpdir"));
		
		System.out.println("Separate labeling: " + options.separateLab);	
		
		if (options.train) {
			DependencyPipe pipe = options.secondOrder ? 
					new DependencyPipe2O (options) : new DependencyPipe (options);
			int[] instanceLengths = 
				pipe.createInstances(options.trainfile, options.trainforest);
			pipe.closeAlphabets();
			DependencyParser dp = new DependencyParser(pipe, options);
			//pipe.printModelStats(null);
			int numFeats = pipe.dataAlphabet.size();
			int numTypes = pipe.typeAlphabet.size();
			System.out.print("Num Feats: " + numFeats);	
			System.out.println(".\tNum Edge Labels: " + numTypes);
			// Augment training data with output predictions, for stacked learning (afm 03-03-08)
			if (options.stackedLevel0) {
				// Output data augmented with output predictions
				System.out.println("Augmenting training data with output predictions...");
				options.testfile = options.trainfile;
				dp.augment(instanceLengths, options.trainfile, options.trainforest, options.augmentNumParts);
				// Now train the base classifier in the whole corpus, nothing being ignored
				System.out.println("Training the base classifier in the whole corpus...");
			}
			// afm 03-06-08 --- To allow some instances to be ignored
			int ignore[] = new int[instanceLengths.length];
			for (int i = 0; i < instanceLengths.length; i++)
				ignore[i] = 0;
			dp.params = new Parameters(pipe.dataAlphabet.size());
			dp.train(instanceLengths, ignore, options.trainfile, options.trainforest);
			System.out.print("Saving model...");
			dp.saveModel(options.modelName);
			System.out.print("done.");
		}
		if (options.test) {
			DependencyPipe pipe = options.secondOrder ? 
					new DependencyPipe2O (options) : new DependencyPipe (options);
			DependencyParser dp = new DependencyParser(pipe, options);
			System.out.print("\tLoading model...");
			dp.loadModel(options.modelName);
			System.out.println("done.");
			pipe.printModelStats(dp.params);
			pipe.closeAlphabets();
			dp.parseFile(null);
		}

		System.out.println();

		if (options.eval) {
			System.out.println("\nEVALUATION PERFORMANCE:");
			DependencyEvaluator.evaluate(options.goldfile, 
					options.outfile, 
					options.format);
		}
	}
}
