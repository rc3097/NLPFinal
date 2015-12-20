import java.util.Random;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;

import nlp.util.Pair;

public class WordVectorIterator implements DataSetIterator {
	/**
	 * implements org.deeplearning4j.datasets.iterator.DataSetIterator.
	 */
	private static final long serialVersionUID = 1L;
	List<DataSet> tweetSets;
	Random r;
	int index;
	
	public WordVectorIterator(HashMap<String,TweetSet> tweetSets) {
		r = new Random(18835775); //pulled from Random.org
		index = 0;
		for (TweetSet set : tweetSets.values()) {
			Double[][] features = new Double[set.size()][25];
			
		}
	}

	@Override
	public boolean hasNext() {
		return index < tweetSets.size();
	}

	@Override
	public DataSet next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int batch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int cursor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int inputColumns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DataSet next(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numExamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int totalExamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int totalOutcomes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
	
}



