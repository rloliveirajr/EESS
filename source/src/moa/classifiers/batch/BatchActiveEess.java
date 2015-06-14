package moa.classifiers.batch;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import br.ufmg.dcc.lac.LacInstance;
import br.ufmg.dcc.lac.Training;

import weka.core.Instance;
import moa.classifiers.eess.EessInstance;
import moa.classifiers.eess.LacWeka;
import moa.classifiers.rules.Eess;
import moa.core.Measurement;
import moa.options.FloatOption;
import moa.options.IntOption;

public class BatchActiveEess extends Eess {

	private static final long serialVersionUID = 4740958383832856257L;
		
	public IntOption batchSizeOption = new IntOption("batchSize", 'B', "Size of processing batch", 32, 1, Integer.MAX_VALUE);
	
	public FloatOption minSimilarityOption = new FloatOption("minSimilarity", 'y', "", 1, 0, 1);
	
	protected final List<LacInstance> instancesInBatch;
	
	private int nPreviousRules;
	
	public BatchActiveEess(){
		super();
		this.instancesInBatch = new Vector<LacInstance>();
	}
	
	@Override
	public void resetLearningImpl() {
		super.resetLearningImpl();
		this.instancesInBatch.clear();
		this.nPreviousRules = 0;
		this.labeledAdded = 0;
	}
	
	@Override
	public void trainOnInstanceImpl(Instance arg0) {
		final EessInstance inst = LacWeka.populateInstance(timestamp, arg0, this.considerFeaturesPositionOption.isSet());
		this.timestamp++;
		if(this.manage == true){
			
			inst.setNRules(this.nPreviousRules);
			this.instancesInBatch.add(inst);

			if(this.instancesInBatch.size() == this.batchSizeOption.getValue()){
				this.nsawInstances++;
				this.eess(inst);
				this.insertInstances();
				
				this.previousRules.clear();
				this.instancesInBatch.clear();
			}
		}else{
			this.eessInstances.add(inst);
		}
	}
	
	private void insertInstances(){
		final List<LacInstance> batch = this.instancesInBatch;

		Collections.sort(batch, new Comparator<LacInstance>() {

			@Override
			public int compare(LacInstance o1, LacInstance o2) {
				String o1Features = Arrays.toString(o1.getFeatures());
				String o2Features = Arrays.toString(o2.getFeatures());
				
				return o1Features.compareTo(o2Features);
			}
		});
		
		final Training t = new Training();
		for(int i = 0; i < batch.size(); i++){
			t.addTransaction(batch.get(i));
		}
		
		this.insertLessSimilar(batch);
	}
	
	private void insertLessSimilar(List<LacInstance> batch){
		LacInstance instance = null;
		Set<Integer> features = null;
		double sim;

		while(!batch.isEmpty()){
			instance = batch.remove(batch.size()-1);
			features = instance.getFeaturesIndexed();
			
			double lessSim = 1.0;
			LacInstance instanceLessSim = instance;
			
			for(int i = batch.size()-1; i > -1; i--){
				final LacInstance batchInstance = batch.get(i);
				final Set<Integer> batchFeatures = batchInstance.getFeaturesIndexed();
	
				final Set<Integer> set = new HashSet<Integer>(features.size() + batchFeatures.size());
				set.addAll(features);
				set.addAll(batchFeatures);
				
				final double union = set.size();
				
				final double overlap = features.size() +  batchFeatures.size() - union;

	            sim = overlap/union;
	            
//	            if(Double.compare(sim, this.minSimilarityOption.getValue()) >= 0){
	            	LacInstance toBeInserted = batch.remove(i);
	            	if(Double.compare(sim, lessSim) <= 0){
	            		lessSim = sim;
	            		instanceLessSim = toBeInserted;
	            		
	            	}
//	            }
			}
			
			final LacInstance lessSimilar = this.lessSimilar(instance, instanceLessSim);
			
			this.eessInstances.add(lessSimilar);
			this.labeledAdded++;
		}
	}
	
	private LacInstance lessSimilar(LacInstance lessSimilar2Train, LacInstance lessSimilarOfMostSimilar){
		
		final int nFeatures = lessSimilar2Train.getFeatures().length;
		final double lowerSimilarity = 1 - (lessSimilar2Train.getNRules()/(double) this.previousRules.size());
		
		final int lsomsNFeatures = lessSimilarOfMostSimilar.getFeatures().length;
		final double lsomsTrainSimilarity = 1 - (lessSimilarOfMostSimilar.getNRules()/(double) this.previousRules.size());
		
		final int comparisson = Double.compare(lsomsTrainSimilarity, lowerSimilarity);
		
		if(comparisson <= 0){
			if((comparisson == 0 && lsomsNFeatures > nFeatures) || comparisson < 0){
				lessSimilar2Train = lessSimilarOfMostSimilar;
			}
		}
		
		return lessSimilar2Train;
	}
	
	@Override
	protected Measurement[] getModelMeasurementsImpl() {

		Measurement[] m = new Measurement[] {
				new Measurement("model training instances", this.nsawInstances),
				new Measurement("labeling cost", this.labeledAdded),
				new Measurement("window", this.trainingSize)};

		return m;
	}
	
	@Override
	public double[] getVotesForInstance(Instance arg0) {
		final int iRulesSize = this.previousRules.size();
		
		double[] votes = super.getVotesForInstance(arg0);
		
		final int eRulesSize = this.previousRules.size();
		
		this.nPreviousRules = eRulesSize - iRulesSize;
		
		return votes;
	}

	@Override
	public boolean isRandomizable() {
		return true;
	}
}