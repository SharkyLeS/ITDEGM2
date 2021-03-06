package tsp;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tsp.AntColony.AntAlgorithm;
import tsp.GA.GA;
import tsp.SA.SA;
import tsp.heuristic.AHeuristic;
import tsp.metaheuristic.AMetaheuristic;
import tsp.metaheuristic.ThreadPerso;
import tsp.opt.opt_2;

/**
 * 
 * This class is the place where you should enter your code and from which you can create your own objects.
 * 
 * The method you must implement is solve(). This method is called by the programmer after loading the data.
 * 
 * The TSPSolver object is created by the Main class.
 * The other objects that are created in Main can be accessed through the following TSPSolver attributes: 
 * 	- #m_instance :  the Instance object which contains the problem data
 * 	- #m_solution : the Solution object to modify. This object will store the result of the program.
 * 	- #m_timeLimit : the maximum time limit (in seconds) given to the program.
 *  
 * @author Damien Prot, Fabien Lehuede, Axel Grimault
 * @version 2017
 * 
 */
public class TSPSolver {

	// -----------------------------
	// ----- ATTRIBUTS -------------
	// -----------------------------

	/**
	 * The Solution that will be returned by the program.
	 */
	private Solution m_solution;

	/** The Instance of the problem. */
	private Instance m_instance;

	/** Time given to solve the problem. */
	private long m_timeLimit;


	// -----------------------------
	// ----- CONSTRUCTOR -----------
	// -----------------------------

	/**
	 * Creates an object of the class Solution for the problem data loaded in Instance
	 * @param instance the instance of the problem
	 * @param timeLimit the time limit in seconds
	 */
	
	public TSPSolver(Instance instance, long timeLimit) {
		m_instance = instance;
		m_solution = new Solution(m_instance);
		m_timeLimit = timeLimit;
	}

	// -----------------------------
	// ----- METHODS ---------------
	// -----------------------------

	/**
	 * **TODO** Modify this method to solve the problem.
	 * 
	 * Do not print text on the standard output (eg. using `System.out.print()` or `System.out.println()`).
	 * This output is dedicated to the result analyzer that will be used to evaluate your code on multiple instances.
	 * 
	 * You can print using the error output (`System.err.print()` or `System.err.println()`).
	 * 
	 * When your algorithm terminates, make sure the attribute #m_solution in this class points to the solution you want to return.
	 * 
	 * You have to make sure that your algorithm does not take more time than the time limit #m_timeLimit.
	 * 
	 * @throws Exception may return some error, in particular if some vertices index are wrong.
	 */
	public void solve() throws Exception {
		long startTime = System.currentTimeMillis();
		
		// Solution initiale : plus proche voisin
		PlusProchesVoisins ppv = new PlusProchesVoisins(m_instance, "PlusProchesVoisins1.0", this.getTimeLimit());
		Solution solutionIni = ppv.Voisin(0);
		
		Callable[] solvers = new Callable[4];

		// Thread 1 : algorithme de colonie de fourmis + 2-opt
		solvers[0] = new ThreadPerso(new AntAlgorithm(m_instance,solutionIni),
				solutionIni, 0 , getTimeLimit());
		
		// Thread 2 : algorithme de colonie de fourmis + 2-opt
		solvers[1] = new ThreadPerso(new AntAlgorithm(m_instance,solutionIni),
				solutionIni, 0 , getTimeLimit());
		
		// Thread 3 : algorithme de colonie de fourmis + 2-opt
		solvers[2] = new ThreadPerso(new AntAlgorithm(m_instance,solutionIni),
				solutionIni, 0 , getTimeLimit());
		
		// Thread 4 :  2-opt + algorithme SA
		solvers[3] = new ThreadPerso(new SA(m_instance),
				solutionIni, 0 , getTimeLimit()-2);		
		
		// Déclaration et exécution des 4 threads
		ExecutorService exe = Executors.newFixedThreadPool(4); 
		Future<Solution> fut0 = exe.submit(solvers[0]);
		Future<Solution> fut1 = exe.submit(solvers[1]);
		Future<Solution> fut2 = exe.submit(solvers[2]);
		Future<Solution> fut3 = exe.submit(solvers[3]);
		
		// Comparaison des solutions et fermeture des threads
		m_solution = translation(compareSolution(fut0.get(),fut1.get(),fut2.get(),fut3.get()));
		exe.shutdownNow();
		
		/*if (fut3.get().getObjectiveValue() == m_solution.getObjectiveValue()) {
		System.out.println("Meilleure solution renvoyée par Sharkyyyyyy le BG");
	} else {
		System.out.println("Meilleure solution renvoyée par Max et Guigui les BG");
	}*/
	}

