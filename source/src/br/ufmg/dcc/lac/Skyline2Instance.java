package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import moa.classifiers.eess.EessInstance;


public class Skyline2Instance implements Serializable{

	private static final long serialVersionUID = -1054447350904769743L;

	private enum RELATION {DOMINATED, DOMINATES, INCOMPARABLE};
	
	public List<EessInstance> window;
	public List<EessInstance> removed;
	private List<String> metricsList;
	
	protected int inserted;
	public Skyline2Instance(List<String> metricsList){
		this.window = new Vector<EessInstance>();
		this.removed = new Vector<EessInstance>();
		this.metricsList = metricsList;
	}
	
	public void addPoint(EessInstance point){
		for(int i = window.size() - 1; i > -1 ; --i){
			EessInstance rule = this.window.get(i);
			RELATION r = this.compare(point, rule);
			
			if(r == RELATION.DOMINATED){
				removed.add(point);
				return;
			}else if(r == RELATION.DOMINATES){
				EessInstance inst = this.window.remove(i);
				removed.add(inst);
			}
		}
		
		this.window.add(point);
	}
	
	private Skyline2Instance.RELATION compare(EessInstance point1, EessInstance point2){
		int i = 0;
		
		double[] metricsPoint1 = point1.getNormalizedMetrics(this.metricsList);
		double[] metricsPoint2 = point2.getNormalizedMetrics(this.metricsList);

		while(i < metricsPoint1.length && Double.compare(metricsPoint1[i], metricsPoint2[i]) == 0){
			++i;
		}

		if(i == metricsPoint1.length){
			return RELATION.INCOMPARABLE;
		}
		
		if(Double.compare(metricsPoint1[i], metricsPoint2[i]) < 0){
			for(++i; i < metricsPoint1.length; ++i){
				if(Double.compare(metricsPoint1[i], metricsPoint2[i]) > 0){
					return RELATION.INCOMPARABLE;
				}
			}
			return RELATION.DOMINATED;
		}
			
		for(++i; i < metricsPoint1.length; ++i){
			if(Double.compare(metricsPoint1[i], metricsPoint2[i]) < 0){
				return RELATION.INCOMPARABLE;
			}
		}
		return RELATION.DOMINATES;
	}
	
	public List<EessInstance> getWindow(){
		return this.window;
	}
}
