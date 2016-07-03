// Luca Citi
// University of Essex
// Dec 2015 - Feb 2016
//
// (0) Public domain
// To the extent possible under law, Luca Citi (lciti@ieee.org) has waived all
// copyright and related or neighboring rights to ga_scheduler.
// This work is published from: United Kingdom.

package ga_scheduler;

import java.util.Arrays;


public class Journey implements Comparable<Journey> {
	protected Leg[] legs;
	protected int leaves, arrives;
	protected int duration;
	
	public Journey(Leg[] legs) {
		this.legs = legs;
		leaves = legs[0].leaves;
		arrives = legs[legs.length-1].arrives;
		duration = arrives - leaves;
	}
	
	public Journey join(Journey j2) {
		Leg[] joined = Arrays.copyOf(legs, legs.length + j2.legs.length);
	    System.arraycopy(j2.legs, 0, joined, legs.length, j2.legs.length);
	    return new Journey(joined);
	}

	public int getSpareCapacity() {
		int spareCapacity = Integer.MAX_VALUE;
        for (Leg leg: legs) {
        	if (leg.spareCapacity < spareCapacity) spareCapacity = leg.spareCapacity;
        }
        return spareCapacity;
	}

	public boolean canAccommodate(int party) {
        for (Leg leg: legs) {
        	if (leg.spareCapacity < party) return false;
        }
        return true;
	}

	public void addPassengers(Request request) {
        for (Leg leg: legs) {
        	leg.spareCapacity -= request.party;
        }
	}

	@Override
	public int compareTo(Journey o) {
		// assumes from==o.from and to==o.to
		return duration != o.duration ? (duration - o.duration) : (legs.length - o.legs.length);
	}

	@Override
    public String toString() {
        String s = "Journey " + this.hashCode() + " ~" + duration + "\n";
        VehicleRoute route = legs[0].route;
        for (Leg leg: legs) {
        	if (route != leg.route) {
        		route = leg.route;
        		s += "    &\n";
        	}
            s += "    " + leg + "\n";
        }
        return s + "\n";
    }
}
