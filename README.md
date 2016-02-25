# StackedMstParser

Fork of code from "Stacking Dependency Parsers", Martins et al., EMNLP 2008,
as released as part of [SEMAFOR 2.1](https://github.com/Noahs-ARK/semafor-semantic-parser).
Please cite the [paper](http://www.aclweb.org/anthology/D08-1017) ([bib](http://www.aclweb.org/anthology/D08-1017.bib)) if you use this software in your research.

I'm using this forked version in [SEMAFOR > 3.0](https://github.com/Noahs-ARK/semafor).
SEMAFOR's accuracy is consistently better (by about 1 pt F1) when using StackedMstParser
than when using MaltParser or 3rd-order TurboParser.
I didn't write most of this code; I just made it faster, the models smaller, and the output CoNLL compliant.
I'm not planning on providing much maintainence or support.
Feel free to open tickets, but no promises.


## Compiling

    ./scripts/compile.sh


## Running as a socket server

    ./scripts/runServer.sh <model-file> [<port>]

A pretrained model (trained on the Wall Street Journal section of Penn Treebank)
is available [here](http://www.cs.cmu.edu/~ark/StackedMstParser/wsj.model).

`<port>` defaults to `12345`


## Parsing files by connecting to a running server

    ./scripts/runClient.sh <input-file> <output-file> [<server-hostname> [<port>]]

`<server-hostname>` defaults to `localhost`

`<port>` defaults to `12345`


## Training

I haven't tried to retrain the model.
If you want to retrain the model, you're on your own, but this might work (probably not):

    ./scripts/trainTestEval.sh
