import java.util.HashMap;
import java.util.List;

import nlp.langmodel.SentimentQuantifier;
import nlp.util.Pair;

public class StanfordAggregateQuantifier  extends SentimentQuantifier{

	@Override
	public void train(HashMap<Pair<String, String>, List<List<String>>> traindata) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double predictProbability(HashMap<Pair<String, String>, List<List<String>>> testdata) {
		// TODO Auto-generated method stub
		return 0;
	}

}
