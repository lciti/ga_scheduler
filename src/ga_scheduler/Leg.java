// Luca Citi
// University of Essex
// Dec 2015 - Feb 2016
//
// (0) Public domain
// To the extent possible under law, Luca Citi (lciti@ieee.org) has waived all
// copyright and related or neighboring rights to ga_scheduler.
// This work is published from: United Kingdom.

package ga_scheduler;

public class Leg {
	protected Junction from, to;
	protected int leaves, arrives;
	protected int spareCapacity;
	protected VehicleRoute route;

    public Leg() {
    }

    public Leg(Junction from, Junction to, int leaves,
			int spare_capacity, VehicleRoute route) {
		this.from = from;
		this.to = to;
		this.leaves = leaves;
		this.arrives = leaves + from.timeTo(to);
		this.spareCapacity = spare_capacity;
		this.route = route;
	}

    @Override
    public String toString() {
        return from + "@" + leaves + "->" + to + "@" + arrives + " h" + spareCapacity;
    }
}
