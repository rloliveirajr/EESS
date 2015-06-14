package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.List;

import moa.classifiers.eess.EessInstance;

public class KaldorHicksEfficiency implements Serializable{

	private static final long serialVersionUID = 302788557565159670L;

	public static void frontier(List<String> metricsList, List<EessInstance> skyline, List<EessInstance> dominated){
		double k = Integer.MAX_VALUE;
		
		for(EessInstance i : skyline){
			double v = i.getComposedMetrics(metricsList);
			
			if(Double.compare(v, k) < 0){
				k = v;
			}
		}
		
		for(int i = dominated.size() - 1; i > -1 ; i--){
			EessInstance li = dominated.get(i);
			double v = li.getComposedMetrics(metricsList);
			
			if(Double.compare(v, k) > -1){
				skyline.add(li);
                dominated.remove(i);
			}
		}
	}
}
