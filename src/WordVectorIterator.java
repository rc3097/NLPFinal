import java.util.Random;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;

public class WordVectorIterator implements DataSetIterator {
	/**
	 * implements org.deeplearning4j.datasets.iterator.DataSetIterator.
	 */
	private static final long serialVersionUID = 1L;
	List<TweetSet> tweetSets;
	Random r;
	int index;
	int miniBatchSize;
	
	public WordVectorIterator(HashMap<String,TweetSet> tweetSets) {
		r = new Random(18835775); //pulled from Random.org
		index = 0;
		miniBatchSize = 1; 
	}

	@Override
	public boolean hasNext() {
		return index + miniBatchSize <= tweetSets.size();
	}

	@Override
	public DataSet next() {
		return next(miniBatchSize);
	}

	@Override
	public int batch() {
		return this.miniBatchSize;
	}

	@Override
	public int cursor() {
		return this.index;
	}

	@Override
	public int inputColumns() {
		return 25;
	}

	@Override
	public DataSet next(int num) {

		int minSize = Integer.MAX_VALUE;
		for (int i = 0; i < num; i++) {
			tweetSets.get(i+index).shuffle();
			if (tweetSets.get(index + i).size() < minSize)
				minSize = tweetSets.get(index + i).size();
		}
		double[] labels = new double[num * minSize];
		double[] features = new double[num * minSize * 25];
		for (int i = 0; i < num; i++) {
			double numPos = 0.0;
			TweetSet set = this.tweetSets.get(index++);
			for (int j = 0; j < minSize; j++) {
				numPos += set.getTweetSet().get(j).isPositive() ? 1.0 : 0.0;
				labels[i * minSize + j] = numPos / (j+1);
				for (int k = 0 ; k < 25 ; k++) {
					features[i * minSize * 25 + 25 * j + k] = set.getTweetSet().get(j).getVector()[k];
				}
			}
		}
		return new DataSet(Nd4j.create(features, new int[] { num, minSize, 25 }),
				Nd4j.create(labels, new int[] { num, minSize }));
	}

	@Override
	public int numExamples() {
		return tweetSets.size();
	}

	@Override
	public void reset() {
		Collections.shuffle(this.tweetSets);
		index = 0;
		
	}

	@Override
	public void setPreProcessor(DataSetPreProcessor arg0) {
		throw new UnsupportedOperationException("Not implemented");		
	}

	@Override
	public int totalExamples() {
		return tweetSets.size();
	}


	@Override
	public void remove() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public int totalOutcomes() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}



