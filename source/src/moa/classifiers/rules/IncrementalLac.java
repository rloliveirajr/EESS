package moa.classifiers.rules;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import br.ufmg.dcc.lac.AssociationRule;
import br.ufmg.dcc.lac.LacInstance;
import br.ufmg.dcc.lac.LacRule;
import br.ufmg.dcc.lac.LacSupervised;
import br.ufmg.dcc.lac.SymbolTable;
import br.ufmg.dcc.lac.Training;

import weka.core.Instance;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.eess.AssociationRuleInterface;
import moa.classifiers.eess.LacWeka;
import moa.core.Measurement;
import moa.options.ClassOption;
import moa.options.FlagOption;
import moa.options.IntOption;

public class IncrementalLac extends AbstractClassifier {

	private static final long serialVersionUID = 4740958383832856257L;

	public final IntOption windowSizeOption = new IntOption("windowSize", 'w', "",
			Integer.MAX_VALUE, 2, Integer.MAX_VALUE);
	
	public final ClassOption associationRuleOption = 
			new ClassOption("associationRule", 'a', "", AssociationRuleInterface.class, 
					"moa.classifiers.eess.EclatAssociationRule");
	
	public final FlagOption considerFeaturesPositionOption = new FlagOption("considerFeaturesPosition", 'p', "Consider the position of features.");

	private Training training;
	private final List<LacInstance> lacInstances;
	private final List<LacRule> previousRules;
	private final LacSupervised lac;
	
	private AssociationRule associationRule;
	private int examplesInserted;
	private boolean manage;
	
	public IncrementalLac(){
		this.lacInstances = new Vector<LacInstance>();
		this.previousRules = new Vector<LacRule>();
		this.lac = new LacSupervised();
	}
	
	@Override
	public void resetLearningImpl() {	
		this.training = new Training();
		
		this.examplesInserted = 0;
		
		this.training.clear();
		this.lacInstances.clear();
		this.previousRules.clear();
		this.manage = false;
		AssociationRuleInterface associationRuleInterface = (AssociationRuleInterface) 
				getPreparedClassOption(this.associationRuleOption);
		
		this.associationRule = associationRuleInterface.getAssociationRule();
	}

	@Override
	public void trainOnInstanceImpl(Instance wekaInstance) {
		LacInstance instance = LacWeka.populateInstance(this.lacInstances.size(), wekaInstance, 
				considerFeaturesPositionOption.isSet());
	
		if(this.manage){
			while(this.lacInstances.size() >= this.windowSizeOption.getValue()){
				this.lacInstances.remove(0);
			}
			
			this.training = null;
			this.training = new Training();
			
			for(LacInstance i : this.lacInstances){
				this.training.addTransaction(i);
			}
			
			this.examplesInserted++;
			this.training.addTransaction(instance);
			this.lacInstances.add(instance);
		}else{
			this.examplesInserted++;
			this.training.addTransaction(instance);
			this.lacInstances.add(instance);
		}
	}

	@Override
	public double[] getVotesForInstance(Instance wekaInstance) {
		this.manage = true;
		final LacInstance instance = LacWeka.populateInstance(this.lacInstances.size(), wekaInstance, 
				considerFeaturesPositionOption.isSet());
		
		this.associationRule.setClassTable(this.training.getClassesTable());
		this.associationRule.setFeaturesTalbe(this.training.getFeaturesTable());
		this.associationRule.setTidClassMap(this.training.getTidClassMap());

		final double[] scores = new double[this.training.getNumberOfClasses()];
		
		this.previousRules.clear();
		lac.predict(this.training, instance, this.associationRule, scores, this.previousRules);
		
//		for(LacRule r : this.previousRules){
//			printRule(training.getFeaturesTable(),training.getClassesTable(),r);
//		}
		
		final double[] probs = new double[wekaInstance.numClasses()];
		Arrays.fill(probs, 0.0);
		
		LacWeka.calcProbability(this.training, wekaInstance.classAttribute(), scores, probs);

		return probs;
	}
	
	@Override
	public boolean isRandomizable() {
		return false;
	}

	@Override
	public void getModelDescription(StringBuilder arg0, int arg1) {}

	@Override
	protected Measurement[] getModelMeasurementsImpl() {		
		return new Measurement[]{
				new Measurement("model training instances", this.examplesInserted),
				new Measurement("window", this.lacInstances.size())};
	}
	
	public void printRule(SymbolTable symbolTable, SymbolTable classesTable, LacRule r){
		System.out.print("[");
		for(int f = 0; f < r.getFeaturesIds().size(); ++f){
			int item = r.getFeaturesIds().get(f);
			if(f+1 < r.getFeaturesIds().size()){
				System.out.print(symbolTable.getName(item) + ", ");
			}else{
				System.out.println(symbolTable.getName(item) + "] -> " + 
			classesTable.getName(r.getIndexedClass()) + " (" + r.getMetrics()[0] + ")");
			}
		}
	}
}