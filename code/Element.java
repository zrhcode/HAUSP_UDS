

/**
 *
 * @Copyright(C): 2024, North Minzu University
 * @Description:
 * @className：Element
 * @Date：2024/4/12
 * @Author：zrh
 */
public class Element {

    /*window identifier*/
    int WID;

    /*batch identidier*/
    int BID;

    /** sequence identifier */
    int SID;

    /** position in the sequence */
    int position;

    /** utility */
    int utility;

    /** remaining utility*/
    int remaining;

    /*probability*/
    float probability;


    //Constructor
    public Element(int SID, int position, int utility, int remaining) {
        this.SID = SID;
        this.position = position;
        this.utility = utility;
        this.remaining = remaining;
    }

    public Element(int BID, int SID, int position, int utility, int remaining) {
        this.BID = BID;
        this.SID = SID;
        this.position = position;
        this.utility = utility;
        this.remaining = remaining;
    }

    public Element(int WID, int BID, int SID, int position, int utility, int remaining) {
        this.WID = WID;
        this.BID = BID;
        this.SID = SID;
        this.position = position;
        this.utility = utility;
        this.remaining = remaining;
    }

    public Element(int WID, int BID, int SID, int position, int utility, int remaining, float probability) {
        this.WID = WID;
        this.BID = BID;
        this.SID = SID;
        this.position = position;
        this.utility = utility;
        this.remaining = remaining;
        this.probability = probability;
    }
}
