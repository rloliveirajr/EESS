package moa.classifiers.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import br.ufmg.dcc.lac.AssociationRule;
import br.ufmg.dcc.lac.KaldorHicksEfficiency;
import moa.classifiers.eess.EessInstance;
import br.ufmg.dcc.lac.LacInstance;
import br.ufmg.dcc.lac.LacRule;
import br.ufmg.dcc.lac.LacSupervised;
import br.ufmg.dcc.lac.Skyline2Instance;
import br.ufmg.dcc.lac.Training;

import weka.core.Instance;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.eess.AssociationRuleInterface;
import moa.classifiers.eess.LacWeka;
import moa.core.Measurement;
import moa.options.ClassOption;
import moa.options.FlagOption;
import moa.options.FloatOption;

public class Eess extends AbstractClassifier {

	private static final long serialVersionUID = 6789593603127136931L;

	public ClassOption associationRuleOption = new ClassOption(
			"associationRule", 'a', "", AssociationRuleInterface.class,
			"moa.classifiers.eess.EclatAssociationRule");
	
	final public FloatOption budgetOption = new FloatOption("budget", 'b', "", 0, 0, 1);
	
	public FlagOption kaldorHicksOption = new FlagOption("kaldorHicks", 'k',
			"Activate sample selection by Kaldor-Hicks Efficiency.");
	
	public FlagOption considerFeaturesPositionOption = new FlagOption(
			"considerFeaturesPosition", 'p',
			"Consider the position of features.");
	
	public FlagOption similarityOption = new FlagOption(
			"similarity", 'S',
			"Consider the position of features.");
	
	public FlagOption freshnessOption = new FlagOption(
			"freshness", 'F',
			"Consider the position of features.");
	
	public FlagOption randomnessOption = new FlagOption(
			"randomness", 'R',
			"Consider the position of features.");
	
	public FlagOption meanSimilarityOption = new FlagOption(
			"meanSimilarity", 'M',
			"Consider the position of features.");

	protected final List<LacInstance> eessInstances;
	protected final Set<EessInstance> previousRules;
	protected final Training training;
	protected AssociationRule associationRule;
	protected final Random rand;
	protected final LacSupervised lac;
	private final List<String> metricsList;

	protected int labeledAdded;
	protected int timestamp;

	protected boolean manage;

	protected int trainingSize;

	protected int skylineAvgSize;
	protected int kaldorHicksAvgSize;
	protected int trainingAvgSize;

	protected int nsawInstances;

	protected boolean kaldorHicks;
	
	public Eess() {
		this.eessInstances = new Vector<LacInstance>();
		this.previousRules = new HashSet<EessInstance>();

		this.training = new Training();
		this.rand = new Random(this.randomSeed);
		this.lac = new LacSupervised();
		this.metricsList = new Vector<String>();
	}

	@Override
	public void resetLearningImpl() {

		this.previousRules.clear();
		this.eessInstances.clear();

		this.labeledAdded = 0;
		this.rand.setSeed(this.randomSeed);

		this.timestamp = 0;

		this.manage = false;

		this.trainingSize = 0;

		this.skylineAvgSize = 0;
		this.kaldorHicksAvgSize = 0;
		this.trainingAvgSize = 0;

		this.nsawInstances = 0;

		AssociationRuleInterface associationRuleInterface = 
				(AssociationRuleInterface) getPreparedClassOption(this.associationRuleOption);

		this.associationRule = associationRuleInterface.getAssociationRule();
		
		this.kaldorHicks = this.kaldorHicksOption.isSet();
		this.metricsList.clear();

	}

	@Override
	public void trainOnInstanceImpl(Instance arg0) {
		EessInstance targetInstance = LacWeka.populateInstance(timestamp, arg0,
				this.considerFeaturesPositionOption.isSet());
		this.timestamp++;

		if (this.manage == true) {
			this.nsawInstances++;
			
			this.eess(targetInstance);
			
			boolean insert = Double.compare(rand.nextDouble(), this.budgetOption.getValue()) < 0;
			
			if(insert){
				
				// Inserting instance
				this.labeledAdded++;
				this.eessInstances.add(targetInstance);
			}
			
			this.previousRules.clear();
		} else {
			this.eessInstances.add(targetInstance);
		}
	}

	protected void eess(EessInstance targetInstance) {

		if(this.metricsList.size() == 0){
			if(this.similarityOption.isSet()){
				metricsList.add("similarity");
			}
			
			if(this.freshnessOption.isSet()){
				metricsList.add("freshness");
			}
			
			if(this.randomnessOption.isSet()){
				metricsList.add("randomness");
			}
			
			if(this.meanSimilarityOption.isSet()){
				metricsList.add("meanSimilarity");
			}
		}
		
		int index = 0;
		for(LacInstance lac_i : this.eessInstances){
			EessInstance i = (EessInstance) lac_i;
			i.computeMetrics(this.timestamp, index++, rand.nextGaussian(), targetInstance);
		}	
		List<EessInstance> skyline = new Vector<EessInstance>();
		List<EessInstance> dominated = new Vector<EessInstance>();

		this.computeSkyline(targetInstance, skyline, dominated);

		this.trainingSize = this.eessInstances.size();
		this.trainingAvgSize += this.eessInstances.size();

		this.eessInstances.clear();
		this.eessInstances.addAll(skyline);
	}

