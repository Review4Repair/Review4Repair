# Review4Repair

This repository contains the dataset for "Review4Repair: Automatic Program Repairing Aided with Natural Language Code Review". We mine 15 Gerrit projects and collect a total of 9,48,670 code reviews among which 1,03,990 are from Java.

# Dataset Details

Our database includes a total of 35 tables, providing useful information about changes codes, reviewer's details, review time, review and corresponding response etc. We highlight some major table from our database below. The full ERD diagram is attached [here](https://github.com/master/erd.pdf).

![](https://github.com/master/erd.png)

To reproduce a particular database <DB_NAME>, follow this following query.

```
cd PROJECT_NAME
mysql -u root -p <DB_NAME> < <PROJECT_NAME>.sql
```

## File Structure

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

The root directory contains a file named 'unified_with_date.json' which contains the combined query result of the fifteen projects. The entire dataset is also available in [Google Drive](https://drive.google.com/).
