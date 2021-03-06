package nlp.langmodel;

import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

import nlp.classify.*;
import nlp.util.CommandLineUtils;
import nlp.util.Counter;

/**
 * This is the main harness for assignment 2. To run this harness, use
 * <p/>
 * java nlp.assignments.ProperNameTester -path ASSIGNMENT_DATA_PATH -model
 * MODEL_DESCRIPTOR_STRING
 * <p/>
 * First verify that the data can be read on your system using the baseline
 * model. Second, find the point in the main method (near the bottom) where a
 * MostFrequentLabelClassifier is constructed. You will be writing new
 * implementations of the ProbabilisticClassifer interface and constructing them
 * there.
 */
public class ProperNameTester {

	public static class ProperNameFeatureExtractor implements
			FeatureExtractor<String, String> {

		public Counter<String> extractFeatures(String name) {
			char[] characters = name.toCharArray();
			Counter<String> features = new Counter<String>();
			// add character unigram features
			// features.incrementCount("UNI-" + characters[characters.length -
			// 1],
			// 2.0);

			for (int i = 0; i < characters.length; i++) {
				char character = characters[i];
				features.incrementCount("UNI-" + character, 1.0);
			}
			for (int i = 0; i < characters.length - 1; i++) {
				features.incrementCount("BI-" + characters[i]
						+ characters[i + 1], 1.0);
			}
			//
			for (int i = 0; i < characters.length - 2; i++) {
				features.incrementCount("TRI-" + characters[i]
						+ characters[i + 1] + characters[i + 2], 2.0);
			}

			String[] names = name.split(" ");
			for (int i = 0; i < names.length; i++) {
				features.incrementCount("WHO-" + names[i], 4.0);
			}

			return features;
		}
	}

	public static class ListStringFeatureExtractor implements
			FeatureExtractor<String[], String> {

		public Counter<String> extractFeatures(String[] words) {
			Counter<String> features = new Counter<String>();
			
			for (String word : words) {
				char[] characters = word.toCharArray();
			
				// add character unigram features
				
				word =word.toLowerCase();
				 if (word.contains("http:")) {
				 continue;
				 }
				word = word.replace(".", "");
				word =  word.replace(",", "");
				if (word.equals("no")) {
					features.incrementCount("NEG-" + word, 1.0);
				}
				if (word.equals("not")) {
					features.incrementCount("NEG-" + word, 1.0);
				}
				if (word.contains("down")) {
					features.incrementCount("NEG-" + word, 1.0);
				}
				for (int i = 0; i < characters.length; i++) {
					char character = characters[i];
					features.incrementCount("UNI-" + character, 1.0);
				}
				for (int i = 0; i < characters.length - 1; i++) {
					features.incrementCount("BI-" + characters[i]
							+ characters[i + 1], 1.0);
				}
				//
				for (int i = 0; i < characters.length - 2; i++) {
					features.incrementCount("TRI-" + characters[i]
							+ characters[i + 1] + characters[i + 2], 2.0);
				}

			}

			return features;
		}
	}

	private static List<LabeledInstance<String, String>> loadData(
			String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		List<LabeledInstance<String, String>> labeledInstances = new ArrayList<LabeledInstance<String, String>>();
		while (reader.ready()) {
			String line = reader.readLine();
			String[] parts = line.split("\t");
			String label = parts[0];
			String name = parts[1];

			LabeledInstance<String, String> labeledInstance = new LabeledInstance<String, String>(
					label, name);
			labeledInstances.add(labeledInstance);
		}
		reader.close();
		return labeledInstances;
	}

	private static void testClassifier(
			ProbabilisticClassifier<String, String> classifier,
			List<LabeledInstance<String, String>> testData, boolean verbose) {
		double numCorrect = 0.0;
		double numTotal = 0.0;
		for (LabeledInstance<String, String> testDatum : testData) {
			String name = testDatum.getInput();
			String label = classifier.getLabel(name);
			double confidence = classifier.getProbabilities(name).getCount(
					label);
			if (label.equals(testDatum.getLabel())) {
				numCorrect += 1.0;
			} else {
				if (verbose) {
					// display an error
					System.err.println("Error: " + name + " guess=" + label
							+ " gold=" + testDatum.getLabel() + " confidence="
							+ confidence);
				}
			}
			numTotal += 1.0;
		}
		double accuracy = numCorrect / numTotal;
		System.out.println("Accuracy: " + accuracy);
	}

	public static void main(String[] args) throws IOException {
		// Parse command line flags and arguments
		Map<String, String> argMap = CommandLineUtils
				.simpleCommandLineParser(args);

		// Set up default parameters and settings
		String basePath = ".";
		String model = "baseline";
		boolean verbose = false;
		boolean useValidation = true;

		// Update defaults using command line specifications

		// The path to the assignment data
		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
		}
		System.out.println("Using base path: " + basePath);

		// A string descriptor of the model to use
		if (argMap.containsKey("-model")) {
			model = argMap.get("-model");
		}
		System.out.println("Using model: " + model);

		// A string descriptor of the model to use
		if (argMap.containsKey("-test")) {
			String testString = argMap.get("-test");
			if (testString.equalsIgnoreCase("test"))
				useValidation = false;
		}
		System.out.println("Testing on: "
				+ (useValidation ? "validation" : "test"));

		// Whether or not to print the individual speech errors.
		if (argMap.containsKey("-verbose")) {
			verbose = true;
		}

		// Load training, validation, and test data
		List<LabeledInstance<String, String>> trainingData = loadData(basePath
				+ "/pnp-train.txt");
		List<LabeledInstance<String, String>> validationData = loadData(basePath
				+ "/pnp-validate.txt");
		List<LabeledInstance<String, String>> testData = loadData(basePath
				+ "/pnp-test.txt");

		// Learn a classifier
		ProbabilisticClassifier<String, String> classifier = null;
		if (model.equalsIgnoreCase("baseline")) {
			classifier = null;
		} else if (model.equalsIgnoreCase("n-gram")) {
			// TODO: construct your n-gram model here
		} else if (model.equalsIgnoreCase("maxent")) {
			// TODO: construct your maxent model here
			ProbabilisticClassifierFactory<String, String> factory = new MaximumEntropyClassifier.Factory<String, String, String>(
					1.0, 40, new ProperNameFeatureExtractor());
			classifier = factory.trainClassifier(trainingData);
		} else if (model.equalsIgnoreCase("perceptron")) {
			// ProbabilisticClassifierFactory<String, String> factory = new
			// PerceptronClassifier.Factory<String, String, String>(
			// 1.0, trainingData.size(), new ProperNameFeatureExtractor());
			// classifier = factory.trainClassifier(trainingData);
		} else {
			throw new RuntimeException("Unknown model descriptor: " + model);
		}

		// Test classifier
		testClassifier(classifier, (useValidation ? validationData : testData),
				verbose);
	}
}
