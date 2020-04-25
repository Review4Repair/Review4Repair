import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class ChangeData {
    public String inputCode, outputCode, status;
    ChangeData(String inputCode, String outputCode, String status) {
        this.inputCode = inputCode;
        this.outputCode = outputCode;
        this.status = status;
    }
}

class AllDiff {

    public static String getChange(String file1, String file2, int max_target_len, int max_input_len, boolean debug) {

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
            ArrayList<String> file1_list_cropped = new ArrayList<String>(file1_list.subList(sublist_start, sublist_end));
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
            alldiff += output + "<|datasep|>";
        }
        return alldiff.substring(0, alldiff.length() - 11);
    }



    public static String getOneLineChanges(String file1, String file2){
        List<String> file1_list = new ArrayList<String>();
        file1_list.addAll(Arrays.asList(file1.split("\\r?\\n")));
        List<String> file2_list = new ArrayList<String>();
        file2_list.addAll(Arrays.asList(file2.split("\\r?\\n")));

        Patch patch = DiffUtils.diff(file1_list, file2_list);
        List<Delta> all_changes = patch.getDeltas();

        List<Delta> changes = new ArrayList<>();
        for (Delta change : all_changes) {
            if (change.getOriginal().size() == 1 || change.getRevised().size() == 1) {
                changes.add(change);
            }
        }

        if (changes.size() == 0) {
            return "<|nochange|>";
        }

        String output = "";
        for(Delta change: changes){
            output+= change.getOriginal().getPosition() + " " + change.getRevised().getPosition() + "\n";
        }

        return output.substring(0, output.length() - 1);
    }
}


class LineNumberDiff {

    static int max_target_len = 5;
    static int max_input_len = 20;

    public static String getChange(String file1, String file2, int line_number, int change_window_size, boolean debug) {

        // LINE NUMBER ERROR CHECK
        if(line_number <0){
            System.out.println("Line number cannot be less than 1!");
            return "";
        } else if (line_number > file1.length()){
            System.out.println("Line number cannot be greater than the file size!");
            return "";
        }

        List<String> file1_list = new ArrayList<String>();
        file1_list.addAll(Arrays.asList(file1.split("\\r?\\n")));
        List<String> file2_list = new ArrayList<String>();
        file2_list.addAll(Arrays.asList(file2.split("\\r?\\n")));

        Patch patch = DiffUtils.diff(file1_list, file2_list);
        List<Delta> changes = patch.getDeltas();

        int start_focus = 0;
        int end_focus = 0;
        int start_tgt = 0;
        int end_tgt = 0;

        boolean noChange = false;
        String status = "none";

        if (changes.size() == 0) {
            start_focus = line_number;
            end_focus = line_number + 1;
            start_tgt = start_focus;
            end_tgt = end_focus;
        }
        else {
            Delta nearest_change = changes.get(0);
            for (Delta change : changes) {
                if (Math.abs(change.getOriginal().getPosition() - line_number) <=
                        Math.abs((nearest_change.getOriginal().getPosition() - line_number))) {
                    nearest_change = change;
                } else {
                    break;
                }
            }

            int src_start = nearest_change.getOriginal().getPosition();
            int src_end = src_start + nearest_change.getOriginal().size();
            int tgt_start = nearest_change.getRevised().getPosition();
            int tgt_end = tgt_start + nearest_change.getRevised().size();

            if (Math.abs(nearest_change.getOriginal().getPosition() - line_number) > change_window_size) {
                if (debug) System.out.println("Outside Context Window.");
                //System.out.println("Line number: "+line_number);
                start_focus = line_number;
                end_focus = line_number + 1;
                start_tgt = start_focus; //Set focus as target
                end_tgt = end_focus;
                noChange = true;
                status = "unchanged";
            } else if (nearest_change.getOriginal().size() == 0 && nearest_change.getRevised().size() > 0) {
                if (debug) System.out.println("Inserted!");
                start_focus = src_start - 1;
                end_focus = src_start;
                start_tgt = nearest_change.getRevised().getPosition() - 1;
                end_tgt = nearest_change.getRevised().getPosition() + nearest_change.getRevised().size();
                status = "insert";
            } else if (nearest_change.getOriginal().size() > 0 && nearest_change.getRevised().size() == 0) {
                if (debug) System.out.println("Deleted!");
                start_focus = src_start;
                end_focus = src_start + nearest_change.getOriginal().size();
                start_tgt = nearest_change.getRevised().getPosition();
                end_tgt = start_tgt;
                status = "delete";
            } else {
                if (debug) System.out.println("Updated!");
                start_focus = src_start;
                end_focus = src_end;
                start_tgt = tgt_start;
                end_tgt = tgt_end;
                status = "update";
            }
        }

        List<String> target = new ArrayList<>();
        //System.out.print("Target: ");
        if (start_tgt == end_tgt) {
            target.add("<|del|>");
        } else if (noChange) {
            // If no change in context window, keep the focus as target
            if (debug) System.out.println("Focus: " + start_focus + " " + end_focus);
            for (int i = start_focus; i < end_focus; i++) {
                target.add(file1_list.get(i));
            }
        } else {
            for (int i = start_tgt; i < end_tgt; i++) {
                target.add(file2_list.get(i));
            }
        }

        file1_list.add(end_focus, "<|endfocus|>");
        file1_list.add(start_focus, "<|startfocus|>");

        String inputCode = "", outputCode = "";
        for (String x : file1_list) {
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
        return output;
    }
}



public class Main {
    public static boolean debug = false;

    public static void main(String[] args) {
        //System.out.println(Paths.get("").toAbsolutePath().toString());
        String file1_path, file2_path;
        int line_number, change_window_size, max_target_len, max_input_len;
        String file1="", file2="", difftype="oneliners";

        file1_path = "prev_code.txt";
        file2_path = "changed_code.txt";
        line_number = 23;
        change_window_size = 5;
        max_target_len =
        max_input_len = 10;

        if (!debug) {
            difftype = args[0];
            file1_path = args[1];
            file2_path = args[2];
            if (difftype.equals("alldiff")){
                max_target_len = Integer.parseInt(args[3]);
                max_input_len = Integer.parseInt(args[4]);
            }
            else if (difftype.equals("linediff")){
                line_number = Integer.parseInt(args[3]);
                change_window_size = Integer.parseInt(args[4]);
            }
            /*else if (difftype.equals("onelines")){

            }*/
        }

        try {
            file1 = new String(Files.readAllBytes(Paths.get(file1_path)));
            file2 = new String(Files.readAllBytes(Paths.get(file2_path)));
        } catch (IOException e) {
            e.printStackTrace();
        }


        String output = "Diff Type Not Selected. Type [alldiff, linediff].";

        if (difftype.equals("alldiff")){
            AllDiff allDiff = new AllDiff();
            output = allDiff.getChange(file1, file2, max_target_len, max_input_len, debug);
        }
        else if (difftype.equals("linediff")){
            LineNumberDiff lineNumberDiff = new LineNumberDiff();
            output = lineNumberDiff.getChange(file1, file2, line_number - 1, change_window_size, debug);
        }
        else  if (difftype.equals("oneliners")){
            AllDiff allDiff = new AllDiff();
            output = allDiff.getOneLineChanges(file1, file2);
        }
        else {
            output = "Acceptable Commands: alldiff, linediff, oneliners";
        }

        if (debug) System.out.println(difftype);
        System.out.println(output);
    }
}
