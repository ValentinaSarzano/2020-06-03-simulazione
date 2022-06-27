package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer, Player> idMap;
	
	private List<Player> best;
	private int maxTitolarita;
	
	public Model() {
		super();
		this.dao = new PremierLeagueDAO();
	}
	
	public void creaGrafo(Double x) {
		this.grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		this.idMap = new HashMap<>();
		
		this.dao.getVertici(x, idMap);
		
		//Aggiungo i vertici
		Graphs.addAllVertices(this.grafo, idMap.values());
		
		//Aggiungo gli archi
		for(Adiacenza a: this.dao.getAdiacenze(idMap)) {
			if(this.grafo.containsVertex(a.getP1()) && this.grafo.containsVertex(a.getP2())) {
				if(a.getPeso() > 0) { // p1 > p2
					Graphs.addEdgeWithVertices(this.grafo, a.getP1(), a.getP2(), a.getPeso());
				}else if(a.getPeso() < 0) { // p2 > p1
					Graphs.addEdgeWithVertices(this.grafo, a.getP2(), a.getP1(), Math.abs(a.getPeso()));
				}
			}
		}
		System.out.println("Grafo creato!");
		System.out.println("#VERTICI: "+ this.grafo.vertexSet().size());
		System.out.println("#ARCHI: "+ this.grafo.edgeSet().size());
			
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}

	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public boolean grafoCreato() {
		if(this.grafo == null)
			return false;
		else 
			return true;
	}


	public Player getTopPlayer() {
		Player topPlayer = null;
		int numMaxBattuti = 0;
		for(Player p: this.grafo.vertexSet()) {
			if(this.grafo.outDegreeOf(p) > numMaxBattuti) {
				numMaxBattuti = this.grafo.outDegreeOf(p);
				topPlayer = p;
			}
		}
		/*for(Player p: this.grafo.vertexSet()) {
			if(this.grafo.outDegreeOf(p) == numMaxBattuti) {
				topPlayer = p;
			}
		}*/
		return topPlayer;
	}
	
	public List<Battuto> getBattuti(Player topPlayer){
		List<Battuto> battuti = new ArrayList<>();
		for(DefaultWeightedEdge e: this.grafo.outgoingEdgesOf(topPlayer)) {
			Battuto b = new Battuto(this.grafo.getEdgeTarget(e), (int) this.grafo.getEdgeWeight(e));
		    battuti.add(b);
		}
		Collections.sort(battuti, new Comparator<Battuto>() {

			@Override
			public int compare(Battuto o1, Battuto o2) {
				return (-1)*o1.getPeso().compareTo(o2.getPeso());
			}
			
		});
		return battuti;
	}
	
	public List<Player> trovaPercorso(int k){
		
		this.best = new ArrayList<>();
		
		List<Player> parziale = new ArrayList<>();
		
		this.maxTitolarita = 0;
		
		List<Player> candidati = new ArrayList<>(this.grafo.vertexSet());
		
	
		cerca(parziale, k, candidati);
		
		
		return best;
	}

	private void cerca(List<Player> parziale, int k, List<Player> candidati) {

			if(parziale.size() == k) {
				int titolarita = this.getTitolarita(parziale);
				if(titolarita > this.maxTitolarita) {
					 this.best = new ArrayList<>(parziale);
					 maxTitolarita = titolarita;
				}
				return;
			}
		
		for(Player p: candidati) {
			if(!parziale.contains(p)) {
				parziale.add(p);
				List<Player> giocatoriRimanenti = new ArrayList<>(candidati);
				giocatoriRimanenti.removeAll(Graphs.successorListOf(this.grafo, p));
				cerca(parziale, k, giocatoriRimanenti);
				parziale.remove(parziale.size()-1);
			}
		}
		
	}

	private Integer getTitolarita(List<Player> parziale) {
		
		int titolaritaTot = 0;
		int gradoTitolarita = 0;
		for(Player p: parziale) {
			int sommaUscenti = 0;
			for(DefaultWeightedEdge e: this.grafo.outgoingEdgesOf(p)) {
				sommaUscenti += this.grafo.getEdgeWeight(e);
			}
			int sommaEntranti = 0;
			for(DefaultWeightedEdge e: this.grafo.incomingEdgesOf(p)) {
				sommaEntranti += this.grafo.getEdgeWeight(e);
			}
			gradoTitolarita = sommaUscenti-sommaEntranti;
			titolaritaTot += gradoTitolarita; //Per ogni singolo giocatore
		}
		
		return titolaritaTot;
	}
	
	public Integer getTitolaritaMax() {
		return this.maxTitolarita;
	}
}
