import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//TODO: CREATE DEDICATED CLASSES FOR EVERY PROCESS OF THE ALGORITHM



//TODO: gestire il tempo in questo modo 
/*long startTime = System.currentTimeMillis();

...
...
// operazioni ...
...
...

long endTime = System.currentTimeMillis();
long seconds = (endTime - startTime) / 1000;
*/
public class EtpSolver {
	
	static public double timeLimit;
	//SHOULD BE OK
	
	public static void main(String[] args) {
		//usage: java -jar EtpSolver.jar instancename -t timelimit
		System.out.println("program is working..");
		if(args.length != 3) {
			System.out.println("usage: java -jar EtpSolver.jar instancename -t timelimit");
			System.exit(-1);
		}
		
		final int popSize = 15;
		//final int maxNonImprovingIterations = 1000;
		final String instanceName = args[0];
		timeLimit = Integer.parseInt(args[2]);
		//we start counting
		long startTime= System.currentTimeMillis();
		
		
		//WE DEFINE A PROBLEM AND WE INSERT IT IN THE ALGORITHM (STATIC CLASS)
		Problem p = new Problem(instanceName);
		GeneticAlgorithm.setProblem(p);
		Population myPop = new Population(popSize, p, true);
		Individual fittest = myPop.getFittest();
		double currentOptimalCost = fittest.getCost();
		
		double timeElapsed = updateTimeElapsed(startTime);
		while(timeElapsed < timeLimit){
			
			myPop = GeneticAlgorithm.evolvePopulation(myPop);
			fittest = myPop.getFittest();
			
			if(fittest.getCost() < currentOptimalCost){
				currentOptimalCost = fittest.getCost();
				System.out.println("NEW better solution: " + fittest.toString());
				
			}
			timeElapsed = updateTimeElapsed(startTime);
			
		}
		System.out.println("Best solution found in " + timeElapsed + " seconds:");
		System.out.println(myPop.getFittest());
		p.checkOutputFile(instanceName, myPop.getFittest());
	}
	
	public static double updateTimeElapsed(long startTime){
		long currentTime = System.currentTimeMillis();
		double elapsedTime = (currentTime - startTime)/1000;
		return elapsedTime;
	}

}//end of class