	protected void computeSkyline(EessInstance targetInstance, List<EessInstance> returnedSkyline,
			List<EessInstance> returnedDominated) {
		
		final Skyline2Instance skyline = new Skyline2Instance(metricsList);
		
		for (int i = 0; i < this.eessInstances.size(); i++) {
			final EessInstance instance = (EessInstance) this.eessInstances.get(i);
			instance.computeMetrics(this.timestamp, i, this.rand.nextGaussian(), targetInstance);

			skyline.addPoint(instance);
		}

		this.skylineAvgSize += skyline.window.size();

		if (this.kaldorHicks) {
			KaldorHicksEfficiency.frontier(metricsList, skyline.window,
					skyline.removed);
			this.kaldorHicksAvgSize += skyline.window.size();
		}

		returnedSkyline.addAll(skyline.window);
	}

	@Override
	public double[] getVotesForInstance(Instance arg0) {
		final double[] votes = new double[arg0.numClasses()];

		if (this.manage == true) {
			this.training.clear();
			this.training.addTransactions(this.eessInstances);

			final EessInstance instance = LacWeka.populateInstance(timestamp,
					arg0, considerFeaturesPositionOption.isSet());

			this.associationRule.setClassTable(this.training.getClassesTable());
			this.associationRule.setFeaturesTalbe(this.training
					.getFeaturesTable());
			this.associationRule.setTidClassMap(this.training.getTidClassMap());

			final double[] scores = new double[this.training
					.getNumberOfClasses()];

			final List<LacRule> rules = new Vector<LacRule>();

			lac.predict(this.training, instance, associationRule, scores, rules);

			LacWeka.calcProbability(this.training, arg0.classAttribute(),
					scores, votes);

			double normalizationFactor = 0.0;
			
			for(int classId = 0; classId < scores.length; classId++){
				normalizationFactor += scores[classId];
			}

			for(int classId = 0; Double.compare(normalizationFactor,0.0) == 1 && classId < scores.length; classId++){
				scores[classId] = scores[classId]/normalizationFactor;
			}
			
			double score = scores[0];
			int predicted = 0;
			for (int i = 1; i < scores.length; i++) {
				if (Double.compare(score, scores[i]) < 0) {
					score = scores[i];
					predicted = i;
				}
			}

			instance.label = this.training.getClassesTable().getName(predicted);
			instance.setIndexedClass(predicted);
			

			this.previousRules.add(instance);

		} else {
			final double equalVotes = 1.0 / arg0.numClasses();
			Arrays.fill(votes, equalVotes);

			this.manage = true;
			this.training.clear();
			this.training.addTransactions(this.eessInstances);
		}

		return votes;
	}

	@Override
	public boolean isRandomizable() {
		return true;
	}

	@Override
	public void getModelDescription(StringBuilder arg0, int arg1) {
	}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {

		Measurement[] m = new Measurement[] {
				new Measurement("model training instances", this.labeledAdded),
				new Measurement("window", this.trainingSize),
				new Measurement("AvgWindow", (double) this.trainingAvgSize
						/ this.nsawInstances),
				new Measurement("AvgSkyline", (double) this.skylineAvgSize
						/ this.nsawInstances),
				new Measurement("AvgKaldor-Hicks",
						(double) this.kaldorHicksAvgSize / this.nsawInstances) };

		this.nsawInstances = 0;
		this.trainingAvgSize = 0;
		this.skylineAvgSize = 0;
		this.kaldorHicksAvgSize = 0;

		return m;
	}
//
//	private double[] similarity() {
//		double[] coverage = new double[this.lacInstances.size()];
//
//		Arrays.fill(coverage, 0);
//
//		final Set<String> union = new HashSet<String>();
//		final Set<String> intersection = new HashSet<String>();
//		final Set<String> iFeatures = new HashSet<String>();
//		final Set<String> features = new HashSet<String>();
//
//		EessInstance instance = null;
//		for (int i = 0; i < this.lacInstances.size(); i++) {
//			instance = this.lacInstances.get(i);
//			features.clear();
//			for (String f : instance.getFeatures()) {
//				features.add(f);
//			}
//
//			for (EessInstance r : this.previousRules) {
//				iFeatures.clear();
//				for (String f : r.getFeatures()) {
//					union.add(f);
//					iFeatures.add(f);
//				}
//
//				union.clear();
//				union.addAll(features);
//
//				intersection.clear();
//				intersection.addAll(features);
//				intersection.retainAll(iFeatures);
//
//				// Jaccard Similarity
//				coverage[i] += (double) intersection.size() / union.size();
//			}
//
//			coverage[i] /= (double) this.previousRules.size();
//		}
//
//		return coverage;
//	}
}
