import java.util.Comparator;
import java.util.Vector;

public class SortedExam{
	
	final public int id;       //n of exams
	final public int nOfConfl; //key
	public double costWeight; //how much this exam influence cost of OF 
									//part of the penalty due to this exam
	
	public Vector<Integer> conflictingExams;
	
	public SortedExam(int id, int nOfConfl, Vector<Integer> conflictingExams){
		this.id = id;
		this.nOfConfl = nOfConfl;
		this.costWeight = -1;
		this.conflictingExams = conflictingExams;
	}
	
	public int getId(){
		return this.id;
	}
	
	@Override
	public boolean equals(Object other){
		SortedExam se = (SortedExam) other;
		return (se.id == this.id);
	}
}

class ConflictComparator implements Comparator<SortedExam> {
	
	public int compare(SortedExam c1, SortedExam c2){
		if(c1.nOfConfl < c2.nOfConfl)
			return 1;
		else if(c1.nOfConfl == c2.nOfConfl)
			return 0;
		else
			return -1;
	}
	
}

class CostWeightComparator implements Comparator<SortedExam> {
	
	public int compare(SortedExam c1, SortedExam c2){
		if(c1.costWeight < c2.costWeight)
			return 1;
		else if(c1.costWeight == c2.costWeight)
			return 0;
		else
			return -1;
	}
	
}