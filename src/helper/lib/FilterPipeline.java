package helper.lib;

import org.opencv.core.Rect;

import java.util.Collection;
import java.util.LinkedList;

public class FilterPipeline {
	
	public interface Filter{
		public boolean test(CvEngine pipe, Rect rect);
	}
	
	Collection<Filter> filterProceduers;
	CvEngine pipe;
	
	public FilterPipeline(){
		this.filterProceduers = new LinkedList<>();
	}
	
	public void injectPipeDependency(CvEngine pipe){
		this.pipe = pipe;
	}
	
	public boolean eval(Rect rect){
		boolean state = true;
		for(Filter filter : filterProceduers){
			if(!filter.test(this.pipe, rect)){
				state = false;
				break;
			}
		}
		return state;
	}
	
	public void addFilter(Filter filter){
		this.filterProceduers.add(filter);
	}
	
}
