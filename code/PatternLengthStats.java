import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PatternLengthStats {
    public static void main(String[] args) {
        String inputFile = "D:\\文献\\实验\\HAUSP_UDS\\结果集统计\\minautil\\ecommerce\\2_3000_uncertain_ecommerce.txt_60000_5.0"; // 输入文件路径
        String outputFile = "D:\\文献\\实验\\HAUSP_UDS\\结果集统计\\ResultStats\\ecommerce\\2_3000_uncertain_ecommerce.txt_60000_5.0"; // 输出文件路径

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            int totalCount = 0;
            int[] lengthCounts = new int[1]; // 初始长度为1，随着统计动态扩展

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length > 0 && parts[0].startsWith("<[")) {
                    String sequence = parts[0].substring(1, parts[0].length() - 1);
                    int length = countPatternLength(sequence);
                    totalCount++;
                    if (length >= lengthCounts.length) {
                        // 动态扩展数组长度
                        int newLength = length + 1;
                        int[] newCounts = new int[newLength];
                        System.arraycopy(lengthCounts, 0, newCounts, 0, lengthCounts.length);
                        lengthCounts = newCounts;
                    }
                    lengthCounts[length]++;
                    writer.write(line.trim() + " Length = " + length + "\n");
                }
            }

            writer.write("\nFinal Pattern Length Statistics:\n");
            System.out.println("Final Pattern Length Statistics:");
            for (int i = 1; i < lengthCounts.length; i++) {
                int count = lengthCounts[i];
                if (count > 0) {
                    double percentage = (double) count / totalCount * 100;
                    String output = "Length=" + i + " | Count=" + count + " | Percentage=" + String.format("%.2f", percentage) + "%\n";
                    writer.write(output);
                    System.out.println("Length=" + i + " | Count=" + count + " | Percentage=" + String.format("%.2f", percentage) + "%");
                }
            }

            reader.close();
            writer.close();
            System.out.println("Pattern lengths statistics written to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int countPatternLength(String sequence) {
        // 模式的长度即为其中包含的数据项个数，这里使用逗号分隔来计算
        int count = 0; // 初始长度为0
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i)=='[' || sequence.charAt(i)==',') {
                count++;
            }
        }
        return count; // 返回数据项的数量作为长度
    }
}
