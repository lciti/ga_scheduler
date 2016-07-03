// Luca Citi
// University of Essex
// Dec 2015 - Feb 2016
//
// (0) Public domain
// To the extent possible under law, Luca Citi (lciti@ieee.org) has waived all
// copyright and related or neighboring rights to ga_scheduler.
// This work is published from: United Kingdom.

package ga_scheduler;

import java.util.HashMap;
import java.util.Map;

public class Junction {
	protected String name;
    protected int id = -1;
    protected Map<Junction, Integer> time_to = new HashMap<Junction, Integer>();

    public Junction(int id, String name) {
    	this.id = id;
        this.name = name;
    }

    public void setTimeTo(Junction to, int time) {
        time_to.put(to, time);
    }

    public int timeTo(Junction to) {
        return time_to.get(to);
    }

    @Override
    public String toString() {
        return name;
    }
}
