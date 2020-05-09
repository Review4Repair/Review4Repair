from sys import argv
from os import path
import os
out_file = "prediction.txt" # predicted file path
checkpoint = str(argv[3]) #checkpoint path
#print("######### ", path.exists("output/c/"+out_file))
src_file = str(argv[1])
tgt_file = str(argv[2])
if not path.exists(out_file):
    os.system("""onmt_translate -gpu 0 \
               -batch_size 30 \
               -model {} \
               -src {} \
               -output {} \
               -max_length 200 \
               -stepwise_penalty \
               -beta 5 \
               -length_penalty wu \
               -alpha 0.9 \
               -replace_unk \
               -block_ngram_repeat 3 \
               -ignore_when_blocking "." "</t>" "<t>" \
               -n_best 10 \
               -beam_size 10
               """.format(checkpoint,src_file, out_file))

pred = open(out_file, 'r', encoding= 'utf-8').read() 
tgt = open(tgt_file, 'r', encoding= 'utf-8').read()

beam_size = 10
preds = pred.split('\n')[:-1]
tgts = tgt.split('\n')[:-1]
print(len(preds))
print(len(tgts))
print("beam match = ", len(preds)== len(tgts)*beam_size) # this should be true otherwise beam size and output file doesn't match

correct_1 = 0
correct_5 = 0
correct_10 = 0
for i in range(len(tgts)):
    min_dis = 10000
    match_idx = -1
    for j in range(10):
        if(tgts[i]==preds[i*beam_size+j]):
            if j <=0:
                correct_1 +=1
            if j <=4:
                correct_5 +=1
            if j <=9:
                correct_10 +=1
print("top-1", correct_1/len(tgts))
print("top-5",correct_5/len(tgts))
print("top-10",correct_10/len(tgts))

'''
unzip model.zip
evaluation.py <test_set_src> <test_set_tgt> <model_name> 

Predict on new data:
prediction.py <test_set_src> <model_name>
'''