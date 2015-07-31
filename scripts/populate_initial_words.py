

lines = open("../data/dict.txt").read().split("\n")
output = open("../app/src/main/res/raw/words.txt", "w")
for ln in lines:
    if "--" in ln:
        fw, nws = ln.split("--")
        nws = nws.split(",")
        fw = fw.strip()
        nws = map(lambda x: x.strip(), nws)
        for n in nws:
            output.write(fw + ";" + n + "\n")