public class HandsBSTNode
{
    Hands myHand;

    HandsBSTNode left;
    HandsBSTNode right;

    public HandsBSTNode(Hands inputHand) 
    {
        this.myHand = inputHand;
        this.left = null;
        this.right = null;
    }
}
