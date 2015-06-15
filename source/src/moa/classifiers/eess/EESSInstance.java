package moa.classifiers.eess;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EESSInstance {
	
	public Set<String> getEessFeatures();
	public void computeMetrics(int n, int index, double rand, EESSInstance targetInstance);
	public void normalizeMetrics(Map<String, Double> max, Map<String, Double> min);
	public void normalizeMetrics(Map<String, Double> mean);
	
	public double getComposedMetrics(List<String> metricsList);	
	public double[] getMetrics(List<String> metricsList);
	public Map<String, Double> getMetrics();
	public double[] getNormalizedMetrics(List<String> metricsList);	
}
