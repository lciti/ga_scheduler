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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class Individual {
	Schedule schedule;
	List<Request> requests;
    static Random rd = new Random();
	
	public Individual() {
	}
	
	public Individual(Collection<Request> requests) {
		this.requests = new ArrayList<Request>(requests);
	}
	
	protected Individual[] crossoverRequests(Individual other) {
		LinkedHashSet<Request> newRequests = new LinkedHashSet<Request>(requests.size());
		Iterator<Request> r1 = requests.iterator();
		Iterator<Request> r2 = other.requests.iterator();
		while (newRequests.size() < requests.size()) {
			newRequests.add(r1.next());
			newRequests.add(r2.next());
		}
		Individual offspring1 = new Individual();
		offspring1.requests = new ArrayList<Request>(newRequests);
		offspring1.schedule = schedule.copy();
		Individual offspring2 = new Individual();
		offspring2.requests = offspring1.requests;
		offspring2.schedule = other.schedule.copy();
		return new Individual[] {offspring1, offspring2};
	}

protected Individual[] crossoverSchedule(Individual other) {
	Individual offspring1 = new Individual();
	offspring1.requests = requests;
	offspring1.schedule = schedule.copy();
	Individual offspring2 = new Individual();
	offspring2.requests = other.requests;
	offspring2.schedule = other.schedule.copy();
	int i = rd.nextInt(schedule.journeyForRequest.length);
	VehicleRoute r1 = schedule.journeyForRequest[i].legs[0].route;
	VehicleRoute r2 = other.schedule.journeyForRequest[i].legs[0].route;
	int r1i = schedule.routes.indexOf(r1);
	int r2i = other.schedule.routes.indexOf(r2);
	r1 = offspring1.schedule.routes.get(r1i);
	r2 = offspring2.schedule.routes.get(r2i);
	if (r1.isEqual(r2)) {
		r1.move(Schedule.DEFAULT_WAIT);
		r2.move(-Schedule.DEFAULT_WAIT);
	} else {
		offspring1.schedule.routes.set(r1i, r2);
		offspring2.schedule.routes.set(r2i, r1);
	}
	return new Individual[] {offspring1, offspring2};
}

	protected Individual mutateRequests(int drop) {
		List<Request> r = new ArrayList<Request>(requests);
		Collections.shuffle(r, rd);
		Individual offspring = new Individual();
		offspring.requests = r;
		offspring.schedule = schedule.copy();
		while (drop-- > 0) {
			offspring.schedule.routes.remove(rd.nextInt(offspring.schedule.routes.size()));
		}
		return offspring;
	}

	protected Individual mutateScheduleTrim(int minSize) {
		Individual offspring = new Individual();
		offspring.requests = requests;
		offspring.schedule = new Schedule();
		for (VehicleRoute r: schedule.routes) {
			VehicleRoute newRoute = null;
			for (Leg l: r.legs) {
				if (VehicleRoute.CAPACITY - l.spareCapacity > minSize) {
					if (newRoute == null) {
						newRoute = new VehicleRoute(l.from, l.to, l.leaves);
					} else {
						newRoute.add(l.to, l.leaves - newRoute.last.arrives);
					}
				} else if (newRoute != null) {
					offspring.schedule.add(newRoute);
					newRoute = null;
				}
			}
			if (newRoute != null) {
				offspring.schedule.add(newRoute);
			}
		}
		return offspring;
	}

	protected Individual mutateScheduleHubs(int numRoutes) {
		double[] junctionCounts = new double[Schedule.jcts.length];
		for (int i = 1; i < junctionCounts.length; i++) {
			junctionCounts[i] = 2. * rd.nextDouble(); // priority to 0 and 1 connection
		}
		for (Journey j: schedule.journeyForRequest) {
			junctionCounts[j.legs[0].from.id]++;
			for (Leg l: j.legs) {
				junctionCounts[l.to.id]++;
			}
		}
		int argMin = 0;
		for (int attempts = 0; attempts < 10; attempts++) {
			double min = junctionCounts[0];
			for (int i = 1; i < junctionCounts.length; i++) {
				if (junctionCounts[i] < min) {
					min = junctionCounts[argMin = i];
				}
			}
			if (rd.nextDouble() < .5) break;
			junctionCounts[argMin] += 1e9;
		}
		Individual offspring = new Individual();
		offspring.requests = requests;
		offspring.schedule = new Schedule();
		boolean flag = false;
    	for (VehicleRoute route: schedule.routes) {
    		if (flag = !flag) { // add every other route
    			offspring.schedule.routes.add(route.copy());
    		}
    	}
		Junction hub = Schedule.jcts[argMin];
		for (Junction j: Schedule.jcts) {
			for (int i = 0; i < numRoutes; i++) {
				Junction j2 = Schedule.jcts[rd.nextInt(Schedule.jcts.length)];
				int duration = Schedule.duration[j.id][hub.id] + Schedule.duration[hub.id][j2.id]; 
		    	VehicleRoute route = new VehicleRoute(j, hub, Schedule.randomTime(duration));
		    	route.add(j2, Schedule.DEFAULT_WAIT);
				offspring.schedule.add(route);
			}
		}
		return offspring;
	}
	
	public double getFitness(double weightTravelTime, double weightDriverTime,
							 double weightPassengerTime, double weightFleetSize) {
		schedule.createMatrices();
		schedule.accommodateRequests(requests);
		schedule.compactAndTrim();
		int travelTime = 0, driverTime = 0, passengerTime = 0;
		for (VehicleRoute r: schedule.routes) {
			int[] time = r.computeTravelAndDriverTime();
			travelTime += time[0];
			driverTime += time[1];
		}
		for (Journey j: schedule.journeyForRequest) {
			passengerTime += j.duration;
		}
		return -(weightTravelTime * travelTime + weightDriverTime * driverTime
				 + weightPassengerTime * passengerTime
				 + weightFleetSize * schedule.routes.size());
	}

}
