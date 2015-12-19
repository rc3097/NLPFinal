package nlp.langmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import nlp.util.Counter;
import nlp.util.Pair;

public class MaxentTesterModel2 extends MaxentTesterModel1{
	public double predictProbability(
			HashMap<Pair<String, String>, List<List<String>>> testdata) {
		int pre_positivecount = 0;
		int real_positviecount = 0;
		int total =0;
		ArrayList<String> tempsentence= new ArrayList<String>();
		for (Entry<Pair<String, String>, List<List<String>>> entry : testdata
				.entrySet()) {
			
			Pair<String, String> keyPair = entry.getKey();
			String label = keyPair.getSecond();
			for (List<String> sentence : entry.getValue()) {
				total++;
				if (label.equals("positive"))
					real_positviecount++;
				tempsentence.addAll(sentence);
			}
		}
		String[] templist = new String[tempsentence.size()];
		tempsentence.toArray(templist);
		Counter<String> result = classifier.getProbabilities(templist);
		double p = real_positviecount*1.0/total;
		double q =  result.getCount("positive");
		return lossfunction(p, q);
	}
}
