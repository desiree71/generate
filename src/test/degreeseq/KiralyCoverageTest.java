package test.degreeseq;

import generate.handler.IsomorphCountingHandler;
import graph.model.Graph;
import graph.model.GraphFileReader;
import graph.model.GraphSignature;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import degreeseq.KiralyHHGenerator;

public class KiralyCoverageTest {
    
    public void testFile(String filename) throws FileNotFoundException {
        Map<String, List<Graph>> degreeSeqMap = new HashMap<String, List<Graph>>(); 
        
        // read the graphs generated by McKay, and group them by degree sequence
        GraphFileReader reader = new GraphFileReader(new FileReader(filename));
        for (Graph g : reader) {
            int[] degSeq = g.degreeSequence(true);
            String key = Arrays.toString(degSeq);
            List<Graph> graphsWithDegSeq;
            if (degreeSeqMap.containsKey(key)) {
                graphsWithDegSeq = degreeSeqMap.get(key);
            } else {
                graphsWithDegSeq = new ArrayList<Graph>();
                degreeSeqMap.put(key, graphsWithDegSeq);
            }
            graphsWithDegSeq.add(g);
        }
        
        // use the degree sequences to re-generate the graphs using KiralyHH
        Map<int[], List<Graph>> missingDegreeSeqMap = new HashMap<int[], List<Graph>>();
        IsomorphCountingHandler duplicateHandler = new IsomorphCountingHandler(true);
        KiralyHHGenerator generator = new KiralyHHGenerator(duplicateHandler);
        for (String degreeSequenceString : degreeSeqMap.keySet()) {
            int[] degreeSequence = parse(degreeSequenceString);
            generator.generate(degreeSequence);
            List<Graph> kiralySet = duplicateHandler.getNonIsomorphicGraphs();
            List<Graph> mckaySet = degreeSeqMap.get(degreeSequenceString);
            if (kiralySet.size() == mckaySet.size()) {
                System.out.println(Arrays.toString(degreeSequence) + " PASS " + kiralySet.size() + "\t" + mckaySet.size());
            } else {
                System.out.println(Arrays.toString(degreeSequence) + " FAIL " + kiralySet.size() + "\t" + mckaySet.size());
                missingDegreeSeqMap.put(degreeSequence, diff(kiralySet, mckaySet));
            }
        }
        
        int count = 0;
        for (int[] degSeq : missingDegreeSeqMap.keySet()) {
            List<Graph> diff = missingDegreeSeqMap.get(degSeq);
            for (Graph g : diff) {
                System.out.println(count + "\tMISSING : " + Arrays.toString(degSeq) + "\t" + g);
                count++;
            }
        }
    }
    
    private int[] parse(String degreeSequenceString) {
        int end = degreeSequenceString.indexOf("]");
        String[] bits = degreeSequenceString.substring(1, end).split(","); 
        int[] degSeq = new int[bits.length];
        int i = 0;
        for (String bit : bits) {
            degSeq[i] = Integer.parseInt(bit.trim());
            i++;
        }
        return degSeq;
    }

    public List<Graph> diff(List<Graph> a, List<Graph> b) {
        // XXX - TODO, properly - perhaps move to GraphFileDiff?
        List<Graph> diffAB = new ArrayList<Graph>();
        Map<Graph, String> bMap = new HashMap<Graph, String>();
        for (Graph gA : a) {
            GraphSignature sigA = new GraphSignature(gA);
            String certA = sigA.toCanonicalString();
            if (bMap.containsValue(certA)) {
                continue;
            } else {
                for (Graph gB : b) {
                    GraphSignature sigB = new GraphSignature(gB);
                    String certB = sigB.toCanonicalString();
                    bMap.put(gB, certB);
                }
                if (bMap.containsValue(certA)) {
                    continue;
                } else {
                    diffAB.add(gA);
                }
            }
        }
        return diffAB;
    }
    
    @Test
    public void testSevens() throws FileNotFoundException {
        testFile("output/mckay/seven_x.txt");
    }
    
    @Test
    public void testSixes() throws FileNotFoundException {
        testFile("output/mckay/six_x.txt");
    }
    
    @Test
    public void testFives() throws FileNotFoundException {
        testFile("output/mckay/five_x.txt");
    }
    
}
