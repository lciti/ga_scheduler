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
import java.util.List;
import java.util.Random;

public class GA {
	protected Individual[] population;
	protected double[] fitness;
	int best;
	double CROSSOVER_PROB = .5;
	double CROSSOVER_SCH = .9;
	double MUTATION_SCH = .5;
	int TRIM_SIZE = 4;
	int NUM_HUB_ROUTES = 1;
	int T_SIZE = 3;
	protected double WEIGHT_TRAVEL = 1./60.;
	protected double WEIGHT_DRIVER = 1./60.;
	protected double WEIGHT_PASSENGER = 0.1 * 1./60.;
	protected double WEIGHT_FLEET = 120;
    protected Random rd;
	
	void init(Collection<Request> requests, Junction[] jcts, long seed, int popSize) {
		rd = new Random();
		if (seed > 0) rd.setSeed(seed); 
		Schedule.rd = rd;
		Individual.rd = rd;

		population = new Individual[popSize];
		fitness = new double[popSize];
		for (int i = 0; i < popSize; i++) {
			List<Request> r = new ArrayList<Request>(requests);
			Collections.shuffle(r, rd);
			population[i] = new Individual();
			population[i].requests = r;
			population[i].schedule = new Schedule(jcts);
			fitness[i] = population[i].getFitness(WEIGHT_TRAVEL, WEIGHT_DRIVER, WEIGHT_PASSENGER, WEIGHT_FLEET);
		}
	}

    void evolve(int generations) {
        stats(0);
        for (int gen = 1; gen < generations; gen++ ) {
            for (int indivs = 0; indivs < population.length; indivs+=2) {
                Individual[] newind;
                int parent1 = tournament(+1, T_SIZE);
                int parent2 = tournament(+1, T_SIZE);
                if (rd.nextDouble() < CROSSOVER_PROB) {
                    if (rd.nextDouble() < CROSSOVER_SCH) {
                    	newind = population[parent1].crossoverSchedule(population[parent2]);
                    } else {
                    	newind = population[parent1].crossoverRequests(population[parent2]);
                    }
                } else {
                	newind = new Individual[2];
                	if (rd.nextDouble() < MUTATION_SCH) {
                		newind[0] = population[parent1].mutateScheduleHubs(NUM_HUB_ROUTES);
                		newind[1] = population[parent2].mutateScheduleTrim(TRIM_SIZE);
                    } else {
                    	newind[0] = population[parent1].mutateRequests(1);
                    	newind[1] = population[parent2].mutateRequests(1);
                    }
                }
                int offspring1 = tournament(-1, T_SIZE);
                int offspring2 = tournament(-1, T_SIZE);
                population[offspring1] = newind[0];
                population[offspring2] = newind[1];
                fitness[offspring1] = newind[0].getFitness(WEIGHT_TRAVEL, WEIGHT_DRIVER, WEIGHT_PASSENGER, WEIGHT_FLEET);
                fitness[offspring2] = newind[1].getFitness(WEIGHT_TRAVEL, WEIGHT_DRIVER, WEIGHT_PASSENGER, WEIGHT_FLEET);
            }
            stats(gen);
        }
    }

	public void stats(int gen) {
		int len = fitness.length;
		double[] sorted = new double[len];
	    System.arraycopy(fitness, 0, sorted, 0, len);
		Arrays.sort(sorted);
		double mean = 0, max = Double.NEGATIVE_INFINITY;
		best = -1;
		for (int i = 0; i < len; i++) {
            if (fitness[i] > max) {
            	max = fitness[best = i];
	        }
            mean += fitness[i];
		}
		mean /= len;
		//for (int i = 0; i < len; i++) System.out.print(fitness[i] + ",");
		System.out.println("\nBest:\n" + population[best].schedule);
		System.out.format("\nGen:%4d Max:%.5g Mean:%.5g 1q:%.5g Median:%.5g 3q:%.5g\n\n\n", gen, max, mean,
				sorted[(int)Math.round(.25*len)], sorted[(int)Math.round(.5*len)], sorted[(int)Math.round(.75*len)]);
	}

	private int tournament(double sign, int tournamentSize) {
		double max = Double.NEGATIVE_INFINITY;
		int argMax = -1;
		for (int i = 0; i < tournamentSize; i++) {
            int test = rd.nextInt(population.length);
            if (sign*fitness[test] > max) {
            	max = sign*fitness[argMax = test];
	        }
		}
		return argMax;
	}

}
