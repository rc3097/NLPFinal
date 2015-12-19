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

public class MaxentTesterModel1 extends SentimentQuantifier {
	ProbabilisticClassifier<String[], String> classifier = null;

	public void train(
			HashMap<Pair<String, String>, List<List<String>>> sentencePair) {
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
		classifier = factory.trainClassifier(trainingData);
	}

	public double predictProbability(
			HashMap<Pair<String, String>, List<List<String>>> testdata) {
		int pre_positivecount = 0;
		int real_positviecount = 0;
		int total =0;
		for (Entry<Pair<String, String>, List<List<String>>> entry : testdata
				.entrySet()) {
			
			Pair<String, String> keyPair = entry.getKey();
			String label = keyPair.getSecond();
			
			for (List<String> sentence : entry.getValue()) {
				total++;
				if (label.equals("positive"))
					real_positviecount++;
				String[] templist = new String[sentence.size()];
				sentence.toArray(templist);
				Counter<String> result = classifier.getProbabilities(templist);
				System.out.println(result);
				if (result.getCount("positive") > result.getCount("negative")) {
					pre_positivecount++;
				}
			}
		}
		double p = real_positviecount*1.0/total;
		double q =  1.0*pre_positivecount/total;
		return lossfunction(p, q);
	}
}
