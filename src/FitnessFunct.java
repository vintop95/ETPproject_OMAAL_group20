//THIS CLASS HANDLES THE OBJECTIVE FUNCTION
class FitnessFunct {
	private static Problem p;
	private static double w[] = {0.0, 16.0, 8.0, 4.0, 2.0, 1.0};
	
	//IT RETURNS THE FITNESS OF A SOLUTION (THAT IS DIFFERENT FROM THE OBJ FUNCT VALUE)
	public static double evaluate(Individual timetable) {
		//WE SET A MAXIMUM FITNESS VALUE:
		//IF THE OBJ FUNCT VALUE IT'S LOWER THAN THE MAXIMUM FIT
		//THE HIGHER THE OBJ FUNCT VALUE, THE LOWER IS THE FITNESS
		//OTHERWISE (THE OBJ FUNCT IS VERY HIGH) WE CONSIDER IT VERY FIT
		double cost = getCost(timetable);
		double maxFit = 200.0;
		double fitness = (cost<maxFit) ? maxFit - cost : maxFit;

		//A LEGAL SOLUTION MUST HAVE A HIGHER FIT TO NEVER SELECT NOT LEGAL SOLUTIONS
		if(isLegal(timetable))
			fitness = fitness + 2*maxFit;
		
		return fitness;
	}
	

	//IT CALCULATES THE OBJECTIVE FUNCTION VALUE
	protected static double getCost(Individual timetable) {
		double cost= 0.0;
		int numExams= p.getExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getGene(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2= timetable.getGene(exam2);
				int d = Math.abs(period1 - period2);
				if(d <= 5)
					cost += w[d]*p.getConflicts(exam1, exam2);
			}
		}
		
		return cost/p.getStudents();
	}
	
	//IT CHECKS IF THE GIVEN SOLUTION IS LEGAL
	protected static boolean isLegal(Individual timetable) {
		int numExams= p.getExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getGene(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2 = timetable.getGene(exam2);
				if(period1 == period2) {
					if(p.areExamsInConflicts(exam1, exam2))
						return false; 
				}
			}
		}
		return true;
	}
	
	//OTHER METHODS
	public static void setProblem(Problem p) {
		FitnessFunct.p = p;
	}
}
