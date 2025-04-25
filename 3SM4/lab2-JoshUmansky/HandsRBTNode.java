public class HandsRBTNode
{
    Hands myHand;

    HandsRBTNode left;
    HandsRBTNode right;
    HandsRBTNode parent;

    boolean colour;  // true = black, false = red

    public HandsRBTNode(Hands inputHand) 
    {
        this.myHand = inputHand;
    }
}
