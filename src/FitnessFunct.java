
class FitnessFunct {
	private static Problem p;
	private static double w[] = {0.0, 16.0, 8.0, 4.0, 2.0, 1.0};
	public static void setProblem(Problem p) {
		FitnessFunct.p = p;
	}
	
	protected static double getCost(Individual timetable) {
		double cost= 0.0;
		int numExams= p.getExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getGene(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2= timetable.getGene(exam2);
				int d = Math.abs(period1 - period2);
				if(d<6)
					cost+= w[d]*p.getConflicts(exam1, exam2);
			}
		}
		
		return cost/p.getStudents();
	}
	
	protected static boolean isLegal(Individual timetable) {
		int numExams= p.getExams();
		
		for(int exam1=0; exam1<numExams; exam1++) {
			int period1 = timetable.getGene(exam1);
			for(int exam2=exam1+1; exam2<numExams; exam2++) {
				int period2= timetable.getGene(exam2);
				if(period1 == period2) {
					if(p.areExamsInConflicts(exam1, exam2))
						return false;
				}
			}
		}
		return true;
	}
	public static double evaluate(Individual timetable) {
		double cost = getCost(timetable);
		double maxFit = 200.0;
		double fitness;
		
		fitness = (cost<maxFit) ? maxFit - cost : maxFit;
		//fitness = maxFit - cost;
		if(isLegal(timetable))
		fitness += 2*maxFit;
		return fitness;
	}
}
