import java.util.*;
import java.io.*;


public class EtpSolver {

	static int readNTimeslots(String istanceName){
		
		int n=0;

		
		try{
			Scanner in = new Scanner(new File(istanceName + ".slo"));
			n = in.nextInt();
			in.close();
		} catch(FileNotFoundException e) {
            System.out.println("FileNotFound");
        } catch(NoSuchElementException e){
        	System.out.println("Empty exm file.");
        }
		
		
		return n;
	}
	
	static int readNExams(String istanceName){
		
		int n=0;
		
		try{
			LineNumberReader count = new LineNumberReader(new FileReader("./" + istanceName + ".stu"));
			
			//TO TEST!!
			while (count.skip(Long.MAX_VALUE) > 0)
			   {
			      // Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read the entire file
			   }

			n = count.getLineNumber() + 1; // +1 because line index starts at 0  
			count.close();
		
		} catch(FileNotFoundException e) {
            System.out.println("FileNotFound");
        } catch(IOException e){
        	System.out.println("IOException.");
        }
		
		return n;
	}

	static void generateConflicts(String istanceName, int[][] n){
		
		try{
			//a vector of students that includes in each element
			//a vector of exams in which the student is enrolled
			Vector<Vector<Integer>> studentList = new Vector<Vector<Integer>>();
			
			//reading exams of the students
			Scanner in = new Scanner(new File(istanceName + ".stu"));
			while (in.hasNextInt()){
				//the position 0 is always empty
				//we ignore and start from 1
				int sId = in.nextInt();
				
				//if the student has never enrolled until now
				if (sId > studentList.size()){
					studentList.add(new Vector<Integer>());
				}
				//add the exam in the list
				int exam = in.nextInt();
				studentList.get(sId).add(exam);
			}
			
			in.close();	
		} catch(FileNotFoundException e) {
            System.out.println("FileNotFound");
        }
	}
	
	public static void main(String[] args) {
		
		System.out.println("program is working..");
		
		if (args[0]==null){
			System.out.println(args[0] + "instance name not inserted");
			System.exit(-1);
		}
		if(args[2]==null){
			System.out.println("timelimit not inserted");
			System.exit(-1);
		}
		
		final String instanceName = args[0];
		final double timeLimit = Integer.parseInt(args[2]);
		
		//initialize counting the rows of instanceXX.exm
		final int N_EXAMS = readNExams(instanceName);
		
		//load from instanceXX.slo
		final int N_TIMESL = readNTimeslots(instanceName);
		
		
		//Conflict matrix
		//may be too large
		int[][] n = new int[N_EXAMS][N_EXAMS];	
		//load from instanceXX.stu 
		generateConflicts(instanceName, n);
		
		

	}

}
