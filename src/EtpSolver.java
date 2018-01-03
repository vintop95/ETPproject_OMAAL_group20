//CLASS THAT CONTAINS THE MAIN

public class EtpSolver {
	
	//used by other classes
	static public double timeLimit;
	
	public static void main(String[] args) {
		//usage: java -jar EtpSolver.jar instancename -t timelimit
		System.out.println("program is working..");
		if(args.length != 3) {
			System.out.println("usage: java -jar EtpSolver.jar instancename -t timelimit");
			System.exit(-1);
		}
		
		final int popMaxSize = 15;
		final String instanceName = args[0];
		timeLimit = Integer.parseInt(args[2]);
		//we start counting time
		long startTime = System.currentTimeMillis();
		
		
		//WE DEFINE A PROBLEM AND WE INSERT IT IN THE ALGORITHM (STATIC CLASS)
		Problem p = new Problem(instanceName);
		OptimizationAlgorithm.setProblem(p);
		Population myPop = new Population(popMaxSize, p, true);
		
		double timeElapsed = updateTimeElapsed(startTime);
		while(timeElapsed < timeLimit){
			double currentCost = p.getBestInd().getCost();
			myPop = OptimizationAlgorithm.evolvePopulation(myPop);
			double newCost= p.getBestInd().getCost();
			if(newCost<currentCost){
				System.out.println("best solution now: " + p.getBestInd().getCost() + " in " + timeElapsed + " s");
			}
			timeElapsed = updateTimeElapsed(startTime);
			
		}
		System.out.println("Best solution found in " + timeElapsed + " seconds:");
		System.out.println(p.getBestInd());
		
		//print the best solution in the file 
		p.outputFile(p.getBestInd());
	
	}
	
	//this method returns the seconds elapsed from the start
	public static double updateTimeElapsed(long startTime){
		long currentTime = System.currentTimeMillis();
		double diff = (double)(currentTime - startTime);
		double elapsedTime = diff/1000;
		return elapsedTime;
	}

}
