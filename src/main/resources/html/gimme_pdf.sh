for f in $(find . -name "index.html*"); do
  cat $f | pup "a attr{href}" | grep PageFiles
done
