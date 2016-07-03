// Luca Citi
// University of Essex
// Dec 2015 - Feb 2016
//
// (0) Public domain
// To the extent possible under law, Luca Citi (lciti@ieee.org) has waived all
// copyright and related or neighboring rights to ga_scheduler.
// This work is published from: United Kingdom.

package ga_scheduler;

public class Request {
	protected int id;
	protected Junction from;
	protected Junction to;
	protected int party;
	protected int leaveAfter;

    public Request(int id, Junction from, Junction to, int party, int leaveAfter) {
    	this.id = id;
        this.from = from;
        this.to = to;
        this.party = party;
        this.leaveAfter = leaveAfter;
    };

    @Override
    public String toString() {
    	return from + "->" + to + "|" + party + "@" + leaveAfter + " id=" + id + "\n";
    }
}
