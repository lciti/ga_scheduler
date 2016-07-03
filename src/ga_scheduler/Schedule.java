// Luca Citi
// University of Essex
// Dec 2015 - Feb 2016
//
// (0) Public domain
// To the extent possible under law, Luca Citi (lciti@ieee.org) has waived all
// copyright and related or neighboring rights to ga_scheduler.
// This work is published from: United Kingdom.

package ga_scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Schedule {
    protected List<VehicleRoute> routes;
    protected List<Journey>[][] connections;
    protected Journey[] journeyForRequest;
    protected static Junction[] jcts;
    protected static int[][] duration;
    protected static int[][] maxDuration;
    static int MAX_DURATION_PERC = 150;
    static int MAX_DURATION_OFFSET = 60;
    static int DEFAULT_WAIT = 10;
    static int MIN_TIME_NEW = 600;
    static int MAX_TIME_NEW = 1080;
    static int DEFAULT_ROUTES = 20;
    static Random rd = new Random();
    
    public Schedule() {
    	routes = new ArrayList<VehicleRoute>(DEFAULT_ROUTES);
    }

    public Schedule(Junction[] jcts) {
    	this();
    	if (Schedule.jcts == jcts) return;
    	Schedule.jcts = jcts;
    	int numJcts = jcts.length;
    	duration = new int[numJcts][numJcts];
    	maxDuration = new int[numJcts][numJcts];
		for (int i = 0; i < numJcts; ++i) {
			for (int j = 0; j < numJcts; ++j) {
				duration[i][j] = jcts[i].timeTo(jcts[j]);
				maxDuration[i][j] = MAX_DURATION_OFFSET + (duration[i][j] * MAX_DURATION_PERC) / 100;
			}
		}
    }
    
    public Schedule add(VehicleRoute vr) {
    	routes.add(vr);
    	return this;
    }
    
    @SuppressWarnings("unchecked")
	public void createMatrices() {
    	int numJcts = jcts.length;
    	// create/reset matrices
    	connections = (List<Journey>[][]) new List<?>[numJcts][numJcts];
    	List<Journey>[][] transfer1 = (List<Journey>[][]) new List<?>[numJcts][numJcts];
		for (int i = 0; i < numJcts; ++i) {
			for (int j = 0; j < numJcts; ++j) {
				connections[i][j] = new LinkedList<Journey>();//ArrayList or LinkedList
				transfer1[i][j] = new LinkedList<Journey>();//ArrayList or LinkedList
			}
		}
    	// direct journeys
    	for (VehicleRoute route : routes) {
    		Leg[] legs = route.legs.toArray(new Leg[route.legs.size()]);
    		for (int i = 0; i < legs.length; ++i) {
    			for (int j = i; j < legs.length; ++j) {
    				if (legs[j].arrives - legs[i].leaves <= maxDuration[legs[i].from.id][legs[j].to.id]) {
    					connections[legs[i].from.id][legs[j].to.id].add(new Journey(Arrays.copyOfRange(legs, i, j+1)));
    				}
    			}
    		}
    	}
    	// remove loops, if any
		for (int i = 0; i < numJcts; ++i) {
			connections[i][i].clear();
    	}
		// one transfer
		for (int i = 0; i < numJcts; ++i) {
			for (int j = 0; j < numJcts; ++j) {
				for (int k = 0; k < numJcts; ++k) {
					if (duration[i][k] > maxDuration[i][j] || duration[k][j] > maxDuration[i][j]) break;
					for (Journey j1: connections[i][k]) {
						if (j1.duration > maxDuration[i][j]) break;
						for (Journey j2: connections[k][j]) {
							if (j1.duration + j2.duration > maxDuration[i][j]) break;
							if (j1.legs[0].route == j2.legs[0].route) continue;
							if (j1.arrives < j2.leaves && j2.arrives - j1.leaves <= maxDuration[i][j]) {
								transfer1[i][j].add(j1.join(j2));
							}
						}
					}
				}
			}
    	}
		// sort by travel duration
		for (int i = 0; i < numJcts; ++i) {
			transfer1[i][i].clear();
			for (int j = 0; j < numJcts; ++j) {
				connections[i][j].addAll(transfer1[i][j]);
				Collections.sort(connections[i][j]);
			}
		}
    }

	public void cleanMatrices() {
		connections = null;
	}
	
	public void accommodateRequests(Collection<Request> requests) {
		// TODO: leaveAfter constraints ignored for now
		if (journeyForRequest == null || journeyForRequest.length != requests.size()) {
			journeyForRequest = new Journey[requests.size()];
		}
		// main loop
		request_loop:
		for (Request request : requests){
			Junction from = request.from, to = request.to;
			// Attempt to accommodate request using existing routes
			for (Iterator<Journey> iter = connections[from.id][to.id].iterator(); iter.hasNext(); ) {
				Journey j = iter.next();
				if (j.canAccommodate(request.party)) {
					j.addPassengers(request);
					journeyForRequest[request.id] = j;
					continue request_loop;
				} else if (request.party == 1) {
					iter.remove();
				}
			}
			// Could not accommodate with existing routes, try to extend existing one
	    	for (VehicleRoute route : routes) {
    			Journey j = null;
	    		if (route.last.to == from && 
	    				route.add(to, DEFAULT_WAIT)) {
	    			j = new Journey(new Leg[] {route.last});
	    		}
	    		if (j == null && route.first.from == to && 
	    				route.prepend(from, DEFAULT_WAIT)) {
	    			j = new Journey(new Leg[] {route.first});
	    		}
	    		if (j != null) {
					j.addPassengers(request);
	    			connections[from.id][to.id].add(0, j);
	    			journeyForRequest[request.id] = j;
	    			continue request_loop;
	    		}
	    	}
			// As nothing worked, create new route
	    	VehicleRoute route = new VehicleRoute(from, to, randomTime(from.timeTo(to)));
	    	this.add(route);
			Journey j = new Journey(new Leg[] {route.first});
			j.addPassengers(request);
			connections[from.id][to.id].add(0, j);
			journeyForRequest[request.id] = j;
			continue request_loop;
		}
	}
	
	static public int randomTime(int travelTime) {
    	return (int) (MIN_TIME_NEW + rd.nextDouble() * (MAX_TIME_NEW - MIN_TIME_NEW - travelTime));
	}

	public void compactAndTrim() {
		// remove empty legs at beginning and end of route, as well as empty routes
		List<VehicleRoute> unused = new ArrayList<VehicleRoute>(routes.size());
		for (VehicleRoute route: routes) {
			if (!route.trim()) unused.add(route);
		}
		routes.removeAll(unused);
		// reduce waiting time if possible (going through all requests in order)
		for (VehicleRoute r: routes) {
			int ready = r.first.leaves;
			for (Leg l: r.legs) {
				l.arrives += ready - l.leaves;
				l.leaves = ready;
				ready = l.arrives + DEFAULT_WAIT;
			}
		}
		int changes;
		do {
			changes = 0;
			for (Journey j: journeyForRequest) {
				int ready = j.legs[0].arrives + DEFAULT_WAIT;
				for (int i = 1; i < j.legs.length; i++) {
					if (j.legs[i].leaves < ready) {
						j.legs[i].arrives += ready - j.legs[i].leaves;
						j.legs[i].leaves = ready;
						changes++;
					}
					ready = j.legs[i].arrives + DEFAULT_WAIT;
				}
			}
		} while (changes > 0);
	}

	public void compact() {
		List<VehicleRoute> unused = new ArrayList<VehicleRoute>(routes.size());
		route_loop:
		for (VehicleRoute route: routes) {
            for (Leg leg: route.legs) {
            	if (leg.spareCapacity < VehicleRoute.CAPACITY) continue route_loop;
            }
            unused.add(route);
    	}
		routes.removeAll(unused);
	}

    public Schedule copy() {
    	DEFAULT_ROUTES = routes.size() + 10;
    	Schedule copy = new Schedule();
    	for (VehicleRoute route: routes) {
    		copy.routes.add(route.copy());
    	}
    	return copy;
    }

    @Override
    public String toString() {
    	String s = "";
//    	if (connections != null) {
//    		for (int i = 0; i < jcts.length; ++i) {
//    			for (int j = 0; j < jcts.length; ++j) {
//    				s += "From " + jcts[i].name + " to " + jcts[j].name + " ~[" + duration[i][j] + "," + maxDuration[i][j] + "]" + "\n";
//    				for (Journey journey: connections[i][j]) {
//    					s += journey;
//    				}
//    				s  += "\n";
//    			}
//    		}
//    	}
    	for (VehicleRoute route: routes) {
    		s += route;
    	}
    	return s;
    }
}
