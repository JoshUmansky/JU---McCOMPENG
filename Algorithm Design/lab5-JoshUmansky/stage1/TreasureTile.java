package stage1;

import java.util.Random;
import stage2.Tile;

public class TreasureTile extends Tile {

    public static final int RANDRANGE = 2000;
    
    private ScoreWeightPair dataPair;
    private boolean splittable;

    // TileType for Treasure in Stage 1:
    // +    basic treasure [2 - 10]
    // $    high treasure [10 - 30]
    // ?    mystery treasure [20 - 50]    

    public TreasureTile(int x, int y, char type, boolean split)
    {
        super(x, y, type, 0);
        Random rand = new Random();
        int score, weight;
        char tiletype = type;
        
        splittable = split;

        switch(type)
        {
            default:
            case '+':
                score = rand.nextInt(RANDRANGE) % 9 + 2; // [2 - 10]
                weight = rand.nextInt(RANDRANGE) % 5 + 1; // [1 - 5]
                dataPair = new ScoreWeightPair(score, weight);
                break;

            case '$':
                score = rand.nextInt(RANDRANGE) % 21 + 10; // [10 - 30]
                weight = rand.nextInt(RANDRANGE) % 10 + 1; // [1 - 10]
                dataPair = new ScoreWeightPair(score, weight);
                break;

            case '?':
                score = rand.nextInt(RANDRANGE) % 31 + 20; // [20 - 50]
                weight = rand.nextInt(RANDRANGE) % 15 + 1; // [1 - 15]
                dataPair = new ScoreWeightPair(score, weight);
                break;
        }
    }

    public void printTreasureTile()
    {
        System.out.printf("%c%d/%d", super.getTileType(), dataPair.getScore(), dataPair.getWeight());
    }

    public int getScore()
    {
        return dataPair.getScore();
    }

    public int getWeight()
    {
        return dataPair.getWeight();
    }
    
    public boolean isSplittable()
    {
        return splittable;
    }

    public char getTileType()
    {
        return tileType;
    }
    
    public ScoreWeightPair getDataPair()
    {
        return dataPair;
    }

    public ScoreWeightPair consumeTreasure(int weightTaken)
    {
        int score = dataPair.getScore();
        int weight = dataPair.getWeight();
        ScoreWeightPair consumedPair;

        if(splittable)
        {
            if(weightTaken < 0)        return new ScoreWeightPair(0,0);
            if(weightTaken > weight)   weightTaken = weight;

            consumedPair = new ScoreWeightPair(score * weightTaken / weight, weightTaken);            
            score = score * (weight - weightTaken) / weight;
            weight -= weightTaken;
            dataPair.setScore(score);
            dataPair.setWeight(weight);
            return consumedPair;
        }
        else
        {
            consumedPair = new ScoreWeightPair(score, weight);
            dataPair.setScore(0);
            dataPair.setWeight(0);
            return consumedPair;            
        }
    }

}
