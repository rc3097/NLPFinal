import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class test {
	
	public static void main(String[] args) {
		INDArray labels = Nd4j.create(new double[] {0.1}, new int[] {1});
		INDArray z = Nd4j.create(new double[] {0.1}, new int[] {1});
		INDArray xEntLogZ = Transforms.log(z);
        INDArray xEntOneMinusLabelsOut = labels.rsub(1);
        INDArray xEntOneMinusLogOneMinusZ = Transforms.log(z).rsubi(1);
        Double ret = labels.mul(xEntLogZ).add(xEntOneMinusLabelsOut).muli(xEntOneMinusLogOneMinusZ).sum(1).sumNumber().doubleValue();
        
        Double xent = - 0.1*Math.log(0.1) - 0.9*Math.log(0.9);
        
        //assert ret == xent;
        System.out.println("xent: " + xent + " , " + " ret: " + ret);
	}

}
