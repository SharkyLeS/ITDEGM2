package tsp.AntColony;
import java.util.ArrayList;
import tsp.Instance;
import tsp.Solution;
import tsp.metaheuristic.AMetaheuristic;
import tsp.opt.opt_2;

/** @author guillaume, maxli
 * @version finale
 */
public class AntAlgorithm extends AMetaheuristic {

	public static final boolean trace = false;
	public static final int m = 100; // Nombre de fourmis par cycle
	public static final double alpha = 1.5; // pondere les phéromones A DEFINIR
	public static final double beta = 5.0; // pondere la visibilité
	public static final double rho = 0.5; // doit être entre 0 et 1
	public static final double Q = 100.0;

	// -----------------------------
	// ----- ATTRIBUTS -------------
	// -----------------------------
	private Solution bestSol;
	private double[][] proba;
	private double[][] visibilite; // à voir si on garde
	private double[][] pheromones; // les tau (i,j)
	
	/** Constructeur de AntAlgorithm : initialise les variables d'instances
	 * @param instance, instance du probleme
	 * @param sol, une solution initiale pour comparer aux solutions obtenues
	 * @throws Exception
	 */
	public AntAlgorithm(Instance instance, Solution sol) throws Exception {
		super(instance, "Colonie de fourmis");
		this.bestSol = sol.copy();
		this.proba = new double[instance.getNbCities()][instance.getNbCities()];
		this.visibilite = new double[instance.getNbCities()][instance.getNbCities()];
		this.pheromones = new double[instance.getNbCities()][instance.getNbCities()];
		
		// initialise les matrices
		for (int i=0; i<instance.getNbCities(); i++) {
			for (int j=i+1; j<instance.getNbCities(); j++) {
				this.visibilite[i][j] = 1.0/(instance.getDistances(i, j));
				this.visibilite[j][i] = this.visibilite[i][j];
				if (trace) System.out.println("distance entre " + i + " et " + j +" : "+instance.getDistances(i, j));
				if (trace) System.out.println("visibilité entre " + i + " et " + j +" : "+this.visibilite[i][j]);
				this.pheromones[i][j] = 10;
				this.pheromones[j][i] = 10;
			}
			this.visibilite[i][i] = 0;
			this.pheromones[i][i] = 0;
		}
	}
	
	// GETTERS AND SETTERS
	
	/** @return la solution contenue dans bestSol*/
	public Solution getBestSol() {
		return this.bestSol;
	}
	
	/**Remplace la solution bestSol par sol 
	 * @param sol */
	public void setBestSol(Solution sol) {
		this.bestSol = sol;
	}
	
	/**@param int i, 0<=i<=super.getInstances().getNbCities();
	 * @param int j, 0<=j<=super.getInstances().getNbCities();
	 * @return la probabilite de passer de la ville i a la ville j */
	public double getProba(int i,int j) {
		return this.proba[i][j];
	}

	/** Remplace la probabilite de passer de i a j par valeur
	 * @param double valeur, valeur qui correspond a une probabilite
	 * @param int i, 0<=i<=super.getInstances().getNbCities();
	 * @param int j, 0<=j<=super.getInstances().getNbCities(); */
	public void setProba(double valeur,int i, int j) {
		this.proba[i][j] = valeur;
	}
	
	/** @param int i, 0<=i<=super.getInstances().getNbCities();
	 * @param int j, 0<=j<=super.getInstances().getNbCities();
	 * @return l'attraction de la ville j sur la ville i (visibilite) */
	public double getVisibilite(int i,int j) {
		return visibilite[i][j];
	}
	
	/** Remplace l'attraction de la ville j sur  la ville i 
	 * @param  double valeur, valeur qui correspond a une attraction
	 * @param int i, 0<=i<=super.getInstances().getNbCities();
	 * @param int j, 0<=j<=super.getInstances().getNbCities(); */
	public void setVisibilite(double valeur,int i, int j) {
		this.visibilite[i][j] = valeur;
	}
	
	/** @param int i, 0<=i<=super.getInstances().getNbCities();
	 * @param int j, 0<=j<=super.getInstances().getNbCities();
	 * @return la quantite de pheromone present sur l'arc (i,j) (passage de i a j) */
	public double getPheromones(int i,int j) {
		return pheromones[i][j];
	}
	
	/** Remplace la quantite de pheromone sur (i,j) par valeur
	 * @param double valeur, valeur qui correspond a une quantite de pheromones
	 * @param int i , 0<=i<=super.getInstances().getNbCities();
	 * @param int j, 0<=j<=super.getInstances().getNbCities(); */
	public void setPheromones(double valeur, int i, int j) {
		this.pheromones[i][j] = valeur;
	}
	
