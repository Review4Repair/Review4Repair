/*
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AllDiffMain {
    public static boolean debug = false;

    public static String getAllChange(String file1, String file2, int max_target_len, int max_input_len) {

        List<String> file1_list = new ArrayList<String>();
        file1_list.addAll(Arrays.asList(file1.split("\\r?\\n")));
        List<String> file2_list = new ArrayList<String>();
        file2_list.addAll(Arrays.asList(file2.split("\\r?\\n")));

        Patch patch = DiffUtils.diff(file1_list, file2_list);
        List<Delta> all_changes = patch.getDeltas();

        if (debug) System.out.println("Total number of changes: " + all_changes.size());

        List<Delta> changes = new ArrayList<>();
        for (Delta change : all_changes) {
            if (change.getOriginal().size() <= max_target_len || change.getRevised().size() <= max_target_len) {
                changes.add(change);
            }
        }

        if (changes.size() == 0) {
            return "<|nochange|>";
        }

        String alldiff = "";

        for (Delta change : all_changes) {

            boolean noChange = false;
            String status = "none";

            int src_start = change.getOriginal().getPosition();
            int src_end = src_start + change.getOriginal().size();
            int tgt_start = change.getRevised().getPosition();
            int tgt_end = tgt_start + change.getRevised().size();


            if (change.getOriginal().size() == 0 && change.getRevised().size() > 0) {
                if (debug) System.out.println("Inserted!");
                if (src_start > 0 && tgt_start > 0) {
                    src_start -= 1;
                    tgt_start -= 1;
                }
                status = "insert";
            } else if (change.getOriginal().size() > 0 && change.getRevised().size() == 0) {
                if (debug) System.out.println("Deleted!");
                status = "delete";
            } else {
                if (debug) System.out.println("Updated!");
                status = "update";
            }


            List<String> target = new ArrayList<>();
            //System.out.print("Target: ");
            if (status == "delete") {
                target.add("<|del|>");
            } else {
                for (int i = tgt_start; i < tgt_end; i++) {
                    target.add(file2_list.get(i));
                }
            }


            int sublist_start = Math.max(0, src_start - max_input_len);
            int sublist_end = Math.min(file1_list.size(), src_end + max_input_len);
            src_end -= sublist_start;
            src_start -= sublist_start;
            List<String> file1_list_cropped = file1_list.subList(sublist_start, sublist_end);
            file1_list_cropped.add(src_end, "<|endfocus|>");
            file1_list_cropped.add(src_start, "<|startfocus|>");

            String inputCode = "", outputCode = "";
            for (String x : file1_list_cropped) {
                inputCode += x + "\n";
            }
            for (String y : target) {
                outputCode += y + "\n";
            }
            //System.out.println(file1_list);
            if (debug) System.out.println("TARGET: " + target);
            ChangeData changeData = new ChangeData(inputCode, outputCode, status);
            String output = changeData.status + "<|sep|>";
            output += changeData.inputCode;
            output += "<|sep|>";
            output += changeData.outputCode;
            alldiff += output+"<|datasep|>";
        }
        return alldiff.substring(0, alldiff.length() - 11);
    }


    public static void main(String[] args) {
        //System.out.println(Paths.get("").toAbsolutePath().toString());
        String file1_path, file2_path;
        //int line_number, change_window_size;
        String file1="", file2="";
        int max_input_len = 20, max_target_len = 5;

        if (debug) {
            file1_path = "prev_code.txt";
            file2_path = "changed_code.txt";
        }
        else {
            file1_path = args[0];
            file2_path = args[1];
            max_target_len = Integer.parseInt(args[2]);
            max_input_len = Integer.parseInt(args[3]);
        }

        try {
            file1 = new String(Files.readAllBytes(Paths.get(file1_path)));
            file2 = new String(Files.readAllBytes(Paths.get(file2_path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String output = getAllChange(file1, file2, max_target_len, max_input_len);
        //System.out.println("Source: "+changeData.inputCode);
        //System.out.println("Target: "+changeData.outputCode);

        System.out.println(output);

    }

}
*/
