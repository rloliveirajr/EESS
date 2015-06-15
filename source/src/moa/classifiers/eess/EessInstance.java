package moa.classifiers.eess;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import br.ufmg.dcc.lac.LacInstance;

public class EessInstance extends LacInstance implements EESSInstance{
	
	public static String[] metricsList = {"similarity", "freshness", "randomness", "meanSimilarity"};
	private static final long serialVersionUID = 1L;
	
	public final Map<String, Double> metrics;
	public final Map<String, Double> normalizedMetrics;
	public final Set<String> eessFeatures;
	public double rank;
	public double score;
	public double n_bla;
	
	public EessInstance(int tid, String[] features, String label){
		super(tid, features, label);
		this.metrics = new TreeMap<String, Double>();
		this.normalizedMetrics = new TreeMap<String, Double>();
		this.eessFeatures = new HashSet<String>();
		
		for (String f : this.getFeatures()) {
			this.eessFeatures.add(f);
		}
	}
	
	@Override
	public void computeMetrics(int n, int index, double rand, EESSInstance targetInstance){
		double similarity = this.similarity(targetInstance);
		this.metrics.put("similarity", similarity);	
		
		double freshness = (double) this.tid / n;
		this.metrics.put("freshness", freshness);
		
		double randomness = (double)((index+1) * rand + 1);
		this.metrics.put("randomness", randomness);
				
		this.normalizedMetrics.putAll(this.metrics);
		this.n_bla += 1.0;
	}
	
	public void normalizeMetrics(Map<String, Double> min, Map<String, Double> max){
		for(Entry<String, Double> entry : this.metrics.entrySet()){
			double normalizedValue = entry.getValue() - min.get(entry.getKey());
			normalizedValue /= max.get(entry.getKey()) - min.get(entry.getKey());
			this.normalizedMetrics.put(entry.getKey(), normalizedValue);
		}
	}
	
	public void normalizeMetrics(Map<String, Double> mean){		
		for(Entry<String, Double> entry : this.metrics.entrySet()){
			double normalizedValue = entry.getValue() - mean.get(entry.getKey());
			this.normalizedMetrics.put(entry.getKey(), normalizedValue);
		}
	}
	
	public double[] getMetrics(List<String> metricsList){
		double[] metrics = new double[metricsList.size()];
		for(int m = 0; m < metricsList.size(); m++){
			metrics[m] = this.metrics.get(metricsList.get(m));
		}
		
		return metrics;
	}
	
	public Map<String, Double> getMetrics(){
		return this.metrics;
	}
	
	public double[] getNormalizedMetrics(List<String> metricsList){
		double[] metrics = new double[metricsList.size()];
		
		for(int m = 0; m < metricsList.size(); m++){
			metrics[m] = this.normalizedMetrics.get(metricsList.get(m));
		}
		return metrics;
	}
	
	public double getComposedMetrics(List<String> metricsList){
		double[] metrics = this.getNormalizedMetrics(metricsList);
		double m = 0;
		for(double d : metrics){
			m += d;
		}
		return m;
	}
	
	public Set<String> getEessFeatures(){
		return this.eessFeatures;
	}
		
	public double similarity(EESSInstance targetInstance){
		final Set<String> union = new HashSet<String>();
		final Set<String> intersection = new HashSet<String>();

		union.addAll(this.getEessFeatures());
		union.addAll(targetInstance.getEessFeatures());
		
		intersection.addAll(this.getEessFeatures());
		intersection.retainAll(targetInstance.getEessFeatures());

		return intersection.size() / union.size();
	}

	public static void normalize(List<LacInstance> instances){
		Map<String, Double> factor = new TreeMap<String, Double>();
		Map<String, Double> i_metrics;
		int n_instances = 0;
		for(LacInstance lac_i : instances){
			EessInstance i = (EessInstance) lac_i;
			i_metrics = i.getMetrics();
			System.out.println(i_metrics);
			for(String m : metricsList){
				double meanValue = factor.getOrDefault(m, 0.0);
				meanValue = n_instances * meanValue + i_metrics.get(m);
				meanValue /= n_instances + 1;
				factor.put(m, meanValue);
			}
			n_instances++;
		}
		
		for(LacInstance lac_i : instances){
			EessInstance i = (EessInstance) lac_i;
			i.normalizeMetrics(factor);
		}
	}
}
