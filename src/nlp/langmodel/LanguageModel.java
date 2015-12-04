package nlp.langmodel;

import java.util.HashMap;
import java.util.List;

import nlp.util.LabelTweet;
import nlp.util.Pair;

/**
 * Language models assign probabilities to sentences and generate sentences.
 */
public abstract class LanguageModel {
	public void train(HashMap<String, Pair<String, List<String>>> traindata) {
		
	}
	public double predictProbability(HashMap<String, Pair<String, List<String>>> traindata) {
		return 0;
	}
	
}
