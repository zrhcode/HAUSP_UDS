import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * @Copyright(C): 2024, North Minzu University
 * @Description:
 * @Filename：MainTestHAUSP_UDS
 * @Date：2024/4/12
 * @Author：zrh
 */

public class MainTestHAUSP_UDS {
    public static void main(String[] args) throws IOException {


        /*The input and output file path*/
        String dataset = "OnlineRetailII_SPMF_with_Weight";
        String result = "OnlineRetailII_SPMF_with_Weight";
        String input = fileToPath("../HAUSP_UDS/datasets/" + dataset + ".txt");
        String output = "../HAUSP_UDS/output/" + result + ".txt";

        /*The parameters*/
        int minAUtil = 1000;
        float minPro = 100.0f;
        int winSize = 2;
        int batchSize = 1000;


        /*多项式衰减函数的参数*/
        double A = 1;
        double n = 0.02;


        /*Run the algorithm*/
        AlgoHAUSP_UDS algoHAUSP_uds = new AlgoHAUSP_UDS();
        algoHAUSP_uds.runAlgorithm(input, winSize, batchSize, minAUtil, minPro, output, A, n);
        algoHAUSP_uds.printStats();

    }


    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestHAUSP_UDS.class.getResource(filename);
        return (url != null) ? java.net.URLDecoder.decode(url.getPath(), "UTF-8") : "./" + filename;
    }

}
