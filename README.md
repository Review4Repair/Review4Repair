# Review4Repair

This repository contains the dataset for "Review4Repair: Code Review Aided Automatic Program Repairing". 

# Pretrained Models

| Index | Model Name | Link  |
|:-------------: |:-------------:| :-----:|
| 1      | best_model_cc_hard      |   [link](https://mega.nz/file/gU41UaAY#wUEldVrrbg0qi_SmLpndSUjHP81A12gOkiKEKWrf1Pk) |
| 2      | best_model_c_hard      |   [link](https://mega.nz/file/MUpnGKwA#Er7ISKH6JR8yPmyz3IDqzkd4maDExDpjTb5_Dclox8I) |
| 3      | best_model_cc_soft      |   [link](https://mega.nz/file/RA4FlC4Q#nCIdznperFBYm6tdSxxRCrf_07xdVsQfHS-_yr_Tlv0) |
| 4      | best_model_c_soft      |   [link](https://mega.nz/file/5NpznY6A#hAzdtUtri8fZLyU6F4WYVeL-gFCh6yvsBafYA8jzIF4) |
| 5      | best_model_c_10k_vocab      |   [link](https://mega.nz/file/1IgBxS4L#vzKnk1ZWYWCdGoM46sWb75mWfEjXM8xlmOQfhwDLbl4) |
| 6      | best_model_c_20k_vocab      |   [link](https://mega.nz/file/ZI5xFKTL#i-QU-9nShGeVVEm4YTHrMcPFsa0IDjZOpOBLJMPNcAk) |
| 7      | best_pretrained_model_c_hard      |   [link](https://mega.nz/file/ZYxlTAyY#dR5MtfC1EHqcclUPjvZCLnkZP1p4yvQXcgJMhrRvd8Y) |

To run inference and evaluation, unzip the particular checkpoint. Sample tokenized test sets are available in "Inference" directory.   
**Evaluation:** ```python Inference\evaluation.py <test_set_src> <test_set_tgt> <model_name>``` 

**Predict:** ```python Inference\prediction.py <test_set_src> <model_name>```

For example, to evaluate model_cc on test set, 

```python Inference\evaluate.py Inference\CC_sample_src-test.txt Inference\CC_sample_tgt-test.txt best_model_cc_hard.pt``` 

To predict using model_cc on test set, 
```python Inference\predict.py Inference\CC_sample_src-test.txt best_model_cc_hard.pt```

# Train

For details on model training, refer to [OpenNMT documentation](https://opennmt.net/OpenNMT-py/index.html). To run training:

```
!onmt_preprocess -train_src training_data/<SRC_DATA>.txt \
    -train_tgt training_data/c/<TGT_DATA>.txt \
    -save_data <SAVE_DIR> \
    -src_vocab vocab/<SRC_VOCAB_FILE>.txt \
    -tgt_vocab vocab/<TGT_VOCAB_FILE>.txt \
    -src_vocab_size <SRC_VOCAB_SIZE> \
    -tgt_vocab_size <TGT_VOCAB_SIZE> \
    -src_seq_length <SRC_LEN> \
    -src_seq_length_trunc <SRC_LEN> \
    -tgt_seq_length 100 \
    -tgt_seq_length_trunc 100 \
    -dynamic_dict \
    -overwrite
```

<SRC_LEN> = 600 for model_cc, 400 for model_c

# Dataset Details

Our database includes a total of 35 tables, providing useful information about changes codes, reviewer's details, review time, review and corresponding response etc. We highlight some major table from our database below. The full ERD diagram is attached [here](https://github.com/Review4Repair/Review4Repair/blob/master/images/erd.pdf).

![](https://github.com/Review4Repair/Review4Repair/blob/master/images/erd.png)

To reproduce a particular database <DB_NAME>, follow this following query.

```
cd PROJECT_NAME
mysql -u root -p <DB_NAME> < <PROJECT_NAME>.sql
```

## Dataset Structure

The dataset follows the following architecture. 

```bash
├───acumos
  ├───acumos.sql
  ├───gerrit_db_acumos.json
  └───Downloaded_Codes_acumos.zip
      ├───acumos_1
        ├───1.java
        ├───2.java
        └───....
      ├───....  
      └───acumos_MapJSON.json
├───android
├───....
├───unicorn
└───unified_with_date.json
```

We provide the database of each project seperately for modularity. The database and downloaded codes are provided in each project folder. The downloaded codes are provided in a zipped file. Under each folder in the zipped file, there are multiple versions of the same file at different commit times. Each zipped folder also contains a <PROJECT_NAME_>MapJSON file that maps the folder name with the corresponding one in the database and json file. Each project folder contains a Json file named gerrit_db_<PROJECT_NAME> which contains the code review with the Java code file name before and after the change. A sample example is given below:
```
{
		"comment_id" : "3a045101_dd7d55b3",
		"message" : "GROUP_INTERFACE string given is wrong.Please modify.",
		"file_name" : "android-android_api-base-src-main-java-org-iotivity-base-OcPlatform.java",
		"line_number" : 47,
		"base_patch_number" : 3,
		"changed_patch_number" : 5,
		"line_change" : 1
}
```

To create a Json from database for other programming language, for example c, execute the following query and export as a Json:
```
use <DB_NAME> ;
select c.comment_id, cu.message, c.base_code as file_name, c.line_number, 
c.base_patch_number, c.changed_patch_number, cu.line_change, cu.written_on 
from code c
inner join comment_usefulness cu
on c.comment_id = cu.comment_id
where c.changed_patch_number != -1
and c.base_code like "%.c"
and cu.in_reply_to is null;

```

The root directory contains a file named 'unified_with_date.json' which contains the combined query result of the fifteen projects. The entire dataset is also available in [Mega](https://mega.nz/folder/1Zo2wQpT#tXx8xE3UfmRWWcyMmxHf4g).

# Change Calculation Tool

## Input Format:

### Find Change Closer to a Specific Line:
**Input:** `linediff file1 file2 line_number change_window_size`

**Output:**
change_type <|sep|> input_code <|sep|> output_code

### Find All Diffs between Two Files:

**Input:** `alldiff file1 file2 max_target_len max_input_len`

**Output:**

If no one line change found:
`<|nochange|>`

If change found:

input_code <|sep|> output_code <|datasep|> -- repeat

1. Separate with <|datasep|> to get the changes
2. Split one datapoint with <|sep|>

### Find Line Numbers of All One Line Changes between Two Files:

**Input:** `oneliner file1 file2`

**Output:**

If no one line change found:
`<|nochange|>`

Else:

original_pos1 revised_pos1

original_pos2 revised_pos2

original_pos3 revised_pos3

.
.
.

(split with newline, then split with space)
