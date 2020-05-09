from sys import argv
from os import path
import os
out_file = "prediction_output.txt" # predicted file path
checkpoint = str(argv[2]) #checkpoint path
#print("######### ", path.exists("output/c/"+out_file))
src_file = str(argv[1])
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