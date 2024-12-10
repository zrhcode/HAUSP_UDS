import java.util.ArrayList;
import java.util.List;
/**
 *
 * @Copyright(C): 2024, North Minzu University
 * @Description:
 * @ClassName：SequenceList
 * @Date：2024/4/12
 * @Author：zrh
 */
public class SequenceList {

    /** the itemsets in this sequence */
    List<List<Integer>> itemsets = new ArrayList<List<Integer>>();

    /** the list of elements */
    List<Element> elements = new ArrayList<Element>();

    /** the sum of the utility */
    int sumUtility = 0;

    /** the sum of probability */
    float sumProbability = 0;

    /** the sum of SWU */
    int sumUB = 0;


    public List<Element> getElements(){
        return this.elements;
    }

    void addElement(int WID,int BID,int SID, int position, int utility,  int ru,float pro){
        Element element = new Element(WID,BID,SID, position, utility, ru,pro);
        this.elements.add(element);
    }

    void addElement(Element element){
        this.elements.add(element);
    }

    void addItemset(List<Integer> itemset){
        this.itemsets.add(itemset);
    }

    void addItemsetList(List<List<Integer>> itemsets){
        for(List<Integer> itemset : itemsets){
            List<Integer> newItemset = new ArrayList<Integer>();
            this.itemsets.add(itemset);
        }
    }

    void itemBasedExtend( SequenceList pattern, int item, List<List<Itemset>> sequenceDatabase){
        for(Element element : pattern.elements){
            itemBasedAddElement(element, item, sequenceDatabase);
        }
    }

    void itemsetBasedExtend( SequenceList pattern, int item, List<List<Itemset>> sequenceDatabase){
        for(Element element : pattern.elements){
            itemsetBasedAddElement(element, item, sequenceDatabase);
        }
    }

    private void itemBasedAddElement(Element element, int item, List<List<Itemset>> sequnceDatabase){
        //item-based add element
        int WID = element.WID;
        int BID = element.BID;
        int SID = element.SID;
        int location = element.position;
        int utility = element.utility;
        int ru = element.remaining;
        float probability=element.probability;

        int size = this.elements.size()-1;

        for(Item Item: sequnceDatabase.get(element.SID).get(element.position).Itemset){
            if(item == Item.item){
                utility += Item.utility;

                if(size >= 0 && SID == this.elements.get(size).SID && location == this.elements.get(size).position){
                    this.elements.get(size).utility = Integer.max(this.elements.get(size).utility, utility);
                }else {
                    Element newElement = new Element(WID,BID,SID, location, utility, ru,probability);
                    this.elements.add(newElement);
                }
                break;
            }
        }
    }
    private void itemsetBasedAddElement(Element element, int item, List<List<Itemset>> sequnceDatabase){
        //itemset-based add element
        int WID = element.WID;
        int BID = element.BID;
        int SID = element.SID;
        int size = this.elements.size()-1;
        int ru = element.remaining;

        for(int i = element.position + 1; i<sequnceDatabase.get(SID).size(); i++){
            int utility = element.utility;
            float probability = element.probability;

            for(Item Item: sequnceDatabase.get(element.SID).get(i).Itemset){
                if(item == Item.item){
                    utility += Item.utility;

                    if(size >= 0 && SID == this.elements.get(size).SID && i == this.elements.get(size).position ){
                        this.elements.get(size).utility = Integer.max(this.elements.get(size).utility, utility);

                    }else {
                        Element newElement = new Element(WID,BID,SID, i, utility, ru,probability);
                        this.elements.add(newElement);
                    }
                    break;
                }
            }
        }
    }

    void calculate(){

        int order = 0;
        int orderUtility = 0;
        float orderProbability=0;
        int orderPEU = 0;

        if( !this.elements.isEmpty()){
            order = this.elements.get(0).SID;
            orderUtility = this.elements.get(0).utility;
            orderProbability = this.elements.get(0).probability;
            orderPEU = this.elements.get(0).remaining + this.elements.get(0).utility;
        }
        for( Element element : this.elements){

            if(element.SID ==order){

                if(element.utility > orderUtility){
                    orderUtility = element.utility;
                }
                if (element.probability>orderProbability){
                    orderProbability = element.probability;
                }
                if(element.remaining + element.utility > orderPEU){
                    orderPEU = element.remaining + element.utility;
                }
            }else {
                this.sumUtility +=orderUtility;
                this.sumProbability+=orderProbability;
                this.sumUB += orderPEU;

                order = element.SID;
                orderUtility = element.utility;
                orderProbability = element.probability;
                orderPEU = element.remaining + element.utility;
            }
        }

        this.sumUtility +=orderUtility;
        this.sumProbability+=orderProbability;
        this.sumUB += orderPEU;
    }
}
