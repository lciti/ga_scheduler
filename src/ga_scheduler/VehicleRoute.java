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
import java.util.Iterator;
import java.util.List;

public class VehicleRoute {
    protected List<Leg> legs = new ArrayList<Leg>();
    protected Leg first, last;
    static int CAPACITY = 16;
    static int MAX_ROUTE_DURATION = 720;

    public VehicleRoute() {
	}

    public VehicleRoute(Junction from, Junction to, int leaves) {
    	legs.add(first = last = new Leg(from, to, leaves, CAPACITY, this));
    }

	public boolean add(Junction to, int after) {
    	if (last == null) {
    		last = new Leg(to, to, after, CAPACITY, this);
            return true;
    	}
		Leg leg = new Leg(last.to, to, last.arrives + after, CAPACITY, this);
		if (first == null) first = leg;
		if (leg.arrives - first.leaves <= MAX_ROUTE_DURATION) {
            return legs.add(last = leg);
		}
    	return false;
    }

    public boolean prepend(Junction from, int wait) {
		Leg leg = new Leg(from, first.from, first.leaves - wait - from.timeTo(first.from), CAPACITY, this);
		if (last.arrives - leg.leaves <= MAX_ROUTE_DURATION) {
			legs.add(0, first = leg);
            return true;
		}
    	return false;
    }

    public void move(int amount) {
    	for (Leg l: legs) {
    		l.leaves += amount;
    		l.arrives += amount;
    	}
    }

    public boolean trim() {
		while (last.spareCapacity == CAPACITY) {
			legs.remove(legs.size() - 1);
			if (legs.isEmpty()) {
				first = last = null;
				return false;
			}
			last = legs.get(legs.size() - 1);
		}
		while (first.spareCapacity == CAPACITY) {
			legs.remove(0);
			first = legs.get(0);
		}
		return true;
    }

    public Leg get(int i) {
    	return legs.get(i);
    }

    public int[] computeTravelAndDriverTime() {
    	int closeLoopTime = last.to.timeTo(first.from);
		int driverTime = last.arrives + closeLoopTime - first.leaves;
		int travelTime = closeLoopTime;
		for (Leg l: legs) {
			travelTime += l.arrives - l.leaves;
		}
		return new int[] {travelTime, driverTime};
	}

    public VehicleRoute copy() {
    	VehicleRoute copy = new VehicleRoute();
    	for (Leg leg: legs) {
    		Leg legCopy = new Leg();
    		legCopy.from = leg.from;
    		legCopy.to = leg.to;
    		legCopy.leaves = leg.leaves;
    		legCopy.arrives = leg.arrives;
    		legCopy.spareCapacity = CAPACITY;
    		legCopy.route = copy;
    		copy.legs.add(legCopy);
    	}
		copy.first = copy.legs.get(0);
		copy.last = copy.legs.get(copy.legs.size()-1);
    	return copy;
    }

    public boolean isEqual(VehicleRoute other) {
    	if (legs.size() != other.legs.size()) return false;
    	Iterator<Leg> iter = other.legs.iterator();
    	for (Leg l: legs) {
    		Leg l2 = iter.next();
    		if ((l.from != l2.from) || (l.to != l2.to) || (l.leaves != l2.leaves)) return false;
    	}
    	return true;
    }

    @Override
    public String toString() {
        String s = "Route " + this.hashCode() + "\n";
        for (Leg leg: legs) {
            s += "    " + leg + "\n";
        }
        return s + "\n";
    }

}
