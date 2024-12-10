import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @Copyright(C): 2024, North Minzu University
 * @Description:
 * @ClassName：AlgoHAUSP_UDS
 * @Date：2024/4/12
 * @Author：zrh
 */

/*
 * 240415
 * 不确定数据中的概率是否随时间的变化而衰减？
 * 本算法假定序列的不确定性概率不随时间的推移而改变，不对其进行衰减处理。
 *
 * */

public class AlgoHAUSP_UDS {

    /*variable for debug mode*/
    boolean debug = true;

    /*algo start timestamp and end timestamp*/
    public long startTimestamp = 0, endTimestamp = 0;

    /*the number of HAUSPs*/
    public int hauspCount = 0;

    /*the number of cadidates generated*/
    public int candidatesCount = 0;

    public int curWindowCandidatesCount = 0;

    /*the window start timestamp and end timestamp*/
    long winStartTimeStamp = 0, winEndTimeStamp = 0;

    /*the path of input file*/
    public String input;

    /*writer to write the output file*/
    BufferedWriter writer = null;

    /*if true, save result to file in a format that is easier to read by humans*/
    boolean SAVE_RESULT_EASIER_TO_READ_FORMAT = false;

    /*the size of window and batch*/
    public int winSize, batchSize;

    /*the output file path*/
    public String output;

    /*the minAverageUtility threshold*/
    public int minAUtil;

    public float minPro;

    static ArrayList<ArrayList<String>> window = new ArrayList<>();

    int BID = 0;

    int WID = 0;

    boolean isWriteToFile = true;

    int winNumber;
    int batchNumber;


    /*Default constructor*/
    public AlgoHAUSP_UDS() {
    }