	// -----------------------------
	// ----- GETTERS / SETTERS -----
	// -----------------------------

	/** @return the problem Solution */
	public Solution getSolution() {
		return m_solution;
	}

	/** @return problem data */
	public Instance getInstance() {
		return m_instance;
	}

	/** @return Time given to solve the problem */
	public long getTimeLimit() {
		return m_timeLimit;
	}

	/**
	 * Initializes the problem solution with a new Solution object (the old one will be deleted).
	 * @param solution : new solution
	 */
	public void setSolution(Solution solution) {
		this.m_solution = solution;
	}

	/**
	 * Sets the problem data
	 * @param instance the Instance object which contains the data.
	 */
	public void setInstance(Instance instance) {
		this.m_instance = instance;
	}

	/**
	 * Sets the time limit (in seconds).
	 * @param time time given to solve the problem
	 */
	public void setTimeLimit(long time) {
		this.m_timeLimit = time;
	}

	
	// ----------------------------------------
	// ----- PERSONAL METHODS -----
	// ----------------------------------------
	
	
	/**
	 * Compare deux solutions et renvoie celle qui a le trajet le plus efficace
	 * @param s1 solution 1 à comparer
	 * @param s2 solution 2 à comparer
	 * @return la solution ayant le plus court chemin parmi s1 et s2
	 * @throws Exception si une des solutions n'est pas une solution faisable
	 */
	public Solution compareSolution(Solution s1, Solution s2) throws Exception {
		if((!s1.isFeasible()) || (!s2.isFeasible())) {
			throw new Exception("Error : at least one solution is not feasible");
		}
		if (s1.getObjectiveValue()>s2.getObjectiveValue()) {
			return s2;
		} else {
			return s1;
		}
	}
	
	/**
	 * Compare 4 solutions et renvoie celle qui a le trajet le plus efficace
	 * @param s1 solution 1 à comparer
	 * @param s2 solution 2 à comparer
	 * @param s3 solution 3 à comparer
	 * @param s4 solution 4 à comparer
	 * @return la solution ayant le plus court chemin parmi s1, s2, s3 et s4
	 * @throws Exception si une des solutions n'est pas une solution faisable
	 */
	public Solution compareSolution(Solution s1, Solution s2, Solution s3,Solution s4) throws Exception {
		return compareSolution(compareSolution(s1,s2),compareSolution(s3,s4));
	}
	
	/** Translate la solution s pour que la ville initiale soit 0
	 * @param s une solution
	 * @return la solution translatee
	 * @throws Exception
	 */
	public Solution translation(Solution s) throws Exception {
		Solution solFinale = new Solution(this.m_instance);
		
		int i = 0;
		while (s.getCity(i)!=0) {
			i++;
		}
		solFinale.setCityPosition(0, 0);
		solFinale.setCityPosition(0, m_instance.getNbCities());
		for (int j= 1; j<m_instance.getNbCities(); j++) {
			solFinale.setCityPosition(s.getCity((i+j)%(m_instance.getNbCities())),j);
		}
		solFinale.evaluate();
		return solFinale;
	}
	
}
