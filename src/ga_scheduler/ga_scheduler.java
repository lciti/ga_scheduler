// Luca Citi
// University of Essex
// Dec 2015 - Feb 2016
//
// (0) Public domain
// To the extent possible under law, Luca Citi (lciti@ieee.org) has waived all
// copyright and related or neighboring rights to ga_scheduler.
// This work is published from: United Kingdom.

package ga_scheduler;

public class ga_scheduler {
    public static void main(String[] args) {
        Problem problem = new Problem();
        problem.readJunctions("test_cases/challenge4_names.txt", "test_cases/challenge4_times.txt");
        //System.out.println(problem.junctions);
        for(Junction j : problem.junctionsMap.values()) {
            System.out.println(j.name);
            System.out.println(j.time_to);
        }
        problem.readRequests("test_cases/challenge4_requests.txt");
        
        
        problem.requests = problem.requests.subList(0, 10);///////////////////////
        
        
        for(Request r : problem.requests) {
            System.out.print(r);
        }
        
        GA ga = new GA();
        ga.init(problem.requests, problem.junctions, 1, 100000);
        ga.evolve(50);
        Schedule sc = ga.population[ga.best].schedule;
        
        for(Request r : problem.requests) {
            System.out.print(r);
            System.out.println(sc.journeyForRequest[r.id]);
        }
        System.out.println("DONE.");
    }

}
