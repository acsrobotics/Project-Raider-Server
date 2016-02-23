package lib;

import java.util.Collection;
import java.util.LinkedList;

import org.opencv.core.Rect;

public class FilterPipeline {
	
	public interface Filter{
		public boolean test(CvPipeline pipe, Rect rect);
	}
	
	Collection<Filter> filterProceduers;
	CvPipeline pipe;
	
	public FilterPipeline(){
		this.filterProceduers = new LinkedList<>();
	}
	
	public void injectPipeDependency(CvPipeline pipe){
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
