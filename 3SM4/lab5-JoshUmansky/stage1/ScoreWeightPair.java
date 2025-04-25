package stage1;

public class ScoreWeightPair {
    
    private int score;
    private int weight;

    public ScoreWeightPair(int sc, int wg)
    {
        score = sc;
        weight = wg;
    }

    public void setScore(int sc) { score = sc; }
    public void setWeight(int wg) { weight = wg; }

    public int getScore() { return score; }
    public int getWeight() { return weight; }
}
