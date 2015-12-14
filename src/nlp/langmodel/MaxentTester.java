package nlp.langmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import nlp.classify.FeatureExtractor;
import nlp.classify.LabeledInstance;
import nlp.classify.ProbabilisticClassifier;
import nlp.classify.ProbabilisticClassifierFactory;
import nlp.langmodel.ProperNameTester.ProperNameFeatureExtractor;
import nlp.util.Counter;
import nlp.util.Pair;

public class MaxentTester extends LanguageModel {
	ProbabilisticClassifier<String[], String> classifier = null;
	public void train(HashMap<Pair<String, String>, List< List<String>>> sentencePair) {
		List<LabeledInstance<String[], String>> trainingData = new ArrayList<LabeledInstance<String[], String>>();

		for (Entry<Pair<String, String>, List<List<String>>> entry : sentencePair
				.entrySet()) {

			Pair<String, String> keyPair = entry.getKey();
			List<List<String>> valuelist = entry.getValue();

			String label = keyPair.getSecond();

			for (List<String> sentence : valuelist) {
				// optimization TODO:
				String[] templist = new String[sentence.size()];
				sentence.toArray(templist);
				LabeledInstance<String[], String> eachsentence = new LabeledInstance<String[], String>(
						label, templist);
				trainingData.add(eachsentence);
			}
		}

		MaximumEntropyClassifier.Factory<String[], String, String> factory = new MaximumEntropyClassifier.Factory<String[], String, String>(
				1.0, 20, new ProperNameTester.ListStringFeatureExtractor());
		classifier=factory.trainClassifier(trainingData);
	}
	public double predictProbability(HashMap<Pair<String, String>, List< List<String>>> testdata) {
		for (Entry<Pair<String, String>, List<List<String>>> entry : testdata
				.entrySet()) {
			Pair<String,String> keyPair = entry.getKey();
			String label = keyPair.getSecond();
			
			for (List<String> sentence : entry.getValue()) {
				String[] templist = new String[sentence.size()];
				sentence.toArray(templist);
				Counter<String> result=classifier.getProbabilities(templist);
				System.out.println( result);
			}
		}
		return 0;
	}
}
