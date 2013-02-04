package junit.Helper;

import java.util.Set;

public class SetComputation {
	
	public <K> boolean noInterection(Set<K> expert, Set<K> ml){
		return true;
	}
	
	public <K> boolean isNonEmptySubset(Set<K> expert, Set<K> ml){
		if(expert.isEmpty() || ml.isEmpty()) return false;
		else if ((expert.containsAll(ml) || ml.containsAll(expert))) return true;
		else return false;
	}
	
	public <K> boolean isIntersection(Set<K> expert, Set<K> ml){
		boolean Jury1 = false;
		boolean Jury2 = false;
		
		for(K foo : expert){
			if (ml.contains(foo)){Jury1 = true;}
		}
		for(K foo : ml){
			if (expert.contains(foo)){Jury2 = true;}
		}
		
		if(Jury1 || Jury2) return true;
		else return false;
	}

}
