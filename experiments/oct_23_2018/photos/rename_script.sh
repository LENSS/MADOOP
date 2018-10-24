counter=1
for f in *.jpg; do
    mv "$f" "$((counter++)).jpg"
done