    /**
     * Run the algo
     *
     * @param input     the input file path
     * @param winSize   the number of batches in a window
     * @param batchSize the number of transactions in a batch
     * @param minAUtil  the minmum average-utility threshold
     * @param output    the output file path
     * @param A         the coefficient of exponential decay
     * @param n         the power of exponential decay
     */
    public void runAlgorithm(String input, int winSize, int batchSize, int minAUtil, float minPro, String output, double A, double n) throws IOException {

        //reset time and memory
        startTimestamp = System.currentTimeMillis();
        MemoryLogger.getInstance().reset();

        // create a writer object to write results to file
        writer = new BufferedWriter(new FileWriter(output));

        int curbatchPatternCount;
        int calNextBatchPatternCount = 0;

        this.output = output;
        this.batchSize = batchSize;
        this.winSize = winSize;
        this.input = input;
        this.minAUtil = minAUtil;
        this.minPro = minPro;
        BufferedReader Input = null;
        String curline;
        ArrayList<String> batchSequence = new ArrayList<>();

        int seqCount = 0;     //Record the number of transactions in the data stream
        int transOfBat = 0, batsOfWin = 0, winNumber = 0, batchNumber = 0;
        try {
            Input = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(input).toPath())));
            int flag = 1;
            while ((curline = Input.readLine()) != null) {
                if (curline.isEmpty() || curline.charAt(0) == '#' || curline.charAt(0) == '%') {
                    continue;
                }
                seqCount++;
                transOfBat++;
                if (transOfBat <= batchSize) {
                    batchSequence.add(curline);
                }
                if (flag == 1) {
                    if (transOfBat == batchSize) {
                        transOfBat = 0;
                        batchNumber++;
                        window.add(new ArrayList<>(batchSequence));
                        batchSequence.clear();
                        batsOfWin++;

                        if (batsOfWin == winSize) {
                            winNumber++;
                            initial_call_HAUSP_Stream(window, winNumber, A, n);
                            calNextBatchPatternCount = hauspCount;

                            if (isWriteToFile) {
                                calNextBatchPatternCount = hauspCount;
                                writer.write("curBatch High Average-Utility Sequential Patterns Count：" + calNextBatchPatternCount);
                                writer.newLine();
                                writer.write("================================@NEXT_Batch=============================");
                                writer.newLine();
                            }
                            window.remove(0);       //将最旧批次移除窗口外，即窗口滑动
                            batsOfWin--;
                            flag = 0;
                        }
                    }
                } else {
                    if (transOfBat == batchSize) {
                        transOfBat = 0;
                        batchNumber++;
                        window.add(new ArrayList<>(batchSequence));
                        batchSequence.clear();
                        batsOfWin++;

                        if (batsOfWin == winSize) {
                            winNumber++;
                            update_call_HAUSP_Stream(window, winNumber, A, n);
                            curbatchPatternCount = hauspCount - calNextBatchPatternCount;
                            calNextBatchPatternCount += curbatchPatternCount;
                            if (isWriteToFile) {
                                writer.write("curBatch High Average-Utility Sequential Patterns Count：" + curbatchPatternCount);
                                writer.newLine();
                                writer.write("================================@NEXT_Batch=============================");
                                writer.newLine();
                            }
//                            if (window.size() == 2 * winSize) {
//                                window.remove(0);  //老师建议两个或三个窗口删一次，保证较高的recall
//                            }
                            window.remove(0);
                            batsOfWin--;
                        }
                    }
                }
            }
            if ((transOfBat > 0) && (transOfBat < batchSize)) {
                winNumber++;
                batchNumber++;
                window.add(new ArrayList<>(batchSequence));
                update_call_HAUSP_Stream(window, winNumber, A, n);
                curbatchPatternCount = hauspCount - calNextBatchPatternCount;
                if (isWriteToFile) {
                    writer.write("curBatch High Average-Utility Sequential Patterns Count：" + curbatchPatternCount);
                    writer.newLine();
                    writer.write("\n");

                }
                batchSequence.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        } finally {
            if (Input != null) {
                try {
                    Input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("error:" + e.getMessage());
                }
            }
        }

        this.winNumber = winNumber;
        this.batchNumber = batchNumber;
        MemoryLogger.getInstance().checkMemory();
        endTimestamp = System.currentTimeMillis();

        writer.write("=============THE RESULT OF HAUSP_UDS Algorithm v2.0 Stats==========\n");
        writer.write(" Test file: " + input.split("/")[input.split("/").length - 1] + "\n");
        writer.write(" Minimum average-utility threshold: " + minAUtil + " \n");
        writer.write(" Minmum Probability threshold: " + minPro + " \n");
        writer.write(" The size of window: " + winSize + " \n");
        writer.write(" The size of batch: " + batchSize + " \n");
        writer.write(" The count of window: " + winNumber + " \n");
        writer.write(" The count of batch: " + batchNumber + " \n");
        writer.write(" Total time: " + (endTimestamp - startTimestamp) / 1000.0 + " s\n");
        writer.write(" Max memory: " + MemoryLogger.getInstance().getMaxMemoryUsage() + " MB\n");
        writer.write(" Number of candidates: " + candidatesCount + " \n");
        writer.write(" Number of HAUSPs: " + hauspCount + " \n");
        writer.write("====================================================================\n");

        writer.close();

        if (debug) {
            System.out.println("The number of transactions in the data streams is " + seqCount);
            System.out.println();
        }


    }


    /**
     * DecayFactor settings
     */
    public static class DecayFunction {

        /*sigmoid衰减函数*/
        public double sigmoidDecay(double T, double t, double alpha) {

            //Parameter T-t controls the time step
            return 1 / (1 + Math.exp(alpha * (T - t)));   //原始sigmoid函数,√
        }

        /*指数衰减*/
        public double exponentialDecay(double T, double t, double A, double alpha) {
//            return A*Math.exp(-alpha*(T-t));
            return A * Math.pow(1.2, -alpha * (T - t));     //√
        }

        /*多项式衰减*/
        public double polynomialDecay(double T, double A, double t, double n) {
            return A * Math.pow(1 - (T - t) / T, n);      //√
        }

        public static void main(String[] args) {

            DecayFunction decayFunction = new DecayFunction();
            double T = 100000;    //当前时刻
            double t = 1;     //上个时刻
            double alpha = 0.1;//衰减参数，用于控制衰减速率

            /*sigmoid衰减函数测试*/
            double decayFactor = decayFunction.sigmoidDecay(T, t, alpha);
            System.out.println("sigmoid衰减因子：" + decayFactor);

            /*指数衰减函数测试*/
            double A1 = 1;
            double decayFactor1 = decayFunction.exponentialDecay(T, t, A1, alpha);
            System.out.println("指数衰减因子：" + decayFactor1);

            /*多项式衰减函数测试*/
            double A2 = 1;        //系数
            double n = 0.02;          //次方
            double decayFactor2 = decayFunction.polynomialDecay(T, A2, t, n);
            System.out.println("多项式衰减因子：" + decayFactor2);
        }
    }


    /**
     * initial call the HAUSP_Stream algorithm
     *
     * @param window    a window
     * @param winNumber Window Number
     * @param A         Polynomial decay function parameters
     * @param n         Polynomial decay function parameters
     */
    public void initial_call_HAUSP_Stream(ArrayList<ArrayList<String>> window, int winNumber, double A, double n) throws IOException {

        if (debug) {
            System.out.println("The current window is " + winNumber);
        }
        winStartTimeStamp = System.currentTimeMillis();


        Map<Integer, Integer> mapItemToSWU = new HashMap<>();
        Map<Integer, Float> mapItemToSWP = new HashMap<>();
        List<List<Itemset>> revisedDB = new ArrayList<>();
        Map<Integer, SequenceList> sequenceListMap = new HashMap<>();
        Map<Integer, Integer> lineSU = new HashMap<>();
        Map<Integer, Float> linePR = new HashMap<>();
        DecayFunction decayFunction = new DecayFunction();
        double t = 1;

        for (ArrayList<String> batchSequence : window) {
            double T = window.size() * batchSequence.size();
            for (String curline : batchSequence) {
                HashSet<Integer> consideredItems = new HashSet<>();
                String[] arr = curline.split(" ");
                int SU = Integer.parseInt(arr[arr.length - 2].substring(arr[arr.length - 2].indexOf(':') + 1));
                float PR = Float.parseFloat(arr[arr.length - 1].substring(arr[arr.length - 1].indexOf(':') + 1));
                double decayFactor = decayFunction.polynomialDecay(T, A, t, n);
                SU = (int) Math.ceil(SU * decayFactor);
                for (int i = 0; i < arr.length - 4; i++) {
                    String itemString = arr[i];
                    if (!itemString.equals("-1")) {
                        int item = Integer.parseInt(itemString.substring(0, itemString.indexOf('[')));

                        if (!consideredItems.contains(item)) {
                            consideredItems.add(item);
                            Integer SWU = mapItemToSWU.get(item);
                            Float SWP = mapItemToSWP.get(item);
                            SWU = (SWU == null) ? mapItemToSWU.put(item, SU) : mapItemToSWU.put(item, SWU + SU);
                            SWP = (SWP == null) ? mapItemToSWP.put(item, PR) : mapItemToSWP.put(item, SWP + PR);
                        }
                    }
                }
                t++;
            }
        }


        int order = 0;
        t = 1;
        for (ArrayList<String> batchSequence : window) {
            double T = window.size() * batchSequence.size();

            for (String curline : batchSequence) {

                List<Itemset> sequence = new ArrayList<>();
                String[] arr = curline.split(" -1 ");

                double decayFactor = decayFunction.polynomialDecay(T, A, t, n);
                int SUtility = 0;
                String[] arrsub = arr[arr.length - 1].split(" ");
                float PR = Float.parseFloat(arrsub[arrsub.length - 1].substring(arrsub[arrsub.length - 1].indexOf(':') + 1));

                for (int i = 0; i < arr.length - 1; i++) {
                    Itemset sitemset = new Itemset();
                    String[] itemset = arr[i].trim().split(" ");

                    for (int j = 0; j < itemset.length; j++) {
//                        int item = Integer.parseInt(itemset[j].substring(0, arr[i].indexOf('[')));
//                        int utility = Integer.parseInt(itemset[j].substring(arr[i].indexOf('[') + 1, arr[i].indexOf(']')));
                        int item = Integer.parseInt(itemset[j].substring(0, itemset[j].indexOf('[')));
                        int utility = Integer.parseInt(itemset[j].substring(itemset[j].indexOf('[') + 1, itemset[j].indexOf(']')));
                        if (mapItemToSWU.get(item) >= minAUtil && mapItemToSWP.get(item) >= minPro) {
                            Item sitem = new Item();
                            sitem.item = item;
                            //计算衰减因子并应用于utility
                            sitem.utility = (int) Math.ceil(utility * decayFactor);
                            sitemset.Itemset.add(sitem);
                            SUtility += sitem.utility;        //重新计算SU
                        }
                    }

                    if (!sitemset.Itemset.isEmpty()) {
                        sequence.add(sitemset);
                    }
                }
                if (!sequence.isEmpty()) {
                    revisedDB.add(sequence);
                    lineSU.put(order, SUtility);
                    linePR.put(order, PR);
                    order++;
                }

                t++;
            }
        }

        WID = winNumber;
        for (int i = 0; i < revisedDB.size(); i++) {
            int ru = lineSU.get(i);
            float Pro = linePR.get(i);
            for (int j = 0; j < revisedDB.get(i).size(); j++) {
                for (int k = 0; k < revisedDB.get(i).get(j).Itemset.size(); k++) {
                    int item = revisedDB.get(i).get(j).Itemset.get(k).item;
                    int utility = revisedDB.get(i).get(j).Itemset.get(k).utility;
                    ru -= utility;
                    Element element = new Element(WID, BID, i, j, utility, ru, Pro);      //WID从1开始,BID、SID都是从0开始

                    if (sequenceListMap.containsKey(item)) {
                        sequenceListMap.get(item).addElement(element);
                    } else {
                        SequenceList Seq = new SequenceList();
                        List<Integer> itemSet = new ArrayList<>();
                        itemSet.add(item);
                        Seq.addItemset(itemSet);
                        Seq.addElement(element);
                        sequenceListMap.put(item, Seq);
                    }
                }
            }
            if ((i + 1) % batchSize == 0) {
                BID++;
            }
        }

        //PEU pruning strategy
        Map<List<Integer>, Integer> mapItemToPEU;
        List<SequenceList> oneSequenceList = new ArrayList<>(sequenceListMap.values());

        mapItemToPEU = getMapItemPEU(sequenceListMap);
        for (int i = 0; i < oneSequenceList.size(); i++) {
            SequenceList sequence = oneSequenceList.get(i);
            if (mapItemToPEU.get(sequence.itemsets.get(0)) < minAUtil || sequence.sumProbability < minPro) {
                oneSequenceList.remove(i);
                i--;
            }
        }

        oneSequenceList.sort(new Comparator<SequenceList>() {
            public int compare(SequenceList mc1, SequenceList mc2) {
                return mc1.itemsets.get(0).get(0) - mc2.itemsets.get(0).get(0);
            }
        });
        MemoryLogger.getInstance().checkMemory();

        //for each matchedSequenceList prefix mining
        for (SequenceList Seq : oneSequenceList) {
            AlgoHAUSP_UDS(Seq, revisedDB, minAUtil, minPro);
        }

        curWindowCandidatesCount = candidatesCount;
        winEndTimeStamp = System.currentTimeMillis();
        if (debug) {
            System.out.println("Time Spent in Window " + winNumber + ": " + (winEndTimeStamp - winStartTimeStamp) / 1000.0 + "s");
            System.out.println("CandidatesCount: " + curWindowCandidatesCount);
            System.out.println("==========================================");
        }

    }

    /**
     * call the HAUSP_Stream algorithm again
     *
     * @param window    a window
     * @param winNumber Window Number
     * @param A         Polynomial decay function parameters
     * @param n         Polynomial decay function parameters
     */
    public void update_call_HAUSP_Stream(ArrayList<ArrayList<String>> window, int winNumber, double A, double n) throws IOException {

        if (debug) {
            System.out.println("The current window is " + winNumber);
        }
        winStartTimeStamp = System.currentTimeMillis();

        Map<Integer, Integer> mapItemToSWU = new HashMap<>();
        Map<Integer, Float> mapItemToSWP = new HashMap<>();
        List<List<Itemset>> revisedDB = new ArrayList<>();
        Map<Integer, SequenceList> sequenceListMap = new HashMap<>();
        Map<Integer, Integer> lineSU = new HashMap<>();
        Map<Integer, Float> linePR = new HashMap<>();
        DecayFunction decayFunction = new DecayFunction();
        double t = 1;


        for (ArrayList<String> batchSequence : window) {
            double T = window.size() * batchSequence.size();
            for (String curline : batchSequence) {
                HashSet<Integer> consideredItems = new HashSet<>();
                String[] arr = curline.split(" ");
                int SU = Integer.parseInt(arr[arr.length - 2].substring(arr[arr.length - 2].indexOf(':') + 1));
                float PR = Float.parseFloat(arr[arr.length - 1].substring(arr[arr.length - 1].indexOf(':') + 1));
                double decayFactor = decayFunction.polynomialDecay(T, A, t, n);
                SU = (int) Math.ceil(SU * decayFactor);
                for (int i = 0; i < arr.length - 3; i++) {
                    String itemString = arr[i];
                    if (!itemString.equals("-1")) {
                        int item = Integer.parseInt(itemString.substring(0, itemString.indexOf('[')));

                        if (!consideredItems.contains(item)) {
                            consideredItems.add(item);
                            Integer SWU = mapItemToSWU.get(item);
                            Float SWP = mapItemToSWP.get(item);
                            SWU = (SWU == null) ? mapItemToSWU.put(item, SU) : mapItemToSWU.put(item, SWU + SU);
                            SWP = (SWP == null) ? mapItemToSWP.put(item, PR) : mapItemToSWP.put(item, SWP + PR);
                        }
                    }
                }
                t++;
            }
        }


        int order = 0;
        t = 1;
        for (ArrayList<String> batchSequence : window) {
            double T = window.size() * batchSequence.size();

            for (String curline : batchSequence) {

                List<Itemset> sequence = new ArrayList<>();
                String[] arr = curline.split(" -1 ");

                double decayFactor = decayFunction.polynomialDecay(T, A, t, n);
                int SUtility = 0;
                String[] arrsub = arr[arr.length - 1].split(" ");
                float PR = Float.parseFloat(arrsub[arrsub.length - 1].substring(arrsub[arrsub.length - 1].indexOf(':') + 1));

                for (int i = 0; i < arr.length - 1; i++) {
                    Itemset sitemset = new Itemset();
                    String[] itemset = arr[i].trim().split(" ");

                    for (int j = 0; j < itemset.length; j++) {
                        int item = Integer.parseInt(itemset[j].substring(0, itemset[j].indexOf('[')));
                        int utility = Integer.parseInt(itemset[j].substring(itemset[j].indexOf('[') + 1, itemset[j].indexOf(']')));
                        if (mapItemToSWU.get(item) >= minAUtil && mapItemToSWP.get(item) >= minPro) {
                            Item sitem = new Item();
                            sitem.item = item;
                            //计算衰减因子并应用于utility
                            sitem.utility = (int) Math.ceil(utility * decayFactor);
                            sitemset.Itemset.add(sitem);
                            SUtility += sitem.utility;        //重新计算SU
                        }
                    }

                    if (!sitemset.Itemset.isEmpty()) {
                        sequence.add(sitemset);
                    }
                }
                if (!sequence.isEmpty()) {
                    revisedDB.add(sequence);
                    lineSU.put(order, SUtility);
                    linePR.put(order, PR);
                    order++;
                }

                t++;
            }
        }

        WID = winNumber;
        for (int i = 0; i < revisedDB.size(); i++) {
            int ru = lineSU.get(i);
            float Pro = linePR.get(i);
            for (int j = 0; j < revisedDB.get(i).size(); j++) {
                for (int k = 0; k < revisedDB.get(i).get(j).Itemset.size(); k++) {
                    int item = revisedDB.get(i).get(j).Itemset.get(k).item;
                    int utility = revisedDB.get(i).get(j).Itemset.get(k).utility;
                    ru -= utility;
                    Element element = new Element(WID, BID, i, j, utility, ru, Pro);

                    if (sequenceListMap.containsKey(item)) {
                        sequenceListMap.get(item).addElement(element);
                    } else {
                        SequenceList Seq = new SequenceList();
                        List<Integer> itemSet = new ArrayList<>();
                        itemSet.add(item);
                        Seq.addItemset(itemSet);
                        Seq.addElement(element);
                        sequenceListMap.put(item, Seq);
                    }
                }
            }
            if ((i + 1) % batchSize == 0) {
                BID++;
            }
        }

        //PEU pruning strategy
        Map<List<Integer>, Integer> mapItemToPEU;
        List<SequenceList> oneSequenceList = new ArrayList<>(sequenceListMap.values());

        mapItemToPEU = getMapItemPEU(sequenceListMap);
        for (int i = 0; i < oneSequenceList.size(); i++) {
            SequenceList sequence = oneSequenceList.get(i);
            if (mapItemToPEU.get(sequence.itemsets.get(0)) < minAUtil || sequence.sumProbability < minPro) {
                oneSequenceList.remove(i);
                i--;
            }
        }

        oneSequenceList.sort(new Comparator<SequenceList>() {
            public int compare(SequenceList mc1, SequenceList mc2) {
                return mc1.itemsets.get(0).get(0) - mc2.itemsets.get(0).get(0);
            }
        });
        MemoryLogger.getInstance().checkMemory();

        //for each matchedSequenceList prefix mining
        for (SequenceList Seq : oneSequenceList) {
            AlgoHAUSP_UDS(Seq, revisedDB, minAUtil, minPro);
        }

        int cur = candidatesCount - curWindowCandidatesCount;

        winEndTimeStamp = System.currentTimeMillis();

        if (debug) {
            System.out.println("Time Spent in Window " + winNumber + ": " + (winEndTimeStamp - winStartTimeStamp) / 1000.0 + "s");
            System.out.println("CandidatesCount: " + cur);
            System.out.println("==========================================");
        }
        curWindowCandidatesCount += cur;

    }

    /**
     * get mapItemToPEU
     */
    protected HashMap<List<Integer>, Integer> getMapItemPEU(Map<Integer, SequenceList> sequenceListMap) {
        HashMap<List<Integer>, Integer> mapItemToPEU = new HashMap<>();

        for (SequenceList sequenceList : sequenceListMap.values()) {
            int order = 0;
            int orderUtility = 0;
            float orderProbability = 0;
            int orderPEU = 0;


            if (!sequenceList.elements.isEmpty()) {
                order = sequenceList.elements.get(0).SID;
                orderUtility = sequenceList.elements.get(0).utility;
                orderProbability = sequenceList.elements.get(0).probability;
                orderPEU = sequenceList.elements.get(0).remaining + sequenceList.elements.get(0).utility;
            }
            for (Element element : sequenceList.elements) {
                if (element.SID == order) {
                    if (element.utility > orderUtility) {
                        orderUtility = element.utility;
                    }
                    if (element.probability > orderProbability) {
                        orderProbability = element.probability;
                    }
                    if (element.remaining + element.utility > orderPEU) {
                        orderPEU = element.remaining + element.utility;
                    }
                } else {
                    sequenceList.sumUtility += orderUtility;        //每行中序列的最大效用
                    sequenceList.sumUB += orderPEU;     //每行中序列的PEU，即效用与最大效用之和的最大值
                    sequenceList.sumProbability += orderProbability;


                    order = element.SID;
                    orderUtility = element.utility;
                    orderPEU = element.remaining + element.utility;
                    orderProbability = element.probability;
                }
            }
            sequenceList.sumUtility += orderUtility;
            sequenceList.sumProbability += orderProbability;
            sequenceList.sumUB += orderPEU;
            mapItemToPEU.put(sequenceList.itemsets.get(0), sequenceList.sumUB);
        }

        return mapItemToPEU;
    }


    /**
     * Mining processing, projection-based
     *
     * @param Seq       prefix sequence
     * @param revisedDB revised window
     * @param minAUtil  the minmum average utility threshold
     */
    public void AlgoHAUSP_UDS(SequenceList Seq, List<List<Itemset>> revisedDB, int minAUtil, float minPro) throws IOException {

        candidatesCount++;
        int length = 0;
        for (int m = 0; m < Seq.itemsets.size(); m++) {
            length += Seq.itemsets.get(m).size();
        }
        int au = (int) Math.ceil((double) Seq.sumUtility / length);     //平均效用计算之后需要保留为double型，然后再向上取整，不然两个int型数据相除，结果还会是int型，少了小数部分。
        if (au >= minAUtil && Seq.sumProbability >= minPro) {
            hauspCount++;
            //写入输出文件
            if (isWriteToFile) {
                writeResultToFile(Seq, au, SAVE_RESULT_EASIER_TO_READ_FORMAT);
            }

        }

        MemoryLogger.getInstance().checkMemory();
        List<SequenceList> childNode = new ArrayList<>();
        Map<Integer, SequenceList> sequenceListMap = new HashMap<>();

        Map<Integer, SequenceList> ilist = new HashMap<>();
        Map<Integer, SequenceList> slist = new HashMap<>();

        for (Element element : Seq.elements) {
            int WID = element.WID;
            int BID = element.BID;
            int SID = element.SID;
            int position = element.position;
            int preUtility = 0;

            int i;
            for (i = 0; i < revisedDB.get(SID).get(position).Itemset.size(); i++) {
                int item = revisedDB.get(SID).get(position).Itemset.get(i).item;//遍历项集内的项，找到目标项
                if (item == Seq.itemsets.get(Seq.itemsets.size() - 1).get(Seq.itemsets.get(Seq.itemsets.size() - 1).size() - 1)) {
                    i++;
                    break;
                }
            }

            //put into ilist
            for (; i < revisedDB.get(SID).get(position).Itemset.size(); i++) {
                int item = revisedDB.get(SID).get(position).Itemset.get(i).item;
                int utility = revisedDB.get(SID).get(position).Itemset.get(i).utility;
                preUtility += utility;

                Element newElement = new Element(WID, BID, SID, position, element.utility + utility, element.remaining - preUtility, element.probability);
                if (!ilist.containsKey(item)) {
                    SequenceList newlist = new SequenceList();
                    newlist.itemsets.addAll(Seq.itemsets);

                    List<Integer> itemset = new ArrayList<>(Seq.itemsets.get(Seq.itemsets.size() - 1));
                    itemset.add(item);
                    newlist.itemsets.remove(newlist.itemsets.size() - 1);
                    newlist.itemsets.add(itemset);

                    newlist.addElement(newElement);
                    ilist.put(item, newlist);
                } else {
                    ilist.get(item).addElement(newElement);
                }
            }


            //put into slist
            for (int j = element.position + 1; j < revisedDB.get(SID).size(); j++) {
                for (int k = 0; k < revisedDB.get(SID).get(j).Itemset.size(); k++) {
                    int item = revisedDB.get(SID).get(j).Itemset.get(k).item;
                    int utility = revisedDB.get(SID).get(j).Itemset.get(k).utility;
                    preUtility += utility;

                    Element newElement = new Element(WID, BID, SID, j, element.utility + utility, element.remaining - preUtility, element.probability);
                    if (!slist.containsKey(item)) {
                        SequenceList newlist = new SequenceList();
                        newlist.itemsets.addAll(Seq.itemsets);

                        List<Integer> itemset = new ArrayList<>();
                        itemset.add(item);
                        newlist.itemsets.add(itemset);

                        newlist.addElement(newElement);
                        slist.put(item, newlist);
                    } else {
                        slist.get(item).addElement(newElement);
                    }
                }
            }
        }

        //iConcatenation and pruning unpromising item
        for (Map.Entry<Integer, SequenceList> entry : ilist.entrySet()) {
            entry.getValue().calculate();  //calculate PEU to store in sumUB
            if (entry.getValue().sumUB >= minAUtil && entry.getValue().sumProbability >= minPro) {
                childNode.add(entry.getValue());
            }
        }


        //sConcatenation and pruning unpromising
        for (Map.Entry<Integer, SequenceList> entry : slist.entrySet()) {
            entry.getValue().calculate(); //calculate PEU to store in sumUB
            if (entry.getValue().sumUB >= minAUtil && entry.getValue().sumProbability >= minPro) {
                childNode.add(entry.getValue());
            }
        }


        for (SequenceList nextlist : childNode) {
            AlgoHAUSP_UDS(nextlist, revisedDB, minAUtil, minPro);
        }


    }


    /**
     * write a high average utility pattern to file
     *
     * @param sequenceList                      a sequenceList Data structure
     * @param au                                Minimum average utility threshold
     * @param SAVE_RESULT_EASIER_TO_READ_FORMAT the output in readable mode
     */
    public void writeResultToFile(SequenceList sequenceList, int au, boolean SAVE_RESULT_EASIER_TO_READ_FORMAT) throws IOException {

        // 创建 DecimalFormat 对象，指定保留两位小数并四舍五入
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        // 格式化 double 值
        String formattedValue = decimalFormat.format(sequenceList.sumProbability);

        if (!SAVE_RESULT_EASIER_TO_READ_FORMAT) {
            for (List<Integer> itemset : sequenceList.itemsets) {
                for (int item : itemset) {
                    writer.write(item + " ");
                }
                writer.write("-1 ");
            }
        } else {
            writer.write("<[");
            for (int i = 0; i < sequenceList.itemsets.size(); i++) {
                List<Integer> itemset = sequenceList.itemsets.get(i);
                for (int j = 0; j < itemset.size(); j++) {
                    int item = itemset.get(j);
                    writer.write(Integer.toString(item));
                    if (j < itemset.size() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("]");
                if (i < sequenceList.itemsets.size() - 1) {
                    writer.write("[");
                }
            }

            writer.write(">");
        }
        writer.write(" #AUTIL: ");
        writer.write(Integer.toString(au));
        writer.write(" #PROB: ");
        writer.write(formattedValue);
        writer.newLine();
    }

    /**
     * Print Statistics about the algorithm execution
     */
    public void printStats() {
        System.out.println("=====================THE RESULT OF HAUSP_UDS Algorithm v2.0 Stats=====================");
        System.out.println("Test file: " + input.split("/")[input.split("/").length - 1]);
        System.out.println("minAUtil: " + minAUtil);
        System.out.println("minPro: " + minPro);
        System.out.println("windowSize: " + winSize);
        System.out.println("batchSize: " + batchSize);
        System.out.println("windowCount:" + winNumber);
        System.out.println("batchCount:" + batchNumber);
        System.out.println("Total time: " + (endTimestamp - startTimestamp) / 1000.0 + " s");
        System.out.println("MaxMemory: " + MemoryLogger.getInstance().getMaxMemoryUsage() + "MB");
        System.out.println("HAUSPs: " + hauspCount);
        System.out.println("Candidates: " + candidatesCount);
        System.out.println("=======================================================================================");
    }

}