	/** La fourmie est a la ville i (colonne i dans la matrice) est on met a jour les 
	 * probabilites qu'elle passe dans chaque villes restantes j. Le calcul
	 * des probabilites de passer de i a j se base sur les visibilite[i][j] (constantes)
	 * et pheromones[i][j] ponderees par des constantes 
	 * @param  int i, 0<=i<super.getInstances().getNbCities()-1 ville actuelle
	 * @param ArrayList<Integer>villesRest (villes pas encore parcourues)
	 * 1<=villesRest.size()<super.getInstances().getNbCities()-1.  */
	public void majProba(int i, ArrayList<Integer> villesRest) {
		double acc = 0.0;
		for (int j : villesRest) { // Dans la formule fournis (1), on calcul d'abord le
								//  numerateur pour chaque proba(i,j). 
			double termeAlpha = Math.pow(getPheromones(i, j), alpha);
			double termeBeta = Math.pow(getVisibilite(i,j),beta);
			
			this.proba[i][j] = termeAlpha * termeBeta; 
			
			if (trace) {
				System.out.println("A modifier : de " + i + " à " +j);
				System.out.println("terme en alpha : " +termeAlpha);
				System.out.println("terme en beta : " +termeBeta);
				System.out.println("donc le terme est " + this.proba[i][j]);
				System.out.println("----------------------------");
			}
			
			acc += this.getProba(i, j);
		}
		for (int j : villesRest) { // On divise par la somme des termeAlpha*termBeta
								// de toutes les villes restantes. 
			this.setProba(getProba(i,j)/acc, i, j);
			if (trace) System.out.println("voici finalement la proba[i][j] : "+this.proba[i][j]);
		}
	}
	
	/** La fourmie est sur la ville i et choisie la ville suivante. La colonne i est une
	 * colonne de probabilite. Prenons un exemple simple pour comprendre. S'il ne reste
	 * que 3 villes et qu'on a 0,6 (1); 0,2(2) et 0,2(3) comme proba associees. On tire
	 *  un double aleatoire entre 0 et 1, s'il est entre 0 et 0,6 -> (1) ; entre 0,6 et
	 *  0,8 ->(2) et entre 0,8 et 1 ->(3).   
	 * @param int i, 0<=i<super.getInstances().getNbcities()-1.
	 * @param ArrayList<Integer>villesRest (villes pas encore parcourues)
	 * 1<=villesRest.size()<super.getInstances().getNbCities()-1.
	 * @return ville choisie apres i.
	 * @throws Exception */
	public int choixVille(int i, ArrayList<Integer> villesRest) throws Exception {
		double x = Math.random();
		double acc = 0.0;
		int villeChoisie = -1;
		for (int j : villesRest) {
			if (x<acc+ this.proba[i][j]) {
				villeChoisie = j;
				break;
			} else if (j == villesRest.get(villesRest.size()-1)) {
				villeChoisie = j; //La somme des probabilites peut ne pas faire 1.
								// Si la fourmie regarde la derniere ville sans avoir
								// encore choisie, alors elle prend la derniere ville.
								// ex : le dernier intervalle est [0,65;0,95] et x=0,98
				break;
			} else {
				if (trace) System.out.println("avant, acc = " +acc+ " ; this.proba[i][j] = " + this.proba[i][j]);
				acc += this.proba[i][j];
				if (trace) System.out.println("apres : " +acc);
			}
		}
		if (trace) System.out.println("la ville choisie est " + villeChoisie);
		return villeChoisie;
	}
	
	/** La fourmie a termine son tour. On calcule la quantitite de pheromones supplementaire
	 * depose sur chaque arc de passage.
	 * @param longueur. Il s'agit de la longueur total du cycle (ce sera
	 * getObjectivevalue() puisque qu'on travaille directement sur des objets Solution. 
	 * @return La quantite de pheromone supplementaire depose sur chaque arc de passage.
	 * @throws Exception  */
	public double getDeltaPheromones(double longueur) throws Exception { //Max
		return Q/longueur;
	}
	
