import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;
import java.io.FileWriter;
import java.io.BufferedWriter;

//TODO: GIVE this class the dignity to live in a new file?
class SortedExam{
	
	final public int id;       //n of exams
	final public int nOfConfl; //key
	public double costWeight; //how much this exam influence cost of OF 
									//part of the penalty due to this exam
	public int nSlotsFree; //calculate when you check the exams in conflict
						   //in an iteration of the algorithm to generate a sol

	public SortedExam(int id, int nOfConfl, int nSlotsFree){
		this.id = id;		
		this.nSlotsFree = nSlotsFree;
		this.nOfConfl = nOfConfl;
		this.costWeight = -1;
	}
	
	public SortedExam(SortedExam old){
		this.id = old.id;
		this.nOfConfl = old.nOfConfl;
		this.nSlotsFree = old.nSlotsFree;
		this.costWeight=old.costWeight;
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

//THIS CLASS HAS THE DUTY OF LOADING
//THE PROBLEMS FROM THE INPUT
public class Problem {
	private int N_EXAMS;
	private int N_STUDENTS;
	private int N_TIMESLOTS;
	//TODO: USE ANOTHER DATA STRUCTURE INSTEAD OF MATRIX FOR CONFLICT MATRIX?
	private int[][] conflictMatrix;
	//needed 2 forms for algorithmic reasons:
	//in priorityqueue and in array.
	private PriorityQueue<SortedExam> sortedExams;
	//exams ordered by id in the array, but they are object SortedExam
	private SortedExam[] arraySortedExams; 
	private HashSet<Integer> timeslotSet;
	private List<Integer> examSet;
	
	public Problem(String instanceName) {
		if(instanceName != null) {
			N_EXAMS = readNExams(instanceName);
			N_TIMESLOTS = readNTimeslots(instanceName);
			generateConflicts(instanceName);
			generateSortedExams();
			generateSets();
		}
	}
	
	private int readNTimeslots(String instanceName){
		int n=0;

		try{
			Scanner in = new Scanner(new File(instanceName + ".slo"));
			n = in.nextInt();
			in.close();
		} catch(FileNotFoundException e) {
            System.out.println("File not found: "+ instanceName + ".slo");
            System.exit(-1);
        } catch(NoSuchElementException e){
        	System.out.println("Empty file: " + instanceName + ".slo");
        	System.exit(-1);
        }
		
		return n;//ritorna n
	}

	private int readNExams(String instanceName){
		//WE COUNT THE ROWS OF THE FILE .exm
		//WE ASSUME THAT EXAMS GO FROM 0 TO N_EXAMS-1
		int nExams=0;
		
		try{
			//counting rows of the file. it works, trust me!
			Scanner in = new Scanner(new File(instanceName + ".exm"));
			
			int stud;//useless
			while( in.hasNext()){
				String s = in.next();
				s = s.replaceFirst("^0+(?!$)", ""); //regex that delete leading zeros
				nExams = Integer.parseInt(s);
				stud = in.nextInt();
			}
		} catch(FileNotFoundException e) {
            System.out.println("File not found: " + instanceName + ".exm");
            System.exit(-1);
        }
		
		return nExams;
	}
	
	private void generateConflicts(String instanceName){
		//WE ASSUME THAT STUDENTS GO FROM 0 TO N_STUDENTS-1
		
			//a vector of students that includes in each element
			//a list of exams in which the student is enrolled
			//Vector allows us to insert numbers in a dynamic array
			Vector<Vector<Integer>> studentList = new Vector<Vector<Integer>>();
			conflictMatrix = new int[N_EXAMS][N_EXAMS];
			try {
			//reading exams for each student
				Scanner in = new Scanner(new File(instanceName + ".stu"));
			
				while (in.hasNextLine() && in.hasNext()){	
					//READ SID WITHOUT INITIAL 's'
					StringBuilder sb = new StringBuilder(in.next());
					sb.deleteCharAt(0);
					int sId = Integer.parseInt(sb.toString()) - 1;//we start from studentId 0
																//READ exam FOR THAT sId
					String s = in.next();
					s = s.replaceFirst("^0+(?!$)", ""); //regex that delete leading zeros
					int exam = Integer.parseInt(s) - 1;//due to Vector index convention, we start
													//counting from exam 0
			
					//if the student has never enrolled until now
					if (sId >= studentList.size()){
						studentList.add(new Vector<Integer>());
					}
					//add the exam in the list
					studentList.get(sId).add(exam);
				}
				in.close();
			}catch(FileNotFoundException e) {
	            System.out.println("File not found: " + instanceName + ".stu");
	            System.exit(-1);
	        }
			N_STUDENTS = studentList.size();
			//POPULATE CONFLICT MATRIX n
			for(int s=0; s<N_STUDENTS; s++) {
				//for each student
				//we iterate through all the possible couples of exam in which he's enrolled
				for(int i=0; i<(studentList.get(s).size() - 1); i++) {
					for(int j=i+1; j<studentList.get(s).size(); j++) {
						int e1, e2; //the two exams involved
						e1= studentList.get(s).get(i); 
						e2= studentList.get(s).get(j);
						conflictMatrix[e1][e2]++;//increment the corresponding value in the conflict matrix
						conflictMatrix[e2][e1]++;//in both cases
						
					}
				}
			} 
	}
	public int getConflicts(int e1, int e2) {
		return conflictMatrix[e1][e2];
	}
	
	public boolean areExamsInConflicts(int e1, int e2) {
		if(getConflicts(e1, e2) > 0)
			return true;
		return false;
	}
	public int getExams() {return N_EXAMS;}
	public int getStudents() {return N_STUDENTS;}
	public int getTimeslots() {return N_TIMESLOTS;}
	public PriorityQueue<SortedExam> getSortedExams() {return sortedExams;}
	public SortedExam getSortedExam(int i) {return (SortedExam) arraySortedExams[i];}
	public HashSet<Integer> getTimeslotSet() {return timeslotSet;}
	public List<Integer> getExamSet() {return examSet;}
	
	
	private void generateSortedExams(){
		sortedExams = new PriorityQueue<SortedExam>(11, new ConflictComparator());
		
		arraySortedExams = new SortedExam[N_EXAMS];
		int nOfConfl;
		for(int e1=0; e1<N_EXAMS; e1++){
			nOfConfl=0;
			for(int e2=0; e2<N_EXAMS; e2++){
				if (areExamsInConflicts(e1, e2)){
					nOfConfl++;
				}
			}
			SortedExam ex = new SortedExam(e1, nOfConfl, N_TIMESLOTS);
			sortedExams.add(ex);
			
			arraySortedExams[e1] = ex;
		}
	}
	
	private void generateSets(){
		timeslotSet = new HashSet<Integer>(N_TIMESLOTS);
		examSet = new ArrayList<Integer>(N_EXAMS);
		
		for(int t=0; t<N_TIMESLOTS; t++){
			timeslotSet.add(t);
		}
		
		for(int e=0; e<N_EXAMS; e++){
			examSet.add(e);
		}
	}
	
	public void generateOutput(String instanceName, Individual fittest ) { 
		
		String fileName = instanceName + "_OMAAL_group20.sol";
		
		try{
			FileWriter fw = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fw);
			for(int i=0; i<N_EXAMS; i++) {
				bw.write((i+1) + "  " + (fittest.getGene(i)+1));
				if(i < N_EXAMS-1)
					bw.write("\r\n");
			}
			bw.flush();
			bw.close();
	     
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		
	}
	
	public void checkOutputFile(String instanceName, Individual fittest) {
		String fileName=instanceName + "_OMAAL_group20.sol";
		File file=new File(fileName);
		
		try{
			if(file.exists()) {
				Individual oldfittest=new Individual(this);
				
				Scanner in = new Scanner(file);
				
				System.out.println("File " + fileName + " may already exist");
				
				 while(in.hasNextInt()) {
				    int i=in.nextInt() - 1;
				    int allele = in.nextInt() - 1;
				    oldfittest.setGene(i, allele);
				 }
				in.close();
	
				if(oldfittest.getCost()>fittest.getCost()) {
					//file.delete();
					generateOutput(instanceName,fittest);
					System.out.println("File " + fileName + " overwritten with a new best solution");
					}
				else {
					System.out.println("File " + fileName + " already contains the best solution");
					System.out.println("Best solution for "+ instanceName + ":");
					System.out.println(oldfittest.toString());
				}
			
			} else {
				generateOutput(instanceName,fittest);
				System.out.println("File " + fileName + " created");
			}
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}// end of class
