// Luca Citi
// University of Essex
// Dec 2015 - Feb 2016
//
// (0) Public domain
// To the extent possible under law, Luca Citi (lciti@ieee.org) has waived all
// copyright and related or neighboring rights to ga_scheduler.
// This work is published from: United Kingdom.

package ga_scheduler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Problem {
    protected Map<String,Junction> junctionsMap = new HashMap<String,Junction>();
    protected List<Request> requests = new ArrayList<Request>();
    protected Junction[] junctions;
    
    public void readJunctions(String fname_names, String fname_times) {
        BufferedReader br;
        try {
            String line;
            int id = 0;
            List<Junction> junctions = new ArrayList<Junction>();
            br = new BufferedReader(new FileReader(fname_names));
            while ((line = br.readLine()) != null) {
            	Junction jct = new Junction(id++, line.trim());
            	junctions.add(jct);
                junctionsMap.put(line.trim(), jct);
            }
            this.junctions = junctions.toArray(new Junction[junctions.size()]);
            br = new BufferedReader(new FileReader(fname_times));
            for (Junction i : this.junctions) {
                Iterator<String> times = Arrays.asList(br.readLine().split(",")).iterator();
                for (Junction j : this.junctions) {
                    i.setTimeTo(j, Integer.parseInt(times.next().trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void readRequests(String fname) {
        BufferedReader br;
        try {
            String line;
            int id = 0;
            br = new BufferedReader(new FileReader(fname));
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                requests.add(new Request(id++, junctionsMap.get(fields[0]), junctionsMap.get(fields[1]),
                             Integer.parseInt(fields[2]), Integer.parseInt(fields[3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