	/** Met ici a jour les quantite de pheromone sur chaque arc (i,j). La quantite de pheromones 
	 * presente sur l'arc (i,j) depend de celle deja presente evapore auquelle on ajoute la somme des
	 * pheromones deposes par un nombre m de fourmis precedentes sur ce meme arc
	 * @param ArrayList<Solution>listeSolution de taille m (aurait pu etre un tableau)
	 * @param double pheromones : somme des pheromones deposes sur chaque arc par 
	 * chaque fourmies.
	 * @throws Exception
	 */
	public void majPheromones(ArrayList<Solution> listeSolution, double pheromones) throws Exception { /*Ajout Max */
		for (int i=0;i<super.getInstance().getNbCities()-1;i++) {
			for (int j=i+1;j<super.getInstance().getNbCities()-1;j++) {
				this.setPheromones(rho*this.getPheromones(i,j),i,j); 
				this.setPheromones(rho*this.getPheromones(j,i),j,i); 
			}
		}
		
		for (Solution s : listeSolution) {
			for (int i=0; i<super.getInstance().getNbCities();i++) {
				this.setPheromones(this.getPheromones(s.getCity(i), s.getCity(i+1)) + pheromones, s.getCity(i), s.getCity(i+1));
			}
		}
	}
		/** En partant d'une villeDeDepart, on obtient le chemin effectue par la fourmie.
		 * @param int villeDeDepart, 0<=villeDeDepart<super.getInstances.getNbCities()-1.
		 * @return une solution (cycle effectue par une fourmi partie de VilleDeDepart)
		 * @throws Exception */
		public Solution lanceFourmi(int villeDeDepart) throws Exception {
			Solution sol = new Solution(super.getInstance());
			
			// Initialisation
			sol.setCityPosition(villeDeDepart, 0);
			sol.setCityPosition(villeDeDepart, super.getInstance().getNbCities());
			ArrayList<Integer> villesRestantes = new ArrayList<Integer>();
			for (int i=0; i<super.getInstance().getNbCities(); i++) {
				if (i!=villeDeDepart) villesRestantes.add(i); // Construction de la liste de villes restantes. 
			}
			
			// Boucle
			int i = villeDeDepart; 
			for(int position=1 ; position<super.getInstance().getNbCities();position++) {
				majProba(i,villesRestantes);
				i = choixVille(i,villesRestantes);
				sol.setCityPosition(i, position);
				for(int j=0;j<villesRestantes.size();j++) { 
					if(villesRestantes.get(j)==i) {
						villesRestantes.remove(j); //On retire la ville choisie de villesRestantes
						break;
					} 
				}		
			}
		return sol;
	}
		 /** Fonction permettant de renvoyer la meilleure solution. Tout d'abord, on 
		  * lance m fourmis qui vont laisser des pheromones sur leurs arcs de passage.
		  * On met ensuite la matrice des pheromones a jour et on observe le comportement
		  * des m+1 jusqu'a m+m fourmis avant de reeffectuer un lancement de m fourmies,
		  * etc...On renvoie la meilleur solution en retirant les croisements (via 2-opt) qui augmentent
		  * bestSol.getObjectiveValue();
		  * @param Solution sol : non utile ici car deja passe dans bestSol lors de l'initialisation
		  * @param long time : temps alloue pour effectuer les calculs.
		  * @return la meilleure solution obtenue par la metaheuristique
		  * @throws Exception */
	public Solution solve(Solution sol,long time) throws Exception {
		// this.bestSol = sol.copy(); déjà fait dans l'initialisation !
		long startTime = System.currentTimeMillis();
		ArrayList<Solution> solList = new ArrayList<Solution>();
		Solution solActuelle;
		double deltaPheromone = 0.0;
		int compteur = 0;
		int villeDeDepart = super.getInstance().getNbCities()-1 ;
		bestSol.evaluate();
		
		while (System.currentTimeMillis() - startTime<(time-5)*1000-100) {
			solActuelle = lanceFourmi(villeDeDepart);
			solActuelle.evaluate();
			//System.out.println(solActuelle.getObjectiveValue());
			solList.add(solActuelle);
			deltaPheromone += getDeltaPheromones(solActuelle.getObjectiveValue());
			villeDeDepart = (int) (Math.random()*(super.getInstance().getNbCities()));
			compteur++;

			if (solActuelle.getObjectiveValue()<bestSol.getObjectiveValue()) {
				this.bestSol = solActuelle.copy();
			}

			if (compteur == m) {
				majPheromones(solList,deltaPheromone);
				solList = new ArrayList<Solution>();
				deltaPheromone = 0.0;
				compteur =0;
			}
		}
		
		opt_2 decroisement = new opt_2(super.getInstance());
		this.bestSol = decroisement.solve(bestSol, 5).copy();
		
		
		
		return this.bestSol;
	}

} 
