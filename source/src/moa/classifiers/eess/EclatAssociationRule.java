package moa.classifiers.eess;

import br.ufmg.dcc.lac.AssociationRule;
import br.ufmg.dcc.lac.Eclat;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.options.FloatOption;
import moa.options.IntOption;
import moa.tasks.TaskMonitor;

public class EclatAssociationRule extends AbstractOptionHandler implements AssociationRuleInterface{

	private static final long serialVersionUID = -2887956593544754512L;
	
	public IntOption maxRuleSizeOption = new IntOption("maxRuleSize", 'R', "",
			3, 0, Integer.MAX_VALUE);
	
	public FloatOption minConfidenceOption = new FloatOption("minConfidence",
			'c', "", 0.1, 0, 1);
	
	public FloatOption minSupportOption = new FloatOption("minSupport", 's',
			"", 0.0, 0, 1);

	
	public AssociationRule getAssociationRule(){
		Eclat eclat = new Eclat(this.minConfidenceOption.getValue(), this.minSupportOption.getValue(), 
				this.maxRuleSizeOption.getValue());
		
		return eclat;
	}
	
	@Override
	public void getDescription(StringBuilder arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void prepareForUseImpl(TaskMonitor arg0, ObjectRepository arg1) {
		// TODO Auto-generated method stub
		
	}

}
