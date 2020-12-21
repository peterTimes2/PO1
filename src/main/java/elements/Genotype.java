package elements;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Genotype {
    private static final int genotypeSize = 32;
    protected final List<Integer> genes;

    public Genotype() {
        this.genes = new ArrayList<>(genotypeSize);
        for(int i = 0; i < genotypeSize; i ++) {
            int random = (int) (Math.random() * 8);
            genes.add(random);
        }
        genes.sort(Integer::compareTo);
    }

    public Genotype(Genotype a, Genotype b) {
        this.genes = new ArrayList<>(genotypeSize);
        Genotype[] genotypes = new Genotype[] {a, b};
        int[] cutPoints = drawCutPoints();
        int[] parents = drawParents();
        for (int i = 0; i < 3; i++) {
            List<Integer> geneSlice = genotypes[parents[i]].genes.subList(cutPoints[i], cutPoints[i + 1]);
            genes.addAll(geneSlice);
        }
        genes.sort(Integer::compareTo);
        restoreExtinctGenes();
    }

    private int[] drawParents() {
        int[] parents = new int[3];
        for(int i = 0; i < 3; i++) {
            parents[i] = (int)(Math.random() * 2);
        }
        if (parents[0] + parents[1] == 2) {
            parents[2] = 0;
        }
        if (parents[0] + parents[1] == 0) {
            parents[2] = 1;
        }
        return parents;
    }

    private int[] drawCutPoints() {
        int[] cutPoints = new int[] {0, 0, 0, genotypeSize};
        cutPoints[1] = (int) (Math.random() * (genotypeSize - 1)) + 1;
        cutPoints[2] = (int) (Math.random() * (genotypeSize - 1)) + 1;
        while(cutPoints[1] == cutPoints[2]) {
            cutPoints[2] = (int) (Math.random() * 31) + 1;
        }
        return Arrays.stream(cutPoints).sorted().toArray();
    }

    private void restoreExtinctGenes() {
        int[] genesCount = {0, 0, 0, 0, 0, 0, 0, 0};
        for (Integer i: genes) {
            genesCount[i]++;
        }
        for (int i = 0; i < 8; i++) {
            if(genesCount[i] == 0) {
                int randomPosition = (int)(Math.random() * 32);
                while (genesCount[genes.get(randomPosition)] == 0) {
                    randomPosition = (int)(Math.random() * 32);
                }
                genes.set(randomPosition, i);
            }
        }
    }
    public int getMostFrequentGene() {
        int mostFrequent = 0;
        int mostFrequentGeneCount = 0;
        int currentGeneCount = 0;
        int previousGene = 0;
        for (int i: genes) {
            if (i == previousGene) {
                currentGeneCount++;
            } else {
                if (currentGeneCount > mostFrequentGeneCount) {
                    mostFrequent = previousGene;
                    mostFrequentGeneCount = currentGeneCount;
                }
                currentGeneCount = 1;
                previousGene = i;
            }
        }
        return mostFrequent;
    }

    public int getRandomDirection() {
        return this.genes.get((int)(Math.random() * 32));
    }
}
